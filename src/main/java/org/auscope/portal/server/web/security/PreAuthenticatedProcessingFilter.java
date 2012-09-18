package org.auscope.portal.server.web.security;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * This AbstractPreAuthenticatedProcessingFilter implementation obtains the
 * username from request header pre-populated by an external OpenID
 * authentication system.
 *
 * @Author Shane Bailie
 * @author Josh Vote
 */
public class PreAuthenticatedProcessingFilter extends
        AbstractPreAuthenticatedProcessingFilter {

    protected final Logger logger = Logger.getLogger(getClass());

    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        SecurityContextImpl context = (SecurityContextImpl) request
                .getSession().getAttribute("SPRING_SECURITY_CONTEXT");

        if (context != null) {
            OpenIDAuthenticationToken auth = (OpenIDAuthenticationToken)context.getAuthentication();

            // get user granted role(s)
            PortalUser portalUser = (PortalUser) auth.getPrincipal();
            Object[] gas = portalUser.getAuthorities().toArray();
            String[] userRoles = new String[gas.length];
            for (int i=0; i < gas.length; i++) {
                userRoles[i] = ((GrantedAuthority)gas[i]).getAuthority();
            }
            request.getSession().setAttribute("user-roles", userRoles);

            List<OpenIDAttribute> attribs = auth.getAttributes();
            String userEmail = null;

            // get the openid email address from
            for (OpenIDAttribute attrib : attribs) {
                if ("email".equals(attrib.getName())) {
                    List<String> values = attrib.getValues();
                    userEmail = values.get(0);
                    request.getSession().setAttribute("openID-Email", userEmail);
                    logger.info("openID email: " + userEmail);
                }
            }

            if (userEmail == null) {
                logger.error("Could not get openid email attribute");
                return false;
            }
        }

        return request.getSession().getAttribute("openID-Email");
    }

    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        // no password - user is already authenticated
        return "NONE";
    }
}
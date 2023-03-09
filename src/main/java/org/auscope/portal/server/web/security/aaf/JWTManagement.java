package org.auscope.portal.server.web.security.aaf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.security.PortalUser.AuthenticationFramework;
import org.auscope.portal.server.web.service.PortalUserDetailsService;
import org.auscope.portal.server.web.service.PortalUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wis056 on 7/04/2015.
 * Modified by woo392.
 */

@Component
public class JWTManagement {

    static private String AAF_PRODUCTION = "https://rapid.aaf.edu.au";
    static private String AAF_TEST = "https://rapid.test.aaf.edu.au";

    @Autowired
    private PortalUserDetailsService userDetailsService;

    @Autowired
    private PortalUserService userService;

    @Value("${spring.security.jwt.aaf.jwtsecret}")
    private String jwtSecret;

    @Value("${portalUrl}")
    private String rootServiceUrl;

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public void setRootServiceUrl(String rootServiceUrl) {
        this.rootServiceUrl = rootServiceUrl;
    }

    public AAFAuthentication parseJWT(String tokenString) throws AuthenticationException {
        if (tokenString == null)
            throw new AuthenticationCredentialsNotFoundException("Unable to authenticate. No AAF credentials found.");
        try {
            SignedJWT signedJWT = SignedJWT.parse(tokenString);
            // Verify token
            JWSVerifier verifier = new MACVerifier(jwtSecret);
            JWSObject jwsObject = JWSObject.parse(tokenString);
            if (!jwsObject.verify(verifier))
                throw new AuthenticationServiceException("Unabe to authenticate. The token could not be "
                        + "verified. Please check the JWS secret in settings");
            // Validate claims
            JWTClaimsSet claimSet = signedJWT.getJWTClaimsSet();
            ObjectMapper mapper = new ObjectMapper();
            AAFJWT token = mapper.readValue(claimSet.toString(), AAFJWT.class);
            if (!(token.aafServiceUrl.equals(AAF_PRODUCTION) || token.aafServiceUrl.equals(AAF_TEST)))
                throw new AuthenticationServiceException(
                        "Unable to authenticate. The AAF URL does not match the expected "
                                + "value for the test or production AAF Rapid Connect services. Expected "
                                + AAF_PRODUCTION + " or " + AAF_TEST + " but got " + token.aafServiceUrl);
            // Make sure a trailing slash on the end of portal URL or token URL doesn't trip
            // us up
            if (rootServiceUrl.endsWith("/")) {
                rootServiceUrl = rootServiceUrl.substring(0, rootServiceUrl.length() - 1);
            }
            String tokenServiceUrl = token.localServiceUrl;
            if (tokenServiceUrl.endsWith("/")) {
                tokenServiceUrl = tokenServiceUrl.substring(0, tokenServiceUrl.length() - 1);
            }
            if (!(tokenServiceUrl.equals(rootServiceUrl)))
                throw new AuthenticationServiceException("Unable to authenticate. The URL of this server, "
                        + rootServiceUrl + " does not match the URL " + "registered with AAF " + tokenServiceUrl);

            Date now = new Date();
            if (now.before(token.notBefore))
                throw new AuthenticationServiceException("Unable to authenticate. The authentication is "
                        + "marked to not be used before the current date, " + now.toString());

            if (token.expires.before(now))
                throw new AuthenticationServiceException("Unable to authenticate. The authentication has "
                        + "expired. Now: " + now.toString() + " expired: " + token.expires.toString());

            // We're not currently storing tokens in order to check for replay attacks
            /*
            try {
                this.jdbcTemplate.update(INSERT, token.replayPreventionToken);
            } catch (DataAccessException e) {
                logger.error(e);
                throw new AuthenticationServiceException("Unable to authenticate. The replay attack prevention "
                + "token already exists, so this is probably a replay attack.");
             }
             */
            PortalUser anvglUser = registerAAFUser(token.attributes);
            return new AAFAuthentication(anvglUser, token.attributes, token, true);
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getLocalizedMessage());
        }
    }

    private PortalUser registerAAFUser(AAFAttributes attributes) {
        PortalUser anvglUser = userService.getByEmail(attributes.email);
        if (anvglUser == null) {
            Map<String, String> userAttributes = new HashMap<String, String>();
            userAttributes.put("email", attributes.email);
            if (attributes.displayName != null && !attributes.displayName.equals(""))
                userAttributes.put("name", attributes.displayName);
            else
                userAttributes.put("name", attributes.email);
            anvglUser = (PortalUser) userDetailsService.createNewUser(attributes.email, AuthenticationFramework.AAF,
                    userAttributes);
        }
        return anvglUser;
    }

}
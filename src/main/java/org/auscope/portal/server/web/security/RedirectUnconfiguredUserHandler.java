package org.auscope.portal.server.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

/**
 * Class for redirecting a user that isn't setup correctly to the user setup page.
 * @author Josh Vote (CSIRO)
 *
 */
public class RedirectUnconfiguredUserHandler implements AuthenticationSuccessHandler {

    private final Log log = LogFactory.getLog(getClass());

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
            throws IOException, ServletException {
        Object principal = auth.getPrincipal();
        if (principal instanceof ANVGLUser) {
            if (!((ANVGLUser) principal).isFullyConfigured()) {
                response.sendRedirect("../user.html");
                return;
            }
        }

        DefaultSavedRequest savedRequest = (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        if (savedRequest != null) {
            response.sendRedirect(savedRequest.getRequestURL());
            return;
        }

        response.sendRedirect("../");
    }
}

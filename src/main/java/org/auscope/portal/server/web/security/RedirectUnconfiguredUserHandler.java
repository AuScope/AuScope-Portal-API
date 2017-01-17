package org.auscope.portal.server.web.security;

import java.io.IOException;
import java.net.URL;

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

    @SuppressWarnings("unused")
    private final Log log = LogFactory.getLog(getClass());

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
            throws IOException, ServletException {
        Object principal = auth.getPrincipal();
        DefaultSavedRequest savedRequest = (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");

        if (principal instanceof ANVGLUser) {
            if (!((ANVGLUser) principal).isFullyConfigured()) {
                String params = "";
                if (savedRequest != null) {
                    URL requestUrl = new URL(savedRequest.getRequestURL());
                    if (!requestUrl.getPath().contains("login.html")) {
                        params = "?next=" + requestUrl.getPath();
                    }
                }
                response.sendRedirect("../user.html" + params);
                return;
            }
        }

        if (savedRequest != null) {
            response.sendRedirect(savedRequest.getRequestURL());
            return;
        }

        response.sendRedirect("../");
    }
}

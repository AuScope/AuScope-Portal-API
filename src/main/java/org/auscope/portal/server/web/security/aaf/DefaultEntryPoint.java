package org.auscope.portal.server.web.security.aaf;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * Default authentication endpoint that will restrict access to the secure
 * URLs, while the authorisation itself is assigned to separate endpoints.
 *  
 * Feels like overkill.
 * 
 * @author woo392
 *
 */
public class DefaultEntryPoint extends BasicAuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        // Redirect to login page
        response.sendRedirect("/login.html");
    }
    
    /*
    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        super.afterPropertiesSet();
    }
    */
}

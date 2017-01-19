package org.auscope.portal.server.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * Catch unauthorised entry attempts so we can redirect to the login choice
 * page ("login.html") instead of the OAuth2 filter login screen.
 * 
 * @author woo392
 *
 */
public class DefaultEntryPoint extends BasicAuthenticationEntryPoint {
    
    /*
    private String aafLoginUrl;
    
    public DefaultEntryPoint() {
        
    }
    
    @Autowired
    public DefaultEntryPoint(@Value("${HOST.aafLoginUrl}") String aafLoginUrl) {
        this.aafLoginUrl = aafLoginUrl;
    }

    public void setAafLoginUrl(String aafLoginUrl) {
        this.aafLoginUrl = aafLoginUrl;
    }
    */
    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        if(authException != null)
            response.sendRedirect("login.html");
        //response.addHeader("aafLoginUrl", aafLoginUrl);
    }
}

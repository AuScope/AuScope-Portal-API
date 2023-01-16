package org.auscope.portal.server.web.security.aaf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by wis056 on 8/04/2015.
 * Modified by woo392.
 */
public class AAFAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	
	@Value("${spring.security.jwt.aaf.callbackUrl}")
	private String aafCallbackUrl;
    
	public AAFAuthenticationFilter(AuthenticationManager authenticationManager, String aafCallbackUrl) {
    	super(new AntPathRequestMatcher(aafCallbackUrl, "POST"));
    	this.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        boolean postOnly = true;
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        String aafAssertion = request.getParameter("assertion");
        return this.getAuthenticationManager().authenticate(new AAFAuthenticationToken(aafAssertion));
    }
}

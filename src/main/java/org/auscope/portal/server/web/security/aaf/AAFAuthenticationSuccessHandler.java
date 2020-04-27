package org.auscope.portal.server.web.security.aaf;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 
 * @author woo392
 *
 */
@Component
public class AAFAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	
	@Value("${frontEndUrl}")
	private String frontEndUrl;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		//authentication.setAuthenticated(true);
		
		// Redirect to front end
		response.sendRedirect(frontEndUrl + "/login/loggedIn");
	}

}

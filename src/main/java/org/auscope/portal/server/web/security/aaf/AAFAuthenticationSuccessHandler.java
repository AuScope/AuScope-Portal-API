package org.auscope.portal.server.web.security.aaf;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Redirect successful AAF logins to the front end
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
		response.sendRedirect(frontEndUrl + "/login/loggedIn");
	}

}

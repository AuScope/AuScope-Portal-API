package org.auscope.portal.server.web.security;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.server.web.service.ANVGLUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 
 * @author woo392
 *
 */
@Component
public class GoogleAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	@Value("${frontEndUrl}")
	private String frontEndUrl;
	
	@Autowired
	@Qualifier("persistedUserGoogleDetailsLoader")
	PersistedGoogleUserDetailsLoader detailsLoader;

	@Autowired
	private ANVGLUserService userService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		// See if user already in DB, persist if not
		OAuth2Authentication oauth = (OAuth2Authentication) authentication;
		Authentication userAuth = oauth.getUserAuthentication();
		@SuppressWarnings("unchecked")
		LinkedHashMap<String, Object> userDetails = (LinkedHashMap<String, Object>) userAuth.getDetails();
		ANVGLUser user = userService.getById(userDetails.get("sub").toString());
		if (user == null) {
			String id = userDetails.get("sub").toString();
			userDetails.put("id", id);
			detailsLoader.createUser(id, userDetails);
		}

		// Redirect to front end
		response.sendRedirect(frontEndUrl + "/login/loggedIn");
	}

}

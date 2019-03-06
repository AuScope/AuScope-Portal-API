package org.auscope.portal.server.web.security.aaf;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.ANVGLUserService;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	private ANVGLUserService userService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		// See if user already in DB, persist if not
		ANVGLUser user = userService.getByFullName(  ((AAFAuthentication)authentication).getDetails().displayName  );
		if(user == null) {
			System.out.println("AAFSuccess: User NOT FOUND");
			
			/*
			Authentication auth = (OAuth2Authentication)authentication; 
			Authentication userAuth = oauth.getUserAuthentication();
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> userDetails = (LinkedHashMap<String, Object>)userAuth.getDetails();
			String id = userDetails.get("sub").toString();
			String name = userDetails.get("name").toString();
			String email = userDetails.get("email").toString();
			ANVGLUser newUser = new ANVGLUser(id, name, email, new ArrayList<ANVGLAuthority>(), new ArrayList<VGLBookMark>());
			newUser.setAcceptedTermsConditions(0);
			newUser.setArnExecution("");
			newUser.setArnStorage("");
			newUser.setAwsKeyName("");
			newUser.setAwsSecret("");
			newUser.setNciDetails(new NCIDetails());
			userService.saveUser(newUser);
			*/
			
		}
		
		//authentication.setAuthenticated(true);
		
		// Redirect to front end
		response.sendRedirect(frontEndUrl + "/login/loggedIn");
	}

}

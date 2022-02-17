package org.auscope.portal.server.web.service;

import java.util.Map;

import org.auscope.portal.server.web.repositories.ANVGLUserRepository;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.VGLUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;


/**
 * Service to provide access to the user repository
 * 
 * @author woo392
 *
 */
@Service
public class ANVGLUserService {

	@Autowired
	private ANVGLUserRepository userRepository;
	
	public ANVGLUser getLoggedInUser() {
		Authentication userAuth = SecurityContextHolder.getContext().getAuthentication();
		String userEmail = "";
		// Google/Github OAuth2
		if(userAuth instanceof OAuth2AuthenticationToken) {
			Map<String, Object> userDetails = ((OAuth2AuthenticationToken)userAuth).getPrincipal().getAttributes();
			// Google
			if(userDetails != null) {
				userEmail = (String)userDetails.get("email");
			}
			// Github
			else {
				userEmail = ((VGLUserPrincipal)((OAuth2AuthenticationToken)userAuth).getPrincipal()).getEmail();
			}
		}
		// Only other supported Authentication is AAFAuthentication, where
		// unique name has been set to user's email
		else {
			userEmail = userAuth.getName();
		}
		return getByEmail(userEmail);
	}
	
	public ANVGLUser getById(String id) {
		ANVGLUser user = userRepository.findById(id).orElse(null);
		return user;
	}
	
	public ANVGLUser getByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	public ANVGLUser getByFullName(String fullName) {
		return userRepository.findByFullName(fullName);
	}
	
	public void saveUser(ANVGLUser user) {
		userRepository.save(user);
	}

	public void deleteUser(ANVGLUser user) {
		userRepository.delete(user);
	}

}

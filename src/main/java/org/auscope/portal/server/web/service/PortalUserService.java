package org.auscope.portal.server.web.service;

import java.util.Map;

import org.auscope.portal.server.web.repositories.PortalUserRepository;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.security.PortalUserPrincipal;
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
public class PortalUserService {

	@Autowired
	private PortalUserRepository userRepository;
	
	public PortalUser getLoggedInUser() {
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
				userEmail = ((PortalUserPrincipal)((OAuth2AuthenticationToken)userAuth).getPrincipal()).getEmail();
			}
		}
		// Only other supported Authentication is AAFAuthentication, where
		// unique name has been set to user's email
		else {
			userEmail = userAuth.getName();
		}
		return getByEmail(userEmail);
	}
	
	public PortalUser getById(String id) {
		PortalUser user = userRepository.findById(id).orElse(null);
		return user;
	}
	
	public PortalUser getByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	public PortalUser getByFullName(String fullName) {
		return userRepository.findByFullName(fullName);
	}
	
	public void saveUser(PortalUser user) {
		userRepository.save(user);
	}

	public void deleteUser(PortalUser user) {
		userRepository.delete(user);
	}

}

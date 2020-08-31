package org.auscope.portal.server.web.security.google;

import java.util.HashMap;

import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.ANVGLUser.AuthenticationFramework;
import org.auscope.portal.server.web.service.ANVGLUserDetailsService;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Google OIDC class for persisting users
 * 
 * @author woo392
 *
 */
@Service
public class GoogleOidcUserService extends OidcUserService {

	@Autowired
	private ANVGLUserService userService;
	
	@Autowired
	private ANVGLUserDetailsService userDetailsService;

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser oidcUser = super.loadUser(userRequest);
		try {
			return processOidcUser(userRequest, oidcUser);
		} catch (Exception ex) {
			throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
		}
	}

	private OidcUser processOidcUser(OidcUserRequest userRequest, OidcUser oidcUser) {
		GoogleUserInfo googleUserInfo = new GoogleUserInfo(oidcUser.getAttributes());
		ANVGLUser userOptional = userService.getByEmail(googleUserInfo.getEmail());
		if (userOptional == null) {
			createNewUser(googleUserInfo);
		}
		return oidcUser;
	}

	/**
	 * Create and persist an ANVGLUSer
	 * 
	 * @param googleUserInfo the Google info from which to build the user
	 * @return an ANVGLUser created from the Google info
	 */
	public ANVGLUser createNewUser(GoogleUserInfo googleUserInfo) {
		HashMap<String, String> userDetails = new HashMap<String, String>();
		userDetails.put("name", googleUserInfo.getName());
		userDetails.put("email", googleUserInfo.getEmail());
		return userDetailsService.createNewUser(googleUserInfo.getId(),
				AuthenticationFramework.GOOGLE, userDetails);
	}
	
}
package org.auscope.portal.server.web.security.google;

import java.util.HashMap;

import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.security.PortalUser.AuthenticationFramework;
import org.auscope.portal.server.web.service.PortalUserDetailsService;
import org.auscope.portal.server.web.service.PortalUserService;
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
	private PortalUserService userService;
	
	@Autowired
	private PortalUserDetailsService userDetailsService;

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
		PortalUser userOptional = userService.getByEmail(googleUserInfo.getEmail());
		if (userOptional == null) {
			createNewUser(googleUserInfo);
		}
		return oidcUser;
	}

	/**
	 * Create and persist a PortalUser
	 * 
	 * @param googleUserInfo the Google info from which to build the user
	 * @return a PortalUser created from the Google info
	 */
	public PortalUser createNewUser(GoogleUserInfo googleUserInfo) {
		HashMap<String, String> userDetails = new HashMap<String, String>();
		userDetails.put("name", googleUserInfo.getName());
		userDetails.put("email", googleUserInfo.getEmail());
		return userDetailsService.createNewUser(googleUserInfo.getId(),
				AuthenticationFramework.GOOGLE, userDetails);
	}
	
}
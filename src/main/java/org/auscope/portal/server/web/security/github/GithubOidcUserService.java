package org.auscope.portal.server.web.security.github;

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
 * Guthub OIDC class for persisting users
 * TDO: Look at combining, do we need this and Google equivalent?
 * 
 * @author woo392
 *
 */
@Service
public class GithubOidcUserService extends OidcUserService {

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
		GithubUserInfo githubUserInfo = new GithubUserInfo(oidcUser.getAttributes());
		PortalUser userOptional = userService.getByEmail(githubUserInfo.getEmail());
		if (userOptional == null) {
			createNewUser(githubUserInfo);
		}
		return oidcUser;
	}

	/**
	 * Create and persist a PortalUser
	 * 
	 * @param githubUserInfo the Github info from which to build the user
	 * @return a PortalUser created from the Github info
	 */
	public PortalUser createNewUser(GithubUserInfo githubUserInfo) {
		HashMap<String, String> userDetails = new HashMap<String, String>();
		userDetails.put("name", githubUserInfo.getName());
		userDetails.put("email", githubUserInfo.getEmail());
		return userDetailsService.createNewUser(githubUserInfo.getId(),
				AuthenticationFramework.GITHUB, userDetails);
	}
	
}
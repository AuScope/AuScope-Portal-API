package org.auscope.portal.server.web.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.auscope.portal.server.web.security.PortalAuthority;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.security.PortalUser.AuthenticationFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Service for creating new users
 * 
 * @author woo392
 *
 */
public class PortalUserDetailsService implements UserDetailsService {
	
	@Autowired
	private PortalUserService userService;
	
	protected SecureRandom random;
	protected String defaultRole;
	protected Map<String, List<String>> rolesByUser;
	
	/**
	 * Creates a new PortalUserDetalsService that will assign defaultRole to every
	 * user as a granted authority.
	 *
	 * @param defaultRole the default role to apply to the user
	 */
	public PortalUserDetailsService(String defaultRole) {
		this(defaultRole, null);
	}

	/**
	 * Creates a new PortalUserDetailsService that will assign defaultRole to every
	 * user AND any authorities found in rolesByUser if the ID matches the current
	 * user ID
	 *
	 * @param defaultRole the default role to apply to the user
	 * @param rolesByUser a list of roles to apply the user
	 */
	public PortalUserDetailsService(String defaultRole, Map<String, List<String>> rolesByUser) {
		this.defaultRole = defaultRole;
		this.rolesByUser = new HashMap<>();
		this.random = new SecureRandom();
		if (rolesByUser != null) {
			for (Entry<String, List<String>> entry : rolesByUser.entrySet()) {
				List<String> authorityStrings = entry.getValue();
				this.rolesByUser.put(entry.getKey(), authorityStrings);
			}
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if(StringUtils.isBlank(username)) 
            throw new UsernameNotFoundException("User name is empty");
		return this.userService.getById(username);
	}
	
	public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
		if(StringUtils.isBlank(email)) 
            throw new UsernameNotFoundException("Email is empty");
		return this.userService.getByEmail(email);
	}
	
	/**
	 * Create and persist an PortalUser
	 * 
	 * @param googleUserInfo the Google info from which to build the user
	 * @return an PortalUser created from the Google info
	 */
	public PortalUser createNewUser(String id,
			AuthenticationFramework authFramework,
			Map<String, String> userDetails) {
		PortalUser newUser = new PortalUser();
		newUser.setId(id);
		newUser.setEmail(userDetails.get("email"));
		newUser.setFullName(userDetails.get("name"));
		newUser.setAuthentication(authFramework);
		userService.saveUser(newUser);
		// Authorities
		List<PortalAuthority> authorities = new ArrayList<>();
		PortalAuthority defaultAuth = new PortalAuthority(defaultRole);
		defaultAuth.setParent(newUser);
		authorities.add(defaultAuth);
		if (rolesByUser != null) {
			List<String> additionalAuthorities = rolesByUser.get(newUser.getId());
			if (additionalAuthorities != null) {
				for (String authority : additionalAuthorities) {
					PortalAuthority auth = new PortalAuthority(authority);
					auth.setParent(newUser);
					authorities.add(auth);
				}
			}
		}
		newUser.setAuthorities(authorities);
		userService.saveUser(newUser);
		return newUser;
	}
	
}

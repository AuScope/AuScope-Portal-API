package org.auscope.portal.server.web.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomStringUtils;
import org.auscope.portal.server.web.security.ANVGLAuthority;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.ANVGLUser.AuthenticationFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

/**
 * Service for creating new users
 * 
 * @author woo392
 *
 */
public class ANVGLUserDetailsService implements UserDetailsService {
	
	public static final int SECRET_LENGTH = 32;
	private static char[] BUCKET_NAME_WHITELIST = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
	
	@Autowired
	private ANVGLUserService userService;
	
	protected SecureRandom random;
	protected String defaultRole;
	protected Map<String, List<String>> rolesByUser;
	
	/**
	 * Creates a new ANVGLUserDetalsService that will assign defaultRole to every
	 * user as a granted authority.
	 *
	 * @param defaultRole the default role to apply to the user
	 */
	public ANVGLUserDetailsService(String defaultRole) {
		this(defaultRole, null);
	}

	/**
	 * Creates a new ANVGLUserDetailsService that will assign defaultRole to every
	 * user AND any authorities found in rolesByUser if the ID matches the current
	 * user ID
	 *
	 * @param defaultRole the default role to apply to the user
	 * @param rolesByUser a list of roles to apply the user
	 */
	public ANVGLUserDetailsService(String defaultRole, Map<String, List<String>> rolesByUser) {
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
		if(StringUtils.isEmpty(username)) 
            throw new UsernameNotFoundException("User name is empty");
		return this.userService.getById(username);
	}
	
	public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
		if(StringUtils.isEmpty(email)) 
            throw new UsernameNotFoundException("Email is empty");
		return this.userService.getByEmail(email);
	}
	
	/**
	 * Create and persist an ANVGLUSer
	 * 
	 * @param googleUserInfo the Google info from which to build the user
	 * @return an ANVGLUser created from the Google info
	 */
	public ANVGLUser createNewUser(String id,
			AuthenticationFramework authFramework,
			Map<String, String> userDetails) {
		ANVGLUser newUser = new ANVGLUser();
		newUser.setId(id);
		newUser.setEmail(userDetails.get("email"));
		newUser.setFullName(userDetails.get("name"));
		newUser.setAuthentication(authFramework);
		userService.saveUser(newUser);
		// AWS secret and bucketname
		synchronized (this.random) {
			String randomSecret = RandomStringUtils.random(SECRET_LENGTH, 0, 0, true, true, null, this.random);
			newUser.setAwsSecret(randomSecret);
			String bucketName = generateRandomBucketName();
			newUser.setS3Bucket(bucketName);
		}
		// Authorities
		List<ANVGLAuthority> authorities = new ArrayList<>();
		ANVGLAuthority defaultAuth = new ANVGLAuthority(defaultRole);
		defaultAuth.setParent(newUser);
		authorities.add(defaultAuth);
		if (rolesByUser != null) {
			List<String> additionalAuthorities = rolesByUser.get(newUser.getId());
			if (additionalAuthorities != null) {
				for (String authority : additionalAuthorities) {
					ANVGLAuthority auth = new ANVGLAuthority(authority);
					auth.setParent(newUser);
					authorities.add(auth);
				}
			}
		}
		newUser.setAuthorities(authorities);
		userService.saveUser(newUser);
		return newUser;
	}
	
	/**
	 * Generate a random bucket name for the user
	 * 
	 * @return a random string preceeded with "vgl-"
	 */
	public String generateRandomBucketName() {
        return "vgl-" + RandomStringUtils.random(32, 0, 0, false, false, BUCKET_NAME_WHITELIST, this.random);
    }
}

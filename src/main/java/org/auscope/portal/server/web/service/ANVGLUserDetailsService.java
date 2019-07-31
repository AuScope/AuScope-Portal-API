package org.auscope.portal.server.web.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.auscope.portal.server.web.repositories.ANVGLUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 
 * @author woo392
 *
 */
@Service("userDetailsService")
public class ANVGLUserDetailsService implements UserDetailsService {
	
	@Autowired
	private ANVGLUserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("UserDetailsService - Looking for user: " + username);
		if(StringUtils.isEmpty(username)) 
            throw new UsernameNotFoundException("User name is empty");
		return userRepository.findById(username).orElse(null);
		
		/*
		ANVGLUser aUser = userRepository.findById(username).orElse(null);
		if(aUser != null) {
			User user =  new User(username, aUser.getPassword(), aUser.getAuthorities());
			return user;
		}
		return null;
		*/

		/*
        //if you don't use authority based security, just add empty set
        Set<GrantedAuthority> authorities = new HashSet<>();
        ANVGLUser userDetails = new CustomUserDetails(userName, "", authorities);
        return userDetails;
        */            

        
		

		/*
		User user = userDao.findByUserName(username);
        List<GrantedAuthority> authorities =
                                      buildUserAuthority(user.getUserRole());
		// if you're implementing UserDetails you wouldn't need to call this method and instead return the User as it is
		//return buildUserForAuthentication(user, authorities);
		return user;
		*/
	}
	
	/*
	private List<GrantedAuthority> buildUserAuthority(Set<UserRole> userRoles) {
        Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();
        // add user's authorities
        for (UserRole userRole : userRoles) {
            setAuths.add(new SimpleGrantedAuthority(userRole.getRole()));
        }
        List<GrantedAuthority> Result = new ArrayList<GrantedAuthority>(setAuths);
        return Result;
    }
    */
}

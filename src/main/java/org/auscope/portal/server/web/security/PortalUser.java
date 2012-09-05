package org.auscope.portal.server.web.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;

/**
 * Simple wrapper for the User class which just adds an easier to use constructor
 * @author Josh Vote
 *
 */
public class PortalUser extends User {

    /**
     * Creates a user with a single authority
     * @param name User name
     * @param credentials User credentials
     * @param grantedAuthority The single authority to be granted to this user
     */
    public PortalUser(String name, String credentials, String grantedAuthority) {
        this(name, credentials, Arrays.asList(grantedAuthority));
    }

    /**
     * Creates a user with a list of authorities
     * @param name User name
     * @param credentials User credentials
     * @param grantedAuthorities A set of authorities to grant this user
     */
    public PortalUser(String name, String credentials, List<String> grantedAuthorities) {
        super(name,credentials,true,true,true,true,authoritiesToArray(grantedAuthorities));
    }

    private static GrantedAuthority[] authoritiesToArray(List<String> authorities) {
        GrantedAuthority[] grantedAuthorities = new GrantedAuthority[authorities.size()];
        int i = 0;
        for (String authority : authorities) {
            grantedAuthorities[i++] = new GrantedAuthorityImpl(authority);
        }
        return grantedAuthorities;
    }
}

package org.auscope.portal.server.web.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;

/**
 * Simple wrapper for the User class which just adds an easier to use constructor
 * @author VOT002
 *
 */
public class PortalUser extends User {

    public PortalUser(String name, String credentials, String grantedAuthority) {
        super(name,credentials,true,true,true,true,new GrantedAuthority[] {new GrantedAuthorityImpl(grantedAuthority)});
    }

}

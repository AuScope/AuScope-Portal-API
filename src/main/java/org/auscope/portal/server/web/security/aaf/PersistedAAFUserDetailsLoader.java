package org.auscope.portal.server.web.security.aaf;

import java.util.List;
import java.util.Map;

import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.ANVGLUserService;
import org.auscope.portal.server.web.security.PersistedGoogleUserDetailsLoader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User details loader, almost identical in functionality to
 * {@link PersistedGoogleUserDetailsLoader} but with the ability to retrieve a
 * user by email.
 * 
 * @author woo392
 *
 */
public class PersistedAAFUserDetailsLoader extends PersistedGoogleUserDetailsLoader {
	
	@Autowired
	private ANVGLUserService userService;

    public PersistedAAFUserDetailsLoader(String defaultRole) {
        super(defaultRole);
    }

    public PersistedAAFUserDetailsLoader(String defaultRole, Map<String, List<String>> rolesByUser) {
        super(defaultRole, rolesByUser);
    }

    protected ANVGLUser getUserByUserEmail(String email) {
        //return getUserDao().getByEmail(email);
    	return userService.getByEmail(email);
    }

}

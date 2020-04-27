package org.auscope.portal.server.web.security;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.RandomStringUtils;
import org.auscope.portal.server.web.security.ANVGLUser.AuthenticationFramework;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import com.racquettrack.security.oauth.OAuth2UserDetailsLoader;

/**
 * A OAuth2UserDetailsLoader implementation that persists the ANVGLUser objects to a database
 * @author Josh Vote (CSIRO)
 *
 */
public class PersistedGoogleUserDetailsLoader implements OAuth2UserDetailsLoader<ANVGLUser> {

    public static final int SECRET_LENGTH = 32;


    private static char[] BUCKET_NAME_WHITELIST = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    protected SecureRandom random;
    protected String defaultRole;
    protected Map<String, List<String>> rolesByUser;
    
    @Autowired
    private ANVGLUserService userService;

    /**
     * Creates a new GoogleOAuth2UserDetailsLoader that will assign defaultRole to every user as a granted authority.
     *
     * @param defaultRole
     */
    public PersistedGoogleUserDetailsLoader(String defaultRole) {
        this(defaultRole, null);
    }

    /**
     * Creates a new GoogleOAuth2UserDetailsLoader that will assign defaultRole to every user AND any authorities found in rolesByUser if the ID matches the
     * current user ID
     *
     * @param defaultRole
     * @param rolesByUser
     */
    public PersistedGoogleUserDetailsLoader(String defaultRole, Map<String, List<String>> rolesByUser) {
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

    /**
     * Extracts keys from userInfo and applies them to appropriate properties in user
     *
     * @param user
     * @param userInfo
     */
    protected void applyInfoToUser(ANVGLUser user, Map<String, Object> userInfo) {
        user.setEmail(userInfo.get("email").toString());
        user.setFullName(userInfo.get("name").toString());
        user.setId(userInfo.get("id").toString());
    }

    protected String generateRandomBucketName() {
        return "vgl-" + RandomStringUtils.random(32, 0, 0, false, false, BUCKET_NAME_WHITELIST, this.random);
    }

    @Override
    public ANVGLUser getUserByUserId(String id) {
    	return userService.getById(id);
    }

    @Override
    public boolean isCreatable(Map<String, Object> userInfo) {
        return userInfo.containsKey("id");
    }

    @Override
    public UserDetails createUser(String id, Map<String, Object> userInfo) {
        ANVGLUser newUser = new ANVGLUser();
        applyInfoToUser(newUser, userInfo);
        newUser.setAuthentication(AuthenticationFramework.GOOGLE);
        userService.saveUser(newUser); //create our new user
        
        synchronized(this.random) {
            //Create an AWS secret for this user
            String randomSecret = RandomStringUtils.random(SECRET_LENGTH, 0, 0, true, true, null, this.random);
            newUser.setAwsSecret(randomSecret);

            //Create a random bucket name for this user
            String bucketName = generateRandomBucketName();
            newUser.setS3Bucket(bucketName);
        }
        
        List<ANVGLAuthority> authorities = new ArrayList<>();
        ANVGLAuthority defaultAuth = new ANVGLAuthority(defaultRole);
        defaultAuth.setParent(newUser);
        authorities.add(defaultAuth);
        if (rolesByUser != null) {
            List<String> additionalAuthorities = rolesByUser.get(id);
            if (additionalAuthorities != null) {
                for (String authority : additionalAuthorities) {
                	ANVGLAuthority auth = new ANVGLAuthority(authority);
                	auth.setParent(newUser);
                    authorities.add(auth);
                }
            }
        }
        
        newUser.setAuthorities(authorities);
        userService.saveUser(newUser); //apply authorities (so they inherit the ID)

        return newUser;
    }

    @Override
    public UserDetails updateUser(UserDetails userDetails,
            Map<String, Object> userInfo) {

        if (userDetails instanceof ANVGLUser) {
            applyInfoToUser((ANVGLUser) userDetails, userInfo);
            userService.saveUser((ANVGLUser) userDetails);
        }

        return userDetails;
    }
}

package org.auscope.portal.server.web.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.racquettrack.security.oauth.OAuth2UserDetailsLoader;

/**
 * A OAuth2UserDetailsLoader implementation that persists the ANVGLUser objects to a database
 * @author Josh Vote (CSIRO)
 *
 */
public class PersistedGoogleUserDetailsLoader implements OAuth2UserDetailsLoader<ANVGLUser> {

    public static final int SECRET_LENGTH = 32;


    protected SecureRandom random;
    protected String defaultRole;
    protected Map<String, List<String>> rolesByUser;
    private ANVGLUserDao userDao;

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
        this.rolesByUser = new HashMap<String, List<String>>();
        this.random = new SecureRandom();
        if (rolesByUser != null) {
            for (Entry<String, List<String>> entry : rolesByUser.entrySet()) {
                List<String> authorityStrings = entry.getValue();
                this.rolesByUser.put(entry.getKey(), authorityStrings);
            }
        }
    }

    /**
     * The DAO that will be used to fetch/set users
     * @return
     */
    public ANVGLUserDao getUserDao() {
        return userDao;
    }

    /**
     * The DAO that will be used to fetch/set users
     * @param userDao
     */
    public void setUserDao(ANVGLUserDao userDao) {
        this.userDao = userDao;
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

    @Override
    public ANVGLUser getUserByUserId(String id) {
        return userDao.getById(id);
    }

    @Override
    public boolean isCreatable(Map<String, Object> userInfo) {
        return userInfo.containsKey("id");
    }

    @SuppressWarnings("unchecked")
    @Override
    public UserDetails createUser(String id, Map<String, Object> userInfo) {
        List<ANVGLAuthority> authorities = new ArrayList<ANVGLAuthority>();
        authorities.add(new ANVGLAuthority(defaultRole));
        if (rolesByUser != null) {
            List<String> additionalAuthorities = rolesByUser.get(id);
            if (additionalAuthorities != null) {
                for (String authority : additionalAuthorities) {
                    authorities.add(new ANVGLAuthority(authority));
                }
            }
        }

        ANVGLUser newUser = new ANVGLUser();
        applyInfoToUser(newUser, userInfo);
        userDao.save(newUser); //create our new user

        synchronized(this.random) {
            //Create an AWS secret for this user
            String randomSecret = RandomStringUtils.random(SECRET_LENGTH, 0, 0, true, true, null, this.random);
            newUser.setAwsSecret(randomSecret);

            //Create a random bucket name for this user
            String storageBucketSalt = RandomStringUtils.random(8, 0, 0, true, true, null, this.random);
            String storageBucketSeed = "anvgl-" + id + storageBucketSalt;

            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                String hashedSeed = Base64.encodeBase64String(md.digest(storageBucketSeed.getBytes("UTF-8")));
                String bucketName = ("ANVGL-" + hashedSeed).replace('=', '_').replace('/', '-').replace('+', '-');

                if (bucketName.length() > 63) {
                    bucketName = bucketName.substring(0, 63);
                }

                newUser.setS3Bucket(bucketName);
            } catch (Exception ex) {
                //How can we handle this other than fail hard?
                throw new RuntimeException("Unable to encode bucket name", ex);
            }
        }
        newUser.setAuthorities(authorities);
        userDao.save(newUser); //apply authorities (so they inherit the ID)

        return newUser;
    }

    @Override
    public UserDetails updateUser(UserDetails userDetails,
            Map<String, Object> userInfo) {

        if (userDetails instanceof ANVGLUser) {
            applyInfoToUser((ANVGLUser) userDetails, userInfo);
            userDao.save((ANVGLUser) userDetails);
        }

        return userDetails;
    }
}

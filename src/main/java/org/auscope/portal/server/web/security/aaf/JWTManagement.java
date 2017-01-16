package org.auscope.portal.server.web.security.aaf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.racquettrack.security.oauth.OAuth2UserDetailsLoader;

import org.auscope.portal.server.web.security.aaf.AAFAuthentication;
import org.auscope.portal.server.web.security.aaf.AAFJWT;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.aaf.AAFAttributes;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by wis056 on 7/04/2015.
 * Modified by woo392.
 */

@Component
public class JWTManagement {
    final static Logger logger = Logger.getLogger(JWTManagement.class);

    private OAuth2UserDetailsLoader<UserDetails> userDetailsLoader;
    private String jwtSecret;
    private String rootServiceUrl;

    public void setUserDetailsLoader(OAuth2UserDetailsLoader<UserDetails> userDetailsLoader) {
        this.userDetailsLoader = userDetailsLoader;
    }
    
    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public void setRootServiceUrl(String rootServiceUrl) {
        this.rootServiceUrl = rootServiceUrl;
    }
    
    static private String AAF_PRODUCTION = "https://rapid.aaf.edu.au";
    static private String AAF_TEST = "https://rapid.test.aaf.edu.au";

    public AAFAuthentication parseJWT(String tokenString) throws AuthenticationException {
        if (tokenString == null)
            throw new AuthenticationCredentialsNotFoundException("Unable to authenticate. No AAF credentials found.");
        Jwt jwt = JwtHelper.decodeAndVerify(tokenString, new MacSigner(jwtSecret.getBytes()));
        String claims = jwt.getClaims();
        logger.debug(claims);
        ObjectMapper mapper = new ObjectMapper();
        try {
            AAFJWT token = mapper.readValue(claims, AAFJWT.class);

            if (!(token.aafServiceUrl.equals(AAF_PRODUCTION) || token.aafServiceUrl.equals(AAF_TEST)))
                throw new AuthenticationServiceException("Unable to authenticate. The AAF URL does not match the expected " +
                        "value for the test or production AAF Rapid Connect services. Expected " + AAF_PRODUCTION +
                        " or " + AAF_TEST + " but got " + token.aafServiceUrl);

            if (!(token.localServiceUrl.equals(rootServiceUrl)))
                throw new AuthenticationServiceException("Unable to authenticate. The URL of this server, "
                        + rootServiceUrl +
                        " does not match the URL registered with AAF " + token.localServiceUrl + " .");

            Date now = new Date();
            if (now.before(token.notBefore))
                throw new AuthenticationServiceException("Unable to authenticate. The authentication is " +
                        "marked to not be used before the current date, " + now.toString());

            if (token.expires.before(now))
                throw new AuthenticationServiceException("Unable to authenticate. The authentication has expired. " +
                        "Now: " + now.toString() + " expired: " + token.expires.toString() );

            /* XXX
            try {
                this.jdbcTemplate.update(INSERT, token.replayPreventionToken);
            } catch (DataAccessException e) {
                logger.error(e);
                throw new AuthenticationServiceException("Unable to authenticate. The replay attack prevention " +
                        "token already exists, so this is probably a replay attack.");
            }
            */
            //boolean didAdd = registerAAFUser(token.attributes);
            ANVGLUser anvglUser = registerAAFUser(token.attributes);
            //logger.debug("First login for AAF user? " + Boolean.toString(didAdd));
            
            return new AAFAuthentication(anvglUser, token.attributes, token, true);
        } catch (IOException e) {
            throw new AuthenticationServiceException(e.getLocalizedMessage());
        }
    }

    private boolean checkUserExists(String username) {
        boolean hasKey = false;

        RowMapper<Boolean> mapper = new RowMapper<Boolean>() {
            public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getBoolean(1);
            }
        };

        /* XXX
        List<Boolean> keys = this.jdbcTemplate.query(KEY_GET, mapper, username);

        if(!keys.isEmpty()) {
            hasKey = keys.get(0);
        }
        */

        return hasKey;
    }

    private ANVGLUser registerAAFUser(AAFAttributes attributes) {
        ANVGLUser anvglUser = null;
        boolean hasKey = checkUserExists(attributes.email);
        if (!hasKey) {
            String userId = UUID.randomUUID().toString();
            Map<String, Object> userAttributes = new HashMap<String, Object>();
            userAttributes.put("id", userId);
            userAttributes.put("email", attributes.email);
            if(attributes.displayName != null && !attributes.displayName.equals(""))
                userAttributes.put("name", attributes.displayName);
            anvglUser = (ANVGLUser)userDetailsLoader.createUser(userId, userAttributes);
        }
        return anvglUser;
    }

}
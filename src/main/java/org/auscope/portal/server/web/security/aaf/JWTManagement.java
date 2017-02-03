package org.auscope.portal.server.web.security.aaf;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.auscope.portal.server.web.security.aaf.AAFAuthentication;
import org.auscope.portal.server.web.security.aaf.AAFJWT;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.ANVGLUser.AuthenticationFramework;
import org.auscope.portal.server.web.security.aaf.AAFAttributes;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wis056 on 7/04/2015.
 * Modified by woo392.
 */

@Component
public class JWTManagement {
    
    static private String AAF_PRODUCTION = "https://rapid.aaf.edu.au";
    static private String AAF_TEST = "https://rapid.test.aaf.edu.au";

    private PersistedAAFUserDetailsLoader userDetailsLoader;
    private String jwtSecret;
    private String rootServiceUrl;

    public void setUserDetailsLoader(PersistedAAFUserDetailsLoader userDetailsLoader) {
        this.userDetailsLoader = userDetailsLoader;
    }
    
    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public void setRootServiceUrl(String rootServiceUrl) {
        this.rootServiceUrl = rootServiceUrl;
    }

    public AAFAuthentication parseJWT(String tokenString) throws AuthenticationException {
        if (tokenString == null)
            throw new AuthenticationCredentialsNotFoundException("Unable to authenticate. No AAF credentials found.");
        Jwt jwt = JwtHelper.decodeAndVerify(tokenString, new MacSigner(jwtSecret.getBytes()));
        String claims = jwt.getClaims();
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

            // XXX We're not currently storing tokens to be in order to check for replay attacks 
            /*
            try {
                this.jdbcTemplate.update(INSERT, token.replayPreventionToken);
            } catch (DataAccessException e) {
                logger.error(e);
                throw new AuthenticationServiceException("Unable to authenticate. The replay attack prevention " +
                        "token already exists, so this is probably a replay attack.");
            }
            */
            
            ANVGLUser anvglUser = registerAAFUser(token.attributes);
            return new AAFAuthentication(anvglUser, token.attributes, token, true);
        } catch (IOException e) {
            throw new AuthenticationServiceException(e.getLocalizedMessage());
        }
    }

    private ANVGLUser registerAAFUser(AAFAttributes attributes) {
        ANVGLUser anvglUser = userDetailsLoader.getUserByUserEmail(attributes.email);
        if (anvglUser == null) {
            Map<String, Object> userAttributes = new HashMap<String, Object>();
            userAttributes.put("id", attributes.email);
            userAttributes.put("email", attributes.email);
            if(attributes.displayName != null && !attributes.displayName.equals(""))
                userAttributes.put("name", attributes.displayName);
            anvglUser = (ANVGLUser)userDetailsLoader.createUser(attributes.email, userAttributes);
            anvglUser.setAuthentication(AuthenticationFramework.AAF);
        }
        return anvglUser;
    }

}
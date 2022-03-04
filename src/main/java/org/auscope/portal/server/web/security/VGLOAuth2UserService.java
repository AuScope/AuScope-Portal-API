package org.auscope.portal.server.web.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.auscope.portal.server.web.repositories.ANVGLUserRepository;
import org.auscope.portal.server.web.security.ANVGLUser.AuthenticationFramework;
import org.auscope.portal.server.web.service.ANVGLUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Note that Google login uses OpenId so this will currently only trigger for Githib logins.
 * 
 * @author woo392
 *
 */
@Service
public class VGLOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private ANVGLUserRepository userRepository;
	
	@Autowired
	ANVGLUserDetailsService userDetailsService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }
    
    
    @SuppressWarnings("rawtypes")
	public String requestEmailAddressFromGithub(OAuth2UserRequest oAuth2UserRequest, Map<String, Object> attributes) throws OAuth2AuthenticationException {
    	String email = "";
    	String emailEndpointUri = "https://api.github.com/user/emails";
    	
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + oAuth2UserRequest.getAccessToken().getTokenValue());
        HttpEntity<?> entity = new HttpEntity<>("", headers);
        ResponseEntity<ArrayList> response = restTemplate.exchange(emailEndpointUri, HttpMethod.GET, entity, ArrayList.class);
        ArrayList responseBody = response.getBody();
        if(responseBody.size() > 0) {
        	String responseString = responseBody.get(0).toString();
        	email = responseString.substring(responseString.indexOf("email=")+6, responseString.indexOf(','));
        }
        return email;
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        VGLOAuth2UserInfo oAuth2UserInfo = VGLOAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getClientName(), oAuth2User.getAttributes());
        String email = "";
        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            email = requestEmailAddressFromGithub(oAuth2UserRequest, oAuth2User.getAttributes());
        } else {
        	email = oAuth2UserInfo.getEmail();
        }
        // Note, this would be findById except we're not persisting authentication framework used to first log in
        ANVGLUser user = userRepository.findByEmail(email);
        if (user != null) {
        	// Reimplement this if we want to overwrite Google login with same email address, but might change name
	        //user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo, email);
        }
        return VGLUserPrincipal.create(user);
    }

    private ANVGLUser registerNewUser(OAuth2UserRequest oAuth2UserRequest, VGLOAuth2UserInfo oAuth2UserInfo, String email) {
    	Map<String, String> attributes = new HashMap<String, String>();
    	attributes.put("name", oAuth2UserInfo.getName());
    	attributes.put("email", email);
    	final AuthenticationFramework authFramework = oAuth2UserRequest.getClientRegistration().getClientName().equalsIgnoreCase("github") ?
    			AuthenticationFramework.GITHUB : AuthenticationFramework.GOOGLE;
    	ANVGLUser user = userDetailsService.createNewUser(oAuth2UserInfo.getId(), authFramework, attributes);
    	return user;
    }

    /*
    // Reimplement this if we want to overwrite Google login with same email address
    private ANVGLUser updateExistingUser(ANVGLUser existingUser, VGLOAuth2UserInfo oAuth2UserInfo) {
        existingUser.setFullName(oAuth2UserInfo.getName());
        return userRepository.save(existingUser);
    }
    */

}
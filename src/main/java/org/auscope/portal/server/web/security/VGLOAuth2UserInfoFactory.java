package org.auscope.portal.server.web.security;

import java.util.Map;

import org.auscope.portal.server.web.security.github.GithubUserInfo;
import org.auscope.portal.server.web.security.google.GoogleUserInfo;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;


public class VGLOAuth2UserInfoFactory {

    public static VGLOAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
    	if(registrationId.equalsIgnoreCase("google")) {
            return new GoogleUserInfo(attributes);
    	} else if (registrationId.equalsIgnoreCase("github")) {
            return new GithubUserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
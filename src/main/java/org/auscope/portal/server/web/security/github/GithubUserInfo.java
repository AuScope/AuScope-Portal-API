package org.auscope.portal.server.web.security.github;

import java.util.Map;

import org.auscope.portal.server.web.security.VGLOAuth2UserInfo;


/**
 * Map of returned Github user attributes
 * 
 * @author woo392
 *
 */
public class GithubUserInfo extends VGLOAuth2UserInfo {

    public GithubUserInfo(Map<String, Object> attributes) {
    	super(attributes);
    }

    public String getId() {
        return (String) attributes.get("id");
    }

    public String getName() {
        return (String) attributes.get("name");
    }

    public String getEmail() {
        return (String) attributes.get("email");
    }
    
}
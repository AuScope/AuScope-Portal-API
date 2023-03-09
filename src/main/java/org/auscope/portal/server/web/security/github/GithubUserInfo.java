package org.auscope.portal.server.web.security.github;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.auscope.portal.server.web.security.PortalOAuth2UserInfo;


/**
 * Map of returned Github user attributes
 * 
 * @author woo392
 *
 */
public class GithubUserInfo extends PortalOAuth2UserInfo {

    public GithubUserInfo(Map<String, Object> attributes) {
    	super(attributes);
    }

    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    public String getName() {
    	String name = (String)attributes.get("name");
    	if (StringUtils.isEmpty(name)) {
	    	if (!StringUtils.isEmpty((String)attributes.get("login"))) {
	    		name = (String)attributes.get("login");
	    	}
    	}
        return name;
    }

    public String getEmail() {
        return (String) attributes.get("email");
    }
    
}
package org.auscope.portal.server.web.security.google;

import java.util.Map;

import org.auscope.portal.server.web.security.PortalOAuth2UserInfo;


/**
 * Map of returned Google user attributes
 * 
 * @author woo392
 *
 */
public class GoogleUserInfo extends PortalOAuth2UserInfo {

    public GoogleUserInfo(Map<String, Object> attributes) {
    	super(attributes);
    }

    public String getId() {
        return (String) attributes.get("sub");
    }

    public String getName() {
        return (String) attributes.get("name");
    }

    public String getEmail() {
        return (String) attributes.get("email");
    }
}
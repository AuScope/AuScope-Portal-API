package org.auscope.portal.server.web.security.google;

import java.util.Map;


/**
 * Map of returned Google user attributes
 * 
 * @author woo392
 *
 */
public class GoogleUserInfo {

    private Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
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
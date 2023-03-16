package org.auscope.portal.server.web.security;

import java.util.Map;

public abstract class PortalOAuth2UserInfo {
    protected Map<String, Object> attributes;

    public PortalOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();

    public abstract String getName();

    public abstract String getEmail();

}
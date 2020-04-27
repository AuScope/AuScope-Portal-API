package org.auscope.portal.server.web.service;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by wis056 on 18/11/2014.
 */
public enum ANVGLServerURL {
    INSTANCE;
    private String serverURL = null;

    public String get() {
        return serverURL;
    }

    public void set(String serverURL) {
        try {
            URI newURL = new URI(serverURL);
            this.serverURL = newURL.getScheme() + "://" + newURL.getAuthority();
        } catch (URISyntaxException e) {
            this.serverURL = serverURL;
        }
    }
}

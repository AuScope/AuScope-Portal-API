package org.auscope.portal.server.web.security;

import java.util.HashMap;
import java.util.Map;

import org.auscope.portal.core.server.security.oauth2.GoogleOAuth2ServiceProperties;

/**
 * ANVGLGoogleOAuth2ServiceProperties combines both the profile and email scopes
 * so that the user information returned contains the user's name once again.
 * 
 * @author woo392
 *
 */
public class ANVGLGoogleOAuth2ServiceProperties extends GoogleOAuth2ServiceProperties {

	public ANVGLGoogleOAuth2ServiceProperties() {
		super("", "", "");
	}

	public ANVGLGoogleOAuth2ServiceProperties(String clientId, String clientSecret, String redirectUri) {
		super(clientId, clientSecret, redirectUri);
		// Change the scope to both profile and email
		Map<String, String> additionalAuthParams = new HashMap<>();
		additionalAuthParams.put("scope",
				"https://www.googleapis.com/auth/userinfo.profile+https://www.googleapis.com/auth/userinfo.email");
		this.setAdditionalAuthParams(additionalAuthParams);
	}
}

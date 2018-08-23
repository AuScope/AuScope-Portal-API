package org.auscope.portal.server.web.security.aaf;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;


public class AAFAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
	private String aafLoginUrl;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		response.sendRedirect(aafLoginUrl);
	}

	public String getAafLoginUrl() {
		return aafLoginUrl;
	}

	public void setAafLoginUrl(String aafLoginUrl) {
		this.aafLoginUrl = aafLoginUrl;
	}

}

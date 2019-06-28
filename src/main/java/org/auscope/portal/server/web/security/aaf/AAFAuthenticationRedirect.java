package org.auscope.portal.server.web.security.aaf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AAFAuthenticationRedirect {
	
	@Value("${aafLoginUrl}")
	private String aafLoginUrl;
	
	
	@GetMapping("/login/aaf")
	public String redirectToAAFLogin(HttpServletRequest request, HttpServletResponse response) {
		return "redirect:" + aafLoginUrl;
	}

}

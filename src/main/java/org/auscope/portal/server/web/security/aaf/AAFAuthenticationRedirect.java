package org.auscope.portal.server.web.security.aaf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Redirect authentication request to the AAF login URL
 * 
 * @author woo392
 *
 */
@RestController
@SecurityRequirement(name = "public")
public class AAFAuthenticationRedirect {
	
	@Value("${spring.security.jwt.aaf.loginUrl}")
	private String aafLoginUrl;
	
	@GetMapping("/login/aaf")
	public String redirectToAAFLogin(HttpServletRequest request, HttpServletResponse response) {
		return "redirect:" + aafLoginUrl;
	}
    
}

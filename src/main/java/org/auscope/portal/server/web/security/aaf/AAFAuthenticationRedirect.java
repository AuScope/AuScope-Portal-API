package org.auscope.portal.server.web.security.aaf;

import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	@GetMapping(value = "/login/aaf")
    public ResponseEntity<Void> redirect(@RequestParam Map<String,String> input){
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(aafLoginUrl)).build();
    }
}

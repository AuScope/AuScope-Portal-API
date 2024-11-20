package org.auscope.portal.server.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.auscope.portal.server.web.security.aaf.AAFAuthenticationFilter;
import org.auscope.portal.server.web.security.aaf.AAFAuthenticationProvider;
import org.auscope.portal.server.web.security.aaf.AAFAuthenticationSuccessHandler;
import org.auscope.portal.server.web.security.google.GoogleOidcUserService;
import org.auscope.portal.server.web.service.PortalUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class PortalSecurityConfig {
	
	@Value("${frontEndUrl}")
	private String frontEndUrl;
	
	@Value("${portalUrl}")
	private String portalUrl;
	
	@Value("${spring.security.jwt.aaf.callbackUrl}")
	private String aafCallbackUrl;
	
	@Lazy
	@Autowired
	GoogleOidcUserService googleOidcUserService;
	
	@Lazy
	@Autowired
	PortalOAuth2UserService oauth2UserService;

	@Autowired
	AAFAuthenticationSuccessHandler aafSuccessHandler;
	
	@Lazy
	@Autowired
	private AAFAuthenticationProvider aafAuthenticationProvider;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(c -> c.disable())
			.formLogin(f -> f.disable())
			.authorizeHttpRequests(auth -> auth
	                .requestMatchers("/secure/**").hasAnyRole("ADMINISTRATOR", "USER")
	                .requestMatchers("/bookmarks/**").hasAnyRole("ADMINISTRATOR", "USER")
	                .requestMatchers("/**").permitAll()
			)
			.oauth2Login(o -> o
					.loginPage(frontEndUrl + "/login")
		    		.defaultSuccessUrl(frontEndUrl + "/login/loggedIn")
		    		.userInfoEndpoint(u -> u
		    				.userService(oauth2UserService)))
			.userDetailsService(userDetailsService())
			.logout(l -> l
					.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					// Disable auto-redirect from back-end, let front-end handle it
					.logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
					    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
					}))
			.exceptionHandling(e -> e
					.authenticationEntryPoint(new Http403ForbiddenEntryPoint()))
			.securityContext(s -> s
					.requireExplicitSave(false))  // Required in Spring Boot 3.0 for AAF
			.addFilterBefore(ssoFilterAAF(), BasicAuthenticationFilter.class);
		return http.build();
	}	
	
	@Bean
	public PortalUserDetailsService userDetailsService() {
		Map<String, List<String>> rolesByUser = new HashMap<String, List<String>>();
		List<String> roles = new ArrayList<String>();
		roles.add("ROLE_ADMINISTRATOR");
		roles.add("ROLE_UBC");
		rolesByUser.put("105810302719127403909", roles);
		PortalUserDetailsService userDetailsService = new PortalUserDetailsService("ROLE_USER", rolesByUser);
		return userDetailsService;
	}
	
	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(Arrays.asList(aafAuthenticationProvider));
	}
	
	private Filter ssoFilterAAF() {
		AAFAuthenticationFilter aafFilter = new AAFAuthenticationFilter(this.authenticationManager(), aafCallbackUrl);
		aafFilter.setAuthenticationSuccessHandler(aafSuccessHandler);
		// Failure handler used only for testing at the moment
		aafFilter.setAuthenticationFailureHandler(new AuthenticationFailureHandler() {
			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				System.out.println("AAF Authentication failed: " + exception.getLocalizedMessage());
				
			}
		});
		return aafFilter;
	}

}
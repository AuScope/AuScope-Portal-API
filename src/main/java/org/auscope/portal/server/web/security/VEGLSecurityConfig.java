package org.auscope.portal.server.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.server.web.security.aaf.AAFAuthenticationFilter;
import org.auscope.portal.server.web.security.aaf.AAFAuthenticationProvider;
import org.auscope.portal.server.web.security.aaf.AAFAuthenticationSuccessHandler;
import org.auscope.portal.server.web.security.aaf.PersistedAAFUserDetailsLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableOAuth2Client
public class VEGLSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	OAuth2ClientContext oauth2ClientContext;
	
	@Autowired
	GoogleAuthenticationSuccessHandler googleSuccessHandler;
	
	@Autowired
	AAFAuthenticationSuccessHandler aafSuccessHandler;
	
	@Autowired
	private AAFAuthenticationProvider aafAuthenticationProvider;
	
	@Value("${frontEndUrl}")
	private String frontEndUrl;

	
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(aafAuthenticationProvider);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()			
				.antMatchers("/secure/**")
					.authenticated()
				.antMatchers("/**")
					.permitAll()
			.and()
				.logout()
					.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
					.logoutSuccessUrl(frontEndUrl)
					.permitAll()
			.and()
				.addFilterBefore(ssoFilterAAF(), BasicAuthenticationFilter.class)
				.addFilterBefore(ssoFilterGoogle(), BasicAuthenticationFilter.class)
				
				// Couldn't get insecure root URLs to work without this, may need to change 
				.csrf().disable();
	}
	
	private Filter ssoFilterGoogle() {
		OAuth2ClientAuthenticationProcessingFilter googleFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login/google");
		OAuth2RestTemplate googleTemplate = new OAuth2RestTemplate(google(), oauth2ClientContext);
		googleFilter.setRestTemplate(googleTemplate);
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(googleResource().getUserInfoUri(),
				google().getClientId());
		tokenServices.setRestTemplate(googleTemplate);
		googleFilter.setTokenServices(tokenServices);
		
		// Success handler used for redirecting to front end
		googleFilter.setAuthenticationSuccessHandler(googleSuccessHandler);
		
		// Failure handler used only for testing at the moment
		googleFilter.setAuthenticationFailureHandler(new AuthenticationFailureHandler() {
			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				System.out.println("Google authentication failed: " + exception.getLocalizedMessage());
			}
		});		
		return googleFilter;
	}
	
	
	// Necessary for AAF Authentication
	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(Arrays.asList(aafAuthenticationProvider));
	}
	
	private Filter ssoFilterAAF() {
		AAFAuthenticationFilter aafFilter = new AAFAuthenticationFilter(this.authenticationManager());
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
	
	@Bean(name="persistedUserGoogleDetailsLoader")
	public PersistedGoogleUserDetailsLoader persistedUserGoogleDetailsLoader() {
		Map<String, List<String>> rolesByUser = new HashMap<String, List<String>>();
		List<String> roles = new ArrayList<String>();
		roles.add("ROLE_ADMINISTRATOR");
		roles.add("ROLE_UBC");
		rolesByUser.put("105810302719127403909", roles);
		PersistedGoogleUserDetailsLoader detailsLoader = new PersistedGoogleUserDetailsLoader("ROLE_USER", rolesByUser);
		return detailsLoader;
	}
	
	@Bean(name = "persistedAAFUserDetailsLoader")
	public PersistedAAFUserDetailsLoader persistedAAFUserDetailsLoader() {
		Map<String, List<String>> rolesByUser = new HashMap<String, List<String>>();
		List<String> roles = new ArrayList<String>();
		roles.add("ROLE_ADMINISTRATOR");
		roles.add("ROLE_UBC");
		rolesByUser.put("105810302719127403909", roles);
		PersistedAAFUserDetailsLoader detailsLoader = new PersistedAAFUserDetailsLoader("ROLE_USER", rolesByUser);
		return detailsLoader;
	}
	
	@Bean
	@ConfigurationProperties("google.client")
	public AuthorizationCodeResourceDetails google() {
		return new AuthorizationCodeResourceDetails();
	}
	
	@Bean
	@ConfigurationProperties("google.resource")
	public ResourceServerProperties googleResource() {
		return new ResourceServerProperties();
	}
	
}

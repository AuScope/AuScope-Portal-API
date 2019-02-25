package org.auscope.portal.server.web.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableOAuth2Client
public class VEGLSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	OAuth2ClientContext oauth2ClientContext;
	
	@Autowired
	GoogleAuthenticationSuccessHandler googleSuccessHandler;
	
	@Autowired
	@Qualifier("userDetailsService")
	ANVGLUserDetailsService userDetailsService;
	 
	
	@Value("${frontEndUrl}")
	private String frontEndUrl;
	
	
	/*
	@Autowired
	DataSource dataSource;
	
	@Autowired
	public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource);
	}
	*/
	
	

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()			
				.antMatchers("/secure/**")
					.authenticated()
				.antMatchers("/**")
					.permitAll()
			//.and()
			//	.formLogin()
			//		.loginPage("/login").permitAll()
			//		//.defaultSuccessUrl("http://localhost:4200/login/loggedIn")
			.and()
				.logout()
					.logoutSuccessUrl(frontEndUrl)
					.permitAll()
			//.and()
			//	.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
			.and()
				//.userDetailsService(createUserDetailsService())
				.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
				// Couldn't get insecure root URLs to work without this, may need to change 
				.csrf().disable();
		/*
		http
			.antMatcher("/**")
			.authorizeRequests()
				.antMatchers("/login**", "/error**") // TODO: Do we need error?
				.permitAll()
			.anyRequest()
				.authenticated()
				//.and()
				//	.formLogin()
				//		.loginPage("/login").permitAll()
				//		//.defaultSuccessUrl("http://localhost:4200/login/loggedIn")
				.and()
					.logout()
						.logoutSuccessUrl(frontEndUrl)
						.permitAll()
				.and()
					.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.and()
					.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
		*/
	}
	
	private Filter ssoFilter() {
		OAuth2ClientAuthenticationProcessingFilter googleFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login/google");
		OAuth2RestTemplate googleTemplate = new OAuth2RestTemplate(google(), oauth2ClientContext);
		googleFilter.setRestTemplate(googleTemplate);
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(googleResource().getUserInfoUri(),
				google().getClientId());
		tokenServices.setRestTemplate(googleTemplate);
		googleFilter.setTokenServices(tokenServices);
		
		//googleFilter.setAuthenticationManager(authenticationManager);
		
		// Success handler used for redirecting to front end
		googleFilter.setAuthenticationSuccessHandler(googleSuccessHandler);
		
		/*
		googleFilter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler() {
			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		        response.sendRedirect(frontEndUrl + "/login/loggedIn");
		    }
		});
		*/
		
		// Failure handler used only for testing at the moment
		googleFilter.setAuthenticationFailureHandler(new AuthenticationFailureHandler() {
			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				System.out.println("Authentication failed: " + exception.getLocalizedMessage());
			}
		});		
		return googleFilter;
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
	
	@Bean
	public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<OAuth2ClientContextFilter>();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}
	
	/*
	// IS THIS ATTACHED TO ANYTHING?
	@Bean
	public UserDetailsService createUserDetailsService() {
		return new ANVGLUserDetailsService();
	}
	*/

}

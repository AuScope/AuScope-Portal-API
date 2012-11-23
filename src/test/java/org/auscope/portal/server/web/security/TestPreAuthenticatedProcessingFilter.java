package org.auscope.portal.server.web.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * Unit tests for PreAuthenticatedProcessingFilter.
 * @author Richard Goh
 */
public class TestPreAuthenticatedProcessingFilter extends PortalTestClass {
    private HttpServletRequest mockRequest;
    private HttpSession mockSession;
    private SecurityContextImpl mockSecurityContext;
    private OpenIDAuthenticationToken mockOpenIDAuthToken;
    private UserDetails mockUserDetails;
    private PreAuthenticatedProcessingFilter testPreAuthProcFilter;
    
    @Before
    public void setup() throws Exception {
        // Setting up mock objects needed for Object Under Test (OUT)
        mockRequest = context.mock(HttpServletRequest.class);
        mockSession = context.mock(HttpSession.class);
        mockSecurityContext = context.mock(SecurityContextImpl.class);
        mockOpenIDAuthToken = context.mock(OpenIDAuthenticationToken.class);
        mockUserDetails = context.mock(UserDetails.class);
        // Object Under Test
        testPreAuthProcFilter = new PreAuthenticatedProcessingFilter();
    }
    
    /**
     * Tests that getting pre-authenticated principal succeeds.
     */
    @Test
    public void testGetPreAuthenticatedPrincipal() {
        final String userEmail = "user@email.com";
        final String[] userRoles = { "role1", "role2" };
        
        final GrantedAuthority mockGrantedAuthority1 = context
                .mock(GrantedAuthority.class, "mockGrantedAuthority1");
        final GrantedAuthority mockGrantedAuthority2 = context
                .mock(GrantedAuthority.class, "mockGrantedAuthority2");
        final Collection<GrantedAuthority> mockAuthorities = Arrays.asList(
                mockGrantedAuthority1, mockGrantedAuthority2);
        final OpenIDAttribute mockOpenIDAttrib1 = context
                .mock(OpenIDAttribute.class, "mockOpenIDAttrib1");
        final OpenIDAttribute mockOpenIDAttrib2 = context
                .mock(OpenIDAttribute.class, "mockOpenIDAttrib2");
        final List<OpenIDAttribute> mockOpenIDAttribs = Arrays.asList(
                mockOpenIDAttrib1, mockOpenIDAttrib2);
        final List<String> mockOpenIDAttrib2Values = Arrays.asList(userEmail,
                "other_value");
        
        context.checking(new Expectations() {{
            //We should have call(s) to http request object to get user's session object
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            //We should have a call to http request session object to get "SPRING_SECURITY_CONTEXT" attribute
            oneOf(mockSession).getAttribute("SPRING_SECURITY_CONTEXT");will(returnValue(mockSecurityContext));
            oneOf(mockSecurityContext).getAuthentication();will(returnValue(mockOpenIDAuthToken));
            //We should have a call to OpenIDAuthenticationToken object to get UserDetails object
            oneOf(mockOpenIDAuthToken).getPrincipal();will(returnValue(mockUserDetails));
            //We should have a call to UserDetails object to get a list of granted authorities
            oneOf(mockUserDetails).getAuthorities();will(returnValue(mockAuthorities));
            
            oneOf(mockGrantedAuthority1).getAuthority();will(returnValue("role1"));
            oneOf(mockGrantedAuthority2).getAuthority();will(returnValue("role2"));
            //We should have a call to http request session object to set "user-roles" attribute
            oneOf(mockSession).setAttribute("user-roles", userRoles);
            
            oneOf(mockOpenIDAuthToken).getAttributes();will(returnValue(mockOpenIDAttribs));
            oneOf(mockOpenIDAttrib1).getName();will(returnValue("notemail"));
            
            oneOf(mockOpenIDAttrib2).getName();will(returnValue("email"));
            oneOf(mockOpenIDAttrib2).getValues();will(returnValue(mockOpenIDAttrib2Values));
            //We should have a call to http request session object to set "openID-Email" attribute
            oneOf(mockSession).setAttribute("openID-Email", userEmail);
            //We should have a call to http request session object to get "openID-Email" attribute
            oneOf(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));
        }});
        
        String preAuthPrincipal = (String)testPreAuthProcFilter.getPreAuthenticatedPrincipal(mockRequest);
        Assert.assertNotNull(preAuthPrincipal);
        Assert.assertEquals(userEmail, preAuthPrincipal);
    }
    
    /**
     * Tests that getting pre-authenticated principal returns 
     * false if spring security context presents in user's session
     * but user email attribute cannot be found in OpenID attributes.
     */
    @Test
    public void testGetPreAuthenticatedPrincipal_EmailNotFound() {
        final String[] userRoles = { "role1", "role2" };
        
        final GrantedAuthority mockGrantedAuthority1 = context
                .mock(GrantedAuthority.class, "mockGrantedAuthority1");
        final GrantedAuthority mockGrantedAuthority2 = context
                .mock(GrantedAuthority.class, "mockGrantedAuthority2");
        final Collection<GrantedAuthority> mockAuthorities = Arrays.asList(
                mockGrantedAuthority1, mockGrantedAuthority2);
        final OpenIDAttribute mockOpenIDAttrib1 = context
                .mock(OpenIDAttribute.class, "mockOpenIDAttrib1");
        final OpenIDAttribute mockOpenIDAttrib2 = context
                .mock(OpenIDAttribute.class, "mockOpenIDAttrib2");
        final List<OpenIDAttribute> mockOpenIDAttribs = Arrays.asList(
                mockOpenIDAttrib1, mockOpenIDAttrib2);
        
        context.checking(new Expectations() {{
            //We should have call(s) to http request object to get user's session object
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            
            //We should have a call to http request session object to get "SPRING_SECURITY_CONTEXT" attribute
            oneOf(mockSession).getAttribute("SPRING_SECURITY_CONTEXT");will(returnValue(mockSecurityContext));
            oneOf(mockSecurityContext).getAuthentication();will(returnValue(mockOpenIDAuthToken));
            //We should have a call to OpenIDAuthenticationToken object to get UserDetails object
            oneOf(mockOpenIDAuthToken).getPrincipal();will(returnValue(mockUserDetails));
            //We should have a call to UserDetails object to get a list of granted authorities
            oneOf(mockUserDetails).getAuthorities();will(returnValue(mockAuthorities));
            
            oneOf(mockGrantedAuthority1).getAuthority();will(returnValue("role1"));
            oneOf(mockGrantedAuthority2).getAuthority();will(returnValue("role2"));
            //We should have a call to http request session object to set "user-roles" attribute
            oneOf(mockSession).setAttribute("user-roles", userRoles);
            
            oneOf(mockOpenIDAuthToken).getAttributes();will(returnValue(mockOpenIDAttribs));
            oneOf(mockOpenIDAttrib1).getName();will(returnValue("notemail"));
            oneOf(mockOpenIDAttrib2).getName();will(returnValue("other"));
        }});
        
        Object authPrincipal = testPreAuthProcFilter.getPreAuthenticatedPrincipal(mockRequest);
        Assert.assertNotNull(authPrincipal);
        Assert.assertFalse((Boolean)authPrincipal);
    }
    
    /**
     * Tests that getting pre-authenticated principal returns null 
     * if the spring security context is not present in user's session.
     */
    @Test
    public void testGetPreAuthenticatedPrincipal_NullSecurityContext() {
        context.checking(new Expectations() {{
            //We should have call(s) to http request object to get user's session object
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            //We should have a call to http request session object to get "SPRING_SECURITY_CONTEXT" attribute
            oneOf(mockSession).getAttribute("SPRING_SECURITY_CONTEXT");will(returnValue(null));
            //We should have a call to http request session object to get "openID-Email" attribute
            oneOf(mockSession).getAttribute("openID-Email");will(returnValue(null));            
        }});
        
        Object authPrincipal = testPreAuthProcFilter.getPreAuthenticatedPrincipal(mockRequest);
        Assert.assertNull(authPrincipal);
    }
    
    /**
     * Tests that getting pre-authenticated credentials succeeds.
     */
    @Test
    public void testGetPreAuthenticatedCredentials() {
        String expectedPreAuthCreds = "NONE";
        
        String preAuthCreds = (String)testPreAuthProcFilter.getPreAuthenticatedCredentials(mockRequest);
        Assert.assertNotNull(preAuthCreds);
        Assert.assertEquals(expectedPreAuthCreds, preAuthCreds);
    }
}
package org.auscope.portal.server.web.interceptors;

import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.view.JSONView;
import org.auscope.portal.server.test.VGLPortalTestClass;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.security.core.Authentication;

/**
 * Unit tests for UserSessionInterceptor
 *
 * @author Richard Goh
 */
@PrepareForTest({JSONView.class})
public class TestUserSessionInterceptor extends VGLPortalTestClass {
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private HttpSession mockSession;
    private UserSessionInterceptor testInterceptor;
    private Authentication mockAuth;
    private PortalUser mockUser;

    /**
     * Load our mock objects
     */
    @Before
    public void init() {
        mockResponse = context.mock(HttpServletResponse.class);
        mockRequest = context.mock(HttpServletRequest.class);
        mockSession = context.mock(HttpSession.class);
        mockAuth = context.mock(Authentication.class);
        mockUser = context.mock(PortalUser.class);

        //Just don't bother about testing this method
        suppress(method(JSONView.class, "render"));

        testInterceptor = new UserSessionInterceptor();
    }

    /**
     * Tests that the preHandle method succeeds.
     * @throws Exception
     */
    @Test
    public void testPreHandle() throws Exception {
        final String userEmail = "test@email.com";

        context.checking(new Expectations() {{
            allowing(mockRequest).getUserPrincipal(); will(returnValue(mockAuth));
            allowing(mockAuth).getPrincipal(); will(returnValue(mockUser));

            allowing(mockRequest).getSession(true);will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));
        }});

        boolean result = testInterceptor.preHandle(mockRequest, mockResponse, null);
        Assert.assertTrue(result);
    }

    /**
     * Tests that the preHandle method when the user session expired.
     * @throws Exception
     */
    @Test
    public void testPreHandle_UserSessionExpired() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockRequest).getUserPrincipal(); will(returnValue(null));
        }});

        boolean result = testInterceptor.preHandle(mockRequest, mockResponse, null);
        Assert.assertFalse(result);
    }
}
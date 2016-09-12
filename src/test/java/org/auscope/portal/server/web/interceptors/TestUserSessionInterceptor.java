package org.auscope.portal.server.web.interceptors;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;

/**
 * Unit tests for UserSessionInterceptor
 *
 * @author Richard Goh
 */
public class TestUserSessionInterceptor extends PortalTestClass {
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
        try (final PrintWriter pw = new PrintWriter(new ByteArrayOutputStream())) {

            context.checking(new Expectations() {
                {
                    allowing(mockRequest).getUserPrincipal();
                    will(returnValue(null));
                    allowing(mockRequest).getAttribute(with(any(String.class)));
                    will(returnValue(null));
                    allowing(mockResponse).setContentType(with(any(String.class)));
                    allowing(mockResponse).getWriter();
                    will(returnValue(pw));
                }
            });

            boolean result = testInterceptor.preHandle(mockRequest, mockResponse, null);
            Assert.assertFalse(result);
        }
    }
}
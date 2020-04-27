package org.auscope.portal.server.web.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.NCIDetailsService;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for MenuController
 * @author Josh Vote
 *
 */
public class TestMenuController extends PortalTestClass {
    private HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);
    private ANVGLUser mockUser = context.mock(ANVGLUser.class);
    private HttpSession mockSession = context.mock(HttpSession.class);
    private CloudComputeService[] mockCloudComputeServices = new CloudComputeService[] {context.mock(CloudComputeService.class)};
    //private NCIDetailsDao mockNciDetailsDao = context.mock(NCIDetailsDao.class);
    private NCIDetailsService mockNciDetailsService = context.mock(NCIDetailsService.class);
    final String gMapKey = "13421asdasd";
    final String gAnalyticsKey = "faf3113f1";
    final String adminEmail = "cg-admin@csiro.au";

    private MenuController mc = null;

    @Before
    public void setup() {
        mc = new MenuController(gMapKey, gAnalyticsKey, adminEmail, "aaf_login_url.html", mockCloudComputeServices, mockNciDetailsService/*, mockNciDetailsDao*/);
        mc.setBuildStamp("FFFF");

        //Global expectations for setting build stamp id
        context.checking(new Expectations() {{
            allowing(mockSession).getServletContext();will(throwException(new IOException("manifest DNE for unit testing")));
        }});
    }

    /**
     * Tests the existence of certain critical API keys + the correct view name being extracted
     * @throws Exception
     */
    /*
    @Test
    public void testHandleHtmlToView_FullyQualified() throws Exception {
        final String uri = "http://example.org/context/path/resource.html";

        context.checking(new Expectations() {{
            oneOf(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(true));
            oneOf(mockSession).setAttribute("existingSession", true);

            allowing(mockUser).configuredServicesStatus(mockNciDetailsService, mockCloudComputeServices);will(returnValue(true));
            allowing(mockUser).acceptedTermsConditionsStatus();will(returnValue(true));
            allowing(mockUser).getUsername();will(returnValue("TestUsername"));
        }});

        ModelAndView mav = mc.handleHtmlToView(mockUser, mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals("resource", mav.getViewName());
        Assert.assertEquals(mav.getModel().get("googleKey"), gMapKey);
        Assert.assertEquals(mav.getModel().get("analyticKey"), gAnalyticsKey);
    }
    */


    /**
     * Tests the existence of certain critical API keys + the correct view name being extracted
     * @throws Exception
     */
    /*
    @Test
    public void testHandleHtmlToView_Relative() throws Exception {
        final String uri = "/context/path/resource.html";

        context.checking(new Expectations() {{
            oneOf(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(null));
            oneOf(mockSession).setAttribute("existingSession", true);

            allowing(mockUser).configuredServicesStatus(mockNciDetailsService, mockCloudComputeServices);will(returnValue(true));
            allowing(mockUser).acceptedTermsConditionsStatus();will(returnValue(true));
            allowing(mockUser).getUsername();will(returnValue("TestUsername"));
        }});

        ModelAndView mav = mc.handleHtmlToView(mockUser, mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals("resource", mav.getViewName());
        Assert.assertEquals(mav.getModel().get("googleKey"), gMapKey);
        Assert.assertEquals(mav.getModel().get("analyticKey"), gAnalyticsKey);
    }
    */

    /**
     * Tests that creating a new session will trigger the isNewSession variable
     * @throws Exception
     */
    /*
    @Test
    public void testHandleHtmlToView_NewSession() throws Exception {
        final String uri = "http://example.org/context/path/resource.html";

        context.checking(new Expectations() {{
            oneOf(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(null));
            oneOf(mockSession).setAttribute("existingSession", true);
            allowing(mockUser).getUsername();will(returnValue("TestUsername"));
        }});

        ModelAndView mav = mc.handleHtmlToView(null, mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals(mav.getModel().get("isNewSession"), true);
    }
    */

    /**
     * Tests that reusing an existing session will trigger the isNewSession variable
     * @throws Exception
     */
    /*
    @Test
    public void testHandleHtmlToView_ExistingSession() throws Exception {
        final String uri = "http://example.org/context/path/resource.html";
        context.checking(new Expectations() {{
            oneOf(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(true));
            oneOf(mockSession).setAttribute("existingSession", true);

            allowing(mockUser).configuredServicesStatus(mockNciDetailsService, mockCloudComputeServices);will(returnValue(true));
            allowing(mockUser).acceptedTermsConditionsStatus();will(returnValue(true));
            allowing(mockUser).getUsername();will(returnValue("TestUsername"));
        }});

        ModelAndView mav = mc.handleHtmlToView(mockUser, mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals(mav.getModel().get("isNewSession"), false);
    }
    */

    /*
    @Test
    public void testUnconfiguredRedirect() throws Exception {
        final String uri = "http://example.org/context/path/resource.html";

        context.checking(new Expectations() {{
            allowing(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(true));
            oneOf(mockSession).setAttribute("existingSession", true);

            allowing(mockUser).configuredServicesStatus(mockNciDetailsService, mockCloudComputeServices);will(returnValue(false));
            allowing(mockUser).acceptedTermsConditionsStatus();will(returnValue(true));
            allowing(mockUser).getUsername();will(returnValue("TestUsername"));

        }});

        ModelAndView mav = mc.handleHtmlToView(mockUser, mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals("redirect:/noconfig.html?next=/context/path/resource.html", mav.getViewName());
    }
    */

    /*
    @Test
    public void testUnacceptedTCsRedirect() throws Exception {
        final String uri = "http://example.org/context/path/resource.html";

        context.checking(new Expectations() {{
            allowing(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(true));
            oneOf(mockSession).setAttribute("existingSession", true);

            allowing(mockUser).configuredServicesStatus(mockNciDetailsService, mockCloudComputeServices);will(returnValue(true));
            allowing(mockUser).acceptedTermsConditionsStatus();will(returnValue(false));
            allowing(mockUser).getUsername();will(returnValue("TestUsername"));

        }});

        ModelAndView mav = mc.handleHtmlToView(mockUser, mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals("redirect:/notcs.html?next=/context/path/resource.html", mav.getViewName());
    }
    */

    /*
    @Test
    public void testUnconfiguredRedirect_AbortOnUser() throws Exception {
        final String uri = "http://example.org/context/path/user.html?a=b";

        context.checking(new Expectations() {{
            allowing(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("existingSession");will(returnValue(true));
            allowing(mockSession).setAttribute("existingSession", true);

            allowing(mockUser).configuredServicesStatus(mockNciDetailsService, mockCloudComputeServices);will(returnValue(false));
            allowing(mockUser).acceptedTermsConditionsStatus();will(returnValue(false));
            allowing(mockUser).getUsername();will(returnValue("TestUsername"));
        }});

        ModelAndView mav = mc.handleHtmlToView(mockUser, mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals("user?a=b", mav.getViewName());

    }
    */
}

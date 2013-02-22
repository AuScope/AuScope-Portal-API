package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.test.PortalTestClass;
import org.jmock.Expectations;
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
    private PortalPropertyPlaceholderConfigurer hostConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);
    private HttpSession mockSession = context.mock(HttpSession.class);


    private MenuController mc = null;

    @Before
    public void setup() {
        mc = new MenuController(hostConfigurer);
    }

    /**
     * Tests the existence of certain critical API keys + the correct view name being extracted
     * @throws Exception
     */
    @Test
    public void testHandleHtmlToView_FullyQualified() throws Exception {
        final String uri = "http://example.org/context/path/resource.html";
        final String gMapKey = "13421asdasd";
        final String gAnalyticsKey = "faf3113f1";

        context.checking(new Expectations() {{
            oneOf(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(true));
            oneOf(mockSession).setAttribute("existingSession", true);

            //Every view should have the analytics key thrown into it
            oneOf(hostConfigurer).resolvePlaceholder("HOST.googlemap.key");will(returnValue(gMapKey));
            oneOf(hostConfigurer).resolvePlaceholder("HOST.google.analytics.key");will(returnValue(gAnalyticsKey));
        }});

        ModelAndView mav = mc.handleHtmlToView(mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals("resource", mav.getViewName());
        Assert.assertEquals(mav.getModel().get("googleKey"), gMapKey);
        Assert.assertEquals(mav.getModel().get("analyticKey"), gAnalyticsKey);
    }


    /**
     * Tests the existence of certain critical API keys + the correct view name being extracted
     * @throws Exception
     */
    @Test
    public void testHandleHtmlToView_Relative() throws Exception {
        final String uri = "/context/path/resource.html";
        final String gMapKey = "131asdasd";
        final String gAnalyticsKey = "fahh113f1";

        context.checking(new Expectations() {{
            oneOf(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(null));
            oneOf(mockSession).setAttribute("existingSession", true);

            //Every view should have the analytics key thrown into it
            oneOf(hostConfigurer).resolvePlaceholder("HOST.googlemap.key");will(returnValue(gMapKey));
            oneOf(hostConfigurer).resolvePlaceholder("HOST.google.analytics.key");will(returnValue(gAnalyticsKey));
        }});

        ModelAndView mav = mc.handleHtmlToView(mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals("resource", mav.getViewName());
        Assert.assertEquals(mav.getModel().get("googleKey"), gMapKey);
        Assert.assertEquals(mav.getModel().get("analyticKey"), gAnalyticsKey);
    }

    /**
     * Tests that creating a new session will trigger the isNewSession variable
     * @throws Exception
     */
    @Test
    public void testHandleHtmlToView_NewSession() throws Exception {
        final String uri = "http://example.org/context/path/resource.html";
        final String gMapKey = "13421asdasd";
        final String gAnalyticsKey = "faf3113f1";

        context.checking(new Expectations() {{
            oneOf(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(null));
            oneOf(mockSession).setAttribute("existingSession", true);


            //Every view should have the analytics key thrown into it
            oneOf(hostConfigurer).resolvePlaceholder("HOST.googlemap.key");will(returnValue(gMapKey));
            oneOf(hostConfigurer).resolvePlaceholder("HOST.google.analytics.key");will(returnValue(gAnalyticsKey));
        }});

        ModelAndView mav = mc.handleHtmlToView(mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals(mav.getModel().get("isNewSession"), true);
    }

    /**
     * Tests that reusing an existing session will trigger the isNewSession variable
     * @throws Exception
     */
    @Test
    public void testHandleHtmlToView_ExistingSession() throws Exception {
        final String uri = "http://example.org/context/path/resource.html";
        final String gMapKey = "13421asdasd";
        final String gAnalyticsKey = "faf3113f1";

        context.checking(new Expectations() {{
            oneOf(mockRequest).getRequestURI();will(returnValue(uri));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("existingSession");will(returnValue(true));
            oneOf(mockSession).setAttribute("existingSession", true);


            //Every view should have the analytics key thrown into it
            oneOf(hostConfigurer).resolvePlaceholder("HOST.googlemap.key");will(returnValue(gMapKey));
            oneOf(hostConfigurer).resolvePlaceholder("HOST.google.analytics.key");will(returnValue(gAnalyticsKey));
        }});

        ModelAndView mav = mc.handleHtmlToView(mockRequest, mockResponse);

        Assert.assertNotNull(mav);
        Assert.assertEquals(mav.getModel().get("isNewSession"), false);
    }
}

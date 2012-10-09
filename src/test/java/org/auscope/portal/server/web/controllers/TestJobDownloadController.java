package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VglDownload;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for ERRDAPController
 * @author Josh Vote
 *
 */
public class TestJobDownloadController extends PortalTestClass {
    private PortalPropertyPlaceholderConfigurer mockHostConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);
    private HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);
    private HttpSession mockSession = context.mock(HttpSession.class);
    private JobDownloadController controller;

    @Before
    public void setup() {
        controller = new JobDownloadController(mockHostConfigurer);
    }

    @Test
    public void testAddErddapRequestToSession() throws Exception {
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String format = "nc";
        final String layerName = "layer";
        final String name = "name";
        final String description = "desc";
        final String localPath = "localPath";
        final String serviceUrl = "http://example.org/service";

        final List<VglDownload> downloads = new ArrayList<VglDownload>();

        context.checking(new Expectations() {{
            oneOf(mockHostConfigurer).resolvePlaceholder("HOST.erddapservice.url");will(returnValue(serviceUrl));

            allowing(mockRequest).getSession();will(returnValue(mockSession));

            oneOf(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(downloads));
            oneOf(mockSession).setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, downloads);
        }});

        ModelAndView mav = controller.addErddapRequestToSession(northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, format, layerName, name, description, localPath, mockRequest, mockResponse);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));

        Assert.assertEquals(1, downloads.size());
        VglDownload download = downloads.get(0);
        Assert.assertEquals(northBoundLatitude, download.getNorthBoundLatitude());
        Assert.assertEquals(eastBoundLongitude, download.getEastBoundLongitude());
        Assert.assertEquals(southBoundLatitude, download.getSouthBoundLatitude());
        Assert.assertEquals(westBoundLongitude, download.getWestBoundLongitude());
        Assert.assertEquals(name, download.getName());
        Assert.assertEquals(description, download.getDescription());
        Assert.assertEquals(localPath, download.getLocalPath());
        Assert.assertTrue(download.getUrl().startsWith(serviceUrl));
        Assert.assertTrue(download.getUrl().contains("." + format));
        Assert.assertTrue(download.getUrl().contains(layerName));
    }

    @Test
    public void testAddDownloadRequestToSession() throws Exception {
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String name = "name";
        final String description = "desc";
        final String localPath = "localPath";
        final String serviceUrl = "http://example.org/service";

        final List<VglDownload> downloads = new ArrayList<VglDownload>();

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));

            oneOf(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(downloads));
            oneOf(mockSession).setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, downloads);
        }});

        ModelAndView mav = controller.addDownloadRequestToSession(serviceUrl, name, description, localPath, northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));

        Assert.assertEquals(1, downloads.size());
        VglDownload download = downloads.get(0);
        Assert.assertEquals(northBoundLatitude, download.getNorthBoundLatitude());
        Assert.assertEquals(eastBoundLongitude, download.getEastBoundLongitude());
        Assert.assertEquals(southBoundLatitude, download.getSouthBoundLatitude());
        Assert.assertEquals(westBoundLongitude, download.getWestBoundLongitude());
        Assert.assertEquals(name, download.getName());
        Assert.assertEquals(description, download.getDescription());
        Assert.assertEquals(localPath, download.getLocalPath());
        Assert.assertEquals(serviceUrl, download.getUrl());
    }
}

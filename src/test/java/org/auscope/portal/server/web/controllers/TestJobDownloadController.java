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
    public void testAddSelectedResourcesToSession() {
        final Double[] northBoundLatitude = { 2.0, 3.0 };
        final Double[] eastBoundLongitude = { 4.0, 5.0 };
        final Double[] southBoundLatitude = { 1.0, 2.0 };
        final Double[] westBoundLongitude = { 3.0, 4.0 };
        final String[] name = { "name1", "name2" };
        final String[] description = { "desc1", "desc2" };
        final String[] localPath = { "localPath1", "localPath2" };
        final String[] serviceUrl = { "http://example.org/service1", "http://example.org/service2" };
        
        final List<VglDownload> downloads = new ArrayList<VglDownload>();
        
        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(downloads));
            allowing(mockSession).setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, downloads);
        }});        
        
        ModelAndView mav = controller.addSelectedResourcesToSession(serviceUrl, name, description, localPath, northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));        
        
        Assert.assertEquals(2, downloads.size());
        
        VglDownload download1 = downloads.get(0);
        Assert.assertEquals(northBoundLatitude[0], download1.getNorthBoundLatitude());
        Assert.assertEquals(eastBoundLongitude[0], download1.getEastBoundLongitude());
        Assert.assertEquals(southBoundLatitude[0], download1.getSouthBoundLatitude());
        Assert.assertEquals(westBoundLongitude[0], download1.getWestBoundLongitude());
        Assert.assertEquals(name[0], download1.getName());
        Assert.assertEquals(description[0], download1.getDescription());
        Assert.assertEquals(localPath[0], download1.getLocalPath());
        Assert.assertEquals(serviceUrl[0], download1.getUrl());
        
        VglDownload download2 = downloads.get(1);
        Assert.assertEquals(northBoundLatitude[1], download2.getNorthBoundLatitude());
        Assert.assertEquals(eastBoundLongitude[1], download2.getEastBoundLongitude());
        Assert.assertEquals(southBoundLatitude[1], download2.getSouthBoundLatitude());
        Assert.assertEquals(westBoundLongitude[1], download2.getWestBoundLongitude());
        Assert.assertEquals(name[1], download2.getName());
        Assert.assertEquals(description[1], download2.getDescription());
        Assert.assertEquals(localPath[1], download2.getLocalPath());
        Assert.assertEquals(serviceUrl[1], download2.getUrl());
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

package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.wfs.WFSGetCapabilitiesResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
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
    private SimpleWfsService mockWfsService = context.mock(SimpleWfsService.class);
    private JobDownloadController controller;

    @Before
    public void setup() {
        controller = new JobDownloadController(mockHostConfigurer, mockWfsService);
    }

    @Test
    public void testMakeErddapUrlSaveSession() throws Exception {
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

        ModelAndView mav = controller.makeErddapUrl(northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, format, layerName, name, description, localPath, true, mockRequest, mockResponse);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));

        //Check response
        Assert.assertNotNull(mav.getModel().get("data"));
        ModelMap data = (ModelMap) mav.getModel().get("data");
        Assert.assertEquals(northBoundLatitude, data.get("northBoundLatitude"));
        Assert.assertEquals(eastBoundLongitude, data.get("eastBoundLongitude"));
        Assert.assertEquals(southBoundLatitude, data.get("southBoundLatitude"));
        Assert.assertEquals(westBoundLongitude, data.get("westBoundLongitude"));
        Assert.assertEquals(name, data.get("name"));
        Assert.assertTrue(data.get("url").toString().contains(serviceUrl));
        
        //Check session variables
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
    public void testMakeErddapUrlNotSaveSession() throws Exception {
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

        context.checking(new Expectations() {{
            oneOf(mockHostConfigurer).resolvePlaceholder("HOST.erddapservice.url");will(returnValue(serviceUrl));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
        }});

        ModelAndView mav = controller.makeErddapUrl(northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, format, layerName, name, description, localPath, false, mockRequest, mockResponse);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));

        //Check response
        Assert.assertNotNull(mav.getModel().get("data"));
        ModelMap data = (ModelMap) mav.getModel().get("data");
        Assert.assertEquals(northBoundLatitude, data.get("northBoundLatitude"));
        Assert.assertEquals(eastBoundLongitude, data.get("eastBoundLongitude"));
        Assert.assertEquals(southBoundLatitude, data.get("southBoundLatitude"));
        Assert.assertEquals(westBoundLongitude, data.get("westBoundLongitude"));
        Assert.assertEquals(name, data.get("name"));
        Assert.assertTrue(data.get("url").toString().contains(serviceUrl));
    }

    @Test
    public void testMakeDownloadUrlSaveSession() throws Exception {
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

        ModelAndView mav = controller.makeDownloadUrl(serviceUrl, name, description, localPath, northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, true, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));

        //Check response
        Assert.assertNotNull(mav.getModel().get("data"));
        ModelMap data = (ModelMap) mav.getModel().get("data");
        Assert.assertEquals(northBoundLatitude, data.get("northBoundLatitude"));
        Assert.assertEquals(eastBoundLongitude, data.get("eastBoundLongitude"));
        Assert.assertEquals(southBoundLatitude, data.get("southBoundLatitude"));
        Assert.assertEquals(westBoundLongitude, data.get("westBoundLongitude"));
        Assert.assertEquals(name, data.get("name"));
        Assert.assertEquals(serviceUrl, data.get("url"));
        
        //Check session variables
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
    
    @Test
    public void testMakeDownloadUrlNotSaveSession() throws Exception {
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String name = "name";
        final String description = "desc";
        final String localPath = "localPath";
        final String serviceUrl = "http://example.org/service";

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
        }});

        ModelAndView mav = controller.makeDownloadUrl(serviceUrl, name, description, localPath, northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, false, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));

        //Check response
        Assert.assertNotNull(mav.getModel().get("data"));
        ModelMap data = (ModelMap) mav.getModel().get("data");
        Assert.assertEquals(northBoundLatitude, data.get("northBoundLatitude"));
        Assert.assertEquals(eastBoundLongitude, data.get("eastBoundLongitude"));
        Assert.assertEquals(southBoundLatitude, data.get("southBoundLatitude"));
        Assert.assertEquals(westBoundLongitude, data.get("westBoundLongitude"));
        Assert.assertEquals(name, data.get("name"));
        Assert.assertEquals(serviceUrl, data.get("url"));
    }
    
    @Test
    public void testMakeWfsUrlSaveSession() throws Exception {
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String srsName = "EPSG:4326";
        final String bboxSrs = "EPSG:4387";
        final String featureType = "test:featureType";
        final String name = "name";
        final String description = "desc";
        final String localPath = "localPath";
        final String serviceUrl = "http://example.org/wfs";
        final String outputFormat = "o-f";
        final Integer maxFeatures = null;
        final List<VglDownload> downloads = new ArrayList<VglDownload>();
        final String wfsRequestString = serviceUrl + "?request=param";
        
        final String[] expectedFormats = new String[] {"format1", "format2"};
        final WFSGetCapabilitiesResponse mockResponse = context.mock(WFSGetCapabilitiesResponse.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));

            oneOf(mockWfsService).getFeatureRequestAsString(with(serviceUrl), with(featureType), with(any(FilterBoundingBox.class)), with(maxFeatures), with(srsName), with(outputFormat));
            will(returnValue(wfsRequestString));
            
            allowing(mockResponse).getGetFeatureOutputFormats();will(returnValue(expectedFormats));
            
            oneOf(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(downloads));
            oneOf(mockSession).setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, downloads);
        }});

        ModelAndView mav = controller.makeWfsUrl(serviceUrl, featureType, srsName, bboxSrs, 
                northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude, 
                outputFormat, maxFeatures, name, description, localPath, true, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));

        //Check response
        Assert.assertNotNull(mav.getModel().get("data"));
        ModelMap data = (ModelMap) mav.getModel().get("data");
        Assert.assertEquals(northBoundLatitude, data.get("northBoundLatitude"));
        Assert.assertEquals(eastBoundLongitude, data.get("eastBoundLongitude"));
        Assert.assertEquals(southBoundLatitude, data.get("southBoundLatitude"));
        Assert.assertEquals(westBoundLongitude, data.get("westBoundLongitude"));
        Assert.assertEquals(name, data.get("name"));
        Assert.assertEquals(wfsRequestString, data.get("url"));
        
        //Check session variables
        Assert.assertEquals(1, downloads.size());
        VglDownload download = downloads.get(0);
        Assert.assertEquals(northBoundLatitude, download.getNorthBoundLatitude());
        Assert.assertEquals(eastBoundLongitude, download.getEastBoundLongitude());
        Assert.assertEquals(southBoundLatitude, download.getSouthBoundLatitude());
        Assert.assertEquals(westBoundLongitude, download.getWestBoundLongitude());
        Assert.assertEquals(name, download.getName());
        Assert.assertEquals(description, download.getDescription());
        Assert.assertEquals(localPath, download.getLocalPath());
        Assert.assertEquals(wfsRequestString, download.getUrl());
    }
    
    @Test
    public void testMakeWfsUrlNotSaveSession() throws Exception {
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String srsName = "EPSG:4326";
        final String bboxSrs = "EPSG:4387";
        final String featureType = "test:featureType";
        final String name = "name";
        final String description = "desc";
        final String localPath = "localPath";
        final String serviceUrl = "http://example.org/wfs";
        final String outputFormat = "o-f";
        final Integer maxFeatures = null;
        final String wfsRequestString = serviceUrl + "?request=param";
        
        final String[] expectedFormats = new String[] {"format1", "format2"};
        final WFSGetCapabilitiesResponse mockResponse = context.mock(WFSGetCapabilitiesResponse.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));

            oneOf(mockWfsService).getFeatureRequestAsString(with(serviceUrl), with(featureType), with(any(FilterBoundingBox.class)), with(maxFeatures), with(srsName), with(outputFormat));
            will(returnValue(wfsRequestString));
            
            allowing(mockResponse).getGetFeatureOutputFormats();will(returnValue(expectedFormats));
        }});

        ModelAndView mav = controller.makeWfsUrl(serviceUrl, featureType, srsName, bboxSrs, 
                northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude, 
                outputFormat, maxFeatures, name, description, localPath, false, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));

        //Check response
        Assert.assertNotNull(mav.getModel().get("data"));
        ModelMap data = (ModelMap) mav.getModel().get("data");
        Assert.assertEquals(northBoundLatitude, data.get("northBoundLatitude"));
        Assert.assertEquals(eastBoundLongitude, data.get("eastBoundLongitude"));
        Assert.assertEquals(southBoundLatitude, data.get("southBoundLatitude"));
        Assert.assertEquals(westBoundLongitude, data.get("westBoundLongitude"));
        Assert.assertEquals(name, data.get("name"));
        Assert.assertEquals(wfsRequestString, data.get("url"));
    }
    
    /**
     * Tests that get the number of download items stored 
     * in user session works as expected
     */
    @Test
    public void testGetNumDownloadRequests() {
        final List<VglDownload> vglDownloads = new ArrayList<VglDownload>();
        final VglDownload d1 = new VglDownload(1);
        final VglDownload d2 = new VglDownload(2);
        vglDownloads.add(d1);
        vglDownloads.add(d2);
        
        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(vglDownloads));
        }});

        ModelAndView mav = controller.getNumDownloadRequests(mockRequest);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Integer numDownloads = (Integer) mav.getModel().get("data");
        Assert.assertEquals(new Integer(2), numDownloads);
    }
    
    /**
     * Tests that get the number of download items stored 
     * in user session works as expected when jobDownloadList 
     * attribute can't be found in user session (meaning user
     * has captured any data set).
     */
    @Test
    public void testGetNumDownloadRequests_NullJobDownloadList() {
        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(null));
        }});
        
        ModelAndView mav = controller.getNumDownloadRequests(mockRequest);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Integer numDownloads = (Integer) mav.getModel().get("data");
        Assert.assertEquals(new Integer(0), numDownloads);
    }    
}
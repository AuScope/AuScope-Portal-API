package org.auscope.portal.server.web.controllers;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.WCSService;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;
import org.auscope.portal.core.services.responses.wfs.WFSGetCapabilitiesResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.jmock.Expectations;
import org.junit.Assert;
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
    private HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);
    private HttpSession mockSession = context.mock(HttpSession.class);
    private SimpleWfsService mockWfsService = context.mock(SimpleWfsService.class);
    private WCSService mockWcsService = context.mock(WCSService.class);
    private JobDownloadController controller;
    final String serviceUrl = "http://example.org/service";
    final String coverageUrl = "http://example.org/coverage";

    @Before
    public void setup() {
        controller = new JobDownloadController(mockWfsService, mockWcsService, serviceUrl);
    }

    @Test
    public void testMakeErddapUrlSaveSession() {
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String format = "nc";
        final String layerName = "layer";
        final String name = "name";
        final String description = "desc";
        final String localPath = "localPath";
        /*
        final String parentName = "parent data";
        final String parentUrl = "http://example.org/service";
        final String owner = "CoolCompany@cool.com";
        */
        

        final List<VglDownload> downloads = new ArrayList<>();

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));

            oneOf(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(downloads));
            oneOf(mockSession).setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, downloads);
        }});

        ModelAndView mav = controller.makeErddapUrl(northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, format, layerName, name, description, description, localPath, /*parentName, parentUrl, owner, */true, mockRequest, mockResponse);
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
    public void testMakeErddapUrlNotSaveSession() {
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String format = "nc";
        final String layerName = "layer";
        final String name = "name";
        final String description = "desc";
        final String localPath = "localPath";
        /*
        final String parentName = "parent data";
        final String parentUrl = "http://example.org/service";
        final String owner = "CoolCompany@cool.com";
        */
        
        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
        }});

        ModelAndView mav = controller.makeErddapUrl(northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, format, layerName, name, description, description, localPath, /*parentName, parentUrl, owner, */false, mockRequest, mockResponse);
        
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
    public void testMakeDownloadUrlSaveSession() {
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String name = "name";
        final String description = "desc";
        final String localPath = "localPath";
        /*
        final String parentName = "parent data";
        final String parentUrl = "http://example.org/service";
        final String owner = "CoolCompany@cool.com";
        */

        final List<VglDownload> downloads = new ArrayList<>();

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));

            oneOf(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(downloads));
            oneOf(mockSession).setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, downloads);
        }});

        ModelAndView mav = controller.makeDownloadUrl(serviceUrl, name, description, description, localPath, northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, /*parentName, parentUrl, owner, */true, mockRequest);
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
    public void testMakeDownloadUrlNotSaveSession() {
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String name = "name";
        final String description = "desc";
        final String localPath = "localPath";
        /*
        final String parentName = "parent data";
        final String parentUrl = "http://example.org/service";
        final String owner = "CoolCompany@cool.com";
        */

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
        }});

        ModelAndView mav = controller.makeDownloadUrl(serviceUrl, name, description, description, localPath, northBoundLatitude, eastBoundLongitude, southBoundLatitude, westBoundLongitude, /*parentName, parentUrl, owner, */false, mockRequest);
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
        final String localServiceUrl = "http://example.org/wfs";
        final String outputFormat = "o-f";
        final Integer maxFeatures = null;
        final List<VglDownload> downloads = new ArrayList<>();
        final String wfsRequestString = localServiceUrl + "?request=param";
        /*
        final String parentName = "parent data";
        final String parentUrl = "http://example.org/wfs";
        final String owner = "CoolCompany@cool.com";
        */

        final String[] expectedFormats = new String[] {"format1", "format2"};
        final WFSGetCapabilitiesResponse localMockResponse = context.mock(WFSGetCapabilitiesResponse.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));

            oneOf(mockWfsService).getFeatureRequestAsString(with(localServiceUrl), with(featureType), with(any(FilterBoundingBox.class)), with(maxFeatures), with(srsName), with(outputFormat));
            will(returnValue(wfsRequestString));

            allowing(localMockResponse).getGetFeatureOutputFormats();will(returnValue(expectedFormats));

            oneOf(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(downloads));
            oneOf(mockSession).setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, downloads);
        }});

        ModelAndView mav = controller.makeWfsUrl(localServiceUrl, featureType, srsName, bboxSrs, 
                northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude, 
                outputFormat, maxFeatures, name, description, description, localPath, /*parentName, parentUrl, owner, */true, mockRequest);
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
        final String localsServiceUrl = "http://example.org/wfs";
        final String outputFormat = "o-f";
        final Integer maxFeatures = null;
        final String wfsRequestString = localsServiceUrl + "?request=param";
        /*
        final String parentName = "parent data";
        final String parentUrl = "http://example.org/wfs";
        final String owner = "CoolCompany@cool.com";
        */

        final String[] expectedFormats = new String[] {"format1", "format2"};
        final WFSGetCapabilitiesResponse localMockResponse = context.mock(WFSGetCapabilitiesResponse.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));

            oneOf(mockWfsService).getFeatureRequestAsString(with(localsServiceUrl), with(featureType), with(any(FilterBoundingBox.class)), with(maxFeatures), with(srsName), with(outputFormat));
            will(returnValue(wfsRequestString));

            allowing(localMockResponse).getGetFeatureOutputFormats();will(returnValue(expectedFormats));
        }});

        ModelAndView mav = controller.makeWfsUrl(localsServiceUrl, featureType, srsName, bboxSrs, 
                northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude, 
                outputFormat, maxFeatures, name, description, description, localPath, /*parentName, parentUrl, owner, */false, mockRequest);
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
     * Test the method makeWcsUrl(...)
     */
    @Test
    public void testMakeWcsUrl() throws PortalServiceException {
        final String localsServiceUrl = "http://example.org/wfs";
        final String name = "name";
        final String coverageName = "coverageName";
        final Double northBoundLatitude = 2.0;
        final Double eastBoundLongitude = 4.0;
        final Double southBoundLatitude = 1.0;
        final Double westBoundLongitude = 3.0;
        final String bboxCrs = "EPSG:4387";
        final String outputCrs = "EPSG:4387";
        final String outputFormat = "o-f";
        final Integer outputWidth = 50;
        final Integer outputHeight = 50;
        final Double outputResolutionX = 1.0;
        final Double outputResolutionY = 1.0;
        final String description = "description";
        final String fullDescription = "fullDescription";
        final String localPath = "";
        
        context.checking(new Expectations() {{
        	oneOf(mockWcsService).getCoverageRequestAsString(
        			with(equal(localsServiceUrl)),
        			with(equal(coverageName)),
        			with(equal(outputFormat)),
        			with(equal(outputCrs)),
        			with(equal(new Dimension(outputWidth, outputHeight))),
        			with(equal(new Resolution(outputResolutionX, outputResolutionY))),
        			with(equal(bboxCrs)),
        			with(any(CSWGeographicBoundingBox.class)),
        			with(aNull(TimeConstraint.class)),
        			with(aNull(Map.class)));
        	will(returnValue(coverageUrl));
        }});
        ModelAndView mav = controller.makeWcsUrl(localsServiceUrl, coverageName, outputFormat, bboxCrs,
        		outputCrs, outputWidth, outputHeight, outputResolutionX, outputResolutionY,
        		northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude,
        		name, description, fullDescription, localPath,
        		false, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue(((Boolean) mav.getModel().get("success")));
        Assert.assertNotNull(mav.getModel().get("data"));
        
        ModelMap data = (ModelMap) mav.getModel().get("data");
        Assert.assertEquals(northBoundLatitude, data.get("northBoundLatitude"));
        Assert.assertEquals(eastBoundLongitude, data.get("eastBoundLongitude"));
        Assert.assertEquals(southBoundLatitude, data.get("southBoundLatitude"));
        Assert.assertEquals(westBoundLongitude, data.get("westBoundLongitude"));
        Assert.assertEquals(name, data.get("name"));
        Assert.assertNotNull(data.get("url"));
    }
    
    /**
     * Tests that get the number of download items stored
     * in user session works as expected
     */
    @Test
    public void testGetNumDownloadRequests() {
        final List<VglDownload> vglDownloads = new ArrayList<>();
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
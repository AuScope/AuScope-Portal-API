package org.auscope.portal.server.web.controllers;

import java.util.Collection;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;
import org.auscope.portal.core.services.responses.wfs.WFSGetCapabilitiesResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class TestWFSController extends PortalTestClass {
    
    private SimpleWfsService mockWfsService = context.mock(SimpleWfsService.class);
    private WFSController controller;
    
    @Before
    public void setup() {
        controller = new WFSController(mockWfsService);
    }
    
    @Test
    public void testGetFeatureCount() throws Exception {
        
        final String serviceUrl = "http://service.com/wfs";
        final String featureType = "test:feature";
        final String bboxCrs ="bbox-srs";
        final Double northBoundLatitude = 1.0;
        final Double southBoundLatitude = 2.0;
        final Double eastBoundLongitude = 3.0;
        final Double westBoundLongitude = 4.0;
        final Integer maxFeatures = null;
        
        final WFSCountResponse result = new WFSCountResponse(123);
        
        context.checking(new Expectations() {{
            oneOf(mockWfsService).getWfsFeatureCount(with(serviceUrl), with(featureType), with(any(String.class)), with(maxFeatures), with((String)null));
            will(returnValue(result));
        }});
        
        ModelAndView mav = controller.requestFeatureCount(serviceUrl, featureType, bboxCrs, northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude, maxFeatures);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success")); 
        Assert.assertEquals((Integer) result.getNumberOfFeatures(), (Integer) mav.getModel().get("data"));   
    }
    
    @Test
    public void testGetFeatureCount_ServiceError() throws Exception {
        final String serviceUrl = "http://service.com/wfs";
        final String featureType = "test:feature";
        final String bboxCrs ="bbox-srs";
        final Double northBoundLatitude = 1.0;
        final Double southBoundLatitude = 2.0;
        final Double eastBoundLongitude = 3.0;
        final Double westBoundLongitude = 4.0;
        final Integer maxFeatures = null;
        
        context.checking(new Expectations() {{
            oneOf(mockWfsService).getWfsFeatureCount(with(serviceUrl), with(featureType), with(any(String.class)), with(maxFeatures), with((String)null));
            will(throwException(new PortalServiceException("error")));
        }});
        
        ModelAndView mav = controller.requestFeatureCount(serviceUrl, featureType, bboxCrs, northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude, maxFeatures);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));    
    }
    
    @Test
    public void testGetFeatureRequestOutputFormats() throws Exception {
        final String serviceUrl = "http://service.com/wfs";
        
        final WFSGetCapabilitiesResponse getCapResp = new WFSGetCapabilitiesResponse();
        
        getCapResp.setFeatureTypes(new String[] {"ft:1", "ft:2"});
        getCapResp.setGetFeatureOutputFormats(new String[] {"format1", "format2"});
        
        context.checking(new Expectations() {{
            oneOf(mockWfsService).getCapabilitiesResponse(serviceUrl);
            will(returnValue(getCapResp));
        }});
        
        ModelAndView mav = controller.getFeatureRequestOutputFormats(serviceUrl);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success")); 
        
        Collection<ModelMap> data = (Collection<ModelMap>) mav.getModel().get("data");
        Assert.assertNotNull(mav);
        String[] resultingFormats = new String[data.size()];
        int i = 0;
        for (ModelMap map : data) {
            resultingFormats[i++] = map.get("format").toString();
        }
        Assert.assertArrayEquals(getCapResp.getGetFeatureOutputFormats(), resultingFormats);
    }
    
    @Test
    public void testGetFeatureRequestOutputFormats_ServiceError() throws Exception {
        final String serviceUrl = "http://service.com/wfs";
        
        context.checking(new Expectations() {{
            oneOf(mockWfsService).getCapabilitiesResponse(serviceUrl);
            will(throwException(new PortalServiceException("error")));
        }});
        
        ModelAndView mav = controller.getFeatureRequestOutputFormats(serviceUrl);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean)mav.getModel().get("success")); 
    }
}

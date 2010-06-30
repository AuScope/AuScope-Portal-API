package org.auscope.portal.server.web.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.KnownFeatureTypeDefinition;
import org.auscope.portal.server.web.service.CSWService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * User: Mathew Wyatt
 * Date: 27/08/2009
 * @version $Id$
 */
public class TestCSWController {

    /**
     * JMock context
     */
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /**
     * Main object we are testing
     */
    private CSWService cswService = context.mock(CSWService.class);

     /**
     * Mock property configurer
     */
    private PortalPropertyPlaceholderConfigurer propertyConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);

    /**
    * Mock KnownTypes arraylist
    */
    @SuppressWarnings("unchecked")
    private ArrayList<KnownFeatureTypeDefinition> knownTypes = context.mock(ArrayList.class);

    /**
     * The controller
     */
    private CSWController cswController;

    private HttpServletRequest mockHttpRequest = context.mock(HttpServletRequest.class);
    private HttpServletResponse mockHttpResponse = context.mock(HttpServletResponse.class);

    @Before
    public void setup() throws Exception {
        final String serviceUrl = "somejunk";

        context.checking(new Expectations() {{
            oneOf(propertyConfigurer).resolvePlaceholder(with(any(String.class)));will(returnValue(serviceUrl));
            oneOf(cswService).setServiceUrl(serviceUrl);
            oneOf(cswService).updateRecordsInBackground();
        }});

        cswController = new CSWController(cswService, propertyConfigurer, knownTypes, new ArrayList());
    }

    /**
     * Tests whether getting a generic feature (with an already known type) will work return nothing
     * @throws Exception
     */
    @Test
    public void testGetGenericFeaturesNoResults() throws Exception {
        final KnownFeatureTypeDefinition def = new KnownFeatureTypeDefinition("0", "1", "2", "3", "4");
        final String expectedJSONResponse = "[]";
        final Iterator mockIterator = context.mock(Iterator.class);
        final StringWriter actualJSONResponse = new StringWriter();
        final CSWRecord mockRecord = context.mock(CSWRecord.class);
        

        context.checking(new Expectations() {{
            oneOf(cswService).updateRecordsInBackground();
            oneOf(knownTypes).iterator();will(returnValue(mockIterator));
            oneOf(mockIterator).hasNext();will(returnValue(true));
            oneOf(mockIterator).next();will(returnValue(def));
            oneOf(cswService).getWFSRecords();will(returnValue(new CSWRecord[]{mockRecord}));
            
            allowing(mockRecord).getOnlineResourceName();will(returnValue("0"));
            allowing(mockRecord).getDataIdentificationAbstract();will(returnValue("1"));
            
            
            oneOf(mockRecord).getServiceUrl();
            oneOf(mockRecord).getContactOrganisation();

            oneOf(mockIterator).hasNext();will(returnValue(false));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        ModelAndView modelAndView = cswController.getGenericFeatures(mockHttpRequest);

        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }
    
    /**
     * Tests whether getting a generic feature (with an already known but non matching type) will work
     * @throws Exception
     */
    @Test
    public void testGetGenericFeatures() throws Exception {
        final KnownFeatureTypeDefinition def = new KnownFeatureTypeDefinition("0", "1", "2", "3", "4");
        final String expectedJSONResponse = "[[\"1\",\" Institutions: , \",\"getAllFeatures.do\",\"wfs\","+("aGenericType".hashCode())+",\"aGenericType\",[\"\"],\"true\",\"<img src='js/external/extjs/resources/images/default/grid/done.gif'>\",\"<img width='16' heigh='16' src=''>\",\"\",\"<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/page_code.png'><\\/a>\"]]";
        final Iterator mockIterator = context.mock(Iterator.class);
        final StringWriter actualJSONResponse = new StringWriter();
        final CSWRecord mockRecord = context.mock(CSWRecord.class);
        

        context.checking(new Expectations() {{
            oneOf(cswService).updateRecordsInBackground();
            oneOf(knownTypes).iterator();will(returnValue(mockIterator));
            oneOf(mockIterator).hasNext();will(returnValue(true));
            oneOf(mockIterator).next();will(returnValue(def));
            oneOf(cswService).getWFSRecords();will(returnValue(new CSWRecord[]{mockRecord}));
            
            allowing(mockRecord).getOnlineResourceName();will(returnValue("aGenericType"));
            allowing(mockRecord).getOnlineResourceDescription();will(returnValue("1"));
            
            
            oneOf(mockRecord).getServiceUrl();
            oneOf(mockRecord).getContactOrganisation();

            oneOf(mockIterator).hasNext();will(returnValue(false));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        ModelAndView modelAndView = cswController.getGenericFeatures(mockHttpRequest);

        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }
    
    @Test
    public void testGetComplexFeatures() throws Exception {
        final String orgName = "testOrg";
        final KnownFeatureTypeDefinition def = new KnownFeatureTypeDefinition("0", "1", "2", "3", "4");
        final String expectedJSONResponse = "[[\"1\",\"2 Institutions: " + orgName + ", \",[\"" + orgName +  "\"],\"3\",\"wfs\","+def.hashCode()+",\"0\",[\"\"],true,\"<img src='js/external/extjs/resources/images/default/grid/done.gif'>\",\"<img width='16' heigh='16' src='4'>\",\"4\",\"<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/page_code.png'><\\/a>\"]]";
        @SuppressWarnings("unchecked")
        final Iterator<KnownFeatureTypeDefinition> mockIterator = context.mock(Iterator.class);
        final StringWriter actualJSONResponse = new StringWriter();
        final CSWRecord mockRecord = context.mock(CSWRecord.class);

        context.checking(new Expectations() {{
        	oneOf(propertyConfigurer).resolvePlaceholder(with(any(String.class)));will(returnValue("somejunk"));
            oneOf(cswService).updateRecordsInBackground();
            oneOf(knownTypes).iterator();will(returnValue(mockIterator));
            oneOf(mockIterator).hasNext();will(returnValue(true));
            oneOf(mockIterator).next();will(returnValue(def));
            oneOf(cswService).getWFSRecordsForTypename(def.getFeatureTypeName());will(returnValue(new CSWRecord[]{mockRecord}));

            oneOf(mockRecord).getServiceUrl();
            allowing(mockRecord).getContactOrganisation();will(returnValue(orgName));

            oneOf(mockIterator).hasNext();will(returnValue(false));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
        }});

        ModelAndView modelAndView = cswController.getComplexFeatures(mockHttpRequest);

        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }
    
    

    /**
     * Test for when there are no services for a given feature type
     * @throws Exception
     */
    @Test
    public void testGetComplexFeaturesNoServices() throws Exception {
        final KnownFeatureTypeDefinition def = new KnownFeatureTypeDefinition("0", "1", "2", "3", "4");
        final String expectedJSONResponse = "[]";
        @SuppressWarnings("unchecked")
        final Iterator<KnownFeatureTypeDefinition> mockIterator = context.mock(Iterator.class);
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
        	oneOf(propertyConfigurer).resolvePlaceholder(with(any(String.class)));will(returnValue("somejunk"));
            oneOf(cswService).updateRecordsInBackground();
            oneOf(knownTypes).iterator();will(returnValue(mockIterator));
            oneOf(mockIterator).hasNext();will(returnValue(true));
            oneOf(mockIterator).next();will(returnValue(def));
            oneOf(cswService).getWFSRecordsForTypename(def.getFeatureTypeName());will(returnValue(new CSWRecord[]{}));

            oneOf(mockIterator).hasNext();will(returnValue(false));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
            
            oneOf(mockHttpRequest).isUserInRole("ROLE_USER");will(returnValue(true));
        }});

        ModelAndView modelAndView = cswController.getComplexFeatures(mockHttpRequest);

        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }

    /**
     * Test that the JSON is formatted correctly for WMS layers
     */
    @Test
    public void testGetWMSLayers() throws Exception {
        final String orgName = "testOrg";
    	
        final CSWRecord mockRecord = context.mock(CSWRecord.class);
        final String expectedJSONResponse = "[[\"\",\"\",\"" + orgName + "\",\"\",\"wms\","+mockRecord.hashCode()+",\"\",[\"\"],true,\"<img src='js/external/extjs/resources/images/default/grid/done.gif'>\",\"<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/picture_link.png'><\\/a>\",\"1.0\"]]";
        final StringWriter actualJSONResponse = new StringWriter();

        context.checking(new Expectations() {{
        	oneOf(propertyConfigurer).resolvePlaceholder(with(any(String.class)));will(returnValue("somejunk"));
            oneOf(cswService).updateRecordsInBackground();
            oneOf(cswService).getWMSRecords();will(returnValue(new CSWRecord[]{mockRecord}));

            oneOf(mockRecord).getServiceName();
            oneOf(mockRecord).getDataIdentificationAbstract();
            oneOf(mockRecord).getOnlineResourceName();
            oneOf(mockRecord).getServiceUrl();
            oneOf(mockRecord).getContactOrganisation();will(returnValue(orgName));

            //check that the correct response is getting output
            oneOf (mockHttpResponse).setContentType(with(any(String.class)));
            oneOf (mockHttpResponse).getWriter(); will(returnValue(new PrintWriter(actualJSONResponse)));
            oneOf (mockHttpRequest).isUserInRole("ROLE_USER");will(returnValue(true));
        }});
        
        ModelAndView modelAndView = cswController.getWMSLayers(mockHttpRequest);

        //check that our JSON response has been nicely populated
        //calling the renderer will write the JSON to our mocks
        modelAndView.getView().render(modelAndView.getModel(), mockHttpRequest, mockHttpResponse);

        //check that the actual is the expected
        if(expectedJSONResponse.equals(actualJSONResponse.getBuffer().toString()))
            Assert.assertTrue(true);
        else
            Assert.assertFalse(true);
    }

    //TODO: testWMS Layers no layers available, none recorded in the CSW

}

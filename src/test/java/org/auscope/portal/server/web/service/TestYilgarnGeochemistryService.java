package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.auscope.portal.gsml.YilgarnLocatedSpecimenRecord;
import org.auscope.portal.gsml.YilgarnObservationRecord;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for YilgarnGeochemistryService
 *
 * @author Josh Vote
 *
 */
public class TestYilgarnGeochemistryService extends PortalTestClass {

    private WFSGetFeatureMethodMaker mockMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);
    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private YilgarnGeochemistryService service;

    /**
     * Load our mock objects into our test instance
     */
    @Before
    public void setup() {
        service = new YilgarnGeochemistryService(mockServiceCaller, mockMethodMaker);
    }

    /**
     * Tests that parsing works as expected
     *
     * @throws Exception
     */
    @Test
    public void testLocatedSpecimenParsing() throws Exception {
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/yilgarn/YilgarnLocSpecimenResponse.xml");
        final ByteArrayInputStream responseStream = new ByteArrayInputStream(responseString.getBytes());
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(responseStream, null)));

                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, YilgarnGeochemistryService.LOCATED_SPECIMEN_TYPENAME,
                        featureId, null);
                will(returnValue(mockMethod));

                oneOf(mockMethod).releaseConnection();
            }
        });

        YilgarnLocatedSpecimenRecord response = service.getLocatedSpecimens(serviceUrl, featureId);
        Assert.assertNotNull(response);
        Assert.assertEquals("Material-Class", response.getMaterialClass());
        Assert.assertEquals(1, response.getRelatedObservations().length);

        YilgarnObservationRecord observation = response.getRelatedObservations()[0];
        Assert.assertEquals("serviceName1", observation.getServiceName());
        Assert.assertEquals("2006-09-28", observation.getDate());
        Assert.assertEquals("quantityName1", observation.getObservedMineralName());
        Assert.assertEquals("MineralDescription1", observation.getPreparationDetails());
        Assert.assertEquals("obsProcessContact1", observation.getLabDetails());
        Assert.assertEquals("obsProcessMethod1", observation.getAnalyticalMethod());
        Assert.assertEquals("observedProperty", observation.getObservedProperty());
        Assert.assertEquals("quantityName1", observation.getAnalyteName());
        Assert.assertEquals("21.4", observation.getAnalyteValue());
        Assert.assertEquals("%", observation.getUom());
    }

    /**
     * Tests that parsing returns null when ID doesnt match anything
     *
     * @throws Exception
     */
    @Test
    public void testLocatedSpecimenParsingEmpty() throws Exception {
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/wfs/EmptyWFSResponse.xml");
        final ByteArrayInputStream responseStream = new ByteArrayInputStream(responseString.getBytes());
        final String serviceUrl = "http://service/wfs";
        final String featureId = "doesnt-match-anything";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(responseStream, null)));

                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, YilgarnGeochemistryService.LOCATED_SPECIMEN_TYPENAME,
                        featureId, null);
                will(returnValue(mockMethod));

                oneOf(mockMethod).releaseConnection();
            }
        });

        YilgarnLocatedSpecimenRecord response = service.getLocatedSpecimens(serviceUrl, featureId);
        Assert.assertNull(response);
    }

    /**
     * Tests that parsing fails if the service cant be connected to
     *
     * @throws Exception
     */
    @Test(expected = PortalServiceException.class)
    public void testLocatedSpecimenConnectError() throws Exception {
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(throwException(new ConnectException()));

                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, YilgarnGeochemistryService.LOCATED_SPECIMEN_TYPENAME,
                        featureId, null);
                will(returnValue(mockMethod));

                oneOf(mockMethod).releaseConnection();
            }
        });

        service.getLocatedSpecimens(serviceUrl, featureId);
    }

    /**
     * Tests that parsing fails when there is an OWSException
     *
     * @throws Exception
     */
    @Test(expected = PortalServiceException.class)
    public void testLocatedSpecimenParsingOWSException() throws Exception {
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml");
        final ByteArrayInputStream responseStream = new ByteArrayInputStream(responseString.getBytes());
        final String serviceUrl = "http://service/wfs";
        final String featureId = "feature-Id-string";

        context.checking(new Expectations() {
            {
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(responseStream));

                oneOf(mockMethodMaker).makeGetMethod(serviceUrl, YilgarnGeochemistryService.LOCATED_SPECIMEN_TYPENAME,
                        featureId, null);
                will(returnValue(mockMethod));

                oneOf(mockMethod).releaseConnection();
            }
        });

        service.getLocatedSpecimens(serviceUrl, featureId);
    }

}

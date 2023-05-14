package org.auscope.portal.server.web.service;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.csw.CSWRecordsHostFilter;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.auscope.portal.gsml.SF0BoreholeFilter;
import org.auscope.portal.nvcl.NVCLNamespaceContext;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class TestBoreholeService.
 */
public class TestSF0BoreholeService extends PortalTestClass {

    /** The service. */
    private SF0BoreholeService service;

    /** The mock http service caller. */
    private HttpServiceCaller mockHttpServiceCaller = context.mock(HttpServiceCaller.class);

    /** The mock method maker. */
    private WFSGetFeatureMethodMaker mockMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);

    /** The mock HTTP method */
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);

    /** The Constant GETSCANNEDBOREHOLEXML. */
    private static final String GETSCANNEDBOREHOLEXML = "org/auscope/portal/nvcl/GetScannedBorehole.xml";

    /** The Constant HOLEIDS. */
    private static final String[] HOLEIDS = new String[] {"WTB5", "GSDD006", "GDDH7"};

    /**
     * Setup.
     *
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception {
        service = new SF0BoreholeService(mockHttpServiceCaller, mockMethodMaker);
    }

    /**
     * Test get all boreholes no bbox.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetAllBoreholesNoBbox() throws Exception {
        final FilterBoundingBox bbox = null;
        final String serviceUrl = "http://example.com";
        final int maxFeatures = 45;
        final String boreholeName = "borehole-name";
        final String custodian = "custodian";
        final String dateOfDrillingStart = "2011-01-01";
        final String dateOfDrillingEnd = "2011-01-02";
        final String gmlString = "xmlString";

        context.checking(new Expectations() {
            {

                oneOf(mockMethodMaker).makePostMethod(with(equal(serviceUrl)), with(equal("gsmlp:BoreholeView")),
                        with(any(String.class)), with(equal(maxFeatures)), with(any(String.class)),
                        with(equal(ResultType.Results)), with(equal((String) null)), with(equal((String) null)));
                will(returnValue(mockMethod));

                oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpRequestBase.class)));
                will(returnValue(gmlString));
            }
        });

        WFSResponse result = service.getAllBoreholes(serviceUrl, boreholeName, custodian,
                dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null, "gsmlp:BoreholeView", false);
        Assert.assertNotNull(result);
        Assert.assertEquals(gmlString, result.getData());
        Assert.assertSame(mockMethod, result.getMethod());
    }


    /**
     * Test get all boreholes bbox.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetAllBoreholesBbox() throws Exception {
        final FilterBoundingBox bbox = new FilterBoundingBox("mySrs", new double[] {0, 1}, new double[] {2, 3});
        final String serviceUrl = "http://example.com";
        final int maxFeatures = 45;
        final String boreholeName = "borehole-name";
        final String custodian = "custodian";
        final String dateOfDrillingStart = "2011-01-01";
        final String dateOfDrillingEnd = "2011-01-02";
        final String gmlString = "xmlString";

        context.checking(new Expectations() {
            {

                oneOf(mockMethodMaker).makePostMethod(with(equal(serviceUrl)), with(equal("gsmlp:BoreholeView")),
                        with(any(String.class)), with(equal(maxFeatures)), with(any(String.class)),
                        with(equal(ResultType.Results)), with(equal((String) null)), with(equal((String) null)));
                will(returnValue(mockMethod));
                oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpRequestBase.class)));
                will(returnValue(gmlString));
            }
        });

        WFSResponse result = service.getAllBoreholes(serviceUrl, boreholeName, custodian,
                dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null, "gsmlp:BoreholeView", false);
        Assert.assertNotNull(result);
        Assert.assertEquals(gmlString, result.getData());
        Assert.assertSame(mockMethod, result.getMethod());
    }


    /**
     * Test get all boreholes with a custom borehole layer name 
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetAllBoreholesMyBoreholeView() throws Exception {
        final String serviceUrl = "http://example.com";
        final int maxFeatures = 45;
        final String gmlString = "xmlString";

        context.checking(new Expectations() {
            {

                oneOf(mockMethodMaker).makePostMethod(with(equal(serviceUrl)), with(equal("gsmlp:MyBoreholeView")),
                        with(any(String.class)), with(equal(maxFeatures)), with(any(String.class)),
                        with(equal(ResultType.Results)), with(equal((String) null)), with(equal((String) null)));
                will(returnValue(mockMethod));
                oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpRequestBase.class)));
                will(returnValue(gmlString));
            }
        });

        WFSResponse result = service.getAllBoreholes(serviceUrl, null, null,
                null, null, maxFeatures, null, null, "gsmlp:MyBoreholeView", false);
        Assert.assertNotNull(result);
        Assert.assertEquals(gmlString, result.getData());
        Assert.assertSame(mockMethod, result.getMethod());
    }


    /**
     * Test get restricted boreholes bbox.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetRestrictedBoreholesBbox() throws Exception {
        final String serviceUrl = "http://example.com";
        final int maxFeatures = 45;
        final String boreholeName = "asda";
        final String custodian = "shaksdhska";
        final String dateOfDrillingStart = "2010-01-02";
        final String dateOfDrillingEnd = "2010-01-03";
        final String gmlString = "xmlString";
        final String filterString = (new SF0BoreholeFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, null, null, null,null))
                .getFilterStringAllRecords();

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).makePostMethod(with(equal(serviceUrl)), with(equal("gsmlp:BoreholeView")),
                        with(equal(filterString)), with(equal(maxFeatures)), with(any(String.class)),
                        with(equal(ResultType.Results)), with(equal((String) null)), with(equal((String) null)));
                will(returnValue(mockMethod));

                oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpRequestBase.class)));
                will(returnValue(gmlString));
            }
        });

        WFSResponse result = service.getAllBoreholes(serviceUrl, boreholeName, custodian,
                dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, null, null, "gsmlp:BoreholeView", false);
        Assert.assertNotNull(result);
        Assert.assertEquals(gmlString, result.getData());
        Assert.assertSame(mockMethod, result.getMethod());
    }

    /**
     * Tests that the service correctly parses a response from an NVCL WFS.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetHyloggerIDs() throws Exception {
        final CSWRecord mockRecord1 = context.mock(CSWRecord.class, "mockRecord1"); //good record
        final CSWRecord mockRecord2 = context.mock(CSWRecord.class, "mockRecord2"); //has the wrong wfs
        final CSWRecord mockRecord3 = context.mock(CSWRecord.class, "mockRecord3"); //has no wfs
        final CSWCacheService mockCSWService = context.mock(CSWCacheService.class);
        final CSWRecordsHostFilter hostFilter = new CSWRecordsHostFilter("");
        final AbstractCSWOnlineResource mockRecord1Resource1 = new CSWOnlineResourceImpl(new URL(
                "http://record.1.resource.1"), "wfs", "dne", "description");
        final AbstractCSWOnlineResource mockRecord1Resource2 = new CSWOnlineResourceImpl(new URL(
                "http://record.1.resource.2"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "description");

        final AbstractCSWOnlineResource mockRecord2Resource1 = new CSWOnlineResourceImpl(new URL(
                "http://record.2.resource.1"), "wfs", "dne", "description");

        final String successResponse = ResourceUtil.loadResourceAsString(GETSCANNEDBOREHOLEXML);

        context.checking(new Expectations() {
            {
                oneOf(mockCSWService).getWFSRecords();
                will(returnValue(Arrays.asList(mockRecord1, mockRecord2, mockRecord3)));

                oneOf(mockRecord1).getOnlineResourcesByType(hostFilter, OnlineResourceType.WFS);
                will(returnValue(new AbstractCSWOnlineResource[] {mockRecord1Resource1, mockRecord1Resource2}));

                oneOf(mockRecord2).getOnlineResourcesByType(hostFilter, OnlineResourceType.WFS);
                will(returnValue(new AbstractCSWOnlineResource[] {mockRecord2Resource1}));

                oneOf(mockRecord3).getOnlineResourcesByType(hostFilter, OnlineResourceType.WFS);
                will(returnValue(new AbstractCSWOnlineResource[] {}));

                oneOf(mockMethodMaker).makeGetMethod(mockRecord1Resource2.getLinkage().toString(),
                        mockRecord1Resource2.getName(), (Integer) null, null);
                oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpRequestBase.class)));
                will(returnValue(successResponse));
            }
        });

        List<String> restrictedIDs = service.discoverHyloggerBoreholeIDs(mockCSWService, hostFilter);
        Assert.assertNotNull(restrictedIDs);
        Assert.assertArrayEquals(HOLEIDS, restrictedIDs.toArray(new String[restrictedIDs.size()]));
    }

    /**
     * Tests that the service correctly parses a response from an NVCL WFS (even when there is an error).
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetHyloggerIDsWithError() throws Exception {
        final CSWRecord mockRecord1 = context.mock(CSWRecord.class, "mockRecord1"); //will return failure
        final CSWRecord mockRecord2 = context.mock(CSWRecord.class, "mockRecord2"); //good record
        final CSWCacheService mockCSWService = context.mock(CSWCacheService.class);
        final HttpRequestBase mockRecord1Method = context.mock(HttpRequestBase.class, "rec1method");
        final HttpRequestBase mockRecord2Method = context.mock(HttpRequestBase.class, "rec2method");
        final CSWRecordsHostFilter hostFilter = new CSWRecordsHostFilter("");
        final AbstractCSWOnlineResource mockRecord1Resource1 = new CSWOnlineResourceImpl(new URL(
                "http://record.1.resource.1"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "description");
        final AbstractCSWOnlineResource mockRecord2Resource1 = new CSWOnlineResourceImpl(new URL(
                "http://record.2.resource.1"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "description");

        final String successResponse = ResourceUtil.loadResourceAsString(GETSCANNEDBOREHOLEXML);

        context.checking(new Expectations() {
            {
                oneOf(mockCSWService).getWFSRecords();
                will(returnValue(Arrays.asList(mockRecord1, mockRecord2)));

                oneOf(mockRecord1).getOnlineResourcesByType(hostFilter, OnlineResourceType.WFS);
                will(returnValue(new AbstractCSWOnlineResource[] {mockRecord1Resource1}));

                oneOf(mockRecord2).getOnlineResourcesByType(hostFilter, OnlineResourceType.WFS);
                will(returnValue(new AbstractCSWOnlineResource[] {mockRecord2Resource1}));

                oneOf(mockMethodMaker).makeGetMethod(mockRecord1Resource1.getLinkage().toString(),
                        mockRecord1Resource1.getName(), (Integer) null, null);
                will(returnValue(mockRecord1Method));

                oneOf(mockMethodMaker).makeGetMethod(mockRecord2Resource1.getLinkage().toString(),
                        mockRecord2Resource1.getName(), (Integer) null, null);
                will(returnValue(mockRecord2Method));

                oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockRecord1Method);
                will(throwException(new Exception("I'm an exception!")));

                oneOf(mockHttpServiceCaller).getMethodResponseAsString(mockRecord2Method);
                will(returnValue(successResponse));
            }
        });

        List<String> restrictedIDs = service.discoverHyloggerBoreholeIDs(mockCSWService, hostFilter);
        Assert.assertNotNull(restrictedIDs);
        Assert.assertArrayEquals(HOLEIDS, restrictedIDs.toArray(new String[restrictedIDs.size()]));
    }

    /**
     * Tests that the service correctly parses a response from an NVCL WFS.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetHyloggerWithOWSError() throws Exception {
        final CSWRecord mockRecord1 = context.mock(CSWRecord.class, "mockRecord1"); //good record
        final CSWRecord mockRecord2 = context.mock(CSWRecord.class, "mockRecord2"); //has the wrong wfs
        final CSWRecord mockRecord3 = context.mock(CSWRecord.class, "mockRecord3"); //has no wfs
        final CSWCacheService mockCSWService = context.mock(CSWCacheService.class);
        final CSWRecordsHostFilter hostFilter = new CSWRecordsHostFilter("");
        final AbstractCSWOnlineResource mockRecord1Resource1 = new CSWOnlineResourceImpl(new URL(
                "http://record.1.resource.1"), "wfs", "dne", "description");
        final AbstractCSWOnlineResource mockRecord1Resource2 = new CSWOnlineResourceImpl(new URL(
                "http://record.1.resource.2"), "wfs", NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME, "description");

        final AbstractCSWOnlineResource mockRecord2Resource1 = new CSWOnlineResourceImpl(new URL(
                "http://record.2.resource.1"), "wfs", "dne", "description");

        final String owsErrorResponse = ResourceUtil
                .loadResourceAsString("org/auscope/portal/core/test/responses/ows/OWSExceptionSample1.xml");

        context.checking(new Expectations() {
            {
                oneOf(mockCSWService).getWFSRecords();
                will(returnValue(Arrays.asList(mockRecord1, mockRecord2, mockRecord3)));

                oneOf(mockRecord1).getOnlineResourcesByType(hostFilter, OnlineResourceType.WFS);
                will(returnValue(new AbstractCSWOnlineResource[] {mockRecord1Resource1, mockRecord1Resource2}));

                oneOf(mockRecord2).getOnlineResourcesByType(hostFilter, OnlineResourceType.WFS);
                will(returnValue(new AbstractCSWOnlineResource[] {mockRecord2Resource1}));

                oneOf(mockRecord3).getOnlineResourcesByType(hostFilter, OnlineResourceType.WFS);
                will(returnValue(new AbstractCSWOnlineResource[] {}));

                oneOf(mockMethodMaker).makeGetMethod(mockRecord1Resource2.getLinkage().toString(),
                        mockRecord1Resource2.getName(), (Integer) null, null);
                oneOf(mockHttpServiceCaller).getMethodResponseAsString(with(any(HttpRequestBase.class)));
                will(returnValue(owsErrorResponse));
            }
        });

        List<String> restrictedIDs = service.discoverHyloggerBoreholeIDs(mockCSWService, hostFilter);
        Assert.assertNotNull(restrictedIDs);
        Assert.assertEquals(0, restrictedIDs.size());
    }

}

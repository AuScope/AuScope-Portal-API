package org.auscope.portal.server.web.service;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.jmock.CSWGetDataRecordsFilterMatcher;
import org.auscope.portal.server.web.service.csw.FacetedSearchResponse;
import org.auscope.portal.server.web.service.csw.SearchFacet;
import org.auscope.portal.server.web.service.csw.SearchFacet.Comparison;
import org.jmock.Expectations;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the filtering at LocalCSWFilterService
 * @author Josh Vote (CSIRO)
 *
 */
public class TestLocalCSWFilterService extends PortalTestClass {

    private CSWFilterService mockFilterService = context.mock(CSWFilterService.class);
    private CSWGetRecordResponse mockResponse1 = context.mock(CSWGetRecordResponse.class, "mockResponse1");
    private CSWGetRecordResponse mockResponse2 = context.mock(CSWGetRecordResponse.class, "mockResponse2");
    private CSWGetRecordResponse mockResponse3 = context.mock(CSWGetRecordResponse.class, "mockResponse3");
    private LocalCSWFilterService localFilterService;

    @Before
    public void setup() {
        localFilterService = new LocalCSWFilterService(mockFilterService);
    }

    /**
     * Ensures our remote filter only requests the bare minimum number of records (while also respecting page size)
     * @throws Exception
     */
    @Test
    public void testRemoteFilter() throws Exception {
        final String serviceId = "service-id";
        final FilterBoundingBox bbox = new FilterBoundingBox("WGS:84", new double[] {-10.0,  -10.0}, new double[] {10.0,  10.0});
        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<FilterBoundingBox>(bbox, "bbox", Comparison.Equal),
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal),
                new SearchFacet<DateTime>(new DateTime(0l), "datefrom", Comparison.GreaterThan),
                new SearchFacet<DateTime>(new DateTime(1000l), "dateto", Comparison.LessThan));

        final int startIndex = 1;
        final int maxRecords = 3;
        final int pageSize = 2;

        localFilterService.setPageSize(pageSize);

        context.checking(new Expectations() {{
            //Request a full page initially
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                                                        with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                                                        with(equal(pageSize)),
                                                        with(equal(startIndex)));
            will(returnValue(mockResponse1));

            allowing(mockResponse1).getNextRecord();will(returnValue(3));
            allowing(mockResponse1).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse1).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("rec1"), new CSWRecord("rec2"))));

            //one remaining record to get on the second request (despite 2 being available)
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(1)),
                    with(equal(startIndex + pageSize)));
            will(returnValue(mockResponse2));

            allowing(mockResponse2).getNextRecord();will(returnValue(4));
            allowing(mockResponse2).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse2).getRecordsReturned();will(returnValue(1));
            allowing(mockResponse2).getRecords();will(returnValue(Arrays.asList(new CSWRecord("rec3"))));

        }});

        FacetedSearchResponse response = localFilterService.getFilteredRecords(serviceId, facets, startIndex, maxRecords);

        Assert.assertNotNull(response);
        Assert.assertEquals(4, response.getNextIndex());
        Assert.assertEquals(3, response.getRecords().size());

        Assert.assertEquals("rec1", response.getRecords().get(0).getFileIdentifier());
        Assert.assertEquals("rec2", response.getRecords().get(1).getFileIdentifier());
        Assert.assertEquals("rec3", response.getRecords().get(2).getFileIdentifier());
    }

    /**
     * Ensures our local filter filters correctly (and requests appropriate sized requests)
     * @throws Exception
     */
    @Test
    public void testLocalFilter() throws Exception {
        final String serviceId = "service-id";
        final FilterBoundingBox bbox = new FilterBoundingBox("WGS:84", new double[] {-10.0,  -10.0}, new double[] {10.0,  10.0});
        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<FilterBoundingBox>(bbox, "bbox", Comparison.Equal),
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal),
                new SearchFacet<OnlineResourceType>(OnlineResourceType.WCS, "servicetype", Comparison.Equal),
                new SearchFacet<OnlineResourceType>(OnlineResourceType.WMS, "servicetype", Comparison.Equal));

        final int startIndex = 1;
        final int maxRecords = 3;
        final int pageSize = 2;

        localFilterService.setPageSize(pageSize);

        context.checking(new Expectations() {{
            //Request a full page initially
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                                                        with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                                                        with(equal(pageSize)),
                                                        with(equal(startIndex)));
            will(returnValue(mockResponse1));
            allowing(mockResponse1).getNextRecord();will(returnValue(3));
            allowing(mockResponse1).getRecordsMatched();will(returnValue(100));
            allowing(mockResponse1).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse1).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec1", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", "")}, null),
                    new CSWRecord(serviceId, "rec2", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));

            //Still 3 records to find
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(pageSize)),
                    with(equal(startIndex + pageSize)));
            will(returnValue(mockResponse2));

            allowing(mockResponse2).getNextRecord();will(returnValue(5));
            allowing(mockResponse2).getRecordsMatched();will(returnValue(100));
            allowing(mockResponse2).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse2).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec3", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", ""), new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null),
                    new CSWRecord(serviceId, "rec4", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", ""), new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));

            //Still 1 more record to find
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(pageSize)),
                    with(equal(startIndex + pageSize * 2)));
            will(returnValue(mockResponse3));

            allowing(mockResponse3).getNextRecord();will(returnValue(7));
            allowing(mockResponse3).getRecordsMatched();will(returnValue(100));
            allowing(mockResponse3).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse3).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec5", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", ""), new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null),
                    new CSWRecord(serviceId, "rec6", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", ""), new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));
        }});

        FacetedSearchResponse response = localFilterService.getFilteredRecords(serviceId, facets, startIndex, maxRecords);

        Assert.assertNotNull(response);
        Assert.assertEquals(3, response.getRecords().size());
        Assert.assertEquals(6, response.getNextIndex());

        Assert.assertEquals("rec3", response.getRecords().get(0).getFileIdentifier());
        Assert.assertEquals("rec4", response.getRecords().get(1).getFileIdentifier());
        Assert.assertEquals("rec5", response.getRecords().get(2).getFileIdentifier());
    }

    /**
     * Ensures our local filter filters correctly abort searching if the remote source indicates no more records
     * @throws Exception
     */
    @Test
    public void testLocalFilterRunoff() throws Exception {
        final String serviceId = "service-id";
        final FilterBoundingBox bbox = new FilterBoundingBox("WGS:84", new double[] {-10.0,  -10.0}, new double[] {10.0,  10.0});
        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<FilterBoundingBox>(bbox, "bbox", Comparison.Equal),
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal),
                new SearchFacet<OnlineResourceType>(OnlineResourceType.WCS, "servicetype", Comparison.Equal));

        final int startIndex = 1;
        final int maxRecords = 2;
        final int pageSize = 2;

        localFilterService.setPageSize(pageSize);

        context.checking(new Expectations() {{
            //Request a full page initially
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                                                        with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                                                        with(equal(pageSize)),
                                                        with(equal(startIndex)));
            will(returnValue(mockResponse1));
            allowing(mockResponse1).getNextRecord();will(returnValue(3));
            allowing(mockResponse1).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse1).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse1).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec1", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WCS", "wcs", "")}, null),
                    new CSWRecord(serviceId, "rec2", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));

            //Still 1 record to find
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(pageSize)),
                    with(equal(startIndex + pageSize)));
            will(returnValue(mockResponse2));

            allowing(mockResponse2).getNextRecord();will(returnValue(0));
            allowing(mockResponse2).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse2).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse2).getRecords();will(returnValue(Arrays.asList(
                    new CSWRecord(serviceId, "rec3", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null),
                    new CSWRecord(serviceId, "rec4", null, null, new AbstractCSWOnlineResource[] {new CSWOnlineResourceImpl(new URL("http://example.com"), "OGC:WMS", "wms", "")}, null))));
        }});

        FacetedSearchResponse response = localFilterService.getFilteredRecords(serviceId, facets, startIndex, maxRecords);

        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.getRecords().size());
        Assert.assertEquals(0, response.getNextIndex());

        Assert.assertEquals("rec1", response.getRecords().get(0).getFileIdentifier());
    }


    /**
     * Ensures we handle service exceptions gracefully
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testServiceException() throws Exception {
        final String serviceId = "service-id";
        final FilterBoundingBox bbox = new FilterBoundingBox("WGS:84", new double[] {-10.0,  -10.0}, new double[] {10.0,  10.0});
        final List<SearchFacet<? extends Object>> facets = Arrays.asList(
                new SearchFacet<FilterBoundingBox>(bbox, "bbox", Comparison.Equal),
                new SearchFacet<String>("kw1", "keyword", Comparison.Equal),
                new SearchFacet<String>("kw2", "keyword", Comparison.Equal),
                new SearchFacet<DateTime>(new DateTime(0l), "datefrom", Comparison.GreaterThan),
                new SearchFacet<DateTime>(new DateTime(1000l), "dateto", Comparison.LessThan));

        final int startIndex = 1;
        final int maxRecords = 3;
        final int pageSize = 2;

        localFilterService.setPageSize(pageSize);

        context.checking(new Expectations() {{
            //Request a full page initially
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                                                        with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                                                        with(equal(pageSize)),
                                                        with(equal(startIndex)));
            will(returnValue(mockResponse1));

            allowing(mockResponse1).getNextRecord();will(returnValue(3));
            allowing(mockResponse1).getRecordsMatched();will(returnValue(4));
            allowing(mockResponse1).getRecordsReturned();will(returnValue(2));
            allowing(mockResponse1).getRecords();will(returnValue(Arrays.asList(new CSWRecord("rec1"), new CSWRecord("rec2"))));

            //Second request throws an exception
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)),
                    with(new CSWGetDataRecordsFilterMatcher(bbox,new String[] {"kw1", "kw2"}, null, null, KeywordMatchType.All, null, null, null, null)),
                    with(equal(1)),
                    with(equal(startIndex + pageSize)));
            will(throwException(new PortalServiceException("Test exception")));
        }});

        localFilterService.getFilteredRecords(serviceId, facets, startIndex, maxRecords);
    }
}

package org.auscope.portal.server.web.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.server.web.service.LocalCSWFilterService;
import org.auscope.portal.server.web.service.csw.FacetedSearchResponse;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for CSWSearchController
 * @author Josh Vote (CSIRO)
 *
 */
public class TestCSWSearchController extends PortalTestClass {
    private CSWCacheService mockCSWService = context.mock(CSWCacheService.class);
    private LocalCSWFilterService mockFilterService = context.mock(LocalCSWFilterService.class);
    private WMSService mockWmsService = context.mock(WMSService.class);

    private CSWSearchController controller;

    /**
     * Setup.
     */
    @Before
    public void setUp() {
        context.checking(new Expectations() {{

        }});

        controller = new CSWSearchController(new ViewCSWRecordFactory(), new ViewKnownLayerFactory(), mockFilterService, mockCSWService, mockWmsService);
    }

    /**
     * Tests rewriting a record with a bad WMS will result the rewrite getting skipped and not crashing the entire search.
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testBadWMSGetCap() throws Exception {
        final Integer start = 1;
        final Integer limit = 2;
        final String serviceId = "service-id-value";
        final String newName = "new-layer-name";
        final String[] rawFields = new String[] {};
        final String[] rawValues = new String[] {};
        final String[] rawTypes = new String[] {};
        final String[] rawComparisons = new String[] {};

        CSWRecord rec1 = new CSWRecord("aaa");
        CSWRecord rec2 = new CSWRecord("bbb");

        rec1.setOnlineResources(new CSWOnlineResourceImpl[] {new CSWOnlineResourceImpl(new URL("http://example.com/wms1"), "OGC:WMS", "Link to Web Map Service", "description"),
                                                             new CSWOnlineResourceImpl(new URL("http://example.com/wcs1"), "OGC:WCS", "Link to Web Coverage Service", "description")});
        rec2.setOnlineResources(new CSWOnlineResourceImpl[] {new CSWOnlineResourceImpl(new URL("http://example.com/wms2"), "OGC:WMS", "Link to Web Map Service", "description"),
                                                             new CSWOnlineResourceImpl(new URL("http://example.com/wcs2"), "OGC:WCS", "Link to Web Coverage Service", "description")});

        final FacetedSearchResponse response = new FacetedSearchResponse();
        response.setNextIndex(-1);
        response.setStartIndex(start);
        response.setRecords(Arrays.asList(rec1, rec2));

        final GetCapabilitiesRecord mockGetCap = context.mock(GetCapabilitiesRecord.class);
        final GetCapabilitiesWMSLayerRecord mockGetCapLayer = context.mock(GetCapabilitiesWMSLayerRecord.class);

        context.checking(new Expectations() {{
            oneOf(mockFilterService).getFilteredRecords(with(equal(serviceId)), with(any(List.class)), with(equal(start)), with(equal(limit)));
            will(returnValue(response));

            oneOf(mockWmsService).getWmsCapabilities(with(equal("http://example.com/wms1")), with(any(String.class)));
            will(throwException(new PortalServiceException("pretend error")));

            oneOf(mockWmsService).getWmsCapabilities(with(equal("http://example.com/wms2")), with(any(String.class)));
            will(returnValue(mockGetCap));

            allowing(mockGetCap).getLayers();
            will(returnValue(new ArrayList<GetCapabilitiesWMSLayerRecord>(Arrays.asList(mockGetCapLayer))));

            allowing(mockGetCapLayer).getName();
            will(returnValue("new-layer-name"));
        }});

        ModelAndView mav = controller.facetedCSWSearch(start, limit, serviceId, rawFields, rawValues, rawTypes, rawComparisons);

        Assert.assertTrue((Boolean) mav.getModelMap().get("success"));
        Assert.assertNotNull(mav.getModelMap().get("data"));

        ModelMap data = (ModelMap) mav.getModelMap().get("data");
        Assert.assertEquals(1, data.get("startIndex"));
        Assert.assertEquals(-1, data.get("nextIndex"));

        List<ModelMap> recs = (List<ModelMap>) data.get("records");

        Assert.assertEquals(2, recs.size());

        List<Map<String, Object>> rec1Resources = (List<Map<String, Object>>) recs.get(0).get("onlineResources");
        List<Map<String, Object>> rec2Resources = (List<Map<String, Object>>) recs.get(1).get("onlineResources");
        Assert.assertNotNull(rec1Resources);
        Assert.assertEquals(2, rec1Resources.size());
        Assert.assertNotNull(rec2Resources);
        Assert.assertEquals(2, rec2Resources.size());

        Assert.assertEquals("Link to Web Map Service", rec1Resources.get(0).get("name"));
        Assert.assertEquals("Link to Web Coverage Service", rec1Resources.get(1).get("name"));

        Assert.assertEquals(newName, rec2Resources.get(0).get("name"));
        Assert.assertEquals(newName, rec2Resources.get(1).get("name"));
    }

}

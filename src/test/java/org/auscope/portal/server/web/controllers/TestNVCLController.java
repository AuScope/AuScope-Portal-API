package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.csw.CSWRecordsFilterVisitor;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.test.ByteBufferedServletOutputStream;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.MosaicResponse;
import org.auscope.portal.server.domain.nvcldataservice.TSGDownloadResponse;
import org.auscope.portal.server.domain.nvcldataservice.TSGStatusResponse;
import org.auscope.portal.server.domain.nvcldataservice.ImageTrayDepthResponse;
import org.auscope.portal.server.web.service.BoreholeService;
import org.auscope.portal.server.web.service.NVCL2_0_DataService;
import org.auscope.portal.server.web.service.NVCLDataService;
import org.auscope.portal.server.web.service.SF0BoreholeService;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * The Class TestNVCLController.
 *
 * @version: $Id$
 */
@SuppressWarnings("rawtypes")
public class TestNVCLController extends PortalTestClass {

    /** The mock http response. */
    private HttpServletResponse mockHttpResponse;

    /** The mock csw service. */
    private CSWCacheService mockCSWService;

    /** The mock borehole service. */
    private BoreholeService mockBoreholeService;

    /** The mock dataservice */
    private NVCLDataService mockDataService;

    /** Mock data service */
    private NVCL2_0_DataService mock2_0_DataService;

    /** The nvcl controller. */
    private NVCLController nvclController;

    private SF0BoreholeService mockSF0Service;

    /**
     * Setup.
     */
    @Before
    public void setUp() {
        this.mockHttpResponse = context.mock(HttpServletResponse.class);
        this.mockBoreholeService = context.mock(BoreholeService.class);
        this.mockCSWService = context.mock(CSWCacheService.class);
        this.mockDataService = context.mock(NVCLDataService.class);
        this.mock2_0_DataService = context.mock(NVCL2_0_DataService.class);
        this.mockSF0Service = context.mock(SF0BoreholeService.class);
        this.nvclController = new NVCLController(this.mockBoreholeService, this.mockCSWService, this.mockDataService,
                this.mock2_0_DataService, this.mockSF0Service);
    }

    /**
     * Tests to ensure that a non hylogger request calls the correct functions.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testNonHyloggerFilter() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDateStart = "1986-10-09";
        final String filterDateEnd = "1986-10-10";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1, 2}, new double[] {3, 4});
        final String nvclWfsResponse = "wfsResponse";
        final String outputFormat = "text/csv";
        final boolean onlyHylogger = false;
        final HttpRequestBase mockHttpMethodBase = context.mock(HttpRequestBase.class);
        final URI httpMethodURI = new URI("http://example.com");

        context.checking(new Expectations() {
            {
                oneOf(mockBoreholeService).getAllBoreholes(serviceUrl, nameFilter, custodianFilter,
                        filterDateStart, filterDateEnd, maxFeatures, bbox, null, outputFormat,"");
                will(returnValue(new WFSResponse(nvclWfsResponse, mockHttpMethodBase)));

                allowing(mockHttpMethodBase).getURI();
                will(returnValue(httpMethodURI));

            }
        });

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter,
                filterDateStart, filterDateEnd, maxFeatures, bbox, onlyHylogger, outputFormat, false,"");
        Assert.assertTrue((Boolean) response.getModel().get("success"));

        Map data = (Map) response.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(nvclWfsResponse, data.get("gml"));
    }

    /**
     * Tests that hylogger filter uses the correct functions.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testHyloggerFilter() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDateStart = "1986-10-09";
        final String filterDateEnd = "1986-10-10";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1, 2}, new double[] {3, 4});
        final String nvclWfsResponse = "wfsResponse";
        final String outputFormat = "text/csv";
        final List<String> restrictedIds = Arrays.asList("ID1", "ID2");
        final boolean onlyHylogger = true;
        final HttpRequestBase mockHttpMethodBase = context.mock(HttpRequestBase.class);
        final URI httpMethodURI = new URI("http://example.com");

        context.checking(new Expectations() {
            {
                oneOf(mockBoreholeService).discoverHyloggerBoreholeIDs(with(equal(mockCSWService)),
                        with(any(CSWRecordsFilterVisitor.class)));
                will(returnValue(restrictedIds));

                oneOf(mockBoreholeService).getAllBoreholes(serviceUrl, nameFilter, custodianFilter,
                        filterDateStart, filterDateEnd, maxFeatures, bbox, restrictedIds, outputFormat,"");
                will(returnValue(new WFSResponse(nvclWfsResponse, mockHttpMethodBase)));

                allowing(mockHttpMethodBase).getURI();
                will(returnValue(httpMethodURI));
            }
        });

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter,
                filterDateStart, filterDateEnd, maxFeatures, bbox, onlyHylogger, outputFormat, false,"");
        Assert.assertTrue((Boolean) response.getModel().get("success"));

        Map data = (Map) response.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(nvclWfsResponse, data.get("gml"));
    }

    /**
     * Tests that hylogger filter uses the correct functions when the underlying hylogger lookup fails.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testHyloggerFilterError() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDateStart = "1986-10-09";
        final String filterDateEnd = "1986-10-10";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1, 2}, new double[] {3, 4});
        final boolean onlyHylogger = true;
        final HttpRequestBase mockHttpMethodBase = context.mock(HttpRequestBase.class);
        final URI httpMethodURI = new URI("http://example.com");
        final String outputFormat = "text/csv";

        context.checking(new Expectations() {
            {
                oneOf(mockBoreholeService).discoverHyloggerBoreholeIDs(with(equal(mockCSWService)),
                        with(any(CSWRecordsFilterVisitor.class)));
                will(throwException(new ConnectException()));

                allowing(mockHttpMethodBase).getURI();
                will(returnValue(httpMethodURI));
            }
        });

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter,
                filterDateStart, filterDateEnd, maxFeatures, bbox, onlyHylogger, outputFormat, false,"");
        Assert.assertFalse((Boolean) response.getModel().get("success"));
    }

    /**
     * Tests that hylogger filter uses the correct functions when the underlying hylogger lookup returns no results.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testHyloggerFilterNoDatasets() throws Exception {
        final String serviceUrl = "http://fake.com/wfs";
        final String nameFilter = "filterBob";
        final String custodianFilter = "filterCustodian";
        final String filterDateStart = "1986-10-09";
        final String filterDateEnd = "1986-10-10";
        final int maxFeatures = 10;
        final FilterBoundingBox bbox = new FilterBoundingBox("EPSG:4326", new double[] {1., 2.}, new double[] {3., 4.});
        final boolean onlyHylogger = true;
        final HttpRequestBase mockHttpMethodBase = context.mock(HttpRequestBase.class);
        final URI httpMethodURI = new URI("http://example.com");
        final String outputFormat = "text/csv";

        context.checking(new Expectations() {
            {
                oneOf(mockBoreholeService).discoverHyloggerBoreholeIDs(with(equal(mockCSWService)),
                        with(any(CSWRecordsFilterVisitor.class)));
                will(returnValue(new ArrayList<String>()));

                allowing(mockHttpMethodBase).getURI();
                will(returnValue(httpMethodURI));
            }
        });

        ModelAndView response = this.nvclController.doBoreholeFilter(serviceUrl, nameFilter, custodianFilter,
                filterDateStart, filterDateEnd, maxFeatures, bbox, onlyHylogger, outputFormat, false,"");
        Assert.assertFalse((Boolean) response.getModel().get("success"));
    }

    /**
     * Tests getting dataset collection succeeds if underlying service succeeds.
     *
     * @throws Exception
     */
    @Test
    public void testGetDatasetCollection() throws Exception {
        final String serviceUrl = "http://example/url";
        final String holeIdentifier = "unique-id";
        final List<GetDatasetCollectionResponse> responseObjs = Arrays.asList(context
                .mock(GetDatasetCollectionResponse.class));

        context.checking(new Expectations() {
            {
                oneOf(mockDataService).getDatasetCollection(serviceUrl, holeIdentifier);
                will(returnValue(responseObjs));
            }
        });

        ModelAndView response = this.nvclController.getNVCLDatasets(serviceUrl, holeIdentifier);
        Assert.assertNotNull(response);
        Assert.assertTrue((Boolean) response.getModel().get("success"));
        Assert.assertSame(responseObjs, response.getModel().get("data"));
    }

    /**
     * Tests getting dataset collection fails if underlying service fails.
     *
     * @throws Exception
     */
    @Test
    public void testGetDatasetCollectionError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String holeIdentifier = "unique-id";

        context.checking(new Expectations() {
            {
                oneOf(mockDataService).getDatasetCollection(serviceUrl, holeIdentifier);
                will(throwException(new ConnectException()));
            }
        });

        ModelAndView response = this.nvclController.getNVCLDatasets(serviceUrl, holeIdentifier);
        Assert.assertNotNull(response);
        Assert.assertFalse((Boolean) response.getModel().get("success"));
    }

    /**
     * Tests getting dataset collection succeeds if underlying service succeeds.
     *
     * @throws Exception
     */
    @Test
    public void testGetLogCollection() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetId = "unique-id";
        final Boolean mosaicService = true;
        final List<GetLogCollectionResponse> responseObjs = Arrays.asList(context.mock(GetLogCollectionResponse.class));

        context.checking(new Expectations() {
            {
                oneOf(mockDataService).getLogCollection(serviceUrl, datasetId, mosaicService);
                will(returnValue(responseObjs));
            }
        });

        ModelAndView response = this.nvclController.getNVCLLogs(serviceUrl, datasetId, mosaicService);
        Assert.assertNotNull(response);
        Assert.assertTrue((Boolean) response.getModel().get("success"));
        Assert.assertSame(responseObjs, response.getModel().get("data"));
    }

    /**
     * Tests getting dataset collection fails if underlying service fails.
     *
     * @throws Exception
     */
    @Test
    public void testGetLogCollectionError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetIdentifier = "unique-id";
        final Boolean mosaicService = false;

        context.checking(new Expectations() {
            {
                oneOf(mockDataService).getLogCollection(serviceUrl, datasetIdentifier, mosaicService);
                will(throwException(new ConnectException()));
            }
        });

        ModelAndView response = this.nvclController.getNVCLLogs(serviceUrl, datasetIdentifier, mosaicService);
        Assert.assertNotNull(response);
        Assert.assertFalse((Boolean) response.getModel().get("success"));
    }

    /**
     * Tests getting mosaic.
     *
     * @throws Exception
     */
    @Test
    public void testGetMosaic() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "unique-id";
        final Integer width = 1;
        final Integer start = 2;
        final Integer end = 3;
        final byte[] data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        final String contentType = "image/jpeg";
        final MosaicResponse mockMosaicResponse = context.mock(MosaicResponse.class);

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(data.length);

        context.checking(new Expectations() {
            {
                oneOf(mockDataService).getMosaic(serviceUrl, logId, width, start, end);
                will(returnValue(mockMosaicResponse));

                oneOf(mockHttpResponse).setContentType(contentType);
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(outputStream));

                allowing(mockMosaicResponse).getContentType();
                will(returnValue(contentType));
                allowing(mockMosaicResponse).getResponse();
                will(returnValue(inputStream));
            }
        });

        this.nvclController.getNVCLMosaic(serviceUrl, logId, width, start, end, mockHttpResponse);
        Assert.assertArrayEquals(data, outputStream.toByteArray());
    }
    


    /**
     * Tests getting mosaic fails gracefully when the service fails.
     *
     * @throws Exception
     */
    @Test
    public void testGetMosaicConnectException() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "unique-id";
        final Integer width = 1;
        final Integer start = 2;
        final Integer end = 3;

        context.checking(new Expectations() {
            {
                oneOf(mockDataService).getMosaic(serviceUrl, logId, width, start, end);
                will(throwException(new ConnectException()));

                oneOf(mockHttpResponse).sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        });

        this.nvclController.getNVCLMosaic(serviceUrl, logId, width, start, end, mockHttpResponse);
    }
    
    
    /**
     * Tests getting tray depths
     *
     * @throws Exception    
     */
    @Test
    public void testGetImageTrayDepths() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "log-id";
        final List<ImageTrayDepthResponse> responseObjs = Arrays.asList(context.mock(ImageTrayDepthResponse.class));

        context.checking(new Expectations() {
            {
                oneOf(mock2_0_DataService).getImageTrayDepths(serviceUrl, logId);
                will(returnValue(responseObjs));
            }
        });
        ModelAndView response = this.nvclController.getNVCLImageTrayDepth(serviceUrl, logId);
        Assert.assertNotNull(response);
        Assert.assertTrue((Boolean) response.getModel().get("success"));
        Assert.assertSame(responseObjs, response.getModel().get("data"));
    }    
    
    
    
    /**
     * Tests tray depths fails if underlying service fails.
     *
     * @throws Exception
     */
    @Test
    public void testGetImageTrayDepthsError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "log-id";

        context.checking(new Expectations() {
            {
                oneOf(mock2_0_DataService).getImageTrayDepths(serviceUrl, logId);
                will(throwException(new ConnectException()));
            }
        });

        ModelAndView response = this.nvclController.getNVCLImageTrayDepth(serviceUrl, logId);
        Assert.assertNotNull(response);
        Assert.assertFalse((Boolean) response.getModel().get("success"));
    }


    /**
     * Tests a TSG download calls the underlying service correctly
     *
     * @throws Exception
     */
    @Test
    public void testTSGDownload() throws Exception {
        final String serviceUrl = "http://example/url";
        final String email = "email@com";
        final String datasetId = "did";
        final String matchString = null;
        final Boolean lineScan = true;
        final Boolean spectra = false;
        final Boolean profilometer = null;
        final Boolean trayPics = true;
        final Boolean mosaicPics = false;
        final Boolean mapPics = true;
        final byte[] data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        final String contentType = "text/html";
        final TSGDownloadResponse mockResponse = context.mock(TSGDownloadResponse.class);

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(data.length);

        context.checking(new Expectations() {
            {
                oneOf(mockDataService).getTSGDownload(serviceUrl, email, datasetId, matchString, lineScan, spectra,
                        profilometer, trayPics, mosaicPics, mapPics);
                will(returnValue(mockResponse));

                oneOf(mockHttpResponse).setContentType(contentType);
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(outputStream));

                allowing(mockResponse).getContentType();
                will(returnValue(contentType));
                allowing(mockResponse).getResponse();
                will(returnValue(inputStream));
            }
        });

        this.nvclController.getNVCLTSGDownload(serviceUrl, email, datasetId, matchString, lineScan, spectra,
                profilometer, trayPics, mosaicPics, mapPics, mockHttpResponse);
        Assert.assertArrayEquals(data, outputStream.toByteArray());
    }

    /**
     * Tests a workaround for spring framework combining multiple parameters (of the same name) into a CSV
     *
     * @throws Exception
     */
    @Test
    public void testTSGDownload_MultiEmail() throws Exception {
        final String serviceUrl = "http://example/url";
        final String emailString = "email@com,email@com";
        final String email = "email@com";
        final String datasetId = "did";
        final String matchString = null;
        final Boolean lineScan = true;
        final Boolean spectra = false;
        final Boolean profilometer = null;
        final Boolean trayPics = true;
        final Boolean mosaicPics = false;
        final Boolean mapPics = true;
        final byte[] data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        final String contentType = "text/html";
        final TSGDownloadResponse mockResponse = context.mock(TSGDownloadResponse.class);

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(data.length);

        context.checking(new Expectations() {
            {
                oneOf(mockDataService).getTSGDownload(serviceUrl, email, datasetId, matchString, lineScan, spectra,
                        profilometer, trayPics, mosaicPics, mapPics);
                will(returnValue(mockResponse));

                oneOf(mockHttpResponse).setContentType(contentType);
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(outputStream));

                allowing(mockResponse).getContentType();
                will(returnValue(contentType));
                allowing(mockResponse).getResponse();
                will(returnValue(inputStream));
            }
        });

        this.nvclController.getNVCLTSGDownload(serviceUrl, emailString, datasetId, matchString, lineScan, spectra,
                profilometer, trayPics, mosaicPics, mapPics, mockHttpResponse);
        Assert.assertArrayEquals(data, outputStream.toByteArray());
    }

    /**
     * Tests a TSG download status calls the underlying service correctly
     *
     * @throws Exception
     */
    @Test
    public void testTSGDownloadStatus() throws Exception {
        final String serviceUrl = "http://example/url";
        final String email = "unique@email";
        final byte[] data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        final String contentType = "text/html";
        final TSGStatusResponse mockResponse = context.mock(TSGStatusResponse.class);

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        final ByteBufferedServletOutputStream outputStream = new ByteBufferedServletOutputStream(data.length);

        context.checking(new Expectations() {
            {
                oneOf(mockDataService).checkTSGStatus(serviceUrl, email);
                will(returnValue(mockResponse));

                oneOf(mockHttpResponse).setContentType(contentType);
                oneOf(mockHttpResponse).getOutputStream();
                will(returnValue(outputStream));

                allowing(mockResponse).getContentType();
                will(returnValue(contentType));
                allowing(mockResponse).getResponse();
                will(returnValue(inputStream));
            }
        });

        this.nvclController.getNVCLTSGDownloadStatus(serviceUrl, email, mockHttpResponse);
        Assert.assertArrayEquals(data, outputStream.toByteArray());
    }


}

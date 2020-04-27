package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpClientResponse;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.auscope.portal.server.domain.nvcldataservice.GetDatasetCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.GetLogCollectionResponse;
import org.auscope.portal.server.domain.nvcldataservice.MosaicResponse;
import org.auscope.portal.server.domain.nvcldataservice.TSGDownloadResponse;
import org.auscope.portal.server.domain.nvcldataservice.TSGStatusResponse;

import org.auscope.portal.server.web.NVCLDataServiceMethodMaker;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for NVCLDataService
 *
 * @author Josh Vote
 *
 */
public class TestNVCLDataService extends PortalTestClass {

    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private NVCLDataServiceMethodMaker mockMethodMaker = context.mock(NVCLDataServiceMethodMaker.class);
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);
    private WFSGetFeatureMethodMaker mockWFSMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);
    private NVCLDataService dataService = new NVCLDataService(mockServiceCaller, mockMethodMaker, mockWFSMethodMaker);

    /**
     * Tests parsing of a getDatasetCollectionResponse
     *
     * @throws Exception
     */
    @Test
    public void testGetDatasetCollection() throws Exception {
        final String serviceUrl = "http://example/url";
        final String holeIdentifier = "holeIdentifier";
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/nvcl/NVCL_GetDatasetCollectionResponse.xml");
        final ByteArrayInputStream responseStream = new ByteArrayInputStream(responseString.getBytes());

        context.checking(new Expectations() {
            {
                oneOf(mockMethodMaker).getDatasetCollectionMethod(serviceUrl, holeIdentifier);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(responseStream, null)));
            }
        });

        List<GetDatasetCollectionResponse> response = dataService.getDatasetCollection(serviceUrl, holeIdentifier);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.size());
        Assert.assertEquals("6dd70215-fe38-457c-be42-3b165fd98c7", response.get(0).getDatasetId());
        Assert.assertEquals("WTB5", response.get(0).getDatasetName());
        Assert.assertEquals("http://example1/geoserverBH/", response.get(0).getOmUrl());

        Assert.assertEquals("7de74515-ae48-4aac-cd43-3bb45dd78cc", response.get(1).getDatasetId());
        Assert.assertEquals("Name#2", response.get(1).getDatasetName());
        Assert.assertEquals("http://example2/geoserverBH/", response.get(1).getOmUrl());
    }

    /**
     * Tests parsing of a getDatasetCollectionResponse fails when we fail to connect to the service
     *
     * @throws Exception
     */
    @Test(expected = ConnectException.class)
    public void testGetDatasetCollectionConnectError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String holeIdentifier = "holeIdentifier";

        context.checking(new Expectations() {
            {

                oneOf(mockMethodMaker).getDatasetCollectionMethod(serviceUrl, holeIdentifier);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(throwException(new ConnectException()));
            }
        });

        dataService.getDatasetCollection(serviceUrl, holeIdentifier);
    }

    /**
     * Tests parsing of a getLogCollectionResponse
     *
     * @throws Exception
     */
    @Test
    public void testGetLogCollection() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetId = "datasetId";
        final boolean forMosaicService = true;
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/nvcl/NVCL_GetLogCollectionResponse.xml");
        final ByteArrayInputStream responseStream = new ByteArrayInputStream(responseString.getBytes());

        context.checking(new Expectations() {
            {

                oneOf(mockMethodMaker).getLogCollectionMethod(serviceUrl, datasetId, forMosaicService);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(responseStream, null)));
            }
        });

        List<GetLogCollectionResponse> response = dataService.getLogCollection(serviceUrl, datasetId, forMosaicService);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.size());
        Assert.assertEquals("logid-1", response.get(0).getLogId());
        Assert.assertEquals("logname-1", response.get(0).getLogName());
        Assert.assertEquals(45, response.get(0).getSampleCount());
        Assert.assertEquals("logid-2", response.get(1).getLogId());
        Assert.assertEquals("", response.get(1).getLogName());
        Assert.assertEquals(0, response.get(1).getSampleCount());
    }

    /**
     * Unit test to ensure the NVCLDataService class correctly compensates for the service return 'logName' when mosaicsvc is omitted or false and 'LogName'
     * otherwise
     *
     * @throws Exception
     */
    @Test
    public void testGetLogCollection_LogNameCase() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetId = "datasetId";
        final String responseString = ResourceUtil
                .loadResourceAsString("org/auscope/portal/nvcl/NVCL_GetLogCollectionResponse.xml");
        final ByteArrayInputStream is1 = new ByteArrayInputStream(responseString.getBytes());
        final ByteArrayInputStream is2 = new ByteArrayInputStream(responseString.getBytes());

        context.checking(new Expectations() {
            {

                allowing(mockMethodMaker).getLogCollectionMethod(with(any(String.class)), with(any(String.class)),
                        with(any(Boolean.class)));
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(is1, null)));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(is2, null)));
            }
        });

        List<GetLogCollectionResponse> response = dataService.getLogCollection(serviceUrl, datasetId, true);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.size());
        Assert.assertEquals("logname-1", response.get(0).getLogName());
        Assert.assertEquals("", response.get(1).getLogName());

        response = dataService.getLogCollection(serviceUrl, datasetId, false);
        Assert.assertNotNull(response);
        Assert.assertEquals(2, response.size());
        Assert.assertEquals("", response.get(0).getLogName());
        Assert.assertEquals("logname-2", response.get(1).getLogName());
    }

    /**
     * Tests parsing of a getDatasetCollectionResponse fails when we fail to connect to the service
     *
     * @throws Exception
     */
    @Test(expected = ConnectException.class)
    public void testGetLogCollectionConnectError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String datasetIdentifier = "datasetIdentifier";
        final boolean forMosaicService = false;

        context.checking(new Expectations() {
            {

                oneOf(mockMethodMaker).getLogCollectionMethod(serviceUrl, datasetIdentifier, forMosaicService);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(throwException(new ConnectException()));
            }
        });

        dataService.getLogCollection(serviceUrl, datasetIdentifier, forMosaicService);
    }


    /**
     * Tests parsing of a GetMosaicResponse fails when we fail to connect to the service
     *
     * @throws Exception
     */
    @Test(expected = ConnectException.class)
    public void testGetMosaicConnectError() throws Exception {
        final String serviceUrl = "http://example/url";
        final String logId = "logId";
        final Integer width = 10;
        final Integer startSampleNo = 11;
        final Integer endSampleNo = 12;

        context.checking(new Expectations() {
            {

                oneOf(mockMethodMaker).getMosaicMethod(serviceUrl, logId, width, startSampleNo, endSampleNo);
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsHttpResponse(mockMethod);
                will(throwException(new ConnectException()));

            }
        });

        dataService.getMosaic(serviceUrl, logId, width, startSampleNo, endSampleNo);
    }

}

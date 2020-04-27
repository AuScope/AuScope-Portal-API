package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpClientInputStream;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.auscope.portal.server.domain.nvcldataservice.AlgorithmOutputClassification;
import org.auscope.portal.server.domain.nvcldataservice.AlgorithmOutputResponse;
import org.auscope.portal.server.domain.nvcldataservice.AnalyticalJobResults;
import org.auscope.portal.server.domain.nvcldataservice.AnalyticalJobStatus;
import org.auscope.portal.server.domain.nvcldataservice.BinnedCSVResponse;
import org.auscope.portal.server.web.NVCL2_0_DataServiceMethodMaker;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

/**
 * Unit tests for NVCLDataService
 *
 * @author Josh Vote
 *
 */
public class TestNVCL2_0_DataService extends PortalTestClass {

    private static final String ANALYTICAL_SERVICES_URL = "http://foo.bar/service";

    private HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    private NVCL2_0_DataServiceMethodMaker mockMethodMaker = context.mock(NVCL2_0_DataServiceMethodMaker.class);
    private HttpRequestBase mockMethod = context.mock(HttpRequestBase.class, "mockMethod");
    private NVCL2_0_DataService dataService;

    @Before
    public void setup() {

        context.checking(new Expectations() {{

        }});

        dataService = new NVCL2_0_DataService(mockServiceCaller, mockMethodMaker, ANALYTICAL_SERVICES_URL);
    }

    /**
     * Tests parsing of a JSON downloadscalars average meter values request
     *
     * @throws Exception
     */
    @Test
    public void testGetNVCL2_0_JSONDataBinned_Av() throws Exception {
        final String serviceUrl = "http://example/url/wfs";
        final String[] logIds = new String[] {"id1", "id2", "id3", "id4"};

        final String responseStr = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/downloadscalar-average.json");
        
        context.checking(new Expectations() {
            {
                atLeast(1).of(mockMethodMaker).getDownloadJSONMethod(with(any(String.class)), with(any(String.class)));will(returnValue(mockMethod));
                atLeast(1).of(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(new String(responseStr)));
            }
        });

        String response = dataService.getNVCL2_0_JSONDownsampledData(serviceUrl, logIds);
        Assert.assertNotNull(response);
        JSONArray dataArr = JSONArray.fromObject(response);
        JSONObject firstObj = dataArr.getJSONObject(0);
        Assert.assertEquals(true, firstObj.containsKey("numericValues"));
        Assert.assertEquals("id1", firstObj.get("logId"));
        JSONArray firstArr = firstObj.getJSONArray("numericValues");
        JSONObject firstValObj = firstArr.getJSONObject(0);
        Assert.assertEquals(22.5, firstValObj.getDouble("roundedDepth"), 0.01);
        Assert.assertEquals(0.49833333, firstValObj.getDouble("averageValue"), 0.000000001);
        JSONObject secondValObj = firstArr.getJSONObject(1);
        Assert.assertEquals(23.5, secondValObj.getDouble("roundedDepth"), 0.01);
        Assert.assertEquals(0.5093243, secondValObj.getDouble("averageValue"), 0.00000001);
    }

    /**
     * Tests parsing of a JSON downloadscalars average meter values request
     *
     * @throws Exception
     */
    @Test
    public void testGetNVCL2_0_JSONDataBinned_Cnt() throws Exception {
        final String serviceUrl = "http://example/url/wfs";
        final String[] logIds = new String[] {"id1", "id2", "id3", "id4"};

        final String responseStr = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/downloadscalar-count.json");
        
        context.checking(new Expectations() {
            {
                atLeast(1).of(mockMethodMaker).getDownloadJSONMethod(with(any(String.class)), with(any(String.class)));will(returnValue(mockMethod));
                atLeast(1).of(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(new String(responseStr)));
            }
        });

        String response = dataService.getNVCL2_0_JSONDownsampledData(serviceUrl, logIds);
        Assert.assertNotNull(response);
        JSONArray dataArr = JSONArray.fromObject(response);
        JSONObject firstObj = dataArr.getJSONObject(0);
        Assert.assertEquals(true, firstObj.containsKey("stringValues"));
        Assert.assertEquals("id1", firstObj.get("logId"));
        JSONArray firstArr = firstObj.getJSONArray("stringValues");
        JSONObject firstValObj = firstArr.getJSONObject(0);
        Assert.assertEquals(170.5, firstValObj.getDouble("roundedDepth"), 0.01);
        Assert.assertEquals(1, firstValObj.getInt("classCount"));
        Assert.assertEquals("Alunite-K", firstValObj.getString("classText"));
        Assert.assertEquals(4351080, firstValObj.getInt("colour"));
        JSONObject secondValObj = firstArr.getJSONObject(1);
        Assert.assertEquals(196.5, secondValObj.getDouble("roundedDepth"), 0.01);
        Assert.assertEquals(5, secondValObj.getInt("classCount"));
        Assert.assertEquals("Ankerite", secondValObj.getString("classText"));
        Assert.assertEquals(16744319, secondValObj.getInt("colour"));
    }


    /**
     * Tests parsing of an empty downloadscalars JSON request
     *
     * @throws Exception
     */
    @Test
    public void testGetNVCL2_0_JSONDataBinned_Empty() throws Exception {
        final String serviceUrl = "http://example/url/wfs";
        final String[] logIds = new String[] {"id1", "id2", "id3"};

        final String responseStr = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/downloadscalar-empty.json");

        context.checking(new Expectations() {
            {
                atLeast(1).of(mockMethodMaker).getDownloadJSONMethod(with(any(String.class)), with(any(String.class)));will(returnValue(mockMethod));
                atLeast(1).of(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(new String(responseStr)));
            }
        });

        String response = dataService.getNVCL2_0_JSONDownsampledData(serviceUrl, logIds);
        Assert.assertNotNull(response);
        JSONArray dataArr = JSONArray.fromObject(response);
        Assert.assertEquals(0,dataArr.size());
    }
    
    
    /**
     * Tests parsing of a download jobs scalars request into a BinnedCSVResponse
     *
     * @throws Exception
     */
    @Test
    public void testGetNVCL2_0_JobsScalarBinned() throws Exception {
        final String boreholeId = "BOREHOLE_1234";
        final String[] jobIds = new String[] {"job1","job2","job3"};
        final double binSizeMetres = 1.0;

        context.checking(new Expectations() {
            {
                atLeast(1).of(mockMethodMaker).getNVCLJobsScalarMethod(with(any(String.class)), with(any(String.class)), with(any(String.class)));
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(ClassLoader.getSystemResourceAsStream("org/auscope/portal/nvcl/downloadjobscalar.csv"), null)));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(ClassLoader.getSystemResourceAsStream("org/auscope/portal/nvcl/downloadjobscalar.csv"), null)));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(ClassLoader.getSystemResourceAsStream("org/auscope/portal/nvcl/downloadjobscalar.csv"), null)));
            }
        });

        BinnedCSVResponse response = dataService.getNVCL2_0_JobsScalarBinned(jobIds, boreholeId, binSizeMetres);
        Assert.assertNotNull(response);
        Assert.assertEquals(3, response.getBinnedValues().length);

        Assert.assertTrue(response.getBinnedValues()[0].isNumeric());
        Assert.assertTrue(response.getBinnedValues()[1].isNumeric());
        Assert.assertTrue(response.getBinnedValues()[2].isNumeric());

        Assert.assertEquals("job1", response.getBinnedValues()[0].getName());
        Assert.assertEquals("job2", response.getBinnedValues()[1].getName());
        Assert.assertEquals("job3", response.getBinnedValues()[2].getName());

        Assert.assertEquals(2, response.getBinnedValues()[0].getNumericValues().size());
        Assert.assertEquals(2, response.getBinnedValues()[0].getStartDepths().size());
        Assert.assertEquals(2, response.getBinnedValues()[1].getNumericValues().size());
        Assert.assertEquals(2, response.getBinnedValues()[1].getStartDepths().size());
        Assert.assertEquals(2, response.getBinnedValues()[2].getNumericValues().size());
        Assert.assertEquals(2, response.getBinnedValues()[2].getStartDepths().size());

        Assert.assertEquals(106.936996459961, (Double)response.getBinnedValues()[0].getStartDepths().get(0), 0.0000001);
        Assert.assertEquals(108.004341125488, (Double)response.getBinnedValues()[0].getStartDepths().get(1), 0.0000001);
        Assert.assertEquals(106.936996459961, (Double)response.getBinnedValues()[1].getStartDepths().get(0), 0.0000001);
        Assert.assertEquals(108.004341125488, (Double)response.getBinnedValues()[1].getStartDepths().get(1), 0.0000001);
        Assert.assertEquals(106.936996459961, (Double)response.getBinnedValues()[2].getStartDepths().get(0), 0.0000001);
        Assert.assertEquals(108.004341125488, (Double)response.getBinnedValues()[2].getStartDepths().get(1), 0.0000001);
        
        Assert.assertEquals(1.0, (Double)response.getBinnedValues()[0].getNumericValues().get(0), 0.001);
        Assert.assertEquals(1.7, (Double)response.getBinnedValues()[0].getNumericValues().get(1), 0.001);
        Assert.assertEquals(1.0, (Double)response.getBinnedValues()[1].getNumericValues().get(0), 0.001);
        Assert.assertEquals(1.7, (Double)response.getBinnedValues()[1].getNumericValues().get(1), 0.001);
        Assert.assertEquals(1.0, (Double)response.getBinnedValues()[2].getNumericValues().get(0), 0.001);
        Assert.assertEquals(1.7, (Double)response.getBinnedValues()[2].getNumericValues().get(1), 0.001);
    }
    
    
    /**
     * Tests parsing of a download job scalars request into a BinnedCSVResponse
     *
     * @throws Exception
     */
    @Test
    public void testGetNVCL2_0_JobsScalarBinned_EmptyCSV() throws Exception {
        final String boreholeId = "BOREHOLE_1234";
        final String[] jobIds = new String[] {"job1","job2","job3"};
        final double binSizeMetres = 1.0;

        context.checking(new Expectations() {
            {
                atLeast(1).of(mockMethodMaker).getNVCLJobsScalarMethod(with(any(String.class)), with(any(String.class)), with(any(String.class)));
                will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(ClassLoader.getSystemResourceAsStream("org/auscope/portal/nvcl/downloadjobscalar-empty.csv"), null)));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(ClassLoader.getSystemResourceAsStream("org/auscope/portal/nvcl/downloadjobscalar-empty.csv"), null)));
                oneOf(mockServiceCaller).getMethodResponseAsStream(mockMethod);
                will(returnValue(new HttpClientInputStream(ClassLoader.getSystemResourceAsStream("org/auscope/portal/nvcl/downloadjobscalar-empty.csv"), null)));
            }
        });

        BinnedCSVResponse response = dataService.getNVCL2_0_JobsScalarBinned(jobIds, boreholeId, binSizeMetres);
        Assert.assertNotNull(response);
        Assert.assertEquals(3, response.getBinnedValues().length);

        Assert.assertEquals("job1", response.getBinnedValues()[0].getName());
        Assert.assertEquals("job2", response.getBinnedValues()[1].getName());
        Assert.assertEquals("job3", response.getBinnedValues()[2].getName());

        Assert.assertEquals(0, response.getBinnedValues()[0].getNumericValues().size());
        Assert.assertEquals(0, response.getBinnedValues()[1].getNumericValues().size());
        Assert.assertEquals(0, response.getBinnedValues()[2].getNumericValues().size());
    }

    /**
     * Tests parsing an example getAlgorithms response
     * @throws Exception
     */
    @Test
    public void testGetAlgorithms() throws Exception {
        final String serviceUrl = "http://example/url/wfs";
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/NVCL_GetAlgorithmsResponse.xml");

        context.checking(new Expectations() {{
                oneOf(mockMethodMaker).getAlgorithms(serviceUrl);will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseString));
        }});

        List<AlgorithmOutputResponse> responses = dataService.getAlgorithms(serviceUrl);

        Assert.assertNotNull(responses);
        Assert.assertEquals(31, responses.size());
        Assert.assertEquals("The Spectral Assistant VNIR", responses.get(0).getAlgorithmName());
        Assert.assertEquals("VNIR Mineral", responses.get(0).getOutputName());
        Assert.assertEquals(1, responses.get(0).getAlgorithmId());
        Assert.assertEquals(5, responses.get(0).getVersions().size());
        Assert.assertEquals(703, responses.get(0).getVersions().get(0).getVersion());
        Assert.assertEquals(82, responses.get(0).getVersions().get(0).getAlgorithmOutputId());
        Assert.assertEquals(601, responses.get(0).getVersions().get(4).getVersion());
        Assert.assertEquals(26, responses.get(0).getVersions().get(4).getAlgorithmOutputId());


        Assert.assertEquals("The Spectral Assistant TIR", responses.get(30).getAlgorithmName());
        Assert.assertEquals("TIR Nil Stat", responses.get(30).getOutputName());
        Assert.assertEquals(3, responses.get(30).getAlgorithmId());
        Assert.assertEquals(4, responses.get(30).getVersions().size());
        Assert.assertEquals(703, responses.get(30).getVersions().get(0).getVersion());
        Assert.assertEquals(73, responses.get(30).getVersions().get(0).getAlgorithmOutputId());
        Assert.assertEquals(701, responses.get(30).getVersions().get(3).getVersion());
        Assert.assertEquals(61, responses.get(30).getVersions().get(3).getAlgorithmOutputId());
    }

    /**
     * Tests parsing an example getClassifications response
     * @throws Exception
     */
    @Test
    public void testGetClassifications() throws Exception {
        final String serviceUrl = "http://example/url/wfs";
        final int algorithmOutputId = 123;
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/NVCL_GetClassificationsResponse.xml");

        context.checking(new Expectations() {{
                oneOf(mockMethodMaker).getClassifications(serviceUrl, algorithmOutputId);will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseString));
        }});

        List<AlgorithmOutputClassification> responses = dataService.getClassifications(serviceUrl, algorithmOutputId);

        Assert.assertNotNull(responses);
        Assert.assertEquals(22, responses.size());
        Assert.assertEquals("NULL", responses.get(0).getClassText());
        Assert.assertEquals(8421504, responses.get(0).getColor());
        Assert.assertEquals(-1, responses.get(0).getIndex());
        Assert.assertEquals("MaskedOff", responses.get(21).getClassText());
        Assert.assertEquals(8947848, responses.get(21).getColor());
        Assert.assertEquals(20, responses.get(21).getIndex());
    }

    /**
     * Tests parsing multiple getClassifications responses
     * @throws Exception
     */
    @Test
    public void testGetManyClassifications() throws Exception {
        final String serviceUrl = "http://example/url/wfs";
        final int[] algorithmOutputIds = new int[] {123, 456};
        final String responseString1 = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/NVCL_GetClassificationsResponse.xml");
        final String responseString2 = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/NVCL_GetClassificationsResponse2.xml");

        context.checking(new Expectations() {{
                oneOf(mockMethodMaker).getClassifications(serviceUrl, algorithmOutputIds[0]);will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseString1));

                oneOf(mockMethodMaker).getClassifications(serviceUrl, algorithmOutputIds[1]);will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseString2));
        }});

        List<AlgorithmOutputClassification> responses = dataService.getClassifications(serviceUrl, algorithmOutputIds);

        Assert.assertNotNull(responses);
        Assert.assertEquals(23, responses.size());

        boolean foundNull = false;
        boolean foundDistinct = false;

        for (AlgorithmOutputClassification c : responses) {
            switch(c.getClassText()) {
            case "NULL":
                Assert.assertFalse("Multiple instances of NULL", foundNull);
                Assert.assertEquals(8421504, c.getColor());
                Assert.assertEquals(-1, c.getIndex());
                foundNull = true;
                break;
            case "NEW_DISTINCT":
                Assert.assertFalse("Multiple instances of NEW_DISTINCT", foundDistinct);
                Assert.assertEquals(1111111, c.getColor());
                Assert.assertEquals(0, c.getIndex());
                foundDistinct = true;
                break;
            }
        }

        Assert.assertTrue("NULL not found", foundNull);
        Assert.assertTrue("NEW_DISTINCT not found", foundDistinct);
    }

    /**
     * Tests parsing an example getClassifications error
     * @throws Exception
     */
    @Test
    public void testGetClassifications_Error() throws Exception {
        final String serviceUrl = "http://example/url/wfs";
        final int algorithmOutputId = 123;
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/NVCL_GetClassificationsResponse_Error.xml");

        context.checking(new Expectations() {{
                oneOf(mockMethodMaker).getClassifications(serviceUrl, algorithmOutputId);will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseString));
        }});

        List<AlgorithmOutputClassification> responses = dataService.getClassifications(serviceUrl, algorithmOutputId);
        Assert.assertEquals(0, responses.size());
    }

    /**
     * Tests parsing an example submit processing response
     * @throws Exception
     */
    @Test
    public void testSubmitProcessingJob() throws Exception {
        final String[] algorithmOutputIds = new String[] {"123", "456"};
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/NVCL_submitAnalyticalJobResponse.json");

        final String[] wfsUrls = new String[] {"http://nvclwebservices.vm.csiro.au/geoserverBH/wfs"};
        final String email = "foo@bar.com";
        final String jobName = "myjobname";
        final String classification = "Muscovite";
        final String logName = null;
        final Integer startDepth = 0;
        final Integer endDepth = 999;
        final String logicalOp = "gt";
        final String value = "50";
        final String units = "pct";
        final Integer span = 1;
        final String wfsFilter = "<ogc:Filter><PropertyIsEqualTo><PropertyName>gsmlp:nvclCollection</PropertyName><Literal>true</Literal></PropertyIsEqualTo></ogc:Filter>";

        context.checking(new Expectations() {{
                oneOf(mockMethodMaker).submitProcessingJob(ANALYTICAL_SERVICES_URL, email, jobName, wfsUrls, wfsFilter, algorithmOutputIds, logName, classification, startDepth, endDepth, logicalOp, value, units, span);will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseString));
        }});

        boolean result = dataService.submitProcessingJob(email, jobName, wfsUrls, wfsFilter, algorithmOutputIds, logName, classification, startDepth, endDepth, logicalOp, value, units, span);
        Assert.assertTrue(result);
    }

    /**
     * Tests parsing an example submit processing response
     * @throws Exception
     */
    @Test
    public void testCheckProcessingJob() throws Exception {
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/NVCL_checkAnalyticalJobResponse.json");
        final String email = "foo@bar.com";

        context.checking(new Expectations() {{
                oneOf(mockMethodMaker).checkProcessingJob(ANALYTICAL_SERVICES_URL, email);will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseString));
        }});

        List<AnalyticalJobStatus> result = dataService.checkProcessingJobs(email);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());

        Assert.assertEquals("85d83b0cc3e0c878f2ec28f71fa8715b", result.get(0).getJobId());
        Assert.assertEquals("Success", result.get(0).getStatus());
        Assert.assertEquals("test001", result.get(0).getJobDescription());

        Assert.assertEquals("f42363ec8d735728d1ffc5b89dce2a7e", result.get(1).getJobId());
        Assert.assertEquals("Success", result.get(1).getStatus());
        Assert.assertEquals("test001", result.get(1).getJobDescription());
    }

    /**
     * Tests parsing an example submit processing response
     * @throws Exception
     */
    @Test
    public void testGetProcessingJobResults() throws Exception {
        final String responseString = ResourceUtil.loadResourceAsString("org/auscope/portal/nvcl/NVCL_getAnalyticalJobResultsResponse.json");
        final String id = "a015180de276f01fc146e7093dbe4389";

        context.checking(new Expectations() {{
                oneOf(mockMethodMaker).getProcessingJobResults(ANALYTICAL_SERVICES_URL, id);will(returnValue(mockMethod));
                oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);will(returnValue(responseString));
        }});

        AnalyticalJobResults results = dataService.getProcessingResults(id);
        Assert.assertNotNull(results);

        Assert.assertEquals("a015180de276f01fc146e7093dbe4389", results.getJobId());
        Assert.assertEquals("foo@bar.com", results.getEmail());
        Assert.assertEquals("test004", results.getJobDescription());

        Assert.assertEquals(2, results.getPassBoreholes().size());
        Assert.assertEquals(1, results.getFailBoreholes().size());
        Assert.assertEquals(1, results.getErrorBoreholes().size());

        Assert.assertEquals("http://example/id1", results.getPassBoreholes().get(0));
        Assert.assertEquals("http://example/id2", results.getPassBoreholes().get(1));
        Assert.assertEquals("http://example/id3", results.getFailBoreholes().get(0));
        Assert.assertEquals("http://example/id4", results.getErrorBoreholes().get(0));
    }
}

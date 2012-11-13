package org.auscope.portal.server.web.controllers;

import java.util.Arrays;
import java.util.Date;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.services.GeonetworkService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VGLSignature;
import org.auscope.portal.server.vegl.VglDownload;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Units tests for GeonetworkController
 * @author Josh Vote
 * @author Richard Goh
 */
public class TestGeonetworkController {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private VEGLJobManager mockJobManager;
    private GeonetworkService mockGNService;
    private CloudStorageService[] cloudStorageServices;
    private CloudComputeService[] cloudComputeServices;

    private GeonetworkController controller;

    private final String storageServiceId = "storage-service-id";
    private final String computeServiceId = "compute-service-id";

    @Before
    public void init() {
        mockJobManager = context.mock(VEGLJobManager.class);
        mockGNService = context.mock(GeonetworkService.class);
        cloudStorageServices = new CloudStorageService[] {context.mock(CloudStorageService.class)};
        cloudComputeServices = new CloudComputeService[] {context.mock(CloudComputeService.class)};

        context.checking(new Expectations() {{
            allowing(cloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(cloudComputeServices[0]).getId();will(returnValue(computeServiceId));
        }});

        controller = new GeonetworkController(mockJobManager, mockGNService, cloudStorageServices, cloudComputeServices);
    }

    /**
     * Tests that the getUserSignature correctly interact with all dependencies.
     */
    @Test
    public void testGetUserSignature() {
        final HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
        final HttpSession mockSession = context.mock(HttpSession.class);
        final String userEmail = "user@test.au";
        final VGLSignature userSignature = new VGLSignature(1, userEmail);

        context.checking(new Expectations() {{
            //We should make a single call to the database for job objects
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            //We should have a single call to the database for user signature object
            oneOf(mockJobManager).getSignatureByUser(userEmail);will(returnValue(userSignature));
        }});

        ModelAndView mav = controller.getUserSignature(mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the getUserSignature
     */
    @Test
    public void testGetUserSignatureDNE() {
        final HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
        final HttpSession mockSession = context.mock(HttpSession.class);
        final String userEmail = "user@test.org";

        context.checking(new Expectations() {{
            //We should have a call to http request session to get user's email
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            //We should have a single call to the database for user signature object
            oneOf(mockJobManager).getSignatureByUser(userEmail);will(returnValue(null));
        }});

        ModelAndView mav = controller.getUserSignature(mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the getUserSignature correctly fails when user session has expired.
     */
    @Test
    public void testGetUserSignatureSessionExpired() {
        final HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
        final HttpSession mockSession = context.mock(HttpSession.class);

        context.checking(new Expectations() {{
            //We should have a call to http request session to get user's email
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("openID-Email");will(returnValue(null));
        }});

        ModelAndView mav = controller.getUserSignature(mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the insertRecord function correctly uses all dependencies on success.
     * @throws Exception
     */
    @Test
    public void testInsertRecord() throws Exception {
        final Integer jobId = 1235;
        final Integer seriesId = 5432;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        final String registeredUrl  = "http://example.csw.url/";
        final HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
        final HttpSession mockSession = context.mock(HttpSession.class);
        final ServletContext mockContext = context.mock(ServletContext.class);
        final String userEmail = "user@test.au";
        final VGLSignature userSignature = new VGLSignature(1, userEmail);
        final CloudFileInformation[] outputFileInfo = new CloudFileInformation[] {
                new CloudFileInformation("my/key1", 100L, "http://public.url1"),
                new CloudFileInformation("my/key2", 200L, "http://public.url2"),
                new CloudFileInformation("my/key3", 300L, "http://public.url3")
        };
        final VglDownload download = new VglDownload(5341);
        download.setNorthBoundLatitude(4.0);
        download.setSouthBoundLatitude(3.0);
        download.setEastBoundLongitude(2.0);
        download.setWestBoundLongitude(1.0);

        download.setDescription("desc");
        download.setName("name");
        download.setUrl("http://example.org/5432");

        //We want to ensure our job is set values BEFORE saving it
        final Sequence jobSavingSequence = context.sequence("jobSavingSequence");

        context.checking(new Expectations() {{
            //Our mock job configuration
            allowing(mockJob).getDescription();will(returnValue("description"));
            allowing(mockJob).getSubmitDate();will(returnValue(new Date()));
            allowing(mockJob).getName();will(returnValue("name"));
            allowing(mockJob).getUser();will(returnValue("user"));
            allowing(mockJob).getSeriesId();will(returnValue(seriesId));
            allowing(mockJob).getEmailAddress();will(returnValue("email@address"));
            allowing(mockJob).getJobDownloads();will(returnValue(Arrays.asList(download)));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));

            //Our series configuration
            allowing(mockSeries).getName();will(returnValue("seriesName"));
            allowing(mockSeries).getDescription();will(returnValue("seriesDescription"));

            //We should make a single call to the database for job objects
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));

            //We should have a call to http request session to get user's email
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            //We should have a single call to the database for user signature object
            oneOf(mockJobManager).getSignatureByUser(userEmail);will(returnValue(userSignature));

            //We should have a call to the job manager to store user signature object
            oneOf(mockJobManager).saveSignature(userSignature);

            //Only 1 call to the job storage service for files
            oneOf(cloudStorageServices[0]).listJobFiles(mockJob);will(returnValue(outputFileInfo));
            allowing(cloudStorageServices[0]).getBucket();will(returnValue("s3-output-bucket"));

            //We should have calls to HttpServletRequest to get parameters needed for registering job to Geonetwork
            allowing(mockRequest).getParameter("organisationName");will(returnValue("organisationName"));
            allowing(mockRequest).getParameter("administrativeArea");will(returnValue("administrativeArea"));
            allowing(mockRequest).getParameter("city");will(returnValue("city"));
            allowing(mockRequest).getParameter("deliveryPoint");will(returnValue("deliveryPoint"));
            allowing(mockRequest).getParameter("postalCode");will(returnValue("postalCode"));
            allowing(mockRequest).getParameter("country");will(returnValue("country"));
            allowing(mockRequest).getParameter("telephone");will(returnValue("telephone"));
            allowing(mockRequest).getParameter("facsimile");will(returnValue("facsimile"));
            allowing(mockRequest).getParameter("onlineContactURL");will(returnValue("http://localhost"));
            allowing(mockRequest).getParameter("onlineContactName");will(returnValue("onlineContactName"));
            allowing(mockRequest).getParameter("onlineContactDescription");will(returnValue("onlineContactDescription"));
            allowing(mockRequest).getParameter("individualName");will(returnValue("individualName"));
            allowing(mockRequest).getParameter("organisationName");will(returnValue("organisationName"));
            allowing(mockRequest).getParameter("positionName");will(returnValue("positionName"));
            allowing(mockRequest).getParameter("constraints");will(returnValue("constraints"));
            allowing(mockRequest).getParameter("keywords");will(returnValue("keyword1, keyword2"));

            //Only 1 call to save our newly created record
            oneOf(mockGNService).makeCSWRecordInsertion(with(any(CSWRecord.class)));will(returnValue(registeredUrl));

            //This must occur in sequence (set our value before saving it)
            oneOf(mockJob).setRegisteredUrl(registeredUrl);inSequence(jobSavingSequence);
            oneOf(mockJobManager).saveJob(mockJob);inSequence(jobSavingSequence);

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getServletContext();will(returnValue(mockContext));
            allowing(mockContext).getRealPath("/");will(returnValue("src/main/webapp"));
        }});

        ModelAndView mav = controller.insertRecord(jobId, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertEquals(registeredUrl, mav.getModel().get("data"));
    }

    /**
     * Tests that the insertRecord function correctly fails when the job object DNE.
     * @throws Exception
     */
    @Test
    public void testInsertRecordJobDNE() throws Exception {
        final Integer jobId = 1235;
        final HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);

        context.checking(new Expectations() {{
            //We should make a single call to the database for job objects
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(null));
        }});

        ModelAndView mav = controller.insertRecord(jobId, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the insertRecord function correctly fails when the job series DNE
     * @throws Exception
     */
    @Test
    public void testInsertRecordSeriesDNE() throws Exception {
        final Integer jobId = 1235;
        final Integer seriesId = 5432;
        final HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            //Our mock job configuration
            allowing(mockJob).getSeriesId();will(returnValue(seriesId));

            //We should make a single call to the database for job objects
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(null));
        }});

        ModelAndView mav = controller.insertRecord(jobId, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the insertRecord function correctly fails when there is a failure to lookup S3 output files.
     * @throws Exception
     */
    @Test
    public void testInsertRecordS3Failure() throws Exception {
        final Integer jobId = 1235;
        final Integer seriesId = 5432;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        final HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
        final HttpSession mockSession = context.mock(HttpSession.class);
        final String userEmail = "user@test.au";
        final VGLSignature userSignature = new VGLSignature(1, userEmail);

        context.checking(new Expectations() {{
            //Our mock job configuration
            allowing(mockJob).getSeriesId();will(returnValue(seriesId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));

            //We should have calls to HttpServletRequest to get parameters needed for registering job to Geonetwork
            allowing(mockRequest).getParameter("organisationName");will(returnValue("organisationName"));
            allowing(mockRequest).getParameter("administrativeArea");will(returnValue("administrativeArea"));
            allowing(mockRequest).getParameter("city");will(returnValue("city"));
            allowing(mockRequest).getParameter("deliveryPoint");will(returnValue("deliveryPoint"));
            allowing(mockRequest).getParameter("postalCode");will(returnValue("postalCode"));
            allowing(mockRequest).getParameter("country");will(returnValue("country"));
            allowing(mockRequest).getParameter("telephone");will(returnValue("telephone"));
            allowing(mockRequest).getParameter("facsimile");will(returnValue("facsimile"));
            allowing(mockRequest).getParameter("onlineContactURL");will(returnValue("http://localhost"));
            allowing(mockRequest).getParameter("onlineContactName");will(returnValue("onlineContactName"));
            allowing(mockRequest).getParameter("onlineContactDescription");will(returnValue("onlineContactDescription"));
            allowing(mockRequest).getParameter("individualName");will(returnValue("individualName"));
            allowing(mockRequest).getParameter("organisationName");will(returnValue("organisationName"));
            allowing(mockRequest).getParameter("positionName");will(returnValue("positionName"));
            allowing(mockRequest).getParameter("constraints");will(returnValue("constraints"));
            allowing(mockRequest).getParameter("keywords");will(returnValue("keyword1, keyword2"));

            //We should make a single call to the database for job objects
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));

            //We should have a call to http request session to get user's email
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            //We should have a single call to the database for user signature object
            oneOf(mockJobManager).getSignatureByUser(userEmail);will(returnValue(userSignature));

            //We should have a call to the job manager to store user signature object
            oneOf(mockJobManager).saveSignature(userSignature);

            //Only 1 call to the job storage service for files
            oneOf(cloudStorageServices[0]).listJobFiles(mockJob);will(throwException(new PortalServiceException("")));
        }});

        ModelAndView mav = controller.insertRecord(jobId, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the insertRecord function correctly fails when registration to geonetwork fails
     * @throws Exception
     */
    @Test
    public void testInsertRecordGNServiceError() throws Exception {
        final Integer jobId = 1235;
        final Integer seriesId = 5432;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        final HttpServletRequest mockRequest = context.mock(HttpServletRequest.class);
        final HttpSession mockSession = context.mock(HttpSession.class);
        final ServletContext mockContext = context.mock(ServletContext.class);
        final String userEmail = "user@test.au";
        final VGLSignature userSignature = new VGLSignature(1, userEmail);
        final CloudFileInformation[] outputFileInfo = new CloudFileInformation[] {
                new CloudFileInformation("my/key1", 100L, "http://public.url1"),
                new CloudFileInformation("my/key2", 200L, "http://public.url2"),
                new CloudFileInformation("my/key3", 300L, "http://public.url3")
        };
        final VglDownload download = new VglDownload(5341);
        download.setNorthBoundLatitude(4.0);
        download.setSouthBoundLatitude(3.0);
        download.setEastBoundLongitude(2.0);
        download.setWestBoundLongitude(1.0);

        download.setDescription("desc");
        download.setName("name");
        download.setUrl("http://example.org/5432");
        context.checking(new Expectations() {{
            //Our mock job configuration
            allowing(mockJob).getDescription();will(returnValue("description"));
            allowing(mockJob).getSubmitDate();will(returnValue("20110713_105730"));
            allowing(mockJob).getName();will(returnValue("name"));
            allowing(mockJob).getUser();will(returnValue("user"));
            allowing(mockJob).getSeriesId();will(returnValue(seriesId));
            allowing(mockJob).getEmailAddress();will(returnValue("email@address"));
            allowing(mockJob).getJobDownloads();will(returnValue(Arrays.asList(download)));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));

            //Our series configuration
            allowing(mockSeries).getName();will(returnValue("seriesName"));
            allowing(mockSeries).getDescription();will(returnValue("seriesDescription"));

            //We should make a single call to the database for job objects
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));

            //We should have a call to http request session to get user's email
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            //We should have a single call to the database for user signature object
            oneOf(mockJobManager).getSignatureByUser(userEmail);will(returnValue(userSignature));

            //We should have a call to the job manager to store user signature object
            oneOf(mockJobManager).saveSignature(userSignature);

            //Only 1 call to the job storage service for files
            oneOf(cloudStorageServices[0]).listJobFiles(mockJob);will(returnValue(outputFileInfo));
            allowing(cloudStorageServices[0]).getBucket();will(returnValue("s3-output-bucket"));

            //We should have calls to HttpServletRequest to get parameters needed for registering job to Geonetwork
            allowing(mockRequest).getParameter("organisationName");will(returnValue("organisationName"));
            allowing(mockRequest).getParameter("administrativeArea");will(returnValue("administrativeArea"));
            allowing(mockRequest).getParameter("city");will(returnValue("city"));
            allowing(mockRequest).getParameter("deliveryPoint");will(returnValue("deliveryPoint"));
            allowing(mockRequest).getParameter("postalCode");will(returnValue("postalCode"));
            allowing(mockRequest).getParameter("country");will(returnValue("country"));
            allowing(mockRequest).getParameter("telephone");will(returnValue("telephone"));
            allowing(mockRequest).getParameter("facsimile");will(returnValue("facsimile"));
            allowing(mockRequest).getParameter("onlineContactURL");will(returnValue("http://localhost"));
            allowing(mockRequest).getParameter("onlineContactName");will(returnValue("onlineContactName"));
            allowing(mockRequest).getParameter("onlineContactDescription");will(returnValue("onlineContactDescription"));
            allowing(mockRequest).getParameter("individualName");will(returnValue("individualName"));
            allowing(mockRequest).getParameter("organisationName");will(returnValue("organisationName"));
            allowing(mockRequest).getParameter("positionName");will(returnValue("positionName"));
            allowing(mockRequest).getParameter("constraints");will(returnValue("constraints"));
            allowing(mockRequest).getParameter("keywords");will(returnValue("keyword1, keyword2"));

            //Only 1 call to save our newly created record
            oneOf(mockGNService).makeCSWRecordInsertion(with(any(CSWRecord.class)));will(throwException(new Exception()));

            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getServletContext();will(returnValue(mockContext));
            allowing(mockContext).getRealPath("/");will(returnValue("src/main/webapp"));
        }});

        ModelAndView mav = controller.insertRecord(jobId, mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }
}
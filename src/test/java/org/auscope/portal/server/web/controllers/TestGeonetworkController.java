package org.auscope.portal.server.web.controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.services.GeonetworkService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.vegl.VglParameter;
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
 *
 */
public class TestGeonetworkController {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private VEGLJobManager mockJobManager;
    private GeonetworkService mockGNService;
    private CloudStorageService cloudStorageService;

    private GeonetworkController controller;

    @Before
    public void init() {
        mockJobManager = context.mock(VEGLJobManager.class);
        mockGNService = context.mock(GeonetworkService.class);
        cloudStorageService = context.mock(CloudStorageService.class);

        controller = new GeonetworkController(mockJobManager, mockGNService, cloudStorageService);
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

            //Our series configuration
            allowing(mockSeries).getName();will(returnValue("seriesName"));
            allowing(mockSeries).getDescription();will(returnValue("seriesDescription"));

            //We should make a single call to the database for job objects
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));

            //Only 1 call to the job storage service for files
            oneOf(cloudStorageService).listJobFiles(mockJob);will(returnValue(outputFileInfo));
            allowing(cloudStorageService).getBucket();will(returnValue("s3-output-bucket"));

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

        context.checking(new Expectations() {{
            //Our mock job configuration
            allowing(mockJob).getSeriesId();will(returnValue(seriesId));

            //We should make a single call to the database for job objects
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));

            //Only 1 call to the job storage service for files
            oneOf(cloudStorageService).listJobFiles(mockJob);will(throwException(new PortalServiceException("")));
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

            //Our series configuration
            allowing(mockSeries).getName();will(returnValue("seriesName"));
            allowing(mockSeries).getDescription();will(returnValue("seriesDescription"));

            //We should make a single call to the database for job objects
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));

            //Only 1 call to the job storage service for files
            oneOf(cloudStorageService).listJobFiles(mockJob);will(returnValue(outputFileInfo));
            allowing(cloudStorageService).getBucket();will(returnValue("s3-output-bucket"));

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

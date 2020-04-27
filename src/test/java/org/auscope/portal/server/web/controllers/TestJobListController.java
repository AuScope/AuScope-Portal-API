package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageServiceJClouds;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.jmock.ReadableServletOutputStream;
import org.auscope.portal.jmock.VEGLJobMatcher;
import org.auscope.portal.jmock.VEGLSeriesMatcher;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.auscope.portal.server.web.service.CloudSubmissionService;
import org.auscope.portal.server.web.service.VGLJobAuditLogService;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for JobListController
 * @author Josh Vote
 * @author Richard Goh
 */
public class TestJobListController extends PortalTestClass {
    private final String computeServiceId = "comp-service-id";
    private final String storageServiceId = "storage-service-id";
    private VEGLJobManager mockJobManager;
    private ANVGLUserService mockUserService;
    private CloudStorageServiceJClouds[] mockCloudStorageServices;
    private FileStagingService mockFileStagingService;
    private CloudComputeService[] mockCloudComputeServices;
    private VGLJobStatusAndLogReader mockVGLJobStatusAndLogReader;
    private VGLJobAuditLogService mockJobAuditLogService;
    private ANVGLUser mockPortalUser;
    private JobStatusMonitor mockJobStatusMonitor;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private JobListController controller;
    private CloudSubmissionService mockCloudSubmissionService;

    /**
     * Load our mock objects
     */
    @Before
    public void init() {
        mockJobManager = context.mock(VEGLJobManager.class);
        mockCloudStorageServices = new CloudStorageServiceJClouds[] {context.mock(CloudStorageServiceJClouds.class)};
        mockFileStagingService = context.mock(FileStagingService.class);
        mockCloudComputeServices = new CloudComputeService[] {context.mock(CloudComputeService.class)};
        mockUserService = context.mock(ANVGLUserService.class);
        mockVGLJobStatusAndLogReader = context.mock(VGLJobStatusAndLogReader.class);
        mockJobStatusMonitor = context.mock(JobStatusMonitor.class);
        mockResponse = context.mock(HttpServletResponse.class);
        mockRequest = context.mock(HttpServletRequest.class);
        mockJobAuditLogService = context.mock(VGLJobAuditLogService.class);
        mockPortalUser = context.mock(ANVGLUser.class);
        mockCloudSubmissionService = context.mock(CloudSubmissionService.class);
        final List<VEGLJob> mockJobs=new ArrayList<>();
        
        context.checking(new Expectations() {{
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            allowing(mockJobManager).getInQueueJobs();will(returnValue(mockJobs));
            allowing(mockUserService).getLoggedInUser();will(returnValue(mockPortalUser));
        }});

        controller = new JobListController(mockJobManager,
                mockCloudStorageServices, mockFileStagingService,
                mockCloudComputeServices, mockUserService,
                mockVGLJobStatusAndLogReader, mockJobStatusMonitor,null,null,"dummy@dummy.com", mockCloudSubmissionService, mockJobAuditLogService);
    }



    public static VEGLJobMatcher aVeglJob(Integer id) {
        return new VEGLJobMatcher(id);
    }

    public static VEGLJobMatcher aNonMatchingVeglJob(Integer id) {
        return new VEGLJobMatcher(id, true);
    }

//    /**
//     * Tests getting a series from the job manager
//     */
//    @Test
//    public void testMySeries() {
//        final String userEmail = "exampleuser@email.com";
//        final VEGLSeries series = context.mock(VEGLSeries.class);
//        final List<VEGLSeries> seriesList = Arrays.asList(series);
//
//        context.checking(new Expectations() {{
//            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));
//
//            oneOf(mockJobManager).querySeries(userEmail, null, null);will(returnValue(seriesList));
//        }});
//
//        ModelAndView mav = controller.mySeries(mockRequest, mockResponse, mockPortalUser);
//        Assert.assertTrue((Boolean)mav.getModel().get("success"));
//    }
//
//    /**
//     * Tests getting a series when there is no email address in the user's session
//     */
//    @Test
//    public void testMySeriesNoEmail() {
//        context.checking(new Expectations() {{
//            allowing(mockPortalUser).getEmail();will(returnValue(null));
//        }});
//
//        ModelAndView mav = controller.mySeries(mockRequest, mockResponse, mockPortalUser);
//        Assert.assertFalse((Boolean)mav.getModel().get("success"));
//    }
//
//    /**
//     * Tests getting a series when there is no email address in the user's session
//     */
//    @Test
//    public void testMySeriesNoUser() {
//        context.checking(new Expectations() {{
//
//        }});
//
//        ModelAndView mav = controller.mySeries(mockRequest, mockResponse, null);
//        Assert.assertFalse((Boolean)mav.getModel().get("success"));
//    }

    /**
     * Tests deleting a job successfully
     * @throws PortalServiceException 
     */
    @Test
    public void testDeleteJob() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String initialStatus = JobBuilderController.STATUS_DONE;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));

            //Make sure the job marked as deleted and its transition audit trial record is created
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(jobId));
            oneOf(mockJob).getStatus();will(returnValue(initialStatus));
            oneOf(mockJob).getStatus();will(returnValue(initialStatus));
            oneOf(mockJob).setStatus(JobBuilderController.STATUS_DELETED);
            oneOf(mockJobManager).saveJob(mockJob);
            oneOf(mockJobManager).createJobAuditTrail(initialStatus, mockJob, "Job deleted.");

            oneOf(mockFileStagingService).deleteStageInDirectory(mockJob);
            oneOf(mockJob).getRegisteredUrl();will(returnValue("geonetwork url"));
        }});

        ModelAndView mav = controller.deleteJob(mockRequest, mockResponse, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a job successfully
     */
    @Test
    public void testDeleteJob_NotRegistered() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String initialStatus = JobBuilderController.STATUS_DONE;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));
            allowing(mockJob).getId();will(returnValue(jobId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));


            //Make sure the job marked as deleted and its transition audit trial record is created
            oneOf(mockJob).getStatus();will(returnValue(initialStatus));
            oneOf(mockJob).getStatus();will(returnValue(initialStatus));
            oneOf(mockJob).setStatus(JobBuilderController.STATUS_DELETED);
            oneOf(mockJobManager).saveJob(mockJob);
            oneOf(mockJobManager).createJobAuditTrail(initialStatus, mockJob, "Job deleted.");

            oneOf(mockFileStagingService).deleteStageInDirectory(mockJob);
            oneOf(mockJob).getRegisteredUrl();will(returnValue(null)); //the job isn't registered
            oneOf(mockCloudStorageServices[0]).deleteJobFiles(mockJob); //this must occur if the job isnt registered
        }});

        ModelAndView mav = controller.deleteJob(mockRequest, mockResponse, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }


    /**
     * Tests deleting a running job successfully
     */
    @Test
    public void testDeleteJob_Running() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String initialStatus = JobBuilderController.STATUS_ACTIVE;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));

            //Make sure the job marked as deleted and its transition audit trial record is created
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(jobId));
            oneOf(mockJob).getStatus();will(returnValue(initialStatus));
            oneOf(mockJob).getStatus();will(returnValue(initialStatus));
            oneOf(mockJob).setStatus(JobBuilderController.STATUS_DELETED);
            oneOf(mockJobManager).saveJob(mockJob);
            oneOf(mockJobManager).createJobAuditTrail(initialStatus, mockJob, "Job deleted.");

            oneOf(mockFileStagingService).deleteStageInDirectory(mockJob);
            oneOf(mockJob).getRegisteredUrl();will(returnValue("geonetwork url"));

            oneOf(mockCloudComputeServices[0]).terminateJob(mockJob);
        }});

        ModelAndView mav = controller.deleteJob(mockRequest, mockResponse, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a queued job successfully
     */
    @Test
    public void testDeleteJob_InQueue() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String initialStatus = JobBuilderController.STATUS_INQUEUE;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));

            //Make sure the job marked as deleted and its transition audit trial record is created
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(jobId));
            oneOf(mockJob).getStatus();will(returnValue(initialStatus));
            oneOf(mockJob).getStatus();will(returnValue(initialStatus));
            oneOf(mockJob).setStatus(JobBuilderController.STATUS_DELETED);
            oneOf(mockJobManager).saveJob(mockJob);
            oneOf(mockJobManager).createJobAuditTrail(initialStatus, mockJob, "Job deleted.");

            oneOf(mockFileStagingService).deleteStageInDirectory(mockJob);
            oneOf(mockJob).getRegisteredUrl();will(returnValue("geonetwork url"));

            oneOf(mockCloudSubmissionService).dequeueSubmission(mockJob, mockCloudComputeServices[0]);
        }});

        ModelAndView mav = controller.deleteJob(mockRequest, mockResponse, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a job fails when its another users job
     * @throws PortalServiceException 
     */
    @Test(expected=AccessDeniedException.class)
    public void testDeleteJobNoPermission() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "adifferentuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));


            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(jobEmail));
        }});

        controller.deleteJob(mockRequest, mockResponse, jobId);
    }

    /**
     * Tests deleting a job fails when the jobID DNE
     * @throws PortalServiceException 
     */
    @Test
    public void testDeleteJobDNE() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(null));
        }});

        ModelAndView mav = controller.deleteJob(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a series successfully
     * @throws PortalServiceException 
     */
    @Test
    public void testDeleteSeries() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;
        final List<VEGLJob> mockJobs = Arrays.asList(
                context.mock(VEGLJob.class, "mockJob1"),
                context.mock(VEGLJob.class, "mockJob2"));
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));
            allowing(mockSeries).getUser();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(mockSeries));
            oneOf(mockJobManager).getSeriesJobs(seriesId, mockPortalUser);will(returnValue(mockJobs));

            //Make sure each job marked as deleted, its transition audit trial record
            //is created and all its files in staging directory are deleted.
            oneOf(mockJobs.get(0)).getStatus();will(returnValue(JobBuilderController.STATUS_PENDING));
            oneOf(mockJobs.get(0)).setStatus(JobBuilderController.STATUS_DELETED);
            oneOf(mockJobManager).saveJob(mockJobs.get(0));
            oneOf(mockJobManager).createJobAuditTrail(JobBuilderController.STATUS_PENDING, mockJobs.get(0), "Job deleted.");
            oneOf(mockFileStagingService).deleteStageInDirectory(mockJobs.get(0));
            oneOf(mockJobs.get(0)).getRegisteredUrl();will(returnValue("geonetwork url"));

            oneOf(mockJobs.get(1)).getStatus();will(returnValue(JobBuilderController.STATUS_DONE));
            oneOf(mockJobs.get(1)).setStatus(JobBuilderController.STATUS_DELETED);
            oneOf(mockJobManager).saveJob(mockJobs.get(1));
            oneOf(mockJobManager).createJobAuditTrail(JobBuilderController.STATUS_DONE, mockJobs.get(1), "Job deleted.");
            oneOf(mockFileStagingService).deleteStageInDirectory(mockJobs.get(1));
            oneOf(mockJobs.get(1)).getRegisteredUrl();will(returnValue("geonetwork url"));

            oneOf(mockJobManager).deleteSeries(mockSeries);
        }});

        ModelAndView mav = controller.deleteSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a series fails when the user doesn't have permission
     * @throws PortalServiceException 
     */
    @Test(expected=AccessDeniedException.class)
    public void testDeleteSeriesNoPermission() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final String seriesEmail = "anotheruser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));
            allowing(mockSeries).getUser();will(returnValue(seriesEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(mockSeries));
        }});

        controller.deleteSeriesJobs(mockRequest, mockResponse, seriesId);
    }

    /**
     * Tests deleting a series fails when series DNE
     * @throws PortalServiceException 
     */
    @Test
    public void testDeleteSeriesDNE() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(null));
        }});

        ModelAndView mav = controller.deleteSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that deleting a series fails when job
     * list is null.
     * @throws PortalServiceException 
     */
    @Test
    public void testDeleteSeries_JobListIsNull() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));
            allowing(mockSeries).getUser();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(mockSeries));
            oneOf(mockJobManager).getSeriesJobs(seriesId, mockPortalUser);will(returnValue(null));
        }});

        ModelAndView mav = controller.deleteSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that killing or cancelling a job succeeds
     */
    @Test
    public void testKillJob() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));
            allowing(mockJob).getUser();will(returnValue(userEmail));
            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));

            allowing(mockJob).getStatus();will(returnValue(JobBuilderController.STATUS_PENDING));

            oneOf(mockCloudComputeServices[0]).terminateJob(mockJob);
            oneOf(mockJob).setStatus(JobBuilderController.STATUS_UNSUBMITTED);
            oneOf(mockJobManager).saveJob(mockJob);
            oneOf(mockJobManager).createJobAuditTrail(JobBuilderController.STATUS_PENDING, mockJob, "Job cancelled by user.");
        }});

        ModelAndView mav = controller.killJob(mockRequest, mockResponse, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing or cancelling job get aborted when the job is processed
     * @throws PortalServiceException 
     */
    @Test
    public void testKillJobAborted() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));
            allowing(mockJob).getUser();will(returnValue(userEmail));
            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getStatus();will(returnValue(JobBuilderController.STATUS_DONE));
        }});

        ModelAndView mav = controller.killJob(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing a job fails when its not the user's job
     * @throws PortalServiceException 
     */
    @Test(expected=AccessDeniedException.class)
    public void testKillJobNoPermission() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "anotheruser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            allowing(mockJob).getUser();will(returnValue(jobEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
        }});

        controller.killJob(mockRequest, mockResponse, jobId);
    }

    /**
     * Tests that killing a job fails when the job cannot be found
     * @throws PortalServiceException 
     */
    @Test
    public void testKillJobDNE() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(null));
        }});

        ModelAndView mav = controller.killJob(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing or cancelling all jobs of a series succeeds
     */
    @Test
    public void testKillSeriesJobs() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        final List<VEGLJob> mockJobs = Arrays.asList(
                context.mock(VEGLJob.class, "mockJobDone"),
                context.mock(VEGLJob.class, "mockJobActive"),
                context.mock(VEGLJob.class, "mockJobUnsubmitted"),
                context.mock(VEGLJob.class, "mockJobPending"));

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));
            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(mockSeries));
            allowing(mockSeries).getUser();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesJobs(seriesId, mockPortalUser);will(returnValue(mockJobs));

            //Each of our jobs is in a different status
            allowing(mockJobs.get(0)).getStatus();will(returnValue(JobBuilderController.STATUS_DONE));
            allowing(mockJobs.get(1)).getStatus();will(returnValue(JobBuilderController.STATUS_ACTIVE));
            allowing(mockJobs.get(2)).getStatus();will(returnValue(JobBuilderController.STATUS_UNSUBMITTED));
            allowing(mockJobs.get(3)).getStatus();will(returnValue(JobBuilderController.STATUS_PENDING));
            allowing(mockJobs.get(0)).getId();will(returnValue(new Integer(0)));
            allowing(mockJobs.get(1)).getId();will(returnValue(new Integer(1)));
            allowing(mockJobs.get(2)).getId();will(returnValue(new Integer(2)));
            allowing(mockJobs.get(3)).getId();will(returnValue(new Integer(3)));
            allowing(mockJobs.get(0)).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJobs.get(0)).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJobs.get(1)).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJobs.get(1)).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJobs.get(2)).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJobs.get(2)).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJobs.get(3)).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJobs.get(3)).getComputeServiceId();will(returnValue(computeServiceId));

            //Only the pending and active job can be cancelled
            oneOf(mockCloudComputeServices[0]).terminateJob(mockJobs.get(1));
            oneOf(mockCloudComputeServices[0]).terminateJob(mockJobs.get(3));
            oneOf(mockJobs.get(1)).setStatus(JobBuilderController.STATUS_UNSUBMITTED);
            oneOf(mockJobs.get(3)).setStatus(JobBuilderController.STATUS_UNSUBMITTED);
            oneOf(mockJobManager).saveJob(mockJobs.get(1));
            oneOf(mockJobManager).saveJob(mockJobs.get(3));
            oneOf(mockJobManager).createJobAuditTrail(JobBuilderController.STATUS_ACTIVE, mockJobs.get(1), "Job cancelled by user.");
            oneOf(mockJobManager).createJobAuditTrail(JobBuilderController.STATUS_PENDING, mockJobs.get(3), "Job cancelled by user.");
        }});

        ModelAndView mav = controller.killSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing all jobs of a series fails when the user lacks permission
     * @throws PortalServiceException 
     */
    @Test(expected=AccessDeniedException.class)
    public void testKillSeriesJobsNoPermission() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final String seriesEmail = "anotheruser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(mockSeries));
            allowing(mockSeries).getUser();will(returnValue(seriesEmail));
        }});

        controller.killSeriesJobs(mockRequest, mockResponse, seriesId);
    }

    /**
     * Tests that killing all jobs of a series fails when the user lacks permission
     * @throws PortalServiceException 
     */
    @Test
    public void testKillSeriesJobsDNE() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(null));
        }});

        ModelAndView mav = controller.killSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * tests listing job files succeeds
     */
    @Test
    public void testListJobFiles() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final CloudFileInformation[] fileDetails = new CloudFileInformation[] {
                context.mock(CloudFileInformation.class, "fileInfo1"),
                context.mock(CloudFileInformation.class, "fileInfo2"),
                context.mock(CloudFileInformation.class, "fileInfo3")
        };

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));

            oneOf(mockCloudStorageServices[0]).listJobFiles(mockJob);will(returnValue(fileDetails));
        }});

        ModelAndView mav = controller.jobCloudFiles(mockRequest, mockResponse, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertSame(fileDetails, mav.getModel().get("data"));
    }

    /**
     * tests listing job files fails if the user doesnt have permission
     * @throws PortalServiceException 
     */
    @Test(expected=AccessDeniedException.class)
    public void testListJobFilesNoPermission() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "anotheruser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(jobEmail));

        }});

        controller.jobCloudFiles(mockRequest, mockResponse, jobId);
    }

    /**
     * tests listing job files fails if the user doesnt have permission
     * @throws PortalServiceException 
     */
    @Test
    public void testListJobFilesDNE() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(null));

        }});

        ModelAndView mav = controller.jobCloudFiles(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * tests listing job files fails when the underlying S3 service fails.
     */
    @Test
    public void testListJobFilesServiceException() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);


        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));

            oneOf(mockCloudStorageServices[0]).listJobFiles(mockJob);will(throwException(new PortalServiceException("")));
        }});

        ModelAndView mav = controller.jobCloudFiles(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that downloading a single job file succeeds
     */
    @Test
    public void testDownloadJobFile() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String key = "my/file/key";
        final String fileName = "fileName.txt";
        final byte[] data = new byte[] {1,2,4,5,6,7,8,6,5,4,4,3,2,1};
        final InputStream inputStream = new ByteArrayInputStream(data);
        try (final ReadableServletOutputStream outStream = new ReadableServletOutputStream()) {
            context.checking(new Expectations() {
                {
                    allowing(mockPortalUser).getEmail();
                    will(returnValue(userEmail));

                    oneOf(mockJobManager).getJobById(jobId, mockPortalUser);
                    will(returnValue(mockJob));
                    allowing(mockJob).getUser();
                    will(returnValue(userEmail));
                    allowing(mockJob).getStorageServiceId();
                    will(returnValue(storageServiceId));
                    allowing(mockJob).getComputeServiceId();
                    will(returnValue(computeServiceId));

                    oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, key);
                    will(returnValue(inputStream));

                    // Ensure our response stream gets written to
                    oneOf(mockResponse).setContentType("application/octet-stream");
                    allowing(mockResponse).setHeader(with(any(String.class)), with(any(String.class)));
                    oneOf(mockResponse).getOutputStream();
                    will(returnValue(outStream));
                }
            });

            // Returns null on success
            ModelAndView mav = controller.downloadFile(mockRequest, mockResponse, jobId, fileName, key);
            Assert.assertNull(mav);

            Assert.assertArrayEquals(data, outStream.getDataWritten());
        }
    }

    /**
     * Tests that downloading a single job file fails when the user doesnt own the job
     * @throws PortalServiceException 
     */
    @Test(expected=AccessDeniedException.class)
    public void testDownloadJobFileNoPermission() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "anotheruser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String key = "my/file/key";
        final String fileName = "fileName.txt";

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(jobEmail));
        }});

        controller.downloadFile(mockRequest, mockResponse, jobId, fileName, key);
    }

    /**
     * Tests that downloading a single job file fails when the job DNE
     * @throws PortalServiceException 
     */
    @Test
    public void testDownloadJobFileDNE() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final String key = "my/file/key";
        final String fileName = "fileName.txt";

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(null));
        }});

        //Returns null on success
        ModelAndView mav = controller.downloadFile(mockRequest, mockResponse, jobId, fileName, key);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that downloading multiple job files succeeds
     */
    @Test
    public void testDownloadJobFilesAsZip() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String fileKey1 = "file/key/1";
        final String fileKey2 = "file/key/2";
        final String fileKey3 = "file/key/3";
        final String files = fileKey1 + "," + fileKey2 + "," + fileKey3 + ",";
        final byte[] file1Data = new byte[] {1,2,4,5,6,7,8,0,5,4,4,4,2,1};
        final byte[] file2Data = new byte[] {2,5,4,5,2,2,8,6,5,7,4,3,4,2,6};
        final byte[] file3Data = new byte[] {3,2,7,5,6,9,8,8,5,4,6,3,4};
        final InputStream is1 = new ByteArrayInputStream(file1Data);
        final InputStream is2 = new ByteArrayInputStream(file2Data);
        final InputStream is3 = new ByteArrayInputStream(file3Data);
        final String jobName = "job WITH !()[]#$%@\\/;\"'";
        final Date submitDate = new SimpleDateFormat("yyyyMMdd").parse("19861009");

        try (final ReadableServletOutputStream outStream = new ReadableServletOutputStream()) {
            context.checking(new Expectations() {
                {
                    allowing(mockPortalUser).getEmail();
                    will(returnValue(userEmail));

                    oneOf(mockJobManager).getJobById(jobId, mockPortalUser);
                    will(returnValue(mockJob));
                    allowing(mockJob).getName();
                    will(returnValue(jobName));
                    allowing(mockJob).getUser();
                    will(returnValue(userEmail));
                    allowing(mockJob).getStorageServiceId();
                    will(returnValue(storageServiceId));
                    allowing(mockJob).getComputeServiceId();
                    will(returnValue(computeServiceId));
                    allowing(mockJob).getSubmitDate();
                    will(returnValue(submitDate));

                    oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, fileKey1);
                    will(returnValue(is1));
                    oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, fileKey2);
                    will(returnValue(is2));
                    oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, fileKey3);
                    will(returnValue(is3));

                    // Ensure our response stream gets written to
                    oneOf(mockResponse).setContentType("application/zip");
                    oneOf(mockResponse).setHeader("Content-Disposition",
                            "attachment; filename=\"jobfiles_job_WITH________________19861009.zip\"");
                    oneOf(mockResponse).getOutputStream();
                    will(returnValue(outStream));
                }
            });

            // Returns null on success
            ModelAndView mav = controller.downloadAsZip(mockRequest, mockResponse, jobId, files);
            Assert.assertNull(mav);

            // Lets decompose our zip stream to verify everything got written
            // correctly
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(outStream.getDataWritten()));
            byte[] buf = null;
            int dataRead = 0;

            // Entry 1
            ZipEntry entry1 = zis.getNextEntry();
            Assert.assertNotNull(entry1);
            Assert.assertEquals(fileKey1, entry1.getName());
            buf = new byte[file1Data.length];
            dataRead = zis.read(buf);
            Assert.assertEquals(buf.length, dataRead);
            Assert.assertArrayEquals(file1Data, buf);

            // Entry 2
            ZipEntry entry2 = zis.getNextEntry();
            Assert.assertNotNull(entry2);
            Assert.assertEquals(fileKey2, entry2.getName());
            buf = new byte[file2Data.length];
            dataRead = zis.read(buf);
            Assert.assertEquals(buf.length, dataRead);
            Assert.assertArrayEquals(file2Data, buf);

            // Entry 3
            ZipEntry entry3 = zis.getNextEntry();
            Assert.assertNotNull(entry3);
            Assert.assertEquals(fileKey3, entry3.getName());
            buf = new byte[file3Data.length];
            dataRead = zis.read(buf);
            Assert.assertEquals(buf.length, dataRead);
            Assert.assertArrayEquals(file3Data, buf);

            // And that should be it
            Assert.assertNull(zis.getNextEntry());
        }
    }

    /**
     * Tests that downloading multiple job files fails if user doesn't own job
     * @throws PortalServiceException 
     */
    @Test(expected=AccessDeniedException.class)
    public void testDownloadJobFilesNoPermission() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "anotheruser@email.com";
        final int jobId = 1234;
        final String files = "filekey1,filekey2";
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));
            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(jobEmail));
        }});

        //Returns null on success
        controller.downloadAsZip(mockRequest, mockResponse, jobId, files);
    }

    /**
     * Tests that downloading multiple job files fails if job DNE
     * @throws PortalServiceException 
     */
    @Test
    public void testDownloadJobFilesDNE() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final String files = "filekey1,filekey2";

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(null));
        }});

        //Returns null on success
        ModelAndView mav = controller.downloadAsZip(mockRequest, mockResponse, jobId, files);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that querying for a set of series returns correct values
     */
    @Test
    public void testQuerySeries() {
        final String userEmail = "exampleuser@email.com";
        final String qUser = "exampleuser@email.com";
        final String qName = "name";
        final String qDescription = "description";
        final List<VEGLSeries> series = Arrays.asList(
                context.mock(VEGLSeries.class, "mockSeries1"),
                context.mock(VEGLSeries.class, "mockSeries2"));

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).querySeries(qUser, qName, qDescription);will(returnValue(series));
        }});

        //Returns null on success
        ModelAndView mav = controller.querySeries(mockRequest, mockResponse, qName, qDescription);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertSame(series, mav.getModel().get("data"));
    }

    /**
     * Tests that querying for a set of series with no params filters via session email
     */
    @Test
    public void testQuerySeriesNoUser() {
        final String userEmail = "exampleuser@email.com";
        final String qName = null;
        final String qDescription = null;
        final List<VEGLSeries> series = Arrays.asList(
                context.mock(VEGLSeries.class, "mockSeries1"),
                context.mock(VEGLSeries.class, "mockSeries2"));

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).querySeries(userEmail, null, null);will(returnValue(series));
        }});

        //Returns null on success
        ModelAndView mav = controller.querySeries(mockRequest, mockResponse, qName, qDescription);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertSame(series, mav.getModel().get("data"));
    }

    public static VEGLSeriesMatcher aVEGLSeries(String user, String name, String description) {
        return new VEGLSeriesMatcher(user, name, description);
    }

    /**
     * Tests that creating a folder succeeds
     * @throws Exception
     */
    @Test
    public void testCreateFolder() {
        final String userEmail = "exampleuser@email.com";
        final String qName = "default";
        final String qDescription = "Everything will now come through to a single default series";

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));
            oneOf(mockJobManager).saveSeries(with(aVEGLSeries(userEmail, qName, qDescription)));
        }});

        ModelAndView mav = controller.createFolder(mockRequest, qName, qDescription);

        Assert.assertTrue((Boolean) mav.getModel().get("success"));

    }

    /**
     * Tests that listing a job succeeds
     * @throws PortalServiceException 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testListJobs() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        final List<VEGLJob> mockJobs = Arrays.asList(
                context.mock(VEGLJob.class, "mockJobActive"),
                context.mock(VEGLJob.class, "mockJobUnsubmitted"),
                context.mock(VEGLJob.class, "mockJobDone"),
                context.mock(VEGLJob.class, "mockJobPending")
                );

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(mockSeries));
            allowing(mockSeries).getUser();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesJobs(seriesId, mockPortalUser);will(returnValue(mockJobs));
        }});

        ModelAndView mav = controller.listJobs(mockRequest, mockResponse, seriesId, false);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertArrayEquals(mockJobs.toArray(), ((List<VEGLJob>) mav.getModel().get("data")).toArray());
    }

    /**
     * Tests that listing a job succeeds (as well as correctly updating job statuses)
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testListJobsWithStatusUpdate() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        final List<VEGLJob> mockJobs = Arrays.asList(
                context.mock(VEGLJob.class, "mockJobActive"),
                context.mock(VEGLJob.class, "mockJobUnsubmitted"),
                context.mock(VEGLJob.class, "mockJobDone"),
                context.mock(VEGLJob.class, "mockJobPending")
                );

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(mockSeries));
            allowing(mockSeries).getUser();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesJobs(seriesId, mockPortalUser);will(returnValue(mockJobs));

            oneOf(mockJobStatusMonitor).statusUpdate(mockJobs);
        }});

        ModelAndView mav = controller.listJobs(mockRequest, mockResponse, seriesId, true);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertArrayEquals(mockJobs.toArray(), ((List<VEGLJob>) mav.getModel().get("data")).toArray());
    }

    /**
     * Tests that listing a job fails when its the incorrect user
     * @throws PortalServiceException 
     * @throws Exception
     */
    @Test(expected=AccessDeniedException.class)
    public void testListJobsNoPermission() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final String seriesEmail = "anotheruser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(mockSeries));
            allowing(mockSeries).getUser();will(returnValue(seriesEmail));
        }});

        ModelAndView mav = controller.listJobs(mockRequest, mockResponse, seriesId, false);
    }

    /**
     * Tests that listing a job fails when its the incorrect user
     * @throws PortalServiceException 
     * @throws Exception
     */
    @Test
    public void testListJobsDNE() throws PortalServiceException {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId, userEmail);will(returnValue(null));
        }});

        ModelAndView mav = controller.listJobs(mockRequest, mockResponse, seriesId, false);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    @Test
    public void testDuplicateJob() throws Exception {
        final Integer jobId = 1234;
        final String userEmail = "exampleuser@email.com";
        final String[] files = new String[] {"file1.txt", "file2.txt"};
        final byte[] data1 = new byte[] {1,3,4};
        final byte[] data2 = new byte[] {2,9,3,4};
        final InputStream is1 = new ByteArrayInputStream(data1);
        final InputStream is2 = new ByteArrayInputStream(data2);
        final CloudFileInformation[] cloudFiles = new CloudFileInformation[] {
                new CloudFileInformation("long/key/file1.txt", data1.length, "http://example.org/file1"),
                new CloudFileInformation("long/key/file2.txt", data2.length, "http://example.org/file2"),
                new CloudFileInformation("long/key/file3.txt", 5L, "http://example.org/file3") //this will not be downloaded
        };

        final String baseKey = "base-key";
        VEGLJob existingJob = new VEGLJob();
        existingJob.setId(jobId);
        existingJob.setUser(userEmail);
        existingJob.setComputeServiceId(computeServiceId);
        existingJob.setStorageServiceId(storageServiceId);

        try (final ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
                final ByteArrayOutputStream bos2 = new ByteArrayOutputStream()) {

            context.checking(new Expectations() {
                {
                    allowing(mockPortalUser).getEmail();
                    will(returnValue(userEmail));

                    oneOf(mockJobManager).getJobById(jobId, mockPortalUser);
                    will(returnValue(existingJob));
                    allowing(mockJobManager).saveJob(with(aNonMatchingVeglJob(jobId)));

                    oneOf(mockFileStagingService).generateStageInDirectory(with(aNonMatchingVeglJob(jobId)));
                    oneOf(mockFileStagingService).writeFile(with(aNonMatchingVeglJob(jobId)),
                            with(cloudFiles[0].getName()));
                    will(returnValue(bos1));
                    oneOf(mockFileStagingService).writeFile(with(aNonMatchingVeglJob(jobId)),
                            with(cloudFiles[1].getName()));
                    will(returnValue(bos2));

                    oneOf(mockCloudStorageServices[0]).generateBaseKey(with(aNonMatchingVeglJob(jobId)));
                    will(returnValue(baseKey));
                    oneOf(mockCloudStorageServices[0]).listJobFiles(with(aVeglJob(jobId)));
                    will(returnValue(cloudFiles));
                    oneOf(mockCloudStorageServices[0]).getJobFile(with(aVeglJob(jobId)), with(cloudFiles[0].getName()));
                    will(returnValue(is1));
                    oneOf(mockCloudStorageServices[0]).getJobFile(with(aVeglJob(jobId)), with(cloudFiles[1].getName()));
                    will(returnValue(is2));

                    // We should have 1 call to our job manager to create a job
                    // audit trail record
                    oneOf(mockJobManager).createJobAuditTrail(with(aNull(String.class)), with(any(VEGLJob.class)),
                            with(any(String.class)));
                }
            });

            ModelAndView mav = controller.duplicateJob(mockRequest, mockResponse, jobId, files);
            Assert.assertTrue((Boolean) mav.getModel().get("success"));

            byte[] fis1Data = bos1.toByteArray();
            byte[] fis2Data = bos2.toByteArray();

            Assert.assertArrayEquals(data1, fis1Data);
            Assert.assertArrayEquals(data2, fis2Data);
        }
    }

    /**
     * Tests requesting instance logs in the best case scenario
     */
    @Test
    public void testGetRawInstanceLogs() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String consoleData = "console\ndata\n";

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(jobId));

            oneOf(mockCloudComputeServices[0]).getConsoleLog(with(mockJob), with(any(Integer.class)));
            will(returnValue(consoleData));
        }});

        ModelAndView mav = controller.getRawInstanceLogs(mockRequest, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(consoleData, mav.getModel().get("data"));
    }

    /**
     * Tests getting instance logs fails with bad service ID
     */
    @Test
    public void testGetRawInstanceLogs_BadService() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));
            allowing(mockJob).getComputeServiceId();will(returnValue("SERVICE-DNE"));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(jobId));

        }});

        ModelAndView mav = controller.getRawInstanceLogs(mockRequest, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests requesting instance logs fails gracefully when the underlying service throws an exception
     */
    @Test
    public void testGetRawInstanceLogs_UnableToRequest() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(jobId));

            oneOf(mockCloudComputeServices[0]).getConsoleLog(with(mockJob), with(any(Integer.class)));
            will(throwException(new PortalServiceException("error")));
        }});

        ModelAndView mav = controller.getRawInstanceLogs(mockRequest, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests requesting instance logs fails gracefully when the underlying service returns null
     */
    @Test
    public void testGetRawInstanceLogs_NullLogs() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(jobId));

            oneOf(mockCloudComputeServices[0]).getConsoleLog(with(mockJob), with(any(Integer.class)));
            will(returnValue(null));
        }});

        ModelAndView mav = controller.getRawInstanceLogs(mockRequest, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }
}

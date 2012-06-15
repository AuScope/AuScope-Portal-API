package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.test.jmock.ReadableServletOutputStream;
import org.auscope.portal.jmock.VEGLSeriesMatcher;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for JobListController
 * @author Josh Vote
 *
 */
public class TestJobListController {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private VEGLJobManager mockJobManager;
    private CloudStorageService mockCloudStorageService;
    private FileStagingService mockFileStagingService;
    private CloudComputeService mockCloudComputeService;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private HttpSession mockSession;
    private JobListController controller;

    /**
     * Load our mock objects
     */
    @Before
    public void init() {
        mockJobManager = context.mock(VEGLJobManager.class);
        mockCloudStorageService = context.mock(CloudStorageService.class);
        mockFileStagingService = context.mock(FileStagingService.class);
        mockCloudComputeService = context.mock(CloudComputeService.class);
        mockResponse = context.mock(HttpServletResponse.class);
        mockRequest = context.mock(HttpServletRequest.class);
        mockSession = context.mock(HttpSession.class);

        controller = new JobListController(mockJobManager, mockCloudStorageService, mockFileStagingService, mockCloudComputeService);


    }

    /**
     * Tests getting a series from the job manager
     */
    @Test
    public void testMySeries() {
        final String userEmail = "exampleuser@email.com";
        final VEGLSeries series = context.mock(VEGLSeries.class);
        final List<VEGLSeries> seriesList = Arrays.asList(series);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).querySeries(userEmail, null, null);will(returnValue(seriesList));
        }});

        ModelAndView mav = controller.mySeries(mockRequest, mockResponse);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests getting a series when there is no email address in the user's session
     */
    @Test
    public void testMySeriesNoEmail() {
        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(null));
        }});

        ModelAndView mav = controller.mySeries(mockRequest, mockResponse);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a job succesfully
     */
    @Test
    public void testDeleteJob() {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));


            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));

            //Make sure deletion occurs
            oneOf(mockFileStagingService).deleteStageInDirectory(mockJob);
            oneOf(mockJobManager).deleteJob(mockJob);
        }});

        ModelAndView mav = controller.deleteJob(mockRequest, mockResponse, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a job fails when its another users job
     */
    @Test
    public void testDeleteJobNoPermission() {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "adifferentuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));


            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(jobEmail));
        }});

        ModelAndView mav = controller.deleteJob(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a job fails when the jobID DNE
     */
    @Test
    public void testDeleteJobDNE() {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(null));
        }});

        ModelAndView mav = controller.deleteJob(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a series succesfully
     */
    @Test
    public void testDeleteSeries() {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;
        final List<VEGLJob> mockJobs = Arrays.asList(
                context.mock(VEGLJob.class, "mockJob1"),
                context.mock(VEGLJob.class, "mockJob2"));
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));
            allowing(mockSeries).getUser();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));
            oneOf(mockJobManager).getSeriesJobs(seriesId);will(returnValue(mockJobs));

            //Make sure job deletion occurs
            oneOf(mockFileStagingService).deleteStageInDirectory(mockJobs.get(0));
            oneOf(mockJobManager).deleteJob(mockJobs.get(0));
            oneOf(mockFileStagingService).deleteStageInDirectory(mockJobs.get(1));
            oneOf(mockJobManager).deleteJob(mockJobs.get(1));

            oneOf(mockJobManager).deleteSeries(mockSeries);
        }});

        ModelAndView mav = controller.deleteSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a series fails when the user doesnt have permission
     */
    @Test
    public void testDeleteSeriesNoPermission() {
        final String userEmail = "exampleuser@email.com";
        final String seriesEmail = "anotheruser@email.com";
        final int seriesId = 1234;
        final List<VEGLJob> mockJobs = Arrays.asList(
                context.mock(VEGLJob.class, "mockJob1"),
                context.mock(VEGLJob.class, "mockJob2"));
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));
            allowing(mockSeries).getUser();will(returnValue(seriesEmail));

            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));
            oneOf(mockJobManager).getSeriesJobs(seriesId);will(returnValue(mockJobs));
        }});

        ModelAndView mav = controller.deleteSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests deleting a series fails when series DNE
     */
    @Test
    public void testDeleteSeriesDNE() {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(null));
        }});

        ModelAndView mav = controller.deleteSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing a job succeeds
     */
    @Test
    public void testKillJob() {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);


        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            allowing(mockJob).getUser();will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));

            oneOf(mockCloudComputeService).terminateJob(mockJob);
            oneOf(mockJob).setStatus(GridSubmitController.STATUS_CANCELLED);
            oneOf(mockJobManager).saveJob(mockJob);
        }});

        ModelAndView mav = controller.killJob(mockRequest, mockResponse, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing a job fails when its not the user's job
     */
    @Test
    public void testKillJobNoPermission() {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "anotheruser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);


        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            allowing(mockJob).getUser();will(returnValue(jobEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
        }});

        ModelAndView mav = controller.killJob(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing a job fails when the job cannot be found
     */
    @Test
    public void testKillJobDNE() {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(null));
        }});

        ModelAndView mav = controller.killJob(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing all jobs of a series succeeds
     */
    @Test
    public void testKillSeriesJobs() {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        final List<VEGLJob> mockJobs = Arrays.asList(
                context.mock(VEGLJob.class, "mockJobDone"),
                context.mock(VEGLJob.class, "mockJobFailed"),
                context.mock(VEGLJob.class, "mockJobCancelled"),
                context.mock(VEGLJob.class, "mockJobActive"),
                context.mock(VEGLJob.class, "mockJobUnsubmitted"));


        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));


            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));
            allowing(mockSeries).getUser();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesJobs(seriesId);will(returnValue(mockJobs));

            //Each of our jobs is in a different status
            allowing(mockJobs.get(0)).getStatus();will(returnValue(GridSubmitController.STATUS_DONE));
            allowing(mockJobs.get(1)).getStatus();will(returnValue(GridSubmitController.STATUS_FAILED));
            allowing(mockJobs.get(2)).getStatus();will(returnValue(GridSubmitController.STATUS_CANCELLED));
            allowing(mockJobs.get(3)).getStatus();will(returnValue(GridSubmitController.STATUS_ACTIVE));
            allowing(mockJobs.get(4)).getStatus();will(returnValue(GridSubmitController.STATUS_UNSUBMITTED));
            allowing(mockJobs.get(0)).getId();will(returnValue(new Integer(0)));
            allowing(mockJobs.get(1)).getId();will(returnValue(new Integer(1)));
            allowing(mockJobs.get(2)).getId();will(returnValue(new Integer(2)));
            allowing(mockJobs.get(3)).getId();will(returnValue(new Integer(3)));
            allowing(mockJobs.get(4)).getId();will(returnValue(new Integer(4)));

            //Only the active job will be cancelled
            oneOf(mockCloudComputeService).terminateJob(mockJobs.get(3));
            oneOf(mockJobs.get(3)).setStatus(GridSubmitController.STATUS_CANCELLED);
            oneOf(mockJobManager).saveJob(mockJobs.get(3));
        }});

        ModelAndView mav = controller.killSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing all jobs of a series fails when the user lacks permission
     */
    @Test
    public void testKillSeriesJobsNoPermission() {
        final String userEmail = "exampleuser@email.com";
        final String seriesEmail = "anotheruser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);



        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));
            allowing(mockSeries).getUser();will(returnValue(seriesEmail));
        }});

        ModelAndView mav = controller.killSeriesJobs(mockRequest, mockResponse, seriesId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that killing all jobs of a series fails when the user lacks permission
     */
    @Test
    public void testKillSeriesJobsDNE() {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(null));
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
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));

            oneOf(mockCloudStorageService).listJobFiles(mockJob);will(returnValue(fileDetails));
        }});

        ModelAndView mav = controller.jobFiles(mockRequest, mockResponse, jobId);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertSame(fileDetails, mav.getModel().get("data"));
    }

    /**
     * tests listing job files fails if the user doesnt have permission
     */
    @Test
    public void testListJobFilesNoPermission() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "anotheruser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(jobEmail));

        }});

        ModelAndView mav = controller.jobFiles(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * tests listing job files fails if the user doesnt have permission
     */
    @Test
    public void testListJobFilesDNE() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(null));

        }});

        ModelAndView mav = controller.jobFiles(mockRequest, mockResponse, jobId);
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
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));

            oneOf(mockCloudStorageService).listJobFiles(mockJob);will(throwException(new PortalServiceException("")));
        }});

        ModelAndView mav = controller.jobFiles(mockRequest, mockResponse, jobId);
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
        final ReadableServletOutputStream outStream = new ReadableServletOutputStream();

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));

            oneOf(mockCloudStorageService).getJobFile(mockJob, key);will(returnValue(inputStream));

            //Ensure our response stream gets written to
            oneOf(mockResponse).setContentType("application/octet-stream");
            allowing(mockResponse).setHeader(with(any(String.class)), with(any(String.class)));
            oneOf(mockResponse).getOutputStream();will(returnValue(outStream));
        }});

        //Returns null on success
        ModelAndView mav = controller.downloadFile(mockRequest, mockResponse, jobId, fileName, key);
        Assert.assertNull(mav);

        Assert.assertArrayEquals(data, outStream.getDataWritten());
    }

    /**
     * Tests that downloading a single job file fails when the user doesnt own the job
     */
    @Test
    public void testDownloadJobFileNoPermission() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "anotheruser@email.com";
        final int jobId = 1234;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String key = "my/file/key";
        final String fileName = "fileName.txt";

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(jobEmail));
        }});

        //Returns null on success
        ModelAndView mav = controller.downloadFile(mockRequest, mockResponse, jobId, fileName, key);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that downloading a single job file fails when the job DNE
     */
    @Test
    public void testDownloadJobFileDNE() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final String key = "my/file/key";
        final String fileName = "fileName.txt";

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(null));
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
        final ReadableServletOutputStream outStream = new ReadableServletOutputStream();

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(userEmail));

            oneOf(mockCloudStorageService).getJobFile(mockJob, fileKey1);will(returnValue(is1));
            oneOf(mockCloudStorageService).getJobFile(mockJob, fileKey2);will(returnValue(is2));
            oneOf(mockCloudStorageService).getJobFile(mockJob, fileKey3);will(returnValue(is3));

            //Ensure our response stream gets written to
            oneOf(mockResponse).setContentType("application/zip");
            allowing(mockResponse).setHeader(with(any(String.class)), with(any(String.class)));
            oneOf(mockResponse).getOutputStream();will(returnValue(outStream));
        }});

        //Returns null on success
        ModelAndView mav = controller.downloadAsZip(mockRequest, mockResponse, jobId, files);
        Assert.assertNull(mav);

        //Lets decompose our zip stream to verify everything got written correctly
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(outStream.getDataWritten()));
        byte[] buf = null;
        int dataRead = 0;

        //Entry 1
        ZipEntry entry1 = zis.getNextEntry();
        Assert.assertNotNull(entry1);
        Assert.assertEquals(fileKey1, entry1.getName());
        buf = new byte[file1Data.length];
        dataRead = zis.read(buf);
        Assert.assertEquals(buf.length, dataRead);
        Assert.assertArrayEquals(file1Data, buf);

        //Entry 2
        ZipEntry entry2 = zis.getNextEntry();
        Assert.assertNotNull(entry2);
        Assert.assertEquals(fileKey2, entry2.getName());
        buf = new byte[file2Data.length];
        dataRead = zis.read(buf);
        Assert.assertEquals(buf.length, dataRead);
        Assert.assertArrayEquals(file2Data, buf);

        //Entry 3
        ZipEntry entry3 = zis.getNextEntry();
        Assert.assertNotNull(entry3);
        Assert.assertEquals(fileKey3, entry3.getName());
        buf = new byte[file3Data.length];
        dataRead = zis.read(buf);
        Assert.assertEquals(buf.length, dataRead);
        Assert.assertArrayEquals(file3Data, buf);

        //And that should be it
        Assert.assertNull(zis.getNextEntry());
    }

    /**
     * Tests that downloading multiple job files fails if user doesn't own job
     */
    @Test
    public void testDownloadJobFilesNoPermission() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final String jobEmail = "anotheruser@email.com";
        final int jobId = 1234;
        final String files = "filekey1,filekey2";
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getUser();will(returnValue(jobEmail));
        }});

        //Returns null on success
        ModelAndView mav = controller.downloadAsZip(mockRequest, mockResponse, jobId, files);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that downloading multiple job files fails if job DNE
     */
    @Test
    public void testDownloadJobFilesDNE() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int jobId = 1234;
        final String files = "filekey1,filekey2";

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getJobById(jobId);will(returnValue(null));
        }});

        //Returns null on success
        ModelAndView mav = controller.downloadAsZip(mockRequest, mockResponse, jobId, files);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that querying for a set of series returns correct values
     */
    @Test
    public void testQuerySeries() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final String qUser = "user";
        final String qName = "name";
        final String qDescription = "description";
        final List<VEGLSeries> series = Arrays.asList(
                context.mock(VEGLSeries.class, "mockSeries1"),
                context.mock(VEGLSeries.class, "mockSeries2"));

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).querySeries(qUser, qName, qDescription);will(returnValue(series));
        }});

        //Returns null on success
        ModelAndView mav = controller.querySeries(mockRequest, mockResponse, qUser, qName, qDescription);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertSame(series, mav.getModel().get("data"));
    }

    /**
     * Tests that querying for a set of series with no params filters via session email
     */
    @Test
    public void testQuerySeriesNoUser() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final String qUser = null;
        final String qName = null;
        final String qDescription = null;
        final List<VEGLSeries> series = Arrays.asList(
                context.mock(VEGLSeries.class, "mockSeries1"),
                context.mock(VEGLSeries.class, "mockSeries2"));

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).querySeries(userEmail, null, null);will(returnValue(series));
        }});

        //Returns null on success
        ModelAndView mav = controller.querySeries(mockRequest, mockResponse, qUser, qName, qDescription);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertSame(series, mav.getModel().get("data"));
    }

    public static VEGLSeriesMatcher aVEGLSeries(String user, String name, String description) {
        return new VEGLSeriesMatcher(user, name, description);
    }

    /**
     * Tests that creating a series succeeds
     * @throws Exception
     */
    @Test
    public void testCreateSeries() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final String qName = "name";
        final String qDescription = "description";

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).saveSeries(with(aVEGLSeries(userEmail, qName, qDescription)));
        }});

        //Returns MAV on failure
        ModelAndView mav = controller.createSeries(mockRequest, qName, qDescription);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        VEGLSeries actualSeries = (VEGLSeries) mav.getModel().get("data");
        Assert.assertNotNull(actualSeries);
        Assert.assertEquals(userEmail, actualSeries.getUser());
        Assert.assertEquals(qName, actualSeries.getName());
        Assert.assertEquals(qDescription, actualSeries.getDescription());
    }

    /**
     * Simple extension to DataAccessException to allow simulating our own Hibernate errors
     * @author Josh Vote
     *
     */
    private class MyDataAccessException extends DataAccessException {
        public MyDataAccessException() {
            super("");
        }
    }

    /**
     * Tests that creating a series will fail if the underlying database fails
     * @throws Exception
     */
    @Test
    public void testCreateSeriesFailure() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final String qName = "name";
        final String qDescription = "description";

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).saveSeries(with(aVEGLSeries(userEmail, qName, qDescription)));will(throwException(new MyDataAccessException()));
        }});

        //Returns MAV on failure
        ModelAndView mav = controller.createSeries(mockRequest, qName, qDescription);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that listing a job succeeds (as well as correctly updating job statuses)
     * @throws Exception
     */
    @Test
    public void testListJobs() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        final List<VEGLJob> mockJobs = Arrays.asList(
                context.mock(VEGLJob.class, "mockJobActive"),
                context.mock(VEGLJob.class, "mockJobUnsubmitted"),
                context.mock(VEGLJob.class, "mockJobDone"));
        final CloudFileInformation[] jobActiveFiles = new CloudFileInformation[] {
                new CloudFileInformation("key2/filename", 100L, "http://public.url2/filename"),
                new CloudFileInformation("key2/vegl.sh.log", 101L, "http://public.url2/vegl.sh.log"),
                new CloudFileInformation("key2/filename3", 102L, "http://public.url2/filename3"),
        };
        final CloudFileInformation[] jobDoneFiles = new CloudFileInformation[] {
                new CloudFileInformation("key3/filename", 100L, "http://public.url3/filename"),
                new CloudFileInformation("key3/filename2", 101L, "http://public.url3/filename2"),
                new CloudFileInformation("key3/filename3", 102L, "http://public.url3/filename3"),
        };

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));
            allowing(mockSeries).getUser();will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesJobs(seriesId);will(returnValue(mockJobs));

            //Different job statuses are treated differently
            allowing(mockJobs.get(0)).getStatus();will(returnValue(GridSubmitController.STATUS_ACTIVE));
            allowing(mockJobs.get(1)).getStatus();will(returnValue(GridSubmitController.STATUS_UNSUBMITTED));
            allowing(mockJobs.get(2)).getStatus();will(returnValue(GridSubmitController.STATUS_DONE));

            //Output files for each job
            oneOf(mockCloudStorageService).listJobFiles(with(mockJobs.get(0)));will(returnValue(jobActiveFiles));
            oneOf(mockCloudStorageService).listJobFiles(with(mockJobs.get(2)));will(returnValue(jobDoneFiles));

            //Update our running job to done (due to presence of vegl.sh.log)
            oneOf(mockJobs.get(0)).setStatus(GridSubmitController.STATUS_DONE);
            oneOf(mockJobManager).saveJob(mockJobs.get(0));
        }});

        ModelAndView mav = controller.listJobs(mockRequest, mockResponse, seriesId);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
        Assert.assertArrayEquals(mockJobs.toArray(), ((List<VEGLJob>) mav.getModel().get("data")).toArray());
    }

    /**
     * Tests that listing a job fails when its the incorrect user
     * @throws Exception
     */
    @Test
    public void testListJobsNoPermission() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final String seriesEmail = "anotheruser@email.com";
        final int seriesId = 1234;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(mockSeries));
            allowing(mockSeries).getUser();will(returnValue(seriesEmail));
        }});

        ModelAndView mav = controller.listJobs(mockRequest, mockResponse, seriesId);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that listing a job fails when its the incorrect user
     * @throws Exception
     */
    @Test
    public void testListJobsDNE() throws Exception {
        final String userEmail = "exampleuser@email.com";
        final int seriesId = 1234;

        context.checking(new Expectations() {{
            allowing(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(userEmail));

            oneOf(mockJobManager).getSeriesById(seriesId);will(returnValue(null));
        }});

        ModelAndView mav = controller.listJobs(mockRequest, mockResponse, seriesId);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }
}

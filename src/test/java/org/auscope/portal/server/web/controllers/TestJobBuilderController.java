package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.cloud.StagedFile;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.auscope.portal.server.vegl.VGLPollingJobQueueManager;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.vegl.VglMachineImage;
import org.auscope.portal.server.vegl.VglParameter;
import org.auscope.portal.server.vegl.mail.JobMailSender;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ANVGLFileStagingService;
import org.auscope.portal.server.web.service.ANVGLProvenanceService;
import org.auscope.portal.server.web.service.ScmEntryService;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler;
import org.auscope.portal.server.web.service.scm.Solution;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for JobBuilderController
 * @author Josh Vote
 *
 */
public class TestJobBuilderController {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private VEGLJobManager mockJobManager;
    private CloudStorageService[] mockCloudStorageServices;
    private CloudComputeService[] mockCloudComputeServices;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private HttpSession mockSession;
    private ANVGLUser mockPortalUser;
    private VGLPollingJobQueueManager vglPollingJobQueueManager;
    private VGLJobStatusChangeHandler vglJobStatusChangeHandler;
    private ANVGLProvenanceService mockAnvglProvenanceService;
    private ANVGLFileStagingService mockFileStagingService;

    private JobMailSender mockJobMailSender;
    private VGLJobStatusAndLogReader mockVGLJobStatusAndLogReader;
    private ScmEntryService mockScmEntryService;

    private JobBuilderController controller;
    private final String vmSh = "http://example2.org";
    private final String vmShutdownSh = "http://example2.org";

    private ANVGLUser user;
    private VEGLJob job;
    private final String jobId = "123";
    private final String userId = "456";
    private final String seriesId = "789";
    private VEGLJob mockJob;
    private VEGLSeries mockSeries;

    @Before
    public void init() {
        //Mock objects required for Object Under Test
        mockJobManager = context.mock(VEGLJobManager.class);
        mockFileStagingService = context.mock(ANVGLFileStagingService.class);
        mockPortalUser = context.mock(ANVGLUser.class);
        mockCloudStorageServices = new CloudStorageService[] {context.mock(CloudStorageService.class)};
        mockCloudComputeServices = new CloudComputeService[] {context.mock(CloudComputeService.class)};
        mockRequest = context.mock(HttpServletRequest.class);
        mockResponse = context.mock(HttpServletResponse.class);
        mockSession = context.mock(HttpSession.class);

        mockJobMailSender = context.mock(JobMailSender.class);
        mockVGLJobStatusAndLogReader = context.mock(VGLJobStatusAndLogReader.class);

        mockAnvglProvenanceService = context.mock(ANVGLProvenanceService.class);
        mockScmEntryService = context.mock(ScmEntryService.class);

        vglJobStatusChangeHandler = new VGLJobStatusChangeHandler(mockJobManager,mockJobMailSender,mockVGLJobStatusAndLogReader, mockAnvglProvenanceService);
        vglPollingJobQueueManager = new VGLPollingJobQueueManager();

        mockJob = context.mock(VEGLJob.class);
        mockSeries = context.mock(VEGLSeries.class);

        //Object Under Test

        controller =
            new JobBuilderController("dummy@dummy.com",
                                     "http://example.org/scm/toolbox/42",
                                     mockJobManager,
                                     mockFileStagingService,
                                     vmSh,
                                     vmShutdownSh,
                                     mockCloudStorageServices,
                                     mockCloudComputeServices,
                                     vglJobStatusChangeHandler,
                                     vglPollingJobQueueManager,
                                     mockScmEntryService,
                                     mockAnvglProvenanceService);

        user = new ANVGLUser();
        user.setEmail("user@example.com");
        user.setId(userId);
        job = new VEGLJob(Integer.parseInt(jobId));
        job.setEmailAddress("user@example.com");
        job.setUser("user@example.com");
        context.checking(new Expectations() {{
            allowing(mockJob).getId();will(returnValue(job.getUser()));
            allowing(mockJob).getUser();will(returnValue(job.getEmailAddress()));
            allowing(mockJob).getEmailAddress();will(returnValue(job.getEmailAddress()));
            allowing(mockPortalUser).getId();will(returnValue(userId));
            allowing(mockPortalUser).getEmail();will(returnValue(user.getEmail()));
            allowing(mockSeries).getUser();will(returnValue(job.getEmailAddress()));
            allowing(mockSeries).getId();will(returnValue(Integer.parseInt(seriesId)));
        }});
    }

    @After
    public void destroy(){
        vglPollingJobQueueManager.getQueue().clear();
    }


    /**
     * Tests that retrieving job object succeeds.
     */
    @Test
    public void testGetJobObject() {
        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(returnValue(job));
        }});

        ModelAndView mav = controller.getJobObject(jobId, user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertNotNull(mav.getModel().get("data"));
    }

    /**
     * Tests that retrieving job object fails when the
     * underlying job manager's job query service fails.
     */
    @Test
    public void testGetJobObject_Exception() {
        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.getJobObject(jobId, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that the retrieving of a list of job file object
     * succeeds.
     * @throws Exception
     */
    @Test
    public void testListStagedJobFiles() throws Exception {
        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] mockStageFiles = new StagedFile[] {
                new StagedFile(job, "mockFile1", mockFile1),
                new StagedFile(job, "mockFile2", mockFile2) };


        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(returnValue(job));

            //We should have a call to file staging service to get our files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(job);
            will(returnValue(mockStageFiles));

            oneOf(mockFile1).length();will(returnValue(512L));
            oneOf(mockFile2).length();will(returnValue(512L));
        }});

        ModelAndView mav = controller.stagedJobFiles(jobId, user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertNotNull(mav.getModel().get("data"));
    }

    /**
     * Tests that the retrieving of job files fails
     * when the job cannot be found.
     */
    @Test
    public void testListStagedJobFiles_JobNotFound() {
        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.stagedJobFiles(jobId, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that the retrieving of job files fails
     * when the underlying file staging service fails.
     * @throws Exception
     */
    @Test
    public void testListStagedJobFiles_Exception() throws Exception {
        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(returnValue(job));
            //We should have a call to file staging service to get our files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(job);
            will(throwException(new PortalServiceException("test exception","test exception")));
        }});

        ModelAndView mav = controller.stagedJobFiles(jobId, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that the downloading of job file fails when
     * the underlying file staging service's file download handler fails.
     * @throws Exception
     */
    @Test
    public void testDownloadFile() throws Exception {
        final String filename = "test.py";
        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(with(equal(job.getId())), with(same(user)));
            will(returnValue(job));
            //We should have a call to file staging service to download a file
            oneOf(mockFileStagingService).handleFileDownload(job, filename, mockResponse);
        }});

        ModelAndView mav = controller.downloadFile(mockRequest, mockResponse, jobId, filename, user);
        Assert.assertNull(mav);
    }

    /**
     * Tests that the deleting of given job files succeeds.
     */
    @Test
    public void testDeleteFiles() {
        final String file1 = "file1.txt";
        final String file2 = "file2.txt";
        final String[] filenames = new String[] { file1, file2 };


        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(with(equal(job.getId())), with(same(user)));
            will(returnValue(job));
            //We should have calls to file staging service to delete files in staging dir
            oneOf(mockFileStagingService).deleteStageInFile(job, file1);
            will(returnValue(true));
            oneOf(mockFileStagingService).deleteStageInFile(job, file2);
            will(returnValue(true));
        }});

        ModelAndView mav = controller.deleteFiles(jobId, filenames, user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that the deleting of given job files fails
     * when the job cannot be found.
     */
    @Test
    public void testDeleteFiles_JobNotFoundException() {
        final String file1 = "file1.txt";
        final String file2 = "file2.txt";
        final String[] filenames = new String[] { file1, file2 };

        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.deleteFiles(jobId, filenames, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that the deleting of a given job's download objects succeeds.
     */
    @Test
    public void testDeleteDownloads() {
        int downloadId1 = 13579;
        int downloadId2 = 23480;
        final Integer[] downloadIds = new Integer[] { downloadId1, downloadId2 };
        VglDownload vglDownload1 = new VglDownload(downloadId1);
        VglDownload vglDownload2 = new VglDownload(downloadId2);
        VglDownload[] vglDownloads = new VglDownload[] { vglDownload1, vglDownload2 };
        final List<VglDownload> downloadList = new ArrayList<VglDownload>(Arrays.asList(vglDownloads));
        job.setJobDownloads(downloadList);


        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(job.getId(), user);
            will(returnValue(job));
            //We should have a call to our job manager to save our job object
            oneOf(mockJobManager).saveJob(job);
        }});

        ModelAndView mav = controller.deleteDownloads(jobId, downloadIds, user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that the deleting of a given job's download objects fails
     * when job saving fails.
     */
    @Test
    public void testDeleteDownloads_SaveJobException() {
        int downloadId1 = 13579;
        int downloadId2 = 23480;
        final Integer[] downloadIds = new Integer[] { downloadId1, downloadId2 };
        VglDownload vglDownload1 = new VglDownload(downloadId1);
        VglDownload vglDownload2 = new VglDownload(downloadId2);
        VglDownload[] vglDownloads = new VglDownload[] { vglDownload1, vglDownload2 };
        final List<VglDownload> downloadList = new ArrayList<VglDownload>(Arrays.asList(vglDownloads));
        job.setJobDownloads(downloadList);


        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(job.getId(), user);
            will(returnValue(job));
            //We should have a call to our job manager to save our job object
            oneOf(mockJobManager).saveJob(job);
            will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.deleteDownloads(jobId, downloadIds, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that the deleting of a given job's download objects fails
     * when the job cannot be found.
     */
    @Test
    public void testDeleteDownloads_JobNotFoundException() {
        int downloadId1 = 13579;
        int downloadId2 = 23480;
        final Integer[] downloadIds = new Integer[] { downloadId1, downloadId2 };


        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.deleteDownloads(jobId, downloadIds, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that file uploading for a given job succeeds.
     * @throws Exception
     */
    @Test
    public void testUploadFile() throws Exception {

        final MultipartHttpServletRequest mockMultipartRequest = context.mock(MultipartHttpServletRequest.class);
        final File mockFile = context.mock(File.class);
        final StagedFile mockStagedFile = new StagedFile(job, "mockFile", mockFile);


        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(job.getId(), user);
            will(returnValue(job));

            //We should have a call to file staging service to update a file
            oneOf(mockFileStagingService).handleFileUpload(job, mockMultipartRequest);
            will(returnValue(mockStagedFile));

            oneOf(mockFile).length();
            will(returnValue(1024L));
        }});

        ModelAndView mav = controller.uploadFile(mockMultipartRequest, mockResponse, job.getId().toString(), user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that file uploading for a given job fails
     * when the underlying file staging file upload handler fails.
     * @throws Exception
     */
    @Test
    public void testUploadFile_FileUploadException() throws Exception {

        final MultipartHttpServletRequest mockMultipartRequest = context.mock(MultipartHttpServletRequest.class);


        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(job.getId(), user);
            will(returnValue(job));

            //We should have a call to file staging service to update a file
            oneOf(mockFileStagingService).handleFileUpload(job, mockMultipartRequest);
            will(throwException(new PortalServiceException("Test Exception","Test Exception")));
        }});

        ModelAndView mav = controller.uploadFile(mockMultipartRequest, mockResponse, job.getId().toString(), user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that file uploading for a given job fails
     * when the given job cannot be found.
     * @throws Exception
     */
    @Test
    public void testUploadFile_JobNotFoundException() throws Exception {

        final MultipartHttpServletRequest mockMultipartRequest = context.mock(MultipartHttpServletRequest.class);


        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(job.getId(), user);
            will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.uploadFile(mockMultipartRequest, mockResponse, job.getId().toString(), user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that retrieving of a given job's download objects succeeds.
     */
    @Test
    public void testGetJobDownloads() {
        final VglDownload[] existingDownloads = new VglDownload[] { new VglDownload(12356) };
        final List<VglDownload> downloadList = new ArrayList<VglDownload>(Arrays.asList(existingDownloads));


        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(returnValue(mockJob));
            //We should have a call to job object to get a list of download objects
            oneOf(mockJob).getJobDownloads();
            will(returnValue(downloadList));
        }});

        ModelAndView mav = controller.getJobDownloads(Integer.parseInt(jobId), user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertNotNull(mav.getModel().get("data"));
    }

    /**
     * Tests that retrieving of a given job's download objects fails
     * when the given job cannot be found.
     */
    @Test
    public void testGetJobDownloads_Exception() {
        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.getJobDownloads(Integer.parseInt(jobId), user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that the getting of job status succeeds.
     */
    @Test
    public void testGetJobStatus() {
        final String expectedStatus = "Pending";

        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(returnValue(mockJob));

            oneOf(mockJob).getStatus();
            will(returnValue(expectedStatus));

            allowing(mockJob).getEmailAddress();
            will(returnValue(job.getEmailAddress()));
        }});

        ModelAndView mav = controller.getJobStatus(jobId, user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        String jobStatus = (String)mav.getModel().get("data");
        Assert.assertEquals(expectedStatus, jobStatus);
    }

    /**
     * Tests that the retrieving of job status fails
     * when the job cannot be found.
     */
    @Test
    public void testGetJobStatus_JobNotFound() {
        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.getJobStatus(jobId, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that cancelling submission succeeds.
     * @throws Exception
     */
    @Test
    public void testCancelSubmission() throws Exception {
        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(returnValue(job));
            //We should have a call to file staging service to get our files
            oneOf(mockFileStagingService).deleteStageInDirectory(job);
            will(returnValue(true));
        }});

        ModelAndView mav = controller.cancelSubmission(jobId, user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that cancelling job submission fails when the
     * job cannot be found.
     * @throws Exception
     */
    @Test
    public void testCancelSubmission_Exception() throws Exception {

        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);
            will(throwException(new Exception()));
        }});

        ModelAndView mav = controller.cancelSubmission(jobId, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertNull(mav.getModel().get("data"));
    }

    /**
     * Tests that job submission correctly interacts with all dependencies
     * @throws Exception
     */
    @Test
    public void testJobSubmission() throws Exception {
        //Instantiate our job object

        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(job, "mockFile1", mockFile1), new StagedFile(job, "mockFile2", mockFile2)};
        final String computeVmId = "compute-vmi-id";
        final String computeServiceId = "compute-service-id";
        final String instanceId = "new-instance-id";
        final String computeKeyName = "key-name";
        final Sequence jobFileSequence = context.sequence("jobFileSequence"); //this makes sure we aren't deleting directories before uploading (and other nonsense)
        final OutputStream mockOutputStream = context.mock(OutputStream.class);
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final String storageBucket = "storage-bucket";
        final String storageAccess = "213-asd-54";
        final String storageSecret = "tops3cret";
        final String storageServiceId = "storageid";
        final String storageEndpoint = "http://example.org";
        final String storageProvider = "provider";
        final String storageAuthVersion = "1.2.3";
        final String regionName = null;

        final String mockUser = "jo@me.com";
        final URI mockProfileUrl = new URI("https://plus.google.com/1");
        final File activityFile = File.createTempFile("activity", ".ttl");
        final String activityFileName = "activity.ttl";
        final CloudFileInformation cloudFileInformation = new CloudFileInformation("one", 0, "");
        CloudFileInformation cloudFileModel = new CloudFileInformation("two", 0, "");
        final CloudFileInformation[] cloudList = {cloudFileInformation, cloudFileModel};

        final Solution mockSolution = context.mock(Solution.class);
        final Set<Solution> solutions = new HashSet<Solution>();
        solutions.add(mockSolution);

        job.setComputeVmId(computeVmId);
        job.setStatus(jobInSavedState); // by default, the job is in SAVED state
        job.setStorageBaseKey("base/key");
        job.setComputeServiceId(computeServiceId);
        job.setStorageServiceId(storageServiceId);
        job.setStorageBucket(storageBucket);

        context.checking(new Expectations() {{
            oneOf(mockScmEntryService).getJobSolutions(job);will(returnValue(solutions));
            oneOf(mockSolution).getUri();will(returnValue("http://sssc.vhirl.org/solution1"));
            oneOf(mockSolution).getDescription();will(returnValue("A Fake Solution"));
            oneOf(mockSolution).getName();will(returnValue("FakeSol"));
            oneOf(mockSolution).getCreatedAt();will(returnValue(new Date()));

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));
            oneOf(mockImages[0]).getPermissions();will(returnValue(new String[] {"testRole2"}));
            allowing(mockRequest).isUserInRole("testRole2");will(returnValue(true));

            //We should have 1 call to our job manager to get our job object and 1 call to save it
            //oneOf(mockJobManager).getJobById(job.getId(), user);will(returnValue(job));
            oneOf(mockJobManager).getJobById(job.getId(), mockPortalUser);will(returnValue(job));
            oneOf(mockJobManager).saveJob(job);

            oneOf(mockFileStagingService).writeFile(job, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(mockOutputStream));
            allowing(mockOutputStream).close();

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(job);will(returnValue(stageInFiles));
            inSequence(jobFileSequence);

            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudStorageServices[0]).getAccessKey();will(returnValue(storageAccess));
            allowing(mockCloudStorageServices[0]).getSecretKey();will(returnValue(storageSecret));
            allowing(mockCloudStorageServices[0]).getProvider();will(returnValue(storageProvider));
            allowing(mockCloudStorageServices[0]).getProvider();will(returnValue(storageProvider));
            allowing(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            allowing(mockCloudStorageServices[0]).getEndpoint();will(returnValue(storageEndpoint));
            allowing(mockCloudStorageServices[0]).getProvider();will(returnValue(storageProvider));
            allowing(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            allowing(mockCloudStorageServices[0]).getRegionName();will(returnValue(regionName));

            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));

            //We should have 1 call to upload them
            oneOf(mockCloudStorageServices[0]).uploadJobFiles(with(equal(job)), with(equal(new File[] {mockFile1, mockFile2})));
            inSequence(jobFileSequence);

            //And finally 1 call to execute the job
            oneOf(mockCloudComputeServices[0]).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(returnValue(instanceId));

            oneOf(mockJobManager).saveJob(job);

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, job, "Set job to provisioning");
            oneOf(mockJobManager).createJobAuditTrail("Provisioning", job, "Set job to Pending");

            oneOf(mockRequest).getRequestURL();will(returnValue(new StringBuffer("http://mock.fake/secure/something")));
            oneOf(mockCloudStorageServices[0]).listJobFiles(with(equal(job)));will(returnValue(cloudList));
            allowing(mockFileStagingService).createLocalFile(activityFileName, job);will(returnValue(activityFile));
            allowing(mockCloudStorageServices[0]).uploadJobFiles(with(any(VEGLJob.class)), with(any(File[].class)));

            oneOf(mockPortalUser).getUsername();will(returnValue(mockUser));
            allowing(mockPortalUser).getEmail();will(returnValue(user.getEmail()));
            oneOf(mockPortalUser).getAwsKeyName();will(returnValue(computeKeyName));
            allowing(mockPortalUser).getId();will(returnValue(mockUser));
            oneOf(mockAnvglProvenanceService).createActivity(job, solutions, mockPortalUser);will(returnValue(""));

            allowing(mockAnvglProvenanceService).setServerURL("http://mock.fake/secure/something");
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, job.getId().toString(), mockPortalUser);

        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Thread.sleep(1000);
        Assert.assertEquals(instanceId, job.getComputeInstanceId());
        Assert.assertEquals(JobBuilderController.STATUS_PENDING, job.getStatus());
        Assert.assertNotNull(job.getSubmitDate());
    }

    /**
     * Tests that job submission fails correctly when user doesn't have permission to use
     * the VMI.
     */
    @Test
    public void testJobSubmission_PermissionDenied() throws Exception {
        //Instantiate our job object

        final String computeServiceId = "ccsid";
        final String injectedComputeVmId = "injected-compute-vmi-id";
        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(job, "mockFile1", mockFile1), new StagedFile(job, "mockFile2", mockFile2)};
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;
        final OutputStream mockOutputStream = context.mock(OutputStream.class);
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final String errorDescription = "You do not have the permission to submit this job for processing.";
        final String storageServiceId = "cssid";

        job.setComputeVmId(injectedComputeVmId);
        job.setStatus(jobInSavedState); // by default, the job is in SAVED state
        job.setComputeServiceId(computeServiceId);
        job.setStorageServiceId(storageServiceId);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            //oneOf(mockJobManager).getJobById(job.getId(), user);will(returnValue(job));
            oneOf(mockJobManager).getJobById(job.getId(), mockPortalUser);will(returnValue(job));
            oneOf(mockJobManager).saveJob(job);

            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            allowing(mockRequest).isUserInRole("a-different-role");will(returnValue(false));
            oneOf(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));
            oneOf(mockImages[0]).getPermissions();will(returnValue(new String[] {"a-different-role"}));

            oneOf(mockFileStagingService).writeFile(job, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(mockOutputStream));
            allowing(mockOutputStream).close();

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(job);will(returnValue(stageInFiles));

            //And one call to upload them (which we will mock as failing)
            oneOf(mockCloudStorageServices[0]).uploadJobFiles(with(equal(job)), with(any(File[].class)));will(throwException(new PortalServiceException("")));

            //We should have 1 call to our job manager to create a job audit trail record
            //oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, job, errorDescription);
            oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, job, "");
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, job.getId().toString(), mockPortalUser);

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_UNSUBMITTED, job.getStatus());
    }

    /**
     * Tests that job submission fails correctly when the job doesn't exist
     * @throws Exception
     */
    @Test
    public void testJobSubmission_JobDNE() throws Exception {
        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), mockPortalUser);will(returnValue(null));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobId, mockPortalUser);

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that job submission fails correctly when files cannot be uploaded to S3
     * @throws Exception
     */
    @Test
    public void testJobSubmission_S3Failure() throws Exception {
        //Instantiate our job object
        final String computeVmId = "compute-vmi-id";
        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(job, "mockFile1", mockFile1), new StagedFile(job, "mockFile2", mockFile2)};
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final String computeServiceId = "id-1";
        final String storageServiceId = "id-2";
        job.setComputeVmId(computeVmId);
        job.setStatus(jobInSavedState); // by default, the job is in SAVED state
        job.setComputeServiceId(computeServiceId);
        job.setStorageServiceId(storageServiceId);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object
            //oneOf(mockJobManager).getJobById(job.getId(), user);will(returnValue(job));
            oneOf(mockJobManager).getJobById(job.getId(), mockPortalUser);will(returnValue(job));

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));
            oneOf(mockImages[0]).getPermissions();will(returnValue(new String[] {"testRole1"}));
            allowing(mockRequest).isUserInRole("testRole1");will(returnValue(true));


            oneOf(mockFileStagingService).writeFile(job, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(bos));

            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(job);will(returnValue(stageInFiles));

            //And one call to upload them (which we will mock as failing)
            oneOf(mockCloudStorageServices[0]).uploadJobFiles(with(equal(job)), with(any(File[].class)));will(throwException(new PortalServiceException("")));

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, job, "");
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, job.getId().toString(), mockPortalUser);

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_UNSUBMITTED, job.getStatus());
    }

    /**
     * Tests that job submission fails correctly when files cannot be uploaded to S3
     * @throws Exception
     */
    @Test
    public void testJobSubmission_ExecuteFailure() throws Exception {
        //Instantiate our job object
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;

        final String computeVmId = "compute-vmi-id";
        final String computeServiceId = "compute-service-id";

        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(job, "mockFile1", mockFile1), new StagedFile(job, "mockFile2", mockFile2)};
        final OutputStream mockOutputStream = context.mock(OutputStream.class);
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final String computeKeyName = "key-name";
        final String storageBucket = "storage-bucket";
        final String storageAccess = "213-asd-54";
        final String storageSecret = "tops3cret";
        final String storageProvider = "provider";
        final String storageAuthVersion = "1.2.3";
        final String storageEndpoint = "http://example.org";
        final String storageServiceId = "storage-service-id";
        final String regionName = "region-name";
        final PortalServiceException exception = new PortalServiceException("Some random error","Some error correction");
        final File activityFile = File.createTempFile("activity", ".ttl");
        final String activityFileName = "activity.ttl";
        final CloudFileInformation cloudFileInformation = new CloudFileInformation("one", 0, "");
        CloudFileInformation cloudFileModel = new CloudFileInformation("two", 0, "");
        final CloudFileInformation[] cloudList = {cloudFileInformation, cloudFileModel};
        final String mockUser = "jo@me.com";

        job.setComputeVmId(computeVmId);
        //As submitJob method no longer explicitly checks for empty storage credentials,
        //we need to manually set the storageBaseKey property to avoid NullPointerException
        job.setStorageBaseKey("storageBaseKey");
        //By default, a job is in SAVED state
        job.setStatus(jobInSavedState);
        job.setComputeServiceId(computeServiceId);
        job.setStorageServiceId(storageServiceId);
        job.setStorageBucket(storageBucket);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            //oneOf(mockJobManager).getJobById(job.getId(), user);will(returnValue(job));
            oneOf(mockJobManager).getJobById(job.getId(), mockPortalUser);will(returnValue(job));

            oneOf(mockFileStagingService).writeFile(job, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(mockOutputStream));
            allowing(mockOutputStream).close();

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(job);will(returnValue(stageInFiles));

            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudStorageServices[0]).getAccessKey();will(returnValue(storageAccess));
            allowing(mockCloudStorageServices[0]).getSecretKey();will(returnValue(storageSecret));
            allowing(mockCloudStorageServices[0]).getProvider();will(returnValue(storageProvider));
            allowing(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            allowing(mockCloudStorageServices[0]).getEndpoint();will(returnValue(storageEndpoint));
            allowing(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            allowing(mockCloudStorageServices[0]).getRegionName();will(returnValue(regionName));

            //We should have 1 call to upload them
            allowing(mockCloudStorageServices[0]).uploadJobFiles(with(equal(job)), with(any(File[].class)));

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));
            oneOf(mockImages[0]).getPermissions();will(returnValue(new String[] {"testRole1"}));
            allowing(mockRequest).isUserInRole("testRole1");will(returnValue(true));

            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));

            allowing(mockJobManager).saveJob(job);

            //And finally 1 call to execute the job (which will throw PortalServiceException indicating failure)
            oneOf(mockCloudComputeServices[0]).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(throwException(exception));

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, job, "Set job to provisioning");

            oneOf(mockJobManager).createJobAuditTrail(JobBuilderController.STATUS_PROVISION, job, exception);

            oneOf(mockJobManager).createJobAuditTrail(JobBuilderController.STATUS_PROVISION, job, "Job status updated.");

            oneOf(mockJobMailSender).sendMail(job);
            oneOf(mockVGLJobStatusAndLogReader).getSectionedLog(job, "Time");

            oneOf(mockRequest).getRequestURL();will(returnValue(new StringBuffer("http://mock.fake/secure/something")));
            oneOf(mockCloudStorageServices[0]).listJobFiles(with(equal(job)));will(returnValue(cloudList));
            allowing(mockFileStagingService).createLocalFile(activityFileName, job);will(returnValue(activityFile));
            allowing(mockPortalUser).getId();will(returnValue(mockUser));
            allowing(mockPortalUser).getAwsKeyName();will(returnValue(computeKeyName));
            allowing(mockAnvglProvenanceService).createActivity(job, null, mockPortalUser);will(returnValue(""));
            oneOf(mockAnvglProvenanceService).setServerURL("http://mock.fake/secure/something");
            oneOf(mockAnvglProvenanceService).createActivity(job, null, mockPortalUser);
            oneOf(mockScmEntryService).getJobSolutions(job);will(returnValue(null));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, job.getId().toString(), mockPortalUser);

        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        //VT:wait a while for the thread to finish before getting the status.
        Thread.sleep(1000);

        Assert.assertEquals(JobBuilderController.STATUS_ERROR, job.getStatus());
        Assert.assertTrue(job.getProcessDate()!=null);

    }


    /**
     * Tests that job submission fails correctly when files cannot be uploaded to S3
     * @throws Exception
     */
    @Test
    public void testJobSubmissionWithQueue_ExecuteFailure() throws Exception {
        //Instantiate our job object
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;

        final String computeVmId = "compute-vmi-id";
        final String computeServiceId = "compute-service-id";

        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(job, "mockFile1", mockFile1), new StagedFile(job, "mockFile2", mockFile2)};
        final OutputStream mockOutputStream = context.mock(OutputStream.class);
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final String computeKeyName = "key-name";
        final String storageBucket = "storage-bucket";
        final String storageAccess = "213-asd-54";
        final String storageSecret = "tops3cret";
        final String storageProvider = "provider";
        final String storageAuthVersion = "1.2.3";
        final String storageEndpoint = "http://example.org";
        final String storageServiceId = "storage-service-id";
        final String regionName = "region-name";
        final File activityFile = File.createTempFile("activity", ".ttl");
        final String activityFileName = "activity.ttl";
        final CloudFileInformation cloudFileInformation = new CloudFileInformation("one", 0, "");
        CloudFileInformation cloudFileModel = new CloudFileInformation("two", 0, "");
        final CloudFileInformation[] cloudList = {cloudFileInformation, cloudFileModel};
        final String mockUser = "jo@me.com";

        job.setComputeVmId(computeVmId);
        //As submitJob method no longer explicitly checks for empty storage credentials,
        //we need to manually set the storageBaseKey property to avoid NullPointerException
        job.setStorageBaseKey("storageBaseKey");
        //By default, a job is in SAVED state
        job.setStatus(jobInSavedState);
        job.setComputeServiceId(computeServiceId);
        job.setStorageServiceId(storageServiceId);
        job.setStorageBucket(storageBucket);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            //oneOf(mockJobManager).getJobById(job.getId(), user);will(returnValue(job));
            oneOf(mockJobManager).getJobById(job.getId(), mockPortalUser);will(returnValue(job));

            oneOf(mockFileStagingService).writeFile(job, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(mockOutputStream));
            allowing(mockOutputStream).close();

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(job);will(returnValue(stageInFiles));

            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudStorageServices[0]).getAccessKey();will(returnValue(storageAccess));
            allowing(mockCloudStorageServices[0]).getSecretKey();will(returnValue(storageSecret));
            allowing(mockCloudStorageServices[0]).getProvider();will(returnValue(storageProvider));
            allowing(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            allowing(mockCloudStorageServices[0]).getEndpoint();will(returnValue(storageEndpoint));
            allowing(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            allowing(mockCloudStorageServices[0]).getRegionName();will(returnValue(regionName));

            //We should have 1 call to upload them
            oneOf(mockCloudStorageServices[0]).uploadJobFiles(with(equal(job)), with(any(File[].class)));

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));
            oneOf(mockImages[0]).getPermissions();will(returnValue(new String[] {"testRole1"}));
            allowing(mockRequest).isUserInRole("testRole1");will(returnValue(true));

            allowing(mockJobManager).saveJob(job);

            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));

            //And finally 1 call to execute the job (which will throw PortalServiceException indicating failure)
            oneOf(mockCloudComputeServices[0]).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(throwException(new PortalServiceException("Some random error","Some error correction with Quota exceeded")));

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, job, "Set job to provisioning");

            oneOf(mockJobManager).createJobAuditTrail(JobBuilderController.STATUS_PROVISION, job, "Job Placed in Queue");

            oneOf(mockRequest).getRequestURL();will(returnValue(new StringBuffer("http://mock.fake/secure/something")));
            oneOf(mockCloudStorageServices[0]).listJobFiles(with(equal(job)));will(returnValue(cloudList));
            allowing(mockFileStagingService).createLocalFile(activityFileName, job);will(returnValue(activityFile));
            allowing(mockCloudStorageServices[0]).uploadJobFiles(with(any(VEGLJob.class)), with(any(File[].class)));

            oneOf(mockPortalUser).getUsername();will(returnValue(mockUser));
            allowing(mockPortalUser).getId();will(returnValue(mockUser));
            allowing(mockPortalUser).getAwsKeyName();will(returnValue(computeKeyName));
            allowing(mockAnvglProvenanceService).createActivity(job, null, mockPortalUser);will(returnValue(""));
            oneOf(mockAnvglProvenanceService).setServerURL("http://mock.fake/secure/something");
            oneOf(mockScmEntryService).getJobSolutions(job);will(returnValue(null));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, job.getId().toString(), mockPortalUser);
        Thread.sleep(2000);
        Assert.assertTrue(vglPollingJobQueueManager.getQueue().hasJob());
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_INQUEUE, job.getStatus());
    }


    /**
     * Tests that job submission fails correctly when user specifies a storage service that DNE
     */
    @Test
    public void testJobSubmission_StorageServiceDNE() throws Exception {
        //Instantiate our job object
        final String computeServiceId = "ccsid";
        final String injectedComputeVmId = "injected-compute-vmi-id";
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        final String storageServiceId = "cssid";

        sessionVariables.put("user-roles", new String[] {"testRole1", "testRole2"});
        job.setComputeVmId(injectedComputeVmId);
        job.setStatus(jobInSavedState); // by default, the job is in SAVED state
        job.setComputeServiceId(computeServiceId);
        job.setStorageServiceId("some-invalid-id");

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            //oneOf(mockJobManager).getJobById(job.getId(), user);will(returnValue(job));
            oneOf(mockJobManager).getJobById(job.getId(), mockPortalUser);will(returnValue(job));
            oneOf(mockJobManager).saveJob(job);

            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("user-roles");will(returnValue(sessionVariables.get("user-roles")));
            oneOf(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, job.getId().toString(), mockPortalUser);

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_UNSUBMITTED, job.getStatus());
    }

    /**
     * Tests that job submission fails correctly when user specifies a compute service that DNE
     */
    @Test
    public void testJobSubmission_ComputeServiceDNE() throws Exception {
        //Instantiate our job object
        final String computeServiceId = "ccsid";
        final String injectedComputeVmId = "injected-compute-vmi-id";
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        final String storageServiceId = "cssid";

        sessionVariables.put("user-roles", new String[] {"testRole1", "testRole2"});
        job.setComputeVmId(injectedComputeVmId);
        job.setStatus(jobInSavedState); // by default, the job is in SAVED state
        job.setComputeServiceId("some-invalid-id");
        job.setStorageServiceId(storageServiceId);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            //oneOf(mockJobManager).getJobById(job.getId(), user);will(returnValue(job));
            oneOf(mockJobManager).getJobById(job.getId(), mockPortalUser);will(returnValue(job));
            oneOf(mockJobManager).saveJob(job);

            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("user-roles");will(returnValue(sessionVariables.get("user-roles")));
            oneOf(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, job.getId().toString(), mockPortalUser);

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_UNSUBMITTED, job.getStatus());
    }

    /**
     * Tests that the bootstrap resource is not too long and has unix line endings and other such
     * conditions.
     * @throws Exception
     */
    @Test
    public void testBootstrapResource() throws Exception {
        //see - http://docs.amazonwebservices.com/AutoScaling/latest/APIReference/API_CreateLaunchConfiguration.html
        final int maxFileSize = 21847;
        final int safeFileSize = maxFileSize - 1024; //arbitrary number to account for long strings being injected into bootstrap

        String contents = ResourceUtil.loadResourceAsString("org/auscope/portal/server/web/controllers/vl-bootstrap.sh");

        Assert.assertNotNull(contents);
        Assert.assertTrue("Bootstrap is empty!", contents.length() > 0);
        Assert.assertTrue("Bootstrap is too big!", contents.length() < safeFileSize);
        Assert.assertFalse("Boostrap needs Unix style line endings!", contents.contains("\r"));
        Assert.assertEquals("Boostrap must start with '#'", '#', contents.charAt(0));

        //We can't use variables in the form ${name} as the {} conflict with java MessageFormat
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(contents);
        while (matcher.find()) {

            if (matcher.groupCount() != 1) {
                continue;
            }
            String name = matcher.group(1);

            try {
                Integer.parseInt(name);
            } catch (NumberFormatException ex) {
                Assert.fail(String.format("The variable ${%1$s} conflicts with java MessageFormat variables. Get rid of curly braces", name));
            }
        }
    }

    /**
     * Tests that Grid Submit Controller's usage of the bootstrap template
     * @throws Exception
     */
    @Test
    public void testCreateBootstrapForJob() throws Exception {
        final String bucket = "stora124e-Bucket";
        final String access = "213-asd-54";
        final String secret = "tops3cret";
        final String provider = "provider";
        final String storageAuthVersion = "1.2.3";
        final String computeServiceId = "ccs";
        final String storageServiceId = "css";
        final String endpoint = "http://example.org";
        final String regionName = "region-name";

        job.setComputeServiceId(computeServiceId);
        job.setStorageServiceId(storageServiceId);
        job.setStorageBucket(bucket);

        context.checking(new Expectations() {{
            //We allow calls to the Configurer which simply extract values from our property file
            atLeast(1).of(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            atLeast(1).of(mockCloudStorageServices[0]).getAccessKey();will(returnValue(access));
            atLeast(1).of(mockCloudStorageServices[0]).getSecretKey();will(returnValue(secret));
            atLeast(1).of(mockCloudStorageServices[0]).getProvider();will(returnValue(provider));
            atLeast(1).of(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            atLeast(1).of(mockCloudStorageServices[0]).getEndpoint();will(returnValue(endpoint));
            atLeast(1).of(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            atLeast(1).of(mockCloudStorageServices[0]).getRegionName();will(returnValue(regionName));
        }});

        job.setStorageBaseKey("test/key");

        String contents = controller.createBootstrapForJob(job);
        Assert.assertNotNull(contents);
        Assert.assertTrue("Bootstrap is empty!", contents.length() > 0);
        Assert.assertFalse("Boostrap needs Unix style line endings!", contents.contains("\r"));
        Assert.assertTrue(contents.contains(bucket));
        Assert.assertTrue(contents.contains(access));
        Assert.assertTrue(contents.contains(job.getStorageBaseKey()));
        Assert.assertTrue(contents.contains(secret));
        Assert.assertTrue(contents.contains(provider));
        Assert.assertTrue(contents.contains(endpoint));
        Assert.assertTrue(contents.contains(storageAuthVersion));
        Assert.assertTrue(contents.contains(vmSh));
        Assert.assertTrue(contents.contains(endpoint));
        Assert.assertTrue(contents.contains(regionName));
    }

    /**
     * Tests that Grid Submit Controller's usage of the bootstrap template correctly encodes an empty
     * string (when required)
     * @throws Exception
     */
    @Test
    public void testCreateBootstrapForJob_NoOptionalValues() throws Exception {
        final String bucket = "stora124e-Bucket";
        final String access = "213-asd-54";
        final String secret = "tops3cret";
        final String provider = "provider";
        final String storageAuthVersion = null;
        final String regionName = null;
        final String computeServiceId = "ccs";
        final String storageServiceId = "css";
        final String endpoint = "http://example.org";

        job.setComputeServiceId(computeServiceId);
        job.setStorageServiceId(storageServiceId);
        job.setStorageBucket(bucket);

        context.checking(new Expectations() {{
            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudStorageServices[0]).getAccessKey();will(returnValue(access));
            allowing(mockCloudStorageServices[0]).getSecretKey();will(returnValue(secret));
            allowing(mockCloudStorageServices[0]).getProvider();will(returnValue(provider));
            allowing(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            allowing(mockCloudStorageServices[0]).getEndpoint();will(returnValue(endpoint));
            allowing(mockCloudStorageServices[0]).getAuthVersion();will(returnValue(storageAuthVersion));
            allowing(mockCloudStorageServices[0]).getRegionName();will(returnValue(regionName));
        }});

        job.setStorageBaseKey("test/key");

        String contents = controller.createBootstrapForJob(job);
        Assert.assertNotNull(contents);
        Assert.assertTrue(contents.contains("STORAGE_AUTH_VERSION=\"\""));
        Assert.assertTrue(contents.contains("OS_REGION_NAME=\"\""));
    }

    /**
     * Tests that listing job images for a user works as expected
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testListImages() throws Exception {
        final String computeServiceId = "compute-service-id";
        final VglMachineImage[] images = new VglMachineImage[] {context.mock(VglMachineImage.class)};

        context.checking(new Expectations() {{
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            oneOf(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(images));

            oneOf(images[0]).getPermissions();will(returnValue(new String[] {"testRole2"}));
            allowing(mockRequest).isUserInRole("testRole2");will(returnValue(true));
        }});

        ModelAndView mav = controller.getImagesForComputeService(mockRequest, computeServiceId, null, new ANVGLUser());
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertNotNull(mav.getModel().get("data"));
        Assert.assertEquals(images.length, ((List) mav.getModel().get("data")).size());
    }

    /**
     * Tests that listing job images for a user works as expected when there is an image with no restrictions
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testListImages_NoRestrictions() throws Exception {
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        final String computeServiceId = "compute-service-id";
        final VglMachineImage[] images = new VglMachineImage[] {context.mock(VglMachineImage.class)};

        sessionVariables.put("user-roles", new String[] {"testRole1", "testRole2"});

        context.checking(new Expectations() {{
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("user-roles");will(returnValue(sessionVariables.get("user-roles")));

            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            oneOf(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(images));

            oneOf(images[0]).getPermissions();will(returnValue(null));
        }});

        ModelAndView mav = controller.getImagesForComputeService(mockRequest, computeServiceId, null, new ANVGLUser());
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertNotNull(mav.getModel().get("data"));
        Assert.assertEquals(images.length, ((List) mav.getModel().get("data")).size());
    }

    /**
     * Tests the creation of a new job object.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateOrCreateJob_CreateJobObject() throws Exception {
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        final String storageProvider = "swift";
        final String storageEndpoint = "http://example.org/storage";
        final String baseKey = "base/key";
        final String storageServiceId = "storage-service";
        final String computeServiceId = "compute-service";
        final String computeVmType = "compute-vm-type";
        final String computeVmId = "compute-vm";
        final String name = "name";
        final String description = "desc";
        final Integer seriesId = 5431;
        final boolean emailNotification = true;

        sessionVariables.put("doubleValue", 123.45);
        sessionVariables.put("intValue", 123);
        sessionVariables.put("notExtracted", new Object()); //this should NOT be requested

        context.checking(new Expectations() {{
            //A whole bunch of parameters will be setup based on what session variables are set
            oneOf(mockRequest).getSession();will(returnValue(mockSession));

            oneOf(mockSession).getAttributeNames();will(returnValue(new IteratorEnumeration(sessionVariables.keySet().iterator())));
            allowing(mockSession).getAttribute("doubleValue");will(returnValue(sessionVariables.get("doubleValue")));
            allowing(mockSession).getAttribute("intValue");will(returnValue(sessionVariables.get("intValue")));;
            allowing(mockSession).getAttribute("notExtracted");will(returnValue(sessionVariables.get("notExtracted")));
            allowing(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(null));
            allowing(mockSession).setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, null);

            allowing(mockPortalUser).getEmail();will(returnValue("email@example.org"));
            allowing(mockPortalUser).getArnExecution(); will(returnValue(null));
            allowing(mockPortalUser).getArnStorage(); will(returnValue(null));
            allowing(mockPortalUser).getAwsSecret(); will(returnValue(null));
            allowing(mockPortalUser).getAwsKeyName(); will(returnValue(null));
            allowing(mockPortalUser).getS3Bucket(); will(returnValue(null));

            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));

            oneOf(mockCloudStorageServices[0]).generateBaseKey(with(any(VEGLJob.class)));will(returnValue(baseKey));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));

            oneOf(mockFileStagingService).generateStageInDirectory(with(any(VEGLJob.class)));

            oneOf(mockJobManager).saveJob(with(any(VEGLJob.class))); //one save job to get ID
            oneOf(mockJobManager).saveJob(with(any(VEGLJob.class))); //one save to finalise initialisation
            oneOf(mockJobManager).saveJob(with(any(VEGLJob.class))); //one save to include updates

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(with(any(String.class)), with(any(VEGLJob.class)), with(any(String.class)));

            oneOf(mockCloudComputeServices[0]).getKeypair();
        }});

        ModelAndView mav = controller.updateOrCreateJob(null,  //The integer ID if not specified will trigger job creation
                name,
                description,
                seriesId,
                computeServiceId,
                computeVmId,
                computeVmType,
                storageServiceId,
                null,
                emailNotification,
                null,
                mockRequest,
                mockPortalUser);

        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        List<VEGLJob> data = (List<VEGLJob>)mav.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());

        VEGLJob newJob = data.get(0);
        Assert.assertNotNull(newJob);
        Assert.assertEquals(storageServiceId, newJob.getStorageServiceId());
        Assert.assertEquals(computeServiceId, newJob.getComputeServiceId());
        Assert.assertEquals(baseKey, newJob.getStorageBaseKey());
        Assert.assertEquals(computeVmType, newJob.getComputeInstanceType());

        Map<String, VglParameter> params = newJob.getJobParameters();
        Assert.assertNotNull(params);
        Assert.assertEquals(2, params.size());

        String paramToTest = "doubleValue";
        VglParameter param = params.get(paramToTest);
        Assert.assertNotNull(param);
        Assert.assertEquals("number", param.getType());
        Assert.assertEquals(sessionVariables.get(paramToTest).toString(), param.getValue());

        paramToTest = "intValue";
        param = params.get(paramToTest);
        Assert.assertNotNull(param);
        Assert.assertEquals("number", param.getType());
        Assert.assertEquals(sessionVariables.get(paramToTest).toString(), param.getValue());
    }

    /**
     * Tests that the updateJob works as expected
     * @throws Exception
     */
    @Test
    public void testUpdateOrCreateJob_UpdateJob() throws Exception {
        final int seriesId = 12;
        final int jobId = 1234;
        final String computeVmType = "compute-vm-type";
        final String newBaseKey = "base/key";
        final boolean emailNotification = true;
        final String keypair = "vl-developers";
        final Integer walltime = Integer.valueOf(0);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobId, mockPortalUser);will(returnValue(mockJob));

            //We should have the following fields updated
            oneOf(mockJob).setSeriesId(seriesId);
            oneOf(mockJob).setName("name");
            oneOf(mockJob).setDescription("description");
            oneOf(mockJob).setComputeVmId("computeVmId");
            oneOf(mockJob).setComputeServiceId("computeServiceId");
            oneOf(mockJob).setStorageServiceId("storageServiceId");
            oneOf(mockJob).setStorageBaseKey(newBaseKey);
            oneOf(mockJob).setEmailNotification(emailNotification);
            oneOf(mockJob).setComputeInstanceType(computeVmType);
            oneOf(mockJob).setWalltime(walltime);

            allowing(mockCloudComputeServices[0]).getId();will(returnValue("computeServiceId"));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue("storageServiceId"));

            oneOf(mockCloudStorageServices[0]).generateBaseKey(mockJob);will(returnValue(newBaseKey));
            //We should have 1 call to save our job
            oneOf(mockJobManager).saveJob(mockJob);

            oneOf(mockCloudComputeServices[0]).getKeypair();will(returnValue(keypair));
            oneOf(mockJob).setComputeInstanceKey(keypair);
        }});

        ModelAndView mav = controller.updateOrCreateJob(jobId,
                "name",
                "description",
                seriesId,
                "computeServiceId",
                "computeVmId",
                computeVmType,
                "storageServiceId",
                "registeredUrl",
                emailNotification,
                Integer.valueOf(walltime),
                mockRequest,
                mockPortalUser);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the updateJob works as expected
     * @throws Exception
     */
    @Test
    public void testUpdateJobSeries() throws Exception {
        final String folderName = "Name";
        final ArrayList<VEGLSeries> series=new ArrayList<VEGLSeries>();
        series.add(mockSeries);

        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(user.getEmail()));
            allowing(mockPortalUser).getId();will(returnValue(userId));
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), mockPortalUser);will(returnValue(mockJob));
            oneOf(mockJobManager).querySeries(user.getEmail(),folderName, null);will(returnValue(series));
            oneOf(mockJob).setSeriesId(Integer.parseInt(seriesId));
            allowing(mockJob).getEmailAddress();will(returnValue(job.getEmailAddress()));
            allowing(mockJob).getUser();will(returnValue(userId));
            oneOf(mockJobManager).saveJob(mockJob);
        }});

        ModelAndView mav = controller.updateJobSeries(Integer.parseInt(jobId),folderName,mockRequest,mockPortalUser);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the updateJob works as expected
     * @throws Exception
     */
    @Test
    public void testUpdateJobSeriesError() throws Exception {
        final String folderName = "Name";
        final ArrayList<VEGLSeries> series=new ArrayList<VEGLSeries>();


        context.checking(new Expectations() {{
            allowing(mockPortalUser).getEmail();will(returnValue(user.getEmail()));
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), new ANVGLUser());will(returnValue(mockJob));
            oneOf(mockJobManager).querySeries(user.getEmail(),folderName, null);will(returnValue(series));

        }});

        ModelAndView mav = controller.updateJobSeries(Integer.parseInt(jobId),folderName,mockRequest,mockPortalUser);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    @Test
    public void testUpdateOrCreateJob_SaveFailure() throws Exception {
        final boolean emailNotification = true;
        final String computeVmType = "compute-vm-type";
        final Integer walltime = Integer.valueOf(0);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), mockPortalUser);will(returnValue(mockJob));

            //We should have the following fields updated
            oneOf(mockJob).setSeriesId(Integer.parseInt(seriesId));
            oneOf(mockJob).setName("name");
            oneOf(mockJob).setDescription("description");
            oneOf(mockJob).setComputeVmId("computeVmId");
            oneOf(mockJob).setComputeServiceId("computeServiceId");
            oneOf(mockJob).setStorageServiceId("storageServiceId");
            oneOf(mockJob).setEmailNotification(emailNotification);
            oneOf(mockJob).setComputeInstanceType(computeVmType);
            oneOf(mockJob).setWalltime(walltime);

            allowing(mockCloudComputeServices[0]).getId();will(returnValue("computeServiceId"));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue("computeStorageId"));

            //We should have 1 call to save our job but will throw Exception
            oneOf(mockJobManager).saveJob(mockJob);will(throwException(new Exception("")));
        }});

        ModelAndView mav = controller.updateOrCreateJob(Integer.parseInt(jobId),
                "name",
                "description",
                Integer.parseInt(seriesId),
                "computeServiceId",
                "computeVmId",
                computeVmType,
                "storageServiceId",
                "registeredUrl",
                emailNotification,
                walltime,
                mockRequest,
                mockPortalUser);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the updateJob fails as expected with a bad storage id
     * @throws Exception
     */
    @Test
    public void testUpdateOrCreateJob_UpdateJobWithBadStorageId() throws Exception {
        final boolean emailNotification = true;
        final String computeVmType = "compute-vm-type";
        final Integer walltime = Integer.valueOf(0);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), mockPortalUser);will(returnValue(mockJob));

            //We should have the following fields updated
            oneOf(mockJob).setSeriesId(Integer.parseInt(seriesId));
            oneOf(mockJob).setName("name");
            oneOf(mockJob).setDescription("description");
            oneOf(mockJob).setComputeVmId("computeVmId");
            oneOf(mockJob).setComputeServiceId("computeServiceId");
            oneOf(mockJob).setStorageServiceId("storageServiceId");
            oneOf(mockJob).setEmailNotification(emailNotification);
            oneOf(mockJob).setComputeInstanceType(computeVmType);
            oneOf(mockJob).setWalltime(walltime);

            allowing(mockCloudComputeServices[0]).getId();will(returnValue("computeServiceId"));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue("computeStorageId-thatDNE"));

            //We should have 1 call to save our job
            oneOf(mockJobManager).saveJob(mockJob);
        }});

        ModelAndView mav = controller.updateOrCreateJob(Integer.parseInt(jobId),
                "name",
                "description",
                Integer.parseInt(seriesId),
                "computeServiceId",
                "computeVmId",
                computeVmType,
                "storageServiceId",
                "registeredUrl",
                emailNotification,
                walltime,
                mockRequest,
                mockPortalUser);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the updateJob fails as expected with a bad compute id
     * @throws Exception
     */
    @Test
    public void testUpdateOrCreateJob_UpdateJobWithBadComputeId() throws Exception {
        final boolean emailNotification = true;
        final String computeVmType = "compute-vm-type";
        final Integer walltime = Integer.valueOf(0);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), mockPortalUser);will(returnValue(mockJob));

            //We should have the following fields updated
            oneOf(mockJob).setSeriesId(Integer.parseInt(seriesId));
            oneOf(mockJob).setName("name");
            oneOf(mockJob).setDescription("description");
            oneOf(mockJob).setComputeVmId("computeVmId");
            oneOf(mockJob).setComputeServiceId("computeServiceId");
            oneOf(mockJob).setStorageServiceId("storageServiceId");
            oneOf(mockJob).setEmailNotification(emailNotification);
            oneOf(mockJob).setComputeInstanceType(computeVmType);
            oneOf(mockJob).setWalltime(walltime);

            allowing(mockCloudComputeServices[0]).getId();will(returnValue("computeServiceId-thatDNE"));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue("computeStorageId"));

            //We should have 1 call to save our job
            oneOf(mockJobManager).saveJob(mockJob);
        }});

        ModelAndView mav = controller.updateOrCreateJob(Integer.parseInt(jobId),
                "name",
                "description",
                Integer.parseInt(seriesId),
                "computeServiceId",
                "computeVmId",
                computeVmType,
                "storageServiceId",
                "registeredUrl",
                emailNotification,
                walltime,
                mockRequest,
                mockPortalUser);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean) mav.getModel().get("success"));
    }

    /**
     * Tests that the updateJobDownloads works as expected when appending
     * @throws Exception
     */
    @Test
    public void testUpdateJobDownloads_Append() throws Exception {
        final String append = "true";
        final String[] names = new String[] {"n1", "n2"};
        final String[] descriptions = new String[] {"d1", "d2"};
        final String[] urls = new String[] {"http://example.org/1", "http://example.org/2"};
        final String[] localPaths = new String[] {"p1", "p2"};
        final VglDownload[] existingDownloads = new VglDownload[] {new VglDownload(12356)};


        job.setJobDownloads(new ArrayList<VglDownload>(Arrays.asList(existingDownloads)));


        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);will(returnValue(job));

            oneOf(mockJobManager).saveJob(job);
        }});

        ModelAndView mav = controller.updateJobDownloads(Integer.parseInt(jobId), append, names, descriptions, urls, localPaths, user);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        //The resulting job should have 3 elements in its list (due to appending)
        List<VglDownload> dls = job.getJobDownloads();
        Assert.assertEquals(existingDownloads.length + names.length, dls.size());
        Assert.assertSame(existingDownloads[0], dls.get(0));
        for (int i = 0; i < names.length; i++) {
            VglDownload dlToTest = dls.get(existingDownloads.length + i);
            Assert.assertEquals(names[i], dlToTest.getName());
            Assert.assertEquals(descriptions[i], dlToTest.getDescription());
            Assert.assertEquals(urls[i], dlToTest.getUrl());
            Assert.assertEquals(localPaths[i], dlToTest.getLocalPath());
        }
    }

    /**
     * Tests that the updateJobDownloads works as expected when replacing
     * @throws Exception
     */
    @Test
    public void testUpdateJobDownloads_Replace() throws Exception {
        final String append = "false";
        final String[] names = new String[] {"n1", "n2"};
        final String[] descriptions = new String[] {"d1", "d2"};
        final String[] urls = new String[] {"http://example.org/1", "http://example.org/2"};
        final String[] localPaths = new String[] {"p1", "p2"};
        final VglDownload[] existingDownloads = new VglDownload[] {new VglDownload(12356)};


        job.setJobDownloads(new ArrayList<VglDownload>(Arrays.asList(existingDownloads)));


        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);will(returnValue(job));

            oneOf(mockJobManager).saveJob(job);
        }});

        ModelAndView mav = controller.updateJobDownloads(Integer.parseInt(jobId), append, names, descriptions, urls, localPaths, user);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        //The resulting job should have 3 elements in its list (due to appending)
        List<VglDownload> dls = job.getJobDownloads();
        Assert.assertEquals(names.length, dls.size());
        for (int i = 0; i < names.length; i++) {
            VglDownload dlToTest = dls.get(i);
            Assert.assertEquals(names[i], dlToTest.getName());
            Assert.assertEquals(descriptions[i], dlToTest.getDescription());
            Assert.assertEquals(urls[i], dlToTest.getUrl());
            Assert.assertEquals(localPaths[i], dlToTest.getLocalPath());
        }
    }

    /**
     * Unit test for succesfull object conversion in getAllJobInputs
     * @throws Exception
     */
    @Test
    public void testGetAllJobInputs() throws Exception {
        final VglDownload dl = new VglDownload(413);
        final File mockFile = context.mock(File.class);
        final long mockFileLength = 21314L;
        final StagedFile[] stagedFiles = new StagedFile[]{new StagedFile(job, "another/file.ext", mockFile)};

        dl.setDescription("desc");
        dl.setEastBoundLongitude(1.0);
        dl.setWestBoundLongitude(2.0);
        dl.setLocalPath("local/path/file.ext");
        dl.setName("myFile");
        dl.setNorthBoundLatitude(3.0);
        dl.setSouthBoundLatitude(4.0);
        dl.setParent(job);

        job.setJobDownloads(Arrays.asList(dl));


        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId), user);will(returnValue(job));
            oneOf(mockFile).length();will(returnValue(mockFileLength));
            oneOf(mockFileStagingService).listStageInDirectoryFiles(job);will(returnValue(stagedFiles));
        }});

        ModelAndView mav = controller.getAllJobInputs(Integer.parseInt(jobId), user);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        Assert.assertNotNull(mav.getModel().get("data"));
        @SuppressWarnings("unchecked")
        List<VglDownload> fileInfo = (List<VglDownload>) mav.getModel().get("data");

        Assert.assertEquals(2, fileInfo.size());

        Assert.assertEquals(stagedFiles[0].getName(), fileInfo.get(0).getLocalPath());
        Assert.assertEquals(dl.getLocalPath(), fileInfo.get(1).getLocalPath());
    }

    /**
     * Simple test to test formatting of cloud service into ModelMap objects
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetComputeServices() throws Exception {
        final String name = "name";
        final String id = "id";


        context.checking(new Expectations() {{
            allowing(mockCloudComputeServices[0]).getName();will(returnValue(name));
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(id));
            allowing(mockScmEntryService).getJobProviders(null, user);will(returnValue(null));
        }});

		ModelAndView mav = controller.getComputeServices(null, user);

        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        ModelMap test = ((List<ModelMap>)mav.getModel().get("data")).get(0);

        Assert.assertEquals(name, test.get("name"));
        Assert.assertEquals(id, test.get("id"));
    }

    /**
     * Simple test to test formatting of cloud service into ModelMap objects
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetStorageServices() throws Exception {
        final String name = "name";
        final String id = "id";

        context.checking(new Expectations() {{
            allowing(mockCloudStorageServices[0]).getName();will(returnValue(name));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(id));
        }});

        ModelAndView mav = controller.getStorageServices();

        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        ModelMap test = ((List<ModelMap>)mav.getModel().get("data")).get(0);

        Assert.assertEquals(name, test.get("name"));
        Assert.assertEquals(id, test.get("id"));
    }

    /**
     * Tests that getting a compute type list for a particular compute service returns no exceptions
     * @throws Exception
     */
    @Test
    public void testGetComputeTypes() throws Exception {
        final String computeId = "compute-id";
        final String imageId = "image-id";
        final ComputeType[] result = new ComputeType[] {new ComputeType("m3.test-compute-type")};
        final MachineImage[] machineImages = new MachineImage[] {new MachineImage("another-image"), new MachineImage(imageId)};

        machineImages[0].setMinimumDiskGB(200);
        machineImages[1].setMinimumDiskGB(1000);

        context.checking(new Expectations() {{
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeId));
            allowing(mockCloudComputeServices[0]).getAvailableComputeTypes(null, null, 1000);will(returnValue(result));
            allowing(mockCloudComputeServices[0]).getAvailableImages();will(returnValue(machineImages));
        }});

        ModelAndView mav = controller.getTypesForComputeService(mockRequest, computeId, imageId);

        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        Object[] actual = (Object[])mav.getModel().get("data");
        Assert.assertEquals(result.length, actual.length);
        Assert.assertSame(result[0], (ComputeType) actual[0]);
    }

    /**
     * Tests that getting a compute type list for a particular compute service returns no exceptions
     * @throws Exception
     */
    @Test
    public void testGetComputeTypes_NoComputeService() throws Exception {
        final String computeId = "compute-id";
        final String imageId = "image-id";

        context.checking(new Expectations() {{
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeId));
        }});

        ModelAndView mav = controller.getTypesForComputeService(mockRequest, "non-matching-compute-id", "image-id");

        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

}

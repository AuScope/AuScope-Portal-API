package org.auscope.portal.server.vegl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudComputeService.InstanceStatus;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.ResourceUtil;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.controllers.JobListController;
import org.auscope.portal.server.web.security.NCIDetails;
import org.auscope.portal.server.web.service.CloudSubmissionService;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ModelMap;

/**
 * Unit tests for VGLJobStatusAndLogReader.
 *
 * @author Richard Goh
 */
public class TestVGLJobStatusAndLogReader extends PortalTestClass {
    private static final String USER_EMAIL = "dummy@dummy.com";
    private final String storageServiceId = "storage-service-id";
    private final String computeServiceId = "compute-service-id";
    private VEGLJobManager mockJobManager;
    private CloudStorageService[] mockCloudStorageServices;
    private CloudComputeService[] mockCloudComputeServices;
    private VGLJobStatusAndLogReader jobStatLogReader;
    private CloudSubmissionService mockCloudSubmissionService;

    @Before
    public void init() {
        mockJobManager = context.mock(VEGLJobManager.class);
        mockCloudStorageServices = new CloudStorageService[] { context.mock(CloudStorageService.class) };
        mockCloudComputeServices = new CloudComputeService[] { context.mock(CloudComputeService.class) };
        mockCloudSubmissionService = context.mock(CloudSubmissionService.class);
        context.checking(new Expectations() {{
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
        }});

        jobStatLogReader = new VGLJobStatusAndLogReader(mockJobManager,
                mockCloudStorageServices, mockCloudComputeServices);
        jobStatLogReader.setCloudSubmissionService(mockCloudSubmissionService);
    }

    /**
     * Tests that the get job status method returns a pending
     * status when the status is still pending.
     *
     * @throws Exception
     */
    @Test
    public void testGetJobStatus_PendingToPending() throws Exception {
        final int mockJobId = 123;
        final String mockJobStatus = JobBuilderController.STATUS_PENDING;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final CloudFileInformation[] jobPendingFiles = new CloudFileInformation[] {
                new CloudFileInformation("key3/filename", 100L, "http://public.url3/filename"),
                new CloudFileInformation("key3/filename2", 101L, "http://public.url3/filename2"),
        };

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(mockJobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(mockJobId));
            allowing(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getStatus();will(returnValue(mockJobStatus));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            oneOf(mockCloudStorageServices[0]).listJobFiles(with(mockJob));will(returnValue(jobPendingFiles));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            oneOf(mockCloudComputeServices[0]).getJobStatus(mockJob);will(returnValue(InstanceStatus.Pending));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(JobBuilderController.STATUS_PENDING, status);
    }

    /**
     * Tests that the get job status method returns a pending
     * status when the status is still pending (even if it's started running)
     *
     * @throws Exception
     */
    @Test
    public void testGetJobStatus_PendingToPending_RunningVM() throws Exception {
        final int mockJobId = 123;
        final String mockJobStatus = JobBuilderController.STATUS_PENDING;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final CloudFileInformation[] jobPendingFiles = new CloudFileInformation[] {
                new CloudFileInformation("key3/filename", 100L, "http://public.url3/filename"),
                new CloudFileInformation("key3/filename2", 101L, "http://public.url3/filename2"),
        };

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(mockJobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(mockJobId));
            allowing(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getStatus();will(returnValue(mockJobStatus));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            oneOf(mockCloudStorageServices[0]).listJobFiles(with(mockJob));will(returnValue(jobPendingFiles));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            oneOf(mockCloudComputeServices[0]).getJobStatus(mockJob);will(returnValue(InstanceStatus.Running));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(JobBuilderController.STATUS_PENDING, status);
    }

    /**
     * Tests that the get job status method returns a error
     * status when its underlying VM goes missing
     *
     * @throws Exception
     */
    @Test
    public void testGetJobStatus_PendingToError() throws Exception {
        final int mockJobId = 123;
        final String mockJobStatus = JobBuilderController.STATUS_PENDING;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final CloudFileInformation[] jobPendingFiles = new CloudFileInformation[] {
                new CloudFileInformation("key3/filename", 100L, "http://public.url3/filename"),
                new CloudFileInformation("key3/filename2", 101L, "http://public.url3/filename2"),
        };

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(mockJobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(mockJobId));
            allowing(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getStatus();will(returnValue(mockJobStatus));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            oneOf(mockCloudStorageServices[0]).listJobFiles(with(mockJob));will(returnValue(jobPendingFiles));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            oneOf(mockCloudComputeServices[0]).getJobStatus(mockJob);will(returnValue(InstanceStatus.Missing));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(JobBuilderController.STATUS_ERROR, status);
    }

    /**
     * Tests that the get job status method returns active
     * status when its status changes from pending to active.
     *
     * @throws Exception
     */
    @Test
    public void testGetJobStatus_PendingToActive() throws Exception {
        final int mockJobId = 123;
        final String mockJobStatus = JobBuilderController.STATUS_PENDING;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final CloudFileInformation[] jobActiveFiles = new CloudFileInformation[] {
                new CloudFileInformation("key2/filename", 100L, "http://public.url2/filename"),
                new CloudFileInformation("key2/filename3", 102L, "http://public.url2/filename3"),
                new CloudFileInformation("key2/workflow-version.txt", 102L, "http://public.url2/filename3"),
        };

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(mockJobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(mockJobId));
            allowing(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getStatus();will(returnValue(mockJobStatus));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            oneOf(mockCloudStorageServices[0]).listJobFiles(with(mockJob));will(returnValue(jobActiveFiles));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));

            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));
            allowing(mockJob).isWalltimeSet(); will(returnValue(false));
            
            allowing(mockJob).getWalltime(); will(returnValue(null));
            oneOf(mockCloudComputeServices[0]).getJobStatus(mockJob);will(returnValue(InstanceStatus.Running));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(JobBuilderController.STATUS_ACTIVE, status);
    }

    /**
     * Tests that the get job status method returns done
     * status when its status changes from pending to done.
     *
     * @throws Exception
     */
    @Test
    public void testGetJobStatus_PendingToDone() throws Exception {
        final int mockJobId = 123;
        final String mockJobStatus = JobBuilderController.STATUS_PENDING;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final CloudFileInformation[] jobDoneFiles = new CloudFileInformation[] {
                new CloudFileInformation("key3/workflow-version.txt", 100L, "http://public.url3/filename"),
                new CloudFileInformation("key3/filename2", 101L, "http://public.url3/filename2"),
                new CloudFileInformation("key3/vl.end", 102L, "http://public.url3/filename3"),
        };

        final List<VglDownload> downloads = new ArrayList<>();
		VglDownload download = new VglDownload(1);
		download.setUrl("http://portal-uploads.vgl.org/file1");
		download.setName("file1");
		downloads.add(download);

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(mockJobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(mockJobId));
            allowing(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getStatus();will(returnValue(mockJobStatus));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudStorageServices[0]).listJobFiles(with(mockJob));will(returnValue(jobDoneFiles));
			allowing(mockJob).getUser();will(returnValue("JaneNg"));
			allowing(mockJob).getJobDownloads();will(returnValue(downloads));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));
            oneOf(mockCloudComputeServices[0]).getJobStatus(mockJob);will(returnValue(InstanceStatus.Missing));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(JobBuilderController.STATUS_DONE, status);
    }

    /**
     * Tests that the status of a completed or un-submitted job
     * remains unchanged.
     */
    @Test
    public void testGetJobStatus_DoneOrUnsubmittedJob() {
        final int jobId = 123;
        final String job123Status = JobBuilderController.STATUS_DONE;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, "a", "b", "c", USER_EMAIL, "d", "e", "f");will(returnValue(mockJob));
            oneOf(mockJob).getId();will(returnValue(jobId));
            allowing(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getStatus();will(returnValue(job123Status));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue("a"));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue("b"));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue("c"));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue("d"));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue("e"));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue("f"));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(job123Status, status);
    }

    /**
     * Tests that the get job status returns null
     * when the job cannot be found in database.
     */
    @Test
    public void testGetJobStatus_JobDNE() {
        final int jobId = 123;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(null));
            oneOf(mockJob).getId();will(returnValue(jobId));
            oneOf(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertNull(status);
    }

    /**
     * Tests that the status of a job remains unchanged
     * when it doesn't have a storage service attached to it.
     */
    @Test
    public void testGetJobStatus_NoStorageService() {
        final int jobId = 123;
        final String job123Status = JobBuilderController.STATUS_PENDING;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(jobId));
            allowing(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getStatus();will(returnValue(job123Status));
            allowing(mockJob).getStorageServiceId();will(returnValue("does-not-exist"));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(job123Status, status);
    }

    /**
     * Tests that the status of a job remains unchanged
     * when an error occurred while the storage service is down.
     * @throws Exception
     */
    @Test
    public void testGetJobStatus_StorageServiceError() throws Exception {
        final int jobId = 123;
        final String job123Status = JobBuilderController.STATUS_PENDING;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(jobId));
            allowing(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getStatus();will(returnValue(job123Status));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).listJobFiles(mockJob);will(throwException(new PortalServiceException("error")));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(job123Status, status);
    }

    /**
     * Tests that the status of a job shifts to error if the job submission service loses a job
     * @throws Exception
     */
    @Test
    public void testGetJobStatus_SubmissionServiceError() throws Exception {
        final int jobId = 123;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(jobId));
            allowing(mockJob).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob).getStatus();will(returnValue(JobBuilderController.STATUS_PROVISION));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));

            oneOf(mockCloudSubmissionService).isSubmitting(mockJob, mockCloudComputeServices[0]);will(returnValue(false));
            oneOf(mockJobManager).getJobById(jobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(JobBuilderController.STATUS_ERROR, status);
    }

    /**
     * Tests that a job updating underneath us due to the CloudSubmissionService won't erroneously
     * shift a job to ERROR
     * @throws Exception
     */
    @Test
    public void testGetJobStatus_SubmissionServiceTransition() throws Exception {
        final int jobId = 123;
        final VEGLJob mockJob1 = context.mock(VEGLJob.class, "mockJob1");
        final VEGLJob mockJob2 = context.mock(VEGLJob.class, "mockJob2");
        final CloudFileInformation[] jobActiveFiles = new CloudFileInformation[] {
                new CloudFileInformation("key2/filename", 100L, "http://public.url2/filename"),
                new CloudFileInformation("key2/filename3", 102L, "http://public.url2/filename3"),
                new CloudFileInformation("key2/workflow-version.txt", 102L, "http://public.url2/filename3"),
        };

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob1));
            allowing(mockJob1).getId();will(returnValue(jobId));
            allowing(mockJob1).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob1).getStatus();will(returnValue(JobBuilderController.STATUS_PROVISION));
            allowing(mockJob1).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob1).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockJob1).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob1).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob1).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob1).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob1).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob1).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));

            //Pretend our job is going to shift to Pending while we are checking everything. Make sure we catch it and then
            //proceed with a normal Pending - Active job check
            oneOf(mockCloudSubmissionService).isSubmitting(mockJob1, mockCloudComputeServices[0]);will(returnValue(false));
            oneOf(mockJobManager).getJobById(jobId, null, null, null, USER_EMAIL, null, null, null);will(returnValue(mockJob2));
            allowing(mockJob2).getStatus();will(returnValue(JobBuilderController.STATUS_PENDING));
            allowing(mockJob2).getId();will(returnValue(jobId));
            allowing(mockJob2).getEmailAddress();will(returnValue(USER_EMAIL));
            allowing(mockJob2).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob2).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            oneOf(mockCloudStorageServices[0]).listJobFiles(with(mockJob2));will(returnValue(jobActiveFiles));
            allowing(mockJob2).getProperty(CloudJob.PROPERTY_STS_ARN); will(returnValue(null));
            allowing(mockJob2).getProperty(CloudJob.PROPERTY_CLIENT_SECRET); will(returnValue(null));
            allowing(mockJob2).getProperty(CloudJob.PROPERTY_S3_ROLE); will(returnValue(null));
            allowing(mockJob2).getProperty(NCIDetails.PROPERTY_NCI_USER); will(returnValue(null));
            allowing(mockJob2).getProperty(NCIDetails.PROPERTY_NCI_PROJECT); will(returnValue(null));
            allowing(mockJob2).getProperty(NCIDetails.PROPERTY_NCI_KEY); will(returnValue(null));
//            allowing(mockJob2).getSubmitDate(); will(returnValue(new Date()));
            allowing(mockJob2).isWalltimeSet(); will(returnValue(false));
            allowing(mockJob2).getWalltime(); will(returnValue(null));
            oneOf(mockCloudComputeServices[0]).getJobStatus(mockJob2);will(returnValue(InstanceStatus.Running));
        }});

        String status = jobStatLogReader.getJobStatus(mockJob1);
        Assert.assertEquals(JobBuilderController.STATUS_ACTIVE, status);
    }

    /**
     * Tests that log sectioning works as expected
     * @throws Exception
     */
    @Test
    public void testGetSectionedLogs() throws Exception {
        try (final InputStream logContents = ResourceUtil.loadResourceAsStream("sectionedVglLog.txt")) {
            final String logContentString = IOUtils.toString(ResourceUtil.loadResourceAsStream("sectionedVglLog.txt"));
            final VEGLJob mockJob = context.mock(VEGLJob.class);

            context.checking(new Expectations() {
                {
                    allowing(mockJob).getStorageServiceId();
                    will(returnValue(storageServiceId));
                    allowing(mockCloudStorageServices[0]).getId();
                    will(returnValue(storageServiceId));
                    oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, JobListController.VL_LOG_FILE);
                    will(returnValue(logContents));
                }
            });

            ModelMap map = jobStatLogReader.getSectionedLogs(mockJob);

            // There should be 3 sections (we don't care about line ending
            // formats - normalise it to unix style \n)
            Assert.assertEquals(4, map.keySet().size());
            Assert.assertEquals("contents of env\n", stripCarriageReturns(map.get("environment").toString()));
            Assert.assertEquals("multiple\nlines\n", stripCarriageReturns(map.get("test").toString()));
            Assert.assertEquals("text\n", stripCarriageReturns(map.get("spaced header").toString()));
            Assert.assertEquals(stripCarriageReturns(logContentString),
                    stripCarriageReturns(map.get("Full").toString()));
        }
    }

    /**
     * Tests that log sectioning fails as expected when log lookup fails
     * @throws Exception
     */
    @Test
    public void testGetSectionedLogs_NoStorageService() {
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockJob).getStorageServiceId();will(returnValue("does-not-exist"));
        }});

        try {
            jobStatLogReader.getSectionedLogs(mockJob);
        } catch (PortalServiceException ex) {
            Assert.assertEquals("The specified job doesn't have a storage service.", ex.getMessage());
            Assert.assertEquals("Please ensure you have chosen a storage provider for the job.", ex.getErrorCorrection());
        }
    }

    /**
     * Tests that log sectioning fails as expected when log lookup fails at both the storage/compute service level
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testGetSectionedLogs_LogAccessErrorNull() throws Exception {
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(1));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, JobListController.VL_LOG_FILE);will(throwException(new PortalServiceException("error")));
            oneOf(mockCloudComputeServices[0]).getConsoleLog(mockJob);will(returnValue(null));
        }});

        jobStatLogReader.getSectionedLogs(mockJob);
    }

    /**
     * Tests that log sectioning fails as expected when log lookup fails at both the storage/compute service level
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testGetSectionedLogs_LogAccessErrorEx() throws Exception {
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(1));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, JobListController.VL_LOG_FILE);will(throwException(new PortalServiceException("error")));
            oneOf(mockCloudComputeServices[0]).getConsoleLog(mockJob);will(throwException(new PortalServiceException("error")));
        }});

        jobStatLogReader.getSectionedLogs(mockJob);
    }

    /**
     * Tests that log sectioning works when log lookup fails but compute lookup succeeds
     * @throws Exception
     */
    @Test
    public void testGetSectionedLogs_LogAccessError_ComputeSuccess() throws Exception {
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final String logContents = IOUtils.toString(ResourceUtil.loadResourceAsStream("sectionedVglLog.txt"));

        context.checking(new Expectations() {{
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(1));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, JobListController.VL_LOG_FILE);will(throwException(new PortalServiceException("error")));
            oneOf(mockCloudComputeServices[0]).getConsoleLog(mockJob);will(returnValue(logContents));
        }});

        String result = jobStatLogReader.getSectionedLog(mockJob, "environment");
        Assert.assertEquals("contents of env\n", stripCarriageReturns(result));
    }

    /**
     * Tests that log sectioning works as expected
     * @throws Exception
     */
    @Test
    public void testGetSectionedLogs_WithSectionName() throws Exception {
        try (final InputStream logContents = ResourceUtil.loadResourceAsStream("sectionedVglLog.txt")) {
            final VEGLJob mockJob = context.mock(VEGLJob.class);

            context.checking(new Expectations() {
                {
                    allowing(mockJob).getStorageServiceId();
                    will(returnValue(storageServiceId));
                    allowing(mockCloudStorageServices[0]).getId();
                    will(returnValue(storageServiceId));
                    allowing(mockCloudStorageServices[0]).getJobFile(mockJob, JobListController.VL_LOG_FILE);
                    will(returnValue(logContents));
                }
            });

            String result = jobStatLogReader.getSectionedLog(mockJob, "environment");
            Assert.assertEquals("contents of env\n", stripCarriageReturns(result));
        }
    }

    /**
     * Tests that log sectioning works as expected
     * @throws Exception
     */
    @Test
    public void testGetSectionedLogs_WithSectionNameError() throws Exception {
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getId();will(returnValue(1));
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            allowing(mockJob).getComputeServiceId();will(returnValue(computeServiceId));
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
            allowing(mockCloudComputeServices[0]).getId();will(returnValue(computeServiceId));
            oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, JobListController.VL_LOG_FILE);will(throwException(new PortalServiceException("error")));
            oneOf(mockCloudComputeServices[0]).getConsoleLog(mockJob);will(returnValue(null));
        }});

        String result = jobStatLogReader.getSectionedLog(mockJob, "environment");
        Assert.assertNull(result);
    }

    private static String stripCarriageReturns(String s) {
        return s.replaceAll("\r", "");
    }
}
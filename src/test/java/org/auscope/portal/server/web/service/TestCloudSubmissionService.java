package org.auscope.portal.server.web.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.internal.NamedSequence;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestCloudSubmissionService extends PortalTestClass {

    private VEGLJobManager mockJobManager = context.mock(VEGLJobManager.class);
    private CloudComputeService mockCloudComputeService = context.mock(CloudComputeService.class);
    private VGLJobStatusChangeHandler mockVglJobStatusChangeHandler = context.mock(VGLJobStatusChangeHandler.class);
    private ScheduledExecutorService executor;
    private CloudSubmissionService service;


    @Before
    public void init() {
        executor = Executors.newScheduledThreadPool(1);
        service = new CloudSubmissionService(mockJobManager, mockVglJobStatusChangeHandler, executor);
    }

    @After
    public void destroy() throws Exception{
        if (!executor.isTerminated()) {
            executor.shutdownNow();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
    }


    /**
     * Tests that job submission succeeds in a best case scenario
     * @throws Exception
     */
    @Test
    public void testJobSubmission() throws Exception {
        //Instantiate our job object
        final String userDataString = "user-data";
        final String instanceId = "instance-id";
        final VEGLJob job = new VEGLJob(213);

        job.setStatus(JobBuilderController.STATUS_PROVISION);


        context.checking(new Expectations() {{
            allowing(mockCloudComputeService).getId();will(returnValue("ccs-id"));

            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(Exception.class)));
            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(String.class)));

            oneOf(mockCloudComputeService).executeJob(with(job), with(userDataString));will(returnValue(instanceId));
            oneOf(mockJobManager).saveJob(job);
            oneOf(mockVglJobStatusChangeHandler).handleStatusChange(job, JobBuilderController.STATUS_PENDING, JobBuilderController.STATUS_PROVISION);
        }});

        try {
            service.queueSubmission(mockCloudComputeService, job, userDataString);
        } finally {
            executor.shutdown();
        }
        executor.awaitTermination(1000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(instanceId, job.getComputeInstanceId());
        Assert.assertNotNull(job.getSubmitDate());
    }

    /**
     * Tests that job submission succeeds in a best case scenario
     * @throws Exception
     */
    @Test
    public void testJobSubmissionError() throws Exception {
        //Instantiate our job object
        final String userDataString = "user-data";
        final String instanceId = "instance-id";
        final VEGLJob job = new VEGLJob(213);

        job.setStatus(JobBuilderController.STATUS_PROVISION);

        context.checking(new Expectations() {{
            allowing(mockCloudComputeService).getId();will(returnValue("ccs-id"));

            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(Exception.class)));
            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(String.class)));

            oneOf(mockCloudComputeService).executeJob(with(job), with(userDataString));will(throwException(new PortalServiceException("error")));
            oneOf(mockJobManager).saveJob(job);
            oneOf(mockVglJobStatusChangeHandler).handleStatusChange(job, JobBuilderController.STATUS_ERROR, JobBuilderController.STATUS_PROVISION);
        }});

        try {
            service.queueSubmission(mockCloudComputeService, job, userDataString);
        } finally {
            executor.shutdown();
        }
        executor.awaitTermination(1000, TimeUnit.MILLISECONDS);

        Assert.assertNull(job.getComputeInstanceId());
        Assert.assertNull(job.getSubmitDate());
    }

    /**
     * Tests that job submission succeeds in a best case scenario
     * @throws Exception
     */
    @Test
    public void testJobSubmission_NoInstanceId() throws Exception {
        //Instantiate our job object
        final String userDataString = "user-data";
        final String instanceId = "instance-id";
        final VEGLJob job = new VEGLJob(213);

        job.setStatus(JobBuilderController.STATUS_PROVISION);

        context.checking(new Expectations() {{
            allowing(mockCloudComputeService).getId();will(returnValue("ccs-id"));

            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(Exception.class)));
            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(String.class)));

            oneOf(mockCloudComputeService).executeJob(with(job), with(userDataString));will(returnValue(null));
            oneOf(mockJobManager).saveJob(job);
            oneOf(mockVglJobStatusChangeHandler).handleStatusChange(job, JobBuilderController.STATUS_ERROR, JobBuilderController.STATUS_PROVISION);
        }});

        try {
            service.queueSubmission(mockCloudComputeService, job, userDataString);
        } finally {
            executor.shutdown();
        }
        executor.awaitTermination(1000, TimeUnit.MILLISECONDS);

        Assert.assertNull(job.getComputeInstanceId());
        Assert.assertNull(job.getSubmitDate());
    }

    /**
     * Tests that job submission submits correctly when the first is quota exceeded
     * @throws Exception
     */
    @Test
    public void testJobSubmissionWithQueue() throws Exception {
        //Instantiate our job object
        final String userDataString = "user-data";
        final String instanceId = "instance-id";
        final VEGLJob job = new VEGLJob(213);

        job.setStatus(JobBuilderController.STATUS_PROVISION);

        service.setQuotaResubmitTime(500L);
        service.setQuotaResubmitUnits(TimeUnit.MILLISECONDS);

        Sequence sequence = new NamedSequence("queue to pending sequence");

        context.checking(new Expectations() {{
            allowing(mockCloudComputeService).getId();will(returnValue("ccs-id"));

            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(Exception.class)));
            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(String.class)));

            //Our main execution sequence goes -  submit - Quota Full - In Queue - submit - pending
            oneOf(mockCloudComputeService).executeJob(with(job), with(userDataString));will(throwException(new PortalServiceException("Some random error","Some error correction with Quota exceeded")));inSequence(sequence);
            oneOf(mockJobManager).saveJob(job);inSequence(sequence);
            oneOf(mockVglJobStatusChangeHandler).handleStatusChange(job, JobBuilderController.STATUS_INQUEUE, JobBuilderController.STATUS_PROVISION);inSequence(sequence);
            oneOf(mockCloudComputeService).executeJob(with(job), with(userDataString));will(returnValue(instanceId));inSequence(sequence);
            oneOf(mockJobManager).saveJob(job);inSequence(sequence);
            oneOf(mockVglJobStatusChangeHandler).handleStatusChange(job, JobBuilderController.STATUS_PENDING, JobBuilderController.STATUS_INQUEUE);inSequence(sequence);
        }});

        try {
            service.queueSubmission(mockCloudComputeService, job, userDataString);
            Assert.assertTrue(service.isSubmitting(job, mockCloudComputeService));
            Thread.sleep(1000L);
        } finally {
            executor.shutdown();
        }
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

        Assert.assertEquals(instanceId, job.getComputeInstanceId());
        Assert.assertNotNull(job.getSubmitDate());
    }

    /**
     * Tests that job submission handles errors when the first request was a quota error
     * @throws Exception
     */
    @Test
    public void testJobSubmissionWithQueueError() throws Exception {
        //Instantiate our job object
        final String userDataString = "user-data";
        final VEGLJob job = new VEGLJob(213);

        job.setStatus(JobBuilderController.STATUS_PROVISION);

        service.setQuotaResubmitTime(500L);
        service.setQuotaResubmitUnits(TimeUnit.MILLISECONDS);

        Sequence sequence = new NamedSequence("queue to error sequence");

        context.checking(new Expectations() {{
            allowing(mockCloudComputeService).getId();will(returnValue("ccs-id"));

            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(Exception.class)));
            allowing(mockJobManager).createJobAuditTrail(with(any(String.class)), with(job), with(any(String.class)));

            //Our main execution sequence goes -  submit - Quota Full - In Queue - submit - error
            oneOf(mockCloudComputeService).executeJob(with(job), with(userDataString));will(throwException(new PortalServiceException("Some random error","Some error correction with Quota exceeded")));inSequence(sequence);
            oneOf(mockJobManager).saveJob(job);inSequence(sequence);
            oneOf(mockVglJobStatusChangeHandler).handleStatusChange(job, JobBuilderController.STATUS_INQUEUE, JobBuilderController.STATUS_PROVISION);inSequence(sequence);
            oneOf(mockCloudComputeService).executeJob(with(job), with(userDataString));will(throwException(new PortalServiceException("error")));inSequence(sequence);
            oneOf(mockJobManager).saveJob(job);inSequence(sequence);
            oneOf(mockVglJobStatusChangeHandler).handleStatusChange(job, JobBuilderController.STATUS_ERROR, JobBuilderController.STATUS_INQUEUE);inSequence(sequence);
        }});

        try {
            service.queueSubmission(mockCloudComputeService, job, userDataString);
            Assert.assertTrue(service.isSubmitting(job, mockCloudComputeService));
            Thread.sleep(1000L);
        } finally {
            executor.shutdown();
        }
        executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

        Assert.assertNull(job.getComputeInstanceId());
        Assert.assertNull(job.getSubmitDate());
    }
}

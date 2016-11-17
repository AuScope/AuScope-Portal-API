package org.auscope.portal.server.web.service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class for handling the provisioning of VM's using a CloudComputeService asynchronously.
 * @author Josh Vote (CSIRO)
 *
 */
@Service
public class CloudSubmissionService {

    public static final int THREAD_POOL_SIZE = 5;
    public static final long QUOTA_RESUBMIT_MINUTES = 30;

    private final Log logger = LogFactory.getLog(getClass());
    private VEGLJobManager jobManager;
    private ScheduledExecutorService executor;
    private VGLJobStatusChangeHandler vglJobStatusChangeHandler;
    private ConcurrentHashMap<String, Future<?>> submittingJobs;
    private long quotaResubmitTime = QUOTA_RESUBMIT_MINUTES;
    private TimeUnit quotaResubmitUnits = TimeUnit.MINUTES;

    @Autowired
    public CloudSubmissionService(VEGLJobManager jobManager, VGLJobStatusChangeHandler vglJobStatusChangeHandler) {
        this(jobManager, vglJobStatusChangeHandler, Executors.newScheduledThreadPool(THREAD_POOL_SIZE));
    }

    public CloudSubmissionService(VEGLJobManager jobManager,
            VGLJobStatusChangeHandler vglJobStatusChangeHandler, ScheduledExecutorService executor) {
        super();
        this.jobManager = jobManager;
        this.vglJobStatusChangeHandler = vglJobStatusChangeHandler;
        this.submittingJobs = new ConcurrentHashMap<String, Future<?>>();
        this.executor = executor;
    }

    private String generateKey(VEGLJob job, CloudComputeService cloudComputeService) {
        return String.format("%1$s-%2$s", job.getId(), cloudComputeService.getId());
    }

    public void setQuotaResubmitTime(long quotaResubmitTime) {
        this.quotaResubmitTime = quotaResubmitTime;
    }

    public void setQuotaResubmitUnits(TimeUnit quotaResubmitUnits) {
        this.quotaResubmitUnits = quotaResubmitUnits;
    }

    /**
     * Using the internal executor, submits a Runnable for submitting this job using the specified cloudComputeService.
     *
     * If the submission fails due to quota errors then the runnable will be rescheduled to run in QUOTA_RESUBMIT_MINUTES minutes
     * @param cloudComputeService The cloud compute service for recieving the job submission
     * @param job The job to be submitted
     * @param userDataString The user data string to be sent to the cloud
     * @throws PortalServiceException
     */
    public void queueSubmission(CloudComputeService cloudComputeService, VEGLJob job, String userDataString) throws PortalServiceException {
        SubmissionRunnable runnable = new SubmissionRunnable(cloudComputeService, job, userDataString, jobManager, vglJobStatusChangeHandler, submittingJobs, executor, quotaResubmitTime, quotaResubmitUnits);
        try {
            Future<?> future = executor.submit(runnable);
            submittingJobs.put(generateKey(job, cloudComputeService), future);
        } catch (RejectedExecutionException ex) {
            logger.warn("Unable to start thread for submitting job: " + ex.getMessage());
            logger.debug("Exception:", ex);
            throw new PortalServiceException("Unable to start thread for submitting job", ex);
        }
    }

    /**
     * Dequeues the specified job from the specified compute service submission queue. If the job is currently
     * submitting, this will have no effect.
     * @param job
     * @param cloudComputeService
     */
    public void dequeueSubmission(VEGLJob job, CloudComputeService cloudComputeService) {
        Future<?> future = submittingJobs.get(generateKey(job, cloudComputeService));
        if (future != null) {
            submittingJobs.remove(generateKey(job, cloudComputeService));
            future.cancel(false);
        }
    }

    /**
     * Returns true if the specified job is submitting to the specified cloudComputeService.
     * @param job
     * @param cloudComputeService
     * @return
     */
    public boolean isSubmitting(VEGLJob job, CloudComputeService cloudComputeService) {
        return submittingJobs.containsKey(generateKey(job, cloudComputeService));
    }

    private class SubmissionRunnable implements Runnable {
        private CloudComputeService cloudComputeService;
        private VEGLJob curJob;
        private String userDataString;
        private VEGLJobManager jobManager;
        private VGLJobStatusChangeHandler vglJobStatusChangeHandler;
        private ConcurrentHashMap<String, Future<?>> submittingJobs;
        private ScheduledExecutorService executor;
        private long quotaResubmitTime = QUOTA_RESUBMIT_MINUTES;
        private TimeUnit quotaResubmitUnits = TimeUnit.MINUTES;

        public SubmissionRunnable(CloudComputeService cloudComputeService, VEGLJob curJob, String userDataString, VEGLJobManager jobManager, VGLJobStatusChangeHandler vglJobStatusChangeHandler,
                ConcurrentHashMap<String, Future<?>> submittingJobs, ScheduledExecutorService executor, long quotaResubmitTime, TimeUnit quotaResubmitUnits) {
            this.cloudComputeService = cloudComputeService;
            this.curJob = curJob;
            this.userDataString = userDataString;
            this.jobManager = jobManager;
            this.vglJobStatusChangeHandler = vglJobStatusChangeHandler;
            this.submittingJobs = submittingJobs;
            this.executor = executor;
            this.quotaResubmitTime = quotaResubmitTime;
            this.quotaResubmitUnits = quotaResubmitUnits;
        }

        @Override
        public void run() {
            String instanceId = null;
            boolean deleteEntry = true;
            try {
                instanceId = cloudComputeService.executeJob(curJob, userDataString);
                if (StringUtils.isEmpty(instanceId)) {
                    throw new PortalServiceException(String.format("Null/Empty instance ID returned for submission to %1$s for job %2$s",cloudComputeService.getId(), curJob.getId()));
                }

                logger.debug("Launched instance: " + instanceId);
                // set reference as instanceId for use when killing a job
                curJob.setComputeInstanceId(instanceId);
                String oldJobStatus = curJob.getStatus();
                curJob.setStatus(JobBuilderController.STATUS_PENDING);
                jobManager.createJobAuditTrail(oldJobStatus, curJob, "Set job to Pending. Instance ID:" + instanceId);
                curJob.setSubmitDate(new Date());
                jobManager.saveJob(curJob);
                vglJobStatusChangeHandler.handleStatusChange(curJob,curJob.getStatus(),oldJobStatus);
            } catch(Exception e) {
                boolean errorState = true;
                if (e instanceof PortalServiceException &&
                    ((PortalServiceException) e).getErrorCorrection() != null &&
                    ((PortalServiceException) e).getErrorCorrection().contains("Quota exceeded")) {
                        try {
                            Future<?> newFuture = executor.schedule(this, this.quotaResubmitTime, this.quotaResubmitUnits); //reschedule this to run again in 30 minutes
                            submittingJobs.put(generateKey(curJob, cloudComputeService), newFuture);
                            deleteEntry = false;
                            String oldJobStatus = curJob.getStatus();
                            curJob.setStatus(JobBuilderController.STATUS_INQUEUE);
                            jobManager.saveJob(curJob);
                            jobManager.createJobAuditTrail(oldJobStatus, curJob, "Job Placed in Queue");
                            vglJobStatusChangeHandler.handleStatusChange(curJob,curJob.getStatus(),oldJobStatus);
                            errorState = false;
                        } catch (RejectedExecutionException ex) {
                            logger.error("Cannot reschedule job submission:" + ex.getMessage());
                            logger.debug("Exception:", ex);
                        }
                }

                if (errorState) {
                    String oldJobStatus = curJob.getStatus();
                    curJob.setStatus(JobBuilderController.STATUS_ERROR);
                    jobManager.saveJob(curJob);
                    jobManager.createJobAuditTrail(oldJobStatus, curJob, e);
                    vglJobStatusChangeHandler.handleStatusChange(curJob,curJob.getStatus(),oldJobStatus);
                }
            } finally {
                if (deleteEntry) {
                    submittingJobs.remove(generateKey(curJob, cloudComputeService));
                }
            }
        }
    }
}

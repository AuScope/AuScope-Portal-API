package org.auscope.portal.server.web.service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

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
    @Autowired
    private VEGLJobManager jobManager;
    @Autowired
    private VGLJobStatusChangeHandler vglJobStatusChangeHandler;
    private ScheduledExecutorService executor;
    private ConcurrentHashMap<String, Future<?>> submittingJobs;
    private long quotaResubmitTime = QUOTA_RESUBMIT_MINUTES;
    private TimeUnit quotaResubmitUnits = TimeUnit.MINUTES;

    public CloudSubmissionService() {
        this(Executors.newScheduledThreadPool(THREAD_POOL_SIZE));
    }

    public CloudSubmissionService(ScheduledExecutorService executor) {
        super();
        this.submittingJobs = new ConcurrentHashMap<String, Future<?>>();
        this.executor = executor;
    }

    @PostConstruct
    public void init() {
        this.vglJobStatusChangeHandler.getJobStatusLogReader().setCloudSubmissionService(this);
    }

    public VEGLJobManager getJobManager() {
        return jobManager;
    }

    public void setJobManager(VEGLJobManager jobManager) {
        this.jobManager = jobManager;
    }

    public VGLJobStatusChangeHandler getVglJobStatusChangeHandler() {
        return vglJobStatusChangeHandler;
    }

    public void setVglJobStatusChangeHandler(VGLJobStatusChangeHandler vglJobStatusChangeHandler) {
        this.vglJobStatusChangeHandler = vglJobStatusChangeHandler;
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
            //Make sure we synchronize so that updates to the job/cache can't start until
            //this future is properly put in the cache
            synchronized(submittingJobs) {
                Future<?> future = executor.submit(runnable);
                submittingJobs.put(generateKey(job, cloudComputeService), future);
            }
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
     *
     * Any updates to the internal cache will be synchronized against job status updates so if this
     * method returns false then you can be certain that the underlying job has been saved to the DB
     *
     * @param job
     * @param cloudComputeService
     * @return
     */
    public boolean isSubmitting(VEGLJob job, CloudComputeService cloudComputeService) {
        synchronized(submittingJobs) {
            return submittingJobs.containsKey(generateKey(job, cloudComputeService));
        }
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
            boolean successfulSubmit = false;
            boolean reschedule = false;
            Exception caughtException = null;

            try {
                instanceId = cloudComputeService.executeJob(curJob, userDataString);
                if (StringUtils.isEmpty(instanceId)) {
                    throw new PortalServiceException(String.format("Null/Empty instance ID returned for submission to %1$s for job %2$s",cloudComputeService.getId(), curJob.getId()));
                }
                logger.debug("Launched instance: " + instanceId);

                successfulSubmit = true;
            } catch(Exception e) {
                caughtException = e;
                successfulSubmit = false;
                if (e instanceof PortalServiceException &&
                    ((PortalServiceException) e).getErrorCorrection() != null &&
                    ((PortalServiceException) e).getErrorCorrection().contains("Quota exceeded")) {
                    reschedule = true;
                }
            }

            //Update job status / fire listeners.
            String oldJobStatus = curJob.getStatus();
            synchronized(submittingJobs) {
                if (successfulSubmit) {
                    //Everything went OK
                    curJob.setComputeInstanceId(instanceId);
                    curJob.setStatus(JobBuilderController.STATUS_PENDING);
                    jobManager.createJobAuditTrail(oldJobStatus, curJob, "Set job to Pending. Instance ID:" + instanceId);
                    curJob.setSubmitDate(new Date());
                    jobManager.saveJob(curJob);
                    vglJobStatusChangeHandler.handleStatusChange(curJob,curJob.getStatus(),oldJobStatus);
                    submittingJobs.remove(generateKey(curJob, cloudComputeService));
                } else if (reschedule) {
                    //Can't get resources now - reschedule for future run
                    try {
                        Future<?> newFuture = executor.schedule(this, this.quotaResubmitTime, this.quotaResubmitUnits); //reschedule this to run again in 30 minutes
                        submittingJobs.put(generateKey(curJob, cloudComputeService), newFuture);
                        curJob.setStatus(JobBuilderController.STATUS_INQUEUE);
                        jobManager.saveJob(curJob);
                        jobManager.createJobAuditTrail(oldJobStatus, curJob, "Job Placed in Queue");
                        vglJobStatusChangeHandler.handleStatusChange(curJob,curJob.getStatus(),oldJobStatus);
                    } catch (RejectedExecutionException ex) {
                        //This is bad - can't submit more jobs for queue - forced to kill job submission
                        logger.error("Cannot reschedule job submission:" + ex.getMessage());
                        logger.debug("Exception:", ex);
                        curJob.setStatus(JobBuilderController.STATUS_ERROR);
                        submittingJobs.remove(generateKey(curJob, cloudComputeService));
                        jobManager.saveJob(curJob);
                        jobManager.createJobAuditTrail(oldJobStatus, curJob, "Unable to queue job for resubmission: " + ex.getMessage());
                        vglJobStatusChangeHandler.handleStatusChange(curJob,curJob.getStatus(),oldJobStatus);
                    }
                } else {
                    //Error state
                    curJob.setStatus(JobBuilderController.STATUS_ERROR);
                    jobManager.saveJob(curJob);
                    jobManager.createJobAuditTrail(oldJobStatus, curJob, caughtException);
                    vglJobStatusChangeHandler.handleStatusChange(curJob,curJob.getStatus(),oldJobStatus);
                    submittingJobs.remove(generateKey(curJob, cloudComputeService));
                }
            }
        }
    }
}

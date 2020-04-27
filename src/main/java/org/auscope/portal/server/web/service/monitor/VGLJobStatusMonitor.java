package org.auscope.portal.server.web.service.monitor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.NCIDetails;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.auscope.portal.server.web.service.NCIDetailsService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * A task that monitors any pending or active VL jobs. It
 * will trigger JobStatusChangeListener(s) to run when the
 * job being processed changes its status.
 *
 * The timing for running this task is configured in
 * applicationContext.xml file.
 *
 * It uses VEGLJobManager to retrieve pending or active job(s)
 * from the database and VGLJobStatusAndLogReader to poll
 * each job execution status from s3 cloud storage.
 *
 * @author Richard Goh
 */
public class VGLJobStatusMonitor extends QuartzJobBean {
    private final Log LOG = LogFactory.getLog(getClass());

    private VEGLJobManager jobManager;
    private JobStatusMonitor jobStatusMonitor;
    private ANVGLUserService jobUserService;
    private NCIDetailsService nciDetailsService;
    
    
    // Solely for testing
    public void setNciDetailsService(NCIDetailsService nciDetailsService) {
        this.nciDetailsService = nciDetailsService;
    }

    public void setJobUserService(ANVGLUserService jobUserService) {
        this.jobUserService = jobUserService;
    }
    
    /**
     * Sets the job manager to be used for querying
     * pending or active jobs from VL DB.
     * @param jobManager
     */
    public void setJobManager(VEGLJobManager jobManager) {
        this.jobManager = jobManager;
    }

    /**
     * Sets the JobStatusMonitor to be used by this class
     * @param jobStatusMonitor
     */
    public void setJobStatusMonitor(JobStatusMonitor jobStatusMonitor) {
        this.jobStatusMonitor = jobStatusMonitor;
    }

    @Override
    protected void executeInternal(JobExecutionContext ctx)
            throws JobExecutionException {
        try {
            List<VEGLJob> jobs = jobManager.getPendingOrActiveJobs();
            for (VEGLJob veglJob : jobs) {
                ANVGLUser user = jobUserService.getByEmail(veglJob.getEmailAddress());
                veglJob.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
                veglJob.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
                NCIDetails nciDetails = nciDetailsService.getByUser(user);
                if (nciDetails != null) {
                    veglJob.setProperty(NCIDetails.PROPERTY_NCI_USER, nciDetails.getUsername());
                    veglJob.setProperty(NCIDetails.PROPERTY_NCI_PROJECT, nciDetails.getProject());
                    veglJob.setProperty(NCIDetails.PROPERTY_NCI_KEY, nciDetails.getKey());
                }
            }
            jobStatusMonitor.statusUpdate(jobs);
        } catch (Exception ex) {
            LOG.info(String.format("Error update jobs: %1$s", ex.getMessage()));
            LOG.debug("Exception:", ex);
            throw new JobExecutionException(ex);
        }
    }
}
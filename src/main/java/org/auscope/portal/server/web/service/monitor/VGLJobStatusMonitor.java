package org.auscope.portal.server.web.service.monitor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.cloud.monitor.JobStatusChangeListener;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * A task that monitors any pending or active VGL jobs. It
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
    private VGLJobStatusAndLogReader jobStatusLogReader;
    private JobStatusChangeListener[] jobStatusChangeListeners;
    
    /**
     * Sets the job manager to be used for querying 
     * pending or active jobs from VGL DB.
     * @param jobManager
     */
    public void setJobManager(VEGLJobManager jobManager) {
        this.jobManager = jobManager;
    }
    
    /**
     * Sets the job status log reader to be used for
     * querying job status from S3 storage.
     * @param jobStatusLogReader
     */
    public void setJobStatusLogReader(VGLJobStatusAndLogReader jobStatusLogReader) {
        this.jobStatusLogReader = jobStatusLogReader;
    }
    
    /**
     * Sets a list of JobStatusChangeListener objects to be 
     * used handling job status change.
     * @param jobStatusChangeListeners
     */
    public void setJobStatusChangeListeners(JobStatusChangeListener[] jobStatusChangeListeners) {
        this.jobStatusChangeListeners = jobStatusChangeListeners;
    }
    
    private void statusChanged(VEGLJob job, String newStatus, String oldStatus) {
        for (JobStatusChangeListener l : jobStatusChangeListeners) {
            try {
                l.handleStatusChange(job, newStatus, oldStatus);
            } catch (Exception ex) {
                //Simply log it if the event handler fails and move on
                LOG.error("An error has occurred while handling status change event.", ex);
            }
        }
    }

    @Override
    protected void executeInternal(JobExecutionContext ctx)
            throws JobExecutionException {
        List<VEGLJob> jobs = jobManager.getPendingOrActiveJobs();
        LOG.trace("Number of pending or active job(s): [" + jobs.size() + "]");

        for (VEGLJob job : jobs) {
            String oldStatus = job.getStatus();
            String newStatus = jobStatusLogReader.getJobStatus(job);
            if (newStatus != null && !newStatus.equals(oldStatus)) {
                statusChanged(job, newStatus, oldStatus);
            } else {
                LOG.trace("Skip bad or status quo job. Job id: " + job.getId());
            }
        }
    }
}
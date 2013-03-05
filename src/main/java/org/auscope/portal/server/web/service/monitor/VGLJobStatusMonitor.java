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
 * 
 * @author Richard Goh
 */
public class VGLJobStatusMonitor extends QuartzJobBean {
    private final Log LOG = LogFactory.getLog(getClass());
    
    private VEGLJobManager jobManager;
    private VGLJobStatusAndLogReader jobStatusLogReader;
    private JobStatusChangeListener[] jobStatusChangeListeners;
    
    public void setJobManager(VEGLJobManager jobManager) {
        this.jobManager = jobManager;
    }
    
    public void setJobStatusLogReader(VGLJobStatusAndLogReader jobStatusLogReader) {
        this.jobStatusLogReader = jobStatusLogReader;
    }
    
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
        LOG.trace("Number of pending or active job(s): [" + jobs.size()
                + "] retrieved by JobManager [" + jobManager.hashCode() + "]");

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
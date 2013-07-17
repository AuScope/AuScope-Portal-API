package org.auscope.portal.server.web.service.monitor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
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
    private JobStatusMonitor jobStatusMonitor;
    
    /**
     * Sets the job manager to be used for querying 
     * pending or active jobs from VGL DB.
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
        List<VEGLJob> jobs = jobManager.getPendingOrActiveJobs();
        
        try {
            jobStatusMonitor.statusUpdate(jobs);
        } catch (Exception ex) {
            LOG.info(String.format("Error update jobs: %1$s", ex.getMessage()));
            LOG.debug("Exception:", ex);
            throw new JobExecutionException(ex);
        }
    }
}
package org.auscope.portal.server.vegl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.NCIDetails;
import org.auscope.portal.server.web.service.NCIDetailsService;
import org.auscope.portal.server.web.service.VEGLJobService;
import org.auscope.portal.server.web.service.VEGLSeriesService;
import org.auscope.portal.server.web.service.VGLJobAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that talks to the data objects to retrieve or save data
 *
 * @author Cihan Altinay
 * @author Josh Vote
 * @author Richard Goh
 */
@Component
public class VEGLJobManager {
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private VEGLJobService jobService;
    
    @Autowired
    private VEGLSeriesService seriesService;
    
    @Autowired
    private VGLJobAuditLogService jobAuditLogService;
    
    @Autowired
    private NCIDetailsService nciDetailsService;
    

    public List<VEGLSeries> querySeries(String user, String name, String desc) {
    	return seriesService.query(user, name, desc);
    }

    public List<VEGLJob> getSeriesJobs(int seriesId, ANVGLUser user) throws PortalServiceException {
    	List<VEGLJob> jobs = jobService.getJobsOfSeries(seriesId, user);
        return applyNCIDetails(jobs, user);
    }

    public List<VEGLJob> getUserJobs(ANVGLUser user) throws PortalServiceException {
    	List<VEGLJob> jobs = jobService.getJobsOfUser(user);
        return applyNCIDetails(jobs, user);
    }

    public List<VEGLJob> getPendingOrActiveJobs() {
    	return jobService.getPendingOrActiveJobs();
    }

    public List<VEGLJob> getInQueueJobs() {
    	return jobService.getInQueueJobs();
    }

    public VEGLJob getJobById(int jobId, ANVGLUser user) throws PortalServiceException {
    	return applyNCIDetails(jobService.get(jobId, user), user);
    }

    public VEGLJob getJobById(int jobId, String stsArn, String clientSecret, String s3Role, String userEmail, String nciUser, String nciProj, String nciKey) {
    	return jobService.get(jobId, stsArn, clientSecret, s3Role, userEmail, nciUser, nciProj, nciKey);
    }

    public void deleteJob(VEGLJob job) {
    	jobService.deleteJob(job);
    }

    public VEGLSeries getSeriesById(int seriesId, String userEmail) {
    	return seriesService.get(seriesId, userEmail);
    }

    public void saveJob(VEGLJob veglJob) {
    	jobService.saveJob(veglJob);
    }

    /**
     * Create the job life cycle audit trail. If the creation is unsuccessful, it
     * will silently fail and log the failure message to error log.
     * @param oldJobStatus
     * @param curJob
     * @param message
     */
    public void createJobAuditTrail(String oldJobStatus, VEGLJob curJob, String message) {
        VGLJobAuditLog vglJobAuditLog = null;
        try {
            vglJobAuditLog = new VGLJobAuditLog();
            vglJobAuditLog.setJobId(curJob.getId());
            vglJobAuditLog.setFromStatus(oldJobStatus);
            vglJobAuditLog.setToStatus(curJob.getStatus());
            vglJobAuditLog.setTransitionDate(new Date());
            vglJobAuditLog.setMessage(message);

            // Failure in the creation of the job life cycle audit trail is
            // not critical hence we allow it to fail silently and log it.
            jobAuditLogService.save(vglJobAuditLog);
        } catch (Exception ex) {
            logger.warn("Error creating audit trail for job: " + vglJobAuditLog, ex);
        }
    }

    /**
     * Create the job life cycle audit trail. If the creation is unsuccessful, it
     * will silently fail and log the failure message to error log.
     * @param oldJobStatus
     * @param curJob
     * @param message
     */
    public void createJobAuditTrail(String oldJobStatus, VEGLJob curJob, Throwable exception) {
        String message = ExceptionUtils.getStackTrace(exception);
        if(message.length() > 1000){
            message = message.substring(0,1000);
        }
        VGLJobAuditLog vglJobAuditLog = null;
        try {
            vglJobAuditLog = new VGLJobAuditLog();
            vglJobAuditLog.setJobId(curJob.getId());
            vglJobAuditLog.setFromStatus(oldJobStatus);
            vglJobAuditLog.setToStatus(curJob.getStatus());
            vglJobAuditLog.setTransitionDate(new Date());
            vglJobAuditLog.setMessage(message);

            // Failure in the creation of the job life cycle audit trail is
            // not critical hence we allow it to fail silently and log it.
            jobAuditLogService.save(vglJobAuditLog);
        } catch (Exception ex) {
            logger.warn("Error creating audit trail for job: " + vglJobAuditLog, ex);
        }
    }

    public void deleteSeries(VEGLSeries series) {
    	seriesService.delete(series);
    }

    public void saveSeries(VEGLSeries series) {
    	seriesService.save(series);
    }

    // These are solely for tests
    public void setVeglJobService(VEGLJobService jobService) {
        this.jobService = jobService;
    }

    public void setVeglSeriesService(VEGLSeriesService seriesService) {
        this.seriesService = seriesService;
    }

    public void setVglJobAuditLogService(VGLJobAuditLogService jobAuditLogService) {
        this.jobAuditLogService = jobAuditLogService;
    }

    /*
    public NCIDetailsService getNciDetailsService() {
        return nciDetailsService;
    }
    */

    public void setNciDetailsService(NCIDetailsService nciDetailsService) {
        this.nciDetailsService = nciDetailsService;
    }
    
    private VEGLJob applyNCIDetails(VEGLJob job, NCIDetails nciDetails) {
        if (nciDetails != null) {
            try {
                nciDetails.applyToJobProperties(job);
            } catch (Exception e) {
                logger.error("Unable to apply nci details to job:", e);
                throw new RuntimeException("Unable to decrypt NCI Details", e);
            }
        }

        return job;
    }

    private VEGLJob applyNCIDetails(VEGLJob job, ANVGLUser user) throws PortalServiceException {
        if (job == null) {
            return null;
        }
        return applyNCIDetails(job, nciDetailsService.getByUser(user));
    }

    private List<VEGLJob> applyNCIDetails(List<VEGLJob> jobs, ANVGLUser user) throws PortalServiceException {
    	NCIDetails nciDetails = nciDetailsService.getByUser(user);

        if (nciDetails != null) {
            for (VEGLJob job: jobs) {
                applyNCIDetails(job, nciDetails);
            }
        }

        return jobs;
    }

}
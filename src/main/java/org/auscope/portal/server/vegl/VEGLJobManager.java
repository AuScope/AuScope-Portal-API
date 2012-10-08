package org.auscope.portal.server.vegl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that talks to the data objects to retrieve or save data
 *
 * @author Cihan Altinay
 * @author Josh Vote
 * @author Richard Goh
 */
public class VEGLJobManager {
    protected final Log logger = LogFactory.getLog(getClass());

    private VEGLJobDao veglJobDao;
    private VEGLSeriesDao veglSeriesDao;
    private VGLJobAuditLogDao vglJobAuditLogDao;

    public List<VEGLSeries> querySeries(String user, String name, String desc) {
        return veglSeriesDao.query(user, name, desc);
    }

    public List<VEGLJob> getSeriesJobs(int seriesId) {
        return veglJobDao.getJobsOfSeries(seriesId);
    }

    public VEGLJob getJobById(int jobId) {
        return veglJobDao.get(jobId);
    }

    public void deleteJob(VEGLJob job) {
        veglJobDao.deleteJob(job);
    }

    public VEGLSeries getSeriesById(int seriesId) {
        return veglSeriesDao.get(seriesId);
    }

    public void saveJob(VEGLJob veglJob) {
        veglJobDao.save(veglJob);
    }

    /**
     * Create the job life cycle audit trail. If the creation is unsuccessful, it
     * will silently fail and log the failure message to error log.
     * @param oldJobStatus
     * @param curJob
     * @param message
     */
    public void createJobAuditTrail(String oldJobStatus, VEGLJob curJob, String message) {
        VGLJobAuditLog vglJobAuditLog = new VGLJobAuditLog();
        vglJobAuditLog.setJobId(curJob.getId());
        vglJobAuditLog.setFromStatus(oldJobStatus);
        vglJobAuditLog.setToStatus(curJob.getStatus());
        vglJobAuditLog.setTransitionDate(new Date());
        vglJobAuditLog.setMessage(message);

        try {
            // Failure in the creation of the job life cycle audit trail is
            // not critical hence we allow it to fail silently and log it.
            vglJobAuditLogDao.save(vglJobAuditLog);
        } catch (Exception ex) {
            logger.warn("Error creating audit trail for job: " + vglJobAuditLog, ex);
        }
    }

    public void deleteSeries(VEGLSeries series) {
        veglSeriesDao.delete(series);
    }

    public void saveSeries(VEGLSeries series) {
        veglSeriesDao.save(series);
    }

    public void setVeglJobDao(VEGLJobDao veglJobDao) {
        this.veglJobDao = veglJobDao;
    }

    public void setVeglSeriesDao(VEGLSeriesDao veglSeriesDao) {
        this.veglSeriesDao = veglSeriesDao;
    }

    public void setVglJobAuditLogDao(VGLJobAuditLogDao vglJobAuditLogDao) {
        this.vglJobAuditLogDao = vglJobAuditLogDao;
    }
}
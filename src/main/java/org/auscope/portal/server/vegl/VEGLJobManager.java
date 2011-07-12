package org.auscope.portal.server.vegl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that talks to the data objects to retrieve or save data
 * 
 * @author Cihan Altinay
 * @author Josh Vote
 *
 */
public class VEGLJobManager {
	protected final Log logger = LogFactory.getLog(getClass());

    private VEGLJobDao veglJobDao;
    private VEGLSeriesDao veglSeriesDao;

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
}

package org.auscope.portal.server.gridjob;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class VRLJobManager {
    protected final Log logger = LogFactory.getLog(getClass());

    private VRLJobDao vrlJobDao;
    private VRLSeriesDao vrlSeriesDao;

    public List<VRLSeries> querySeries(String user, String name, String desc) {
        return vrlSeriesDao.query(user, name, desc);
    }

    public List<VRLJob> getSeriesJobs(int seriesId) {
        return vrlJobDao.getJobsOfSeries(seriesId);
    }

    public VRLJob getJobById(int jobId) {
        return vrlJobDao.get(jobId);
    }

    public VRLSeries getSeriesById(int seriesId) {
        return vrlSeriesDao.get(seriesId);
    }

    public void saveJob(VRLJob vrlJob) {
        vrlJobDao.save(vrlJob);
    }

    public void saveSeries(VRLSeries series) {
        vrlSeriesDao.save(series);
    }

    public void setVRLJobDao(VRLJobDao vrlJobDao) {
        this.vrlJobDao = vrlJobDao;
    }

    public void setVRLSeriesDao(VRLSeriesDao vrlSeriesDao) {
        this.vrlSeriesDao = vrlSeriesDao;
    }
}


/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that talks to the data objects to retrieve or save data
 *
 * @author Cihan Altinay
 */
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


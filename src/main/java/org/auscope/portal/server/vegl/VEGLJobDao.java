package org.auscope.portal.server.vegl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A data access object for VEGLJob 
 * @author Josh Vote
 *
 */
public class VEGLJobDao extends HibernateDaoSupport {
	protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Retrieves jobs that are grouped under given series
     *
     * @param seriesID the ID of the series
     */
    public List<VEGLJob> getJobsOfSeries(final int seriesID) {
        return (List<VEGLJob>) getHibernateTemplate()
            .findByNamedParam("from VEGLJob j where j.seriesId=:searchID",
                    "searchID", seriesID);
    }

    /**
     * Retrieves jobs that belong to a specific email
     *
     * @param emailAddress the email whose jobs are to be retrieved
    
     */
    public List<VEGLJob> getJobsByEmail(final String emailAddress) {
        return (List<VEGLJob>) getHibernateTemplate()
            .findByNamedParam("from VEGLJob j where j.emailAddress=:email",
                    "email", emailAddress);
    }

    /**
     * Retrieves the job with given ID.
     */
    public VEGLJob get(final int id) {
        return (VEGLJob) getHibernateTemplate().get(VEGLJob.class, id);
    }
    
    
    /**
     * Deletes the job with given ID.
     */
    public void deleteJob(final VEGLJob job) {
        getHibernateTemplate().delete(job);
    }

    /**
     * Saves or updates the given job.
     */
    public void save(final VEGLJob job) {
        getHibernateTemplate().saveOrUpdate(job);
    }
}

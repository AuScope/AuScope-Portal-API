package org.auscope.portal.server.gridjob;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A Hibernate-backed VRLJob data object
 */
public class VRLJobDao extends HibernateDaoSupport {

    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Retrieves jobs that are grouped under given series
     *
     * @param seriesID the ID of the series
     *
     * @return list of <code>VRLJob</code> objects belonging to given series
     */
    public List<VRLJob> getJobsOfSeries(final int seriesID) {
        return (List<VRLJob>) getHibernateTemplate()
            .findByNamedParam("from VRLJob j where j.seriesId=:searchID",
                    "searchID", seriesID);
    }

    /**
     * Retrieves jobs that belong to a specific user
     *
     * @param user the user whose jobs are to be retrieved
     *
     * @return list of <code>VRLJob</code> objects belonging to given user
     */
    public List<VRLJob> getJobsByUser(final String user) {
        return (List<VRLJob>) getHibernateTemplate()
            .findByNamedParam("from VRLJob j where j.user=:searchUser",
                    "searchUser", user);
        /*
        return sessionFactory.getCurrentSession()
            .createQuery("from jobs j where j.user=:searchUser")
            .setString("searchUser", user)
            .list();
        */
    }

    /**
     * Retrieves the job with given ID.
     *
     * @return <code>VRLJob</code> object with given ID.
     */
    public VRLJob get(final int id) {
        return (VRLJob) getHibernateTemplate().get(VRLJob.class, id);
    }

    /**
     * Saves or updates the given job.
     */
    public void save(final VRLJob job) {
        getHibernateTemplate().saveOrUpdate(job);
    }
}


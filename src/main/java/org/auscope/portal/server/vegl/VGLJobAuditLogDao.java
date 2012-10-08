package org.auscope.portal.server.vegl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A hibernate-backed VGLJobAuditLog data access object.
 *
 * @author Richard Goh
 */
public class VGLJobAuditLogDao extends HibernateDaoSupport {
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Retrieves the audit logs of a given job ID.
     *
     * @param jobId the id of a job
     */
    public List<VGLJobAuditLog> getJobsOfSeries(final int jobId) {
        return (List<VGLJobAuditLog>) getHibernateTemplate().findByNamedParam(
                "from VGLJobAuditLog j where j.jobId=:jobId", "jobId", jobId);
    }

    /**
     * Retrieves the series with given ID.
     */
    public VGLJobAuditLog get(final int id) {
        return (VGLJobAuditLog) getHibernateTemplate().get(VGLJobAuditLog.class, id);
    }

    /**
     * Saves or updates the given series.
     */
    public void save(final VGLJobAuditLog jobAuditLog) {
        getHibernateTemplate().saveOrUpdate(jobAuditLog);
    }
}
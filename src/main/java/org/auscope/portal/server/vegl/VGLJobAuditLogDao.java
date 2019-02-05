package org.auscope.portal.server.vegl;

import java.util.List;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

/**
 * A hibernate-backed VGLJobAuditLog data access object.
 *
 * @author Richard Goh
 */
public class VGLJobAuditLogDao extends HibernateDaoSupport {
    public VGLJobAuditLogDao() {
        super();
    }

    /**
     * Retrieves the audit logs of a given job ID.
     *
     * @param jobId the id of a job
     */
    @SuppressWarnings("unchecked")
    public List<VGLJobAuditLog> getAuditLogsOfJob(final int jobId) {
        return (List<VGLJobAuditLog>) getHibernateTemplate().findByNamedParam(
                "from VGLJobAuditLog j where j.jobId=:jobId", "jobId", jobId);
    }

    /**
     * Retrieves the series with given ID.
     */
    public VGLJobAuditLog get(final int id) {
        return getHibernateTemplate().get(VGLJobAuditLog.class, id);
    }

    /**
     * Saves or updates the given series.
     */
    public void save(final VGLJobAuditLog jobAuditLog) {
        getHibernateTemplate().saveOrUpdate(jobAuditLog);
    }
}
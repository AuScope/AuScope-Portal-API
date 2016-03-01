package org.auscope.portal.server.vegl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A data access object for VEGLJob
 * @author Josh Vote
 *
 */
public class VEGLJobDao extends HibernateDaoSupport {
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * Retrieves jobs that are grouped under given series.
     * It excludes jobs that are deleted.
     *
     * @param seriesID the ID of the series
     * @param user 
     */
    @SuppressWarnings("unchecked")
    public List<VEGLJob> getJobsOfSeries(final int seriesID, ANVGLUser user) {
        List<VEGLJob> res = (List<VEGLJob>) getHibernateTemplate()
            .findByNamedParam("from VEGLJob j where j.seriesId=:searchID and lower(j.status)!='deleted'",
                    "searchID", seriesID);
        for (VEGLJob job : res) {
            job.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
            job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
            job.setProperty(CloudJob.PROPERTY_S3_ROLE, user.getArnStorage()); 
        }
        return res;
    }

    /**
     * Retrieves jobs that belong to a specific email
     *
     * @param emailAddress the email whose jobs are to be retrieved
     */
    @SuppressWarnings("unchecked")
    public List<VEGLJob> getJobsByEmail(final String emailAddress) {
        return (List<VEGLJob>) getHibernateTemplate()
            .findByNamedParam("from VEGLJob j where j.emailAddress=:email",
                    "email", emailAddress);
    }

    /**
     * Retrieves jobs that are either pending or active.
     *
     * @return a list of pending or active jobs.
     */
    @SuppressWarnings("unchecked")
    public List<VEGLJob> getPendingOrActiveJobs() {
        String query = "from VEGLJob j where lower(j.status)='"
                + JobBuilderController.STATUS_PENDING + "' or lower(j.status)='"
                + JobBuilderController.STATUS_ACTIVE + "'";
        return (List<VEGLJob>) getHibernateTemplate().find(query);
    }

    /**getInQueueJobs
     * Retrieves jobs that are either pending or active.
     *
     * @return a list of pending or active jobs.
     */
    @SuppressWarnings("unchecked")
    public List<VEGLJob> getInQueueJobs() {
        String query = "from VEGLJob j where lower(j.status)='"
                + JobBuilderController.STATUS_INQUEUE + "'";

        return (List<VEGLJob>) getHibernateTemplate().find(query);
    }

    /**
     * Retrieves the job with given ID.
     * @param user 
     */
    public VEGLJob get(final int id, ANVGLUser user) {
        VEGLJob job = (VEGLJob) getHibernateTemplate().get(VEGLJob.class, id);
        job.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
        job.setProperty(CloudJob.PROPERTY_S3_ROLE, user.getArnStorage());
        return job;
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

    public VEGLJob get(int id, String stsArn, String clientSecret, String s3Role) {
        VEGLJob job = (VEGLJob) getHibernateTemplate().get(VEGLJob.class, id);
        job.setProperty(CloudJob.PROPERTY_STS_ARN, stsArn);
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, clientSecret);
        job.setProperty(CloudJob.PROPERTY_S3_ROLE, s3Role);
        return job;
    }
}

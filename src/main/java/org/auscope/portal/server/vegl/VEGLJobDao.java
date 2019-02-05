package org.auscope.portal.server.vegl;

import java.util.List;

import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.NCIDetails;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.security.access.AccessDeniedException;

/**
 * A data access object for VEGLJob
 * @author Josh Vote
 *
 */
public class VEGLJobDao extends HibernateDaoSupport {
    /**
     * Retrieves jobs that are grouped under given series.
     * It excludes jobs that are deleted.
     *
     * @param seriesID the ID of the series
     * @param user
     */
    @SuppressWarnings("unchecked")
    public List<VEGLJob> getJobsOfSeries(final int seriesID, ANVGLUser user) {
        String[] paramKeys = new String[] {"searchID", "email"};
        Object[] paramVals = new Object[] {seriesID, user.getEmail()};
        List<VEGLJob> res = (List<VEGLJob>) getHibernateTemplate()
            .findByNamedParam("from VEGLJob j where j.seriesId=:searchID and j.emailAddress=:email and lower(j.status)!='deleted'",
                    paramKeys, paramVals);
        for (VEGLJob job : res) {
            job.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
            job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
            job.setProperty(CloudJob.PROPERTY_S3_ROLE, user.getArnStorage());
        }
        return res;
    }

    /**
     * Retrieves jobs that are grouped under a given user.
     * It excludes jobs that are deleted.
     *
     * @param user
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<VEGLJob> getJobsOfUser(ANVGLUser user) {
        List<VEGLJob> res = (List<VEGLJob>) getHibernateTemplate()
            .findByNamedParam("from VEGLJob j where j.emailAddress=:email and lower(j.status)!='deleted'",
                    "email", user.getEmail());

        for (VEGLJob job : res) {
            job.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
            job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
            job.setProperty(CloudJob.PROPERTY_S3_ROLE, user.getArnStorage());
        }

        return res;
    }


//    /**
//     * Retrieves jobs that belong to a specific email
//     *
//     * @param emailAddress the email whose jobs are to be retrieved
//     */
//    @SuppressWarnings("unchecked")
//    public List<VEGLJob> getJobsByEmail(final String emailAddress) {
//        return (List<VEGLJob>) getHibernateTemplate()
//            .findByNamedParam("from VEGLJob j where j.emailAddress=:email",
//                    "email", emailAddress);
//    }

    /**
     * Retrieves jobs that are either pending or active.
     *
     * !!! Does not check authorization. Internal use only. Never expose to web end point.
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
     * !!! Does not check authorization. Internal use only. Never expose to web end point.
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
        VEGLJob job = getHibernateTemplate().get(VEGLJob.class, id);
        job.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
        job.setProperty(CloudJob.PROPERTY_S3_ROLE, user.getArnStorage());
        if( job.getEmailAddress() == null || user.getEmail()==null || (!job.getEmailAddress().trim().equalsIgnoreCase(user.getEmail().trim()) ))
            throw new AccessDeniedException("User does not have access to the requested job");

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

    public VEGLJob get(int id, String stsArn, String clientSecret, String s3Role, String userEmail, String nciUser, String nciProj, String nciKey) {
        VEGLJob job = getHibernateTemplate().get(VEGLJob.class, id);

        if( job.getEmailAddress() == null || userEmail==null || (!job.getEmailAddress().trim().equalsIgnoreCase(userEmail.trim()) ))
            throw new AccessDeniedException("User does not have access to the requested job");

        job.setProperty(CloudJob.PROPERTY_STS_ARN, stsArn);
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, clientSecret);
        job.setProperty(CloudJob.PROPERTY_S3_ROLE, s3Role);
        job.setProperty(NCIDetails.PROPERTY_NCI_USER, nciUser);
        job.setProperty(NCIDetails.PROPERTY_NCI_PROJECT, nciProj);
        job.setProperty(NCIDetails.PROPERTY_NCI_KEY, nciKey);
        return job;
    }
}

package org.auscope.portal.server.web.service;

import java.util.List;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.web.repositories.VEGLJobRepository;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.NCIDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class VEGLJobService {
	
	@Autowired
	private VEGLJobRepository jobRepository;
	
	
	/**
     * Retrieves jobs that are grouped under given series.
     * It excludes jobs that are deleted.
     *
     * @param seriesID the ID of the series
     * @param user
     */
    public List<VEGLJob> getJobsOfSeries(final int seriesId, ANVGLUser user) {
        List<VEGLJob> res = jobRepository.findBySeriesIdAndEmail(seriesId, user.getEmail());
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
    public List<VEGLJob> getJobsOfUser(ANVGLUser user) {
    	List<VEGLJob> res = jobRepository.findByEmail(user.getEmail());
        for (VEGLJob job : res) {
            job.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
            job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
            job.setProperty(CloudJob.PROPERTY_S3_ROLE, user.getArnStorage());
        }

        return res;
    }

    /**
     * Retrieves jobs that are either pending or active.
     *
     * !!! Does not check authorization. Internal use only. Never expose to web end point.
     * @return a list of pending or active jobs.
     */
    public List<VEGLJob> getPendingOrActiveJobs() {
    	return jobRepository.findPendingOrActiveJobs();
    }

    /**getInQueueJobs
     * Retrieves jobs that are either pending or active.
     *
     * !!! Does not check authorization. Internal use only. Never expose to web end point.
     * @return a list of pending or active jobs.
     */
    public List<VEGLJob> getInQueueJobs() {
    	return jobRepository.findInqueueJobs();
    }

    /**
     * Retrieves the job with given ID.
     * @param user
     */
    public VEGLJob get(final int id, ANVGLUser user) {
    	VEGLJob job = jobRepository.findById(id).orElse(null);
        if(job != null) {
	        job.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
	        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
	        job.setProperty(CloudJob.PROPERTY_S3_ROLE, user.getArnStorage());
	        if( job.getEmailAddress() == null || user.getEmail()==null || (!job.getEmailAddress().trim().equalsIgnoreCase(user.getEmail().trim()) ))
	            throw new AccessDeniedException("User does not have access to the requested job");
        }
        return job;
    }

    /**
     * Deletes the job with given ID.
     */
    public void deleteJob(final VEGLJob job) {
    	jobRepository.delete(job);
    }

    /**
     * Saves or updates the given job.
     */
    public void saveJob(final VEGLJob job) {
        //getHibernateTemplate().saveOrUpdate(job);
    	jobRepository.save(job);
    }

    public VEGLJob get(int id, String stsArn, String clientSecret, String s3Role, String userEmail, String nciUser, String nciProj, String nciKey) {
    	VEGLJob job = jobRepository.findById(id).orElse(null);
    	if(job != null) {
	        if( job.getEmailAddress() == null || userEmail==null || (!job.getEmailAddress().trim().equalsIgnoreCase(userEmail.trim()) ))
	            throw new AccessDeniedException("User does not have access to the requested job");
	        job.setProperty(CloudJob.PROPERTY_STS_ARN, stsArn);
	        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, clientSecret);
	        job.setProperty(CloudJob.PROPERTY_S3_ROLE, s3Role);
	        job.setProperty(NCIDetails.PROPERTY_NCI_USER, nciUser);
	        job.setProperty(NCIDetails.PROPERTY_NCI_PROJECT, nciProj);
	        job.setProperty(NCIDetails.PROPERTY_NCI_KEY, nciKey);
    	}
        return job;
    }

}

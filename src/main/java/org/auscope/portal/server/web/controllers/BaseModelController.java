package org.auscope.portal.server.web.controllers;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.security.access.AccessDeniedException;

/**
 * Base Controller class for controllers wishing to access and modify the underlying
 * model (database) for a virtual lab.
 * @author Josh Vote (CSIRO)
 *
 */
public class BaseModelController extends BasePortalController {

    protected VEGLJobManager jobManager;

    protected BaseModelController(VEGLJobManager jobManager) {
        this.jobManager = jobManager;
    }

    /**
     * Attempts to get a job with a particular ID. If the job ID does NOT belong to the current
     * user session null will be returned.
     *
     * This function will log all appropriate errors.
     * @param jobId
     * @return The VEGLJob object on success or null otherwise.
     */
    protected VEGLJob attemptGetJob(Integer jobId, ANVGLUser user) {
        log.info("Getting job with ID " + jobId);

        VEGLJob job = null;

        //Check we have a user email
        if (user == null || user.getEmail() == null) {
            log.warn("The current session is missing an email attribute");
            return null;
        }

        //Attempt to fetch our job
        if (jobId != null) {
            try {
                job = jobManager.getJobById(jobId.intValue(), user);
                log.debug("Job [" + job.hashCode() + "] retrieved by jobManager [" + jobManager.hashCode() + "]");
            } catch (AccessDeniedException e) {
                throw e;
            } catch (Exception ex) {
                log.error(String.format("Exception when accessing jobManager for job id '%1$s'", jobId), ex);
                return null;
            }
        }

        if (job == null) {
            log.warn(String.format("Job with ID '%1$s' does not exist", jobId));
            return null;
        }

        //Check user matches job
        if (!user.getEmail().equals(job.getUser())) {
            log.warn(String.format("%1$s's attempt to fetch %2$s's job denied!", user, job.getUser()));
            throw new AccessDeniedException(String.format("%1$s doesn't have permission to access job %2$s", user, jobId));
        }

        return job;
    }

    /**
     * Attempts to get a series with a particular ID. If the series ID does NOT belong to the current
     * user session null will be returned.
     *
     * This function will log all appropriate errors.
     * @param jobId
     * @return The VEGLSeries object on success or null otherwise.
     */
    protected VEGLSeries attemptGetSeries(Integer seriesId, ANVGLUser user) {
        VEGLSeries series = null;

        //Check we have a user email
        if (user == null || user.getEmail() == null) {
            log.warn("The current session is missing an email attribute");
            return null;
        }

        //Attempt to fetch our job
        if (seriesId != null) {
            try {
                series = jobManager.getSeriesById(seriesId.intValue(), user.getEmail());
            } catch (Exception ex) {
                log.error(String.format("Exception when accessing jobManager for series id '%1$s'", seriesId), ex);
                return null;
            }
        }

        if (series == null) {
            log.warn(String.format("Series with ID '%1$s' does not exist", seriesId));
            return null;
        }

        //Check user matches job
        if (!user.getEmail().equals(series.getUser())) {
            log.warn(String.format("%1$s's attempt to fetch %2$s's job denied!", user, series.getUser()));
            throw new AccessDeniedException(String.format("%1$s doesn't have permission to access series %2$s", user, seriesId));
        }

        return series;
    }
}

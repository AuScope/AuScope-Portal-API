package org.auscope.portal.server.web.service.monitor;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.cloud.monitor.JobStatusChangeListener;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.mail.JobMailSender;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A handler that provides the concrete implementation of
 * JobStatusChangeListener.
 *
 * It uses VEGLJobManager to update job status and to create job
 * audit trail record. In addition, it uses JobMailSender to
 * send out email notification upon job processing.
 *
 * @author Richard Goh
 */
public class VGLJobStatusChangeHandler implements JobStatusChangeListener {
    private final Log LOG = LogFactory.getLog(getClass());

    private VEGLJobManager jobManager;
    private JobMailSender jobMailSender;


    public VGLJobStatusChangeHandler(VEGLJobManager jobManager,
            JobMailSender jobMailSender) {
        this.jobManager = jobManager;
        this.jobMailSender = jobMailSender;
    }

    @Override
    public void handleStatusChange(CloudJob job, String newStatus, String oldStatus) {
        if (!newStatus.equals(JobBuilderController.STATUS_UNSUBMITTED)) {
            VEGLJob vglJob = (VEGLJob)job;
            vglJob.setProcessDate(new Date());
            vglJob.setStatus(newStatus);
            jobManager.saveJob(vglJob);
            jobManager.createJobAuditTrail(oldStatus, vglJob, "Job status updated.");
            if ((newStatus.equals(JobBuilderController.STATUS_DONE) || newStatus.equals(JobBuilderController.STATUS_ERROR))
                    && vglJob.getEmailNotification()) {
                //Send job completion email notification. Exception in sending
                //the notification won't be propagated to the calling method.
                jobMailSender.sendMail(vglJob);
                LOG.trace("Job completion email notification sent. Job id: " + vglJob.getId());
            }
        }
    }
}
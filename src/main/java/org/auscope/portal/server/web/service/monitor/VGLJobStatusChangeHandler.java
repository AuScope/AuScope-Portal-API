package org.auscope.portal.server.web.service.monitor;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.cloud.monitor.JobStatusChangeListener;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
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
    private VGLJobStatusAndLogReader jobStatusLogReader;


    public VGLJobStatusChangeHandler(VEGLJobManager jobManager,
            JobMailSender jobMailSender, VGLJobStatusAndLogReader jobStatusLogReader) {
        this.jobManager = jobManager;
        this.jobMailSender = jobMailSender;
        this.jobStatusLogReader=jobStatusLogReader;
    }

    @Override
    public void handleStatusChange(CloudJob job, String newStatus, String oldStatus) {
        if (!newStatus.equals(JobBuilderController.STATUS_UNSUBMITTED)) {
            VEGLJob vglJob = (VEGLJob)job;
            vglJob.setProcessDate(new Date());
            this.setProcessDuration(vglJob,newStatus);
            vglJob.setStatus(newStatus);
            jobManager.saveJob(vglJob);
            jobManager.createJobAuditTrail(oldStatus, vglJob, "Job status updated.");
            //VT: only status done we email here. Any error notification are mailed not by polling but
            //when the job has it status set to error;
            if ((newStatus.equals(JobBuilderController.STATUS_DONE) && vglJob.getEmailNotification()) || newStatus.equals(JobBuilderController.STATUS_ERROR)) {
                jobMailSender.sendMail(vglJob);
                LOG.trace("Job completion email notification sent. Job id: " + vglJob.getId());
            }
        }
    }



    public void setProcessDuration(VEGLJob job,String newStatus){
        if (newStatus.equals(JobBuilderController.STATUS_DONE) || newStatus.equals(JobBuilderController.STATUS_ERROR)){
            String time = this.jobStatusLogReader.getSectionedLog(job, "Time");
            job.setProcessTimeLog(time);
        }
    }
}
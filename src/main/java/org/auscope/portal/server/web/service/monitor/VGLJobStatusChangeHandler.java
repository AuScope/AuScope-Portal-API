package org.auscope.portal.server.web.service.monitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.cloud.monitor.JobStatusChangeListener;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.auscope.portal.server.vegl.mail.JobMailSender;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.service.ANVGLProvenanceService;

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
    private ANVGLProvenanceService anvglProvenanceService;

    public VGLJobStatusChangeHandler(VEGLJobManager jobManager,
            JobMailSender jobMailSender, VGLJobStatusAndLogReader jobStatusLogReader,
            ANVGLProvenanceService anvglProvenanceService) {
        this.jobManager = jobManager;
        this.jobMailSender = jobMailSender;
        this.jobStatusLogReader = jobStatusLogReader;
        this.anvglProvenanceService = anvglProvenanceService;
    }

    @Override
    public void handleStatusChange(CloudJob job, String newStatus, String oldStatus) {
        if (!newStatus.equals(JobBuilderController.STATUS_UNSUBMITTED)) {
            VEGLJob vglJob = (VEGLJob)job;
            vglJob.setProcessDate(new Date());
            try {
                this.setProcessDuration(vglJob,newStatus);
            } catch (Throwable ex) {
                LOG.debug("Unable to set process duration for" + job, ex);
            }
            vglJob.setStatus(newStatus);
            // Execution time, only accurate to 5 minutes and may not be set
            // for short jobs so will be set later from the job log 
            if(newStatus.equals(JobBuilderController.STATUS_PENDING) ||
                    newStatus.equals(JobBuilderController.STATUS_ACTIVE))
                vglJob.setExecuteDate(new Date());
            jobManager.saveJob(vglJob);
            jobManager.createJobAuditTrail(oldStatus, vglJob, "Job status updated.");
            
            //VT: only status done we email here. Any error notification are mailed not by polling but
            //when the job has it status set to error;
            if ((newStatus.equals(JobBuilderController.STATUS_DONE) && vglJob.getEmailNotification()) ||
                    newStatus.equals(JobBuilderController.STATUS_ERROR) ||
                    newStatus.equals(JobBuilderController.STATUS_WALLTIME_EXCEEDED)) {
                jobMailSender.sendMail(vglJob);
                LOG.trace("Job completion email notification sent. Job id: " + vglJob.getId());
            }
            // Job successfully completed
            if(newStatus.equals(JobBuilderController.STATUS_DONE)) {
                // Provenance
                String reportUrl = anvglProvenanceService.createEntitiesForOutputs(vglJob);
                if(!reportUrl.equals("")) {
                    vglJob.setPromsReportUrl(reportUrl);
                }
                // Get job execution date/time from log
                String execDateLog = jobStatusLogReader.getSectionedLog(vglJob,  "Execute");
                if(execDateLog != null) {
                    execDateLog = execDateLog.trim();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy'T'hh:mm:ss");
                    try {
                        Date d = formatter.parse(execDateLog);
                        vglJob.setExecuteDate(d);
                    } catch(ParseException pe) {
                        LOG.warn("Unable to read job execution date from log file");
                    }
                }
                jobManager.saveJob(vglJob);
            }
        }
    }

    public void setProcessDuration(VEGLJob job,String newStatus){
        if (newStatus.equals(JobBuilderController.STATUS_DONE) ||
                newStatus.equals(JobBuilderController.STATUS_ERROR) ||
                newStatus.equals(JobBuilderController.STATUS_WALLTIME_EXCEEDED)){
            String time = this.jobStatusLogReader.getSectionedLog(job, "Time");
            job.setProcessTimeLog(time);
        }
    }
}
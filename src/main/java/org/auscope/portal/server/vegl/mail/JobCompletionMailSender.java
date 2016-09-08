package org.auscope.portal.server.vegl.mail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.util.DateUtil;
import org.auscope.portal.core.util.TextUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 * A concrete implementation of JobMailServer interface
 * that responsible for constructing and sending out
 * job completion notification.
 *
 * @author Richard Goh
 */
public class JobCompletionMailSender implements JobMailSender {
    private final Log LOG = LogFactory.getLog(getClass());

    //Properties that get injected thru constructor
    private VEGLJobManager jobManager;
    private VGLJobStatusAndLogReader jobStatLogReader;
    private MailSender mailSender;
    private VelocityEngine velocityEngine;

    //Properties that get injected thru setter methods
    private String template;
    private String dateFormat;
    private int maxLengthForSeriesNameInSubject;
    private int maxLengthForJobNameInSubject;
    private int maxLinesForTail;
    private String emailSender;
    private String emailSubject;

    private String portalUrl=null;
    
    /**
     * @return the portalUrl
     */
    public String getPortalUrl() {
        return portalUrl;
    }

    /**
     * @param portalUrl the portalUrl to set
     */
    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public JobCompletionMailSender(VEGLJobManager jobManager,
            VGLJobStatusAndLogReader jobStatLogReader, MailSender mailSender,
            VelocityEngine velocityEngine) {
        this.jobManager = jobManager;
        this.jobStatLogReader = jobStatLogReader;
        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;
    }

    /**
     * Sets the job completion notification template file including
     * its location on the class path.
     *
     * @param template
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Gets the job completion notification template file path.
     *
     * @return
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the date format to be used in the email text.
     *
     * @param dateFormat The pattern describing the date and time format.
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Gets the date format used in the email text.
     *
     * @return the pattern describing the date and time format.
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the maximum length for series name to be
     * displayed in email subject.
     *
     * @param maxLengthForSeriesNameInSubject
     */
    public void setMaxLengthForSeriesNameInSubject(int maxLengthForSeriesNameInSubject) {
        this.maxLengthForSeriesNameInSubject = maxLengthForSeriesNameInSubject;
    }

    /**
     * Gets the maximum length of series name in the
     * email subject.
     *
     * @return
     */
    public int getMaxLengthForSeriesNameInSubject() {
        return maxLengthForSeriesNameInSubject;
    }

    /**
     * Sets the maximum length for job name to be
     * displayed on email subject.
     *
     * @param maxLengthForJobNameInSubject
     */
    public void setMaxLengthForJobNameInSubject(int maxLengthForJobNameInSubject) {
        this.maxLengthForJobNameInSubject = maxLengthForJobNameInSubject;
    }

    /**
     * Gets the maximum length of job name in the
     * email subject.
     *
     * @return
     */
    public int getMaxLengthForJobNameInSubject() {
        return maxLengthForJobNameInSubject;
    }

    /**
     * Sets the maximum number of lines (N - counting from
     * the bottom) to be used for getting the last N lines
     * of text from the job execution log.
     *
     * @param maxLinesForTail
     */
    public void setMaxLinesForTail(int maxLinesForTail) {
        this.maxLinesForTail = maxLinesForTail;
    }

    /**
     * Get the maximum number of lines for tailing the
     * job execution log.
     *
     * @return
     */
    public int getMaxLinesForTail() {
        return maxLinesForTail;
    }

    /**
     * Sets the email sender.
     *
     * @param emailSender
     */
    public void setEmailSender(String emailSender) {
        this.emailSender = emailSender;
    }

    /**
     * Gets the email sender.
     *
     * @return
     */
    public String getEmailSender() {
        return emailSender;
    }

    /**
     * Sets the email subject.
     *
     * @param emailSubject
     */
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    /**
     * Get the email subject.
     *
     * @return
     */
    public String getEmailSubject() {
        return emailSubject;
    }

    /**
     * Constructs job completion notification email content.
     * @param seriesName 
     */
    @Override
    public String constructMailContent(String seriesName, VEGLJob job) {

        Date submitDate, processDate, executeDate;
        if(job.getSubmitDate()!=null){
            submitDate=job.getSubmitDate();
        }else{
            submitDate=new Date();
        }

        if(job.getProcessDate()!=null){
            processDate=job.getProcessDate();
        }else{
            processDate=new Date();
        }
        
        // If execution date failed to set revert to submission date
        if(job.getExecuteDate()!=null){
            executeDate=job.getExecuteDate();
        }else{
            executeDate= job.getSubmitDate();
        }

        long[] diff = DateUtil.getTimeDifference(executeDate, processDate);
        String timeElapsed = diff[0] + " day(s) " + diff[1] + " hour(s) "
                + diff[2] + " minute(s) " + diff[3] + " second(s)";

        Map<String, Object> model = new HashMap<>();
        model.put("userName", job.getUser().substring(0,job.getUser().indexOf("@")));
        model.put("status", job.getStatus());
        model.put("jobId", job.getId().toString());
        model.put("seriesName", seriesName);
        model.put("jobName", job.getName());
        model.put("jobDescription", job.getDescription());
        model.put("dateSubmitted", DateUtil.formatDate(submitDate, dateFormat));
        model.put("dateExecuted", DateUtil.formatDate(executeDate, dateFormat));
        model.put("dateProcessed", DateUtil.formatDate(processDate, dateFormat));
        model.put("timeElapsed", timeElapsed);
        model.put("jobExecLogSnippet", TextUtil.tail(jobStatLogReader.getSectionedLog(job, "Python"), maxLinesForTail));
        model.put("emailSender", getEmailSender());
        model.put("portalUrl", getPortalUrl());
        
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, model);
    }

    /**
     * Sends job completion notification email with Spring
     * framework's MailSender.
     */
    @Override
    public void sendMail(VEGLJob job) {
        String jobName = job.getName();
        String seriesName = "";
        
        if (job.getSeriesId() != 0) { 
            VEGLSeries jobSeries = jobManager.getSeriesById(job.getSeriesId(), job.getEmailAddress());
            if (jobSeries != null) {
                seriesName = jobSeries.getName();

                if (seriesName.length() > maxLengthForSeriesNameInSubject) {
                    seriesName = seriesName.substring(0, maxLengthForJobNameInSubject);
                }
            }
        }

        if (jobName.length() > maxLengthForJobNameInSubject) {
            jobName = jobName.substring(0, maxLengthForJobNameInSubject);
        }

        String subject = String.format(this.emailSubject, jobName);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(this.emailSender);
        msg.setTo(job.getEmailAddress());
        msg.setSubject(subject);
        msg.setText(constructMailContent(seriesName, job));

        try {
            this.mailSender.send(msg);
        } catch (Exception ex) {
            LOG.error("Sending of email notification failed for job id [" + job.getId() + "].", ex);
        }
    }
}
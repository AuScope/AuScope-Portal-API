package org.auscope.portal.server.vegl.mail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.util.DateUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.hibernate.exception.ExceptionUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 * A concrete implementation of JobMailServer interface
 * that responsible for constructing and sending out
 * job completion notification.
 *
 * @author Victor Tey
 */
public class JobErrorMailSender {
    private final Log LOG = LogFactory.getLog(getClass());

    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;

    //Properties that get injected thru setter methods
    private String template;
    private String dateFormat;
    private int maxLengthForSeriesNameInSubject;
    private int maxLengthForJobNameInSubject;
    private int maxLinesForTail;
    private String emailSender;
    private String emailSubject;

    public JobErrorMailSender( JavaMailSender mailSender,
            VelocityEngine velocityEngine) {
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
     */
    public String constructMailContent(VEGLJob job) {

        Date submitDate, processDate;
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

        long[] diff = DateUtil.getTimeDifference(submitDate, processDate);
        String timeElapsed = diff[0] + " day(s) " + diff[1] + " hour(s) "
                + diff[2] + " minute(s) " + diff[3] + " second(s)";

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("userName", job.getUser().substring(0,job.getUser().indexOf("@")));
        model.put("seriesId", job.getSeriesId());
        model.put("status", job.getStatus());
        model.put("jobId", job.getId().toString());
        model.put("jobName", job.getName());
        model.put("jobDescription", job.getDescription());
        model.put("dateSubmitted", DateUtil.formatDate(submitDate, dateFormat));
        model.put("dateProcessed", DateUtil.formatDate(processDate, dateFormat));
        model.put("timeElapsed", timeElapsed);


        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, model);
    }

    /**
     * Sends job completion notification email with Spring
     * framework's MailSender.
     * @throws MessagingException
     */
    public void sendMail(VEGLJob job,Exception e) {


        int seriesId = job.getSeriesId();
        String jobName = job.getName();


        if (jobName.length() > maxLengthForJobNameInSubject) {
            jobName = jobName.substring(0, maxLengthForJobNameInSubject);
        }

        String subject = String.format(this.emailSubject, seriesId, jobName);
        try {
            MimeMessage msg = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg,true);
            helper.setFrom(this.emailSender);
            helper.setTo(job.getEmailAddress());
            helper.setSubject(subject);
            helper.setText(constructMailContent(job));
            helper.addAttachment("StackTrace.txt", new ByteArrayResource((ExceptionUtils.getStackTrace(e)).getBytes()), "text/plain");
            this.mailSender.send(msg);
        } catch (Exception ex) {
            LOG.error("Sending of email notification failed for job id [" + job.getId() + "].", ex);
        }
    }
}
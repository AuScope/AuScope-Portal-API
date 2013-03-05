package org.auscope.portal.server.vegl.mail;

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
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.ui.velocity.VelocityEngineUtils;

public class JobCompletionMailSender implements JobMailSender {
    private final Log LOG = LogFactory.getLog(getClass());
    
    //Properties that get injected thru constructor
    private VEGLJobManager jobManager;
    private VGLJobStatusAndLogReader jobStatLogReader;
    private MailSender mailSender;
    private SimpleMailMessage templateMessage;
    private VelocityEngine velocityEngine;
    
    //Properties that get injected thru setter methods
    private String template;
    private String dateFormat;
    private int maxLengthForSeriesNameInSubject;
    private int maxLengthForJobNameInSubject;
    private int maxLinesForTail;
    
    public JobCompletionMailSender(VEGLJobManager jobManager, 
            VGLJobStatusAndLogReader jobStatLogReader, MailSender mailSender, 
            SimpleMailMessage templateMessage, VelocityEngine velocityEngine) {
        this.jobManager = jobManager;
        this.jobStatLogReader = jobStatLogReader;
        this.mailSender = mailSender;
        this.templateMessage = templateMessage;
        this.velocityEngine = velocityEngine;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
    
    public String getDateFormat() {
        return dateFormat;
    }
    
    public void setMaxLengthForSeriesNameInSubject(int maxLengthForSeriesNameInSubject) {
        this.maxLengthForSeriesNameInSubject = maxLengthForSeriesNameInSubject;
    }
    
    public int getMaxLengthForSeriesNameInSubject() {
        return maxLengthForSeriesNameInSubject;
    }
    
    public void setMaxLengthForJobNameInSubject(int maxLengthForJobNameInSubject) {
        this.maxLengthForJobNameInSubject = maxLengthForJobNameInSubject;
    }
    
    public int getMaxLengthForJobNameInSubject() {
        return maxLengthForJobNameInSubject;
    }
    
    public void setMaxLinesForTail(int maxLinesForTail) {
        this.maxLinesForTail = maxLinesForTail;
    }
    
    public int getMaxLinesForTail() {
        return maxLinesForTail;
    }
    
    public String constructMailContent(VEGLSeries jobSeries, VEGLJob job) {
        long[] diff = DateUtil.getTimeDifference(job.getSubmitDate(), job.getProcessDate());
        String timeElapsed = diff[0] + " day(s) " + diff[1] + " hour(s) " 
                + diff[2] + " minute(s) " + diff[3] + " second(s)";
        
        Map<String, String> model = new HashMap<String, String>();
        model.put("userName", job.getUser().substring(0,job.getUser().indexOf("@")));
        model.put("seriesName", jobSeries.getName());
        model.put("jobId", job.getId().toString());
        model.put("jobName", job.getName());
        model.put("jobDescription", job.getDescription());
        model.put("dateSubmitted", DateUtil.formatDate(job.getSubmitDate(), dateFormat));
        model.put("dateProcessed", DateUtil.formatDate(job.getProcessDate(), dateFormat));
        model.put("timeElapsed", timeElapsed);
        model.put("jobExecLogSnippet", TextUtil.tail(jobStatLogReader.getSectionedLog(job, "Python"), maxLinesForTail));
        
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, model);
    }
    
    public void sendMail(VEGLJob job) {
        VEGLSeries jobSeries = jobManager.getSeriesById(job.getSeriesId());
        
        String seriesName = jobSeries.getName();
        String jobName = job.getName();
        
        if (seriesName.length() > maxLengthForSeriesNameInSubject) {
            seriesName = seriesName.substring(0, maxLengthForJobNameInSubject);
        }
        
        if (jobName.length() > maxLengthForJobNameInSubject) {
            jobName = jobName.substring(0, maxLengthForJobNameInSubject);
        }
        
        String subject = String.format(templateMessage.getSubject(), seriesName, jobName);
        
        SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
        msg.setTo(job.getEmailAddress());
        msg.setSubject(subject);
        msg.setText(constructMailContent(jobSeries, job));
        
        try {
            this.mailSender.send(msg);
        } catch (MailException ex) {
            LOG.error("Sending of email notification failed for job id [" + job.getId() + "].", ex);
        }
    }
}
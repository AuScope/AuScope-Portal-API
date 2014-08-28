package org.auscope.portal.server.vegl.mail.services;

import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.mail.JobErrorMailSender;


public class VGLMailService {


    private JobErrorMailSender jobErrorMailSender;

    public VGLMailService(JobErrorMailSender jobErrorMailSender){
        this.jobErrorMailSender = jobErrorMailSender;
    }

    public void sendErrorMail(VEGLJob job, Exception e){
        jobErrorMailSender.sendMail(job,e);
    }


}

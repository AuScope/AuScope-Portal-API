package org.auscope.portal.server.vegl.mail;

import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLSeries;

/**
 * An interface with common methods for any beans that 
 * need to implement send mail functionality.
 * 
 * @author Richard Goh
 */
public interface JobMailSender {
    public String constructMailContent(VEGLSeries jobSeries, VEGLJob job);
    public void sendMail(VEGLJob job);
}
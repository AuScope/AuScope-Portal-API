package org.auscope.portal.server.vegl.mail;

import org.auscope.portal.server.vegl.VEGLJob;

/**
 * An interface with common methods for any beans that
 * need to implement send mail functionality.
 *
 * @author Richard Goh
 */
public interface JobMailSender {
    /**
     * Constructs job notification email content.
     *
     * @param seriesName The series name
     * @param job The VEGLJob object
     * @return
     */
    public String constructMailContent(String seriesName, VEGLJob job);

    /**
     * Sends email with SMTP protocol.
     *
     * @param job
     */
    public void sendMail(VEGLJob job);

}
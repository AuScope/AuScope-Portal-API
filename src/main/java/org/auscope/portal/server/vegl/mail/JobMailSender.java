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
    /**
     * Constructs job notification email content.
     *
     * @param jobSeries The VEGLSeries object
     * @param job The VEGLJob object
     * @return
     */
    public String constructMailContent(VEGLSeries jobSeries, VEGLJob job);

    /**
     * Sends email with SMTP protocol.
     *
     * @param job
     */
    public void sendMail(VEGLJob job);
}
package org.auscope.portal.server.vegl.mail;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.jmock.SimpleMailMessageMatcher;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Unit tests for JobCompletionMailSender.
 *
 * @author Richard Goh
 */
public class TestJobCompletionMailSender extends PortalTestClass {
    private VEGLJobManager mockJobManager;
    private VGLJobStatusAndLogReader mockJobStatLogReader;
    private JavaMailSenderImpl mockMailSender;
    private JobCompletionMailSender jobCompMailSender;
    private VEGLSeries mockSeries;
    private VEGLJob mockJob;
    private VelocityEngine velocityEngine;
    private Date dateSubmitted = null;
    private Date dateProcessed = null;
    private Date dateExecuted = null;

    @Before
    public void init() throws Exception {
        //Mock objects to be used in the unit tests.
        mockJobManager = context.mock(VEGLJobManager.class);
        mockJobStatLogReader = context.mock(VGLJobStatusAndLogReader.class);
        mockMailSender = context.mock(JavaMailSenderImpl.class);
        mockSeries = context.mock(VEGLSeries.class);
        mockJob = context.mock(VEGLJob.class);
        
        // Create actual Velocity engine needed for proper testing
        Properties properties = new Properties();
	    properties.setProperty("input.encoding", "UTF-8");
	    properties.setProperty("output.encoding", "UTF-8");
	    properties.setProperty("resource.loader", "class");
	    properties.setProperty("class.resource.loader.class",
	    					   "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	    velocityEngine = new VelocityEngine(properties);

        //Global test variables to be used in all unit tests.
        Calendar cal1 = new GregorianCalendar(2013, 2, 5, 12, 00, 00);
        Calendar cal2 = new GregorianCalendar(2013, 2, 5, 12, 00, 10);
        Calendar cal3 = new GregorianCalendar(2013, 2, 5, 12, 00, 55);
        dateSubmitted = cal1.getTime();
        dateExecuted = cal2.getTime();
        dateProcessed = cal3.getTime();

        //Create object under test with mock objects and set its required property fields.
        jobCompMailSender = new JobCompletionMailSender(mockJobManager, mockJobStatLogReader, mockMailSender, velocityEngine);
        jobCompMailSender.setTemplate("org/auscope/portal/server/web/service/monitor/templates/job-completion.tpl");
        jobCompMailSender.setDateFormat("EEE, d MMM yyyy HH:mm:ss");
        jobCompMailSender.setMaxLengthForSeriesNameInSubject(15);
        jobCompMailSender.setMaxLengthForJobNameInSubject(15);
        jobCompMailSender.setMaxLinesForTail(5);
        jobCompMailSender.setEmailSender("test-admin@email.com");
        jobCompMailSender.setEmailSubject("VL Job (%s)");
    }

    /**
     * jMock matcher used to compare SimpleMailMessage object.
     */
    private static Matcher<SimpleMailMessage> aSimpleMailMessage(String from,
            String to, String subject, String text) {
        return new SimpleMailMessageMatcher(from, to, subject, text);
    }

    /**
     * Tests that the content of email notification being generated
     * contains information considered as essential.
     */
    @Test
    public void testConstructMailContent() {
        final String user = "user@test.com";
        final int jobId = 123;
        final String seriesName = "TestSeries#1";
        final String jobName = "TestJob#1";
        final String jobDescription = "Job#1Description";
        final String jobLog = "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7";
        final String jobStatus="Done";

        context.checking(new Expectations() {{
            allowing(mockJob).getSubmitDate();will(returnValue(dateSubmitted));
            allowing(mockJob).getExecuteDate();will(returnValue(dateExecuted));
            allowing(mockJob).getProcessDate();will(returnValue(dateProcessed));
            allowing(mockJob).getStatus();will(returnValue(jobStatus));
            allowing(mockJob).getUser();will(returnValue(user));
            oneOf(mockSeries).getName();will(returnValue(seriesName));
            oneOf(mockJob).getId();will(returnValue(jobId));
            oneOf(mockJob).getName();will(returnValue(jobName));
            oneOf(mockJob).getDescription();will(returnValue(jobDescription));

            oneOf(mockJobStatLogReader).getSectionedLog(mockJob, "Python");will(returnValue(jobLog));
        }});

        String content = jobCompMailSender.constructMailContent(mockSeries.getName(), mockJob);
        //Email content shouldn't be null.
        Assert.assertNotNull(content);
        //Email body must contain user email alias, job id, series name and job name.
        Assert.assertTrue(content.contains("user"));
        Assert.assertTrue(content.contains(String.valueOf(jobId)));
        Assert.assertTrue(content.contains(seriesName));
        Assert.assertTrue(content.contains(jobName));
    }

    /**
     * Tests that the sending of job completion email notification succeeds.
     */
    @Test
    public void testSendMail() {
        final String user = "user@test.com";
        final int jobId = 123;
        final int seriesId = 1;
        final String seriesName = "TestSeries#1abcdefgh";
        final String jobName = "TestJob#1abcdefghijk";
        final String jobDescription = "Job#1Description";
        final String jobLog = "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7";
        final String jobNameInSubject =
                jobName.substring(0, jobCompMailSender.getMaxLengthForJobNameInSubject());
        final String subject = String.format(jobCompMailSender.getEmailSubject(), jobNameInSubject);
        final String jobStatus="Done";

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getSeriesById(seriesId, user);will(returnValue(mockSeries));
            allowing(mockJob).getSeriesId();will(returnValue(seriesId));

            oneOf(mockSeries).getName();will(returnValue(seriesName));

            oneOf(mockJob).getName();will(returnValue(jobName));
            allowing(mockJob).getEmailAddress();will(returnValue(user));

            //The following expectations are for invoking constructMailContent method.
            allowing(mockJob).getSubmitDate();will(returnValue(dateSubmitted));
            allowing(mockJob).getExecuteDate();will(returnValue(dateExecuted));
            allowing(mockJob).getProcessDate();will(returnValue(dateProcessed));
            allowing(mockJob).getUser();will(returnValue(user));
            allowing(mockJob).getStatus();will(returnValue(jobStatus));
            oneOf(mockJob).getId();will(returnValue(jobId));
            oneOf(mockJob).getName();will(returnValue(jobName));
            oneOf(mockJob).getDescription();will(returnValue(jobDescription));
            //Ensure we've one call to getSectionedLog to get Python execution log
            oneOf(mockJobStatLogReader).getSectionedLog(mockJob, "Python");will(returnValue(jobLog));
            //Ensure we've one call to MailSender to send out job completion notification
            oneOf(mockMailSender).send(with(aSimpleMailMessage(null, null, subject, null)));
        }});

        jobCompMailSender.sendMail(mockJob);
    }

    /**
     * Tests that failure or exception thrown in sending out
     * email notification would not be propagated back to the
     * caller.
     */
    @Test
    public void testSendMail_MailException() {
        final String user = "user@test.com";
        final int jobId = 123;
        final int seriesId = 1;
        final String seriesName = "TestSeries#1abcdefgh";
        final String jobName = "TestJob#1abcdefghijk";
        final String jobDescription = "Job#1Description";
        final String jobLog = "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7";
        final Exception sendMailEx = new Exception();
        final String jobStatus="Done";

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getSeriesById(seriesId, user);will(returnValue(mockSeries));
            allowing(mockJob).getSeriesId();will(returnValue(seriesId));

            oneOf(mockSeries).getName();will(returnValue(seriesName));

            oneOf(mockJob).getName();will(returnValue(jobName));
            oneOf(mockJob).getEmailAddress();will(returnValue(user));

            //The following expectations are for invoking constructMailContent method.
            allowing(mockJob).getSubmitDate();will(returnValue(dateSubmitted));
            allowing(mockJob).getExecuteDate();will(returnValue(dateExecuted));
            allowing(mockJob).getProcessDate();will(returnValue(dateProcessed));
            allowing(mockJob).getUser();will(returnValue(user));
            allowing(mockJob).getStatus();will(returnValue(jobStatus));
            allowing(mockJob).getId();will(returnValue(jobId));
            allowing(mockJob).getEmailAddress();will(returnValue(user));
            oneOf(mockJob).getName();will(returnValue(jobName));
            oneOf(mockJob).getDescription();will(returnValue(jobDescription));
            //Ensure we've one call to getSectionedLog to get Python execution log
            oneOf(mockJobStatLogReader).getSectionedLog(mockJob, "Python");will(returnValue(jobLog));
            //Ensure we've one call to MailSender to send out job completion notification
            oneOf(mockMailSender).send(with(any(SimpleMailMessage.class)));will(throwException(sendMailEx));
        }});

        jobCompMailSender.sendMail(mockJob);
    }
}
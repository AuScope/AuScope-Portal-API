package org.auscope.portal.server.vegl;

import org.auscope.portal.core.services.PortalServiceException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;



/**
 * Unit tests for VGLTimePollQueue
 * @author Josh Vote
 *
 */
public class TestVGLTimePollQueue {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};


    @Test
    public void testManageJob() throws PortalServiceException{
        VGLTimePollQueue queue= new VGLTimePollQueue();
        final VGLQueueJob job= context.mock(VGLQueueJob.class);

        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(job).run();will(returnValue(true));
        }});

        queue.addJob(job);
        Assert.assertTrue(queue.hasJob());
        queue.run();
        Assert.assertFalse(queue.hasJob());

    }

    @Test
    public void testManageJobWithQuotaExceeded() throws PortalServiceException{
        VGLTimePollQueue queue= new VGLTimePollQueue();
        final VGLQueueJob job= context.mock(VGLQueueJob.class);

        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(job).run();will(throwException(new PortalServiceException("Some random message","Some Quota exceeded error")));
        }});

        queue.addJob(job);
        Assert.assertTrue(queue.hasJob());
        queue.run();
        Assert.assertTrue(queue.hasJob());

    }

    @Test
    public void testManageJobWithRandomError() throws PortalServiceException{
        VGLTimePollQueue queue= new VGLTimePollQueue();
        final VGLQueueJob job= context.mock(VGLQueueJob.class);

        context.checking(new Expectations() {{
            //We should have a call to our job manager to get our job object
            oneOf(job).run();will(throwException(new PortalServiceException("Some random Quota exceeded message","Some error")));
            allowing(job).updateErrorStatus();
        }});

        queue.addJob(job);
        Assert.assertTrue(queue.hasJob());
        queue.run();
        Assert.assertFalse(queue.hasJob());

    }
}

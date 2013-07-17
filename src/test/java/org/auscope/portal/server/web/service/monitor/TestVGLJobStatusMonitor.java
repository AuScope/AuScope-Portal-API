package org.auscope.portal.server.web.service.monitor;

import java.util.Arrays;
import java.util.List;

import org.auscope.portal.core.services.cloud.monitor.JobStatusException;
import org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Unit tests for VGLJobStatusMonitor. 
 * 
 * @author Richard Goh
 */
public class TestVGLJobStatusMonitor extends PortalTestClass {
    private VGLJobStatusMonitor monitor;
    private JobExecutionContext mockJobExecCtx;
    private VEGLJobManager mockJobManager;
    private JobStatusMonitor mockJobStatusMonitor;
    
    @Before
    public void init() {
        //Mock objects required for the unit tests
        mockJobExecCtx = context.mock(JobExecutionContext.class);
        mockJobManager = context.mock(VEGLJobManager.class);
        mockJobStatusMonitor = context.mock(JobStatusMonitor.class);
        
        //Component under test
        monitor = new VGLJobStatusMonitor();
        monitor.setJobManager(mockJobManager);
        monitor.setJobStatusMonitor(mockJobStatusMonitor);
    }
    
    /**
     * Tests that the execution of VGLJobStatusMonitor task
     * run as expected.
     * @throws Exception
     */
    @Test
    public void testExecuteInternal() throws Exception {
        final VEGLJob job1 = new VEGLJob(1);
        job1.setStatus(JobBuilderController.STATUS_PENDING);
        
        final VEGLJob job2 = new VEGLJob(2);
        job2.setStatus(JobBuilderController.STATUS_ACTIVE);
        
        final List<VEGLJob> pendingActiveJobs = Arrays.asList(job1, job2);
        
        context.checking(new Expectations() {{
            oneOf(mockJobManager).getPendingOrActiveJobs();will(returnValue(pendingActiveJobs));
            
            oneOf(mockJobStatusMonitor).statusUpdate(pendingActiveJobs);
        }});
        
        monitor.executeInternal(mockJobExecCtx);
    }
    
    /**
     * Tests that exception caused by job status change handler
     * will correctly wrap exceptions
     * @throws Exception
     */
    @Test(expected=JobExecutionException.class)
    public void testExecuteInternal_Exception() throws Exception {
        final VEGLJob job1 = new VEGLJob(1);
        job1.setStatus(JobBuilderController.STATUS_PENDING);
        
        final VEGLJob job2 = new VEGLJob(2);
        job2.setStatus(JobBuilderController.STATUS_ACTIVE);
        
        final List<VEGLJob> pendingActiveJobs = Arrays.asList(job1, job2);
        
        context.checking(new Expectations() {{
            oneOf(mockJobManager).getPendingOrActiveJobs();will(returnValue(pendingActiveJobs));
            
            oneOf(mockJobStatusMonitor).statusUpdate(pendingActiveJobs);will(throwException(new JobStatusException(new Exception(), job1)));
        }});
        
        monitor.executeInternal(mockJobExecCtx);
    }
}
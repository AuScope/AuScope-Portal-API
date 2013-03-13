package org.auscope.portal.server.web.service.monitor;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.cloud.monitor.JobStatusChangeListener;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionContext;

/**
 * Unit tests for VGLJobStatusMonitor. 
 * 
 * @author Richard Goh
 */
public class TestVGLJobStatusMonitor extends PortalTestClass {
    private VGLJobStatusMonitor monitor;
    private JobExecutionContext mockJobExecCtx;
    private VEGLJobManager mockJobManager;
    private VGLJobStatusAndLogReader mockJobStatusLogReader;
    private JobStatusChangeListener[] mockJobStatusChangeListeners;
    
    @Before
    public void init() {
        //Mock objects required for the unit tests
        mockJobExecCtx = context.mock(JobExecutionContext.class);
        mockJobManager = context.mock(VEGLJobManager.class);
        mockJobStatusLogReader = context.mock(VGLJobStatusAndLogReader.class);
        mockJobStatusChangeListeners = new JobStatusChangeListener[] {context.mock(VGLJobStatusChangeHandler.class)};
        //Component under test
        monitor = new VGLJobStatusMonitor();
        monitor.setJobManager(mockJobManager);
        monitor.setJobStatusLogReader(mockJobStatusLogReader);
        monitor.setJobStatusChangeListeners(mockJobStatusChangeListeners);
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
        
        final List<VEGLJob> pendingActiveJobs = new ArrayList<VEGLJob>();
        pendingActiveJobs.add(job1);
        pendingActiveJobs.add(job2);
        
        final String job1NewStat = JobBuilderController.STATUS_ACTIVE;
        final String job2NewStat = JobBuilderController.STATUS_DONE;
        
        context.checking(new Expectations() {{
            oneOf(mockJobManager).getPendingOrActiveJobs();will(returnValue(pendingActiveJobs));
            
            oneOf(mockJobStatusLogReader).getJobStatus(job1);will(returnValue(job1NewStat));
            oneOf(mockJobStatusLogReader).getJobStatus(job2);will(returnValue(job2NewStat));
            
            oneOf(mockJobStatusChangeListeners[0]).handleStatusChange(job1, job1NewStat, job1.getStatus());
            oneOf(mockJobStatusChangeListeners[0]).handleStatusChange(job2, job2NewStat, job2.getStatus());
        }});
        
        monitor.executeInternal(mockJobExecCtx);
    }
    
    /**
     * Tests that exception caused by job status change handler
     * won't impact the status change handling for other job(s) 
     * being processed.
     * @throws Exception
     */
    @Test
    public void testExecuteInternal_Exception() throws Exception {
        final VEGLJob job1 = new VEGLJob(1);
        job1.setStatus(JobBuilderController.STATUS_PENDING);
        
        final VEGLJob job2 = new VEGLJob(2);
        job2.setStatus(JobBuilderController.STATUS_ACTIVE);
        
        final List<VEGLJob> pendingActiveJobs = new ArrayList<VEGLJob>();
        pendingActiveJobs.add(job1);
        pendingActiveJobs.add(job2);
        
        final String job1NewStat = JobBuilderController.STATUS_ACTIVE;
        final String job2NewStat = JobBuilderController.STATUS_DONE;
        
        context.checking(new Expectations() {{
            oneOf(mockJobManager).getPendingOrActiveJobs();will(returnValue(pendingActiveJobs));
            
            oneOf(mockJobStatusLogReader).getJobStatus(job1);will(returnValue(job1NewStat));
            oneOf(mockJobStatusLogReader).getJobStatus(job2);will(returnValue(job2NewStat));
            
            oneOf(mockJobStatusChangeListeners[0]).handleStatusChange(job1, job1NewStat, job1.getStatus());
            will(throwException(new Exception()));
            oneOf(mockJobStatusChangeListeners[0]).handleStatusChange(job2, job2NewStat, job2.getStatus());
        }});
        
        monitor.executeInternal(mockJobExecCtx);
    }
}
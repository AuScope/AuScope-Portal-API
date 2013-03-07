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

public class TestVGLJobStatusMonitor extends PortalTestClass {
    private VGLJobStatusMonitor monitor;
    private JobExecutionContext mockJobExecCtx;
    private VEGLJobManager mockJobManager;
    private VGLJobStatusAndLogReader mockJobStatusLogReader;
    private JobStatusChangeListener[] mockJobStatusChangeListeners;
    
    @Before
    public void init() {
        mockJobExecCtx = context.mock(JobExecutionContext.class);
        mockJobManager = context.mock(VEGLJobManager.class);
        mockJobStatusLogReader = context.mock(VGLJobStatusAndLogReader.class);
        mockJobStatusChangeListeners = new JobStatusChangeListener[] { context.mock(VGLJobStatusChangeHandler.class) };
        monitor = new VGLJobStatusMonitor();
        monitor.setJobManager(mockJobManager);
        monitor.setJobStatusLogReader(mockJobStatusLogReader);
        monitor.setJobStatusChangeListeners(mockJobStatusChangeListeners);
    }
    
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
}
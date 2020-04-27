package org.auscope.portal.server.web.service.monitor;

import java.util.Arrays;
import java.util.List;

import org.auscope.portal.core.services.cloud.monitor.JobStatusException;
import org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.auscope.portal.server.web.service.NCIDetailsService;
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
    private ANVGLUserService mockUserService;
    private NCIDetailsService mockNciService;
    
    @Before
    public void init() {
        //Mock objects required for the unit tests
        mockJobExecCtx = context.mock(JobExecutionContext.class);
        mockJobManager = context.mock(VEGLJobManager.class);
        mockJobStatusMonitor = context.mock(JobStatusMonitor.class);
        mockUserService = context.mock(ANVGLUserService.class);
        mockNciService = context.mock(NCIDetailsService.class);
        
        //Component under test
        monitor = new VGLJobStatusMonitor();
        monitor.setJobManager(mockJobManager);
        monitor.setJobStatusMonitor(mockJobStatusMonitor);
        monitor.setJobUserService(mockUserService);
        monitor.setNciDetailsService(mockNciService);
    }
    
    /**
     * Tests that the execution of VGLJobStatusMonitor task
     * run as expected.
     * @throws Exception
     */
    @Test
    public void testExecuteInternal() throws Exception {
        final VEGLJob job1 = new VEGLJob();
        job1.setId(1);
        job1.setStatus(JobBuilderController.STATUS_PENDING);
        
        final VEGLJob job2 = new VEGLJob();
        job2.setId(2);
        job2.setStatus(JobBuilderController.STATUS_ACTIVE);
        
        final List<VEGLJob> pendingActiveJobs = Arrays.asList(job1, job2);
        final ANVGLUser user = new ANVGLUser();
        
        context.checking(new Expectations() {{
            oneOf(mockJobManager).getPendingOrActiveJobs();will(returnValue(pendingActiveJobs));
            allowing(mockUserService).getByEmail(null); will(returnValue(user));
            allowing(mockNciService).getByUser(user); will(returnValue(null));
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
        final VEGLJob job1 = new VEGLJob();
        job1.setId(1);
        job1.setStatus(JobBuilderController.STATUS_PENDING);
        
        final VEGLJob job2 = new VEGLJob();
        job2.setId(2);
        job2.setStatus(JobBuilderController.STATUS_ACTIVE);
        
        final List<VEGLJob> pendingActiveJobs = Arrays.asList(job1, job2);
        final ANVGLUser user = new ANVGLUser();
        
        context.checking(new Expectations() {{
            allowing(mockJobManager).getPendingOrActiveJobs();will(returnValue(pendingActiveJobs));
            allowing(mockUserService).getByEmail(null); will(returnValue(user));
            allowing(mockNciService).getByUser(user); will(returnValue(null));
            oneOf(mockJobStatusMonitor).statusUpdate(pendingActiveJobs);will(throwException(new JobStatusException(new Exception(), job1)));
        }});
        
        monitor.executeInternal(mockJobExecCtx);
    }
}
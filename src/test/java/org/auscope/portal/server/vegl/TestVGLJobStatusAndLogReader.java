package org.auscope.portal.server.vegl;

import java.io.InputStream;
import java.util.HashMap;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.controllers.JobListController;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

public class TestVGLJobStatusAndLogReader extends PortalTestClass {
    private final String storageServiceId = "storage-service-id";
    private VEGLJobManager mockJobManager;
    private CloudStorageService[] mockCloudStorageServices;
    private CloudComputeService[] mockCloudComputeServices;
    private VGLJobStatusAndLogReader jobStatLogReader;
    
    @Before
    public void init() {
        mockJobManager = context.mock(VEGLJobManager.class);
        mockCloudStorageServices = new CloudStorageService[] {context.mock(CloudStorageService.class)};
        mockCloudComputeServices = new CloudComputeService[] {context.mock(CloudComputeService.class)};
        
        context.checking(new Expectations() {{
            allowing(mockCloudStorageServices[0]).getId();will(returnValue(storageServiceId));
        }});
        
        jobStatLogReader = new VGLJobStatusAndLogReader(mockJobManager, 
                mockCloudStorageServices, mockCloudComputeServices);
    }
    
    @Test
    public void testGetJobStatus_PendingToActive() {
    }
    
    @Test
    public void testGetJobStatus_PendingToDone() {
    }
    
    @Test
    public void testGetJobStatus_DoneOrUnsubmittedJob() {
        final int jobId = 123;
        final String job123Status = JobBuilderController.STATUS_DONE;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        
        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            oneOf(mockJob).getId();will(returnValue(jobId));
            allowing(mockJob).getStatus();will(returnValue(job123Status));
        }});
        
        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(job123Status, status);
    }
    
    @Test
    public void testGetJobStatus_JobDNE() {
        final int jobId = 123;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        
        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(null));
            oneOf(mockJob).getId();will(returnValue(jobId));
        }});
        
        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertNull(status);
    }
    
    @Test
    public void testGetJobStatus_NoStorageService() {
        final int jobId = 123;
        final String job123Status = JobBuilderController.STATUS_PENDING;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        
        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(jobId));
            allowing(mockJob).getStatus();will(returnValue(job123Status));
            allowing(mockJob).getStorageServiceId();will(returnValue("does-not-exist"));
        }});
        
        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(job123Status, status);
    }
    
    @Test
    public void testGetJobStatus_StorageServiceError() throws Exception {
        final int jobId = 123;
        final String job123Status = JobBuilderController.STATUS_PENDING;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        
        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(mockJob));
            allowing(mockJob).getId();will(returnValue(jobId));
            allowing(mockJob).getStatus();will(returnValue(job123Status));
            allowing(mockJob).getStorageServiceId();will(returnValue("storageServiceId"));
            allowing(mockCloudStorageServices[0]).listJobFiles(mockJob);will(throwException(new PortalServiceException("error")));
        }});
        
        String status = jobStatLogReader.getJobStatus(mockJob);
        Assert.assertEquals(job123Status, status);
    }
    
    /**
     * Tests that log sectioning works as expected
     * @throws Exception
     */
    @Test
    public void testGetSectionedLogs() throws Exception {
        final InputStream logContents = ResourceUtil.loadResourceAsStream("sectionedVglLog.txt");
        final String logContentString = IOUtils.toString(ResourceUtil.loadResourceAsStream("sectionedVglLog.txt"));
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, JobListController.VGL_LOG_FILE);will(returnValue(logContents));
        }});

        HashMap map = jobStatLogReader.getSectionedLogs(mockJob);

        //There should be 3 sections (we don't care about line ending formats - normalise it to unix style \n)
        Assert.assertEquals(4, map.keySet().size());
        Assert.assertEquals("contents of env\n", stripCarriageReturns(map.get("environment").toString()));
        Assert.assertEquals("multiple\nlines\n", stripCarriageReturns(map.get("test").toString()));
        Assert.assertEquals("text\n", stripCarriageReturns(map.get("spaced header").toString()));
        Assert.assertEquals(stripCarriageReturns(logContentString), stripCarriageReturns(map.get("Full").toString()));
    }
    
    /**
     * Tests that log sectioning fails as expected when log lookup fails
     * @throws Exception
     */
    @Test
    public void testGetSectionedLogs_NoStorageService() throws Exception {        
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockJob).getStorageServiceId();will(returnValue("does-not-exist"));
        }});
        
        try {
            jobStatLogReader.getSectionedLogs(mockJob);
        } catch (PortalServiceException ex) {
            Assert.assertEquals("The specified job doesn't have a storage service.", ex.getMessage());
            Assert.assertEquals("Please ensure you have chosen a storage provider for the job.", ex.getErrorCorrection());
        }
    }

    /**
     * Tests that log sectioning fails as expected when log lookup fails
     * @throws Exception
     */
    @Test
    public void testGetSectionedLogs_LogAccessError() throws Exception {
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            allowing(mockJob).getStorageServiceId();will(returnValue(storageServiceId));
            oneOf(mockCloudStorageServices[0]).getJobFile(mockJob, JobListController.VGL_LOG_FILE);will(throwException(new PortalServiceException("error")));
        }});
        
        try {
            jobStatLogReader.getSectionedLogs(mockJob);
        } catch (PortalServiceException ex) {
            Assert.assertEquals("The specified job hasn't uploaded any logs yet.", ex.getMessage());
        }
    }

    private String stripCarriageReturns(String s) {
        return s.replaceAll("\r", "");
    }    
}
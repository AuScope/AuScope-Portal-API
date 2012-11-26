package org.auscope.portal.server.vegl;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.junit.Before;
import org.junit.Test;
import org.jmock.Expectations;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Unit tests for VEGLJobDao
 * @author Richard Goh
 */
public class TestVEGLJobManager extends PortalTestClass {
    private VEGLJobDao mockJobDao;
    private VEGLSeriesDao mockSeriesDao;
    private VGLJobAuditLogDao mockJobAuditLogDao;
    private VGLSignatureDao mockSignatureDao;
    private VEGLJobManager jobManager;
    
    /**
     * Load our mock objects
     */
    @Before
    public void init() {
        // Setting up mock objects needed for Object Under Test (OUT)
        mockJobDao = context.mock(VEGLJobDao.class);
        mockSeriesDao = context.mock(VEGLSeriesDao.class);
        mockJobAuditLogDao = context.mock(VGLJobAuditLogDao.class);
        mockSignatureDao = context.mock(VGLSignatureDao.class);
        // Object Under Test
        jobManager = new VEGLJobManager();
        jobManager.setVeglJobDao(mockJobDao);
        jobManager.setVeglSeriesDao(mockSeriesDao);
        jobManager.setVglJobAuditLogDao(mockJobAuditLogDao);
        jobManager.setVglSignatureDao(mockSignatureDao);
    }    
    
    /**
     * Tests that querying job series of a given criteria succeeds.
     */
    @Test
    public void testQuerySeries() {
        final String user = "user@email.com";
        final String name = "user";
        final String desc = "series description";
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        final List<VEGLSeries> seriesList = Arrays.asList(mockSeries);
        
        context.checking(new Expectations() {{
            oneOf(mockSeriesDao).query(user, name, desc);
            will(returnValue(seriesList));
        }});
        
        Assert.assertNotNull(jobManager.querySeries(user, name, desc));
    }
    
    /**
     * Tests that retrieving jobs of a given series succeeds.
     */
    @Test
    public void testGetSeriesJobs() {
        final int seriesId = 1;
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        final List<VEGLJob> jobList = Arrays.asList(mockJob);
        
        context.checking(new Expectations() {{
            oneOf(mockJobDao).getJobsOfSeries(seriesId);
            will(returnValue(jobList));
        }});
        
        Assert.assertNotNull(jobManager.getSeriesJobs(seriesId));
    }
    
    /**
     * Tests that retrieving job of a given id succeeds.
     * null is return when a job cannot be found.
     */
    @Test
    public void testGetJobById() {
        final int jobId1 = 1;
        final int jobId2 = 2;
        final VEGLJob mockJob = context.mock(VEGLJob.class);

        context.checking(new Expectations() {{
            oneOf(mockJobDao).get(jobId1);will(returnValue(mockJob));
            oneOf(mockJobDao).get(jobId2);will(returnValue(null));
        }});
        
        Assert.assertNotNull(jobManager.getJobById(jobId1));
        Assert.assertNull(jobManager.getJobById(jobId2));
    }
    
    /**
     * Tests that the deleting of a given job succeeds.
     */
    @Test
    public void testDeleteJob() {
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        
        context.checking(new Expectations() {{
            oneOf(mockJobDao).deleteJob(mockJob);
        }});
        
        jobManager.deleteJob(mockJob);
    }
    
    /**
     * Tests that retrieving series of a give id succeeds.
     * null is returned when a series cannot be found.
     */
    @Test
    public void testGetSeriesById() {
        final int series1 = 1;
        final int series2 = 2;
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);

        context.checking(new Expectations() {{
            oneOf(mockSeriesDao).get(series1);
            will(returnValue(mockSeries));
            oneOf(mockSeriesDao).get(series2);
            will(returnValue(null));
        }});
        
        Assert.assertNotNull(jobManager.getSeriesById(series1));
        // Test to ensure null is returned when user's signature 
        // cannot be found.
        Assert.assertNull(jobManager.getSeriesById(series2));
    }
    
    /**
     * Tests that the retrieving of signature of a given user succeeds.
     * null is returned when the user's signature cannot be found.
     */
    @Test
    public void testGetSignatureByUser() {
        final String user1 = "user1@email.com";
        final String user2 = "user2@email.com";
        final VGLSignature mockSignature = context.mock(VGLSignature.class);

        context.checking(new Expectations() {{
            oneOf(mockSignatureDao).getSignatureOfUser(user1);
            will(returnValue(mockSignature));
            oneOf(mockSignatureDao).getSignatureOfUser(user2);
            will(returnValue(null));
        }});
        
        Assert.assertNotNull(jobManager.getSignatureByUser(user1));
        // Test to ensure null is returned when user's signature 
        // cannot be found.
        Assert.assertNull(jobManager.getSignatureByUser(user2));
    }
    
    /**
     * Tests that the storing of a given job succeeds.
     */
    @Test
    public void testSaveJob() {
        final VEGLJob mockJob = context.mock(VEGLJob.class);
        
        context.checking(new Expectations() {{
            oneOf(mockJobDao).save(mockJob);
        }});
        
        jobManager.saveJob(mockJob);
    }
    
    /**
     * Tests that creating job audit trail succeeds.
     */
    @Test
    public void testCreateJobAuditTrail() {
        final String oldJobStatus = JobBuilderController.STATUS_UNSUBMITTED;
        final VEGLJob mockCurJob = context.mock(VEGLJob.class);
        final String message = "Job submitted";
        
        context.checking(new Expectations() {{
            oneOf(mockCurJob).getId();
            will(returnValue(1));
            oneOf(mockCurJob).getStatus();
            will(returnValue(JobBuilderController.STATUS_PENDING));
            oneOf(mockJobAuditLogDao).save(with(any(VGLJobAuditLog.class)));
        }});
        
        jobManager.createJobAuditTrail(oldJobStatus, mockCurJob, message);
    }
    
    /**
     * Tests that creating job audit trail fails.
     */
    @Test
    public void testCreateJobAuditTrail_Exception() {
        final String oldJobStatus = JobBuilderController.STATUS_UNSUBMITTED;
        final VEGLJob mockCurJob = context.mock(VEGLJob.class);
        final String message = "Job submitted";
        
        context.checking(new Expectations() {{
            oneOf(mockCurJob).getId();
            will(returnValue(1));
            oneOf(mockCurJob).getStatus();
            will(returnValue(JobBuilderController.STATUS_PENDING));
            oneOf(mockJobAuditLogDao).save(with(any(VGLJobAuditLog.class)));
            will(throwException(new DataRetrievalFailureException("")));
        }});
        
        jobManager.createJobAuditTrail(oldJobStatus, mockCurJob, message);
    }
    
    /**
     * Tests that deleting a given series succeeds.
     */
    @Test
    public void testDeleteSeries() {
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        
        context.checking(new Expectations() {{
            oneOf(mockSeriesDao).delete(mockSeries);
        }});
        
        jobManager.deleteSeries(mockSeries);
    }
    
    /**
     * Tests that storing a given series succeeds.
     */
    @Test
    public void testSaveSeries() {
        final VEGLSeries mockSeries = context.mock(VEGLSeries.class);
        
        context.checking(new Expectations() {{
            oneOf(mockSeriesDao).save(mockSeries);
        }});
        
        jobManager.saveSeries(mockSeries);
    }
    
    /**
     * Tests that storing a given user's signature succeeds.
     */
    @Test
    public void testSaveSignature() {
        final VGLSignature mockSignature = context.mock(VGLSignature.class);
        
        context.checking(new Expectations() {{
            oneOf(mockSignatureDao).save(mockSignature);
        }});
        
        jobManager.saveSignature(mockSignature);
    }
}
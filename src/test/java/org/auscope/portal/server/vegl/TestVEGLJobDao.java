package org.auscope.portal.server.vegl;

import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.auscope.portal.server.test.VGLPortalTestClass;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Unit tests for VEGLJobDao
 * @author Richard Goh
 */
@PrepareForTest({HibernateDaoSupport.class})
public class TestVEGLJobDao extends VGLPortalTestClass {
    private HibernateTemplate mockTemplate;
    private VEGLJob mockVEGLJob;
    private VEGLJobDao testDao;
    
    @Before
    public void setup() throws Exception {       
        // Setting up mock objects needed for Object Under Test (OUT)
        mockTemplate = context.mock(HibernateTemplate.class);
        mockVEGLJob = context.mock(VEGLJob.class);
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);        
        
        // DAO under test
        testDao = new VEGLJobDao();
    }
    
    @Test
    public void testGetJobsOfSeries() {
        final String query = "from VEGLJob j where j.seriesId=:searchID and lower(j.status)!='deleted'";
        final int fakeSeriesId1 = 1;
        final int fakeSeriesId2 = 2;
        final List<VEGLJob> mockVGLJobList1 = Arrays.asList(
                context.mock(VEGLJob.class, "mockVGLJob1"),
                context.mock(VEGLJob.class, "mockVGLJob2"));
        final List<VEGLJob> mockVGLJobList2 = new ArrayList<VEGLJob>();
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).findByNamedParam(query, "searchID", fakeSeriesId1);
            will(returnValue(mockVGLJobList1));
            oneOf(mockTemplate).findByNamedParam(query, "searchID", fakeSeriesId2);
            will(returnValue(mockVGLJobList2));
        }});
        
        // Test to ensure non-empty list is returned
        List<VEGLJob> logs = testDao.getJobsOfSeries(fakeSeriesId1);
        Assert.assertNotNull(logs);
        Assert.assertTrue(logs.size() > 0);
        
        // Test to ensure empty list is returned
        logs = testDao.getJobsOfSeries(fakeSeriesId2);
        Assert.assertNotNull(logs);
        Assert.assertTrue(logs.size() == 0);
    }
    
    @Test
    public void testGetJobsByEmail() {
        final String query = "from VEGLJob j where j.emailAddress=:email";
        final String emailAddress1 = "user1@email.com";
        final String emailAddress2 = "user2@email.com";
        final List<VEGLJob> mockVGLJobList1 = Arrays.asList(
                context.mock(VEGLJob.class, "mockVGLJob1"),
                context.mock(VEGLJob.class, "mockVGLJob2"));
        final List<VEGLJob> mockVGLJobList2 = new ArrayList<VEGLJob>();
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).findByNamedParam(query, "email", emailAddress1);
            will(returnValue(mockVGLJobList1));
            oneOf(mockTemplate).findByNamedParam(query, "email", emailAddress2);
            will(returnValue(mockVGLJobList2));
        }});
        
        // Test to ensure non-empty list is returned
        List<VEGLJob> vglJobList = testDao.getJobsByEmail(emailAddress1);
        Assert.assertNotNull(vglJobList);
        Assert.assertTrue(vglJobList.size() > 0);
        
        // Test to ensure empty list is returned
        vglJobList = testDao.getJobsByEmail(emailAddress2);
        Assert.assertNotNull(vglJobList);
        Assert.assertTrue(vglJobList.size() == 0);
    }
    
    /**
     * Test that the retrieving of pending or active jobs succeeds.
     */
    @Test
    public void testGetPendingOrActiveJobs() {
        final String query = "from VEGLJob j where lower(j.status)='" 
                + JobBuilderController.STATUS_PENDING + "' or lower(j.status)='" 
                + JobBuilderController.STATUS_ACTIVE + "'";
        final List<VEGLJob> mockVGLJobList1 = Arrays.asList(
                context.mock(VEGLJob.class, "mockVGLJob1"),
                context.mock(VEGLJob.class, "mockVGLJob2"));

        context.checking(new Expectations() {{
            oneOf(mockTemplate).find(query);will(returnValue(mockVGLJobList1));
        }});

        // Test to ensure non-empty list is returned
        List<VEGLJob> jobs = testDao.getPendingOrActiveJobs();
        Assert.assertNotNull(jobs);
        Assert.assertTrue(jobs.size() > 0);
    }
    
    /**
     * Tests that the retrieving of a VGL job succeeds.
     */
    @Test
    public void testGet() {
        final int fakeId1 = 1;
        final int fakeId2 = 2;
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).get(VEGLJob.class, fakeId1);
            will(returnValue(mockVEGLJob));
            oneOf(mockTemplate).get(VEGLJob.class, fakeId2);
            will(returnValue(null));
        }});
        
        Assert.assertNotNull(testDao.get(fakeId1));
        Assert.assertNull(testDao.get(fakeId2));
    }
    
    /**
     * Tests that saving or updating a VGL job succeeds.
     */
    @Test
    public void testSave() {
        final VEGLJob fakeJob = new VEGLJob();

        context.checking(new Expectations() {{
            oneOf(mockTemplate).saveOrUpdate(fakeJob);
        }});

        testDao.save(fakeJob);
    }
    
    /**
     * Tests that deleting a VGL job succeeds.
     */
    @Test
    public void testDelete() {
        final VEGLJob fakeJob = new VEGLJob();

        context.checking(new Expectations() {{
            oneOf(mockTemplate).delete(fakeJob);
        }});

        testDao.deleteJob(fakeJob);
    }
}
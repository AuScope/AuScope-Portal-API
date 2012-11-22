package org.auscope.portal.server.vegl;

import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.auscope.portal.server.test.VGLPortalTestClass;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Unit tests for VGLJobAuditLogDao
 * @author Richard Goh
 */
@PrepareForTest({HibernateDaoSupport.class})
public class TestVGLJobAuditLogDao extends VGLPortalTestClass {
    private final String query = "from VGLJobAuditLog j where j.jobId=:jobId";
    private HibernateTemplate mockTemplate;
    private VGLJobAuditLog mockVGLJobAuditLog;
    private VGLJobAuditLogDao testDao;
    
    @Before
    public void setup() throws Exception {       
        // Setting up mock objects needed for Object Under Test (OUT)
        mockTemplate = context.mock(HibernateTemplate.class);
        mockVGLJobAuditLog = context.mock(VGLJobAuditLog.class);
        // DAO under test
        testDao = new VGLJobAuditLogDao();
    }
    
    /**
     * Tests that the retrieving of a job audit logs succeeds.
     */
    @Test
    public void testGetAuditLogsOfJob() {
        final int fakeJobId1 = 1; // non-empty list
        final int fakeJobId2 = 2; // empty list
        final List<VGLJobAuditLog> mockLogs1 = Arrays.asList(
                context.mock(VGLJobAuditLog.class, "mockLog1"),
                context.mock(VGLJobAuditLog.class, "mockLog2"));
        final List<VGLJobAuditLog> mockLogs2 = new ArrayList<VGLJobAuditLog>();
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).findByNamedParam(query, "jobId", fakeJobId1);
            will(returnValue(mockLogs1));
            oneOf(mockTemplate).findByNamedParam(query, "jobId", fakeJobId2);
            will(returnValue(mockLogs2));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        // Test to ensure non-empty list is returned
        List<VGLJobAuditLog> logs = testDao.getAuditLogsOfJob(fakeJobId1);
        Assert.assertNotNull(logs);
        Assert.assertTrue(logs.size() > 0);
        
        // Test to ensure empty list is returned
        logs = testDao.getAuditLogsOfJob(fakeJobId2);
        Assert.assertNotNull(logs);
        Assert.assertTrue(logs.size() == 0);
    }
    
    /**
     * Tests that the retrieving of a job audit log succeeds.
     */
    @Test
    public void testGet() {
        final int fakeId1 = 1;
        final int fakeId2 = 2;
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).get(VGLJobAuditLog.class, fakeId1);
            will(returnValue(mockVGLJobAuditLog));
            oneOf(mockTemplate).get(VGLJobAuditLog.class, fakeId2);
            will(returnValue(null));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        Assert.assertNotNull(testDao.get(fakeId1));
        Assert.assertNull(testDao.get(fakeId2));
    }
    
    /**
     * Tests that saving or updating a job audit log succeeds.
     */
    @Test
    public void testSave() {
        final VGLJobAuditLog fakeLog = new VGLJobAuditLog();

        context.checking(new Expectations() {{
            oneOf(mockTemplate).saveOrUpdate(fakeLog);
        }});

        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);

        testDao.save(fakeLog);
    }
}
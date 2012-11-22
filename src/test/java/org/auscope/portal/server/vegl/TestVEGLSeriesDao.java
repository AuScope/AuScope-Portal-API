package org.auscope.portal.server.vegl;

import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

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
 * Unit tests for VEGLSeriesDao
 * @author Richard Goh
 */
@PrepareForTest({HibernateDaoSupport.class})
public class TestVEGLSeriesDao extends VGLPortalTestClass {
    private final String baseQuery = "from VEGLSeries s where";
    private HibernateTemplate mockTemplate;
    private VEGLSeries mockVEGLSeries;
    private VEGLSeriesDao testDao;
    
    @Before
    public void setup() throws Exception {       
        // Setting up mock objects needed for Object Under Test (OUT)
        mockTemplate = context.mock(HibernateTemplate.class);
        mockVEGLSeries = context.mock(VEGLSeries.class);
        // DAO under test
        testDao = new VEGLSeriesDao();
    }
    
    /**
     * Tests that the retrieving of job series records with 
     * all criteria succeeds.
     */    
    @Test
    public void testQuery_AllCriteria() {
        final String user = "test_user";
        final String name = "test_series_name";
        final String desc = "test_series_desc";
        final String query = baseQuery + " s.user = '" 
                + user + "' and s.name like '%" 
                + name + "%' and s.description like '%"
                + desc + "%'";
        
        final List<VEGLSeries> mockUserSeriesList = Arrays.asList(
                context.mock(VEGLSeries.class, "mockUserSeries1"),
                context.mock(VEGLSeries.class, "mockUserSeries2"));
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).find(query);
            will(returnValue(mockUserSeriesList));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        // Test to ensure non-empty list is returned
        List<VEGLSeries> seriesList = testDao.query(user, name, desc);
        Assert.assertNotNull(seriesList);
        Assert.assertTrue(seriesList.size() > 0);
    }
    
    /**
     * Tests that the retrieving of job series records with 
     * user and name criteria succeeds.
     */
    @Test
    public void testQuery_UserAndNameCriteria() {
        final String user = "test_user";
        final String name = "test_series_name";
        final String desc = null;
        final String query = baseQuery + " s.user = '" 
                + user + "' and s.name like '%" 
                + name + "%'";
        
        final List<VEGLSeries> mockUserSeriesList = Arrays.asList(
                context.mock(VEGLSeries.class, "mockUserSeries1"),
                context.mock(VEGLSeries.class, "mockUserSeries2"));
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).find(query);
            will(returnValue(mockUserSeriesList));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        // Test to ensure non-empty list is returned
        List<VEGLSeries> seriesList = testDao.query(user, name, desc);
        Assert.assertNotNull(seriesList);
        Assert.assertTrue(seriesList.size() > 0);
    }
    
    /**
     * Tests that the retrieving of job series records with 
     * user and desc criteria succeeds.
     */
    @Test
    public void testQuery_UserAndDescCriteria() {
        final String user = "test_user";
        final String name = null;
        final String desc = "test_series_desc";
        final String query = baseQuery + " s.user = '" 
                + user + "' and s.description like '%"
                + desc + "%'";
        
        final List<VEGLSeries> mockUserSeriesList = Arrays.asList(
                context.mock(VEGLSeries.class, "mockUserSeries1"),
                context.mock(VEGLSeries.class, "mockUserSeries2"));
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).find(query);
            will(returnValue(mockUserSeriesList));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        // Test to ensure non-empty list is returned
        List<VEGLSeries> seriesList = testDao.query(user, name, desc);
        Assert.assertNotNull(seriesList);
        Assert.assertTrue(seriesList.size() > 0);
    }
    
    /**
     * Tests that the retrieving of job series records with 
     * user only criterion succeeds.
     */    
    @Test
    public void testQuery_UserCriterion() {
        final String user = "testuser1";
        final String name = null;
        final String desc = null;
        final String query = baseQuery + " s.user = '" + user +"'";
        final List<VEGLSeries> mockUserSeriesList = Arrays.asList(
                context.mock(VEGLSeries.class, "mockUserSeries1"),
                context.mock(VEGLSeries.class, "mockUserSeries2"));        

        context.checking(new Expectations() {{
            oneOf(mockTemplate).find(query);
            will(returnValue(mockUserSeriesList));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        // Test to ensure non-empty list is returned
        List<VEGLSeries> seriesList = testDao.query(user, name, desc);
        Assert.assertNotNull(seriesList);
        Assert.assertTrue(seriesList.size() > 0);
    }
    
    /**
     * Tests that the retrieving of job series records with 
     * name only criterion succeeds.
     * NOTE: In practice and for security reason, the user parameter
     * shouldn't be null as this will allow other users' job 
     * series records to be queried. That checking is currently
     * implemented on the caller side.
     */    
    @Test
    public void testQuery_NameCriterion() {
        final String user = null;
        final String name = "test_name";
        final String desc = null;
        final String query = baseQuery + " s.name like '%" + name +"%'";
        final List<VEGLSeries> mockUserSeriesList = Arrays.asList(
                context.mock(VEGLSeries.class, "mockUserSeries1"),
                context.mock(VEGLSeries.class, "mockUserSeries2"));        

        context.checking(new Expectations() {{
            oneOf(mockTemplate).find(query);
            will(returnValue(mockUserSeriesList));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        // Test to ensure non-empty list is returned
        List<VEGLSeries> seriesList = testDao.query(user, name, desc);
        Assert.assertNotNull(seriesList);
        Assert.assertTrue(seriesList.size() > 0);
    }
    
    /**
     * Tests that the retrieving of job series records with 
     * desc only criterion succeeds.
     * NOTE: In practice and for security reason, the user parameter
     * shouldn't be null as this will allow other users' job 
     * series records to be queried. That checking is currently
     * implemented on the caller side. 
     */
    @Test
    public void testQuery_DescCriterion() {
        final String user = "";
        final String name = "";
        final String desc = "test_desc";
        final String query = baseQuery + " s.description like '%" + desc +"%'";
        final List<VEGLSeries> mockUserSeriesList = Arrays.asList(
                context.mock(VEGLSeries.class, "mockUserSeries1"),
                context.mock(VEGLSeries.class, "mockUserSeries2"));        

        context.checking(new Expectations() {{
            oneOf(mockTemplate).find(query);
            will(returnValue(mockUserSeriesList));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        // Test to ensure non-empty list is returned
        List<VEGLSeries> seriesList = testDao.query(user, name, desc);
        Assert.assertNotNull(seriesList);
        Assert.assertTrue(seriesList.size() > 0);
    }
    
    /**
     * Tests that the retrieving of a job series with empty 
     * or null criteria returns null.
     * NOTE: In practice and for security reason, the user parameter
     * shouldn't be null as this will allow other users' job 
     * series records to be queried. That checking is currently
     * implemented on the caller side. 
     */
    @Test
    public void testQuery_EmptyOrNullCriteria() {
        String user = null;
        String name = null;
        String desc = null;

        List<VEGLSeries> seriesList = testDao.query(user, name, desc);
        Assert.assertNull(seriesList);
        
        user = "";
        name = "";
        desc = "";
        
        seriesList = testDao.query(user, name, desc);
        Assert.assertNull(seriesList);   
    }
    
    /**
     * Tests that the retrieving of a job series succeeds.
     */
    @Test
    public void testGet() {
        final int fakeId1 = 1;
        final int fakeId2 = 2;
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).get(VEGLSeries.class, fakeId1);
            will(returnValue(mockVEGLSeries));
            oneOf(mockTemplate).get(VEGLSeries.class, fakeId2);
            will(returnValue(null));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        Assert.assertNotNull(testDao.get(fakeId1));
        Assert.assertNull(testDao.get(fakeId2));
    }
    
    /**
     * Tests that saving or updating a job series succeeds.
     */
    @Test
    public void testSave() {
        final VEGLSeries fakeSeries = new VEGLSeries();

        context.checking(new Expectations() {{
            oneOf(mockTemplate).saveOrUpdate(fakeSeries);
        }});

        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);

        testDao.save(fakeSeries);
    }
    
    /**
     * Tests that deleting a job series succeeds.
     */
    @Test
    public void testDelete() {
        final VEGLSeries fakeSeries = new VEGLSeries();

        context.checking(new Expectations() {{
            oneOf(mockTemplate).delete(fakeSeries);
        }});

        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);

        testDao.delete(fakeSeries);
    }
}
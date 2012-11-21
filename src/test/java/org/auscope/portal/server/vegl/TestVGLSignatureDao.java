package org.auscope.portal.server.vegl;

import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;
import junit.framework.Assert;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Unit tests for VGLSignatureDao
 * @author Richard Goh
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HibernateDaoSupport.class})
public class TestVGLSignatureDao {
    private Mockery context = new JUnit4Mockery(){{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private String query = "from VGLSignature s where s.user=:user";
    private Session mockSession;
    private HibernateTemplate mockTemplate;
    private Query mockQuery;
    private VGLSignature mockVGLSignature;
    private VGLSignatureDao testDao;
    
    @Before
    public void setup() throws Exception {        
        mockSession = context.mock(Session.class);
        mockTemplate = context.mock(HibernateTemplate.class);
        mockQuery = context.mock(Query.class);
        mockVGLSignature = context.mock(VGLSignature.class);
        
        // DAO under test
        testDao = new VGLSignatureDao();
    }
    
    /**
     * Tests that the retrieving of user signature succeeded
     * when the user can be found in the database.
     */
    @Test
    public void testGetSignatureOfUser() {
        final String user = "user@email.com";
        
        context.checking(new Expectations(){{
            oneOf(mockSession).createQuery(query);will(returnValue(mockQuery));
            oneOf(mockQuery).setParameter("user", user);will(returnValue(mockQuery));
            oneOf(mockQuery).uniqueResult();will(returnValue(mockVGLSignature));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getSession")).toReturn(mockSession);        

        VGLSignature userSignature = testDao.getSignatureOfUser(user);
        Assert.assertNotNull(userSignature);
    }
    
    /**
     * Tests that the retrieving of user signature succeeded
     * when the user domain can be found in the database.
     */
    @Test
    public void testGetSignatureOfUser_DomainExists() {
        final String fakeUser = "user@email.com";
        final String fakeUserDomain = "@email.com";
        final VGLSignature fakeUserSignature = new VGLSignature(1, fakeUser);
        
        context.checking(new Expectations(){{ 
            oneOf(mockSession).createQuery(query);will(returnValue(mockQuery));
            oneOf(mockQuery).setParameter("user", fakeUser);will(returnValue(mockQuery));
            oneOf(mockQuery).uniqueResult();will(returnValue(null));
            
            oneOf(mockQuery).setParameter("user", fakeUserDomain);will(returnValue(mockQuery));
            oneOf(mockQuery).uniqueResult();will(returnValue(fakeUserSignature));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getSession")).toReturn(mockSession);        

        VGLSignature userSignature = testDao.getSignatureOfUser(fakeUser);
        Assert.assertNotNull(userSignature);
        Assert.assertNull(userSignature.getId());
        Assert.assertEquals(fakeUser, userSignature.getUser());
    }
    
    /**
     * Tests that retrieving of user signature returns null when 
     * the user and user domain cannot be found in database.
     */
    @Test
    public void testGetSignatureOfUserDNE() {
        final String user = "userdoesnotexist@email.com";
        final String expectedUserDomain = "@email.com";
        
        context.checking(new Expectations(){{
            oneOf(mockSession).createQuery(query);will(returnValue(mockQuery));
            oneOf(mockQuery).setParameter("user", user);will(returnValue(mockQuery));            
            oneOf(mockQuery).setParameter("user", expectedUserDomain);will(returnValue(mockQuery));
            allowing(mockQuery).uniqueResult();will(returnValue(null));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getSession")).toReturn(mockSession);  
        
        VGLSignature userSignature = testDao.getSignatureOfUser(user);
        Assert.assertNull(userSignature);
    }
    
    /**
     * Tests that retrieving a given user signature succeeds.
     */
    @Test
    public void testGet() {
        final int fakeId1 = 1;
        final int fakeId2 = 2;
        
        context.checking(new Expectations() {{
            oneOf(mockTemplate).get(VGLSignature.class, fakeId1);
            will(returnValue(mockVGLSignature));
            oneOf(mockTemplate).get(VGLSignature.class, fakeId2);
            will(returnValue(null));
        }});
        
        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);
        
        Assert.assertNotNull(testDao.get(fakeId1));
        Assert.assertNull(testDao.get(fakeId2));
    }
    
    /**
     * Tests that saving or updating a given user signature succeeds.
     */
    @Test
    public void testSave() {
        final String fakeUser = "user@email.com";
        final VGLSignature fakeUserSignature = new VGLSignature(1, fakeUser);

        context.checking(new Expectations() {{
            oneOf(mockTemplate).saveOrUpdate(fakeUserSignature);
        }});

        // Stub the protected method
        stub(method(HibernateDaoSupport.class, "getHibernateTemplate"))
                .toReturn(mockTemplate);

        testDao.save(fakeUserSignature);
    }
}
package org.auscope.portal.server.web.service;

import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.test.ResourceUtil;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSimpleWfsService extends PortalTestClass {
    final HttpServiceCaller mockServiceCaller = context.mock(HttpServiceCaller.class);
    final WFSGetFeatureMethodMaker mockMethodMaker = context.mock(WFSGetFeatureMethodMaker.class);
    final HttpRequestBase mockMethod = context.mock(HttpRequestBase.class);
    
    SimpleWfsService service;
    
    @Before
    public void setup() {
        service = new SimpleWfsService(mockServiceCaller, mockMethodMaker);
    }
    
    /**
     * Tests that a DescribeFeatureType response is correctly parsed
     * @throws Exception
     */
    @Test
    public void testDescribeFeatures() throws Exception {
        
        final String endpoint = "http://example.com/wfs";
        final String typeName = "ga:gravitypoints";
        final String responseString = ResourceUtil.loadResourceAsString("DescribeFeatureTypeResponse-SF0.xml");
        
        context.checking(new Expectations() {{
            oneOf(mockMethodMaker).makeDescribeFeatureTypeMethod(endpoint, typeName);
            will(returnValue(mockMethod));
            
            oneOf(mockServiceCaller).getMethodResponseAsString(mockMethod);
            will(returnValue(responseString));
        }});
        
        List<SimpleFeatureProperty> result = service.describeSimpleFeature(endpoint, typeName);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(18, result.size());
        
        SimpleFeatureProperty sf0 = result.get(0);
        Assert.assertEquals(1, sf0.getMaxOccurs());
        Assert.assertEquals(1, sf0.getMinOccurs());
        Assert.assertEquals(0, sf0.getIndex());
        Assert.assertEquals("xsd:int", sf0.getTypeName());
        Assert.assertEquals("id", sf0.getName());
        Assert.assertFalse(sf0.isNillable());
        
        SimpleFeatureProperty sf17 = result.get(17);
        Assert.assertEquals(1, sf17.getMaxOccurs());
        Assert.assertEquals(0, sf17.getMinOccurs());
        Assert.assertEquals(17, sf17.getIndex());
        Assert.assertEquals("xsd:double", sf17.getTypeName());
        Assert.assertEquals("infinite_slab_bouguer_anomaly", sf17.getName());
        Assert.assertTrue(sf17.isNillable());
    }
    
}

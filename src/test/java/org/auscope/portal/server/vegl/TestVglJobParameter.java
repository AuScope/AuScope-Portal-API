package org.auscope.portal.server.vegl;

import junit.framework.Assert;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Test;

public class TestVglJobParameter extends PortalTestClass {
    /**
     * Unit test to ensure 'equal' objects return the same hashcode
     * @throws Exception
     */
    @Test
    public void testEqualsMatchesHashcode() throws Exception {
        VglParameter p1 = new VglParameter(1, "name1", "v1", "number", new VEGLJob(1));
        VglParameter p2 = new VglParameter(2, "name1", "v2", "string", new VEGLJob(1));
        VglParameter p3 = new VglParameter(3, "name1", "v3", "number", new VEGLJob(2));
        VglParameter p4 = new VglParameter(4, "name2", "v4", "string", new VEGLJob(1));
        VglParameter p5 = new VglParameter(5, "name3", "v5", "number", new VEGLJob(3));

        //Test general equality
        Assert.assertTrue(equalsWithHashcode(p1, p1));
        Assert.assertTrue(equalsWithHashcode(p1, p2));
        Assert.assertFalse(equalsWithHashcode(p1, p3));
        Assert.assertFalse(equalsWithHashcode(p1, p4));
        Assert.assertFalse(equalsWithHashcode(p1, p5));
        Assert.assertFalse(equalsWithHashcode(p2, p3));
        Assert.assertFalse(equalsWithHashcode(p2, p4));
        Assert.assertFalse(equalsWithHashcode(p2, p5));
        Assert.assertFalse(equalsWithHashcode(p3, p4));
        Assert.assertFalse(equalsWithHashcode(p3, p5));
        Assert.assertFalse(equalsWithHashcode(p4, p5));
    }
}

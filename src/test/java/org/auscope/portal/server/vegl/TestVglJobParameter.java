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
        VglParameter p1 = new VglParameter(1, 42, "name1");
        VglParameter p2 = new VglParameter(2, 42, "name1");
        VglParameter p3 = new VglParameter(3, 43, "name1");
        VglParameter p4 = new VglParameter(4, 42, "name2");
        VglParameter p5 = new VglParameter(5, 123, "name3");

        p1.setType("number");
        p2.setType("string");
        p3.setType("number");
        p4.setType("string");
        p5.setType("number");

        p1.setValue("v1");
        p2.setValue("v2");
        p3.setValue("v3");
        p4.setValue("v4");
        p5.setValue("v5");

        //Test general equality
        Assert.assertTrue(p1.equals(p1));
        Assert.assertTrue(p1.equals(p2));
        Assert.assertFalse(p1.equals(p3));
        Assert.assertFalse(p1.equals(p4));
        Assert.assertFalse(p1.equals(p5));
        Assert.assertFalse(p2.equals(p3));
        Assert.assertFalse(p2.equals(p4));
        Assert.assertFalse(p2.equals(p5));
        Assert.assertFalse(p3.equals(p4));
        Assert.assertFalse(p3.equals(p5));
        Assert.assertFalse(p4.equals(p5));

        //Test hashcode equality
        Assert.assertEquals(p1.hashCode(), p1.hashCode());
        Assert.assertEquals(p1.hashCode(), p2.hashCode());
    }
}

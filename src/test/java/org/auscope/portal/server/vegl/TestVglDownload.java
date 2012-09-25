package org.auscope.portal.server.vegl;

import junit.framework.Assert;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Test;

public class TestVglDownload extends PortalTestClass {
    /**
     * Tests equals and hashCode align
     */
    @Test
    public void testEquality() {
        VglDownload dl1 = new VglDownload(1);
        VglDownload dl2 = new VglDownload(1);
        VglDownload dl3 = new VglDownload(2);

        Assert.assertTrue(equalsWithHashcode(dl1, dl2));
        Assert.assertTrue(equalsWithHashcode(dl2, dl1));
        Assert.assertFalse(equalsWithHashcode(dl1, dl3));
    }
}

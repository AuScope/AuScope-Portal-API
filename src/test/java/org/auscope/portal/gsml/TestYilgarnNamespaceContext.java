package org.auscope.portal.gsml;

import javax.xml.namespace.NamespaceContext;

import org.junit.Assert;
import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Test;

/**
 * The Class TestYilgarnNamespaceContext.
 */
public class TestYilgarnNamespaceContext extends PortalTestClass {

    /** The TEST prefix. */
    private static final String TESTPREFIX = "ogc";

    /** The namespace uri. */
    private static final String TESTNAMESPACEURI = "http://www.opengis.net/ogc";

    /**
     * Test context.
     */
    @Test
    public void testContext() {

        NamespaceContext context = new YilgarnNamespaceContext();

        Assert.assertEquals(TESTNAMESPACEURI, context.getNamespaceURI(TESTPREFIX));
    }

}

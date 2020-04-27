package org.auscope.portal.mineraloccurrence;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.domain.ogc.AbstractFilterTestUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

public class TestMinOccurViewFilter extends PortalTestClass {

    /**
     * Test with commodity
     * 
     * @throws Exception
     */
    @Test
    public void testMinOccurFilter() throws Exception {
        MinOccurViewFilter filter = new MinOccurViewFilter("abc", "cde", "fgh", "ijk");

        String result = filter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(result);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"abc"}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc,
                "/descendant::ogc:PropertyIsGreaterThanOrEqualTo/ogc:Literal",
                new String[] {"cde", "fgh", "ijk"}, 3);

    }

    /**
     * Test without commodity. Should return a empty string.
     * 
     * @throws Exception
     */
    @Test
    public void testEmptyComoodityFilter() throws Exception {
        MinOccurViewFilter filter = new MinOccurViewFilter("", "", "", "");

        String result = filter.getFilterStringAllRecords();
        Assert.assertTrue(result.isEmpty());

    }
}

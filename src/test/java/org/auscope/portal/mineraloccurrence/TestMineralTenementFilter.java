package org.auscope.portal.mineraloccurrence;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.domain.ogc.AbstractFilterTestUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

public class TestMineralTenementFilter extends PortalTestClass {
    /**
     * Test with commodity
     *
     * @throws Exception
     */
    @Test
    public void testMinOccurFilter() throws Exception {
        MineralTenementFilter filter = new MineralTenementFilter(
        		"{\"value\":\"abc\",\"label\":\"Name\",\"toolTip\":null,\"xpath\":\"mt:name\",\"predicate\":\"ISLIKE\",\"type\":\"OPTIONAL.TEXT\"},"
        + "{\"value\":\"def\",\"label\":\"Tenement Type\",\"toolTip\":null,\"xpath\":\"mt:tenementType\",\"predicate\":\"ISLIKE\",\"type\":\"OPTIONAL.TEXT\"},"
        + "{\"value\":\"ghi\",\"label\":\"Owner\",\"toolTip\":null,\"xpath\":\"mt:owner\",\"predicate\":\"ISLIKE\",\"type\":\"OPTIONAL.TEXT\"},"
        + "{\"value\":\"2020-09-17\",\"label\":\"Start Date\",\"toolTip\":null,\"xpath\":\"mt:grantDate\",\"predicate\":\"BIGGER_THAN\",\"type\":\"OPTIONAL.DATE\"},"
        + "{\"value\":\"2020-09-17\",\"label\":\"End Date\",\"toolTip\":null,\"xpath\":\"mt:endDate\",\"predicate\":\"SMALLER_THAN\",\"type\":\"OPTIONAL.DATE\"},"
        , null);

        String result = filter.getFilterStringAllRecords();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(result);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"*abc*", "*def*", "*ghi*"}, 3);

    }

    /**
     * Test without commodity. Should return a empty string.
     *
     * @throws Exception
     */
    @Test
    public void testEmptyComoodityFilter() throws Exception {
        MineralTenementFilter filter = new MineralTenementFilter("", null);

        String result = filter.getFilterStringAllRecords();
        Assert.assertTrue(result.isEmpty());

    }

    /**
     * Test without commodity. Should return a empty string.
     *
     * @throws Exception
     */
    @Test
    public void testAdditionalStyle() throws Exception {
        MineralTenementFilter filter = new MineralTenementFilter(
        		"{\"value\":\"abc\",\"label\":\"Name\",\"toolTip\":null,\"xpath\":\"mt:name\",\"predicate\":\"ISLIKE\",\"type\":\"OPTIONAL.TEXT\"},"
                + "{\"value\":\"def\",\"label\":\"Tenement Type\",\"toolTip\":null,\"xpath\":\"mt:tenementType\",\"predicate\":\"ISLIKE\",\"type\":\"OPTIONAL.TEXT\"},"
                + "{\"value\":\"ghi\",\"label\":\"Owner\",\"toolTip\":null,\"xpath\":\"mt:owner\",\"predicate\":\"ISLIKE\",\"type\":\"OPTIONAL.TEXT\"},"
                + "{\"value\":\"2020-09-16\",\"label\":\"Start Date\",\"toolTip\":null,\"xpath\":\"mt:grantDate\",\"predicate\":\"BIGGER_THAN\",\"type\":\"OPTIONAL.DATE\"},"
                + "{\"value\":\"2020-09-17\",\"label\":\"End Date\",\"toolTip\":null,\"xpath\":\"mt:endDate\",\"predicate\":\"SMALLER_THAN\",\"type\":\"OPTIONAL.DATE\"}"
                , null);

        String result = filter.getFilterWithAdditionalStyle();
        Document doc = AbstractFilterTestUtilities.parsefilterStringXML(result);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc, "/descendant::ogc:PropertyIsLike/ogc:Literal",
                new String[] {"*abc*", "*def*", "*ghi*", "Active", "GRANTED"}, 5);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc,
                "/descendant::ogc:PropertyIsGreaterThan/ogc:Function/ogc:Literal[2]",
                new String[] {"  2020-09-16 00:00:00 "}, 1);
        AbstractFilterTestUtilities.runNodeSetValueCheck(doc,
                "/descendant::ogc:PropertyIsLessThan/ogc:Function/ogc:Literal[2]",
                new String[] {"  2020-09-17 00:00:00 "}, 1);

    }
}

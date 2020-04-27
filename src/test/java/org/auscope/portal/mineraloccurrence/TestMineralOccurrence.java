package org.auscope.portal.mineraloccurrence;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.core.util.ResourceUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * User: Michael Stegherr Date: 30/03/2009 Time: 3:27:26 PM
 */
public class TestMineralOccurrence extends PortalTestClass {

    private static MineralOccurrence validMineralOccurrence;
    private static MineralOccurrence invalidMineralOccurrence;

    @BeforeClass
    public static void setUp() throws IOException, SAXException, XPathException, ParserConfigurationException {
        //create updateCSWRecords valid mineral occurrence
        Document mineralOccurrenceDocument = DOMUtil.buildDomFromStream(ResourceUtil
                .loadResourceAsStream("org/auscope/portal/erml/minocc/mineralOccurrenceNodeValid.xml"));
        XPathExpression expr = DOMUtil.compileXPathExpr("/er:MineralOccurrence",
                new MineralOccurrenceNamespaceContext());

        Node mineralOccurrenceNode = (Node) expr.evaluate(mineralOccurrenceDocument, XPathConstants.NODE);
        validMineralOccurrence = new MineralOccurrence(mineralOccurrenceNode);

        //create an invalid mineral occurrence
        Document mineralOccurrenceDocument2 = DOMUtil.buildDomFromStream(ResourceUtil
                .loadResourceAsStream("org/auscope/portal/erml/minocc/mineralOccurrenceNodeInvalid.xml"));
        XPathExpression expr2 = DOMUtil.compileXPathExpr("/er:MineralOccurrence",
                new MineralOccurrenceNamespaceContext());

        Node mineralOccurrenceNode2 = (Node) expr2.evaluate(mineralOccurrenceDocument2, XPathConstants.NODE);
        invalidMineralOccurrence = new MineralOccurrence(mineralOccurrenceNode2);
    }

    @Test
    public void testGetURNValid() throws XPathExpressionException {
        Assert.assertEquals("URN is: urn:cgi:feature:PIRSA:MineralOccurrence:394deposit",
                "urn:cgi:feature:PIRSA:MineralOccurrence:394deposit", validMineralOccurrence.getURN());
    }

    @Test
    public void testGetTypeValid() throws XPathExpressionException {
        Assert.assertEquals("Type is: ore deposit", "ore deposit", validMineralOccurrence.getType());
    }

    @Test
    public void testGetMineralDepositGroupValid() throws XPathExpressionException {
        Assert.assertEquals("Mineral deposit group is: Hydrothermal: precipitation of ore and gangue from " +
                "watery fluids of diverse origin, temperature range 50-7000C, generally below 4000C, " +
                "pressure 1-3 kbar",
                "Hydrothermal: precipitation of ore and gangue from watery fluids of diverse origin, " +
                        "temperature range 50-7000C, generally below 4000C, pressure 1-3 kbar",
                validMineralOccurrence.getMineralDepositGroup());
    }

    @Test
    public void testGetCommodityDescriptionURNsValid() {
        ArrayList<String> URNs = new ArrayList<String>();
        URNs.add("urn:cgi:feature:PIRSA:MineralCommodity:394deposit:Au");

        Assert.assertEquals(
                "Commodity Description URN is: urn:cgi:feature:PIRSA:MineralCommodity:394deposit:Au",
                URNs,
                validMineralOccurrence.getCommodityDescriptionURNs());
    }

    @Test
    public void testGetURNInvalid() throws XPathExpressionException {
        Assert.assertEquals("URN is: empty string", "", invalidMineralOccurrence.getURN());
    }

    @Test
    public void testGetTypeInvalid() throws XPathExpressionException {
        Assert.assertEquals("Type is: empty string", "", invalidMineralOccurrence.getType());
    }

    @Test
    public void testGetMineralDepositGroupInvalid() throws XPathExpressionException {
        Assert.assertEquals("",
                invalidMineralOccurrence.getMineralDepositGroup());
    }

    @Test
    public void testGetCommodityDescriptionURNsInvalid() {
        Assert.assertEquals(
                "Commodity Description URN is: an empty list",
                new ArrayList<String>(),
                invalidMineralOccurrence.getCommodityDescriptionURNs());
    }
}

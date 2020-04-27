package org.auscope.portal.mineraloccurrence;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.core.util.ResourceUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * User: Michael Stegherr Date: 30/03/2009 Time: 3:14:31 PM
 */
public class TestCommodity extends PortalTestClass {

    private static Commodity validCommodity;
    private static Commodity invalidCommodity;

    @Before
    public void setUp() throws IOException, SAXException, XPathException, ParserConfigurationException {
        //create updateCSWRecords valid commodity
        Document mineDocument = DOMUtil.buildDomFromStream(ResourceUtil
                .loadResourceAsStream("org/auscope/portal/erml/commodity/commodityNodeValid.xml"));

        XPathExpression expr = DOMUtil.compileXPathExpr("/er:Commodity", new MineralOccurrenceNamespaceContext());
        Node commodityNode = (Node) expr.evaluate(mineDocument, XPathConstants.NODE);
        validCommodity = new Commodity(commodityNode);

        //create an invalid commodity
        Document mineDocument2 = DOMUtil.buildDomFromStream(ResourceUtil
                .loadResourceAsStream("org/auscope/portal/erml/commodity/commodityNodeInvalid.xml"));

        XPathExpression expr2 = DOMUtil.compileXPathExpr("/er:Commodity", new MineralOccurrenceNamespaceContext());
        Node commodityNode2 = (Node) expr2.evaluate(mineDocument2, XPathConstants.NODE);
        invalidCommodity = new Commodity(commodityNode2);

    }

    @Test
    public void testGetCommodityNameValid() throws XPathExpressionException {
        Assert.assertEquals("Commodity name is: Gold", "Gold", validCommodity.getCommodityName());
    }

    @Test
    public void testGetSourcevalid() throws XPathExpressionException {
        Assert.assertEquals("URI is: urn:cgi:feature:GSV:MineralOccurrence:361169",
                "urn:cgi:feature:GSV:MineralOccurrence:361169", validCommodity.getSource());
    }

    @Test
    public void testGetCommodityImportanceValid() throws XPathExpressionException {
        Assert.assertEquals("Commodity importance is: major", "major", validCommodity.getCommodityImportance());
    }

    @Test
    public void testGetCommodityNameInvalid() throws XPathExpressionException {
        Assert.assertEquals("Commodity name is: empty string", "", invalidCommodity.getCommodityName());
    }

    @Test
    public void testGetSourceInvalid() throws XPathExpressionException {
        Assert.assertEquals("URI is: empty string", "", invalidCommodity.getSource());
    }

    @Test
    public void testGetCommodityImportanceInvalid() throws XPathExpressionException {
        Assert.assertEquals("Commodity importance is: empty string", "", invalidCommodity.getCommodityImportance());
    }
}

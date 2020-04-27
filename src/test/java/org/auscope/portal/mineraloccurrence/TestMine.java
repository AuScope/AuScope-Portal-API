package org.auscope.portal.mineraloccurrence;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.core.util.ResourceUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * ERML mine test harness.
 *
 * @version $Id$
 *
 *          User: Mathew Wyatt Date: 24/03/2009 Time: 9:01:48 AM
 */
public class TestMine extends PortalTestClass {
    /** The Document */
    private static final String MINEDOCUMENT = "org/auscope/portal/erml/mine/mineNode.xml";

    @Test
    public void testGetPrefferedName() throws XPathException, ParserConfigurationException,
            UnsupportedEncodingException, SAXException, IOException {
        Document mineDocument = DOMUtil.buildDomFromStream(ResourceUtil.loadResourceAsStream(MINEDOCUMENT));

        XPathExpression expr = DOMUtil.compileXPathExpr("/er:Mine", new MineralOccurrenceNamespaceContext());
        Node mineNode = (Node) expr.evaluate(mineDocument, XPathConstants.NODE);
        Mine mine = new Mine(mineNode);

        Assert.assertEquals("Preferred mine name is Good Hope", "Good Hope", mine.getMineNamePreffered());
    }

    @Test
    public void testGetURI() throws XPathException, ParserConfigurationException, IOException, SAXException,
            IOException {
        Document mineDocument = DOMUtil.buildDomFromStream(ResourceUtil.loadResourceAsStream(MINEDOCUMENT));
        XPathExpression expr = DOMUtil.compileXPathExpr("/er:Mine", new MineralOccurrenceNamespaceContext());

        Node mineNode = (Node) expr.evaluate(mineDocument, XPathConstants.NODE);
        Mine mine = new Mine(mineNode);

        Assert.assertEquals("URI should be urn:cgi:feature:GSV:Mine:361068", "urn:cgi:feature:GSV:Mine:361068",
                mine.getMineNameURI());
    }

}

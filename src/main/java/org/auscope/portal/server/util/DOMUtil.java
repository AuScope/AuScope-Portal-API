package org.auscope.portal.server.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.springframework.stereotype.Repository;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

/**
 * User: Mathew Wyatt
 * Date: 20/08/2009
 * Time: 4:42:13 PM
 */
@Repository
public class DOMUtil {
    public Document buildDomFromString(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        //build the XML dom
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlString.toString()));
        Document doc = builder.parse(inputSource);

        return doc;
    }
    
    /**
     * Given a DOM (sub)tree generate a string representation with no formatting
     * @param node The node to generate the XML for
     * @param omitXmlDeclaration Whether the <?xml?> header should be omitted
     * 
     * @return
     * @throws TransformerException
     */
    public String buildStringFromDom(Node node, boolean omitXmlDeclaration) throws TransformerException {
    	StringWriter outText = new StringWriter();
    	StreamResult sr = new StreamResult(outText);
    	Properties oprops = new Properties();
    	oprops.put(OutputKeys.METHOD, "xml");
    	if (omitXmlDeclaration) {
    		oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
    	} else {
    		oprops.put(OutputKeys.OMIT_XML_DECLARATION, "no");
    	}
    	TransformerFactory tf = TransformerFactory.newInstance();
    	
    	Transformer t = tf.newTransformer();
		t.setOutputProperties(oprops);
		t.transform(new DOMSource(node),sr);
		
		return outText.toString();
    }
}

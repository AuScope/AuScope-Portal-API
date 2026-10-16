package org.auscope.portal.core.services.responses.wmts;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Constructs a WMTS layer from the XML response
 */
public class GetCapabilitiesWMTSLayer_1_0_0 implements GetCapabilitiesWMTSLayerRecord {

    private final Log log = LogFactory.getLog(getClass());

    private String identifier = "";
    private String title = "";
    private String abstractText = "";

    private List<String> formats = new ArrayList<>();
    private String tileTemplate = "";
    private List<String> tileMatrixSetLinks = new ArrayList<>();

    private String defaultStyle = "";
    private List<String> styles = new ArrayList<>();

    public GetCapabilitiesWMTSLayer_1_0_0(Node layerNode) {
        this.identifier = getChildValue(layerNode, "Identifier");
        this.title = getChildValue(layerNode, "Title");
        this.abstractText = getChildValue(layerNode, "Abstract");

        this.formats = getFormats(layerNode);
        this.tileTemplate = getTileTemplate(layerNode);
        this.tileMatrixSetLinks = getTileMatrixSetLinks(layerNode);

        this.styles = getStyles(layerNode);
        this.defaultStyle = extractDefaultStyle(layerNode);
    }

    private String getChildValue(Node parent, String name) {
        try {
            Node node = (Node) DOMUtil.compileXPathExpr("*[local-name()='" + name + "']").evaluate(parent,
                    XPathConstants.NODE);

            if (node != null) {
                return node.getTextContent();
            }
        } catch (XPathException e) {
            log.error("WMTS layer child value parsing error: " + e.getMessage());
        }
        return "";
    }

    private List<String> getFormats(Node layerNode) {
        List<String> result = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr("*[local-name()='Format']").evaluate(layerNode,
                    XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getTextContent());
            }
        } catch (XPathException e) {
            log.error("WMTS layer formats parsing error: " + e.getMessage());
        }
        return result;
    }

    private String getTileTemplate(Node layerNode) {
        try {
            Node node = (Node) DOMUtil
                    .compileXPathExpr("*[local-name()='ResourceURL' and @resourceType='tile']/@template")
                    .evaluate(layerNode, XPathConstants.NODE);
            if (node != null) {
                return node.getNodeValue();
            }
        } catch (XPathException e) {
            log.error("WMTS tile template parsing error: " + e.getMessage());
        }
        return "";
    }

    private List<String> getTileMatrixSetLinks(Node layerNode) {
        List<String> result = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) DOMUtil
                    .compileXPathExpr("*[local-name()='TileMatrixSetLink']/*[local-name()='TileMatrixSet']")
                    .evaluate(layerNode, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getTextContent());
            }
        } catch (XPathException e) {
            log.error("WMTS TileMatrixSetLink parsing error: " + e.getMessage());
        }
        return result;
    }

    private List<String> getStyles(Node layerNode) {
        List<String> result = new ArrayList<>();

        try {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr("*[local-name()='Style']/*[local-name()='Identifier']")
                    .evaluate(layerNode, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getTextContent());
            }

        } catch (XPathException e) {
            log.error("WMTS styles parsing error: " + e.getMessage());
        }

        return result;
    }

    private String extractDefaultStyle(Node layerNode) {
        try {
            Node node = (Node) DOMUtil
                    .compileXPathExpr("*[local-name()='Style'][@isDefault='true']/*[local-name()='Identifier']")
                    .evaluate(layerNode, XPathConstants.NODE);

            if (node != null) {
                return node.getTextContent();
            }

        } catch (XPathException e) {
            log.error("WMTS default style parsing error: " + e.getMessage());
        }
        return "";
    }

    @Override
    public String toString() {
        return "WMTS Layer [identifier=" + identifier + ", title=" + title + "]";
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getAbstract() {
        return abstractText;
    }

    @Override
    public List<String> getFormats() {
        return formats;
    }

    @Override
    public String getTileTemplate() {
        return tileTemplate;
    }

    @Override
    public List<String> getTileMatrixSetLinks() {
        return tileMatrixSetLinks;
    }

    @Override
    public String getDefaultStyle() {
        return defaultStyle;
    }

    @Override
    public List<String> getStyles() {
        return styles;
    }
}
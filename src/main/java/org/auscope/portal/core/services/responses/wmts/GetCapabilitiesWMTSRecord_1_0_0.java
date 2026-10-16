package org.auscope.portal.core.services.responses.wmts;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class for constructing WMTS GetCapabilities records from input streams
 */
public class GetCapabilitiesWMTSRecord_1_0_0 implements GetCapabilitiesWMTSRecord {

    private final Log log = LogFactory.getLog(getClass());

    private String serviceType = "WMTS";
    private String version = "1.0.0";
    private String organisation = "";
    private String getTileUrl = "";
    private String applicationProfile = "";

    private List<GetCapabilitiesWMTSLayerRecord> layers = new ArrayList<>();
    private List<TileMatrixSet> tileMatrixSets = new ArrayList<>();

    private List<String> formats = new ArrayList<>();
    private List<String> infoFormats = new ArrayList<>();
    private String serviceMetadataUrl = "";

    private String[] accessConstraints = new String[] {};

    public GetCapabilitiesWMTSRecord_1_0_0(InputStream inXml)
            throws SAXException, IOException, ParserConfigurationException {
        try {
            Document doc = DOMUtil.buildDomFromStream(inXml);
            this.organisation = getOrganisation(doc);
            this.getTileUrl = getGetTileUrl(doc);
            this.layers = getWMTSLayers(doc);
            this.tileMatrixSets = getTileMatrixSets(doc);
            this.accessConstraints = getAccessConstraints(doc);
            this.formats = extractFormats(doc);
            this.infoFormats = extractInfoFormats(doc);
            this.serviceMetadataUrl = getServiceMetadataUrl(doc);
        } catch (SAXException e) {
            log.error("Parsing error: " + e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("IO error: " + e.getMessage());
            throw e;
        } catch (ParserConfigurationException e) {
            log.error("Parser Config Error: " + e.getMessage());
            throw e;
        }
    }

    private String getOrganisation(Document doc) {
        try {
            Node node = (Node) DOMUtil.compileXPathExpr("//*[local-name()='ProviderName']").evaluate(doc,
                    XPathConstants.NODE);

            if (node != null) {
                return node.getTextContent();
            }

        } catch (XPathException e) {
            log.error("WMTS organisation parsing error: " + e.getMessage());
        }
        return "";
    }

    private String getGetTileUrl(Document doc) {
        try {
            Node node = (Node) DOMUtil.compileXPathExpr("//*[local-name()='Operation' and @name='GetTile']"
                    + "//*[local-name()='Get']/@*[local-name()='href']").evaluate(doc, XPathConstants.NODE);

            if (node != null) {
                return node.getNodeValue();
            }

        } catch (XPathException e) {
            log.error("WMTS GetTile URL parsing error: " + e.getMessage());
        }
        return "";
    }

    private List<GetCapabilitiesWMTSLayerRecord> getWMTSLayers(Document doc) {
        List<GetCapabilitiesWMTSLayerRecord> layerList = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr("//*[local-name()='Contents']/*[local-name()='Layer']")
                    .evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                GetCapabilitiesWMTSLayer_1_0_0 layer = new GetCapabilitiesWMTSLayer_1_0_0(nodes.item(i));
                layerList.add(layer);
            }
        } catch (XPathException e) {
            log.error("WMTS layer parsing error: " + e.getMessage());
        }
        return layerList;
    }

    private List<TileMatrixSet> getTileMatrixSets(Document doc) {
        List<TileMatrixSet> sets = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr("//*[local-name()='TileMatrixSet']").evaluate(doc,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node tmsNode = nodes.item(i);

                TileMatrixSet set = new TileMatrixSet();

                set.setIdentifier(getChildValue(tmsNode, "Identifier"));
                set.setSupportedCRS(getChildValue(tmsNode, "SupportedCRS"));
                set.setTileMatrices(getTileMatrices(tmsNode));

                sets.add(set);
            }

        } catch (XPathException e) {
            log.error("WMTS TileMatrixSet parsing error: " + e.getMessage());
        }
        return sets;
    }

    private List<TileMatrix> getTileMatrices(Node tmsNode) {
        List<TileMatrix> matrices = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr("*[local-name()='TileMatrix']").evaluate(tmsNode,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node tmNode = nodes.item(i);

                TileMatrix tm = new TileMatrix();

                tm.setIdentifier(getChildValue(tmNode, "Identifier"));
                tm.setScaleDenominator(parseDouble(tmNode, "ScaleDenominator"));
                tm.setTileWidth(parseInt(tmNode, "TileWidth"));
                tm.setTileHeight(parseInt(tmNode, "TileHeight"));
                tm.setMatrixWidth(parseInt(tmNode, "MatrixWidth"));
                tm.setMatrixHeight(parseInt(tmNode, "MatrixHeight"));

                matrices.add(tm);
            }
        } catch (XPathException e) {
            log.error("WMTS TileMatrix parsing error: " + e.getMessage());
        }
        return matrices;
    }

    private String[] getAccessConstraints(Document doc) {
        List<String> constraints = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr("//*[local-name()='AccessConstraints']").evaluate(doc,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                constraints.add(nodes.item(i).getTextContent());
            }
        } catch (XPathException e) {
            log.error("WMTS access constraints parsing error: " + e.getMessage());
        }
        return constraints.toArray(new String[0]);
    }

    private List<String> extractFormats(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) DOMUtil.compileXPathExpr("//*[local-name()='Layer']/*[local-name()='Format']")
                    .evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getTextContent());
            }
        } catch (XPathException e) {
            log.error("WMTS formats parsing error: " + e.getMessage());
        }
        return result;
    }

    private List<String> extractInfoFormats(Document doc) {
        List<String> result = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) DOMUtil
                    .compileXPathExpr("//*[local-name()='Layer']/*[local-name()='InfoFormat']")
                    .evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getTextContent());
            }
        } catch (XPathException e) {
            log.error("WMTS info formats parsing error: " + e.getMessage());
        }
        return result;
    }

    private String getServiceMetadataUrl(Document doc) {
        try {
            Node node = (Node) DOMUtil.compileXPathExpr("//*[local-name()='ServiceMetadata']").evaluate(doc,
                    XPathConstants.NODE);

            if (node != null) {
                return node.getTextContent();
            }
        } catch (XPathException e) {
            log.error("WMTS metadata URL parsing error: " + e.getMessage());
        }
        return "";
    }

    private String getChildValue(Node parent, String name) {
        try {
            Node node = (Node) DOMUtil.compileXPathExpr("*[local-name()='" + name + "']").evaluate(parent,
                    XPathConstants.NODE);

            if (node != null) {
                return node.getTextContent();
            }
        } catch (XPathException e) {
            log.error("WMTS child value error: " + e.getMessage());
        }
        return "";
    }

    private int parseInt(Node node, String name) {
        try {
            String val = getChildValue(node, name);
            return val.isEmpty() ? 0 : Integer.parseInt(val);
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(Node node, String name) {
        try {
            String val = getChildValue(node, name);
            return val.isEmpty() ? 0.0 : Double.parseDouble(val);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Locate a specific tile matrix set
     * 
     * @param identifier identifier of set
     * @return the requested tile matrix set if it exists or null
     */
    public TileMatrixSet findTileMatrixSet(String identifier) {
        if (identifier == null) {
            return null;
        }
        for (TileMatrixSet tileMatrixSet : tileMatrixSets) {
            if (identifier.equals(tileMatrixSet.getIdentifier())) {
                return tileMatrixSet;
            }
        }
        return null;
    }

    @Override
    public boolean isWMTS() {
        return true;
    }

    @Override
    public String getServiceType() {
        return serviceType;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getOrganisation() {
        return organisation;
    }

    @Override
    public String getGetTileUrl() {
        return getTileUrl;
    }

    @Override
    public List<GetCapabilitiesWMTSLayerRecord> getLayers() {
        return layers;
    }

    public List<TileMatrixSet> getTileMatrixSets() {
        return tileMatrixSets;
    }

    @Override
    public String[] getAccessConstraints() {
        return accessConstraints;
    }

    @Override
    public String getApplicationProfile() {
        return applicationProfile;
    }

    @Override
    public List<String> getFormats() {
        return formats;
    }

    @Override
    public List<String> getInfoFormats() {
        return infoFormats;
    }

    @Override
    public String getServiceMetadataUrl() {
        return serviceMetadataUrl;
    }

}
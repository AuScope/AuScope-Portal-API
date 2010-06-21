package org.auscope.portal.server.gridjob;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.sf.json.JSONObject;

/**
 * This class represents a url pointer to an input file to the grid (along with associated metadata)
 * @author VOT002
 *
 */
public class GeodesyGridInputFile implements Serializable {
	/**
	 * The 4 character ID of the station that generated this input file
	 */
	private String stationId;
	/**
	 * The remote URL where the actual input file is stored
	 */
	private String fileUrl;
	/**
	 * Whether this input file has been selected
	 */
	private boolean selected;
	/**
	 * The date at which this input file was generated
	 */
	private String fileDate;
	
	public GeodesyGridInputFile() {
		
	}
	
	public GeodesyGridInputFile(String stationId, String fileUrl,
			boolean selected, String fileDate) {
		this.stationId = stationId;
		this.fileUrl = fileUrl;
		this.selected = selected;
		this.fileDate = fileDate;
	}
	
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public String getFileDate() {
		return fileDate;
	}
	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}
	
	public static GeodesyGridInputFile fromJSONObject(JSONObject obj) {
		return new GeodesyGridInputFile(
				(String)obj.get("stationId"),
				(String)obj.get("fileUrl"),
				(Boolean)obj.get("selected"),
				(String)obj.get("fileDate")
				);
	}
	
	public static List<GeodesyGridInputFile> fromGmlString(String gmlString) throws Exception {
		//Parse our XML string into a document
		List<GeodesyGridInputFile> result = new ArrayList<GeodesyGridInputFile>();
		XPath xPath = XPathFactory.newInstance().newXPath();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document responseDocument = builder.parse(new InputSource(new StringReader(gmlString)));
		
        //Extract the URL list and parse it into the JSON list
        String featureMemberExpression = "/FeatureCollection/featureMember";
        NodeList nodes = (NodeList) xPath.evaluate(featureMemberExpression, responseDocument, XPathConstants.NODESET);
        if (nodes != null) {
            for(int i=0; i < nodes.getLength(); i++ ) {
            	GeodesyGridInputFile ggif = new GeodesyGridInputFile();
            	
            	Node tempNode = (Node) xPath.evaluate("station_observations/url", nodes.item(i), XPathConstants.NODE);
            	ggif.setFileUrl(tempNode == null ? "" : tempNode.getTextContent());
            	
            	tempNode = (Node) xPath.evaluate("station_observations/date", nodes.item(i), XPathConstants.NODE);
            	ggif.setFileDate(tempNode == null ? "" : tempNode.getTextContent());
            	
            	tempNode = (Node) xPath.evaluate("station_observations/id", nodes.item(i), XPathConstants.NODE);
            	ggif.setStationId(tempNode == null ? "" : tempNode.getTextContent());
            	
            	ggif.setSelected(true);
            	
            	result.add(ggif);
            }
        }
        
        return result;
	}
}

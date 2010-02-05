package org.auscope.portal.server.gridjob;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * User: Jacqueline Githaiga
 * Date: 13/01/2010
 * Time: 11:58:21 AM
 */

/**
	This class gets a sample GeoNetwork Record XML file
	as input, sets the various elements of the XML file 
	and saves the file.
  */
public class PrepareCSWTransactionRecord {		

	private static final long serialVersionUID = 1L;
	private static final Log logger = LogFactory
	.getLog(PrepareCSWTransactionRecord.class);

	/* Parse the XML file */
	private Document doc = null;
	private GeodesyRecordInfo info = null;
	private boolean recordLoaded = true; 

	public PrepareCSWTransactionRecord(GeodesyRecordInfo info, InputStream file) throws ParserConfigurationException,
	SAXException, IOException {
		
		this.info = info;
		if(info != null)
		{
			try{
				/* Get the XML File */
				//String fileName = "/WEB-INF/xml/GNRecord.xml";
				//logger.debug(fileName);
				//File file = new File(fileName);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(file);
				/* Normalise the text representation */
				doc.getDocumentElement().normalize();
				addGeodesyInfo();
			}catch(Exception e){
				logger.error("Preparing CSWTRecord Failed:"+e.getMessage());
				recordLoaded = false; 
			}
		}
		else
		{
			logger.error("Preparing CSWTRecord Failed: GeodesyRecordInfo is null");
			recordLoaded = false;
		}
	}
	
	public String createGNRecord(){
		try{
		      //Serialize DOM
		      OutputFormat format    = new OutputFormat (doc); 
		      // as a String
		      StringWriter stringOut = new StringWriter ();    
		      XMLSerializer serial   = new XMLSerializer (stringOut, 
		                                                  format);
		      serial.serialize(doc);

	        //StringWriter stw = new StringWriter();
	        //Transformer serializer = TransformerFactory.newInstance().newTransformer();
	        //serializer.transform(new DOMSource(doc), new StreamResult(stw));
	        return stringOut.toString(); 
		}catch(Exception e){
			logger.error("Converting CSWTRecord XML Failed:"+e.getMessage());
			return null;
		}
	}
	
	public boolean isRecordLoaded(){
		return recordLoaded;
	}
	
	private void addGeodesyInfo(){
		logger.info("Adding GeodesyInfo to the CSWT Record:");
		try{
			setJobSeriesName();
			setDate();
			setJobDescription();
			setCreatorName();
			setCreatorIdp();
			setCreatorEmail();
			setKeywords();
			setWestBoundLongitude();
			setEastBoundLongitude();
			setSouthBoundLongitude();
			setNorthBoundLongitude();
			setSupplementalInformation();
			setRecordURL();
			setResultsForSeries();
		}catch(Exception e){
			logger.error("Setting some of the values of GN record failed. "+e.getMessage());
			recordLoaded = false;
		}		
	}

	private void setJobSeriesName() {

		NodeList titleNode = doc.getElementsByTagName("gmd:title");

		for (int i = 0; i < titleNode.getLength(); i++) {

			Node tNode = titleNode.item(i);

			if (tNode.getNodeType() == Node.ELEMENT_NODE) {

				Element tElmnt = (Element) tNode;
				NodeList tNmElmntLst = tElmnt
				.getElementsByTagName("gco:CharacterString");
				Node n1 = tNmElmntLst.item(0).getFirstChild();
				n1.setNodeValue(info.getRecordName());
				logger.info("Series Name: " + n1.getNodeValue());
			}
		}
	}

	private void setDate() {
		NodeList dateNode = doc.getElementsByTagName("gmd:date");

		for (int i = 0; i < dateNode.getLength(); i++) {

			Node dNode = dateNode.item(i);

			if (dNode.getNodeType() == Node.ELEMENT_NODE) {

				Element dElmnt = (Element) dNode;
				NodeList dNmElmntLst = dElmnt
				.getElementsByTagName("gco:DateTime");
				Node n1 = dNmElmntLst.item(0).getFirstChild();
				n1.setNodeValue(info.getRecordDateTime());
				logger.info("Date Created: " + n1.getNodeValue());
			}
		}
	}

	private void setJobDescription() {
		NodeList jobNode = doc.getElementsByTagName("gmd:abstract");

		for (int i = 0; i < jobNode.getLength(); i++) {

			Node jNode = jobNode.item(i);

			if (jNode.getNodeType() == Node.ELEMENT_NODE) {

				Element dElmnt = (Element) jNode;
				NodeList dNmElmntLst = dElmnt
				.getElementsByTagName("gco:CharacterString");
				Node n1 = dNmElmntLst.item(0).getFirstChild();
				n1.setNodeValue(info.getRecordDesc());
				logger.info("Job Description: " + n1.getNodeValue());
			}
		}
	}
	private void setCreatorName() {
		NodeList creatorNode = doc.getElementsByTagName("gmd:individualName");

		for (int s = 0; s < creatorNode.getLength(); s++) {

			Node fstNode = creatorNode.item(s);

			if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

				Element fstElmnt = (Element) fstNode;
				NodeList fstNmElmntLst = fstElmnt
				.getElementsByTagName("gco:CharacterString");
				Node n1 = fstNmElmntLst.item(0).getFirstChild();
				n1.setNodeValue(info.getCreatorName());
				logger.info("Creator Name: " + n1.getNodeValue());
			}
		}
	}

	private void setCreatorIdp() {

		NodeList creatorIdpNode = doc
		.getElementsByTagName("gmd:organisationName");

		for (int t = 0; t < creatorIdpNode.getLength(); t++) {

			Node scdNode = creatorIdpNode.item(t);

			if (scdNode.getNodeType() == Node.ELEMENT_NODE) {

				Element scdElmnt = (Element) scdNode;
				NodeList scdNmElmntLst = scdElmnt
				.getElementsByTagName("gco:CharacterString");
				Node n2 = scdNmElmntLst.item(0).getFirstChild();
				n2.setNodeValue(info.getCreatorIdp());
				logger.info("Creator Idp: " + n2.getNodeValue());
			}
		}
	}
	
	private void setCreatorEmail() {

		NodeList emailNode = doc
		.getElementsByTagName("gmd:electronicMailAddress");

		for (int i = 0; i < emailNode.getLength(); i++) {

			Node eNode = emailNode.item(i);

			if (eNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElmnt = (Element) eNode;
				NodeList eElmntLst = eElmnt
				.getElementsByTagName("gco:CharacterString");
				Node n2 = eElmntLst.item(0).getFirstChild();

				n2.setNodeValue(info.getCreatorEmail());
				logger.info("Creator email: " + n2.getNodeValue());
			}
		}
	}


	private void setKeywords() {

		NodeList keywordsNode = doc
		.getElementsByTagName("gmd:keyword");

			Node kNode = keywordsNode.item(0);

			if (kNode.getNodeType() == Node.ELEMENT_NODE) {
				
				Element kElmnt = (Element) kNode;
				NodeList kElmntLst = kElmnt
				.getElementsByTagName("gco:CharacterString");
				Node n2 = kElmntLst.item(0).getFirstChild();
				String sampleKeyword = n2.getNodeValue();

				logger.info("Keyword 1 : " + sampleKeyword);
				n2.setNodeValue("Geodesy");
				String setKeyword = n2.getNodeValue();
				logger.info("Keyword 1 Changed : " + setKeyword);
			}
				Node k1Node = keywordsNode.item(1);

				if (k1Node.getNodeType() == Node.ELEMENT_NODE) {
					
					Element kElmnt = (Element) k1Node;
					NodeList kElmntLst = kElmnt
					.getElementsByTagName("gco:CharacterString");
					Node n2 = kElmntLst.item(0).getFirstChild();
					String sampleKeyword = n2.getNodeValue();

					logger.info("Keyword 2 : " + sampleKeyword);
					n2.setNodeValue("Alice Springs");
					String setKeyword = n2.getNodeValue();
					logger.info("Keyword 2 Changed : " + setKeyword);
				}
				
				Node k2Node = keywordsNode.item(2);

				if (k2Node.getNodeType() == Node.ELEMENT_NODE) {
					
					Element kElmnt = (Element) k2Node;
					NodeList kElmntLst = kElmnt
					.getElementsByTagName("gco:CharacterString");
					Node n2 = kElmntLst.item(0).getFirstChild();
					String sampleKeyword = n2.getNodeValue();

					logger.info("Keyword 3 : " + sampleKeyword);
					n2.setNodeValue("GPS (Again..)");
					String setKeyword = n2.getNodeValue();
					logger.info("Keyword 3 Changed : " + setKeyword);
				}

							
		}
	
	
	
	private void setWestBoundLongitude()
	{
		NodeList westNode = doc
		.getElementsByTagName("gmd:westBoundLongitude");

		for (int i = 0; i < westNode.getLength(); i++) {

			Node wNode = westNode.item(i);

			if (wNode.getNodeType() == Node.ELEMENT_NODE) {

				Element wElmnt = (Element) wNode;
				NodeList wElmntLst = wElmnt
				.getElementsByTagName("gco:Decimal");
				Node n2 = wElmntLst.item(0).getFirstChild();

				n2.setNodeValue(String.valueOf(info.getWestBoundLongitude()));
				logger.info("West Bound Longitude: " + n2.getNodeValue());
			}
		}
	}
	
	private void setEastBoundLongitude()
	{
		NodeList eastNode = doc
		.getElementsByTagName("gmd:eastBoundLongitude");

		for (int i = 0; i < eastNode.getLength(); i++) {

			Node eNode = eastNode.item(i);

			if (eNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElmnt = (Element) eNode;
				NodeList eElmntLst = eElmnt
				.getElementsByTagName("gco:Decimal");
				Node n2 = eElmntLst.item(0).getFirstChild();
				n2.setNodeValue(String.valueOf(info.getEastBoundLongitude()));
				logger.info("East Bound Longitude: " + n2.getNodeValue());
			}
		}
	}
	
	private void setSouthBoundLongitude()
	{
		NodeList southNode = doc
		.getElementsByTagName("gmd:southBoundLatitude");

		for (int i = 0; i < southNode.getLength(); i++) {

			Node sNode = southNode.item(i);

			if (sNode.getNodeType() == Node.ELEMENT_NODE) {

				Element sElmnt = (Element) sNode;
				NodeList sElmntLst = sElmnt
				.getElementsByTagName("gco:Decimal");
				Node n2 = sElmntLst.item(0).getFirstChild();
				n2.setNodeValue(String.valueOf(info.getSouthBoundLatitude()));
				logger.info("South Bound Latitude: " + n2.getNodeValue());
			}
		}
	}
	
	private void setNorthBoundLongitude()
	{
		NodeList northNode = doc
		.getElementsByTagName("gmd:northBoundLatitude");

		for (int i = 0; i < northNode.getLength(); i++) {

			Node nNode = northNode.item(i);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element nElmnt = (Element) nNode;
				NodeList nElmntLst = nElmnt
				.getElementsByTagName("gco:Decimal");
				Node n2 = nElmntLst.item(0).getFirstChild();
				n2.setNodeValue(String.valueOf(info.getNorthBoundLatitude()));
				logger.info("North Bound Latitude Changed : " + n2.getNodeValue());
			}
		}
	}
	
	private void setSupplementalInformation()
	{
		NodeList suppNode = doc
		.getElementsByTagName("gmd:supplementalInformation");

		for (int i = 0; i < suppNode.getLength(); i++) {

			Node supNode = suppNode.item(i);

			if (supNode.getNodeType() == Node.ELEMENT_NODE) {

				Element supElmnt = (Element) supNode;
				NodeList supElmntLst = supElmnt
				.getElementsByTagName("gco:CharacterString");
				Node n2 = supElmntLst.item(0).getFirstChild();
				n2.setNodeValue(info.getSupplementalInformation());
				logger.info("Supplemental Information: " + n2.getNodeValue());
			}
		}
	}
	
	private void setRecordURL()
	{
		NodeList urlNode = doc
		.getElementsByTagName("gmd:linkage");

			Node uNode = urlNode.item(0);
			if (uNode.getNodeType() == Node.ELEMENT_NODE) {

				Element uElmnt = (Element) uNode;
				NodeList uElmntLst = uElmnt
				.getElementsByTagName("gmd:URL");
				Node n2 = uElmntLst.item(0).getFirstChild();
				n2.setNodeValue(info.getRecordUrl());
				logger.info("Record URL: " + n2.getNodeValue());
			}
		
	}

	private void setResultsForSeries()
	{
		NodeList resultsNode = doc
		.getElementsByTagName("gmd:description");

			Node rNode = resultsNode.item(0);
			if (rNode.getNodeType() == Node.ELEMENT_NODE) {

				Element rElmnt = (Element) rNode;
				NodeList rElmntLst = rElmnt
				.getElementsByTagName("gco:CharacterString");
				Node n2 = rElmntLst.item(0).getFirstChild();
				String sampleResults = n2.getNodeValue();

				logger.info("Results : " + sampleResults);
				n2.setNodeValue("Results");
				String setResults = n2.getNodeValue();
				logger.info("Results Changed : " + setResults);
			}
		
	}

	public void writeGNRecordToFile() throws FileNotFoundException {
		try {
			// Created DOMSource from Document
			DOMSource source = new DOMSource(doc);
			OutputStream outStream = new FileOutputStream("c://input.xml");
			StreamResult result = new StreamResult(outStream);

			Transformer xformer = TransformerFactory.newInstance()
			.newTransformer();
			logger.info("Written to new file");
			xformer.transform(source, result);
			outStream.flush();
			outStream.close();
		}

		catch (Exception e) {
			logger.error(e);
		}

	}
	

	
	
	
	/*public static void main(String argv[]) throws ParserConfigurationException, SAXException, IOException {
		
		PrepareCSWTransactionRecord grinfo = new PrepareCSWTransactionRecord();
		grinfo.setCreatorIdp();
		grinfo.setCreatorName();
		grinfo.setDate();
		grinfo.setJobDescription();
		grinfo.setJobSeriesName();
		grinfo.setCreatorEmail();
		grinfo.setKeywords();
		grinfo.setWestBoundLongitude();
		grinfo.setSouthBoundLongitude();
		grinfo.setNorthBoundLongitude();
		grinfo.setEastBoundLongitude();
		grinfo.setSupplementalInformation();
		grinfo .setRecordURL();
		grinfo.setResultsForSeries();
		grinfo.createGNRecord();		
		
	}*/
}


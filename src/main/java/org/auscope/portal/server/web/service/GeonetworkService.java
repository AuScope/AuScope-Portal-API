package org.auscope.portal.server.web.service;

import java.io.InputStream;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWNamespaceContext;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.CSWRecordTransformer;
import org.auscope.portal.csw.CSWXPathUtil;
import org.auscope.portal.server.domain.xml.XMLStreamAttributeExtractor;
import org.auscope.portal.server.util.DOMUtil;
import org.auscope.portal.server.web.GeonetworkDetails;
import org.auscope.portal.server.web.GeonetworkMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A service class that provides high level interactions with Geonetwork
 * @author Josh Vote
 *
 */
@Service
public class GeonetworkService {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private HttpServiceCaller serviceCaller;
	private GeonetworkMethodMaker gnMethodMaker;
    private GeonetworkDetails gnDetails;
    
    private static final XPathExpression getMetadataXML_GetID;
    
    static {
    	getMetadataXML_GetID = CSWXPathUtil.attemptCompileXpathExpr("/gmd:MD_Metadata/geonet:info/id");
    }
    
    @Autowired
	public GeonetworkService(HttpServiceCaller serviceCaller,
			GeonetworkMethodMaker gnMethodMaker, GeonetworkDetails gnDetails) {
		super();
		this.serviceCaller = serviceCaller;
		this.gnMethodMaker = gnMethodMaker;
		this.gnDetails = gnDetails;
	}
    
    /**
     * Helper method for transforming an arbitrary CSWRecord into a
     * <gmd:MD_Metadata> representation
     * @param record
     * @return
     * @throws Exception
     */
    private String cswRecordToMDMetadataXml(CSWRecord record) throws Exception {
    	DOMUtil util = new DOMUtil();
    	
    	CSWRecordTransformer transformer = new CSWRecordTransformer(); //more than meets the eye
		Node mdMetadataNode = transformer.transformToNode(record);
		return util.buildStringFromDom(mdMetadataNode, true);
    }
    
    /**
     * Returns the record id from the response of an insert operation (or empty string if N/A)
     * @param gnResponse A response from a CSWInsert operation
     * @return
     */
    private String extractUuid(String gnResponse) {
    	String rtnValue = "";
    	if(gnResponse != null && !gnResponse.isEmpty()){
    		DOMUtil util = new DOMUtil();
    		try{
    			Document doc = util.buildDomFromString(gnResponse);

    			NodeList insertNode = doc.getElementsByTagName("csw:totalInserted");
    			Node n1 = insertNode.item(0).getFirstChild();
    			if(n1.getNodeValue().equals("1")){
    				NodeList idNode = doc.getElementsByTagName("identifier");
    				Node n2 = idNode.item(0).getFirstChild();
    				rtnValue = n2.getNodeValue();
    				logger.debug("Insert response id: "+rtnValue);
    			}
    		} catch(Exception e) {
        		logger.warn("Unable to parse a geonetwork response", e); 			
    		}    		
    	}
    	return rtnValue;
    }
    
    /**
     * Attempts to query geonetwork for a record with the specified UUID. If succesful the 
     * underlying "record id" (not uuid) is returned which is used by certain operations in this
     * service
     * 
     * @param uuid
     * @return
     */
    private String convertUUIDToRecordID(String uuid, String sessionCookie) throws Exception {    	
    	HttpMethodBase metadataInfoMethod = gnMethodMaker.makeRecordMetadataGetMethod(gnDetails.getUrl(), uuid, sessionCookie);
		String responseString = serviceCaller.getMethodResponseAsString(metadataInfoMethod, serviceCaller.getHttpClient());
		Document responseDoc = (new DOMUtil()).buildDomFromString(responseString);

		Node idNode = (Node) getMetadataXML_GetID.evaluate(responseDoc, XPathConstants.NODE);
		if (idNode == null) {
			throw new Exception("Response does not contain geonetwork info about record's internal ID");
		}
		String recordId = idNode.getTextContent();
		
		logger.debug(String.format("converted uuid='%1$s' to recordId='%2$s", uuid, recordId));
		
		return recordId;
    }
    
    /**
     * Attempts to insert the specified CSWRecord into Geonetwork. The record will be made publicly
     * viewable.
     * 
     * If successful the URL of the newly created record will be returned
     * @param record
     * @return
     * @throws Exception
     */
    public String makeCSWRecordInsertion(CSWRecord record) throws Exception {
		String mdMetadataXml = cswRecordToMDMetadataXml(record);
    	String gnResponse = null;
		
		//Login and extract our cookies (this will be our session id)
		HttpMethodBase methodLogin = gnMethodMaker.makeUserLoginMethod(gnDetails.getUrl(), gnDetails.getUser(), gnDetails.getPassword());
		gnResponse = serviceCaller.getMethodResponseAsString(methodLogin, serviceCaller.getHttpClient());
		logger.debug(String.format("GN Login response: %1$s", gnResponse));
		if (!gnResponse.contains("<ok />")) {
			throw new Exception("Geonetwork login failed");
		}
		
		String sessionCookie = methodLogin.getResponseHeader("Set-Cookie").getValue();
		
		//Insert our record
		HttpMethodBase methodInsertRecord = gnMethodMaker.makeInsertRecordMethod(gnDetails.getUrl(), mdMetadataXml, sessionCookie);
		gnResponse = serviceCaller.getMethodResponseAsString(methodInsertRecord, serviceCaller.getHttpClient());
		logger.debug(String.format("GN Insert response: %1$s", gnResponse));
		
		//Extract our uuid and convert it to a recordId for usage in the next step
		String uuid = extractUuid(gnResponse);
		if (uuid == null || uuid.isEmpty()) {
			throw new Exception("Unable to extract uuid");
		}
		String recordId = convertUUIDToRecordID(uuid, sessionCookie);

		//Use our new record ID to FINALLY set the record to public 
		HttpMethodBase methodSetPublic = gnMethodMaker.makeRecordPublicMethod(gnDetails.getUrl(), recordId, sessionCookie);
		gnResponse = serviceCaller.getMethodResponseAsString(methodSetPublic, serviceCaller.getHttpClient());
		logger.debug(String.format("GN setting record %1$s (uuid=%2$s) public returned: %3$s", recordId, uuid , gnResponse));
		
		//Logout (just in case)
		HttpMethodBase methodLogout = gnMethodMaker.makeUserLogoutMethod(gnDetails.getUrl(), sessionCookie);
		gnResponse = serviceCaller.getMethodResponseAsString(methodLogout, serviceCaller.getHttpClient());
		logger.debug(String.format("GN Logout response: %1$s", gnResponse));
		
		//Finally get the URL to access the record's page
		HttpMethodBase metadataInfoMethod = gnMethodMaker.makeRecordMetadataShowMethod(gnDetails.getUrl(), uuid, sessionCookie);
		return metadataInfoMethod.getURI().toString();
    }
}

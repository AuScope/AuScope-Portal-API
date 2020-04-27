package org.auscope.portal.server.web.controllers;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * gsmlp namespace lookup table for the SF0 Borehole
 *
 * @author Lingbo Jiang 
 *
 */

public class GsmlpNameSpaceTable {
    protected HttpServiceCaller httpServiceCaller;
    protected WFSGetFeatureMethodMaker wfsMethodMaker;    
    private ConcurrentMap <String, String> gsmlpNameSpaceCache; 
    private final Log log = LogFactory.getLog(getClass());  
    /**
     * Constructor Construct all the member variables.
     * 
     */    
    public GsmlpNameSpaceTable() {
        httpServiceCaller = new HttpServiceCaller(9000);
        wfsMethodMaker = new WFSGetFeatureMethodMaker();
        gsmlpNameSpaceCache = new ConcurrentHashMap<String, String>();
    }
    /**
     * Get gsmlp namespace based on the serviceUrl
     * 
     * @param serviceUrl
     *            The serviceUrl
     * @return string of gsmlp namespace
     */    
    public String getGsmlpNameSpace(String serviceUrl) {
        String gsmlpNameSpace = gsmlpNameSpaceCache.get(serviceUrl);
        if (gsmlpNameSpace == null) {
            gsmlpNameSpace = getOnlineGsmlpNameSpace(serviceUrl);
            gsmlpNameSpaceCache.put(serviceUrl, gsmlpNameSpace);            
        }
        return gsmlpNameSpace;
    }
    /**
     * Clear cache of gsmlpNameSpaceCache 
     * @return void
     */     
    public void clearCache() {
        gsmlpNameSpaceCache.clear();        
    }
    
    /**
     * Get gsmlp namespace from online request of the serviceUrl
     * 
     * @param serviceUrl
     *            The serviceUrl
     * @return string of gsmlp namespace
     */     
    private String getOnlineGsmlpNameSpace(String serviceUrl) {
        HttpRequestBase method = null;
        String gsmlpNameSpace = "http://xmlns.geosciml.org/geosciml-portrayal/2.0";
        try {
            method = wfsMethodMaker.makeGetCapabilitiesMethod(serviceUrl);
            String responseString = httpServiceCaller.getMethodResponseAsString(method);
            Document responseDoc = DOMUtil.buildDomFromString(responseString);
            Element elem = responseDoc.getDocumentElement();
            gsmlpNameSpace = elem.getAttribute("xmlns:gsmlp");             
            //XPathExpression exprWFS_Cap = DOMUtil.compileXPathExpr("wfs:WFS_Capabilities");
            //Element elem2 = (Element) exprWFS_Cap.evaluate(responseDoc, XPathConstants.STRING); 
        }catch (Exception ex) {
            log.warn(String.format("Get onlineGsmlpNameSpace for '%s' failed", serviceUrl));         
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }        
        return gsmlpNameSpace;
    }
    
}
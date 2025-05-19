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
     * Constructor constructs all the member variables.
     *
     */
    public GsmlpNameSpaceTable() {
        httpServiceCaller = new HttpServiceCaller(9000);
        wfsMethodMaker = new WFSGetFeatureMethodMaker();
        gsmlpNameSpaceCache = new ConcurrentHashMap<String, String>();
    }

    /**
     * Clear cache of gsmlpNameSpaceCache 
     * @return void
     */
    public void clearCache() {
        gsmlpNameSpaceCache.clear();        
    }

}
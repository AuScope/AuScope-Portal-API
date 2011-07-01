package org.auscope.portal.server.web.service;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.server.web.IWFSGetFeatureMethodMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class for making a normal WFS request that is constrained
 * by an additional list of gml:id's
 * 
 * The service will contain a number of optimisations to minimise load/query
 * times at the WFS
 * 
 * @author Josh Vote
 *
 */
@Service
public class WFSFilterWithIDService {
	private HttpServiceCaller httpServiceCaller;
    private IWFSGetFeatureMethodMaker methodMaker;
    
    @Autowired
    public WFSFilterWithIDService(HttpServiceCaller httpServiceCaller, IWFSGetFeatureMethodMaker methodMaker) {
    	this.httpServiceCaller = httpServiceCaller;
    	this.methodMaker = methodMaker;
    }
    
    public String makeIDFilterRequest(String serviceURL, String featureType, String filterString, int maxFeatures) throws Exception {
        return makeIDFilterRequest(serviceURL, featureType, filterString, maxFeatures, null);
    }
    
    /**
     * Creates a PostMethod given the following parameters
     * @param serviceURL - required, exception thrown if not provided
     * @param featureType - required, exception thrown if not provided
     * @param filterString - optional
     * @param maxFeatures - Set to non zero to specify a cap on the number of features to fetch
     * @param srsName - Can be null or empty
     * @return
     * @throws Exception if service URL or featureType is not provided
     */
    public String makeIDFilterRequest(String serviceURL, String featureType, String filterString, int maxFeatures, String srsName) throws Exception {
    	return "";
    }
}

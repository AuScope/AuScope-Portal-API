package org.auscope.portal.server.web.service;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.BaseWFSService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;

/**
 * Minimal implementation of the core BaseWFSService
 * @author Josh Vote
 *
 */
public class SimpleWfsService extends BaseWFSService {

    public SimpleWfsService(HttpServiceCaller httpServiceCaller,
            WFSGetFeatureMethodMaker wfsMethodMaker) {
        super(httpServiceCaller, wfsMethodMaker);
    }

    /**
     * Makes a WFS GetFeature request constrained by the specified parameters. Instead
     * of returning the full response only the count of features will be returned.
     *
     * @param wfsUrl the web feature service url
     * @param featureType the type name
     * @param filterString A OGC filter string to constrain the request
     * @param maxFeatures  A maximum number of features to request
     * @param srsName [Optional] the SRS to make the WFS request using - will use BaseWFSService.DEFAULT_SRS if unspecified
     * @return
     * @throws PortalServiceException
     * @throws URISyntaxException
     */
    public WFSCountResponse getWfsFeatureCount(String wfsUrl, String featureType, String filterString, Integer maxFeatures, String srsName) throws PortalServiceException, URISyntaxException {
        HttpRequestBase method = generateWFSRequest(wfsUrl, featureType, null, filterString, maxFeatures, srsName, ResultType.Hits);
        return getWfsFeatureCount(method);
    }
    
    /**
     * Gets the WFS GET URL for executing a GetFeature request with the specified parameters. Does NOT execute the request,
     * only generates the URL for request execution
     * @param wfsUrl The service endpoint to query
     * @param featureType The feature type to request
     * @param bbox [Optional] The bounds to constrain the request
     * @param maxFeatures [Optional] an upper bound bound of features
     * @param srsName [Optional] the SRS that the response should be encoded using
     * @param outputFormat [Optional] The output format that the response should take
     * @return
     * @throws PortalServiceException 
     */
    public String getFeatureRequestAsString(String wfsUrl, String featureType, FilterBoundingBox bbox, Integer maxFeatures, String srsName, String outputFormat) throws PortalServiceException {
        if (srsName == null || srsName.isEmpty()) {
            srsName = BaseWFSService.DEFAULT_SRS;
        }
        
        try {
            return this.wfsMethodMaker.makeGetMethod(wfsUrl, featureType, maxFeatures, ResultType.Results, srsName, bbox, outputFormat).getURI().toString();
        } catch (URISyntaxException e) {
            throw new PortalServiceException(null, e);
        }
    }
}

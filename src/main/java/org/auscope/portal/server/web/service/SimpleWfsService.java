package org.auscope.portal.server.web.service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.BaseWFSService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.namespaces.XsdNamespace;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;
import org.auscope.portal.core.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    
    /**
     * Makes a WFS request to the specified service, returns the resulting GML
     * @param serviceUrl The service endpoint to query
     * @param featureType The feature type to request
     * @param featureId The specific feature to request
     * @return
     * @throws PortalServiceException
     */
    public String getWfsFeature(String serviceUrl, String featureType, String featureId) throws PortalServiceException {
        HttpRequestBase method = null;
        try {
            method = generateWFSRequest(serviceUrl, featureType, featureId, null, null, null, null);
            String wfs = httpServiceCaller.getMethodResponseAsString(method);
            
            OWSExceptionParser.checkForExceptionResponse(wfs);
            
            return wfs;
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }
    
    /**
     * Utility for making a DescribeFeatureType request for a SF0 feature. The resulting
     * simple schema will be parsed into a collection of SimpleFeatureProperty elements
     * @param serviceUrl The WFS endpoint to query
     * @param featureType The feature type name to describe
     * @return
     * @throws PortalServiceException
     */
    public List<SimpleFeatureProperty> describeSimpleFeature(String serviceUrl, String featureType) throws PortalServiceException {
        
        HttpRequestBase request = null;
        String wfsDescribeFeatureResponse = null;
        try {
            //Make our request
            request = this.wfsMethodMaker.makeDescribeFeatureTypeMethod(serviceUrl, featureType);
            wfsDescribeFeatureResponse = httpServiceCaller.getMethodResponseAsString(request);
            
            OWSExceptionParser.checkForExceptionResponse(wfsDescribeFeatureResponse);
        
            //Parse our response
            Document doc = DOMUtil.buildDomFromString(wfsDescribeFeatureResponse);
            
            NamespaceContext nc = new XsdNamespace();
            XPathExpression expr = DOMUtil.compileXPathExpr(String.format("xsd:schema/xsd:complexType/xsd:complexContent/xsd:extension/xsd:sequence/xsd:element", featureType), nc);
            NodeList elementNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
         
            List<SimpleFeatureProperty> featureTypes = new ArrayList<SimpleFeatureProperty>();
            for (int i = 0; i < elementNodes.getLength(); i++) {
                Node n = elementNodes.item(i);
                
                SimpleFeatureProperty sft = new SimpleFeatureProperty(
                        Integer.parseInt(n.getAttributes().getNamedItem("maxOccurs").getTextContent()), 
                        Integer.parseInt(n.getAttributes().getNamedItem("minOccurs").getTextContent()), 
                        n.getAttributes().getNamedItem("name").getTextContent(), 
                        Boolean.parseBoolean(n.getAttributes().getNamedItem("nillable").getTextContent()), 
                        n.getAttributes().getNamedItem("type").getTextContent(),
                        i + 1);
                
                featureTypes.add(sft);
            }
            
            return featureTypes;
        } catch (Exception ex) {
            throw new PortalServiceException(request, "Error making/handling DescribeFeatureType request"); 
        }
    }
}

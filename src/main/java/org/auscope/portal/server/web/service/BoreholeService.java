package org.auscope.portal.server.web.service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.BaseWFSService;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.csw.CSWRecordsFilterVisitor;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.IFilter;
import org.auscope.portal.core.services.namespaces.WFSNamespaceContext;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.util.DOMUtil;
import org.auscope.portal.gsml.BoreholeFilter;
import org.auscope.portal.gsml.BoreholeWithConfigurableFilter;
import org.auscope.portal.nvcl.NVCLNamespaceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A utility class which provides methods for querying borehole service
 *
 * @author Jarek Sanders
 * @version $Id$
 *
 */
@Service
public class BoreholeService extends BaseWFSService {

    /**
     * Type of SLD Mark
     * @author Josh Vote (CSIRO)
     *
     */
    public enum Mark {
        SQUARE("square"),
        CIRCLE("circle"),
        TRIANGLE("triangle"),
        STAR("star"),
        CROSS("cross"),
        X("x");

        private final String text;
        private Mark(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    // -------------------------------------------------------------- Constants

    private final Log log = LogFactory.getLog(getClass());

    private String gsmlpNameSpace = null;
    // ----------------------------------------------------------- Constructors

    @Autowired
    public BoreholeService(HttpServiceCaller serviceCaller, WFSGetFeatureMethodMaker methodMaker) {
        super(serviceCaller, methodMaker);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Get all boreholes from a given service url and return the response
     *
     * @param serviceUrl
     * @param bbox
     *            Set to the bounding box in which to fetch results, otherwise set it to null
     * @param restrictToIDList
     *            [Optional] A list of gml:id values that the resulting filter should restrict its search space to
     * @return
     * @throws Exception
     */
    public WFSResponse getAllBoreholes(String serviceUrl, String boreholeName, String custodian,
            String dateOfDrillingStart,String dateOfDrillingEnd, int maxFeatures, FilterBoundingBox bbox, List<String> restrictToIDList, String outputFormat,String optionalFilters)
                    throws Exception {
        String filterString;



        if(optionalFilters==null || optionalFilters.isEmpty()){
            BoreholeFilter nvclFilter = new BoreholeFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd,restrictToIDList,null);
            if (bbox == null) {
                filterString = nvclFilter.getFilterStringAllRecords();
            } else {
                filterString = nvclFilter.getFilterStringBoundingBox(bbox);
            }
        }else{
            BoreholeWithConfigurableFilter nvclFilter = new BoreholeWithConfigurableFilter(optionalFilters,restrictToIDList);
            if (bbox == null) {
                filterString = nvclFilter.getFilterStringAllRecords();
            } else {
                filterString = nvclFilter.getFilterStringBoundingBox(bbox);
            }
        }



        HttpRequestBase method = null;
        try {
            // Create a GetFeature request with an empty filter - get all
            method = this.generateWFSRequest(serviceUrl, getTypeName(), null, filterString, maxFeatures, null,
                    ResultType.Results, outputFormat);
            String responseData = this.httpServiceCaller.getMethodResponseAsString(method);

            return new WFSResponse(responseData, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    /**
     * Counts all boreholes from a given service url and return the response
     *
     * @param serviceUrl
     * @param bbox
     *            Set to the bounding box in which to fetch results, otherwise set it to null
     * @param restrictToIDList
     *            [Optional] A list of gml:id values that the resulting filter should restrict its search space to
     * @return
     * @throws Exception
     */
    public int countAllBoreholes(String serviceUrl, String boreholeName, String custodian,
            String dateOfDrillingStart,String dateOfDrillingEnd, int maxFeatures, FilterBoundingBox bbox, List<String> restrictToIDList)
                    throws Exception {
        String filterString;
        BoreholeFilter nvclFilter = new BoreholeFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd,restrictToIDList,null);
        if (bbox == null) {
            filterString = nvclFilter.getFilterStringAllRecords();
        } else {
            filterString = nvclFilter.getFilterStringBoundingBox(bbox);
        }

        HttpRequestBase method = null;
        try {
            // Create a GetFeature request with an empty filter - get all
            method = this.generateWFSRequest(serviceUrl, getTypeName(), null, filterString, maxFeatures, null,
                    ResultType.Hits, null);
            String responseGml = this.httpServiceCaller.getMethodResponseAsString(method);

            Document doc = DOMUtil.buildDomFromString(responseGml, true);
            String number = (String) DOMUtil.compileXPathExpr("wfs:FeatureCollection/@numberOfFeatures", new WFSNamespaceContext()).evaluate(doc, XPathConstants.STRING);
            return Integer.parseInt(number);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    private void appendHyloggerBoreholeIDs(String url, String typeName, List<String> idList)
            throws PortalServiceException, URISyntaxException {
        //Make request
        HttpRequestBase method = wfsMethodMaker.makeGetMethod(url, typeName, (Integer) null, null);
        try {
            String wfsResponse = httpServiceCaller.getMethodResponseAsString(method);

            //Parse response
            Document doc = DOMUtil.buildDomFromString(wfsResponse);
            OWSExceptionParser.checkForExceptionResponse(doc);

            NVCLNamespaceContext nc = new NVCLNamespaceContext();

            //Get our ID's
            NodeList publishedDatasets = (NodeList) DOMUtil.compileXPathExpr("/wfs:FeatureCollection/gml:featureMembers/" + NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME + "/nvcl:scannedBorehole", nc)
                    .evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < publishedDatasets.getLength(); i++) {
                Node holeIdentifier = (Node) DOMUtil.compileXPathExpr("@xlink:href", nc)
                        .evaluate(publishedDatasets.item(i), XPathConstants.NODE);
                if (holeIdentifier != null) {
                    String[] urnBlocks = holeIdentifier.getTextContent().split("/");
                    if (urnBlocks.length > 1) {
                        // skip invalid URIs
                        idList.add(urnBlocks[urnBlocks.length - 1].trim());
                    }
                }

            }
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    /**
     * Goes to the CSWService to get all services that support the PUBLISHED_DATASETS_TYPENAME and queries them to generate a list of borehole ID's that
     * represent every borehole with Hylogger data.
     *
     * If any of the services queried fail to return valid responses they will be skipped
     *
     * @param cswService
     *            Will be used to find the appropriate service to query
     * @param CSWRecordsFilterVisitor
     *            A filter visitor used to perform filter operation on the online resource. Use null if not required
     * @throws Exception
     */
    public List<String> discoverHyloggerBoreholeIDs(CSWCacheService cswService, CSWRecordsFilterVisitor visitor) {
        List<String> ids = new ArrayList<String>();

        for (CSWRecord record : cswService.getWFSRecords()) {
            for (AbstractCSWOnlineResource resource : record.getOnlineResourcesByType(visitor, OnlineResourceType.WFS)) {
                if (resource.getName().equals(NVCLNamespaceContext.PUBLISHED_DATASETS_TYPENAME)) {
                    try {
                        appendHyloggerBoreholeIDs(resource.getLinkage().toString(), resource.getName(), ids);
                    } catch (Exception ex) {
                        log.warn(String.format("Discovering boreholes at '%1$s' failed", resource.getLinkage()), ex);
                    }
                }
            }
        }

        return ids;
    }

    public String getFilter(String boreholeName, String custodian, String dateOfDrillingStart,String dateOfDrillingEnd,
            int maxFeatures, FilterBoundingBox bbox, List<String> ids, Boolean justNVCL,String optionalFilters) throws Exception {
        BoreholeFilter filter = new BoreholeFilter(boreholeName, custodian, dateOfDrillingStart,dateOfDrillingEnd, ids,optionalFilters);
        return generateFilterString(filter, bbox);
    }

    /**
     * Utility for turning a filter and optional bounding box into a OGC filter string
     *
     * @param filter
     *            The filter
     * @param bbox
     *            [Optional] the spatial bounds to constrain the result set
     * @return
     */
    public static String generateFilterString(IFilter filter, FilterBoundingBox bbox) {
        String filterString = null;
        if (bbox == null) {
            filterString = filter.getFilterStringAllRecords();
        } else {
            filterString = filter.getFilterStringBoundingBox(bbox);
        }

        return filterString;
    }

    /**
     * Generates a broad SLD for symbolising a set of filters
     *
     * @param names 1-1 correspondance with filters - the human readable names of each filter
     * @param filters The filters to be symbolised
     * @param colors 1-1 correspondance with filters - The CSS color for each filter to be symbolised with
     * @param marks 1-1 correspondance with filters - The SLD symbols to use for each filter
     * @param gsmlpNameSpace
     * @return
     */
    public String getStyle(List<String> names, List<String> filters, List<String> colors, List<String> marks, String gsmlpNameSpace) {
        if (filters.size() != colors.size() || filters.size() != names.size() || filters.size() != marks.size()) {
            throw new IllegalArgumentException("names/filters/colors/marks must have the same length");
        }

        if (gsmlpNameSpace != null && !gsmlpNameSpace.isEmpty()) {
            setGsmlpNameSpace(gsmlpNameSpace);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<StyledLayerDescriptor version=\"1.0.0\" xmlns:gsmlp=\"" + getGsmlpNameSpace() + "\" ");
        sb.append("xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        sb.append("<NamedLayer>" + "<Name>");
        sb.append(getTypeName());
        sb.append("</Name>");
        sb.append("<UserStyle>");
        sb.append("<Name>portal-style</Name>");
        sb.append("<Title>portal-style</Title>");
        sb.append("<Abstract>portal-style</Abstract>");
        sb.append("<IsDefault>1</IsDefault>");
        for (int i = 0; i < filters.size(); i++) {
            sb.append("<FeatureTypeStyle>");
            sb.append("<Rule>");
            sb.append("<Name>" + names.get(i) + "</Name>");
            sb.append("<Abstract>" + names.get(i) + "</Abstract>");
            sb.append("<MaxScaleDenominator>4000000</MaxScaleDenominator>");
            sb.append(filters.get(i));
            sb.append("<PointSymbolizer>");
            sb.append("<Geometry><ogc:PropertyName>" + getGeometryName() + "</ogc:PropertyName></Geometry>");
            sb.append("<Graphic>");
            sb.append("<Mark>");
            sb.append("<WellKnownName>" + marks.get(i) + "</WellKnownName>");
            sb.append("<Fill>");
            sb.append("<CssParameter name=\"fill\">");
            sb.append(colors.get(i));
            sb.append("</CssParameter>");
            sb.append("<CssParameter name=\"fill-opacity\">0.4</CssParameter>");            
            sb.append("</Fill>");
            sb.append("<Stroke>");
            sb.append("<CssParameter name=\"stroke\">");
            sb.append(colors.get(i));
            sb.append("</CssParameter>");
            sb.append("<CssParameter name=\"stroke-width\">0.5</CssParameter>");            
            sb.append("</Stroke>");
            sb.append("</Mark>");
            sb.append("<Size>8</Size>");
            sb.append("</Graphic>");
            sb.append("</PointSymbolizer>");
            sb.append("<TextSymbolizer>");
            sb.append("<Label>");
            sb.append("<ogc:Function name=\"strSubstringStart\">");
            sb.append("<ogc:PropertyName>gsmlp:name</ogc:PropertyName>");
            sb.append("<ogc:Function name=\"parseInt\">");
            sb.append("<ogc:Literal>27</ogc:Literal>");
            sb.append("</ogc:Function>");
            sb.append("</ogc:Function>");
            sb.append("</Label>");
            sb.append("<Font>");
            sb.append("<CssParameter name=\"font-family\">Arial</CssParameter>");
            sb.append("<CssParameter name=\"font-size\">12</CssParameter>");
            sb.append("<CssParameter name=\"font-style\">normal</CssParameter>");
            sb.append("<CssParameter name=\"font-weight\">normal</CssParameter>");
            sb.append("</Font>");
            sb.append("<LabelPlacement>");
            sb.append("<PointPlacement>");
            sb.append("<Displacement>");
            sb.append("<DisplacementX>6</DisplacementX>");
            sb.append("<DisplacementY>-6</DisplacementY>");
            sb.append("</Displacement>");
            sb.append("</PointPlacement>");
            sb.append("</LabelPlacement>");
            sb.append("<Fill>");
            sb.append("<CssParameter name=\"fill\">#000000</CssParameter>");
            sb.append("</Fill>");
            sb.append("</TextSymbolizer>");
            sb.append("</Rule>");
            // No Labels at high zoom levels
            sb.append("<Rule>");
            sb.append("<Name>" + names.get(i) + "</Name>");
            sb.append("<Abstract>" + names.get(i) + "</Abstract>");
			sb.append("<MinScaleDenominator>4000000</MinScaleDenominator>");
            sb.append(filters.get(i));
            sb.append("<PointSymbolizer>");
            sb.append("<Geometry><ogc:PropertyName>" + getGeometryName() + "</ogc:PropertyName></Geometry>");
            sb.append("<Graphic>");
            sb.append("<Mark>");
            sb.append("<WellKnownName>" + marks.get(i) + "</WellKnownName>");
            sb.append("<Fill>");
            sb.append("<CssParameter name=\"fill\">");
            sb.append(colors.get(i));
            sb.append("</CssParameter>");
            sb.append("<CssParameter name=\"fill-opacity\">0.4</CssParameter>");            
            sb.append("</Fill>");
            sb.append("<Stroke>");
            sb.append("<CssParameter name=\"stroke\">");
            sb.append(colors.get(i));
            sb.append("</CssParameter>");
            sb.append("<CssParameter name=\"stroke-width\">0.5</CssParameter>");            
            sb.append("</Stroke>");
            sb.append("</Mark>");
            sb.append("<Size>8</Size>");
            sb.append("</Graphic>");
            sb.append("</PointSymbolizer>");
            sb.append("</Rule>");
            sb.append("</FeatureTypeStyle>");
        }

        sb.append("</UserStyle>");
        sb.append("</NamedLayer>");
        sb.append("</StyledLayerDescriptor>");

        return sb.toString();
    }

    public boolean namespaceSupportsHyloggerFilter(String namespace) {
        return namespace.equals("http://xmlns.geosciml.org/geosciml-portrayal/4.0");
    }

    public String getTypeName() {
        return "gsml:Borehole";
    }

    public String getGeometryName() {
        return "gsml:collarLocation/gsml:BoreholeCollar/gsml:location";
    }
    public String getGsmlpNameSpace() {
        if (gsmlpNameSpace == null)
            return " xmlns:gsmlp=\"http://xmlns.geosciml.org/geosciml-portrayal/2.0\" ";
        else
            return gsmlpNameSpace;
    }
    public void setGsmlpNameSpace(String gsmlpNameSpace) {
        this.gsmlpNameSpace = gsmlpNameSpace;
    }
}

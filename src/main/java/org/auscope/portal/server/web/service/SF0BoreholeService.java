package org.auscope.portal.server.web.service;

import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.gsml.SF0BoreholeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class for making requests to the SF0 Borehole web service
 *
 * @author Florence Tan
 *
 */
@Service
public class SF0BoreholeService extends BoreholeService {

    // -------------------------------------------------------------- Constants

    // ----------------------------------------------------------- Constructors

    @Autowired
    public SF0BoreholeService(HttpServiceCaller serviceCaller, WFSGetFeatureMethodMaker methodMaker) {
        super(serviceCaller, methodMaker);
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Get all SF0 Boreholes from a given service url and return the response
     *
     * @param serviceUrl
     * @param bbox
     *            Set to the bounding box in which to fetch results, otherwise set it to null
     * @param restrictToIDList
     *            [Optional] A list of gml:id values that the resulting filter should restrict its search space to
     * @return
     * @throws Exception
     */
    /*public WFSResponse getAllBoreholes(String serviceUrl, String boreholeName, String custodian,
            String dateOfDrillingStart,String dateOfDrillingEnd, int maxFeatures, FilterBoundingBox bbox) throws Exception {
        return getAllBoreholes(serviceUrl, boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null);
    }*/

    /**
     * Get all SF0 Boreholes from a given service url and return the response
     *
     * @param serviceUrl
     * @param bbox
     *            Set to the bounding box in which to fetch results, otherwise set it to null
     * @param restrictToIDList
     *            [Optional] A list of gml:id values that the resulting filter should restrict its search space to
     * @param skipGsmlpName if true then skip the gsmlp:name property in the generated filter
     * @return
     * @throws Exception
     */
    public WFSResponse getAllBoreholes(String serviceUrl, String boreholeName, String custodian,
            String dateOfDrillingStart, String dateOfDrillingEnd, int maxFeatures, FilterBoundingBox bbox, 
            String outputFormat, String typeName, Boolean skipGsmlpShapeProperty) throws Exception {
        String filterString;
        SF0BoreholeFilter sf0BoreholeFilter = new SF0BoreholeFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, null, null, null, null, skipGsmlpShapeProperty);
        if (bbox == null) {
            filterString = sf0BoreholeFilter.getFilterStringAllRecords();
        } else {
            filterString = sf0BoreholeFilter.getFilterStringBoundingBox(bbox);
        }
        HttpRequestBase method = null;
        try {
            // Create a GetFeature request with an empty filter - get all
            method = this.generateWFSRequest(serviceUrl, typeName, null, filterString, maxFeatures, null,
                    ResultType.Results, outputFormat);
            String responseGml = this.httpServiceCaller.getMethodResponseAsString(method);
            
            System.out.println(responseGml);

            return new WFSResponse(responseGml, method);
        } catch (Exception ex) {
            throw new PortalServiceException(method, ex);
        }
    }

    @Override
    public String getFilter(String boreholeName, String custodian, String dateOfDrillingStart, String dateOfDrillingEnd,
            int maxFeatures, FilterBoundingBox bbox, List<String> ids, Boolean justNVCL,String optionalFilters) throws Exception {
        SF0BoreholeFilter filter = new SF0BoreholeFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, ids, null, justNVCL,optionalFilters);
        return generateFilterString(filter, bbox);
    }

    public String getFilter(String boreholeName, String custodian, String dateOfDrillingStart, String dateOfDrillingEnd,
            int maxFeatures, FilterBoundingBox bbox, List<String> ids, List<String> identifiers, Boolean justNVCL,String optionalFilters) throws Exception {
        SF0BoreholeFilter filter = new SF0BoreholeFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, ids, identifiers, justNVCL,optionalFilters);
        return generateFilterString(filter, bbox);
    }

    @Override
    public String getTypeName() {
        return "gsmlp:BoreholeView";
    }

    @Override
    public String getGeometryName() {
        return "gsmlp:shape";
    }

}

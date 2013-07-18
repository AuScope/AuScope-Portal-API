package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.SimpleBBoxFilter;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;
import org.auscope.portal.core.services.responses.wfs.WFSGetCapabilitiesResponse;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Acts as a proxy to WFS's
 * 
 */
@Controller
public class WFSController extends BasePortalController {

    private SimpleWfsService wfsService;

    @Autowired
    public WFSController(SimpleWfsService wfsService) {
        this.wfsService = wfsService;
    }

    /**
     * Given a WFS service Url and a feature type this will query for the count of all of the features
     * that optionally lie within a bounding box
     *
     * @param serviceUrl The WFS endpoint
     * @param featureType The feature type name to query
     * @param boundingBox [Optional] A JSON encoding of a FilterBoundingBox instance
     * @param maxFeatures [Optional] The maximum number of features to query
     */
    @RequestMapping("/getFeatureCount.do")
    public ModelAndView requestFeatureCount(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("typeName") final String featureType,
                                           @RequestParam(required = false, value = "crs") final String bboxCrs,
                                           @RequestParam(required = false, value = "northBoundLatitude") final Double northBoundLatitude,
                                           @RequestParam(required = false, value = "southBoundLatitude") final Double southBoundLatitude,
                                           @RequestParam(required = false, value = "eastBoundLongitude") final Double eastBoundLongitude,
                                           @RequestParam(required = false, value = "westBoundLongitude") final Double westBoundLongitude,
                                           @RequestParam(required = false, value = "maxFeatures") Integer maxFeatures) throws Exception {

        FilterBoundingBox bbox = null;
        if (northBoundLatitude != null) {
            bbox = FilterBoundingBox.parseFromValues(bboxCrs, northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude);
        }
        SimpleBBoxFilter filter = new SimpleBBoxFilter();
        String filterString = null;
        if (bbox == null) {
            filterString = filter.getFilterStringAllRecords();
        } else {
            filterString = filter.getFilterStringBoundingBox(bbox);
        }

        WFSCountResponse response = null;
        try {
            response = wfsService.getWfsFeatureCount(serviceUrl, featureType, filterString, maxFeatures, null);
        } catch (Exception ex) {
            log.warn(String.format("Exception getting '%2$s' from '%1$s': %3$s", serviceUrl, featureType, ex));
            log.debug("Exception: ", ex);
            return generateExceptionResponse(ex, serviceUrl);
        }

        return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
    }
    
    /**
     * Generate and execute a WFS GetCapabilities request to the specified WFS endpoint. Parse the result into
     * an array of JSON objects {format: String} representing valid options for the GetFeature outputFormat parameter 
     * @param serviceUrl WFS endpoint to query
     * @return
     */
    @RequestMapping("/getFeatureRequestOutputFormats.do")
    public ModelAndView getFeatureRequestOutputFormats(@RequestParam("serviceUrl") final String serviceUrl) {
        WFSGetCapabilitiesResponse response = null;
        try {
            response = wfsService.getCapabilitiesResponse(serviceUrl);
        } catch (Exception ex) {
            log.warn(String.format("Exception generating service request for '%1$s': %2$s", serviceUrl, ex));
            log.debug("Exception: ", ex);
            return generateExceptionResponse(ex, serviceUrl);
        }
        
        //Convert the response from Strings to named JSON objects
        List<ModelMap> convertedItems = new ArrayList<ModelMap>(response.getGetFeatureOutputFormats().length);
        for (String of : response.getGetFeatureOutputFormats()) {
            convertedItems.add(new ModelMap("format", of));
        }
        
        return generateJSONResponseMAV(true, convertedItems, "");
    }

}

package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.WFSService;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.gsml.SF0BoreholeFilter;
import org.auscope.portal.server.web.service.NVCL2_0_DataService;
import org.auscope.portal.server.web.service.SF0BoreholeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for handling requests for the SF0 Borehole
 *
 * @author Florence Tan
 *
 */
@Controller
public class SF0BoreholeController extends BasePortalController {

    private SF0BoreholeService boreholeService;

    private NVCL2_0_DataService nvclDataService;
    private WFSService wfsService;

    @Autowired
    public SF0BoreholeController(SF0BoreholeService sf0BoreholeService, CSWCacheService cswService, NVCL2_0_DataService nvclDataService, WFSService wfsService) {
        this.boreholeService = sf0BoreholeService;
        this.nvclDataService = nvclDataService;
        this.wfsService = wfsService;
    }

    /**
     * Handles the borehole filter queries.
     *
     * @param serviceUrl
     *            the url of the service to query
     * @param mineName
     *            the name of the mine to query for
     * @param request
     *            the HTTP client request
     * @param omitGsmlpShapeProperty
     *            if true, omit the gsmlp:shape property from the generated filter (can cause problems with GADDS 2.0)
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doBoreholeViewFilter.do")
    public ModelAndView doBoreholeFilter(String serviceUrl, String boreholeName, String custodian,
            String dateOfDrillingStart, String dateOfDrillingEnd, int maxFeatures, String bbox, String typeName,
            @RequestParam(required=false, value="outputFormat") String outputFormat,
            @RequestParam(required=false, defaultValue="false") boolean omitGsmlpShapeProperty) throws Exception {

        try {
            FilterBoundingBox box = FilterBoundingBox.attemptParseFromJSON(bbox);
            WFSResponse response = this.boreholeService.getAllBoreholes(serviceUrl, boreholeName, custodian,
                    dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, box, outputFormat, typeName, omitGsmlpShapeProperty);
            return generateNamedJSONResponseMAV(true, "gml", response.getData(), response.getMethod());
        } catch (Exception e) {
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }

    
    /**
     * Handles the borehole filter queries, but returns CSV values
     *
     * @param serviceUrl
     *            the url of the service to query
     * @param mineName
     *            the name of the mine to query for
     * @param request
     *            the HTTP client request
     * @return a WFS response converted into CSV
     * @throws Exception
     */
    @RequestMapping("/doNVCLBoreholeViewCSVDownload.do")
    public void doNVCLBoreholeViewCSVDownload(String serviceUrl,String typeName,
    		@RequestParam(required=false, value="bbox") String bbox,
            @RequestParam(required=false, value="outputFormat") String outputFormat,
            @RequestParam(required=false, value="filter") String filter,
            @RequestParam(required=false, value="maxFeatures",defaultValue = "100000") Integer maxFeatures,
            HttpServletResponse response) throws Exception {
    	
    	OutputStream outputStream = response.getOutputStream();
        try {
        	response.setContentType("text/csv");
        	
            FilterBoundingBox box = FilterBoundingBox.attemptParseFromJSON(bbox);
            
            String filterString;
            SF0BoreholeFilter sf0BoreholeFilter = new SF0BoreholeFilter(true);

            InputStream result = null;
            if (filter != null && filter.indexOf("ogc:Filter")>0) { //Polygon filter
                filterString = filter;
                result = wfsService.downloadCSVByPolygonFilter(serviceUrl, typeName, filterString, maxFeatures);
                // LJ filtering out records of nvclCollection == false
                @SuppressWarnings("unused")
                int totalReturnLine = nvclDataService.nvclCollectionFilter(result, outputStream);
                //System.out.print(totalReturnLine);
            } else {
                if (box == null ) {
                    filterString = sf0BoreholeFilter.getFilterStringAllRecords();
                } else {
                    filterString = sf0BoreholeFilter.getFilterStringBoundingBox(box);
                }

                result = wfsService.downloadCSV(serviceUrl, typeName, filterString,maxFeatures);
            }            
            
            FileIOUtil.writeInputToOutputStream(result, outputStream, 8 * 1024, true);
            outputStream.close();
            
        } catch (Exception e) {
        	log.warn(String.format("Unable to request/transform WFS response from '%1$s': %2$s", serviceUrl,e));
            log.debug("Exception: ", e);  
            IOUtils.write("An error has occurred: "+ e.getMessage(), outputStream, StandardCharsets.UTF_8);
            outputStream.close();
        }
    }
    /**
     * Handles getting the style of the SF0 borehole filter queries. (If the bbox elements are specified, they will limit the output response to 200 records
     * implicitly)
     *
     * @param mineName
     *            the name of the mine to query for
     * @param bbox
     * @param maxFeatures
     * @throws Exception
     */
    @RequestMapping("/doNvclV2Filter.do")
    public void doNvclV2Filter(
            HttpServletResponse response,
            @RequestParam(required = false, value = "boreholeName", defaultValue = "") String boreholeName,
            @RequestParam(required = false, value = "custodian", defaultValue = "") String custodian,
            @RequestParam(required = false, value = "dateOfDrillingStart", defaultValue = "") String dateOfDrillingStart,
            @RequestParam(required = false, value = "dateOfDrillingEnd", defaultValue = "") String dateOfDrillingEnd,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters)

                    throws Exception {

        FilterBoundingBox bbox = null;
        List<String> hyloggerBoreholeIDs = null;

        String filters = this.boreholeService.getFilter(boreholeName,
                    custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox,
                    hyloggerBoreholeIDs, true,optionalFilters);

        response.setContentType("text/xml");
        ByteArrayInputStream styleStream = new ByteArrayInputStream(filters.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
}

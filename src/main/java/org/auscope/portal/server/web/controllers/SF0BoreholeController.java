package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.WFSService;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.gsml.SF0BoreholeFilter;
import org.auscope.portal.server.domain.nvcldataservice.AnalyticalJobResults;
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

    // private GsmlpNameSpaceTable gsmlpNameSpaceTable;
    private NVCL2_0_DataService nvclDataService;
    private WFSService wfsService;

    @Autowired
    public SF0BoreholeController(SF0BoreholeService sf0BoreholeService, CSWCacheService cswService, NVCL2_0_DataService nvclDataService, WFSService wfsService) {
        this.boreholeService = sf0BoreholeService;
        this.nvclDataService = nvclDataService;
        // GsmlpNameSpaceTable _gsmlpNameSpaceTable = new GsmlpNameSpaceTable();
        // this.gsmlpNameSpaceTable = _gsmlpNameSpaceTable;
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
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doBoreholeViewFilter.do")
    public ModelAndView doBoreholeFilter(String serviceUrl, String boreholeName, String custodian,
            String dateOfDrillingStart, String dateOfDrillingEnd, int maxFeatures, String bbox,
            @RequestParam(required=false, value="outputFormat") String outputFormat) throws Exception {

        try {
            FilterBoundingBox box = FilterBoundingBox.attemptParseFromJSON(bbox);
            WFSResponse response = this.boreholeService.getAllBoreholes(serviceUrl, boreholeName, custodian,
                    dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, box, outputFormat);
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
            HttpServletResponse response) throws Exception {
    	
    	OutputStream outputStream = response.getOutputStream();
        try {
        	response.setContentType("text/csv");
        	
            FilterBoundingBox box = FilterBoundingBox.attemptParseFromJSON(bbox);
            
            String filterString;
            SF0BoreholeFilter sf0BoreholeFilter = new SF0BoreholeFilter(true);
            if (box == null) {
                filterString = sf0BoreholeFilter.getFilterStringAllRecords();
            } else {
                filterString = sf0BoreholeFilter.getFilterStringBoundingBox(box);
            }
            
            
            InputStream result = wfsService.downloadCSV(serviceUrl, typeName, filterString,0);
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
    @RequestMapping("/doNvclV2FilterStyle.do")
    public void doNvclV2FilterStyle(
            HttpServletResponse response,
            @RequestParam(required = false, value = "serviceUrl", defaultValue = "") String serviceUrl,
            @RequestParam(required = false, value = "boreholeName", defaultValue = "") String boreholeName,
            @RequestParam(required = false, value = "custodian", defaultValue = "") String custodian,
            @RequestParam(required = false, value = "dateOfDrillingStart", defaultValue = "") String dateOfDrillingStart,
            @RequestParam(required = false, value = "dateOfDrillingEnd", defaultValue = "") String dateOfDrillingEnd,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "serviceFilter", defaultValue = "") String serviceFilter,
            @RequestParam(required = false, value = "color", defaultValue = "") String color,
            @RequestParam(required = false, value = "analyticsJobId") String analyticsJobId,
            @RequestParam(required = false, value = "failIds") String failIds,
            @RequestParam(required = false, value = "errorIds") String errorIds,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters)

                    throws Exception {

        FilterBoundingBox bbox = null;
        List<String> hyloggerBoreholeIDs = null;

        List<String> filterNames = new ArrayList<String>();
        List<String> filterColors = new ArrayList<String>();
        List<String> filters = new ArrayList<String>();
        List<String> filterMarks = new ArrayList<String>();
        String gsmlpNameSpace = "http://xmlns.geosciml.org/geosciml-portrayal/4.0";
		// gsmlpNameSpaceTable is no longer required as all data providers support v4
		// However it may be required again for future versions of boreholeview 		
		// String gsmlpNameSpace = gsmlpNameSpaceTable.getGsmlpNameSpace(serviceUrl);
        if (StringUtils.isNotEmpty(analyticsJobId)) {
            //Generate a style for displaying pass/fail/error holes
            AnalyticalJobResults analyticsResults = nvclDataService.getProcessingResults(analyticsJobId);

            if (!analyticsResults.getErrorBoreholes().isEmpty()) {
                filterNames.add("Error Boreholes");
                filterColors.add("#ff8000");
                filters.add(this.boreholeService.getFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null, analyticsResults.getErrorBoreholes(), true,optionalFilters));
                //filterMarks.add("ttf://Webdings#0x0073");
                filterMarks.add("circle");
            }

            if (!analyticsResults.getFailBoreholes().isEmpty()) {
                filterNames.add("Fail Boreholes");
                filterColors.add("#cc0000");
                filters.add(this.boreholeService.getFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null, analyticsResults.getFailBoreholes(), true,optionalFilters));
                //filterMarks.add("ttf://Webdings#0x0072");
                filterMarks.add("circle");
            }

            if (!analyticsResults.getPassBoreholes().isEmpty()) {
                filterNames.add("Pass Boreholes");
                filterColors.add(color.isEmpty() ? "#0000ff" : color);
                filters.add(this.boreholeService.getFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null, analyticsResults.getPassBoreholes(), true,optionalFilters));
                //filterMarks.add("ttf://Webdings#0x0061");
                filterMarks.add("circle");
            }
        } else {
            if (this.boreholeService.namespaceSupportsHyloggerFilter(gsmlpNameSpace)) {
                filters.add(this.boreholeService.getFilter(boreholeName,
                        custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox,
                        hyloggerBoreholeIDs, true,optionalFilters));
                filterColors.add("#FF0000");
                filterNames.add("Hylogged");
                filterMarks.add("circle");
            }
        }

        response.setContentType("text/xml");

        String style = this.boreholeService.getStyle(filterNames, filters, filterColors, filterMarks, gsmlpNameSpace);
        ByteArrayInputStream styleStream = new ByteArrayInputStream(style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
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
    @RequestMapping("/doBoreholeViewFilterStyle.do")
    public void doFilterStyle(
            HttpServletResponse response,
            @RequestParam(required = false, value = "serviceUrl", defaultValue = "") String serviceUrl,
            @RequestParam(required = false, value = "boreholeName", defaultValue = "") String boreholeName,
            @RequestParam(required = false, value = "custodian", defaultValue = "") String custodian,
            @RequestParam(required = false, value = "dateOfDrillingStart", defaultValue = "") String dateOfDrillingStart,
            @RequestParam(required = false, value = "dateOfDrillingEnd", defaultValue = "") String dateOfDrillingEnd,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "serviceFilter", defaultValue = "") String serviceFilter,
            @RequestParam(required = false, value = "color", defaultValue = "") String color,
            @RequestParam(required = false, value = "analyticsJobId") String analyticsJobId,
            @RequestParam(required = false, value = "failIds") String failIds,
            @RequestParam(required = false, value = "errorIds") String errorIds,
            @RequestParam(required = false, value = "showNoneHylogged", defaultValue = "false") Boolean showNoneHylogged,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters)

                    throws Exception {

        FilterBoundingBox bbox = null;
        //				FilterBoundingBox
        //				.attemptParseFromJSON(bboxJson);

        List<String> hyloggerBoreholeIDs = null;
        // AUS-2445
        // RA: we can't show WMS for NVCL for now because the way GeoServer filter WMS isn't very efficient and
        // it will cause services with a lot of scanned boreholes (e.g. SA) to run out of memory!
        //		try {
        //			// don't get hylogger IDs if this is only to populate the legend
        //			if (!serviceUrl.isEmpty()) {
        //				hyloggerBoreholeIDs = this.boreholeService
        //						.discoverHyloggerBoreholeIDs(this.cswService,
        //								new CSWRecordsHostFilter(serviceUrl));
        //			}
        //		} catch (Exception e) {
        //			log.warn(String
        //					.format("Error requesting list of hylogger borehole ID's from %1$s: %2$s",
        //							serviceUrl, e));
        //			log.debug("Exception:", e);
        //		}

        //Generate a style. Either generate a Hylogged/Non Hylogged style OR generate a style for NVCL analytics
        //that includes pass/fail/error boreholes
        List<String> filterNames = new ArrayList<String>();
        List<String> filterColors = new ArrayList<String>();
        List<String> filters = new ArrayList<String>();
        List<String> filterMarks = new ArrayList<String>();
		String gsmlpNameSpace = "http://xmlns.geosciml.org/geosciml-portrayal/4.0";
		// gsmlpNameSpaceTable is no longer required as all data providers support v4
		// However it may be required again for future versions of boreholeview 
        // String gsmlpNameSpace = gsmlpNameSpaceTable.getGsmlpNameSpace(serviceUrl);
        if (StringUtils.isNotEmpty(analyticsJobId)) {
            //Generate a style for displaying pass/fail/error holes
            AnalyticalJobResults analyticsResults = nvclDataService.getProcessingResults(analyticsJobId);

            if (!analyticsResults.getErrorBoreholes().isEmpty()) {
                filterNames.add("Error Boreholes");
                filterColors.add("#ff8000");
                filters.add(this.boreholeService.getFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null, analyticsResults.getErrorBoreholes(), true,optionalFilters));
                //filterMarks.add("ttf://Webdings#0x0073");
                filterMarks.add("circle");
            }

            if (!analyticsResults.getFailBoreholes().isEmpty()) {
                filterNames.add("Fail Boreholes");
                filterColors.add("#cc0000");
                filters.add(this.boreholeService.getFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null, analyticsResults.getFailBoreholes(), true,optionalFilters));
                //filterMarks.add("ttf://Webdings#0x0072");
                filterMarks.add("circle");
            }

            if (!analyticsResults.getPassBoreholes().isEmpty()) {
                filterNames.add("Pass Boreholes");
                filterColors.add(color.isEmpty() ? "#0000ff" : color);
                filters.add(this.boreholeService.getFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null, analyticsResults.getPassBoreholes(), true,optionalFilters));
                //filterMarks.add("ttf://Webdings#0x0061");
                filterMarks.add("circle");
            }
        } else {
            //Generate a Hylogged vs Non Hylogged style
            filters.add(this.boreholeService.getFilter(boreholeName, custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox, null, null, null,optionalFilters));
            filterColors.add(color.isEmpty() ? "#2242c7" : color);
            filterNames.add("Boreholes");
            filterMarks.add("circle");

            //Not all borehole services support the hylogged attribute
            Boolean justNVCL = showNoneHylogged;
            if (justNVCL && this.boreholeService.namespaceSupportsHyloggerFilter(gsmlpNameSpace)) {
                filters.add(this.boreholeService.getFilter(boreholeName,
                        custodian, dateOfDrillingStart, dateOfDrillingEnd, maxFeatures, bbox,
                        hyloggerBoreholeIDs, justNVCL,optionalFilters));
                filterColors.add("#FF0000");
                filterNames.add("Hylogged");
                filterMarks.add("circle");
            }
        }

        response.setContentType("text/xml");

        String style = this.boreholeService.getStyle(filterNames, filters, filterColors, filterMarks, gsmlpNameSpace);
        ByteArrayInputStream styleStream = new ByteArrayInputStream(style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }

    /**
     * NOT CURRENTLY USED
	 * This controller method is for forcing the internal cache of GsmlpNameSpaceTable to invalidate and update.
     *
     * @return
     */
	/*
    @RequestMapping("/updateGsmlpNSCache.do")
    public ModelAndView updateGsmlpNSCache() throws Exception {
	    try {
            if (gsmlpNameSpaceTable != null )
                gsmlpNameSpaceTable.clearCache();
            return generateJSONResponseMAV(true);
        } catch (Exception e) {
            log.warn(String.format("Error updating GsmlpNS cache: %1$s", e));
            log.debug("Exception:", e);
            return generateJSONResponseMAV(false);
        }
		
    }
	*/
}

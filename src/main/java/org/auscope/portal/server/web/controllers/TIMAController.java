package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.WFSService;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.gsml.TIMAGeosampleFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controller handles mineral data analysis service of certain sites
 */
@RestController
@SecurityRequirement(name = "public")
public class TIMAController extends BasePortalController {

    /** Used for making general WFS requests */
    private WFSService wfsService;

    @Autowired
    public TIMAController(WFSService wfsService) {
        this.wfsService = wfsService;
    }


    /**
     * Downloads results from TIMA (TESCAN Integrated Mineral Analyser) analysis in CSV format
     *
     * @param sampleName
     *            Name of sample
     * @param igsn
     *            IGSN (International Geo Sample Number) of sample
     * @param bbox
     *            JSON bounding box
     * @param maxFeatures
     *            Maximum numb er of features to retrieve
     * @param optionalFilters
     *            Extra WFS filters to insert into query
     * @param outputFormat
     *             (not used)
     * @return a WFS response converted into CSV
     * @throws Exception
     */
    @GetMapping("/doTIMAGeoSampleCSVDownload.do")
    public void doTIMAGeoSampleCSVDownload(@RequestParam(required = false, value = "serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "sampleName") String sampleName,
            @RequestParam(required = false, value = "igsn") String igsn,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "filter") String filter,
            @RequestParam(required = false, value = "maxFeatures",defaultValue = "100000") Integer maxFeatures,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "outputFormat") String outputFormat,
            HttpServletResponse response)
                    throws Exception {

        String filterString;

        //Make our request and get it transformed
        InputStream result = null;
        response.setContentType("text/csv");
        OutputStream outputStream = response.getOutputStream();

        try {
            if (filter != null && filter.indexOf("ogc:Filter")>0) { //Polygon filter
                filterString = filter.replace("gsmlp:shape","gml:location");
                result = wfsService.downloadCSVByPolygonFilter(serviceUrl, "tima:geosample_and_mineralogy", filterString, maxFeatures);
            } else{ //BBox or no filter
                //Build our filter details
                filterString = generateGeoSampleFilter(sampleName, igsn, bboxJson, optionalFilters);
                result = wfsService.downloadCSV(serviceUrl, "tima:geosample_and_mineralogy", filterString, maxFeatures);
            }
        } catch (Exception ex) {
            log.warn(String.format("Unable to request/transform WFS response for '%1$s' from '%2$s': %3$s", sampleName,
                    serviceUrl, ex));
            log.debug("Exception: ", ex);
        }
        FileIOUtil.writeInputToOutputStream(result, outputStream, 8 * 1024, true);
        outputStream.close();
    }


    /**
     * Downloads results from SHRIMP (Sensitive High Resolution Ion Micro Probe) analysis in CSV format
     *
     * @param serviceUrl
     *            the url of the service to query
     * @param sampleName
     *            The name of the sample
     * @param igsn
     * @return a WFS response converted into CSV
     * @throws Exception
     */
    @GetMapping("/doSHRIMPGeoSampleCSVDownload.do")
    public void doSHRIMPGeoSampleCSVDownload(@RequestParam(required = false, value = "serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "sampleName") String sampleName,
            @RequestParam(required = false, value = "igsn") String igsn,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "filter") String filter,
            @RequestParam(required = false, value = "maxFeatures",defaultValue = "200") Integer maxFeatures,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "outputFormat") String outputFormat,
            HttpServletResponse response)
                    throws Exception {

        //Build our filter details
        String filterString;
        response.setContentType("text/csv");
        OutputStream outputStream = response.getOutputStream();
        //Make our request and get it transformed
        InputStream results = null;

        try {
            if (filter != null && filter.indexOf("ogc:Filter")>0) { //Polygon filter
                filterString = filter.replace("gsmlp:shape","gml:location");
                results = wfsService.downloadCSVByPolygonFilter(serviceUrl, "tima:view_shrimp_geochronology_result", filterString, maxFeatures);
            } else{ //BBox or no filter
                //Build our filter details
                filterString = generateGeoSampleFilter(sampleName, igsn, bboxJson, optionalFilters);
                results = wfsService.downloadCSV(serviceUrl, "tima:view_shrimp_geochronology_result", filterString, maxFeatures);
            }
        } catch (Exception ex) {
            log.warn(String.format("Unable to request/transform WFS response for '%1$s' from '%2$s': %3$s", sampleName,
                    serviceUrl, ex));
            log.debug("Exception: ", ex);
        }
        FileIOUtil.writeInputToOutputStream(results, outputStream, 8 * 1024, true);
        outputStream.close();
    }


    /**
     * Utility function for generating an OGC filter for a TIMA simple feature
     *
     * @return
     */
    private String generateGeoSampleFilter(String name, String igsn, String bboxString,String optionalFilters) {
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxString);
        TIMAGeosampleFilter timaGeosampleFilter = new TIMAGeosampleFilter(name, igsn,optionalFilters);
        if (bbox == null) {
            return timaGeosampleFilter.getFilterStringAllRecords();
        } else {
            return timaGeosampleFilter.getFilterStringBoundingBox(bbox);
        }
    }

}
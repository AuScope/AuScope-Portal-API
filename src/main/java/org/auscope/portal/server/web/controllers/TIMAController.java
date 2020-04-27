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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller handles mineral data analysis service of certain sites
 */
@Controller
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
    @RequestMapping("/doTIMAGeoSampleCSVDownload.do")
    public void doTIMAGeoSampleCSVDownload(@RequestParam(required = false, value = "serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "sampleName") String sampleName,
            @RequestParam(required = false, value = "igsn") String igsn,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "outputFormat") String outputFormat,
            HttpServletResponse response)
                    throws Exception {

        //Build our filter details
        String filterString = generateGeoSampleFilter(sampleName, igsn, bboxJson, optionalFilters);

        //Make our request and get it transformed
        InputStream result = null;
        response.setContentType("text/csv");
        OutputStream outputStream = response.getOutputStream();
        try {
            result = wfsService.downloadCSV(serviceUrl, "tima:geosample_and_mineralogy", filterString,
                    maxFeatures);
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
    @RequestMapping("/doSHRIMPGeoSampleCSVDownload.do")
    public void doSHRIMPGeoSampleCSVDownload(@RequestParam(required = false, value = "serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "sampleName") String sampleName,
            @RequestParam(required = false, value = "igsn") String igsn,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "200") int maxFeatures,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "outputFormat") String outputFormat,
            HttpServletResponse response)
                    throws Exception {

        //Build our filter details
        String filterString = generateGeoSampleFilter(sampleName, igsn, bboxJson,optionalFilters);
        response.setContentType("text/csv");
        OutputStream outputStream = response.getOutputStream();
        //Make our request and get it transformed
        InputStream results = null;
        try {
            results = wfsService.downloadCSV(serviceUrl, "tima:view_shrimp_geochronology_result", filterString,
                    maxFeatures);
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

package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.WFSService;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.gsml.TIMAGeosampleFilter;
import org.auscope.portal.server.web.service.GenericService;
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
    private GenericService genericService;
    @Autowired
    public TIMAController(WFSService wfsService, GenericService genericService) {
        this.genericService = genericService;
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
    @RequestMapping("/doSHRIMPGeoSampleCSVDownload.do")
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

    @RequestMapping("doGeoSampleFilterStyle.do")
    private void doGeoSampleFilterStyle (
            HttpServletResponse response,
            @RequestParam(required = false, value = "layerName", defaultValue = "") String layerName,
            @RequestParam(required = false, value = "spatialPropertyName", defaultValue = "") String spatialPropertyName,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "styleType", defaultValue = "POINT") String styleType,
            @RequestParam(required = false, value = "color", defaultValue = "#FF0000") String styleColor,
            @RequestParam(required = false, value = "labelProperty") String labelProperty,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters)

                    throws Exception {
        log.error("layerName: "  + layerName);
        log.error("spatialPropertyName: "  + spatialPropertyName);
        log.error("bbox: "  + bboxJson);
        log.error("color: "  + styleColor);
        log.error("styleType: "  + styleType);
        log.error("labelProperty: "  + labelProperty);
        log.error("optionalFilters: "  + optionalFilters);
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        response.setContentType("text/xml");
        String filter = "";
        String style = "";
        if (optionalFilters != null) {
          filter = this.genericService.getFilter(bbox,optionalFilters);
        }
        if (labelProperty == null) {
          style = this.getStyle(filter, layerName, spatialPropertyName, styleType, styleColor);
        } else {
          style = this.getStyleWithLabel(filter, layerName, spatialPropertyName, styleType, styleColor, labelProperty);
        }
        ByteArrayInputStream styleStream = new ByteArrayInputStream(style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
    /**
     * Generates a broad SLD for symbolising a set of filters
     *
     * @param filter The filters to be symbolised
     * @param layerName 
     * @param styleType
     * @param styleColor 1-1 correspondance with filters - The CSS color for each filter to be symbolised with
     * @return
     */    
    public String getStyle(String filter, String layerName, String spatialPropertyName, String styleType, String styleColor) {
        String header = "<sld:StyledLayerDescriptor version=\"1.0.0\" xmlns:gsmlp=\"http://xmlns.geosciml.org/geosciml-portrayal/4.0\" xsi:schemaLocation=\"http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            + "<sld:NamedLayer>" + "<sld:Name>" + layerName + "</sld:Name>" + "<sld:UserStyle>" + "<sld:Name>portal-style</sld:Name>"
            + "<sld:FeatureTypeStyle>";
        String tail = "</sld:FeatureTypeStyle>" + "</sld:UserStyle>" + "</sld:NamedLayer>" + "</sld:StyledLayerDescriptor>";
  
        String rule = "";
      switch (styleType) {
        case "POLYGON":
            rule = "<sld:Rule>" + filter + "<sld:PolygonSymbolizer>" 
            + "<sld:Fill>" + "<sld:CssParameter name=\"fill\">" + styleColor + "</sld:CssParameter>" + "<sld:CssParameter name=\"fill-opacity\">0.1</sld:CssParameter>" + "</sld:Fill>"
            + "<sld:Stroke>" + "<sld:CssParameter name=\"stroke\">" + styleColor + "</sld:CssParameter>" + "<sld:CssParameter name=\"stroke-width\">0.1</sld:CssParameter>" + "</sld:Stroke>"
            + "</sld:PolygonSymbolizer>" + "</sld:Rule>";
            break;
        case "LINE":
            rule = "<sld:Rule>" + filter + "<sld:LineSymbolizer>" 
            + "<sld:Stroke>" + "<sld:CssParameter name=\"stroke\">" + styleColor + "</sld:CssParameter>" + "<sld:CssParameter name=\"stroke-width\">0.1</sld:CssParameter>" + "</sld:Stroke>"
            + "</sld:LineSymbolizer>" + "</sld:Rule>";
            break;
        case "POINT":
        default:
            rule = "<sld:Rule>" + filter + "<sld:PointSymbolizer>" + "<sld:Graphic>" + "<sld:Mark>" + "<sld:WellKnownName>circle</sld:WellKnownName>" 
                    + "<sld:Fill>" + "<sld:CssParameter name=\"fill\">" + styleColor + "</sld:CssParameter>"
                    + "<sld:CssParameter name=\"fill-opacity\">0.4</sld:CssParameter>" + "</sld:Fill>"
                    + "</sld:Mark>" + "<sld:Size>8</sld:Size>" + "</sld:Graphic>" + "</sld:PointSymbolizer>" + "</sld:Rule>";
            break;
        }
        String style = header + rule + tail;
        return style;
      } 
      
      /**
       * Generates a broad SLD for symbolising a set of filters
       *
       * @param filter The filters to be symbolised
       * @param layerName 
       * @param styleType
       * @param styleColor 1-1 correspondance with filters - The CSS color for each filter to be symbolised with
       * @return
       */    
      public String getStyleWithLabel(String filter, String layerName, String spatialPropertyName, String styleType, String styleColor, String labelProperty) {
        String header = "<sld:StyledLayerDescriptor version=\"1.0.0\" xmlns:gsmlp=\"http://xmlns.geosciml.org/geosciml-portrayal/4.0\" xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            + "<sld:NamedLayer>" + "<sld:Name>" + layerName + "</sld:Name>" + "<sld:UserStyle>" + "<sld:Name>portal-style</sld:Name>"
            + "<sld:FeatureTypeStyle>";
        String tail = "</sld:FeatureTypeStyle>" + "</sld:UserStyle>" + "</sld:NamedLayer>" + "</sld:StyledLayerDescriptor>";
  
        String rule = "";
        String textSymbolizer = "<TextSymbolizer><Label><ogc:PropertyName>" + labelProperty + "</ogc:PropertyName></Label><Font><CssParameter name=\"font-family\">Arial</CssParameter><CssParameter name=\"font-size\">12</CssParameter><CssParameter name=\"font-style\">normal</CssParameter><CssParameter name=\"font-weight\">normal</CssParameter></Font><LabelPlacement><PointPlacement><Displacement><DisplacementX>6</DisplacementX><DisplacementY>-6</DisplacementY></Displacement></PointPlacement></LabelPlacement><Fill><CssParameter name=\"fill\">#000000</CssParameter></Fill></TextSymbolizer>";
  
      switch (styleType) {
        case "POLYGON":
            rule = "<sld:Rule>" + filter + "<sld:PolygonSymbolizer>" 
            + "<sld:Fill>" + "<sld:CssParameter name=\"fill\">" + styleColor + "</sld:CssParameter>" + "<sld:CssParameter name=\"fill-opacity\">0.1</sld:CssParameter>" + "</sld:Fill>"
            + "<sld:Stroke>" + "<sld:CssParameter name=\"stroke\">" + styleColor + "</sld:CssParameter>" + "<sld:CssParameter name=\"stroke-width\">0.1</sld:CssParameter>" + "</sld:Stroke>"
            + "</sld:PolygonSymbolizer>" + textSymbolizer + "</sld:Rule>";
            break;
        case "LINE":
            rule = "<sld:Rule>" + filter + "<sld:LineSymbolizer>" 
            + "<sld:Stroke>" + "<sld:CssParameter name=\"stroke\">" + styleColor + "</sld:CssParameter>" + "<sld:CssParameter name=\"stroke-width\">0.1</sld:CssParameter>" + "</sld:Stroke>"
            + "</sld:LineSymbolizer>" + textSymbolizer + "</sld:Rule>";
            break;
        case "POINT":
        default:
            rule = "<sld:Rule>" + filter + "<sld:PointSymbolizer>" + "<sld:Graphic>" + "<sld:Mark>" + "<sld:WellKnownName>circle</sld:WellKnownName>" 
                    + "<sld:Fill>" + "<sld:CssParameter name=\"fill\">" + styleColor + "</sld:CssParameter>"
                    + "<sld:CssParameter name=\"fill-opacity\">0.4</sld:CssParameter>" + "</sld:Fill>"
                    + "</sld:Mark>" + "<sld:Size>8</sld:Size>" + "</sld:Graphic>" + "</sld:PointSymbolizer>" + textSymbolizer + "</sld:Rule>";
            break;
        }
        String style = header + rule + tail;
        return style;
      }     
}

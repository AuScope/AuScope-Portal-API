package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.web.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controller for handling requests for the borehole
 *
 * @author Lingbo Jiang
 *
 */
@RestController
@SecurityRequirement(name = "internal")
public class GenericController extends BasePortalController {
  private GenericService genericService;
    @Autowired
    public GenericController(GenericService genericService) {
        this.genericService = genericService;
    }
    /**
     * Handles getting the filter of the generic borehole queries. (If the bbox elements are specified, they will limit the output response to 200 records
     * implicitly)
     *
     * @param bbox
     * @param optionalFilters 
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET, value = {"/doGenericFilter.do"})
    public void doGenericFilter(
            HttpServletResponse response,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = true, value = "optionalFilters") String optionalFilters)

                    throws Exception {

        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        response.setContentType("text/xml");
        String filter = "";
        if (optionalFilters != null) {
          filter = this.genericService.getFilter(bbox,optionalFilters);
          filter = filter.replace("<ogc:Filter>","<ogc:Filter  xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\">");

          //<ogc:PropertyIsLike escapeChar="!" singleChar="#" matchCase="false" wildCard="*" ><ogc:PropertyName>DOCUMENT_SPEC_ID</ogc:PropertyName><ogc:Literal>*PR4535*</ogc:Literal></ogc:PropertyIsLike>
          // remove matchCase from propertyIsLike because getFeature don't like it.
          filter = filter.replace("PropertyIsLike escapeChar=\"!\" singleChar=\"#\" matchCase=\"false\"","PropertyIsLike escapeChar=\"!\" singleChar=\"#\" ");
        }
        ByteArrayInputStream styleStream = new ByteArrayInputStream(filter.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
    /**
     * Handles getting the style of the generic borehole filter queries. (If the bbox elements are specified, they will limit the output response to 200 records
     * implicitly)
     *
     * @param layerName
     *            the name of the mine to query for
     * @param spatialPropertyName
     * @param bbox
     * @param styleType
     * @param color
     * @param labelProperty
     * @param optionalFilters 
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET, value = {"/doGenericFilterStyle.do"})
    public void doGenericFilterStyle(
            HttpServletResponse response,
            @RequestParam(required = false, value = "layerName", defaultValue = "") String layerName,
            @RequestParam(required = false, value = "spatialPropertyName", defaultValue = "") String spatialPropertyName,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "styleType", defaultValue = "POINT") String styleType,
            @RequestParam(required = false, value = "color", defaultValue = "#FF0000") String styleColor,
            @RequestParam(required = false, value = "labelProperty") String labelProperty,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters)

                    throws Exception {

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

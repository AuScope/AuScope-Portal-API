package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.web.service.GeologicalProvincesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Controller for geological province service
 */
@Controller
public class GeologicalProvincesController extends BasePortalController {
    private GeologicalProvincesService geologicalProvincesService;
    @Autowired
    public GeologicalProvincesController(GeologicalProvincesService geologicalProvincesService) {
        this.geologicalProvincesService = geologicalProvincesService;
        //this.geologicalProvincesService = new GeologicalProvincesService();

    }

    /**
     * Handles getting the style of the geological provinces filter queries. (If the bbox elements are specified, they will limit the output response to 200 records
     * implicitly)
     *
     * @param serviceUrl
     * @param name
     * @throws Exception
     */
    @RequestMapping("/getGeologicalProvincestyle.do")
    public void getGeologicalProvincestyle(
            @RequestParam(required = false, value = "serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "name") String name,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            HttpServletResponse response) throws Exception {
        
        response.setContentType("text/xml");
        String style = "";        
        String filter = this.geologicalProvincesService.getGeologicalProvincesFilter(name,optionalFilters);
        style = getStyle("gml:ProvinceFullExtent", name, filter);
        ByteArrayInputStream styleStream = new ByteArrayInputStream(style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }

    public String getStyle(String layerName, String name, String filter) {
        String style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.0.0\" xmlns:gsmlp=\"http://xmlns.geosciml.org/geosciml-portrayal/4.0\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NamedLayer>"
                + "<Name>"
                + layerName
                + "</Name>"
                + "<UserStyle>"
                + "<Name>portal-style</Name>"
                + "<Title>portal-style</Title>"
                + "<IsDefault>1</IsDefault>"
                + "<FeatureTypeStyle>"
                + "<Rule>"
                + "<Name>Boreholes</Name>"
                + filter
                + "<sld:PolygonSymbolizer>"
                + "<sld:Fill>"
                + "<sld:CssParameter name=\"fill\">"
                + "<ogc:Function name=\"Recode\">"
                + "<ogc:Function name=\"IEEERemainder\">"
                + "<ogc:PropertyName>OBJECTID</ogc:PropertyName>"
                + "<ogc:Function name=\"parseInt\">"
                + "<ogc:Literal>9</ogc:Literal>"
                + "</ogc:Function>"
                + "</ogc:Function>"
                + "<ogc:Literal>-4</ogc:Literal>"
                + "<ogc:Literal>#8dd3c7</ogc:Literal>"
                + "<ogc:Literal>-3</ogc:Literal>"
                + "<ogc:Literal>#ffffb3</ogc:Literal>"
                + "<ogc:Literal>-2</ogc:Literal>"
                + "<ogc:Literal>#bebada</ogc:Literal>"
                + "<ogc:Literal>-1</ogc:Literal>"
                + "<ogc:Literal>#fb8072</ogc:Literal>"
                + "<ogc:Literal>0</ogc:Literal>"
                + "<ogc:Literal>#80b1d3</ogc:Literal>"
                + "<ogc:Literal>1</ogc:Literal>"
                + "<ogc:Literal>#fdb462</ogc:Literal>"
                + "<ogc:Literal>2</ogc:Literal>"
                + "<ogc:Literal>#b3de69</ogc:Literal>"
                + "<ogc:Literal>3</ogc:Literal>"
                + "<ogc:Literal>#fccde5</ogc:Literal>"
                + "<ogc:Literal>4</ogc:Literal>"
                + "<ogc:Literal>#d9d9d9</ogc:Literal>"
                + "</ogc:Function>"
                + "</sld:CssParameter>"
                + "<CssParameter name=\"fill-opacity\">0.4</CssParameter>"
                + "</sld:Fill>"
                + "<sld:Stroke>"
                + "<sld:CssParameter name=\"stroke\">#000000</sld:CssParameter>"
                + "<sld:CssParameter name=\"stroke-width\">0.5</sld:CssParameter>"
                + "</sld:Stroke>"
                + "</sld:PolygonSymbolizer>"
                + "<TextSymbolizer>"
                + "<Label>"
                + "<ogc:PropertyName>NAME</ogc:PropertyName>"
                + "</Label>"
				+ "<LabelPlacement>"
				+ "<PointPlacement>"
				+ "<AnchorPoint>"
				+ "<AnchorPointX>0.5</AnchorPointX>"
				+ "<AnchorPointY>0.5</AnchorPointY>"
				+ "</AnchorPoint>"
				+ "</PointPlacement>"
				+ "</LabelPlacement>"
                + "<Font>"
                + "<CssParameter name=\"font-family\">Arial</CssParameter>"
                + "<CssParameter name=\"font-size\">12</CssParameter>"
                + "<CssParameter name=\"font-style\">normal</CssParameter>"
                + "<CssParameter name=\"font-weight\">normal</CssParameter>"
                + "</Font>"
                + "<Fill>"
                + "<CssParameter name=\"fill\">#000000</CssParameter>"
                + "</Fill>"
                + "</TextSymbolizer>"
                + "</Rule>"
                + "</FeatureTypeStyle>"
                + "</UserStyle>" + "</NamedLayer>" + "</StyledLayerDescriptor>";      
        return style;
    }   
}


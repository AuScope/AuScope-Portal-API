package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.web.service.RemanentAnomaliesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 *  Controller for the Remanent Anomalies service
 */
@Controller
public class RemanentAnomaliesController extends BasePortalController {

    private RemanentAnomaliesService remanentAnomaliesService;

    public static final String REMANENT_ANOMALIES_TYPE = "RemAnom:Anomaly";

    @Autowired
    public RemanentAnomaliesController(RemanentAnomaliesService remanentAnomaliesService) {
        this.remanentAnomaliesService = remanentAnomaliesService;
    }


    /**
     * Handles getting the style of the Remanent Anomalies filter queries. (If the bbox elements are specified, they will limit the output response to 200
     * records implicitly)
     *
     * @param serviceURL
     *            URL for the anomaly service
     * @param name
     *            anomaly name
     * @param ARRAMin
     *            minimum Apparent_resultant_rotation_angle of the first model
     * @param ARRAMax
     *            maximum Apparent_resultant_rotation_angle of the first model
     * @param decMin
     *            minimum declination of the first model
     * @param decMax
     *            maximum declination of the first model
     * @param incMin
     *            minimum inclination of the first model
     * @param incMax
     *            maximum inclination of the first model
     * @param modelCountMin
     *            minimum number of models generated for this anomaly
     * @param modelCountMax
     *            maximum number of models generated for this anomaly
     * @param styleSwitch
     *            set to "models" to enable a special filter required for modelCount styling
     * @param optionalFilters
     *            Additional style filters
     * @throws Exception
     */
    @RequestMapping("/getRemanentAnomaliesStyle.do")
    public void doRemanentAnomaliesStyle(
            @RequestParam(required = false, value = "serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "name") String name,
            @RequestParam(required = false, value = "ARRAMin") Float ARRAMin,
            @RequestParam(required = false, value = "ARRAMax") Float ARRAMax,
            @RequestParam(required = false, value = "decMin") Float decMin,
            @RequestParam(required = false, value = "decMax") Float decMax,
            @RequestParam(required = false, value = "incMin") Float incMin,
            @RequestParam(required = false, value = "incMax") Float incMax,
            @RequestParam(required = false, value = "modelCountMin") Integer modelCountMin,
            @RequestParam(required = false, value = "modelCountMax") Integer modelCountMax,
            @RequestParam(required = false, value = "styleSwitch", defaultValue="default") String styleSwitch,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            HttpServletResponse response) throws Exception {

        //Vt: wms shouldn't need the bbox because it is tiled.
        FilterBoundingBox bbox = null;
        //String stylefilter=this.remanentAnomaliesService.getRemanentAnomaliesWithStyling(name); //VT:get filter from service
        Boolean modelsfilter = "models".equals(styleSwitch);
        String filter = this.remanentAnomaliesService.getRemanentAnomaliesFilter(name, ARRAMin, ARRAMax, decMin, decMax, incMin, incMax, modelCountMin, modelCountMax,modelsfilter,optionalFilters, bbox); //VT:get filter from service

        String style = this.getStyle(filter, "#000000",styleSwitch);

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }

    public String getStyle(String filter, String color, String styleSwitch) {

    	String style;
    	switch (styleSwitch) {
        case "ARRA":  style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.0.0\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd http://remanentanomalies.csiro.au/schemas/anomaly.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns=\"http://www.opengis.net/sld\" xmlns:RemAnom=\"http://remanentanomalies.csiro.au\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NamedLayer>" + "<Name>"
                + REMANENT_ANOMALIES_TYPE
                + "</Name>"
                + "<UserStyle>"
                + "<Name>ARRARemAnomStyle</Name>"
                + "<Title>ARRARemAnomStyle</Title>"
                + "<Abstract>ARRARemAnomStyle</Abstract>"
                + "<IsDefault>1</IsDefault>"
                + "<FeatureTypeStyle>"
                + "<Rule>"
                + "<Name>Anomaly</Name>"
                + filter
                + "<PointSymbolizer>"
                + "<Graphic>"
                + "<Mark>"
                + "<WellKnownName>circle</WellKnownName>"
                + "<Fill>"
                + "<CssParameter name=\"fill\">"
                + "<Function name=\"Categorize\">"
                + "<PropertyName>RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member[1]/RemAnom:Model/RemAnom:Apparent_resultant_rotation_angle</PropertyName>"
                + "<Literal>#0000ff</Literal>"
                + "<Literal>45</Literal>"
                + "<Literal>#00ff00</Literal>"
                + "<Literal>90</Literal>"
                + "<Literal>#ffff00</Literal>"
                + "<Literal>135</Literal>"
                + "<Literal>#ff0000</Literal>"
                + "</Function>"
                + "</CssParameter>"
                + "</Fill>"
                + "</Mark>"
                + "<Size>8</Size>"
                + "</Graphic>"
                + "</PointSymbolizer>"
                + "</Rule>"
                + "</FeatureTypeStyle>"
                + "</UserStyle>" + "</NamedLayer>" + "</StyledLayerDescriptor>";
                 break;
        case "inc":  style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.0.0\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd http://remanentanomalies.csiro.au/schemas/anomaly.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns=\"http://www.opengis.net/sld\" xmlns:RemAnom=\"http://remanentanomalies.csiro.au\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NamedLayer>" + "<Name>"
                + REMANENT_ANOMALIES_TYPE
                + "</Name>"
                + "<UserStyle>"
                + "<Name>incRemAnomStyle</Name>"
                + "<Title>incRemAnomStyle</Title>"
                + "<Abstract>incRemAnomStyle</Abstract>"
                + "<IsDefault>1</IsDefault>"
                + "<FeatureTypeStyle>"
                + "<Rule>"
                + "<Name>Anomaly</Name>"
                + filter
                + "<PointSymbolizer>"
                + "<Graphic>"
                + "<Mark>"
                + "<WellKnownName>circle</WellKnownName>"
                + "<Fill>"
                + "<CssParameter name=\"fill\">"
                + "<Function name=\"Categorize\">"
                + "<PropertyName>RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member[1]/RemAnom:Model/RemAnom:resultant_inclination</PropertyName>"
                + "<Literal>#0000ff</Literal>"
                + "<Literal>-45</Literal>"
                + "<Literal>#00ff00</Literal>"
                + "<Literal>0</Literal>"
                + "<Literal>#ffff00</Literal>"
                + "<Literal>45</Literal>"
                + "<Literal>#ff0000</Literal>"
                + "</Function>"
                + "</CssParameter>"
                + "</Fill>"
                + "</Mark>"
                + "<Size>8</Size>"
                + "</Graphic>"
                + "</PointSymbolizer>"
                + "</Rule>"
                + "</FeatureTypeStyle>"
                + "</UserStyle>" + "</NamedLayer>" + "</StyledLayerDescriptor>";
                 break;
        case "dec":  style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.0.0\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd http://remanentanomalies.csiro.au/schemas/anomaly.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns=\"http://www.opengis.net/sld\" xmlns:RemAnom=\"http://remanentanomalies.csiro.au\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NamedLayer>" + "<Name>"
                + REMANENT_ANOMALIES_TYPE
                + "</Name>"
                + "<UserStyle>"
                + "<Name>decRemAnomStyle</Name>"
                + "<Title>decRemAnomStyle</Title>"
                + "<Abstract>decRemAnomStyle</Abstract>"
                + "<IsDefault>1</IsDefault>"
                + "<FeatureTypeStyle>"
                + "<Rule>"
                + "<Name>Anomaly</Name>"
                + filter
                + "<PointSymbolizer>"
                + "<Graphic>"
                + "<Mark>"
                + "<WellKnownName>circle</WellKnownName>"
                + "<Fill>"
                + "<CssParameter name=\"fill\">"
                + "<Function name=\"Categorize\">"
                + "<PropertyName>RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member[1]/RemAnom:Model/RemAnom:resultant_declination</PropertyName>"
                + "<Literal>#0000ff</Literal>"
                + "<Literal>90</Literal>"
                + "<Literal>#00ff00</Literal>"
                + "<Literal>180</Literal>"
                + "<Literal>#ffff00</Literal>"
                + "<Literal>270</Literal>"
                + "<Literal>#ff0000</Literal>"
                + "</Function>"
                + "</CssParameter>"
                + "</Fill>"
                + "</Mark>"
                + "<Size>8</Size>"
                + "</Graphic>"
                + "</PointSymbolizer>"
                + "</Rule>"
                + "</FeatureTypeStyle>"
                + "</UserStyle>" + "</NamedLayer>" + "</StyledLayerDescriptor>";
                 break;
        case "models":  style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.0.0\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd http://remanentanomalies.csiro.au/schemas/anomaly.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns=\"http://www.opengis.net/sld\" xmlns:RemAnom=\"http://remanentanomalies.csiro.au\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NamedLayer>" + "<Name>"
                + REMANENT_ANOMALIES_TYPE
                + "</Name>"
                + "<UserStyle>"
                + "<Name>modelsRemAnomStyle</Name>"
                + "<Title>modelsRemAnomStyle</Title>"
                + "<Abstract>modelsRemAnomStyle</Abstract>"
                + "<IsDefault>1</IsDefault>"
                + "<FeatureTypeStyle>"
                + "<Rule>"
                + "<Name>Anomaly</Name>"
                + filter
                + "<PointSymbolizer>"
                + "<Graphic>"
                + "<Mark>"
                + "<WellKnownName>circle</WellKnownName>"
                + "<Fill>"
                + "<CssParameter name=\"fill\">"
                + "<Function name=\"Categorize\">"
                + "<Function name=\"attributeCount\">"
                + "<PropertyName>RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member/RemAnom:Model</PropertyName>"
                + "</Function>"
                + "<Literal>#000000</Literal>"
                + "<Literal>1</Literal>"
                + "<Literal>#0000ff</Literal>"
                + "<Literal>2</Literal>"
                + "<Literal>#00ff00</Literal>"
                + "<Literal>3</Literal>"
                + "<Literal>#ffff00</Literal>"
                + "<Literal>4</Literal>"
                + "<Literal>#ff0000</Literal>"
                + "</Function>"
                + "</CssParameter>"
                + "</Fill>"
                + "</Mark>"
                + "<Size>8</Size>"
                + "</Graphic>"
                + "</PointSymbolizer>"
                + "</Rule>"
                + "</FeatureTypeStyle>"
                + "</UserStyle>" + "</NamedLayer>" + "</StyledLayerDescriptor>";
                 break;
        default: style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.0.0\" "
                + "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd http://remanentanomalies.csiro.au/schemas/anomaly.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns=\"http://www.opengis.net/sld\" xmlns:RemAnom=\"http://remanentanomalies.csiro.au\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NamedLayer>" + "<Name>"
                + REMANENT_ANOMALIES_TYPE
                + "</Name>"
                + "<UserStyle>"
                + "<Name>defaultRemAnomStyle</Name>"
                + "<Title>defaultRemAnomStyle</Title>"
                + "<Abstract>defaultRemAnomStyle</Abstract>"
                + "<IsDefault>1</IsDefault>"
                + "<FeatureTypeStyle>"
                + "<Rule>"
                + "<Name>Anomaly</Name>"
                + filter
                + "<PointSymbolizer>"
                + "<Graphic>"
                + "<Mark>"
                + "<WellKnownName>circle</WellKnownName>"
                + "<Fill>"
                + "<CssParameter name=\"fill\">"
                + color
                + "</CssParameter>"
                + "</Fill>"
                + "</Mark>"
                + "<Size>8</Size>"
                + "</Graphic>"
                + "</PointSymbolizer>"
                + "</Rule>"
                + "</FeatureTypeStyle>"
                + "</UserStyle>" + "</NamedLayer>" + "</StyledLayerDescriptor>";
                 break;
    	}
    	

        return style;
    }

}

package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.core.util.SLDLoader;
import org.auscope.portal.server.MineralTenementServiceProviderType;
import org.auscope.portal.server.web.service.MineralTenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/*
 * Controller for Mineral Tenement services
 */
@RestController
@SecurityRequirement(name = "public")
public class MineralTenementController extends BasePortalController {

    private MineralTenementService mineralTenementService;
    private HashMap<String, String> MINERAL_TENEMENT_COLOUR_MAP = new HashMap<String, String>();    

    @Autowired
    public MineralTenementController(MineralTenementService mineralTenementService, WMSService wmsService) {
        this.mineralTenementService = mineralTenementService;
        MINERAL_TENEMENT_COLOUR_MAP.put("exploration", "#0000FF");
        MINERAL_TENEMENT_COLOUR_MAP.put("prospecting", "#00FFFF");
        MINERAL_TENEMENT_COLOUR_MAP.put("miscellaneous", "#00FF00");
        MINERAL_TENEMENT_COLOUR_MAP.put("mining", "#FFFF00");
        MINERAL_TENEMENT_COLOUR_MAP.put("licence", "#FF0000");
        
        MINERAL_TENEMENT_COLOUR_MAP.put("LIVE", "#0000FF");
        MINERAL_TENEMENT_COLOUR_MAP.put("CURRENT", "#00FF00");
        MINERAL_TENEMENT_COLOUR_MAP.put("PENDING", "#FF0000");
        
        MINERAL_TENEMENT_COLOUR_MAP.put("MineralTenement", "#0000FF");
        
        }


    /**
     * Returns mineral tenement features in CSV format
     *
     * @param serviceUrl
     *        URL to request mineral tenements features from
     * @param name
     *        name of mineral tenement layer
     * @param tenementType
     *        mineral tenement type
     * @param owner
     *        name of owner of mineral tenement
     * @param size
     *        size of mineral tenement area
     * @param endDate
     *        mineral tenement expiry date
     * @param bbox
     *        bounding box in JSON format
     * @return mineral tenement features in CSV format
     * @throws Exception
     */
    @GetMapping("/doMineralTenementCSVDownload.do")
    public void doMineralTenementCSVDownload(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "name") String name,
            @RequestParam(required = false, value = "tenementType") String tenementType,
            @RequestParam(required = false, value = "owner") String owner,
            @RequestParam(required = false, value = "size") String size,
            @RequestParam(required = false, value = "endDate") String endDate,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value="filter") String filter,
            @RequestParam(required = false, value="maxFeatures",defaultValue = "100000") Integer maxFeatures,
            HttpServletResponse response) throws Exception {


            OutputStream outputStream = response.getOutputStream();
            try {
                response.setContentType("text/csv");
                MineralTenementServiceProviderType mineralTenementServiceProviderType = MineralTenementServiceProviderType.parseUrl(serviceUrl);

                FilterBoundingBox box = FilterBoundingBox.attemptParseFromJSON(bboxJson);
                
                String filterString;

                InputStream result = null;
                if (filter != null && filter.indexOf("ogc:Filter")>0) { //Polygon filter
                    filterString = filter.replace("gsmlp:shape","mt:shape");
                    result = this.mineralTenementService.downloadCSVByPolygonFilter(serviceUrl, mineralTenementServiceProviderType.featureType(), filterString, maxFeatures);
                } else {
                    filterString = this.mineralTenementService.getMineralTenementFilter(box, null, mineralTenementServiceProviderType);
                    // Some ArcGIS servers do not support filters (not enabled?)
                    if (mineralTenementServiceProviderType == MineralTenementServiceProviderType.ArcGIS) {
                        filterString = "";
                    }

                    outputStream = response.getOutputStream();
                    result = this.mineralTenementService.downloadCSV(serviceUrl, mineralTenementServiceProviderType.featureType(), filterString, null);
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
     * Handles getting the style of the mineral tenement filter queries. (If the bbox elements are specified, they will limit the output response to 200 records
     * implicitly)
     *
     * @param serviceUrl
     *        URL of WMS mineral tenement service
     * @param name
     *        name of WMS mineral tenement layer
     * @param tenementType
     *        type of mineral tenement
     * @param owner
     *        name of mineral tenement owner
     * @param startDate
     *        mineral tenement grant date
     * @param endDate
     *        mineral tenement expiry date
     * @param ccProperty
     *        resulting image can be styled according to "TenementType" or "TenementStatus" or ""
     * @param optionalFilters
     *        optional filters which can be applied to stylesheet, xml format
     * @return xml stylesheet
     * @throws Exception
     */
    @GetMapping("/getMineralTenementStyle.do")
    public void doMineFilterStyle(
            @RequestParam(required = false, value = "serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "ccProperty", defaultValue="") String ccProperty,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            HttpServletResponse response) throws Exception {
        String style = "";
        ccProperty = org.auscope.portal.core.util.TextUtil.cleanQueryParameter(ccProperty);
        switch (ccProperty) {
        case "TenementType" :
            style = this.getStyle(false,ccProperty,"mt:MineralTenement", optionalFilters);
            break;
        case "TenementStatus":
            style = this.getStyle(false,ccProperty,"mt:MineralTenement", optionalFilters);
            break;
        default:
            MineralTenementServiceProviderType mineralTenementServiceProviderType = MineralTenementServiceProviderType.GeoServer;
            String filter = this.mineralTenementService.getMineralTenementFilter(null, optionalFilters, mineralTenementServiceProviderType); //VT:get filter from service
            style = this.getPolygonStyle(filter, mineralTenementServiceProviderType.featureType(), mineralTenementServiceProviderType.fillColour(), mineralTenementServiceProviderType.borderColour(),
                    mineralTenementServiceProviderType.styleName());
            break;
        }

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }

    public String getPolygonStyle(String filter, String name, String color, String borderColor, String styleName) throws IOException {

        Hashtable<String,String> valueMap = new Hashtable<String,String>();
        valueMap.put("name", name);
        valueMap.put("filter", filter);
        valueMap.put("color", color);
        valueMap.put("borderColor", borderColor);
        valueMap.put("styleName", styleName);


        return  SLDLoader.loadSLD("/org/auscope/portal/slds/MineralTenement_getPolygonStyle.sld", valueMap,false);

    }

    /**
     * Generate an SLD content for tenement filter
     * @param isLegend
     * @param ccProperty
     * @param layerName
     * @param optionalFilters
     * @return
     */
    public String getStyle(boolean isLegend,String ccProperty, String layerName, String optionalFilters) {
        String rules = getRules(isLegend,ccProperty, optionalFilters);
        String header="";
        if (isLegend) {
            header = "<StyledLayerDescriptor version=\"1.0.0\" " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> ";
        } else {
            header = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
                    "<StyledLayerDescriptor version=\"1.0.0\" " +
                    "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" " +
                    "xmlns=\"http://www.opengis.net/sld\" " +
                    "xmlns:mt=\"http://xmlns.geoscience.gov.au/mineraltenementml/1.0\" " +
                    "xmlns:ogc=\"http://www.opengis.net/ogc\" " +
                    "xmlns:ows=\"http://www.opengis.net/ows\" " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"> ";
        }
        String style =  header +
                    "<NamedLayer>" +
                    "<Name>" + layerName + "</Name>" +
                    "<UserStyle>" +
                    "<FeatureTypeStyle>" + rules + "</FeatureTypeStyle>" +
                    "</UserStyle>" +
                    "</NamedLayer>" +
                    "</StyledLayerDescriptor>";
        return style;
    }

    private String getRules(boolean isLegend, String ccProperty, String optionalFilters) {

        String rules = "";
        if (ccProperty.contains("TenementType")){
            rules += getRuleByName(isLegend,ccProperty,"exploration", optionalFilters);
            rules += getRuleByName(isLegend,ccProperty,"prospecting", optionalFilters);
            rules += getRuleByName(isLegend,ccProperty,"miscellaneous", optionalFilters);
            rules += getRuleByName(isLegend,ccProperty,"mining", optionalFilters);
            rules += getRuleByName(isLegend,ccProperty,"licence", optionalFilters);
        } else if (ccProperty.contains("TenementStatus")){
            rules += getRuleByName(isLegend,ccProperty,"LIVE", optionalFilters);
            rules += getRuleByName(isLegend,ccProperty,"CURRENT", optionalFilters);
            rules += getRuleByName(isLegend,ccProperty,"PENDING", optionalFilters);
        } else {
            rules = getRuleByName(isLegend,ccProperty,"Tenement", optionalFilters);
        }
        return rules;
    }

    private String getRuleByName(boolean isLegend,String ccProperty,String ruleName, String optionalFilters) {
        String filter = "";
        if (isLegend) {
            filter = "";
        } else {
            try {
                filter = this.mineralTenementService.getMineralTenementFilterCCProperty(optionalFilters, null, ccProperty, ruleName+ "*");
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }
        String color = MINERAL_TENEMENT_COLOUR_MAP.get(ruleName);
        String rule = "<Rule>" +
        "<Name>T</Name>" +
        "<Title>" + ruleName + "</Title>" +
        "<MaxScaleDenominator>4000000</MaxScaleDenominator>" +
        filter +
        "<PolygonSymbolizer>" +
        "<Fill>" +
        "<CssParameter name=\"fill\">" + color + "</CssParameter>" +
        "<CssParameter name=\"fill-opacity\">0.4</CssParameter>" +
        "</Fill>" +
        "<Stroke>" +
        "<CssParameter name=\"stroke\">" + color + "</CssParameter>" +
        "<CssParameter name=\"stroke-width\">0.5</CssParameter>" +
        "</Stroke>" +
        "</PolygonSymbolizer>" +
        "<TextSymbolizer>" +
        "<Label>" +
        "<ogc:Function name=\"strSubstringStart\">" +
        "<ogc:PropertyName>mt:name</ogc:PropertyName>" +
        "<ogc:Function name=\"parseInt\">" +
        "<ogc:Literal>27</ogc:Literal>" +
        "</ogc:Function>" +
        "</ogc:Function>" +
        "</Label>" +
        "<Font>" +
        "<CssParameter name=\"font-family\">Arial</CssParameter>" +
        "<CssParameter name=\"font-size\">12</CssParameter>" +
        "<CssParameter name=\"font-style\">normal</CssParameter>" +
        "<CssParameter name=\"font-weight\">normal</CssParameter>" +
        "</Font>" +
		"<LabelPlacement>" +
		"<PointPlacement>" +
		"<AnchorPoint>" +
		"<AnchorPointX>0.5</AnchorPointX>" +
		"<AnchorPointY>0.5</AnchorPointY>" +
		"</AnchorPoint>" +
		"</PointPlacement>" +
		"</LabelPlacement>" +
        "<Fill>" +
        "<CssParameter name=\"fill\">#000000</CssParameter>" +
        "</Fill>" +
        "</TextSymbolizer>" +
        "</Rule>" +
        "<Rule>" +
        "<Name>T</Name>" +
        "<Title>" + ruleName + "1</Title>" +
		"<MinScaleDenominator>4000000</MinScaleDenominator>" +
        filter +
        "<PolygonSymbolizer>" +
        "<Fill>" +
        "<CssParameter name=\"fill\">" + color + "</CssParameter>" +
        "<CssParameter name=\"fill-opacity\">0.4</CssParameter>" +
        "</Fill>" +
        "<Stroke>" +
        "<CssParameter name=\"stroke\">" + color + "</CssParameter>" +
        "<CssParameter name=\"stroke-width\">0.5</CssParameter>" +
        "</Stroke>" +
        "</PolygonSymbolizer>" +
        "</Rule>";
        return rule;

    }
}


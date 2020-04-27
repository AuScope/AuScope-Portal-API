package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Controller for Mineral Tenement services
 */
@Controller
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
    @RequestMapping("/doMineralTenementCSVDownload.do")
    public void doMineralTenementCSVDownload(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "name") String name,
            @RequestParam(required = false, value = "tenementType") String tenementType,
            @RequestParam(required = false, value = "owner") String owner,
            @RequestParam(required = false, value = "size") String size,
            @RequestParam(required = false, value = "endDate") String endDate,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            HttpServletResponse response) throws Exception {

        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);
        MineralTenementServiceProviderType mineralTenementServiceProviderType = MineralTenementServiceProviderType.parseUrl(serviceUrl);
        String filter = this.mineralTenementService.getMineralTenementFilter(name, tenementType, owner, size, endDate,
                bbox, null,  mineralTenementServiceProviderType);

        // Some ArcGIS servers do not support filters (not enabled?)
        if (mineralTenementServiceProviderType == MineralTenementServiceProviderType.ArcGIS) {
            filter = "";
        }
        response.setContentType("text/csv");
        OutputStream outputStream = response.getOutputStream();
        InputStream results = this.mineralTenementService.downloadCSV(serviceUrl, mineralTenementServiceProviderType.featureType(), filter, null);
        FileIOUtil.writeInputToOutputStream(results, outputStream, 8 * 1024, true);
        outputStream.close();

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
     * @param size
     *        size of mineral tenement area
     * @param endDate
     *        mineral tenement expiry date
     * @param ccProperty
     *        resulting image can be styled according to "TenementType" or "TenementStatus" or ""
     * @param optionalFilters
     *        optional filters which can be applied to stylesheet, xml format
     * @return xml stylesheet
     * @throws Exception
     */
    @RequestMapping("/getMineralTenementStyle.do")
    public void doMineFilterStyle(
            @RequestParam(required = false, value = "serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "name") String name,
            @RequestParam(required = false, value = "tenementType") String tenementType,
            @RequestParam(required = false, value = "owner") String owner,
            @RequestParam(required = false, value = "size") String size,
            @RequestParam(required = false, value = "endDate") String endDate,
            @RequestParam(required = false, value = "ccProperty", defaultValue="") String ccProperty,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            HttpServletResponse response) throws Exception {
        String style = "";
        ccProperty = org.auscope.portal.core.util.TextUtil.cleanQueryParameter(ccProperty);
        switch (ccProperty) {
        case "TenementType" :
            style = this.getStyle(false,ccProperty,"mt:MineralTenement",name, tenementType, owner, size, endDate);
            break;
        case "TenementStatus":
            style = this.getStyle(false,ccProperty,"mt:MineralTenement",name, tenementType, owner, size, endDate);
            break;
        default:
            MineralTenementServiceProviderType mineralTenementServiceProviderType = MineralTenementServiceProviderType.GeoServer;
            String filter = this.mineralTenementService.getMineralTenementFilter(name, tenementType, owner, size, endDate,null,optionalFilters, mineralTenementServiceProviderType); //VT:get filter from service
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
    public String getStyle(boolean isLegend,String ccProperty,String layerName,String name, String tenementType, String owner, String size, String endDate) {
        String rules = getRules(isLegend,ccProperty,name,  tenementType, owner,  size,  endDate) ;
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
    
    private String getRules(boolean isLegend,String ccProperty,String name, String tenementType,String owner, String size, String endDate) {

        String rules = "";
        if (ccProperty.contains("TenementType")){
            rules += getRuleByName(isLegend,ccProperty,"exploration",name,tenementType, owner, size, endDate);
            rules += getRuleByName(isLegend,ccProperty,"prospecting",name,tenementType, owner, size, endDate);
            rules += getRuleByName(isLegend,ccProperty,"miscellaneous",name,tenementType, owner, size, endDate);
            rules += getRuleByName(isLegend,ccProperty,"mining",name,tenementType, owner, size, endDate);
            rules += getRuleByName(isLegend,ccProperty,"licence",name,tenementType, owner, size, endDate);
        } else if (ccProperty.contains("TenementStatus")){
            rules += getRuleByName(isLegend,ccProperty,"LIVE",name,tenementType, owner, size, endDate);
            rules += getRuleByName(isLegend,ccProperty,"CURRENT",name,tenementType, owner, size, endDate);
            rules += getRuleByName(isLegend,ccProperty,"PENDING",name,tenementType, owner, size, endDate);            
        } else {
            rules = getRuleByName(isLegend,ccProperty,"Tenement",name,tenementType, owner, size, endDate);
        }
        return rules;
    }
    
    private String getRuleByName(boolean isLegend,String ccProperty,String ruleName,String name, String tenementType,String owner, String size, String endDate) {
        String filter = "";        
        if (isLegend) { 
            filter = "";
        } else {
            try {
                filter = this.mineralTenementService.getMineralTenementFilterCCProperty(name,tenementType, owner, size, endDate, null, ccProperty,ruleName+ "*");
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


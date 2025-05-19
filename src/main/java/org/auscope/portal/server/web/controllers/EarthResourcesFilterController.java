package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import jakarta.servlet.http.HttpServletResponse;

import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.uifilter.GenericFilterAdapter;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.core.util.SLDLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller that handles all Earth Resource related requests
 * <p>
 * It handles the following WFS features:
 * <ul>
 * <li>Mine</li>
 * <li>Mineral Occurrence</li>
 * <li>Mining Activity</li>
 * </ul>
 * </p>
 *
 * @author Jarek Sanders
 * @author Josh Vote
 */
@Controller
public class EarthResourcesFilterController extends BasePortalController {

    // ----------------------------------------------------- Instance variables

    private ServiceConfiguration serviceConfig;
    // ----------------------------------------------------------- Constructors

    @Autowired
    public EarthResourcesFilterController(ServiceConfiguration serviceConfig) {
        this.serviceConfig = serviceConfig;
    }
    
    public static final String MIN_OCCUR_VIEW_TYPE = "mo:MinOccView";

    // ------------------------------------------- Property Setters and Getters


    /**
     * Handles getting the style of the Earth Resource Lite Mine View filter queries. (If the bbox elements are specified, they will limit the output response to 200
     * records implicitly)
     *
     * @param optionalFilters
     * @param maxFeatures
     * @throws Exception
     */
    @RequestMapping("/getErlMineViewStyle.do")
    public void getErlMineViewStyle(
            HttpServletResponse response,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        // Get the mining activities
        GenericFilterAdapter filterObject = new GenericFilterAdapter(optionalFilters,"shape"); 
        String filter = filterObject.getFilterStringAllRecords();

        String style = this.getErLStyle(filter, "erl:MineView", "#a51f2f");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
    
    /**
     * Handles getting the style of the Earth Resource Lite Mineral Occurrence filter queries. (If the bbox elements are specified, they will limit the output response to 200
     * records implicitly)
     *
     * @param optionalFilters 
     *            
     * @param maxFeatures
     * @throws Exception
     */
    @RequestMapping("/getErlMineralOccurrenceViewStyle.do")
    public void getErlMineralOccurrenceViewStyle(
            HttpServletResponse response,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        // Get the mining activities
        GenericFilterAdapter filterObject = new GenericFilterAdapter(optionalFilters,"shape"); 
        String filter = filterObject.getFilterStringAllRecords();

        String style = this.getErLStyle(filter, "erl:MineralOccurrenceView", "#e02e16");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
    

    
    public String getErLStyle(String filter, String name, String color) throws IOException{
    	 Hashtable<String,String> valueMap = new Hashtable<String,String>();
         valueMap.put("filter", filter);
         valueMap.put("name", name);
         valueMap.put("color", color);

         return  SLDLoader.loadSLD("/org/auscope/portal/slds/erl_MineView.sld", valueMap,false);
    }

    public String getStyle(String serviceUrl, String filter, String name, String color) {
        //VT : This is a hack to get around using functions in feature chaining
        // https://jira.csiro.au/browse/SISS-1374
        // there are currently no available fix as wms request are made prior to
        // knowing app-schema mapping.

        String style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.0.0\" xmlns:mo=\"http://xmlns.geoscience.gov.au/minoccml/1.0\" "
                + getERMLNamespaces(serviceUrl) 
                + "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NamedLayer>" + "<Name>"
                + name + "</Name>"
                + "<UserStyle>"
                + "<Title>" + name + "</Title>"
                + "<FeatureTypeStyle>"
                + "<Rule>"
                + filter
                + "<PointSymbolizer>"
                + "<Graphic>"
                + "<Mark>"
                + "<WellKnownName>circle</WellKnownName>"
                + "<Fill>"
                + "<CssParameter name=\"fill\">" + color + "</CssParameter>"
                + "<CssParameter name=\"fill-opacity\">0.4</CssParameter>"
                + "</Fill>"
                + "<Stroke>"
                + "<CssParameter name=\"stroke\">" + color + "</CssParameter>"  
                + "<CssParameter name=\"stroke-width\">1</CssParameter>"
                + "</Stroke>"
                + "</Mark>"
                + "<Size>8</Size>"
                + "</Graphic>"
                + "</PointSymbolizer>"
                + "</Rule>"
                + "</FeatureTypeStyle>"
                + "</UserStyle>" + "</NamedLayer>" + "</StyledLayerDescriptor>";
        return style;
    }
    
    private String getERMLNamespaces(String serviceUrl) {
        String erNamespace;
        String gmlNamespace;
        String gsmlNamespace;
        
        ServiceConfigurationItem config = serviceConfig.getServiceConfigurationItem(serviceUrl);
        if (config != null && config.isGml32()) {
            // use ERML 2.0 namespaces
            erNamespace = "http://xmlns.earthresourceml.org/EarthResource/2.0";
            gmlNamespace = "http://www.opengis.net/gml/3.2";
            gsmlNamespace = "http://xmlns.geosciml.org/GeoSciML-Core/3.2";
        } else {
            // use ERML 1.1 namespaces
            erNamespace = "urn:cgi:xmlns:GGIC:EarthResource:1.1";
            gmlNamespace = "http://www.opengis.net/gml";
            gsmlNamespace = "urn:cgi:xmlns:CGI:GeoSciML:2.0";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("xmlns:er=\"").append(erNamespace).append("\" ");
        sb.append("xmlns:gml=\"").append(gmlNamespace).append("\" ");
        sb.append("xmlns:gsml=\"").append(gsmlNamespace).append("\" ");
        return sb.toString();
            
    }

}

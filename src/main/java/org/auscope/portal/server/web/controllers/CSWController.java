package org.auscope.portal.server.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.web.KnownFeatureTypeDefinition;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.apache.log4j.Logger;
import net.sf.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Mathew Wyatt
 * Date: 23/06/2009
 * Time: 4:57:12 PM
 */
@Controller
public class CSWController {

    private Logger logger = Logger.getLogger(getClass());
    private CSWService cswService;
    private PortalPropertyPlaceholderConfigurer portalPropertyPlaceholderConfigurer;
    private ArrayList knownTypes;
    private ArrayList iconUrls;

    /**
     * Construct
     * @param
     */
    @Autowired
    public CSWController(CSWService cswService,
                         PortalPropertyPlaceholderConfigurer portalPropertyPlaceholderConfigurer,
                         @Qualifier(value = "knownTypes")  ArrayList knownTypes,
                         @Qualifier(value = "googleMapIconUrls") ArrayList iconUrls) {

        this.cswService = cswService;
        this.portalPropertyPlaceholderConfigurer = portalPropertyPlaceholderConfigurer;
        this.knownTypes = knownTypes;




        this.iconUrls = iconUrls;
        
        
        String cswServiceUrl = portalPropertyPlaceholderConfigurer.resolvePlaceholder("HOST.cswservice.url");
        logger.debug("cswServiceUrl: " + cswServiceUrl);
        
        if (cswServiceUrl != null)
        	cswService.setServiceUrl(cswServiceUrl);
        
        try {
            cswService.updateRecordsInBackground();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * If we dont know the feature type, try and generate a description from what we have so it can be displayed anyway
     * @param record
     * @return
     */
    private KnownFeatureTypeDefinition generateTempKnownFeatureType(CSWRecord record, int recordIndex) {
    	
    	String iconUrl = "";
    	if (iconUrls.size() > 0) {
    		iconUrl = iconUrls.get(recordIndex % iconUrls.size()).toString();
    	}
    	
    	String description = record.getOnlineResourceDescription();
    	if (description == null || description.length() == 0)
    		description = record.getDataIdentificationAbstract();
    	
    	return new KnownFeatureTypeDefinition(record.getOnlineResourceName(),
    										  description,
    										  "",
    										  "getAllFeatures.do",
    										  iconUrl);
    }
    
    
    /**
     * Generates a JSONArray from a knownType definition and a list of records belonging to that known type
     * This is a helper function for any controller method that returns a set of complex records
     * 
     * Returns null if no response is needed
     * @param knownType
     * @param recordList
     * @return
     */
    private JSONArray generateComplexJSONResponse(KnownFeatureTypeDefinition knownType, CSWRecord[] records) {
    	JSONArray tableRow = new JSONArray();

        //add the name of the layer/feature type
        if (knownType.getDisplayName().length() == 0)
        	tableRow.add(knownType.getFeatureTypeName()); //we dont want to display an empty name
        else 
        	tableRow.add(knownType.getDisplayName());
        
        String servicesDescription = "Institutions: ";
        JSONArray serviceURLs = new JSONArray();

        //if there are no services available for this feature type then don't show it in the portal
        if(records.length == 0)
            return null;

        JSONArray contactOrgs = new JSONArray();
        
        for(CSWRecord record : records) {
            serviceURLs.add(record.getServiceUrl());
            servicesDescription += record.getContactOrganisation() + ", ";
            contactOrgs.add(record.getContactOrganisation());
            //serviceURLs.add("http://www.gsv-tb.dpi.vic.gov.au/AuScope-MineralOccurrence/services");
            //serviceURLs.add("http://auscope-services.arrc.csiro.au/deegree-wfs/services");
        }

        //add the abstract text to be shown as updateCSWRecords description
        tableRow.add(knownType.getDescription() + " " + servicesDescription);
        
        tableRow.add(contactOrgs);

        //add the service URL - this is the spring controller for handling minocc
        tableRow.add(knownType.getProxyUrl());

        //add the type: wfs or wms
        tableRow.add("wfs");

        //TODO: add updateCSWRecords proper unique id
        tableRow.add(knownType.hashCode());

        //add the featureType name (in case of updateCSWRecords WMS feature)
        tableRow.add(knownType.getFeatureTypeName());

        tableRow.add(serviceURLs);

        tableRow.add("true");
        tableRow.add("<img src='js/external/extjs/resources/images/default/grid/done.gif'>");

        tableRow.add("<img width='16' heigh='16' src='" + knownType.getIconUrl() + "'>");
        tableRow.add(knownType.getIconUrl());

        tableRow.add("<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/page_code.png'></a>");
        
        return tableRow;
    }
    
    /**
     * This controller queries a CSW for all of its WFS data records based on known feature types, then created a JSON response as a list
     * which can then be put into a table.
     *
     * Returns a JSON response with a data structure like so
     *
     * [
     * [title, description, proxyURL, serviceType, id, typeName, [serviceURLs], checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage],
     * [title, description, proxyURL, serviceType, id, typeName, [serviceURLs], checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage]
     * ]
     *
     * @return
     */
    @RequestMapping("/getComplexFeatures.do")
    public ModelAndView getComplexFeatures(HttpServletRequest request) throws Exception {
    	
        //update the records if need be
        cswService.updateRecordsInBackground();

        //the main holder for the items
        JSONArray dataItems = new JSONArray();

        for(Object known : knownTypes) {
            KnownFeatureTypeDefinition knownType = (KnownFeatureTypeDefinition)known;
            CSWRecord[] records = cswService.getWFSRecordsForTypename(knownType.getFeatureTypeName());
            
            JSONArray tableRow = generateComplexJSONResponse(knownType, records);
            if (tableRow == null)
            	break;

            dataItems.add(tableRow);
        }
        logger.debug(dataItems.toString());
        return new JSONModelAndView(dataItems);
    }
    
    /**
     * This controller queries a CSW for all of its WFS data records based on unknown feature types, then created a JSON response as a list
     * which can then be put into a table.
     *
     * Returns a JSON response with a data structure like so
     *
     * [
     * [title, description, proxyURL, serviceType, id, typeName, [serviceURLs], checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage],
     * [title, description, proxyURL, serviceType, id, typeName, [serviceURLs], checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage]
     * ]
     *
     * @return
     */
    @RequestMapping("/getGenericFeatures.do")
    public ModelAndView getGenericFeatures(HttpServletRequest request) throws Exception {
    	
        //update the records if need be
        cswService.updateRecordsInBackground();

        //the main holder for the items
        JSONArray dataItems = new JSONArray();

        //We need to combine records based on their record.getOnlineResourceName
        //therefore we store all the records in lists in a hashtable based on their getOnlineResourceName
        Map knownTypeMap = new HashMap();
        
        //Iterate our record list to build up our hashtable of resource types
        CSWRecord[] records = cswService.getWFSRecords();
        int index = 0;
        for (CSWRecord record : records) {
        	
        	//Ensure that this doesn't belong to a known type
        	KnownFeatureTypeDefinition knownType = null;
        	for (Object objToCheck : knownTypes) {
        		KnownFeatureTypeDefinition typeToCheck = (KnownFeatureTypeDefinition) objToCheck; 
        		if (typeToCheck.getFeatureTypeName().equals(record.getOnlineResourceName())) {
        			knownType = typeToCheck;
        			break;
        		}
        	}
        	if (knownType == null)
        		knownType = this.generateTempKnownFeatureType(record, index++);
        	else
        		continue;
        	
        	//Lookup the list of items for that known type (The known type generates its hashvalue based on record
        	ArrayList recordListForType = (ArrayList) knownTypeMap.get(knownType); 
        	if (recordListForType == null) {
        		recordListForType = new ArrayList(); //creating it if it hasnt already been created
        		knownTypeMap.put(knownType, recordListForType); 
        	}
        	
        	recordListForType.add(record);
        }
        
        //now we have a list of "known types" with each entry containing a list of records
        //The known types are however "fake" and only exist because we generated them on the fly
        Iterator it = knownTypeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            
            KnownFeatureTypeDefinition knownType = (KnownFeatureTypeDefinition) pairs.getKey();
            ArrayList recordList = (ArrayList) pairs.getValue();
            
            records = new CSWRecord[recordList.size()];
            records = (CSWRecord[]) recordList.toArray(records);
            
            //Add the mineral occurrence
            JSONArray tableRow = generateComplexJSONResponse(knownType, records);
            if (tableRow == null)
            	break;

            dataItems.add(tableRow);
        }

        logger.debug(dataItems.toString());
        return new JSONModelAndView(dataItems);
    }

    /**
     * Gets all WMS data records from a CSW service, and then creats a JSON response for the WMS layers list in the portal
     *
     * Returns a JSON response with a data structure like so
     *
     * [
     * [title, description, contactOrganisation, proxyURL, serviceType, id, typeName, [serviceURLs], checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage, opacity],
     * [title, description, contactOrganisation, proxyURL, serviceType, id, typeName, [serviceURLs], checked, statusImage, markerIconHtml, markerIconUrl, dataSourceImage, opacity]
     * ]
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/getWMSLayers.do")
    public ModelAndView getWMSLayers(HttpServletRequest request) throws Exception {
    	
        //update the records if need be
        cswService.updateRecordsInBackground();

        //the main holder for the items
        JSONArray dataItems = new JSONArray();

        CSWRecord[] records = cswService.getWMSRecords();

        for(CSWRecord record : records) {
        	
            //Add the mineral occurrence
            JSONArray tableRow = new JSONArray();

            //add the name of the layer/feature type
            tableRow.add(record.getServiceName());

            //add the abstract text to be shown as updateCSWRecords description
            tableRow.add(record.getDataIdentificationAbstract());
            
            //Add the contact organisation
            String org = record.getContactOrganisation();
            if (org == null || org.length() == 0)
            	org = "Unknown";
            tableRow.add(org);

            //wms dont need updateCSWRecords proxy url
            tableRow.add("");

            //add the type: wfs or wms
            tableRow.add("wms");

            //TODO: add updateCSWRecords proper unique id
            tableRow.add(record.hashCode());

            //add the featureType name (in case of updateCSWRecords WMS feature)
            tableRow.add(record.getOnlineResourceName());

            JSONArray serviceURLs = new JSONArray();

            serviceURLs.add(record.getServiceUrl());

            tableRow.add(serviceURLs);

            tableRow.element(true);
            tableRow.add("<img src='js/external/extjs/resources/images/default/grid/done.gif'>");

            tableRow.add("<a href='http://portal.auscope.org' id='mylink' target='_blank'><img src='img/picture_link.png'></a>");
            tableRow.add("1.0");

            dataItems.add(tableRow);
        }
        logger.debug(dataItems.toString());
        return new JSONModelAndView(dataItems);
    }
}
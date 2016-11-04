package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.auscope.portal.core.server.controllers.BaseCSWController;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.server.web.service.LocalCSWFilterService;
import org.auscope.portal.server.web.service.csw.FacetedSearchResponse;
import org.auscope.portal.server.web.service.csw.SearchFacet;
import org.auscope.portal.server.web.service.csw.SearchFacet.Comparison;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for handling search requests for a remote CSW
 * @author Josh Vote (CSIRO)
 *
 */
@Controller
public class CSWSearchController extends BaseCSWController {

    private LocalCSWFilterService filterService;
    private CSWCacheService cacheService;
    private WMSService wmsService;

    @Autowired
    public CSWSearchController(ViewCSWRecordFactory viewCSWRecordFactory, ViewKnownLayerFactory viewKnownLayerFactory, LocalCSWFilterService filterService, CSWCacheService cacheService, WMSService wmsService) {
        super(viewCSWRecordFactory, viewKnownLayerFactory);
        this.filterService = filterService;
        this.cacheService = cacheService;
        this.wmsService = wmsService;
    }

    @ResponseStatus(value =  org.springframework.http.HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    /**
     * Returns the list of keywords available for searching at a particular registry
     * @param serviceId
     * @return
     */
    @RequestMapping("facetedKeywords.do")
    public ModelAndView facetedKeywords(@RequestParam("serviceId") String serviceId) {
        return generateJSONResponseMAV(true, this.cacheService.getKeywordsForEndpoint(serviceId), "");
    }

    /**
     * Parses and performs a faceted search on a CSW
     * @param start 1 based index to start record search from
     * @param limit
     * @param serviceId
     * @param rawFields A list of all fields to be searched on. Must match the other raw params in length.
     * @param rawValues A list of all values to be searched against. Must match the other raw params in length.
     * @param rawTypes A list of all value types to be searched on. Must match the other raw params in length.
     * @param rawComparisons A list of all comparisons between field/value to be searched against. Must match the other raw params in length.
     * @return
     */
    @RequestMapping("facetedCSWSearch.do")
    public ModelAndView facetedCSWSearch(
            @RequestParam(value="start", required=false, defaultValue="1") Integer start,
            @RequestParam(value="limit", required=false, defaultValue="10") Integer limit,
            @RequestParam("serviceId") String serviceId,
            @RequestParam(value="field", required=false) String[] rawFields,
            @RequestParam(value="value", required=false) String[] rawValues,
            @RequestParam(value="type", required=false) String[] rawTypes,
            @RequestParam(value="comparison", required=false) String[] rawComparisons) {

        if (rawFields == null) {
            rawFields = new String[0];
        }

        if (rawValues == null) {
            rawValues = new String[0];
        }

        if (rawTypes == null) {
            rawTypes = new String[0];
        }

        if (rawComparisons == null) {
            rawComparisons = new String[0];
        }

        if (rawFields.length != rawValues.length || rawFields.length != rawTypes.length || rawFields.length != rawComparisons.length) {
            throw new IllegalArgumentException("field/value/type/comparison lengths mismatch");
        }

        if (limit > 20) {
            throw new IllegalArgumentException("Limit too high (max 20)");
        }

        //Parse our raw request info into a list of search facets
        List<SearchFacet<? extends Object>> facets = new ArrayList<SearchFacet<? extends Object>>();
        for (int i = 0; i < rawFields.length; i++) {
            Comparison cmp = null;
            switch(rawComparisons[i]) {
            case "gt":
                cmp = Comparison.GreaterThan;
                break;
            case "lt":
                cmp = Comparison.LessThan;
                break;
            case "eq":
                cmp = Comparison.Equal;
                break;
            default:
                throw new IllegalArgumentException("Unknown comparison type: " + rawComparisons[i]);
            }

            SearchFacet<? extends Object> newFacet = null;
            switch(rawTypes[i]) {
            case "servicetype":
                newFacet = new SearchFacet<OnlineResourceType>(Enum.valueOf(OnlineResourceType.class, rawValues[i]), rawFields[i], cmp);
                break;
            case "bbox":
                JSONObject jsonValue = JSONObject.fromObject(rawValues[i]);
                FilterBoundingBox bbox = FilterBoundingBox.parseFromValues("WGS:84", jsonValue.getDouble("northBoundLatitude"), jsonValue.getDouble("southBoundLatitude"), jsonValue.getDouble("eastBoundLongitude"), jsonValue.getDouble("westBoundLongitude"));

                if (bbox == null) {
                    throw new IllegalArgumentException("Unable to parse bounding box");
                }

                newFacet = new SearchFacet<FilterBoundingBox>(bbox, rawFields[i], cmp);
                break;
            case "date":
                DateTime value = new DateTime(Long.parseLong(rawValues[i]));
                newFacet = new SearchFacet<DateTime>(value, rawFields[i], cmp);
                break;
            case "string":
                newFacet = new SearchFacet<String>(rawValues[i], rawFields[i], cmp);
                break;
            }

            facets.add(newFacet);
        }

        //Make our request and then convert the records for transport to the view
        FacetedSearchResponse response;
        try {
            response = filterService.getFilteredRecords(serviceId, facets, start, limit);
            workaroundMissingNCIMetadata(response.getRecords());
        } catch (Exception ex) {
            log.error("Unable to filter records from remote service", ex);
            return generateJSONResponseMAV(false);
        }

        List<ModelMap> viewRecords = new ArrayList<ModelMap>(response.getRecords().size());
        for (CSWRecord record : response.getRecords()) {
            viewRecords.add(viewCSWRecordFactory.toView(record));
        }
        ModelMap mm = new ModelMap();
        mm.put("startIndex", response.getStartIndex());
        mm.put("nextIndex", response.getNextIndex());
        mm.put("records", viewRecords);

        return generateJSONResponseMAV(true, mm, "");
    }

    private boolean nameIsRewriteCandidate(String name) {
        return name.equalsIgnoreCase("Link to Web Map Service") ||
               name.equalsIgnoreCase("Link to Web Coverage Service") ||
               name.contains("S for dataset ");
    }

    /**
     * This is a workaround to address Online Resource "name" metadata lacking in records coming from NCI/GA.
     *
     *  Currently we receive a number of records where the Online Resource "name" element comes in the form
     *     + WCS for dataset UUID
     *     + WMS for dataset UUID
     *     + NCSS for dataset UUID
     *
     *  This is unhelpful and forces us to actually go back to the THREDDS instance for more info.
     *
     *  This method iterates the supplied records, searches for the above pattern in online resources and rewrites the online
     *  resources (using WMS GetCapabilities requests) if found. It will be assumed that all online resources matching the pattern
     *  can be rewritten to have the same layer/coverage/variable names.
     * @param records
     * @throws PortalServiceException
     */
    private void workaroundMissingNCIMetadata(List<CSWRecord> records) {
        for (CSWRecord record : records) {
            //Firstly figure out whether there are one or more "bad resources". We identify a bad resource
            //as a WMS with a name matching the pattern
            AbstractCSWOnlineResource layerNameSource = null;
            for (AbstractCSWOnlineResource wmsOr : record.getOnlineResourcesByType(OnlineResourceType.WMS)) {
                if (nameIsRewriteCandidate(wmsOr.getName())) {
                    layerNameSource = wmsOr;
                    break;
                }
            }
            if (layerNameSource == null) {
                continue;
            }

            //Now find our WMS resource that we will use to lookup all the layer/coverage/variable names
            List<String> layerNames = new ArrayList<String>();
            try {
                GetCapabilitiesRecord getCap = wmsService.getWmsCapabilities(layerNameSource.getLinkage().toString(), "1.3.0");
                for (GetCapabilitiesWMSLayerRecord layer : getCap.getLayers()) {
                    String name = layer.getName().trim();
                    if (!StringUtils.isEmpty(name)) {
                        layerNames.add(layer.getName());
                    }
                }
            } catch (Exception ex) {
                log.error("Unable to retrieve WMS capabilities. metadata will not be rewritten: " + ex.getMessage());
                log.debug("Exception:", ex);
                continue;
            }

            //Now seperate our online resources into those being rewritten and those being saved
            List<AbstractCSWOnlineResource> resourcesToSave = new ArrayList<AbstractCSWOnlineResource>();
            for (AbstractCSWOnlineResource or : record.getOnlineResources()) {
                switch (or.getType()) {
                case WCS:
                case WMS:
                case NCSS:
                    if (!nameIsRewriteCandidate(or.getName())) {
                        resourcesToSave.add(or);
                        break;
                    }

                    for (String layerName : layerNames) {
                        resourcesToSave.add(new CSWOnlineResourceImpl(or.getLinkage(), or.getProtocol(), layerName, or.getDescription()));
                    }

                    break;
                default:
                    resourcesToSave.add(or);
                }
            }

            record.setOnlineResources(resourcesToSave.toArray(new AbstractCSWOnlineResource[resourcesToSave.size()]));
        }
    }

    /**
     * Gets a list of CSWServiceItem objects that the portal is using for sources of CSWRecords.
     *
     * @return
     */
    @RequestMapping("/getCSWServices.do")
    public ModelAndView getCSWServices() {
        List<ModelMap> convertedServiceItems = new ArrayList<ModelMap>();

        //Simplify our service items for the view
        for (CSWServiceItem item : this.filterService.getCSWServiceItems()) {
            //VT: skip the loop if we intend to hide this from catalogue.
            if (item.getHideFromCatalogue()) {
                continue;
            }
            ModelMap map = new ModelMap();

            map.put("title", item.getTitle());
            map.put("id", item.getId());
            map.put("url", item.getServiceUrl());
            convertedServiceItems.add(map);
        }

        return generateJSONResponseMAV(true, convertedServiceItems, "");
    }
}

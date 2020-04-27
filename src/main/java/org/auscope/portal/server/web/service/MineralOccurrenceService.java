package org.auscope.portal.server.web.service;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.BaseWFSService;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.IFilter;
import org.auscope.portal.mineraloccurrence.MinOccurViewFilter;
import org.auscope.portal.mineraloccurrence.MineFilter;
import org.auscope.portal.mineraloccurrence.MineralOccurrenceFilter;
import org.auscope.portal.mineraloccurrence.MiningActivityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages mineral occurrence queries
 *
 * @version $Id$
 */
@Service
public class MineralOccurrenceService extends BaseWFSService {

    // -------------------------------------------------------------- Constants

    public static final String MINE_FEATURE_TYPE = "er:MiningFeatureOccurrence";
    public static final String MINERAL_OCCURRENCE_FEATURE_TYPE = "gsml:MappedFeature";
    public static final String MINING_ACTIVITY_FEATURE_TYPE = "er:MiningFeatureOccurrence";

    // ----------------------------------------------------- Instance variables

    // ----------------------------------------------------------- Constructors

    @Autowired
    public MineralOccurrenceService(HttpServiceCaller httpServiceCaller,
            WFSGetFeatureMethodMaker methodMaker) {
        super(httpServiceCaller, methodMaker);
    }

    /**
     * Utility for turning a filter and optional bounding box into a OGC filter string
     *
     * @param filter
     *            The filter
     * @param bbox
     *            [Optional] the spatial bounds to constrain the result set
     * @return
     */
    public static String generateFilterString(IFilter filter, FilterBoundingBox bbox) {
        String filterString = null;
        if (bbox == null) {
            filterString = filter.getFilterStringAllRecords();
        } else {
            filterString = filter.getFilterStringBoundingBox(bbox);
        }

        return filterString;
    }


	public String getMiningActivityFilter(String mineName, String startDate,
            String endDate, String oreProcessed, String producedMaterial,
            String cutOffGrade, String production,
            FilterBoundingBox bbox) {
        MiningActivityFilter filter = new MiningActivityFilter(mineName,
                startDate, endDate, oreProcessed, producedMaterial,
                cutOffGrade, production);
        return generateFilterString(filter, bbox);
    }

    public String getMineFilter(String mineName, FilterBoundingBox bbox, String optionalFilters) {
        MineFilter filter = new MineFilter(mineName, optionalFilters);
        return generateFilterString(filter, bbox);
    }
    

    public String getMineralOccurrenceFilter(String commodityName, FilterBoundingBox bbox,String optionalFilters) {
        MineralOccurrenceFilter filter = new MineralOccurrenceFilter(commodityName, "", "", "", "", "",optionalFilters);
        return generateFilterString(filter, bbox);
    }

    public String getMinOccurViewFilter(String commodityName, String minOreAmount, String minReserves,
            String minResources, FilterBoundingBox bbox) {
        MinOccurViewFilter filter = new MinOccurViewFilter(commodityName, minOreAmount, minReserves, minResources);
        return generateFilterString(filter, bbox);
    }

}

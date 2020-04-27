package org.auscope.portal.server.web.service;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.BaseWFSService;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.IFilter;
import org.auscope.portal.mineraloccurrence.MineralTenementCCFilter;
import org.auscope.portal.mineraloccurrence.MineralTenementFilter;
import org.auscope.portal.server.MineralTenementServiceProviderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages mineral tenement queries
 *
 * @author VictorTey
 * @version
 */
@Service
public class MineralTenementService extends BaseWFSService {

    @Autowired
    public MineralTenementService(
            HttpServiceCaller httpServiceCaller,
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

    public String getMineralTenementFilter(String name, String tenementType, String owner, String size, String endDate,
            FilterBoundingBox bbox,String optionalFilters, MineralTenementServiceProviderType mineralTenementServiceProviderType)
                    throws Exception {
        MineralTenementFilter filter = new MineralTenementFilter(name, tenementType, owner, size, endDate,null,optionalFilters, mineralTenementServiceProviderType);
        return generateFilterString(filter, bbox);
    }
    public String getMineralTenementFilterCCProperty(String name, String tenementType, String owner, String size, String endDate,
            FilterBoundingBox bbox,String ccProperty,String ccPropertyValue)
                    throws Exception {
        MineralTenementCCFilter filter = new MineralTenementCCFilter(name, tenementType, owner, size, endDate);
        filter.addCCPropertyInFilter(ccProperty,ccPropertyValue);
        return generateFilterString(filter, bbox);
    }

}

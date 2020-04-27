package org.auscope.portal.server.web.service;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.BaseWFSService;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.IFilter;
import org.auscope.portal.remanentanomalies.RemanentAnomaliesFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages Remanent Anomaly queries
 *
 * @author Peter Warren
 * @version
 */
@Service
public class RemanentAnomaliesService extends BaseWFSService {

    @Autowired
    public RemanentAnomaliesService(
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

    public String getRemanentAnomaliesFilter(String name, Float ARRAMin, Float ARRAMax, Float decMin, Float decMax, Float incMin, Float incMax, Integer modelCountMin, Integer modelCountMax, Boolean modelsfilter, String optionalFilters, FilterBoundingBox bbox)
            throws Exception {
        RemanentAnomaliesFilter filter = new RemanentAnomaliesFilter(name, ARRAMin, ARRAMax, decMin, decMax, incMin, incMax, modelCountMin, modelCountMax,modelsfilter, optionalFilters);
        return generateFilterString(filter, bbox);
    }


}

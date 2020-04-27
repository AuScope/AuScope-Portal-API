package org.auscope.portal.server.web.service;

import javax.naming.OperationNotSupportedException;

import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.BaseWFSService;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.IFilter;
import org.auscope.portal.remanentanomalies.RemanentAnomaliesAutoSearchFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages Remanent Anomaly AutoSearch queries
 *
 * @author Peter Warren
 * @version
 */
@Service
public class RemanentAnomaliesAutoSearchService extends BaseWFSService {

    @Autowired
    public RemanentAnomaliesAutoSearchService(
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

    public String getRemanentAnomaliesAutoSearchFilter(FilterBoundingBox bbox) {
        RemanentAnomaliesAutoSearchFilter filter = new RemanentAnomaliesAutoSearchFilter();
        return generateFilterString(filter, bbox);
    }
}

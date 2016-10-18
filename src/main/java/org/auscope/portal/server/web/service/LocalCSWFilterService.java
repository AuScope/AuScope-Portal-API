package org.auscope.portal.server.web.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.core.services.methodmakers.filter.csw.CSWGetDataRecordsFilter.KeywordMatchType;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWGetRecordResponse;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.server.web.service.csw.FacetedSearchResponse;
import org.auscope.portal.server.web.service.csw.SearchFacet;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An wrapper around CSWFilterService that allows local/client side
 * filtering at the expense of a more limited API (no pagination)
 * and slower performance.
 * @author Josh Vote (CSIRO)
 *
 */
@Service
public class LocalCSWFilterService {

    public static final int DEFAULT_PAGE_SIZE = 100;

    private CSWFilterService filterService;
    private final Log log = LogFactory.getLog(getClass());
    private int pageSize = DEFAULT_PAGE_SIZE;

    @Autowired
    public LocalCSWFilterService(CSWFilterService filterService) {
        this.filterService = filterService;
    }

    /**
     * The amount of records requested from a CSW in a batch for filtering operations. This is always static
     * as local filtering means that we can't ever know how many we'll need. Defaults to DEFAULT_PAGE_SIZE
     * @param pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * The amount of records requested from a CSW in a batch for filtering operations. This is always static
     * as local filtering means that we can't ever know how many we'll need. Defaults to DEFAULT_PAGE_SIZE
     * @param pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Iterates facets and for each item either a) Adds the facet to remoteFilter or b) returns the facet in the return list for use
     * in a local filter later.
     * @param facets
     * @param remoteFilter
     * @return
     */
    private List<SearchFacet<? extends Object>> seperateFacets(List<SearchFacet<? extends Object>> facets, CSWGetDataRecordsFilter remoteFilter) {
        List<SearchFacet<? extends Object>> localFacets = new ArrayList<SearchFacet<? extends Object>>();
        for (SearchFacet<? extends Object> facet : facets) {
            switch(facet.getField()) {
            case "anytext":
                remoteFilter.setAnyText((String)facet.getValue());
                break;
            case "bbox":
                remoteFilter.setSpatialBounds((FilterBoundingBox)facet.getValue());
                break;
            case "keyword":
                String kw = (String)facet.getValue();
                String[] keywords = ArrayUtils.add(remoteFilter.getKeywords(), kw);
                remoteFilter.setKeywords(keywords);
                remoteFilter.setKeywordMatchType(KeywordMatchType.All);
                break;
            case "datefrom":
                remoteFilter.setModifiedDateFrom((DateTime)facet.getValue());
                break;
            case "dateto":
                remoteFilter.setModifiedDateTo((DateTime)facet.getValue());
                break;
            default:
                localFacets.add(facet);
                break;
            }
        }
        return localFacets;
    }

    /**
     * Tests a CSWRecord against a given search facet
     * @param record
     * @param facet
     * @return true if the record passes the facet.
     */
    private boolean recordPassesSearchFacet(CSWRecord record, SearchFacet<? extends Object> facet) {
        switch(facet.getField()) {
        case "servicetype":
            OnlineResourceType type = (OnlineResourceType) facet.getValue();
            return record.getOnlineResourcesByType(type).length > 0;
        default:
            log.error("Unable to local filter on field: " + facet.getField());
            return false;
        }
    }

    /**
     * Enumerates CSWRecords from source testing each against the search facets. Any passing record is copied to sink.
     * @param source
     * @param sink
     * @param facets
     * @param maxRecords At most this many records will be copied
     * @return The 0 based index of the LAST examined record from source or -1
     */
    private int performLocalFilter(List<CSWRecord> source, List<CSWRecord> sink, List<SearchFacet<? extends Object>> facets, int maxRecords) {
        int lastIndex = -1;
        int recordsCopied = 0;

        for (int i = 0; i < source.size(); i++) {
            CSWRecord record = source.get(i);
            lastIndex = i;
            boolean allPassing = true;
            for (SearchFacet<? extends Object> facet : facets) {
                if (!recordPassesSearchFacet(record, facet)) {
                    allPassing = false;
                    break;
                }
            }

            if (allPassing) {
                sink.add(record);
                if (++recordsCopied >= maxRecords) {
                    break;
                }
            }
        }

        return lastIndex;
    }

    /**
     * Given a specific service to query with a set of local/remote search facets. Perform a full filter until maxRecords are received or the remote CSW runs out of records. This can result
     * in many calls to the remote CSW if the local portion of the filter is quite specific and the remote portion of very permissive.
     * @param serviceId
     * @param facets
     * @param startIndex
     * @param maxRecords
     * @return
     * @throws PortalServiceException
     */
    public FacetedSearchResponse getFilteredRecords(String serviceId, List<SearchFacet<? extends Object>> facets, int startIndex, int maxRecords) throws PortalServiceException {
        //Build our remote filter and keep track of what facets need to be done locally
        CSWGetDataRecordsFilter remoteFilter = new CSWGetDataRecordsFilter();
        List<SearchFacet<? extends Object>> localFacets = seperateFacets(facets, remoteFilter);

        //Keep getting pages of data until we serve up enough records
        FacetedSearchResponse result = new FacetedSearchResponse();
        result.setStartIndex(startIndex);
        result.setRecords(new ArrayList<CSWRecord>(maxRecords));
        int recordsRemaining, currentStartIndex = startIndex;
        while((recordsRemaining = (maxRecords - result.getRecords().size())) > 0) {

            //If we are dealing with a purely remote filter we can just request the exact number of records
            int recsToRequest = this.pageSize;
            if (localFacets.size() == 0) {
                recsToRequest = Math.min(this.pageSize, recordsRemaining);
            }

            //If this starts spamming requests we can always look at upping the page size every iteration.
            CSWGetRecordResponse cswResponse = filterService.getFilteredRecords(serviceId, remoteFilter, recsToRequest, currentStartIndex);

            //Filter our response and copy passing records to our result list
            int lastIndex;
            if (localFacets.size() == 0) {
                result.getRecords().addAll(cswResponse.getRecords());
                lastIndex = cswResponse.getRecords().size() - 1;
            } else {
                lastIndex = performLocalFilter(cswResponse.getRecords(), result.getRecords(), localFacets, recordsRemaining);
            }

            if (lastIndex < 0) {
                //If we got an empty response then we are done.
                result.setNextIndex(0);
                break;
            } else {
                currentStartIndex += lastIndex + 1;
                result.setNextIndex(currentStartIndex);
            }

            //If there are no more records to retrieve, abort early
            if (cswResponse.getNextRecord() <= 0) {
                result.setNextIndex(0);
                break;
            }
        }

        return result;
    }

    /**
     * Returns the list of internal CSWServiceItems that powers this service (passes straight through to underlying CSWFilterService
     *
     * @return
     */
    public CSWServiceItem[] getCSWServiceItems() {
        return filterService.getCSWServiceItems();
    }
}

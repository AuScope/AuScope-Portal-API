package org.auscope.portal.remanentanomalies;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.AbstractFilter;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;

public class RemanentAnomaliesAutoSearchFilter extends AbstractFilter {
    List<String> fragments;

    /**
     * This object will build an empty filter
     */
    public RemanentAnomaliesAutoSearchFilter() {
        fragments = new ArrayList<String>();
    }

    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()])));
    }

    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {

        List<String> localFragment = new ArrayList<String>(fragments);
        localFragment.add(this.generateBboxFragment(bbox, "RemAnomAutoSearch:shape"));

        return this.generateFilter(this.generateAndComparisonFragment(localFragment.toArray(new String[localFragment
                .size()])));
    }
}

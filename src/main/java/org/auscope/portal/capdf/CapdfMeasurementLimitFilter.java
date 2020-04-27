package org.auscope.portal.capdf;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.AbstractFilter;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;

/**
 * Class that represents ogc:Filter markup for capricorn distal footprint queries
 *
 * @author Victor Tey
 * @version
 */
public class CapdfMeasurementLimitFilter extends AbstractFilter {
    List<String> fragments;

    /**
     * Construct an object the aids in generating filter object for the capricorn distal measurement limit filter
     * 
     * @param group
     */
    public CapdfMeasurementLimitFilter(String group) {

        fragments = new ArrayList<String>();
        if (group != null && !group.isEmpty()) {
            fragments.add(this.generatePropertyIsLikeFragment("group", group));
        }

    }

    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()])));
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        List<String> localFragment = new ArrayList<String>(fragments);
        localFragment.add(this.generateBboxFragment(bbox, "geom"));

        return this.generateFilter(this.generateAndComparisonFragment(localFragment.toArray(new String[localFragment
                .size()])));
    }

}

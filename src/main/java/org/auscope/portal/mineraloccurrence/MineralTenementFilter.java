package org.auscope.portal.mineraloccurrence;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.uifilter.GenericFilter;
import org.auscope.portal.core.uifilter.GenericFilterAdapter;
import org.auscope.portal.server.MineralTenementServiceProviderType;

/**
 * Class that represents ogc:Filter markup for mt:mineralTenement queries
 *
 * @author Victor Tey
 * @version
 */
public class MineralTenementFilter extends GenericFilter {
    List<String> fragments;

    /**
     * Given a mine name, this object will build a filter to a wild card search for mine names
     * Extended to support mt:status as well.
     * @param mineName
     *            the main name
     */
    public MineralTenementFilter(String optionalFilters, MineralTenementServiceProviderType mineralTenementServiceProviderType) {
        super(optionalFilters);
        if (mineralTenementServiceProviderType == null) {
            mineralTenementServiceProviderType = MineralTenementServiceProviderType.GeoServer;
        }
        fragments = new ArrayList<String>();

        if(optionalFilters == null || optionalFilters.isEmpty()){
            GenericFilterAdapter filterObject = new GenericFilterAdapter(optionalFilters, "mt:shape");
            fragments.add(filterObject.getFilterStringAllRecords());
        }else{
            fragments = this.generateParameterFragments();

        }
    }
    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()])));
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {

        List<String> localFragment = new ArrayList<String>(fragments);
        localFragment.add(this.generateBboxFragment(bbox, "mt:shape"));

        return this.generateFilter(this.generateAndComparisonFragment(localFragment.toArray(new String[localFragment
                                                                                                       .size()])));
    }

    public String getFilterWithAdditionalStyle() {

        List<String> localFragment = new ArrayList<String>(fragments);
        localFragment.add(this.generateOrComparisonFragment(this.generatePropertyIsLikeFragment("mt:status", "Active"),
                this.generatePropertyIsLikeFragment("mt:status", "GRANTED")));

        return this.generateFilter(this.generateAndComparisonFragment(localFragment.toArray(new String[localFragment
                                                                                                       .size()])));
    }

}

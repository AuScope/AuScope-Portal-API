package org.auscope.portal.geologicalprovinces;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.uifilter.GenericFilter;

/**
 * Class that represents ogc:Filter markup for geologicalprovinces queries
 *
 * @author Lingbo Jiang
 * @version
 */
public class GeologicalProvincesFilter extends GenericFilter {
    List<String> fragments;

    /**
     * Given a mine name, this object will build a filter to a wild card search for mine names
     * Extended to support mt:status as well.
     * @param mineName
     *            the main name
     */
    public GeologicalProvincesFilter(String name, String optionalFilters) {
        super(optionalFilters);

        fragments = new ArrayList<String>();

        if(optionalFilters == null || optionalFilters.isEmpty()){
            if (name != null && !name.isEmpty()) {
                fragments.add(this.generatePropertyIsLikeFragment("NAME", "*" + name + "*"));
            }
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
        localFragment.add(this.generateBboxFragment(bbox, "the_geom"));

        return this.generateFilter(this.generateAndComparisonFragment(localFragment.toArray(new String[localFragment
                                                                                                       .size()])));
    }


}

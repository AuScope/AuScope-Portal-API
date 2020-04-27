package org.auscope.portal.mineraloccurrence;

import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.uifilter.GenericFilter;

/**
 * Class that represents ogc:Filter markup for er:mine queries
 *
 * @author Mat Wyatt
 * @version $Id$
 */
public class MineFilter extends GenericFilter {
    private String filterFragment;

    /**
     * Given a mine name, this object will build a filter to a wild card search for mine names
     *
     * @param mineName
     *            the main name
     */
    public MineFilter(String mineName,String optionalFilters) {
        super(optionalFilters);
        // Check the NON-Feature Chained name - faster!
        if (mineName != null && mineName.length() > 0) {
            // Geoserver bug - cant search on duplicate attributes -
            // once resolved delete fragment and change to this: SISS-912
            // this.filterFragment = this.generatePropertyIsLikeFragment("er:specification/er:Mine/gml:name", mineName);
            this.filterFragment = this.generatePropertyIsLikeFragment(
                    "er:specification/er:Mine/er:mineName/er:MineName/er:mineName", mineName);
        }else if(optionalFilters!=null && !optionalFilters.isEmpty()){
            List<String> frags = this.generateParameterFragments();
            if(frags.size()>0){
                this.filterFragment = this.generateParameterFragments().get(0);
            }else{
                this.filterFragment = this.generatePropertyIsLikeFragment("er:specification/er:Mine/gml:name", "*");
            }
        }
        // Ensure a MFO query always returns a type mine!
        else {
            this.filterFragment = this.generatePropertyIsLikeFragment("er:specification/er:Mine/gml:name", "*");
        }
    }

    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.filterFragment);
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {
        return this.generateFilter(
                this.generateAndComparisonFragment(
                        this.generateBboxFragment(bbox, "er:location"),
                        this.filterFragment));
    }

}

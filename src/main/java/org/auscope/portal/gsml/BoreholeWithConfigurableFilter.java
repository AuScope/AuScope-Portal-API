package org.auscope.portal.gsml;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.uifilter.GenericFilter;

/**
 * @author Tannu Gupta
 *
 * @version $Id$
 */

public class BoreholeWithConfigurableFilter extends GenericFilter {

    protected List<String> restrictToIDList;

    // -------------------------------------------------------------- Constants

    // ----------------------------------------------------------- Constructors

    public BoreholeWithConfigurableFilter(String optionalFilters,List<String> restrictToIDList) {
        super(optionalFilters);
        this.restrictToIDList = restrictToIDList;
    }

    // --------------------------------------------------------- Public Methods

    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generateFilterFragment());
    }


    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {

        return this
                .generateFilter(this.generateAndComparisonFragment(
                        this.generateBboxFragment(bbox,
                                "gsml:collarLocation/gsml:BoreholeCollar/gsml:location"),
                                this.generateFilterFragment()));
    }

    // -------------------------------------------------------- Private Methods

    /**
     * we had to override generateFilterFragment just for NVCL as we need to filter by id for nvcl boreholes
     */
    @Override
    protected String generateFilterFragment() {
        List<String> parameterFragments = generateParameterFragments();

        if (this.restrictToIDList != null && !this.restrictToIDList.isEmpty()) {
            List<String> idFragments = new ArrayList<String>();
            for (String id : restrictToIDList) {
                if (id != null && id.length() > 0) {
                    idFragments.add(generateFeatureIdFragment("gsml.borehole." + id));
                }
            }
            parameterFragments.add(this
                    .generateOrComparisonFragment(idFragments
                            .toArray(new String[idFragments.size()])));
        }

        return this.generateAndComparisonFragment(this
                .generateAndComparisonFragment(parameterFragments
                        .toArray(new String[parameterFragments.size()])));
    }
}

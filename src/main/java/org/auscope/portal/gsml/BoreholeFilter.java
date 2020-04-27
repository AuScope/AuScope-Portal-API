package org.auscope.portal.gsml;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.uifilter.GenericFilter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author Tannu Gupta
 *
 * @version $Id$
 */

public class BoreholeFilter extends GenericFilter {

    protected String boreholeName;
    protected String custodian;
    protected String dateOfDrillingStart;
    protected String dateOfDrillingEnd;
    protected List<String> restrictToIDList;

    // -------------------------------------------------------------- Constants

    // ----------------------------------------------------------- Constructors

    public BoreholeFilter(String boreholeName, String custodian,
            String dateOfDrillingStart, String dateOfDrillingEnd,List<String> restrictToIDList,String optionalFilters) {
        super(optionalFilters);

        this.boreholeName = boreholeName;
        this.custodian = custodian;
        this.dateOfDrillingStart = dateOfDrillingStart;
        this.dateOfDrillingEnd = dateOfDrillingEnd;
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
    @Override
    protected String generateFilterFragment() {
        String optionalFilters = this.getxPathFilters();
        List<String> parameterFragments =null;
        if(optionalFilters == null || optionalFilters.isEmpty()){
            parameterFragments = new ArrayList<String>();
            if (boreholeName != null && !boreholeName.isEmpty()) {
                parameterFragments.add(this.generatePropertyIsLikeFragment(
                        "gml:name", this.boreholeName));
            }

            if (custodian != null && !custodian.isEmpty()) {
                parameterFragments
                .add(this
                        .generatePropertyIsLikeFragment(
                                "gsml:indexData/gsml:BoreholeDetails/gsml:coreCustodian/@xlink:title",
                                this.custodian));
            }


            if (dateOfDrillingStart != null && !dateOfDrillingStart.isEmpty()
                    && dateOfDrillingEnd != null && !dateOfDrillingEnd.isEmpty()) {
                // AUS-2595 Due to the date compare does not like the
                // PropertyIsLike, it was change to use PropertyIsGreaterThan & PropertyIsLessThan.
                DateTimeFormatter formatter = DateTimeFormat
                        .forPattern("yyyy-MM-dd");
                DateTime dtStart = formatter
                        .parseDateTime(this.dateOfDrillingStart);
                DateTime dtEnd = formatter.parseDateTime(this.dateOfDrillingEnd);
                // LJ: Need to minus 1 second for startDate to cover the time of
                // 00:00:00
                // Need to plus 1 second for endDate to cover the time of 00:00:00
                dtStart = dtStart.minusSeconds(1);
                dtEnd = dtEnd.plusSeconds(1);
                DateTimeFormatter outFormatter = DateTimeFormat
                        .forPattern("yyyy-MM-dd HH:mm:ss");
                String utcDateofDrillingStart = outFormatter.print(dtStart);
                String utcDateofDrillingEnd = outFormatter.print(dtEnd);
                parameterFragments.add(this.generateDatePropertyIsGreaterThan(
                        "gsml:indexData/gsml:BoreholeDetails/gsml:dateOfDrilling",false,
                        this.generateFunctionDateParse(utcDateofDrillingStart)));

                parameterFragments
                .add(this
                        .generateDatePropertyIsLessThan(
                                "gsml:indexData/gsml:BoreholeDetails/gsml:dateOfDrilling",false,
                                this.generateFunctionDateParse(utcDateofDrillingEnd)));

            }
        }else{
            parameterFragments = this.generateParameterFragments();
        }



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

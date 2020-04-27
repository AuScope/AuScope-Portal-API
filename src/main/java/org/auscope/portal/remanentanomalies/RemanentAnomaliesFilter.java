package org.auscope.portal.remanentanomalies;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.uifilter.GenericFilter;

public class RemanentAnomaliesFilter extends GenericFilter {
    List<String> fragments;


    /**
     * Given an anomaly name, this object will build a filter
     *
     * @param anomalyName
     *            the main name or Id
     * @param ARRAMin
     * 			minimum Apparent_resultant_rotation_angle of the first model
     * @param ARRAMax
     * 			maximum Apparent_resultant_rotation_angle of the first model
     * @param incMin
     * 			minimum inclination of the first model
     * @param incMax
     * 			maximum inclination of the first model
     * @param decMin
     * 			minimum declination of the first model
     * @param decMax
     * 			maximum declination of the first model
     * @param modelCountMin
     * 			minimum number of models generated for this anomaly
     * @param modelCountMax
     * 			maximum number of models generated for this anomaly
     * @param modelsfilter
     * 			add a special filter required for modelCount styling (This is a workaround for a limitation in geoserver's attributeCount function)
     */
    public RemanentAnomaliesFilter(String name, Float ARRAMin, Float ARRAMax, Float decMin, Float decMax, Float incMin, Float incMax, Integer modelCountMin, Integer modelCountMax, Boolean modelsfilter, String optionalFilters) {
        super(optionalFilters);
       
        if(optionalFilters == null || optionalFilters.isEmpty()){
            fragments = new ArrayList<String>();
        }else{
            fragments = this.generateParameterFragments();
        }
        
        if (modelsfilter==true || modelCountMin!=null || modelCountMax !=null) fragments.add(this.generateOrComparisonFragment(this.generateNotComparisonFragment(this.generatePropertyIsNull("RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member/RemAnom:Model/RemAnom:DataType")),this.generatePropertyIsNull("RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member/RemAnom:Model/RemAnom:DataType")));
        
        if (name != null && !name.isEmpty()) {
            fragments.add(this.generateOrComparisonFragment(this.generatePropertyIsLikeFragment("RemAnom:AnomalyName", name),this.generateFeatureIdFragment("anomaly."+name)));
        }
        
		if (ARRAMin != null) {
			fragments.add(this.generatePropertyIsGreaterThanOrEqualTo(
					"RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member[1]/RemAnom:Model/RemAnom:Apparent_resultant_rotation_angle",
					ARRAMin.toString()));
		}

		if (ARRAMax != null) {
			fragments.add(this.generatePropertyIsLessThanOrEqualTo(
					"RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member[1]/RemAnom:Model/RemAnom:Apparent_resultant_rotation_angle",
					ARRAMax.toString()));
		}
        
		if (decMin != null) {
			fragments.add(this.generatePropertyIsGreaterThanOrEqualTo(
					"RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member[1]/RemAnom:Model/RemAnom:resultant_declination",
					decMin.toString()));

		}

		if (decMax != null) {
			fragments.add(this.generatePropertyIsLessThanOrEqualTo(
					"RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member[1]/RemAnom:Model/RemAnom:resultant_declination",
					decMax.toString()));
		}
        
		if (incMin != null) {
			fragments.add(this.generatePropertyIsGreaterThanOrEqualTo(
					"RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member[1]/RemAnom:Model/RemAnom:resultant_inclination",
					incMin.toString()));
		}

		if (incMax != null) {
			fragments.add(this.generatePropertyIsLessThanOrEqualTo(
					"RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member[1]/RemAnom:Model/RemAnom:resultant_inclination",
					incMax.toString()));
		}
        
		if (modelCountMin != null) {
			fragments.add("<ogc:PropertyIsGreaterThanOrEqualTo matchCase=\"false\" >"+
					this.generateFunctionAttributeCount(
							"RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member/RemAnom:Model")+"<ogc:Literal>"+modelCountMin.toString()+"</ogc:Literal></ogc:PropertyIsGreaterThanOrEqualTo>"
					);
		}

		if (modelCountMax != null) {
			fragments.add("<ogc:PropertyIsLessThanOrEqualTo matchCase=\"false\" >"+
					this.generateFunctionAttributeCount(
							"RemAnom:modelCollection/RemAnom:ModelCollection/RemAnom:member/RemAnom:Model")+"<ogc:Literal>"+modelCountMax.toString()+"</ogc:Literal></ogc:PropertyIsLessThanOrEqualTo>"
					);
		}
        
    }

    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()])));
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {

        List<String> localFragment = new ArrayList<String>(fragments);
        localFragment.add(this.generateBboxFragment(bbox, "RemAnom:CentreLocation"));

        return this.generateFilter(this.generateAndComparisonFragment(localFragment.toArray(new String[localFragment
                                                                                                       .size()])));
    }


}

package org.auscope.portal.mineraloccurrence;

/**
 * Class that represents ogc:Filter markup for mt:mineralTenement queries with CCStatus&CCType supporting 
 *
 * @author Lingbo Jiang
 * @version
 */
public class MineralTenementCCFilter extends  MineralTenementFilter {

    public MineralTenementCCFilter(String name, String tenementType, String owner, String size, String endDate) {
        super(name, tenementType, owner, size, endDate, null);

    }
    /**
     * Given a status name, this method will add mt:status into the filter list.
     * @param ccPropertyValue
     *            the mt:status or  mt:tenementType 
     * @return 
     */
    public void addCCPropertyInFilter(String ccProperty,String ccPropertyValue) {
        if (ccPropertyValue != null && !ccPropertyValue.isEmpty()) {
    
            if(ccProperty.contains("TenementType")){
                fragments.add(this.generatePropertyIsLikeFragment("mt:tenementType", ccPropertyValue));
            } else if (ccProperty.contains("TenementStatus")){
                fragments.add(this.generatePropertyIsLikeFragment("mt:status", ccPropertyValue));
            }
        }

    }
}

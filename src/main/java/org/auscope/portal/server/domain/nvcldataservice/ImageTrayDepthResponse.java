package org.auscope.portal.server.domain.nvcldataservice;

import java.io.Serializable;

public class ImageTrayDepthResponse implements Serializable {

    private static final long serialVersionUID = 4240200019321300734L;
    String sampleNo;
    String startValue;
    String endValue;
    
    /**
     * Creates an ImageTrayDepthResponse
     * 
     * @param sampleNo
     *            The tray image sample number
     * @param startValue
     *            The starting depth of the tray image
     * @param endValue
     *            The ending depth of the tray image
     */ 
    public ImageTrayDepthResponse(String sampleNo, String startValue, String endValue) {
        this.sampleNo = sampleNo;
        this.startValue = startValue;
        this.endValue = endValue;
    }
    
    /**
     * Gets the sample number of the tray image
     *
     * @return String
     */
    public String getSampleNo() {
        return this.sampleNo;
    }
    
    /**
     * Sets the sample number of the tray image
     *
     * @param sampleNo
     */
    public void setSampleNo(String sampleNo) {
        this.sampleNo = sampleNo;
    }
    
    /**
     * Gets the starting depth of the tray image
     *
     * @return String
     */
    public String getStartValue() {
        return this.startValue;
    }
    
    /**
     * Sets the starting depth of the tray image
     *
     * @param startValue
     */
    public void setStartValue(String startValue) {
        this.startValue = startValue;
    }
    
    /**
     * Gets the end depth of the tray image
     *
     * @return String
     */
    public String getEndValue() {
        return this.endValue;
    }
    
    /**
     * Sets the end depth for the tray image
     *
     * @param endValue
     */
    public void setEndValue(String endValue) {
        this.endValue = endValue;
    }    
}

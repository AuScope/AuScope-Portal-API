package org.auscope.portal.service.colorcoding;

/**
 * Capdf Hydrochemistry specific color coding manager.
 * 
 * @author tey006
 *
 */
public class CapdfHydroChemColorCoding {

    private String poi;
    ColorCoding capdfConfigObject;

    public CapdfHydroChemColorCoding(String poi, double min, double max) {
        this.poi = poi;
        this.capdfConfigObject = new ColorCoding(min, max);
    }

    public String getPOI() {
        return poi;
    }

    public ColorCodingConfig getColorCodingConfig() {
        return capdfConfigObject.getColorCodingConfig();
    }

    //VT: this is only used in Capricorn. However, should future use arrived for this, this inner class
    //VT: should be refactor into portal-core along with ColorCodingConfig.java.
    public class ColorCoding {

        ColorCodingConfig colorConfig;
        private final int DEFAULT_INTERVAL = 10;

        public ColorCoding(double min, double max, int interval, ColorCodingConfig.COLOR color) {
            this.colorConfig = new ColorCodingConfig(min, max, interval, color);
            ;
        }

        public ColorCoding(double min, double max, ColorCodingConfig.COLOR color) {
            this.colorConfig = new ColorCodingConfig(min, max, DEFAULT_INTERVAL, color);
            ;
        }

        public ColorCoding(double min, double max) {
            //VT: random color
            this.colorConfig = new ColorCodingConfig(min, max, DEFAULT_INTERVAL);
        }

        public ColorCoding(ColorCodingConfig ccc) {
            this.colorConfig = ccc;
        }

        public ColorCodingConfig getColorCodingConfig() {
            return this.colorConfig;
        }

    }

}

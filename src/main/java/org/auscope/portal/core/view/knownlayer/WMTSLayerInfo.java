package org.auscope.portal.core.view.knownlayer;

import java.util.List;

/**
 * WMTS layer info for KnownLayers 
 */
public class WMTSLayerInfo {

    private String url;
    private String selector;
    private String style;
    private String format;
    private String tileMatrixSetID;
    private List<String> tileMatrixLabels;

    private List<String> endpoints;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTileMatrixSetID() {
        return tileMatrixSetID;
    }

    public void setTileMatrixSetID(String tileMatrixSetID) {
        this.tileMatrixSetID = tileMatrixSetID;
    }

    public List<String> getTileMatrixLabels() {
        return tileMatrixLabels;
    }

    public void setTileMatrixLabels(List<String> tileMatrixLabels) {
        this.tileMatrixLabels = tileMatrixLabels;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }
}

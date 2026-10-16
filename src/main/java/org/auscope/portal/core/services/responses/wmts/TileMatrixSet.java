package org.auscope.portal.core.services.responses.wmts;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for WMTS TileMatrixSet objects
 */
public class TileMatrixSet {

    private String identifier;
    private String supportedCRS;
    private List<TileMatrix> tileMatrices;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSupportedCRS() {
        return supportedCRS;
    }

    public void setSupportedCRS(String supportedCRS) {
        this.supportedCRS = supportedCRS;
    }

    public List<TileMatrix> getTileMatrices() {
        return tileMatrices;
    }

    public void setTileMatrices(List<TileMatrix> tileMatrices) {
        this.tileMatrices = tileMatrices;
    }

    /**
     * Get a list of tile matrix set labels
     * 
     * @return tile matrix set labels as a List<String>
     */
    public List<String> getLabels() {
        List<String> labels = new ArrayList<String>();
        if (tileMatrices != null) {
            for (TileMatrix tileMatrix : tileMatrices) {
                if (tileMatrix != null && tileMatrix.getIdentifier() != null) {
                    labels.add(tileMatrix.getIdentifier());
                }
            }
        }
        return labels;
    }

}
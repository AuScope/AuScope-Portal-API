package org.auscope.portal.core.services.responses.wmts;

import java.util.List;

/**
 * Interface for WMTS GetCapabilities records
 */
public interface GetCapabilitiesWMTSLayerRecord {

    String getIdentifier();

    String getTitle();

    String getAbstract();

    List<String> getFormats();

    String getTileTemplate();

    List<String> getTileMatrixSetLinks();

    String getDefaultStyle();

    List<String> getStyles();
}

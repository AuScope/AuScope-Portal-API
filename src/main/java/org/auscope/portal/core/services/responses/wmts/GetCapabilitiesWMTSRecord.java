package org.auscope.portal.core.services.responses.wmts;

import java.util.List;

/**
 * Interface for WMTS GetCapabilties records
 */
public interface GetCapabilitiesWMTSRecord {

    boolean isWMTS();

    String getServiceType();

    String getVersion();

    String getOrganisation();

    String getGetTileUrl();

    String getServiceMetadataUrl();

    List<GetCapabilitiesWMTSLayerRecord> getLayers();

    // Tile formats (image/png, etc.)
    List<String> getFormats();

    // Feature info formats
    List<String> getInfoFormats();

    TileMatrixSet findTileMatrixSet(String identifier);

    String getApplicationProfile();

    String[] getAccessConstraints();
}

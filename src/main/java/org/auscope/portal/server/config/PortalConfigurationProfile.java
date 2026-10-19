package org.auscope.portal.server.config;

import java.util.ArrayList;
import org.auscope.portal.core.view.knownlayer.KnownLayer;

public interface PortalConfigurationProfile {
    /**
     * Common method to trigger a fresh layers.yaml reload.
     */
    void reloadLayers();

    /**
     * Common getter to retrieve the current list of active layers.
     */
    ArrayList<KnownLayer> getActiveLayers();
}

package org.auscope.portal.server;

import java.util.Arrays;

import org.auscope.portal.core.server.PortalProfileXmlWebApplicationContext;

/**
 *
 * @author vot002
 *
 */
public class VLWebAppContext extends PortalProfileXmlWebApplicationContext {
    @Override
    protected String[] getDefaultConfigLocations() {
        String[] locations = super.getDefaultConfigLocations();

        String[] auscopeLocations = Arrays.copyOf(locations, locations.length + 3);
        auscopeLocations[auscopeLocations.length - 1] = DEFAULT_CONFIG_LOCATION_PREFIX + "vl-known-layers.xml";
        auscopeLocations[auscopeLocations.length - 2] = DEFAULT_CONFIG_LOCATION_PREFIX + "vl-registries.xml";
        auscopeLocations[auscopeLocations.length - 3] = DEFAULT_CONFIG_LOCATION_PREFIX + "applicationContext-security.xml";
        return auscopeLocations;
    }
}

package org.auscope.portal.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.yaml.snakeyaml.Yaml;

/**
 * Definitions for all known layers
 */

@Configuration
@Profile("prod")
public class ProfilePortalProduction extends AbstractBasePortalProfile {

    @Override
    public KnownLayer knownType(String id) {
        LayerFactory lf = new LayerFactory(yamlLayers, layersLoaded);
        return lf.annotateLayer(id);
    }

    @Bean
    public ArrayList<KnownLayer> knownTypes() {
        reloadLayers(); 
        return activeLayers;
    }
}

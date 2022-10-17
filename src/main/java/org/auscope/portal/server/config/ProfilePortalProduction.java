package org.auscope.portal.server.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.yaml.snakeyaml.Yaml;

/**
 * Definitions for all known layers
 */

@Configuration
@Profile("prod")
public class ProfilePortalProduction {

    private boolean layersLoaded = false;

    Map<String, Object> yamlLayers;
    
    public KnownLayer knownType(String id) {
        
        LayerFactory lf = new LayerFactory(yamlLayers, layersLoaded);
        KnownLayer layer = lf.annotateLayer(id);

        return layer;
    }


    
    @Bean
    public ArrayList<KnownLayer> knownTypes() {
        ArrayList<KnownLayer> knownLayers = new ArrayList<KnownLayer>();

        layersLoaded = true;
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("layers.yaml");
        yamlLayers = yaml.load(inputStream);

        int[] counter = new int[1];
        yamlLayers.forEach((k, v) -> {
            counter[0]++;
            String id = k.toString();
            //if (counter[0] <= 181) { // 180
                System.out.println(counter[0] + ", Key = " + id + ", Value = " + v);
                knownLayers.add(knownType(id));
            //}
        });

        return knownLayers;
    }
}

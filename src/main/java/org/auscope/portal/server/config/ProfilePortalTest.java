package org.auscope.portal.server.config;

import java.awt.Dimension;
import java.awt.Point;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.auscope.portal.core.uifilter.AbstractBaseFilter;
import org.auscope.portal.core.uifilter.FilterCollection;
import org.auscope.portal.core.uifilter.Predicate;
import org.auscope.portal.core.uifilter.mandatory.AbstractMandatoryParamBinding;
import org.auscope.portal.core.uifilter.optional.UICheckBoxGroupProvider;
import org.auscope.portal.core.uifilter.optional.xpath.UIDate;
import org.auscope.portal.core.uifilter.optional.xpath.UIDropDownRemote;
import org.auscope.portal.core.uifilter.optional.xpath.UIDropDownSelectList;
import org.auscope.portal.core.uifilter.optional.xpath.UIPolygonBBox;
import org.auscope.portal.core.uifilter.optional.xpath.UITextBox;
import org.auscope.portal.core.view.knownlayer.CSWRecordSelector;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.WFSSelector;
import org.auscope.portal.core.view.knownlayer.WMSSelector;
import org.auscope.portal.core.view.knownlayer.WMSWFSSelector;
import org.auscope.portal.view.knownlayer.IRISSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.yaml.snakeyaml.Yaml;
import org.auscope.portal.server.config.LayerFactory;

/**
 * Definitions for all known layers
 */

@Configuration
@Profile("test")
public class ProfilePortalTest {

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
            //System.out.println(counter[0] + ", Key = " + id + ", Value = " + v);
            knownLayers.add(knownType(id));
            //}
        });

        return knownLayers;
    }

}

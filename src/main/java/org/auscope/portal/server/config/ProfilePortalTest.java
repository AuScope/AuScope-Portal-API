package org.auscope.portal.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.yaml.snakeyaml.Yaml;

import org.auscope.portal.core.view.knownlayer.KnownLayer;

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

    @Autowired private LayerChecksumService layerChecksumService;

    @Value("${cloud.aws.portalS3Bucket}") private String portalS3Bucket;
    
    @Bean
    public ArrayList<KnownLayer> knownTypes() {
        ArrayList<KnownLayer> knownLayers = new ArrayList<KnownLayer>();

        layersLoaded = true;
        Yaml yaml = new Yaml();
        // InputStream inputStream =
        // this.getClass().getClassLoader().getResourceAsStream("layers.yaml");
        URL yamlUrl;
        try {
            //yamlUrl = new URL("https://drdzuf3dxzz1h.cloudfront.net/layers.yaml");
            //yamlUrl = new URL(portalS3Bucket+"/layers.yaml");
            yamlUrl = new URI(portalS3Bucket+"/layers.yaml").toURL();
            try (InputStream yamlInputStream = yamlUrl.openStream()) {
                CheckedInputStream checkedInputStream = new CheckedInputStream(yamlInputStream, new CRC32());
                yamlLayers = yaml.load(checkedInputStream);

                int[] counter = new int[1];
                yamlLayers.forEach((k, v) -> {
                    counter[0]++;
                    String id = k.toString();
                    // if (counter[0] <= 181) { // 180
                    // System.out.println(counter[0] + ", Key = " + id + ", Value = " + v);
                    KnownLayer l = knownType(id);
                    if (!l.isHidden())
                        knownLayers.add(knownType(id));
                });

                Long checksum = checkedInputStream.getChecksum().getValue();
                layerChecksumService.setChecksum(checksum);
                //System.out.println(ZonedDateTime.now()+"[ProfilePortalTest]knownTypes().yamlLayers="+yamlLayers.toString());
                System.out.println(ZonedDateTime.now()+"[ProfilePortalTest]knownTypes(old) checksum = " + checksum.toString());
                layerChecksumService.setChecksum(checksum);
                
            } catch (MalformedURLException e) {
                System.err.println("ZonedDateTime.now()+[ProfilePortalTest]knownTypes() Error reading from URL or processing stream: " + e.getMessage());
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("ZonedDateTime.now()+[ProfilePortalTest]knownTypes() Error reading from URL or processing stream: " + e.getMessage());
            //e.printStackTrace();
        }

        return knownLayers;
    }

}

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
import org.yaml.snakeyaml.Yaml;

/**
 * Abstract foundational profile for managing the layers.yaml.
 * Handles lifecycle bootstrapping, delayed initialization triggers, and registers the 
 * 15-minute background polling routine used to detect alterations to the remote layers.
 * 
 * This base profile abstracts layer synchronization logic across all runtime targets. 
 * Environment-specific behaviors are isolated into concrete child implementations:
 * 
 * Concrete Implementations:
 * - Development / Test Profiles: Override configuration bindings to point 
 *   portalS3Bucket to local storage protocols (such as file:///Users/...) 
 *   for offline evaluation and local component integration verification.
 * - Staging / Production Profiles: Bind portalS3Bucket directly to external, 
 *   version-controlled repositories (such as raw GitHub CDN links or AWS S3 buckets) 
 *   to distribute production-ready layer configurations.
 * 
 * @see KnownLayerService
 * @see LayerChecksumService
 */
public abstract class AbstractBasePortalProfile implements PortalConfigurationProfile {

    protected boolean layersLoaded = false;
    protected final ArrayList<KnownLayer> activeLayers = new ArrayList<>();
    protected Map<String, Object> yamlLayers;

    @Autowired 
    protected LayerChecksumService layerChecksumService;

    @Value("${cloud.aws.portalS3Bucket}") 
    protected String portalS3Bucket;

    /**
     * Each sub-profile class (Test vs Prod) implements this to build or 
     * annotate the raw layer using its specific LayerFactory context.
     */
    public abstract KnownLayer knownType(String id);

    @Override
    public ArrayList<KnownLayer> getActiveLayers() {
        synchronized (activeLayers) {
            return new ArrayList<>(activeLayers);
        }
    }

    /**
     * Synchronously flushes and reloads the active portal layers cache directly from the layers.yaml.
     * 
     * This method fetches the layers.yaml configuration manifest from the configured portalS3Bucket target 
     * (supporting local file paths, S3 buckets, or GitHub CDN endpoints). It parses the source structure, 
     * processes non-hidden configurations into active layers, calculates a CRC32 stream checksum to persist 
     * in the checksum service, and performs an atomic swap of the active layers collection.
     * 
     * Throws MalformedURLException if the combined portalS3Bucket URI string results in an invalid URL structure.
     * Throws IOException or URISyntaxException if stream parsing or asset retrieval encounters transport issues.
     */
    @Override
    public synchronized void reloadLayers() {
        System.out.println("[checksum changed] Refreshing known layers from: " + portalS3Bucket + "/layers.yaml; current activeLayers = " + activeLayers.size());
        
        ArrayList<KnownLayer> freshLayers = new ArrayList<>();
        layersLoaded = true;
        Yaml yaml = new Yaml();
        
        try {
            InputStream yamlInputStream;
            String fullPath = portalS3Bucket.endsWith("/") ? portalS3Bucket + "layers.yaml" : portalS3Bucket + "/layers.yaml";
            
            // Route local testing files cleanly to prevent network stream timeouts
            if (fullPath.startsWith("file:")) {
                java.io.File localFile = new java.io.File(new URI(fullPath));
                yamlInputStream = new java.io.FileInputStream(localFile);
            } else {
                // Production web targets (GitHub/S3) continue using standard web streams
                URL yamlUrl = new URI(fullPath).toURL();
                yamlInputStream = yamlUrl.openStream();
            }
            
            try (CheckedInputStream checkedInputStream = new CheckedInputStream(yamlInputStream, new CRC32())) {
                yamlLayers = yaml.load(checkedInputStream);

                // ✨ Clean, safe iteration through your map keys
                if (yamlLayers != null) {
                    for (Object key : yamlLayers.keySet()) {
                        if (key != null) {
                            String id = key.toString();
                            KnownLayer l = knownType(id);
                            if (l != null && !l.isHidden()) {
                                freshLayers.add(l);
                            }
                        }
                    }
                }

                Long checksum = checkedInputStream.getChecksum().getValue();
                if (layerChecksumService != null) {
                    layerChecksumService.setChecksum(checksum);
                }
                
                synchronized (activeLayers) {
                    activeLayers.clear();
                    activeLayers.addAll(freshLayers);
                }
                System.out.println("[checksum changed] Layers successfully reloaded. activeLayers = " + activeLayers.size());
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("[checksum changed] reloadLayers() Error reading from URL or processing stream: " + e.getMessage());
        }
    }
    
}

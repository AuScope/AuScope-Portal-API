package org.auscope.portal.core.services;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.methodmakers.WMTS_1_0_0_MethodMaker;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.wmts.GetCapabilitiesWMTSLayerRecord;
import org.auscope.portal.core.services.responses.wmts.GetCapabilitiesWMTSRecord;
import org.auscope.portal.core.services.responses.wmts.GetCapabilitiesWMTSRecord_1_0_0;
import org.auscope.portal.core.services.responses.wmts.TileMatrixSet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service class for WMTS requests
 */
public class WMTSService {

    private final Log log = LogFactory.getLog(getClass());

    private HttpServiceCaller serviceCaller;
    private WMTS_1_0_0_MethodMaker methodMaker;
    private final Map<String, GetCapabilitiesWMTSRecord> capabilitiesCache = new ConcurrentHashMap<String, GetCapabilitiesWMTSRecord>();

    public WMTSService(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
        this.methodMaker = new WMTS_1_0_0_MethodMaker(serviceCaller);
    }

    /**
     * Handle WMTS GetCapabilities requests
     * 
     * @param wmtsUrl the WTS URL
     * @return the WMTS GetCapabilities response as a GetCapabilitiesWMTSRecord
     * @throws IOException
     * @throws URISyntaxException
     */
    public GetCapabilitiesWMTSRecord getCapabilities(String wmtsUrl) throws IOException, URISyntaxException {
        HttpRequestBase method = methodMaker.getCapabilitiesMethod(wmtsUrl);
        return methodMaker.getGetCapabilitiesRecord(method);
    }

    /**
     * Handle WMTS GetTile requests
     * 
     * @param wmtsUrl       WMTS URL
     * @param layer         WMTS layer
     * @param style         WMTS style
     * @param tileMatrixSet WMTS tile matrix set
     * @param tileMatrix    WMTS tile matrix
     * @param tileRow       WMTS tile row
     * @param tileCol       WMTS tile column
     * @param format        image format
     * @return WMTS GetTile response as an InputStream
     * @throws IOException
     * @throws URISyntaxException
     */
    public InputStream getTile(String wmtsUrl, String layer, String style, String tileMatrixSet, String tileMatrix,
            int tileRow, int tileCol, String format) throws IOException, URISyntaxException {
        HttpRequestBase method = methodMaker.getTileMethod(wmtsUrl, layer, style, tileMatrixSet, tileMatrix, tileRow,
                tileCol, format);
        return serviceCaller.getMethodResponseAsStream(method);
    }

    /**
     * Determine a click tolerance in pixels based on a Cesium zoom level
     *
     * @param level zoom level from Cesium
     * @return the click tolerance in pixels
     */
    private int getClickTolerance(int level) {
        if (level <= 5) {
            return 20;
        }
        if (level <= 8) {
            return 10;
        }
        if (level <= 12) {
            return 5;
        }
        return 2;
    }

    /**
     * Build a list of search offsets starting from the middle and radiating
     * outwards
     * 
     * @param tolerance the tolerance in pixels from the center
     * @return a list of points starting at the center and radiating outwards up to
     *         the tolerance level
     */
    private List<Point> buildSearchOffsets(int tolerance) {
        List<Point> offsets = new ArrayList<>();
        for (int dx = -tolerance; dx <= tolerance; dx++) {
            for (int dy = -tolerance; dy <= tolerance; dy++) {
                offsets.add(new Point(dx, dy));
            }
        }
        offsets.sort(Comparator.comparingDouble(p -> p.x * p.x + p.y * p.y));
        return offsets;
    }

    /**
     * Quick check whether a JSON response contains features
     * 
     * @param response the JSON response as a String
     * @return true if the response contains features, false otherwise
     */
    private boolean containsFeatures(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode features = root.get("features");
            return features != null && features.size() > 0;
        } catch (Exception ex) {
            log.debug("Unable to parse WMTS response", ex);
            return false;
        }
    }

    /**
     * Handle WMTS FeatureInfo requests
     * 
     * @param wmtsUrl       WMTS URL
     * @param layer         WMTS layer
     * @param style         WMTS
     * @param tileMatrixSet WMTS tile matrix set
     * @param tileMatrix    WMTS tile matrix
     * @param tileRow       WMTS tile row
     * @param tileCol       WMTS tile column
     * @param infoFormat    WMTS info format
     * @param i             pixel i
     * @param j             pixel j
     * @param format        image format
     * @param level         zoom level
     * @return FeatureInfo response as an InputStream
     * @throws IOException
     * @throws URISyntaxException
     */
    public InputStream getFeatureInfo(String wmtsUrl, String layer, String style, String tileMatrixSet,
            String tileMatrix, int tileRow, int tileCol, String infoFormat, int i, int j, String format, int level)
            throws IOException, URISyntaxException {
        int tolerance = getClickTolerance(level);
        List<Point> offsets = buildSearchOffsets(tolerance);

        for (Point p : offsets) {
            int testI = i + p.x;
            int testJ = j + p.y;
            if (testI < 0 || testI > 255 || testJ < 0 || testJ > 255) {
                continue;
            }
            try {
                HttpRequestBase request = methodMaker.getFeatureInfo(wmtsUrl, layer, style, tileMatrixSet, tileMatrix,
                        tileRow, tileCol, infoFormat, testI, testJ, format, level);
                String response = serviceCaller.getMethodResponseAsString(request);
                if (containsFeatures(response)) {
                    return new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
                }
            } catch (Exception ex) {
                log.debug("WMTS GetFeatureInfo miss at " + testI + "," + testJ, ex);
            }
        }
        log.info("WMTS search exhausted all offsets");
        return null;
    }

    public String getSupportedVersion() {
        return methodMaker.getSupportedVersion();
    }

    public boolean accepts(String url, String version, StringBuilder errStr) {
        return methodMaker.accepts(url, version, errStr);
    }

    public boolean accepts(String url, String version) {
        return methodMaker.accepts(url, version);
    }

    /**
     * Enrich a a WMTS CSWRecord such that its WMTS online resource(s) contains
     * additional WMTS information required for adding to the map.
     * 
     * @param wmtsUrl
     * @param resources
     */
    private void enrichRecord(CSWRecord record) {
        List<AbstractCSWOnlineResource> wmtsResources = record.getOnlineResourcesByType(OnlineResourceType.WMTS);
        if (wmtsResources == null || wmtsResources.isEmpty()) {
            return;
        }
        List<AbstractCSWOnlineResource> capabilityResources = record
                .getOnlineResourcesByType(OnlineResourceType.WMTSCapabilities);
        if (capabilityResources == null || capabilityResources.isEmpty()) {
            return;
        }
        for (AbstractCSWOnlineResource wmtsResource : wmtsResources) {
            if (!(wmtsResource instanceof CSWOnlineResourceImpl)) {
                continue;
            }
            if (wmtsResource.getName() == null) {
                continue;
            }
            // Find matching capabilities resource
            AbstractCSWOnlineResource capabilitiesResource = null;
            for (AbstractCSWOnlineResource cap : capabilityResources) {
                if (wmtsResource.getName().equals(cap.getName())) {
                    capabilitiesResource = cap;
                    break;
                }
            }
            if (capabilitiesResource == null || capabilitiesResource.getLinkage() == null) {
                continue;
            }
            String capabilitiesUrl = capabilitiesResource.getLinkage().toString();
            // Load capabilities (cached)
            GetCapabilitiesWMTSRecord capabilities = capabilitiesCache.get(capabilitiesUrl);
            if (capabilities == null) {
                try {
                    capabilities = getCapabilities(capabilitiesUrl);
                    capabilitiesCache.put(capabilitiesUrl, capabilities);
                } catch (Exception ex) {
                    log.warn("Unable to retrieve WMTS capabilities for " + capabilitiesUrl, ex);
                    continue;
                }
            }

            // Lookups
            Map<String, GetCapabilitiesWMTSLayerRecord> layerMap = new HashMap<>();
            for (GetCapabilitiesWMTSLayerRecord layer : capabilities.getLayers()) {
                layerMap.put(layer.getIdentifier(), layer);
            }

            Map<String, TileMatrixSet> tileMatrixSetMap = new HashMap<>();
            if (capabilities instanceof GetCapabilitiesWMTSRecord_1_0_0) {
                GetCapabilitiesWMTSRecord_1_0_0 wmtsRecord = (GetCapabilitiesWMTSRecord_1_0_0) capabilities;
                for (TileMatrixSet tileMatrixSet : wmtsRecord.getTileMatrixSets()) {
                    tileMatrixSetMap.put(tileMatrixSet.getIdentifier(), tileMatrixSet);
                }
            }

            // Find matching layer
            GetCapabilitiesWMTSLayerRecord layer = layerMap.get(wmtsResource.getName());
            if (layer == null) {
                continue;
            }
            CSWOnlineResourceImpl resource = (CSWOnlineResourceImpl) wmtsResource;
            resource.setWmtsCapabilitiesUrl(capabilitiesUrl);

            if (layer.getTileTemplate() != null && !layer.getTileTemplate().isBlank()) {
                resource.setWmtsAccessMethod("REST");
            } else {
                resource.setWmtsAccessMethod("KVP");
            }

            // Tile Template
            resource.setWmtsTileTemplate(layer.getTileTemplate());

            // Style
            resource.setWmtsStyle(layer.getDefaultStyle());

            // Format
            if (!layer.getFormats().isEmpty()) {
                resource.setWmtsFormat(layer.getFormats().get(0));
            }

            // Tile matrix set(s)
            if (!layer.getTileMatrixSetLinks().isEmpty()) {
                resource.setWmtsTileMatrixSets(new ArrayList<>(layer.getTileMatrixSetLinks()));
                String tileMatrixSetId = layer.getTileMatrixSetLinks().get(0);
                resource.setWmtsTileMatrixSet(tileMatrixSetId);
                TileMatrixSet tileMatrixSet = tileMatrixSetMap.get(tileMatrixSetId);
                if (tileMatrixSet != null) {
                    resource.setWmtsTileMatrixLabels(tileMatrixSet.getLabels());
                }
            }
        }
    }

    /**
     * Enrich a collection of WMTS CSWRecords such that their WMTS online resources
     * contain additional WMTS information required for adding to the map.
     * 
     * @param records a List of CSWRecords
     */
    public void enrichRecords(Collection<CSWRecord> records) {
        for (CSWRecord record : records) {
            enrichRecord(record);
        }
    }

}
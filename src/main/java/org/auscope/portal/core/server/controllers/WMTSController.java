package org.auscope.portal.core.server.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.WMTSService;
import org.auscope.portal.core.services.responses.wmts.GetCapabilitiesWMTSRecord;
import org.auscope.portal.core.util.FileIOUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller class for WMTS requests from the front-end
 */
@Controller
@Scope("session")
public class WMTSController {

    private final Log log = LogFactory.getLog(getClass());
    protected static int BUFFERSIZE = 1024 * 1024;

    private WMTSService wmtsService;

    public WMTSController(WMTSService wmtsService) {
        this.wmtsService = wmtsService;
    }

    /**
     * Gets the capabilities document for a given WMTS URL
     * @param wmtsUrlthe WMTS URL
     * @return the GetCapabilities response as a GetCapabilitiesWMTSRecord
     * @throws IOException
     * @throws URISyntaxException
     */
    public GetCapabilitiesWMTSRecord getCapabilities(String wmtsUrl) throws IOException, URISyntaxException {
        log.debug("WMTSController - getCapabilities: " + wmtsUrl);
        return wmtsService.getCapabilities(wmtsUrl);
    }

    /**
     * Gets a tile from the WMTS
     * @param wmtsUrl WMTS URL
     * @param layer WMTS layer
     * @param style WMTS style
     * @param tileMatrixSetWMTS tile matrix set
     * @param tileMatrix WNTS tile matrix
     * @param tileRow WMTS tile row
     * @param tileCol WMTS tile column
     * @param format WMTS tile format
     * @return an InoutStream for the tile
     * @throws IOException
     * @throws URISyntaxException
     */
    public InputStream getTile(String wmtsUrl, String layer, String style, String tileMatrixSet, String tileMatrix,
            int tileRow, int tileCol, String format) throws IOException, URISyntaxException {
        log.debug("WMTSController - getTile: " + layer);
        return wmtsService.getTile(wmtsUrl, layer, style, tileMatrixSet, tileMatrix, tileRow, tileCol, format);
    }

    /**
     * Gets feature info from the WMTS
     * @param wmtsUrl WMTS URL
     * @param layer WMTS layer
     * @param style WMTS style
     * @param tileMatrixSet WMTS tile matrix set
     * @param tileMatrix WMTS tile matrix
     * @param tileRow WMTS tile row
     * @param tileCol WMTS tile column
     * @param infoFormat WMTS info format
     * @param i pixel i
     * @param j pixel j
     * @param format image format
     * @param level zoom level
     * @return InputStream of feature info
     * @throws IOException
     * @throws URISyntaxException
     */
    public InputStream getFeatureInfo(String wmtsUrl, String layer, String style, String tileMatrixSet,
            String tileMatrix, int tileRow, int tileCol, String infoFormat, int i, int j, String format, int level)
            throws IOException, URISyntaxException {
        log.debug("WMTSController - getFeatureInfo: " + layer);
        return wmtsService.getFeatureInfo(wmtsUrl, layer, style, tileMatrixSet, tileMatrix, tileRow, tileCol,
                infoFormat, i, j, format, level);
    }

    /**
     * Feature info request from the front-end
     * @param request the feature info request
     * @param response the response
     * @param serviceUrl WMTS service URL
     * @param layer WMTS layer
     * @param style WMTS style
     * @param tileMatrixSet WMTS tile matrix set
     * @param tileMatrix WMTS tile matrix
     * @param tileRow WMTS tile row
     * @param tileCol WMTS tile column
     * @param infoFormat WMTS info format
     * @param i pixel i
     * @param j pixel j
     * @param format image format
     * @param levelzoom level
     * @throws Exception
     */
    @RequestMapping(value = "/wmtsMarkerPopup.do", method = { RequestMethod.GET, RequestMethod.POST })
    public void wmtsMarkerPopup(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("serviceUrl") String serviceUrl, @RequestParam("layer") String layer,
            @RequestParam("style") String style, @RequestParam("tileMatrixSet") String tileMatrixSet,
            @RequestParam("tileMatrix") String tileMatrix, @RequestParam("tileRow") int tileRow,
            @RequestParam("tileCol") int tileCol, @RequestParam("infoFormat") String infoFormat,
            @RequestParam("i") int i, @RequestParam("j") int j, @RequestParam("format") String format,
            @RequestParam("level") int level) throws Exception {

        log.info(String.format(
                "WMTS GetFeatureInfo: service=%s layer=%s style=%s matrixSet=%s matrix=%s row=%d col=%d i=%d j=%d format=%s level=%d",
                serviceUrl, layer, style, tileMatrixSet, tileMatrix, tileRow, tileCol, i, j, format, level));

        InputStream responseStream = null;
        try {
            responseStream = wmtsService.getFeatureInfo(serviceUrl, layer, style, tileMatrixSet, tileMatrix, tileRow,
                    tileCol, infoFormat, i, j, format, level);
            FileIOUtil.writeInputToOutputStream(responseStream, response.getOutputStream(), BUFFERSIZE, true);
        } finally {
            if (responseStream != null) {
                responseStream.close();
            }
        }
    }

    public boolean accepts(String url, String version, StringBuilder errStr) {
        return wmtsService.accepts(url, version, errStr);
    }

    public boolean accepts(String url, String version) {
        return wmtsService.accepts(url, version);
    }

    public String getSupportedVersion() {
        return wmtsService.getSupportedVersion();
    }
}
package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.services.responses.wmts.GetCapabilitiesWMTSRecord;

/**
 * Interface for WMTS method generation
 */
public interface WMTSMethodMakerInterface {

    HttpRequestBase getCapabilitiesMethod(String wmtsUrl) throws URISyntaxException;

    HttpRequestBase getTileMethod(String wmtsUrl, String layer, String style, String tileMatrixSet, String tileMatrix,
            int tileRow, int tileCol, String format) throws URISyntaxException;

    HttpRequestBase getFeatureInfo(String wmtsUrl, String layer, String style, String tileMatrixSet, String tileMatrix,
            int tileRow, int tileCol, String infoFormat, int i, int j, String format, int level)
            throws URISyntaxException;

    GetCapabilitiesWMTSRecord getGetCapabilitiesRecord(HttpRequestBase method) throws IOException;

    String getSupportedVersion();

    boolean accepts(String url, String version, StringBuilder errStr);

    boolean accepts(String url, String version);
}
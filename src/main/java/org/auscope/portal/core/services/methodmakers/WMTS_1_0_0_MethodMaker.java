package org.auscope.portal.core.services.methodmakers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.responses.wmts.GetCapabilitiesWMTSRecord;
import org.auscope.portal.core.services.responses.wmts.GetCapabilitiesWMTSRecord_1_0_0;
import org.auscope.portal.core.util.HttpUtil;
import org.xml.sax.SAXException;

/**
 * Class for generating WMTS methods
 */
public class WMTS_1_0_0_MethodMaker extends AbstractMethodMaker implements WMTSMethodMakerInterface {

    // Default WMTS version
    public static final String VERSION = "1.0.0";

    private final Log log = LogFactory.getLog(getClass());

    private HttpServiceCaller serviceCaller;

    public WMTS_1_0_0_MethodMaker(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    @Override
    public HttpRequestBase getCapabilitiesMethod(String wmtsUrl) throws URISyntaxException {
        List<NameValuePair> params = this.extractQueryParams(wmtsUrl);

        params.add(new BasicNameValuePair("service", "WMTS"));
        params.add(new BasicNameValuePair("request", "GetCapabilities"));
        params.add(new BasicNameValuePair("version", VERSION));

        HttpGet method = new HttpGet();
        method.setURI(HttpUtil.parseURI(wmtsUrl, params));

        return method;
    }

    @Override
    public HttpRequestBase getTileMethod(String wmtsUrl, String layer, String style, String tileMatrixSet,
            String tileMatrix, int tileRow, int tileCol, String format) throws URISyntaxException {
        List<NameValuePair> params = this.extractQueryParams(wmtsUrl);

        params.add(new BasicNameValuePair("service", "WMTS"));
        params.add(new BasicNameValuePair("request", "GetTile"));
        params.add(new BasicNameValuePair("version", VERSION));

        params.add(new BasicNameValuePair("layer", layer));
        params.add(new BasicNameValuePair("style", style));
        params.add(new BasicNameValuePair("tilematrixset", tileMatrixSet));
        params.add(new BasicNameValuePair("tilematrix", tileMatrix));
        params.add(new BasicNameValuePair("tilerow", Integer.toString(tileRow)));
        params.add(new BasicNameValuePair("tilecol", Integer.toString(tileCol)));
        params.add(new BasicNameValuePair("format", format));

        HttpGet method = new HttpGet();
        method.setURI(HttpUtil.parseURI(wmtsUrl, params));

        return method;
    }

    @Override
    public HttpRequestBase getFeatureInfo(String wmtsUrl, String layer, String style, String tileMatrixSet,
            String tileMatrix, int tileRow, int tileCol, String infoFormat, int i, int j, String format, int level)
            throws URISyntaxException {
        List<NameValuePair> params = this.extractQueryParams(wmtsUrl);

        params.add(new BasicNameValuePair("service", "WMTS"));
        params.add(new BasicNameValuePair("request", "GetFeatureInfo"));
        params.add(new BasicNameValuePair("version", VERSION));

        params.add(new BasicNameValuePair("layer", layer));
        params.add(new BasicNameValuePair("style", style));
        params.add(new BasicNameValuePair("tilematrixset", tileMatrixSet));
        params.add(new BasicNameValuePair("tilematrix", tileMatrix));
        params.add(new BasicNameValuePair("tilerow", Integer.toString(tileRow)));
        params.add(new BasicNameValuePair("tilecol", Integer.toString(tileCol)));

        params.add(new BasicNameValuePair("infoFormat", infoFormat));

        params.add(new BasicNameValuePair("i", Integer.toString(i)));
        params.add(new BasicNameValuePair("j", Integer.toString(j)));

        params.add(new BasicNameValuePair("format", format));

        HttpGet method = new HttpGet();
        method.setURI(HttpUtil.parseURI(wmtsUrl, params));

        return method;
    }

    @Override
    public GetCapabilitiesWMTSRecord getGetCapabilitiesRecord(HttpRequestBase method) throws IOException {

        try (InputStream response = serviceCaller.getMethodResponseAsStream(method)) {
            return new GetCapabilitiesWMTSRecord_1_0_0(response);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedVersion() {
        return VERSION;
    }

    @Override
    public boolean accepts(String url, String version, StringBuilder errStr) {
        if (version != null) {
            if (!version.equals(VERSION)) {
                errStr.setLength(0);
                errStr.append("WMTS version not supported");
                return false;
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean accepts(String url, String version) {
        return accepts(url, version, new StringBuilder());
    }
}
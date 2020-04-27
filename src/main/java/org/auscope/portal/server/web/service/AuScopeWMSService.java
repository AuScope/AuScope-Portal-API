package org.auscope.portal.server.web.service;

import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.methodmakers.WMSMethodMakerInterface;
import org.auscope.portal.core.services.responses.ows.OWSExceptionParser;

public class AuScopeWMSService extends WMSService {

    public AuScopeWMSService(HttpServiceCaller serviceCaller, List<WMSMethodMakerInterface> methodMaker) {
        super(serviceCaller, methodMaker);
        // TODO Auto-generated constructor stub
    }
    /**
     * Makes a WMS GetFeatureInfo request using the specified parameters. Returns the response as a string
     *
     * @param wmsUrl
     *            The WMS endpoint (will have any existing query parameters preserved)
     * @param format
     *            The desired mime type of the response
     * @param layer
     *            The name of the layer to download
     * @param srs
     *            The spatial reference system for the bounding box
     * @param westBoundLongitude
     *            The west bound longitude of the bounding box
     * @param southBoundLatitude
     *            The south bound latitude of the bounding box
     * @param eastBoundLongitude
     *            The east bound longitude of the bounding box
     * @param northBoundLatitude
     *            The north bound latitude of the bounding box
     * @param width
     *            The desired output image width in pixels
     * @param height
     *            The desired output image height in pixels
     * @param styles
     *            [Optional] What style should be included
     * @param pointLng
     *            Where the user clicked (longitude)
     * @param pointLat
     *            Where the user clicked (latitude)
     * @param pointX
     *            Where the user clicked in pixel coordinates relative to the GetMap that was used (X direction)
     * @param pointY
     *            Where the user clicked in pixel coordinates relative to the GetMap that was used (Y direction)
     * @return
     * @throws PortalServiceException
     */
    public String getFeatureInfo(String wmsUrl, String format, String layer, String srs, double westBoundLongitude,
            double southBoundLatitude, double eastBoundLongitude, double northBoundLatitude, int width, int height,
            double pointLng, double pointLat, int pointX, int pointY, String styles, String sldBody,
            boolean postMethod,
            String version, String feature_count, boolean attemptOtherVersion) throws PortalServiceException {
        // Do the request
        HttpRequestBase method = null;
        WMSMethodMakerInterface methodMaker;
        try {
            methodMaker = getSupportedMethodMaker(wmsUrl, version);

            if (postMethod) {
                method = methodMaker.getFeatureInfoPost(wmsUrl, format, layer, srs, westBoundLongitude,
                        southBoundLatitude, eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat,
                        pointX, pointY, styles, sldBody, feature_count);
            } else {
                method = methodMaker.getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude, southBoundLatitude,
                        eastBoundLongitude, northBoundLatitude, width, height, pointLng, pointLat, pointX, pointY,
                        styles, sldBody, feature_count);
            }
            String response = serviceCaller.getMethodResponseAsString(method);
            //VT: a html response may not be xml valid therefore cannot go through the same validation process.
            //Rely on the service to return meaningful response to the user.
            if (format.toLowerCase().equals("text/html") ||
                format.toLowerCase().equals("text/plain") ||
                format.toLowerCase().equals("application/vnd.ogc.gml") ||
                format.toLowerCase().equals("application/vnd.ogc.gml/3.1.1")) {
                //VT: Ugly hack for the GA wms layer in registered tab as its font is way too small at 80.
                //VT : GA style sheet also mess up the portal styling of tables as well.
                if (response.contains("table, th, td {")) {
                    response = response.replaceFirst("table, th, td \\{",
                            ".ausga table, .ausga th, .ausga td {");
                    response = response.replaceFirst("th, td \\{", ".ausga th, .ausga td {");
                    response = response.replaceFirst("th \\{", ".ausga th {");
                    response = response.replace("<table", "<table class='ausga'");
                }                
                return response;
            } else {
                OWSExceptionParser.checkForExceptionResponse(response);
                return response;
            }

        } catch (NullPointerException npe) {
            npe.printStackTrace();
            throw new NullPointerException("Call configWMSVersion to setup the right wms method maker to use");

        } catch (Exception ex) {

            //VT:Making this more robust, maybe the wrong version is used;
            if (attemptOtherVersion) {
                for (WMSMethodMakerInterface maker : listOfSupportedWMSMethodMaker) {
                    if (!maker.getSupportedVersion().equals(version)) {
                        try {
                            return this.getFeatureInfo(wmsUrl, format, layer, srs, westBoundLongitude,
                                    southBoundLatitude, eastBoundLongitude, northBoundLatitude, width,
                                    height, pointLng, pointLat, pointX, pointY, styles, sldBody, postMethod,
                                    maker.getSupportedVersion(), feature_count, false);
                        } catch (Exception e) {
                            throw new PortalServiceException(method, "Failure requesting feature info", ex);
                        }
                    }
                }
            } else {
                throw new PortalServiceException(method, "Failure requesting feature info", ex);

            }

        }
        return "";
    }
}

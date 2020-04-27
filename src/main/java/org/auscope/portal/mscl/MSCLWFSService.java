package org.auscope.portal.mscl;

import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.BaseWFSService;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class is a specialisation of BaseWFSService. Its purpose is to allow access to MSCL's observations WFS. It circumvents an issue with the
 * BaseWFSService's generateWFSRequest(...) implementation by allowing a null or blank SRS. The other implementation converted such SRSs to a default value
 * which causes a reprojection to occur later on which it would always fail.
 * 
 * @author bro879
 */
@Service
public class MSCLWFSService extends BaseWFSService {
    /**
     * Creates a new instance of this class with the specified dependencies
     * 
     * @param httpServiceCaller
     *            Will be used for making requests
     * @param wfsMethodMaker
     *            Will be used for generating WFS methods
     */
    @Autowired
    public MSCLWFSService(
            HttpServiceCaller httpServiceCaller,
            WFSGetFeatureMethodMaker wfsMethodMaker) {
        super(httpServiceCaller, wfsMethodMaker);
    }

    /**
     * @param serviceUrl
     *            The URL of the WFS's endpoint. It should be of the form: http://{domain}:{port}/{path}/wfs
     * @param featureType
     *            The name of the feature type you wish to request (including its prefix if necessary).
     * @param featureId
     *            The ID of the feature you want to return.
     * @return The response of a WFS query as a string of XML.
     * @throws ConnectException
     * @throws ConnectTimeoutException
     * @throws UnknownHostException
     * @throws Exception
     */
    public String getWFSReponse(
            String serviceUrl,
            String featureType,
            String featureId) throws ConnectException, ConnectTimeoutException, UnknownHostException, Exception {

        HttpRequestBase method = this.generateWFSRequest(
                serviceUrl,
                featureType,
                featureId,
                "",
                null,
                null,
                WFSGetFeatureMethodMaker.ResultType.Results,
                "");

        return httpServiceCaller.getMethodResponseAsString(method);
    }

    public String getObservations(
            final String serviceUrl,
            final String boreholeHeaderId,
            final String startDepth,
            final String endDepth) throws ConnectException, ConnectTimeoutException, UnknownHostException, Exception {

        // Remove the first part of the id.; we need to use the numerical part when looking up the observations:
        String boreholeHeaderIdDigitsOnly = boreholeHeaderId.replaceFirst("borehole\\.", "");

        String filterString = String.format(
                "<Filter>" +
                        "    <And>" +
                        "        <PropertyIsEqualTo>" +
                        "            <PropertyName>mscl:borehole_header_id</PropertyName>" +
                        "            <Literal>%s</Literal>" +
                        "        </PropertyIsEqualTo>" +
                        "        <PropertyIsBetween>" +
                        "            <PropertyName>mscl:depth</PropertyName>" +
                        "            <LowerBoundary>" +
                        "                <Literal>%s</Literal>" +
                        "            </LowerBoundary>" +
                        "            <UpperBoundary>" +
                        "                <Literal>%s</Literal>" +
                        "            </UpperBoundary>" +
                        "        </PropertyIsBetween>" +
                        "    </And>" +
                        "</Filter>",
                boreholeHeaderIdDigitsOnly,
                startDepth,
                endDepth);

        HttpRequestBase method = this.generateWFSRequest(
                serviceUrl,
                "mscl:scanned_data", // TODO: hard-coding feels bad but this is, after all, an MSCL-specific service...
                null,
                filterString,
                null,
                null,
                WFSGetFeatureMethodMaker.ResultType.Results,
                "");

        return httpServiceCaller.getMethodResponseAsString(method);
    }

    /**
     * Generates a WFS request based on the arguments provided.
     * 
     * @param wfsUrl
     *            The URL of the WFS's endpoint. It should be of the form: http://{domain}:{port}/{path}/wfs
     * @param featureType
     *            The name of the feature type you wish to request (including its prefix if necessary).
     * @param featureId
     *            The ID of the feature you want to return.
     * @param filterString
     *            A filter string, if required.
     * @param maxFeatures
     *            The maximum number of features to return.
     * @param srs
     *            The SRS.
     * @param resultType
     *            The desired result type.
     * @param outputFormat
     *            The desired output format.
     * @return A HttpMethodBase object that encodes the request arguments as an HTTP request.
     */
    @Override
    protected HttpRequestBase generateWFSRequest(
            String wfsUrl,
            String featureType,
            String featureId,
            String filterString,
            Integer maxFeatures,
            String srs,
            ResultType resultType,
            String outputFormat) {
        int max = maxFeatures == null ? 0 : maxFeatures.intValue();
        if (featureId == null) {
            return wfsMethodMaker.makePostMethod(wfsUrl, featureType, filterString, max, srs, resultType, outputFormat);
        } else {
            try {
                return wfsMethodMaker.makeGetMethod(wfsUrl, featureType, featureId, srs, outputFormat);
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }
    }
}
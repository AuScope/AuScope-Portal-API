package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.namespaces.IterableNamespace;
import org.auscope.portal.core.util.DOMUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 *
 * Controller for handling IRIS (Incorporated Research Institutions for Seismology) services
 *
 */
@Controller
public class IRISController extends BasePortalController {
    /**
     * The format with which to encode input stream.
     */
    private static final String ENCODING = "ISO-8859-1";

    /**
     * Makes sure that a string has a trailing forward slash.
     *
     * @param string
     *            The string that you want to have a trailing forward slash.
     * @return The original string, modified if required, with a trailing forward slash.
     */
    private String ensureTrailingForwardslash(String string) {
        // Make sure the trailing separator is in place:
        return string.endsWith("/") ? string : string + "/";
    }

    private NamespaceContext getIrisNamespace() {
        return new IterableNamespace() {
            {
                map.put("default", "http://www.fdsn.org/xml/station/1");
            }
        };
    }

    /**
     * Instantiates the IRISController object.
     *
     * Initialises the xPath object.
     */
    @Autowired
    public IRISController(HttpServiceCaller httpService) {
    }

    /**
     * Downloads a resource from a URL and returns it as Document object.
     *
     * @param queryUrl
     *            The URL of the resource you require.
     * @return The resource at the URL requested, converted to a Document object.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private Document getDocumentFromURL(String queryUrl)
            throws IOException, ParserConfigurationException, SAXException {
        String irisResponse = getIrisResponseFromQuery(queryUrl);
        return DOMUtil.buildDomFromString(irisResponse);
    }

    /**
     * Downloads a resource from a URL and returns it as a String object.
     *
     * @param queryUrl
     *            The URL of the resource you require.
     * @return The resource at the requested URL, as a String object.
     * @throws IOException
     */
    protected String getIrisResponseFromQuery(String queryUrl) throws IOException {
        // NB: This method is protected so that it can be overridden in order to break external dependencies in tests.
        //TODO VT: As part of the review for AGOS-15 , we should be following the same architecture as the rest of portal and use
        // HttpServiceCaller to make any external http requests.
        InputStream inputStream = new URL(queryUrl).openStream();
        Scanner scanner = new Scanner(inputStream, ENCODING);
        String irisResponse = scanner.useDelimiter("\\A").next();
        scanner.close();
        inputStream.close();
        return irisResponse;
    }

    /**
     * Makes a request to the IRIS service specified, for all the stations on the network code provided.
     *
     * The response is converted to some KML points to be rendered on the map.
     *
     * The request will look something like this: http://service.iris.edu/fdsnws/station/1/query?net=S
     *
     * @param serviceUrl
     *            The IRIS web service URL.
     * @param networkCode
     *            The network code that you're interested in.
     * @return a JSONResponseMAV containing KML points of each station.
     */
    @RequestMapping("/getIRISStations.do")
    public ModelAndView getIRISStations(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("networkCode") String networkCode) {
        serviceUrl = ensureTrailingForwardslash(serviceUrl);
        
        // System.out.println("[getIRISStations] serviceUrl ="+serviceUrl+",networkCode ="+networkCode);

        try {
            Document irisDoc = getDocumentFromURL(serviceUrl + "fdsnws/station/1/query?net=" + networkCode + "&level=channel");

            //TODO VT: As part of the review for AGOS-15 , we should be following the same architecture as the rest of portal and
            // create a xslt file to do the transformation from xml to kml
            NodeList stations = irisDoc.getDocumentElement().getElementsByTagName("Station");
            NamespaceContext nc = getIrisNamespace();
            XPathExpression nameExpression = DOMUtil.compileXPathExpr("default:Site/default:Name/text()", nc);
            XPathExpression countryExpression = DOMUtil.compileXPathExpr("default:Site/default:Country/text()", nc);
            XPathExpression latExpression = DOMUtil.compileXPathExpr("default:Latitude/text()", nc);
            XPathExpression lonExpression = DOMUtil.compileXPathExpr("default:Longitude/text()", nc);
            XPathExpression elevationExpression = DOMUtil.compileXPathExpr("default:Elevation/text()", nc);

            StringBuilder kml = new StringBuilder(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document><name>GML Links to KML</name><description>GeoSciML data converted to KML</description>");

            // For each station:
            for (int i = 0; i < stations.getLength(); i++) {
                Node station = stations.item(i);
                Node staCode = station.getAttributes().getNamedItem("code");
                Node startDate = station.getAttributes().getNamedItem("startDate");
                Node endDate = station.getAttributes().getNamedItem("endDate");
                XPathExpression azimuthExpression = DOMUtil.compileXPathExpr("default:Azimuth /text()", nc);
                XPathExpression dipExpression = DOMUtil.compileXPathExpr("default:Dip /text()", nc);
                XPathExpression sampleRateExpression = DOMUtil.compileXPathExpr("default:SampleRate /text()", nc);

                Element staElement = (Element)station;
                NodeList channels =staElement.getElementsByTagName("Channel");
                StringBuilder channelExpr = new StringBuilder();
                // For each channel:
                for (int j = 0; j < channels.getLength(); j++) {
                    Node channel = channels.item(j);
                    Node channelCode = channel.getAttributes().getNamedItem("code");
                    channelExpr.append(
                            String.format(  
                                    "<Channel Code=\"%s\">"+
                                        "<Azimuth>%s</Azimuth>\""+
                                        "<Dip>%s</Dip>"+
                                        "<SampleRate>%s</SampleRate>"+
                                    "</Channel>",
                                    channelCode.getTextContent(),
                                    azimuthExpression.evaluate(channel, XPathConstants.STRING),
                                    dipExpression.evaluate(channel, XPathConstants.STRING),
                                    sampleRateExpression.evaluate(channel, XPathConstants.STRING)));                                   
                }
                String xmlStr = String.format(
                    "<Placemark><name>%s</name>"+
                            "<description>IRIS layer for station: %s></description>" +
                            "<MultiGeometry><Point><coordinates>%s,%s,%s</coordinates></Point></MultiGeometry>"+
                            "<ExtendedData>" +
                                "<Data name=\"Country\"><value>%s</value></Data>"+
                                "<Data name=\"Code\"><value>%s</value></Data>" +
                                "<Data name=\"StartDate\"><value>%s</value></Data>",
                                nameExpression.evaluate(station, XPathConstants.STRING),
                                staCode.getTextContent(),
                                lonExpression.evaluate(station, XPathConstants.STRING),
                                latExpression.evaluate(station, XPathConstants.STRING),
                                elevationExpression.evaluate(station, XPathConstants.STRING),
                                countryExpression.evaluate(station, XPathConstants.STRING),
                                staCode.getTextContent(),
                                startDate.getTextContent());
                // Skip end date as some AusPASS projects have not ended yet
                if (endDate != null) {
                    xmlStr += String.format("<Data name=\"EndDate\"><value>%s</value></Data>", endDate.getTextContent());
                }
                xmlStr += String.format(
                            "</ExtendedData>"+
                            "<Channels>%s</Channels>"+
                            "</Placemark>",
                                channelExpr
                );
                kml.append(xmlStr);
            }
            kml.append("</Document></kml>");

            // & character needs to be escaped i.e. &amp
            String irisResponse = kml.toString().replaceAll("&(?!amp;|apos;|quot;|lt;|gt;)", "&amp;");
            return generateJSONResponseMAV(true, "gml", irisResponse, null);
        } catch (Exception e) {
            return generateJSONResponseMAV(false, e.getMessage(), "Failed.");
        }
    }

    /**
     * Makes a request to the IRIS service for a particular station's channels.
     *
     * The request will look something like this: http://www.iris.edu/ws/station/query?net=S&station=AUDAR&level=chan
     *
     * @param serviceUrl
     *            The IRIS web service URL.
     * @param networkCode
     *            The network code that you're interested in.
     * @param stationCode
     *            The code of the station to interrogate.
     * @return a JSONResponseMAV containing the start and end dates of the site and a collection of channel codes.
     */
    @RequestMapping("/getStationChannels.do")
    public ModelAndView getStationChannels(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("networkCode") String networkCode,
            @RequestParam("stationCode") String stationCode) {
        serviceUrl = ensureTrailingForwardslash(serviceUrl);
        try {
            Document irisDoc = getDocumentFromURL(serviceUrl + "fdsnws/station/1/query?net=" + networkCode + "&sta="
                    + stationCode + "&level=chan");

            NodeList channels = irisDoc.getDocumentElement().getElementsByTagName("Channel");

            String[] channelCodes = new String[channels.getLength()];

            // For each station:
            for (int i = 0; i < channels.getLength(); i++) {
                Node channel = channels.item(i);
                channelCodes[i] = channel.getAttributes().getNamedItem("code").getTextContent();
            }

            NamespaceContext nc = getIrisNamespace();
            String startDate = DOMUtil.compileXPathExpr("//default:Station[@code='" + stationCode + "']/default:Channel/@startDate", nc)
                    .evaluate(irisDoc, XPathConstants.STRING).toString();
            String endDate = DOMUtil.compileXPathExpr("//default:Station[@code='" + stationCode + "']/default:Channel/@endDate", nc)
                    .evaluate(irisDoc, XPathConstants.STRING).toString();

            ModelMap channelInfo = new ModelMap();
            channelInfo.put("start_date", startDate);
            channelInfo.put("end_date", endDate);
            channelInfo.put("channel_codes", channelCodes);

            return generateJSONResponseMAV(true, channelInfo, "OK");
        } catch (Exception e) {
            return generateJSONResponseMAV(false, e.getMessage(), "Failed.");
        }
    }

    /**
     * Makes a request to IRIS service for time series data
     *
     * @param serviceUrl
     *            The IRIS web service URL.
     * @param networkCode
     *            The network code that you're interested in.
     * @param stationCode
     *            The code of the station to interrogate.
     * @param channel
     *            The channel that you're interested in.
     * @param start
     *            Time series start date.
     * @param duration
     *            Time series duration.
     * @param output
     *            Output format.
     *
     * @return a JSONResponseMAV containing the time series data
     */
    @RequestMapping("/getTimeseriesUrl.do")
    public ModelAndView getTimeseriesUrl(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("networkCode") String networkCode,
            @RequestParam("stationCode") String stationCode,
            @RequestParam("channel") String channel,
            @RequestParam("start") String start,
            @RequestParam("duration") String duration,
            @RequestParam("output") String output,
            HttpServletResponse response) throws ServletException {

        serviceUrl = ensureTrailingForwardslash(serviceUrl);
        String url = serviceUrl + "irisws/timeseries/1/query?net=" + networkCode +
                "&sta=" + stationCode +
                "&cha=" + channel +
                "&starttime=" + start +
                "&duration=" + duration +
                "&output=" + output +
                "&loc=--";//VT: i removed ref=xml so because the url returned seem to be unresolvable.

        return generateJSONResponseMAV(true, url, "OK");

    }
}
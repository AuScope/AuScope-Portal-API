package org.auscope.portal.server.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.server.vegl.VglDownload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller class for handling the creation of ERRDAP and other download requests
 * @author Josh Vote
 *
 */
@Controller
public class JobDownloadController extends BasePortalController {

    /**
     * Name of the session variable where a List<VglDownload> resides
     */
    public static final String SESSION_DOWNLOAD_LIST = "jobDownloadList";


    protected final Log logger = LogFactory.getLog(getClass());
    private PortalPropertyPlaceholderConfigurer hostConfigurer;

    @Autowired
    public JobDownloadController(PortalPropertyPlaceholderConfigurer hostConfigurer) {
        this.hostConfigurer = hostConfigurer;
    }

    /**
     * Utility for adding a single VglDownload object to the session based array of VglDownload objects.
     * @param request
     * @param download
     */
    private void addDownloadToSession(HttpServletRequest request, VglDownload download) {
        @SuppressWarnings("unchecked")
        List<VglDownload> erddapUrlList = (List<VglDownload>) request.getSession().getAttribute(SESSION_DOWNLOAD_LIST);
        if (erddapUrlList == null) {
            erddapUrlList = new ArrayList<VglDownload>();
        }

        logger.trace("Adding download: " + download.getUrl());
        synchronized(erddapUrlList) {
            erddapUrlList.add(download);
        }

        request.getSession().setAttribute(SESSION_DOWNLOAD_LIST, erddapUrlList);
    }

    /**
     * Adds user selected file(s) or download request(s) to the session wide SESSION_OWNLOAD_LIST list.
     * @return ModelAndView response object.
     */
    @RequestMapping("/addSelectedResourcesToSession.do")
    public ModelAndView addSelectedResourcesToSession(@RequestParam("url") String[] url,
            @RequestParam("name") String[] name,
            @RequestParam("description") String[] description,
            @RequestParam("localPath") String[] localPath,
            @RequestParam("northBoundLatitude") final Double[] northBoundLatitude,
            @RequestParam("eastBoundLongitude") final Double[] eastBoundLongitude,
            @RequestParam("southBoundLatitude") final Double[] southBoundLatitude,
            @RequestParam("westBoundLongitude") final Double[] westBoundLongitude,
            HttpServletRequest request) {

        for (int i = 0; i < url.length; i++) {
            VglDownload newDownload = new VglDownload();
            newDownload.setName(name[i]);
            newDownload.setDescription(description[i]);
            newDownload.setLocalPath(localPath[i]);
            newDownload.setUrl(url[i]);
            newDownload.setNorthBoundLatitude(northBoundLatitude[i]);
            newDownload.setEastBoundLongitude(eastBoundLongitude[i]);
            newDownload.setSouthBoundLatitude(southBoundLatitude[i]);
            newDownload.setWestBoundLongitude(westBoundLongitude[i]);

            addDownloadToSession(request, newDownload);
        }

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Adds a new download request to the session wide SESSION_OWNLOAD_LIST list. This list
     * will be added to the next job the user creates
     * @return
     */
    @RequestMapping("/addDownloadRequestToSession.do")
    public ModelAndView addDownloadRequestToSession(@RequestParam("url") String url,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("localPath") String localPath,
            @RequestParam("northBoundLatitude") final Double northBoundLatitude,
            @RequestParam("eastBoundLongitude") final Double eastBoundLongitude,
            @RequestParam("southBoundLatitude") final Double southBoundLatitude,
            @RequestParam("westBoundLongitude") final Double westBoundLongitude,
            HttpServletRequest request) {

        VglDownload newDownload = new VglDownload();
        newDownload.setName(name);
        newDownload.setDescription(description);
        newDownload.setLocalPath(localPath);
        newDownload.setUrl(url);
        newDownload.setNorthBoundLatitude(northBoundLatitude);
        newDownload.setEastBoundLongitude(eastBoundLongitude);
        newDownload.setSouthBoundLatitude(southBoundLatitude);
        newDownload.setWestBoundLongitude(westBoundLongitude);

        addDownloadToSession(request, newDownload);

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Adds a new ERDDAP request to the session wide SESSION_OWNLOAD_LIST list. This list
     * will be added to the next job the user creates
     * @return
     * @throws Exception
     */
    @RequestMapping("/addErddapRequestToSession.do")
    public ModelAndView addErddapRequestToSession(@RequestParam("northBoundLatitude") final Double northBoundLatitude,
                                @RequestParam("eastBoundLongitude") final Double eastBoundLongitude,
                                @RequestParam("southBoundLatitude") final Double southBoundLatitude,
                                @RequestParam("westBoundLongitude") final Double westBoundLongitude,
                                @RequestParam("format") final String format,
                                @RequestParam("layerName") final String layerName,
                                @RequestParam("name") final String name,
                                @RequestParam("description") final String description,
                                @RequestParam("localPath") final String localPath,
                                HttpServletRequest request,
                                HttpServletResponse response) throws Exception {

        String serviceUrl = hostConfigurer.resolvePlaceholder("HOST.erddapservice.url");
        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox(westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude);
        String erddapUrl = getCoverageSubsetUrl(bbox, serviceUrl, layerName, format);

        // Append this download list to the existing list of download objects
        VglDownload newDownload = new VglDownload();
        newDownload.setName(name);
        newDownload.setDescription(description);
        newDownload.setLocalPath(localPath);
        newDownload.setUrl(erddapUrl);
        newDownload.setNorthBoundLatitude(northBoundLatitude);
        newDownload.setEastBoundLongitude(eastBoundLongitude);
        newDownload.setSouthBoundLatitude(southBoundLatitude);
        newDownload.setWestBoundLongitude(westBoundLongitude);

        addDownloadToSession(request, newDownload);

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Takes the co-ordinates of a user drawn bounding box and constructs an ERDDAP
     * coverage subset request URL.
     *
     * @param coords The lat/lon co-ordinates of the user drawn bounding box
     * @param serviceUrl The remote URL to query
     * @param layerName The coverage layername to request
     * @return The ERDDAP coverage subset request URL
     */
    private String getCoverageSubsetUrl(CSWGeographicBoundingBox bbox, String serviceUrl, String layerName, String format) {
        logger.debug(String.format("serviceUrl='%1$s' bbox='%2$s' layerName='%3$s'", serviceUrl, bbox, layerName));

        // convert bbox co-ordinates to ERDDAP an ERDDAP dimension string
        String erddapDimensions = "%5B("+ bbox.getSouthBoundLatitude() +"):1:("+ bbox.getNorthBoundLatitude() +
        ")%5D%5B("+ bbox.getWestBoundLongitude() +"):1:("+ bbox.getEastBoundLongitude() +")%5D";

        String url = serviceUrl + layerName + "." + format + "?" + layerName + erddapDimensions;

        return url;
    }
}
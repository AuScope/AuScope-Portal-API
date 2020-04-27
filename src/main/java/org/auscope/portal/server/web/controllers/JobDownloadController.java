package org.auscope.portal.server.web.controllers;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.WCSService;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
    private SimpleWfsService wfsService;
    private WCSService wcsService;


    private String erddapServiceUrl;

    @Autowired
    public JobDownloadController(SimpleWfsService wfsService, WCSService wcsService, @Value("${erddapservice.url}") String erddapServiceUrl) {
        this.wfsService = wfsService;
        this.wcsService = wcsService;
        this.erddapServiceUrl=erddapServiceUrl;
    }

    private static ModelMap toView(VglDownload dl) {
        ModelMap map = new ModelMap();
        map.put("url", dl.getUrl());
        map.put("northBoundLatitude", dl.getNorthBoundLatitude());
        map.put("southBoundLatitude", dl.getSouthBoundLatitude());
        map.put("eastBoundLongitude", dl.getEastBoundLongitude());
        map.put("westBoundLongitude", dl.getWestBoundLongitude());
        map.put("name", dl.getName());
        map.put("description", dl.getDescription());
        map.put("localPath", dl.getLocalPath());
        return map;
    }

    /**
     * // TODO: No longer using session vars
     * 
     * Utility for adding a single VglDownload object to the session based array of VglDownload objects.
     * @param request
     * @param download
     */
    private void addDownloadToSession(HttpServletRequest request, VglDownload download) {
        @SuppressWarnings("unchecked")
        List<VglDownload> erddapUrlList = (List<VglDownload>) request.getSession().getAttribute(SESSION_DOWNLOAD_LIST);
        if (erddapUrlList == null) {
            erddapUrlList = new ArrayList<>();
        }

        logger.trace("Adding download: " + download.getUrl());
        synchronized(erddapUrlList) {
            erddapUrlList.add(download);
        }

        request.getSession().setAttribute(SESSION_DOWNLOAD_LIST, erddapUrlList);
    }

    /**
     * Creates a new VL Download object from a remote URL. The Download object is returned. If saveSession
     * is true the download object will also be saved to the session wide SESSION_DOWNLOAD_LIST list.
     * @return
     */
    @RequestMapping("/makeDownloadUrl.do")
    public ModelAndView makeDownloadUrl(@RequestParam("url") String url,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam(required = false, value = "fullDescription") final String fullDescription,
            @RequestParam("localPath") String localPath,
            @RequestParam("northBoundLatitude") final Double northBoundLatitude,
            @RequestParam("eastBoundLongitude") final Double eastBoundLongitude,
            @RequestParam("southBoundLatitude") final Double southBoundLatitude,
            @RequestParam("westBoundLongitude") final Double westBoundLongitude,
            //@RequestParam(required = false, value = "parentName") String parentName,
            //@RequestParam(required = false, value = "parentUrl") String parentUrl,
            //@RequestParam(required = false, value = "owner") String owner,
            @RequestParam(required=false,defaultValue="false",value="saveSession") final boolean saveSession,
            HttpServletRequest request) {

        VglDownload newDownload = new VglDownload();
        newDownload.setName(name);
        newDownload.setDescription(fullDescription == null ? description : fullDescription);
        newDownload.setLocalPath(localPath);
        newDownload.setUrl(url);
        newDownload.setNorthBoundLatitude(northBoundLatitude);
        newDownload.setEastBoundLongitude(eastBoundLongitude);
        newDownload.setSouthBoundLatitude(southBoundLatitude);
        newDownload.setWestBoundLongitude(westBoundLongitude);
        /*
        newDownload.setOwner(owner);
        newDownload.setParentName(parentName);
        newDownload.setParentUrl(parentUrl);
        */

        if (saveSession) {
            addDownloadToSession(request, newDownload);
        }

        return generateJSONResponseMAV(true, toView(newDownload), "");
    }

    /**
     * Creates a new VL Download object from a some ERDDAP parameters. The Download object is returned. If saveSession
     * is true the download object will also be saved to the session wide SESSION_DOWNLOAD_LIST list.
     * @return
     * @throws Exception
     */
    @RequestMapping("/makeErddapUrl.do")
    public ModelAndView makeErddapUrl(@RequestParam("northBoundLatitude") final Double northBoundLatitude,
                                @RequestParam("eastBoundLongitude") final Double eastBoundLongitude,
                                @RequestParam("southBoundLatitude") final Double southBoundLatitude,
                                @RequestParam("westBoundLongitude") final Double westBoundLongitude,
                                @RequestParam("format") final String format,
                                @RequestParam("layerName") final String layerName,
                                @RequestParam("name") final String name,
                                @RequestParam("description") final String description,
                                @RequestParam(required = false, value = "fullDescription") final String fullDescription,
                                @RequestParam("localPath") final String localPath,
                                //@RequestParam(required = false, value = "parentName") String parentName,
                                //@RequestParam(required = false, value = "parentUrl") String parentUrl,
                                //@RequestParam(required = false, value = "owner") String owner,
                                @RequestParam(required=false,defaultValue="false",value="saveSession") final boolean saveSession,
                                HttpServletRequest request,
                                HttpServletResponse response) {

        String serviceUrl = erddapServiceUrl;
        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox(westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude);
        String erddapUrl = getCoverageSubsetUrl(bbox, serviceUrl, layerName, format);

        // Append this download list to the existing list of download objects
        VglDownload newDownload = new VglDownload();
        newDownload.setName(name);
        newDownload.setDescription(fullDescription == null ? description : fullDescription);
        newDownload.setLocalPath(localPath);
        newDownload.setUrl(erddapUrl);
        newDownload.setNorthBoundLatitude(northBoundLatitude);
        newDownload.setEastBoundLongitude(eastBoundLongitude);
        newDownload.setSouthBoundLatitude(southBoundLatitude);
        newDownload.setWestBoundLongitude(westBoundLongitude);
        /*
        newDownload.setOwner(owner);
        newDownload.setParentName(parentName);
        newDownload.setParentUrl(parentUrl);
        */

        if (saveSession) {
            addDownloadToSession(request, newDownload);
        }

        return generateJSONResponseMAV(true, toView(newDownload), "");
    }

    /**
     * Creates a new VGL Download object from some NCSS parameters. The Download object is returned. If saveSession
     * is true the download object will also be saved to the session wide SESSION_DOWNLOAD_LIST list.
     * @return
     * @throws Exception
     */

    @RequestMapping("/makeNetcdfsubseserviceUrl.do")
    public ModelAndView makeNetcdfsubsetserviceUrl(@RequestParam("url") String url,
                                @RequestParam("northBoundLatitude") final Double northBoundLatitude,
                                @RequestParam("eastBoundLongitude") final Double eastBoundLongitude,
                                @RequestParam("southBoundLatitude") final Double southBoundLatitude,
                                @RequestParam("westBoundLongitude") final Double westBoundLongitude,
                                @RequestParam("name") final String name,
                                @RequestParam("description") final String description,
                                @RequestParam(required = false, value = "fullDescription") final String fullDescription,
                                @RequestParam("localPath") final String localPath,
                                //@RequestParam(required = false, value = "parentName") String parentName,
                                //@RequestParam(required = false, value = "parentUrl") String parentUrl,
                                //@RequestParam(required = false, value = "owner") String owner,
                                @RequestParam(required=false,defaultValue="false",value="saveSession") final boolean saveSession,
                                HttpServletRequest request,
                                HttpServletResponse response) {

        //String serviceUrl = hostConfigurer.resolvePlaceholder("HOST.erddapservice.url");
        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox(westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude);
        String netcdfsubsetserviceUrl = getNetcdfSubsetUrl(bbox, url, name, description, "nc");

        // Append this download list to the existing list of download objects
        VglDownload newDownload = new VglDownload();
        newDownload.setName(name);
        newDownload.setDescription(fullDescription == null ? description : fullDescription);
        newDownload.setLocalPath(localPath);
        newDownload.setUrl(netcdfsubsetserviceUrl);
        newDownload.setNorthBoundLatitude(northBoundLatitude);
        newDownload.setEastBoundLongitude(eastBoundLongitude);
        newDownload.setSouthBoundLatitude(southBoundLatitude);
        newDownload.setWestBoundLongitude(westBoundLongitude);
        /*
        newDownload.setOwner(owner);
        newDownload.setParentName(parentName);
        newDownload.setParentUrl(parentUrl);
		*/

        if (saveSession) {
            addDownloadToSession(request, newDownload);
        }

        return generateJSONResponseMAV(true, toView(newDownload), "");
    }


    /**
     * Creates a new VL Download object from some WFS parameters. The Download object is returned. If saveSession
     * is true the download object will also be saved to the session wide SESSION_DOWNLOAD_LIST list.
     *
     * @param serviceUrl The WFS endpoint
     * @param featureType The feature type name to query
     * @param maxFeatures [Optional] The maximum number of features to query
     */
    @RequestMapping("/makeWfsUrl.do")
    public ModelAndView makeWfsUrl(@RequestParam("serviceUrl") final String serviceUrl,
                                           @RequestParam("featureType") final String featureType,
                                           @RequestParam(required = false, value = "srsName") final String srsName,
                                           @RequestParam(required = false, value = "crs") final String bboxCrs,
                                           @RequestParam(required = false, value = "northBoundLatitude") final Double northBoundLatitude,
                                           @RequestParam(required = false, value = "southBoundLatitude") final Double southBoundLatitude,
                                           @RequestParam(required = false, value = "eastBoundLongitude") final Double eastBoundLongitude,
                                           @RequestParam(required = false, value = "westBoundLongitude") final Double westBoundLongitude,
                                           @RequestParam(required = false, value = "outputFormat") final String outputFormat,
                                           @RequestParam(required = false, value = "maxFeatures") Integer maxFeatures,
                                           @RequestParam("name") final String name,
                                           @RequestParam("description") final String description,
                                           @RequestParam(required = false, value = "fullDescription") final String fullDescription,
                                           @RequestParam("localPath") final String localPath,
                                           //@RequestParam(required = false, value = "parentName") String parentName,
                                           //@RequestParam(required = false, value = "parentUrl") String parentUrl,
                                           //@RequestParam(required = false, value = "owner") String owner,
                                           @RequestParam(required=false,defaultValue="false",value="saveSession") final boolean saveSession,
                                           HttpServletRequest request) {

        FilterBoundingBox bbox = null;
        if (northBoundLatitude != null) {
            bbox = FilterBoundingBox.parseFromValues(bboxCrs, northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude);
        }

        String response = null;

        try {
            response = wfsService.getFeatureRequestAsString(serviceUrl, featureType, bbox, maxFeatures, srsName, outputFormat);
        } catch (Exception ex) {
            log.warn(String.format("Exception generating service request for '%2$s' from '%1$s': %3$s", serviceUrl, featureType, ex));
            log.debug("Exception: ", ex);
            return generateExceptionResponse(ex, serviceUrl);
        }

        VglDownload newDownload = new VglDownload();
        newDownload.setName(name);
        newDownload.setDescription(fullDescription == null ? description : fullDescription);
        newDownload.setLocalPath(localPath);
        newDownload.setUrl(response);
        newDownload.setNorthBoundLatitude(northBoundLatitude);
        newDownload.setEastBoundLongitude(eastBoundLongitude);
        newDownload.setSouthBoundLatitude(southBoundLatitude);
        newDownload.setWestBoundLongitude(westBoundLongitude);
        /*
        newDownload.setOwner(owner);
        newDownload.setParentName(parentName);
        newDownload.setParentUrl(parentUrl);
		*/

        if (saveSession) {
            addDownloadToSession(request, newDownload);
        }

        return generateJSONResponseMAV(true, toView(newDownload), "");
    }

    /**
     * Creates a new VL Download object from some WCS parameters. The Download object is returned. If saveSession
     * is true the download object will also be saved to the session wide SESSION_DOWNLOAD_LIST list.
     * 
     * TODO: Do we need time constraint (TimeConstraint) and custom params (Map<String,String>)?
     *
     * @param serviceUrl The WCS endpoint
     * @param coverageName
     * @param format
     * @param outputCrs
     * @param outputWidth
     * @param outputHeight
     * @param outputResolutionX
     * @param outputResolutionY
     * @param inputCrs
     * @param northBoundLatitude
     * @param southBoundLatitude
     * @param eastBoundLongitude
     * @param westBoundLongitude
     * @param name
     * @param description
     * @param fullDescription
     * @param localPath
     * @param saveSession
     * @param request
     * @return
     */
    @RequestMapping("/makeWcsUrl.do")
    public ModelAndView makeWcsUrl(@RequestParam("serviceUrl") final String serviceUrl,
                                   @RequestParam("coverageName") final String coverageName,
                                   @RequestParam(required = false, value = "format") final String format,
                                   @RequestParam(required = false, value = "crs") final String inputCrs,
                                   @RequestParam(required = false, value = "outputCrs") final String outputCrs,
                                   @RequestParam(required = false, value = "outputWidth") final Integer outputWidth,
                                   @RequestParam(required = false, value = "outputHeight") final Integer outputHeight,
                                   @RequestParam(required = false, value = "outputResolutionX") final Double outputResolutionX,
                                   @RequestParam(required = false, value = "outputResolutionY") final Double outputResolutionY,
                                   @RequestParam(required = false, value = "northBoundLatitude") final Double northBoundLatitude,
                                   @RequestParam(required = false, value = "southBoundLatitude") final Double southBoundLatitude,
                                   @RequestParam(required = false, value = "eastBoundLongitude") final Double eastBoundLongitude,
                                   @RequestParam(required = false, value = "westBoundLongitude") final Double westBoundLongitude,
                                   @RequestParam("name") final String name,
                                   @RequestParam("description") final String description,
                                   @RequestParam(required = false, value = "fullDescription") final String fullDescription,
                                   @RequestParam("localPath") final String localPath,
                                   @RequestParam(required=false,defaultValue="false",value="saveSession") final boolean saveSession,
                                   HttpServletRequest request) {

    	Dimension outputSize = null;
    	if(outputWidth != null && outputHeight != null) {
    		outputSize = new Dimension(outputWidth, outputHeight);
    	}
    	
    	Resolution outputResolution = null;
    	if(outputResolutionX != null && outputResolutionY != null) {
    		outputResolution = new Resolution(outputResolutionX, outputResolutionY);
    	}
    	
        CSWGeographicBoundingBox bbox = null;
        if (northBoundLatitude != null) {
        	bbox = new CSWGeographicBoundingBox(westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude);
        }
        
        String response = null;
        try {
            response = wcsService.getCoverageRequestAsString(serviceUrl, coverageName, format, outputCrs, outputSize, outputResolution, inputCrs, bbox, null, null);
        } catch (Exception ex) {
            log.warn(String.format("Exception generating service request for '%2$s' from '%1$s': %3$s", serviceUrl, coverageName, ex));
            log.debug("Exception: ", ex);
            return generateExceptionResponse(ex, serviceUrl);
        }

        VglDownload newDownload = new VglDownload();
        newDownload.setName(name);
        newDownload.setDescription(fullDescription == null ? description : fullDescription);
        newDownload.setLocalPath(localPath);
        newDownload.setUrl(response);
        newDownload.setNorthBoundLatitude(northBoundLatitude);
        newDownload.setEastBoundLongitude(eastBoundLongitude);
        newDownload.setSouthBoundLatitude(southBoundLatitude);
        newDownload.setWestBoundLongitude(westBoundLongitude);

        if (saveSession) {
            addDownloadToSession(request, newDownload);
        }

        return generateJSONResponseMAV(true, toView(newDownload), "");
    }

    /**
     * Get the number of download requests stored in user session. This method
     * will be used by VL frontend to check if any data set has been captured
     * before creating a new job.
     *
     * @param request The servlet request with query parameters
     * @return number of download requests in user session.
     */
    @RequestMapping("/getNumDownloadRequests.do")
    public ModelAndView getNumDownloadRequests(HttpServletRequest request) {
        int size = 0;
        List<?> downloadList = (List<?>)request.getSession().getAttribute(SESSION_DOWNLOAD_LIST);
        if (downloadList != null && downloadList.size() > 0) {
            size = downloadList.size();
        }
        return generateJSONResponseMAV(true, size, "");
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

    /**
     * Takes the co-ordinates of a user drawn bounding box and constructs an Netcdf
     * subset request URL.
     *
     * @param coords The lat/lon co-ordinates of the user drawn bounding box
     * @param serviceUrl The remote URL to query
     * @param layerName The coverage layername to request
     * @return The NCSS subset request URL
     */
    private String getNetcdfSubsetUrl(CSWGeographicBoundingBox bbox, String serviceUrl, String name, String description, String format) {
        logger.debug(String.format("serviceUrl='%1$s' bbox='%2$s' layerName='%3$s'  layerDescription='%4$s'", serviceUrl, bbox, name, description));

        // convert bbox co-ordinates to an netcdfsubsetservice dimension string
        String netcdfsubsetserviceDimensions = "&spatial=bb" +
        		"&north="+ bbox.getNorthBoundLatitude() +
        		"&south=" + bbox.getSouthBoundLatitude() +
        		"&west=" + bbox.getWestBoundLongitude() +
        		"&east="+ bbox.getEastBoundLongitude();
        String otherParams = "";//"&temporal=all&time_start=&time_end=&horizStride=";

        String url = serviceUrl + "?var=" + name + netcdfsubsetserviceDimensions + otherParams;

        return url;
    }

}

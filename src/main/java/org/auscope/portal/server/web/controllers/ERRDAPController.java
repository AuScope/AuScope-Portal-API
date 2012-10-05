package org.auscope.portal.server.web.controllers;

import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * A controller class for handling the creation of ERRDAP requests
 * @author Josh Vote
 *
 */
@Controller
public class ERRDAPController extends BasePortalController {

    public static final String SESSION_ERRDAP_DOWNLOAD_LIST = "errdapDownloadList";

    //This is a file path for a CENTOS VM
    public static final String ERRDAP_SUBSET_VM_FILE_PATH = "/tmp/vegl-subset.csv";


    protected final Log logger = LogFactory.getLog(getClass());
    private PortalPropertyPlaceholderConfigurer hostConfigurer;

    @Autowired
    public ERRDAPController(PortalPropertyPlaceholderConfigurer hostConfigurer) {
        this.hostConfigurer = hostConfigurer;
    }

    /**
     * Calculates the likely MGA zone that the user should use. This zone is determined by finding what zone
     * the center of the selected region is in.
     *
     * @param layerName The name of the layer that the area has been selected from
     * @param dataCoords The lat/lon co-ordinates that were selected
     * @param format The desired format of the subset file
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return
     * @throws Exception
     */
    @RequestMapping("/calculateMgaZone.do")
    public ModelAndView calculateMgaZone(@RequestParam("dataCoords") final String dataCoords,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        String[] coords = dataCoords.split(",");
        double lngMin = Double.parseDouble(coords[0]);
        double lngMax = Double.parseDouble(coords[2]);

        // calculate likely MGA zone
        double lngDiff = lngMax - lngMin;
        double lngCenter = lngMin + (lngDiff/2);
        int mgaZone = 0;

        if (lngCenter >= 108 && lngCenter < 114){
            mgaZone = 49;
        } else if (lngCenter >= 114 && lngCenter < 120){
            mgaZone = 50;
        } else if (lngCenter >= 120 && lngCenter < 126){
            mgaZone = 51;
        } else if (lngCenter >= 126 && lngCenter < 132){
            mgaZone = 52;
        } else if (lngCenter >= 132 && lngCenter < 138){
            mgaZone = 53;
        } else if (lngCenter >= 138 && lngCenter < 144){
            mgaZone = 54;
        } else if (lngCenter >= 144 && lngCenter < 150){
            mgaZone = 55;
        } else if (lngCenter >= 150 && lngCenter < 156){
            mgaZone = 56;
        } else {
            logger.error("could not calculate MGA zone");
            return generateJSONResponseMAV(false, null, "Could not calculate MGA zone");
        }

        return generateJSONResponseMAV(true, mgaZone, "");
    }

    /**
     * Projects the lat/lng co-ordinates to UTM and increases the bounds to fully accommodate the user
     * entered inversion cell dimensions. The x, y and z cell dimensions need to be a divisor of the
     * width, height and depth values.
     *
     * @param layerName The name of the layer that the area has been selected from
     * @param dataCoords The lat/lon co-ordinates that were selected
     * @param format The desired format of the subset file
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return
     * @throws Exception
     */
    @RequestMapping("/projectToUtm.do")
    public ModelAndView projectToUtm(@RequestParam("dataCoords") final String dataCoords,
                                @RequestParam("mgaZone") final int mgaZone,
                                @RequestParam("xCellDim") final int xCellDim,
                                @RequestParam("yCellDim") final int yCellDim,
                                @RequestParam("zCellDim") final int zCellDim,
                                @RequestParam("maxDepth") final int maxDepth,
                                HttpServletRequest request,
                                HttpServletResponse response) throws Exception {

        String[] coords = dataCoords.split(",");
        double lngMin = Double.parseDouble(coords[0]);
        double latMin = Double.parseDouble(coords[1]);
        double lngMax = Double.parseDouble(coords[2]);
        double latMax = Double.parseDouble(coords[3]);

        // create new projection object
        Projection projection = ProjectionFactory.fromPROJ4Specification(
                new String[] {
                    "+proj=utm", // Projection name
                    "+zone=" + mgaZone, //UTM zone
                    "+ellps=WGS84", // Ellipsoid name
                    "+x_0=500000", // False easting
                    "+y_0=10000000", // False northing
                    "+k_0=0.99960000" // Scaling factor (new name)
                }
            );

        // project the selected region into appropriate UTM projection
        Rectangle2D rect = new Rectangle2D.Double(lngMin, latMin, (lngMax - lngMin), (latMax - latMin));
        Rectangle2D approxAreaMga = projection.transform(rect);

        // Calculate bounding box which fully encompasses this polygon
        int minEast = (int)Math.floor(approxAreaMga.getMinX());
        int maxEast = (int)Math.ceil(approxAreaMga.getMaxX());
        int minNorth = (int)Math.floor(approxAreaMga.getMinY());
        int maxNorth = (int)Math.ceil(approxAreaMga.getMaxY());

        // validate the dimensions entered by the client. The bounding box will be adjusted to fit if required.
        int [] eastingArray = validateCellDimensions(minEast, maxEast, xCellDim);
        int [] northingArray = validateCellDimensions(minNorth, maxNorth, yCellDim);
        int [] depthArray = validateCellDimensions(0, maxDepth, zCellDim);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("eastingArray", eastingArray);
        data.put("northingArray", northingArray);
        data.put("depthArray", depthArray);
        data.put("success", true);
        return generateJSONResponseMAV(true, data, "");
    }


    /**
     * Adds a new ERDDAP request to the session wide SESSION_ERRDAP_DOWNLOAD_LIST list.
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
        @SuppressWarnings("unchecked")
        List<VglDownload> erddapUrlList = (List<VglDownload>) request.getSession().getAttribute(SESSION_ERRDAP_DOWNLOAD_LIST);
        if (erddapUrlList == null) {
            erddapUrlList = new ArrayList<VglDownload>();
        }
        VglDownload newDownload = new VglDownload();
        newDownload.setName(name);
        newDownload.setDescription(description);
        newDownload.setLocalPath(localPath);
        newDownload.setUrl(erddapUrl);
        newDownload.setNorthBoundLatitude(northBoundLatitude);
        newDownload.setEastBoundLongitude(eastBoundLongitude);
        newDownload.setSouthBoundLatitude(southBoundLatitude);
        newDownload.setWestBoundLongitude(westBoundLongitude);
        erddapUrlList.add(newDownload);

        logger.debug("erddapUrl: " + erddapUrl);

        request.getSession().setAttribute(SESSION_ERRDAP_DOWNLOAD_LIST, erddapUrlList);

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * This method checks that the desired cell dimensions are able to fit completely within the
     * bounds of the bounding box. If they cannot then the bounds of the box will be enlarged to
     * fit them. Eg, if the width of the bounding box is 100 and the X cell dimension is 30, the
     * width will be increased to 120 to accommodate a full set of cells. The cell dimensions are
     * used during later processing to build a 3D mesh.
     *
     * @param minValue The minimum value of the axis range
     * @param maxValue The maximum value of the axis range
     * @param cellDimension The size of the cell dimension for a particular axis.
     * @return an int array containing the correct min and max bounding box range values
     */
    private int [] validateCellDimensions(int minValue, int maxValue, int cellDimension) {

        // check if the cells fit the bounds without any remainder
        int reamainder = (maxValue - minValue) % cellDimension;

        if (reamainder != 0) {
            // calculate how much the bounds need to grow to accommodate whole cells
            int diff = cellDimension - reamainder;

            // if minValue is zero then add the difference to the maxValue
            if (minValue == 0) {
                maxValue = maxValue + diff;
            } else if (diff % 2 == 0){
                // if the difference can be split evenly then half is added as a buffer to each value
                   minValue = minValue - diff / 2;
                   maxValue = maxValue + diff / 2;
            } else {
                // take a bit more off the minimum if it's not even
                int intDiff = (int) diff / 2;
                   minValue = minValue - (intDiff + 1);
                maxValue = maxValue + intDiff;
            }

            logger.info("Bbox axis range adjusted to fit cell dimensions");
        }

        int [] bboxAxisRange = {minValue, maxValue};
        return bboxAxisRange;
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

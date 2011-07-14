package org.auscope.portal.server.web.controllers;

import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
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
public class ERRDAPController extends BaseVEGLController {
	
	public static final String SESSION_ERRDAP_URL_MAP = "errdapUrlMap";
	
	public static final String SESSION_SELECTION_MIN_EASTING = "job-selection-mineast";
	public static final String SESSION_SELECTION_MAX_EASTING = "job-selection-maxeast";
	public static final String SESSION_SELECTION_MIN_NORTHING = "job-selection-minnorth";
	public static final String SESSION_SELECTION_MAX_NORTHING = "job-selection-maxnorth";
	
	public static final String SESSION_PADDED_MIN_EASTING = "job-padded-mineast";
	public static final String SESSION_PADDED_MAX_EASTING = "job-padded-maxeast";
	public static final String SESSION_PADDED_MIN_NORTHING = "job-padded-minnorth";
	public static final String SESSION_PADDED_MAX_NORTHING = "job-padded-maxnorth";
	public static final String SESSION_MGA_ZONE = "job-mgazone";
	
	public static final String SESSION_INVERSION_DEPTH = "job-inversiondepth";
	public static final String SESSION_CELL_X = "job-cellx";
	public static final String SESSION_CELL_Y = "job-celly";
	public static final String SESSION_CELL_Z = "job-cellz";
	
	
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
     * Adds a buffer around the subset area based on the depth of inversion and projects back to lat/lng. The lat/lng 
     * co-ordinates are then used to create an ERDDAP request for getting the subset file. The buffer is required because 
     * The modelling that is applied on the Eucalyptus VM to the data in the area selected by the user, is affected by 
     * data outside these bounds. For this reason we need to apply a buffer around the selected area so we end up 
     * capturing a larger subset than what was selected.
     * 
     * @param dataCoords The original lat/lon co-ordinates that were selected
     * @param mgaZone The MGA zone that was used for the UTM projection
     * @param xCellDim The x axis cell dimension value
     * @param yCellDim The y axis cell dimension value
     * @param zCellDim The z axis cell dimension value
     * @param maxDepth The max depth of inversion
     * @param minEast The minimum easting value of the UTM co-ordinates
     * @param maxEast The maximum easting value of the UTM co-ordinates
     * @param minNorth The minimum northing value of the UTM co-ordinates
     * @param maxNorth The maximum northing value of the UTM co-ordinates
     * @param layerName The name of the layer being subset
     * @param format The desired format of the subset output file
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @return
     * @throws Exception
     */
    @RequestMapping("/createErddapRequest.do")
    public ModelAndView createErddapRequest(@RequestParam("dataCoords") final String dataCoords,
    							@RequestParam("mgaZone") final int mgaZone,
    							@RequestParam("xCellDim") final int xCellDim,
    							@RequestParam("yCellDim") final int yCellDim,
    							@RequestParam("zCellDim") final int zCellDim,
    							@RequestParam("maxDepth") final int maxDepth,
    							@RequestParam("minEast") final int minEast,
    							@RequestParam("maxEast") final int maxEast,
    							@RequestParam("minNorth") final int minNorth,
    							@RequestParam("maxNorth") final int maxNorth,
    							@RequestParam("layerName") final String layerName,
                                @RequestParam("format") final String format,
    							HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
    	
    	String serviceUrl = hostConfigurer.resolvePlaceholder("HOST.erddapservice.url");
    	
    	// validate the dimensions again in case client has changed them. The bounding box will be adjusted to fit if required.
    	int [] eastingArray = validateCellDimensions(minEast, maxEast, xCellDim);  
    	int [] northingArray = validateCellDimensions(minNorth, maxNorth, yCellDim);
    	int [] depthArray = validateCellDimensions(0, maxDepth, zCellDim);
    	
    	// calculate data padding
    	int minEastingPadded = eastingArray[0] - depthArray[1];
    	int maxEastingPadded = eastingArray[1] + depthArray[1];
    	int minNorthingPadded = northingArray[0] - depthArray[1];
    	int maxNorthingPadded = northingArray[1] + depthArray[1];
    	
    	String[] coords = dataCoords.split(",");
    	double latMin = Double.parseDouble(coords[1]);
    	double latMax = Double.parseDouble(coords[3]);
    	
    	// calculate likely lat zone
    	double latDiff = latMax - latMin;
    	double latCenter = latMin + (latDiff/2);
    	char latZone = 0;
    	
    	if (latCenter <= -8 && latCenter > -16){
    		latZone = 'L';
    	} else if (latCenter <= -16 && latCenter > -24){
    		latZone = 'K';
    	} else if (latCenter <= -24 && latCenter > -32){
    		latZone = 'J';
    	} else if (latCenter <= -32 && latCenter > -40){
    		latZone = 'H';
    	} else if (latCenter <= -40 && latCenter > -48){
    		latZone = 'G';
    	} else {
    		logger.error("could not calculate lat zone");
    		return generateJSONResponseMAV(false, null, "Could not calculate lat zone");
    	}    	
    	
    	// convert back to lat/lng
    	UTMRef utmBotLeft = new UTMRef(minEastingPadded, minNorthingPadded, latZone, mgaZone);
    	UTMRef utmTopLeft = new UTMRef(minEastingPadded, maxNorthingPadded, latZone, mgaZone);
    	UTMRef utmTopRight = new UTMRef(maxEastingPadded, maxNorthingPadded, latZone, mgaZone);
    	UTMRef utmBotRight = new UTMRef(maxEastingPadded, minNorthingPadded, latZone, mgaZone);
    	LatLng latLngBotLeft = utmBotLeft.toLatLng();
    	LatLng latLngTopLeft = utmTopLeft.toLatLng();
    	LatLng latLngTopRight = utmTopRight.toLatLng();
    	LatLng latLngBotRight = utmBotRight.toLatLng();
    	
    	// these lat/lng co-ords wont be square so we need to select a lat/lng bounding box that fully 
    	// encompasses the projected data area
    	double bigLngMin = Math.min(latLngBotLeft.getLng(), latLngTopLeft.getLng());
    	double bigLatMin = Math.min(latLngBotLeft.getLat(), latLngBotRight.getLat());
    	double bigLngMax = Math.min(latLngBotRight.getLng(), latLngTopRight.getLng());
    	double bigLatMax = Math.min(latLngTopLeft.getLat(), latLngTopRight.getLat());
    	
    	// round the final lat/lng values to 6 decimal places
    	BigDecimal finalLngMin = new BigDecimal(bigLngMin).setScale(6, RoundingMode.FLOOR);
    	BigDecimal finalLatMin = new BigDecimal(bigLatMin).setScale(6, RoundingMode.FLOOR);
    	BigDecimal finalLngMax = new BigDecimal(bigLngMax).setScale(6, RoundingMode.CEILING);
    	BigDecimal finalLatMax = new BigDecimal(bigLatMax).setScale(6, RoundingMode.CEILING);
    	
    	// construct final lat/lng co-ordinates to use in the ERDDAP subset reqest
    	String newDataCoords = finalLngMin + "," + finalLatMin + "," + finalLngMax + "," + finalLatMax;
    	
    	String oldErddapUrl = getCoverageSubsetUrl(dataCoords, serviceUrl, layerName, format);
    	String erddapUrl = getCoverageSubsetUrl(newDataCoords, serviceUrl, layerName, format);

    	// add erddap url to the map or create a new one if it doesn't exist
    	HashMap<String, String> erddapUrlMap = new HashMap<String,String>();;
    	erddapUrlMap.put(layerName+"."+format, erddapUrl);
    	logger.debug("oldErddapUrl: " + oldErddapUrl);
    	logger.debug("erddapUrl: " + erddapUrl);
    	
    	//Write out our selection details to the user session for use later 
    	//(used if the user decides to proceed with job submission)
    	CSWGeographicBoundingBox originalSelection = createBoundingBox(dataCoords);
    	request.getSession().setAttribute(SESSION_ERRDAP_URL_MAP, erddapUrlMap);
    	
    	request.getSession().setAttribute(SESSION_SELECTION_MIN_EASTING, originalSelection.getWestBoundLongitude());
    	request.getSession().setAttribute(SESSION_SELECTION_MAX_EASTING, originalSelection.getEastBoundLongitude());
    	request.getSession().setAttribute(SESSION_SELECTION_MIN_NORTHING, originalSelection.getSouthBoundLatitude());
    	request.getSession().setAttribute(SESSION_SELECTION_MAX_NORTHING, originalSelection.getNorthBoundLatitude());
    	
    	request.getSession().setAttribute(SESSION_PADDED_MIN_EASTING, (double) minEast);
    	request.getSession().setAttribute(SESSION_PADDED_MAX_EASTING, (double) maxEast);
    	request.getSession().setAttribute(SESSION_PADDED_MIN_NORTHING, (double) minNorth);
    	request.getSession().setAttribute(SESSION_PADDED_MAX_NORTHING, (double) maxNorth);
    	request.getSession().setAttribute(SESSION_MGA_ZONE, Integer.toString(mgaZone));
    	
    	request.getSession().setAttribute(SESSION_INVERSION_DEPTH, maxDepth);
    	request.getSession().setAttribute(SESSION_CELL_X, xCellDim);
    	request.getSession().setAttribute(SESSION_CELL_Y, yCellDim);
    	request.getSession().setAttribute(SESSION_CELL_Z, zCellDim);
    	
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
	private String getCoverageSubsetUrl(String coords, String serviceUrl, String layerName, String format) {
		
		CSWGeographicBoundingBox bbox = createBoundingBox(coords);
		
		logger.debug(String.format("serviceUrl='%1$s' bbox='%2$s' layerName='%3$s'", serviceUrl, bbox, layerName));
        
		// convert bbox co-ordinates to ERDDAP an ERDDAP dimension string
        String erddapDimensions = "%5B("+ bbox.getSouthBoundLatitude() +"):1:("+ bbox.getNorthBoundLatitude() +
		")%5D%5B("+ bbox.getWestBoundLongitude() +"):1:("+ bbox.getEastBoundLongitude() +")%5D";
		
        String url = serviceUrl + layerName + "." + format + "?" + layerName + erddapDimensions;
        
        return url;
	}
	
	/**
	 * Create a CSWGeographicBoundingBox from an array of lat/lon co-ordinates. 
	 * Co-ordinates need to be in the order of SW lon, SW lat, NE lon, NE lat. 
	 * 
	 * @param coords String of comma separated lat/lon co-ordinates.
	 * @return bbox the converted CSWGeographicBoundingBox
	 */
	private CSWGeographicBoundingBox createBoundingBox(String coords) {
		String[] coordsArray = coords.split(",");
        
        CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox();
        bbox.setWestBoundLongitude(Double.parseDouble(coordsArray[0]));
        bbox.setSouthBoundLatitude(Double.parseDouble(coordsArray[1]));
        bbox.setEastBoundLongitude(Double.parseDouble(coordsArray[2]));
        bbox.setNorthBoundLatitude(Double.parseDouble(coordsArray[3]));
		
        return bbox;
	}
}

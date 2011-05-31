package org.auscope.portal.server.web.controllers;

import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.server.domain.wcs.DescribeCoverageRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.IWCSDescribeCoverageMethodMaker;
import org.auscope.portal.server.web.IWCSGetCoverageMethodMaker;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * A controller that attempts to provide functions for use by the generic WCS use case.
 * 
 * @author vot002
 *
 */
@Controller
public class WCSController {
    protected final Log logger = LogFactory.getLog(getClass());
    
    private HttpServiceCaller serviceCaller;
    private IWCSGetCoverageMethodMaker getCoverageMethodMaker;
    private IWCSDescribeCoverageMethodMaker describeCoverageMethodMaker;
    private PortalPropertyPlaceholderConfigurer hostConfigurer;
    
    @Autowired
    public WCSController(HttpServiceCaller serviceCaller, IWCSGetCoverageMethodMaker methodMaker, 
    		IWCSDescribeCoverageMethodMaker describeCoverageMethodMaker, PortalPropertyPlaceholderConfigurer hostConfigurer) {
        this.serviceCaller = serviceCaller;      
        this.getCoverageMethodMaker = methodMaker;
        this.getCoverageMethodMaker = methodMaker;
        this.describeCoverageMethodMaker = describeCoverageMethodMaker;
        this.hostConfigurer = hostConfigurer;
    }
    
    private String generateOutputFilename(String layerName, String format) throws IllegalArgumentException {
        if (format.toLowerCase().contains("geotiff"))
            return String.format("%1$s.tiff", layerName);
        else if (format.toLowerCase().contains("netcdf"))
            return String.format("%1$s.nc", layerName);
        else 
            return String.format("%1$s.%2$s", layerName, format);
    }
    
    private String parseTimeConstraint(final String[] timePositions,
                                 final String timePeriodFrom,
                                 final String timePeriodTo,
                                 final String timePeriodResolution) throws ParseException {
        String timeString = null;
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");                
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        
        outputFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        //We will receive a list of time positions
        if (timePositions != null && timePositions.length > 0) {
            StringBuilder sb = new StringBuilder();
            
            for (String s : timePositions) {
                if (s != null && !s.isEmpty()) {
                    
                    Date d = inputFormat.parse(s);
                    if (sb.length() > 0)
                        sb.append(",");
                    sb.append(outputFormat.format(d));
                }
            }
            
            timeString = sb.toString();
        //or an actual time period
        } else if (timePeriodFrom != null && timePeriodTo != null && !timePeriodFrom.isEmpty() && !timePeriodTo.isEmpty()) {
            
            Date from = inputFormat.parse(timePeriodFrom);
            Date to = inputFormat.parse(timePeriodTo);
            
            timeString = String.format("%1$s/%2$s", outputFormat.format(from), outputFormat.format(to));
            if (timePeriodResolution != null && !timePeriodResolution.isEmpty())  {
                timeString += String.format("/%1$s", timePeriodResolution);
            }
        }
        
        return timeString;
    }
    
    private void closeZipWithError(ZipOutputStream zout,String debugQuery, Exception exceptionToPrint) {
        String message = null;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            exceptionToPrint.printStackTrace(pw);
            message = String.format("An exception occured whilst requesting/parsing your WCS download.\r\n%1$s\r\nMessage=%2$s\r\n%3$s",debugQuery, exceptionToPrint.getMessage(), sw.toString());
        } finally {
            try {
                if(pw != null)  pw.close();
                if(sw != null)  sw.close();
            } catch (Exception ignore) {}
        }
        
        try {
            zout.putNextEntry(new ZipEntry("error.txt"));
            
            zout.write(message.getBytes());
        } catch (Exception ex) {
            logger.error("Couldnt create debug error.txt in output", ex);
        } finally {
            try {
                zout.close();
            } catch (Exception ex) {}
        }
    }
    
    /**
     * 
     * @param customParamValues a list of PARAMETER=VALUE
     * @param customParamIntervals a list of PARAMETER=MIN/MAX/RESOLUTION
     * @return
     */
    private Map<String, String> generateCustomParamMap(final String[] customParamValues) {
        Map<String, String> customKvps = new HashMap<String, String>();
        
        if (customParamValues != null) {
            for (String kvpString : customParamValues) {
                String[] kvp = kvpString.split("=");
                if (kvp.length != 2) 
                    throw new IllegalArgumentException("Couldnt parse customParamValue " + kvpString);
                
                //This is a sanity check to ensure we are getting all numbers
                String[] values = kvp[1].split("/");
                for (String value : values) {
                    try {
                        Double.parseDouble(value);
                    } catch (Exception ex) {
                        throw new IllegalArgumentException(String.format("Couldnt parse double from '%1$s' in customParam '%2$s'", value, kvpString));
                    }
                }
                
                String valueList = customKvps.get(kvp[0]);
                if (valueList == null) 
                    valueList = "";
                else
                    valueList += ","; 
                
                valueList += kvp[1];
                customKvps.put(kvp[0], valueList);
            }
        }
        
        return customKvps;
    }
    
    /**
     * A function that given the parameters for a WCS GetCovereage request will make the request
     * on behalf of the user and return the results in a zipped file.
     * 
     * One set of outputWidth/outputHeight or outputResX/outputResy must be specified
     * 
     * One of a BBOX constraint or a TIMEPERIOD/TIMEPOSITION constraint must be specified
     * 
     * You cannot specify both a TIMEPERIOD and TIMEPOSITION constraint
     * 
     * @param serviceUrl The remote URL to query
     * @param layerName The coverage layername to request
     * @param downloadFormat Either [GeoTIFF, NetCDF]
     * @param inputCrs the coordinate reference system to query
     * @param outputWidth [Optional] Width of output dataset (Not compatible with outputResX/outputResY)
     * @param outputHeight [Optional] Height of output dataset (Not compatible with outputResX/outputResY)
     * @param outputResX [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputResY [Optional] When requesting a georectified grid coverage, this requests a subset with a specific spatial resolution (Not compatible with outputWidth/outputHeight)
     * @param outputCrs [Optional] The Coordinate reference system of the output data
     * @param northBoundLatitude [Optional] [BBOX] A point on the bounding box
     * @param southBoundLatitude [Optional] [BBOX] A point on the bounding box
     * @param eastBoundLongitude [Optional] [BBOX] A point on the bounding box
     * @param westBoundLongitude [Optional] [BBOX] A point on the bounding box
     * @param timePositions [Optional] [TIMEPOSITION] A list of time positions to query for. Format YYYY-MM-DD HH:MM:SS GMT
     * @param timePeriodFrom [Optional] [TIMEPERIOD] a time range start. Format YYYY-MM-DD HH:MM:SS GMT
     * @param timePeriodTo [Optional] [TIMEPERIOD] a time range end. Format YYYY-MM-DD HH:MM:SS GMT
     * @param timePeriodResolution [Optional] [TIMEPERIOD] a time range resolution (not required for time period)
     * @param customParamValue [Optional] A list of strings in the form "PARAMETER=VALUE" or "PARAMETER=MIN/MAX/RES" which will be used for compound parameter filtering in the request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/downloadWCSAsZip.do")
    public void downloadWCSAsZip(@RequestParam("serviceUrl") final String serviceUrl,
                                 @RequestParam("layerName") final String layerName,
                                 @RequestParam("downloadFormat") final String downloadFormat,
                                 @RequestParam("inputCrs") final String inputCrs,
                                 @RequestParam(required=false, defaultValue="0", value="outputWidth") final int outputWidth,
                                 @RequestParam(required=false, defaultValue="0", value="outputHeight") final int outputHeight,
                                 @RequestParam(required=false, defaultValue="0", value="outputResX") final double outputResX,
                                 @RequestParam(required=false, defaultValue="0", value="outputResY") final double outputResY,
                                 @RequestParam(required=false, value="outputCrs") final String outputCrs,
                                 @RequestParam(required=false, defaultValue="0",  value="northBoundLatitude") final double northBoundLatitude,
                                 @RequestParam(required=false, defaultValue="0", value="southBoundLatitude") final double southBoundLatitude,
                                 @RequestParam(required=false, defaultValue="0", value="eastBoundLongitude") final double eastBoundLongitude,
                                 @RequestParam(required=false, defaultValue="0", value="westBoundLongitude") final double westBoundLongitude,
                                 @RequestParam(required=false, value="timePosition") final String[] timePositions,
                                 @RequestParam(required=false, value="timePeriodFrom") final String timePeriodFrom,
                                 @RequestParam(required=false, value="timePeriodTo") final String timePeriodTo,
                                 @RequestParam(required=false, value="timePeriodResolution") final String timePeriodResolution,
                                 @RequestParam(required=false, value="customParamValue") final String[] customParamValues,
                                HttpServletResponse response) throws Exception {
        
        String outFileName = generateOutputFilename(layerName, downloadFormat);
        String timeString = parseTimeConstraint(timePositions, timePeriodFrom, timePeriodTo, timePeriodResolution);
        
        Map<String, String> customParams = generateCustomParamMap(customParamValues);
        
        CSWGeographicBoundingBox bbox = null;
        if (!(eastBoundLongitude == 0 &&
                westBoundLongitude == 0 &&
                northBoundLatitude == 0 && 
                southBoundLatitude == 0)) {
            bbox = new CSWGeographicBoundingBox();
            bbox.setEastBoundLongitude(eastBoundLongitude);
            bbox.setSouthBoundLatitude(southBoundLatitude);
            bbox.setNorthBoundLatitude(northBoundLatitude);
            bbox.setWestBoundLongitude(westBoundLongitude);
        }
        
        logger.debug(String.format("serviceUrl='%1$s' bbox='%2$s' timeString='%3$s' layerName='%4$s'", serviceUrl, bbox, timeString, layerName));
        
        HttpMethodBase method = getCoverageMethodMaker.makeMethod(serviceUrl, layerName, downloadFormat, 
                outputCrs, outputWidth, outputHeight, outputResX, outputResY, inputCrs, bbox, timeString, customParams);
        
        downloadWCSAsZip(outFileName, method, response);
    }
    
    /**
     * An internal function that handles using a HttpMethodBase and calling a remote service for the raw binary data that we zip and return to the user.
     * @param outputFileName The name of the file (in the zip output) that we will use
     * @param method the method used to make the request for data
     * @param response the servlet response that will receive the output binary data
     */
    protected void downloadWCSAsZip(String outputFileName,HttpMethodBase method, HttpServletResponse response) throws Exception {
        
        //Lets make the request and zip up the response before passing it back to the user
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition","inline; filename=WCSDownload.zip;");
        
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
        try{
            InputStream inData = serviceCaller.getMethodResponseAsStream(method, serviceCaller.getHttpClient());
            
            zout.putNextEntry(new ZipEntry(outputFileName));
            
            //Read the input in 1MB chunks and don't stop till we run out of data
            byte[] buffer = new byte[1024 * 1024];
            int dataRead;
            do {
                dataRead = inData.read(buffer, 0, buffer.length);
                if (dataRead > 0) {
                    zout.write(buffer, 0, dataRead);
                }
            } while (dataRead != -1);
            
            zout.finish();
            zout.flush();
            zout.close();
        } catch (Exception ex) {
            logger.error("Failure downloading WCS - returning error message in ZIP response", ex);
            closeZipWithError(zout,method.getURI().toString(), ex);
        } finally {
            method.releaseConnection(); //Ensure this gets called otherwise we leak connections
        }
    }
    
    /**
     * Returns a DescribeCoverageRecord as a JSON Response representing the response
     * 
     *  {
     *      success : true/false
     *      errorMsg : ''
     *      rawXml : [Can be null] <Set to the raw XML string returned from the DescribeCoverageResponse>
     *      records : [Can be null] <Set to the DescribeCoverageRecord list parsed from the rawXml>
     *  }
     *  
     * @param serviceUrl
     * @param layerName
     * @return
     */
    @RequestMapping("/describeCoverage.do")
    public ModelAndView describeCoverage(String serviceUrl, String layerName) {
        
        
        HttpMethodBase method = null;
        
        try {
            method = describeCoverageMethodMaker.makeMethod(serviceUrl, layerName);
        } catch (Exception ex) {
            logger.error("Error generating method", ex);
            return getDescribeCoverageResponse(false, "Error generating request method. Are layerName and serviceUrl specified?", null, null);
        }
        
        String xmlResponse = null;
        try {
            xmlResponse = serviceCaller.getMethodResponseAsString(method, serviceCaller.getHttpClient());
        } catch (Exception ex) {
            logger.info("Error making request", ex);
            return getDescribeCoverageResponse(false, "Error occured whilst communicating to remote service: " + ex.getMessage(), null, null);
        }
        
        DescribeCoverageRecord[] records = null;
        try {
            records = DescribeCoverageRecord.parseRecords(xmlResponse);
        } catch (Exception ex) {
            logger.warn("Error parsing request", ex);
            return getDescribeCoverageResponse(false, "Error occured whilst parsing response: " + ex.getMessage(), xmlResponse, null);
        }
        
        return getDescribeCoverageResponse(true, "No errors found",xmlResponse, records );
    }
    
    private JSONModelAndView getDescribeCoverageResponse(boolean success, String errorMessage, String responseXml, DescribeCoverageRecord[] records ) {
        
        ModelMap response = new ModelMap();
        response.put("success", success);
        response.put("errorMsg", errorMessage);
        response.put("rawXml", responseXml);
        response.put("records", records);
        
        return new JSONModelAndView(response);
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
    	
    	ModelAndView mav = new ModelAndView("jsonView");
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
    		mav.addObject("success", false);
    		return mav;
    	}
    	
    	mav.addObject("mgaZone", mgaZone);
    	mav.addObject("success", true);
		return mav;
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
    	
    	ModelAndView mav = new ModelAndView("jsonView");
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
    	
    	mav.addObject("eastingArray", eastingArray);
    	mav.addObject("northingArray", northingArray);
    	mav.addObject("depthArray", depthArray);
    	mav.addObject("success", true);
		return mav;
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
    	
    	ModelAndView mav = new ModelAndView("jsonView");
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
    		mav.addObject("success", false);
    		return mav;
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
    	HashMap<String, String> erddapUrlMap = (HashMap)request.getSession().getAttribute("erddapUrlMap");
    	
    	if (erddapUrlMap == null) {
    		erddapUrlMap = new HashMap<String,String>();
    	}
    	
    	erddapUrlMap.put(layerName+"."+format, erddapUrl);
    	logger.debug("oldErddapUrl: " + oldErddapUrl);
    	logger.debug("erddapUrl: " + erddapUrl);
    	
    	request.getSession().setAttribute("erddapUrlMap", erddapUrlMap);
        
        mav.addObject("success", true);
 	    
        return mav;
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
}

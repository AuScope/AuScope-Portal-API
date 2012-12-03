package org.auscope.portal.server.web.controllers;

import java.awt.Dimension;
import java.awt.Point;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.WCSService;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.wcs.DescribeCoverageRecord;
import org.auscope.portal.core.services.responses.wcs.RectifiedGrid;
import org.auscope.portal.core.services.responses.wcs.Resolution;
import org.auscope.portal.core.services.responses.wcs.SpatialDomain;
import org.auscope.portal.core.services.responses.wcs.TimeConstraint;
import org.auscope.portal.core.util.FileIOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller that attempts to provide functions for use by the generic WCS use case.
 *
 * @author Josh Vote
 *
 */
@Controller
public class WCSController extends BasePortalController {
    private final Log logger = LogFactory.getLog(getClass());

    /** The format string view's are expected to use when working with this controller*/
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z";

    private WCSService wcsService;

    @Autowired
    public WCSController(WCSService wcsService) {
        this.wcsService = wcsService;
    }

    private String generateOutputFilename(String layerName, String format) throws IllegalArgumentException {
        if (format.toLowerCase().contains("geotiff"))
            return String.format("%1$s.tiff", layerName);
        else if (format.toLowerCase().contains("netcdf"))
            return String.format("%1$s.nc", layerName);
        else
            return String.format("%1$s.%2$s", layerName, format);
    }

    /**
     * Parses an array of date strings that are conforming to DATE_FORMAT
     * into date objects
     * @param dateStrings
     * @return
     * @throws ParseException
     */
    private Date[] parseDates(final String[] dateStrings) throws ParseException {
        Date[] dates = new Date[dateStrings.length];
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);

        for (int i = 0; i < dateStrings.length; i++) {
            dates[i] = format.parse(dateStrings[i]);
        }

        return dates;
    }

    /**
     * Attempts to parse a time constraint from the listed time information.
     * Returns null if no constraint can be generated
     * @param timePositions
     * @param timePeriodFrom
     * @param timePeriodTo
     * @param timePeriodResolution
     * @return
     * @throws ParseException
     */
    private TimeConstraint parseTimeConstraint(final String[] timePositions,
                                 final String timePeriodFrom,
                                 final String timePeriodTo,
                                 final String timePeriodResolution) throws ParseException {
        //We will receive a list of time positions
        if (timePositions != null && timePositions.length > 0) {
            return TimeConstraint.parseTimeConstraint(parseDates(timePositions));
        //or an actual time period
        } else if (timePeriodFrom != null && timePeriodTo != null && !timePeriodFrom.isEmpty() && !timePeriodTo.isEmpty()) {
            DateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT);
            Date from = inputFormat.parse(timePeriodFrom);
            Date to = inputFormat.parse(timePeriodTo);

            return TimeConstraint.parseTimeConstraint(from, to, timePeriodResolution);
        }

        return null;
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
                                 @RequestParam(required=false, value="outputWidth") final Integer outputWidth,
                                 @RequestParam(required=false, value="outputHeight") final Integer outputHeight,
                                 @RequestParam(required=false, value="outputResX") final Double outputResX,
                                 @RequestParam(required=false, value="outputResY") final Double outputResY,
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
        TimeConstraint timeConstraint = parseTimeConstraint(timePositions, timePeriodFrom, timePeriodTo, timePeriodResolution);
        Map<String, String> customParams = generateCustomParamMap(customParamValues);
        Dimension outputSize = null;
        Resolution outputResolution = null;

        if (outputWidth != null && outputHeight != null) {
            outputSize = new Dimension(outputWidth.intValue(), outputHeight.intValue());
        }

        if (outputResX != null && outputResY != null) {
            outputResolution = new Resolution(outputResX.doubleValue(), outputResY.doubleValue());
        }

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

        logger.debug(String.format("serviceUrl='%1$s' bbox='%2$s' timeString='%3$s' layerName='%4$s'", serviceUrl, bbox, timeConstraint, layerName));

        InputStream dataStream = null;

        //Pipe the request into a zip
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition","inline; filename=WCSDownload.zip;");
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
        try {
            //Make our request
            dataStream = wcsService.getCoverage(serviceUrl, layerName, downloadFormat, outputSize, outputResolution, outputCrs, inputCrs, bbox, timeConstraint, customParams);
            zout.putNextEntry(new ZipEntry(outFileName));
            writeInputToOutputStream(dataStream, zout, 1024 * 1024, false);
        } catch (Exception ex) {
            writeErrorToZip(zout, "", ex, "error.txt");
        } finally {
            FileIOUtil.closeQuietly(dataStream);
            FileIOUtil.closeQuietly(zout);
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
        DescribeCoverageRecord[] records = null;
        try {
            records = wcsService.describeCoverage(serviceUrl, layerName);
        } catch (Exception ex) {
            logger.error("Error describing coverage", ex);
            return generateJSONResponseMAV(false, null, "Error occured whilst communicating to remote service: " + ex.getMessage());
        }

        return generateJSONResponseMAV(true, records, "");
    }

    /**
     * Given a rectified grid, calculate the wgs:84 max/min by multiplying the
     * grid envelope high values against the offset vectors
     *
     * Returns an array representing X/Y width/height
     *
     * @param arr
     * @param scalar
     * @return
     */
    public double[] calculate2dExtents(RectifiedGrid rg) {
        double maxX = 0.0;
        double maxY = 0.0;

        //Assume at least 2 dimensions. The first being X, the second being Y
        double[][] offsetVectors = rg.getOffsetVectors();
        int[] highValues = rg.getEnvelopeHighValues();
        int[] lowValues = rg.getEnvelopeLowValues();
        for (int dimension = 0; dimension < 2; dimension++) {
            maxX += offsetVectors[dimension][0] * (double) (highValues[0] - lowValues[0]);
            maxY += offsetVectors[dimension][1] * (double) (highValues[1] - lowValues[1]);
        }

        return new double[] {maxX, maxY};
    }

    /**
     * Given a RectifiedGrid, convert a WGS84 lat/long coordinate into a datapoint index
     * within a rectified grid
     * @param rg Must contain a WGS4 coordinate space. Assumed to contain at least two axes, X and Y
     * @param latitude
     * @param longitude
     * @return
     */
    public Point estimageLatLngToGridSpace(RectifiedGrid rg, double latitude, double longitude) {
        double originX = rg.getOrigin()[0];
        double originY = rg.getOrigin()[1];
        double[] widthHeight = calculate2dExtents(rg);
        double[] maxValues = new double[] {widthHeight[0] + originX, widthHeight[1] + originY};

        //Get lat/lng as a proportional offset between origin and maxValues
        double proportionalX = 1 - ((maxValues[0] - longitude) / (maxValues[0] - originX));
        double proportionalY = 1 - ((maxValues[1] - latitude) / (maxValues[1] - originY));

        //Used the proportional offset as a multiplier against the data indexes
        int[] highIndexes = rg.getEnvelopeHighValues();
        int[] lowIndexes = rg.getEnvelopeLowValues();
        int indexX = (int) Math.round((((double)(highIndexes[0] - lowIndexes[0])) * proportionalX));
        int indexY = (int) Math.round((((double)(highIndexes[1] - lowIndexes[1])) * proportionalY));

        return new Point(indexX, indexY);
    }

    /**
     * Attempts to estimate the size of a subset of a given coverage in data units.
     * @param northBoundLatitude
     * @param southBoundLatitude
     * @param eastBoundLongitude
     * @param westBoundLongitude
     * @param serviceUrl
     * @param coverageName
     * @return
     */
    @RequestMapping("/estimateCoverageSize.do")
    public ModelAndView estimateCoverageSize(@RequestParam("northBoundLatitude") double northBoundLatitude,
                                 @RequestParam("southBoundLatitude") double southBoundLatitude,
                                 @RequestParam("eastBoundLongitude") double eastBoundLongitude,
                                 @RequestParam("westBoundLongitude") double westBoundLongitude,
                                 @RequestParam("serviceUrl") String serviceUrl,
                                 @RequestParam("coverageName") String coverageName) {

        //Perform our calculations based on coverage description
        DescribeCoverageRecord[] records = null;
        try {
            records = wcsService.describeCoverage(serviceUrl, coverageName);
        } catch (Exception ex) {
            logger.error(String.format("Error describing coverage for coverage size: %1$s", ex));
            logger.debug("Exception: ", ex);
            return generateJSONResponseMAV(false, null, "Error occured whilst communicating to remote service: " + ex.getMessage());
        }

        if (records == null || records.length == 0) {
            return generateJSONResponseMAV(false, null, "No coverage description available for coverage: " + coverageName);
        }

        //Check our response has a rectified grid with at least 2 dimensions
        SpatialDomain sd = records[0].getSpatialDomain();
        RectifiedGrid rg = null;
        if (sd == null || (rg = sd.getRectifiedGrid()) == null) {
            return generateJSONResponseMAV(false, null, "No spatial domain with rectified grid for described coverage: " + coverageName);
        }
        if (rg.getDimension() < 2) {
            return generateJSONResponseMAV(false, null, "The rectified grid for coverage must have at least 2 dimensions: " + coverageName);
        }

        //ASSUMPTIONS - the rg is in a WGS:84 compatible srs
        //            - The axes read X, Y and then possibly Z
        Point ne = estimageLatLngToGridSpace(rg, northBoundLatitude, eastBoundLongitude);
        Point sw = estimageLatLngToGridSpace(rg, southBoundLatitude, westBoundLongitude);

        ModelMap model = new ModelMap();
        model.put("width", ne.x - sw.x);
        model.put("height", ne.y - sw.y);

        return generateJSONResponseMAV(true, model, "");
    }
}

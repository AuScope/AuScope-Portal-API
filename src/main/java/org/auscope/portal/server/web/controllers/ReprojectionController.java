package org.auscope.portal.server.web.controllers;

import java.awt.geom.Rectangle2D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

/**
 * Controller for performing simplistic BBox reprojection
 * @author Josh Vote
 *
 */
@Controller
public class ReprojectionController extends BasePortalController {
    protected final Log logger = LogFactory.getLog(getClass());

    public ReprojectionController() {

    }

    /**
     * Utility for calculating the best Mga zone for a given point
     * (Inherited from VEGL - should be revised)
     */
    private int calculateIdealMgaZone(double northBoundLatitude, double southBoundLatitude, double eastBoundLongitude, double westBoundLongitude) {
        double lngDiff = eastBoundLongitude - westBoundLongitude;
        double lngCenter = westBoundLongitude + (lngDiff/2);
        double latDiff = northBoundLatitude - southBoundLatitude;
        double latCenter = southBoundLatitude + (latDiff/2);

        return calculateIdealMgaZone(latCenter, lngCenter);
    }

    /**
     * Utility for calculating the best Mga zone for a given point
     * (Inherited from VEGL - should be revised)
     */
    private int calculateIdealMgaZone(double latitude, double longitude) {
        if (longitude >= 108 && longitude < 114){
            return 49;
        } else if (longitude >= 114 && longitude < 120){
            return 50;
        } else if (longitude >= 120 && longitude < 126){
            return 51;
        } else if (longitude >= 126 && longitude < 132){
            return 52;
        } else if (longitude >= 132 && longitude < 138){
            return 53;
        } else if (longitude >= 138 && longitude < 144){
            return 54;
        } else if (longitude >= 144 && longitude < 150){
            return 55;
        } else if (longitude >= 150 && longitude < 156){
            return 56;
        }

        logger.error("could not calculate MGA zone");
        return-1;
    }

    /**
     * Calculates the likely MGA zone that the user should use. This zone is determined by finding what zone
     * the center of the selected region is in.
     * @return
     * @throws Exception
     */
    @RequestMapping("/calculateMgaZoneForBBox.do")
    public ModelAndView calculateMgaZoneForBBox(@RequestParam("northBoundLatitude") final double northBoundLatitude,
                                         @RequestParam("southBoundLatitude") final double southBoundLatitude,
                                         @RequestParam("eastBoundLongitude") final double eastBoundLongitude,
                                         @RequestParam("westBoundLongitude") final double westBoundLongitude) throws Exception {

        // calculate likely MGA zone
        int mgaZone = calculateIdealMgaZone(northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude);
        if (mgaZone < 0) {
            logger.error("could not calculate MGA zone");
            return generateJSONResponseMAV(false, null, "Could not calculate MGA zone");
        }

        return generateJSONResponseMAV(true, mgaZone, "");
    }

    /**
     * Projects the lat/lng co-ordinates to UTM. If mga zone is not specified it will be estimated
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/projectBBoxToUtm.do")
    public ModelAndView projectBBoxToUtm(@RequestParam("northBoundLatitude") double northBoundLatitude,
                                @RequestParam("southBoundLatitude") double southBoundLatitude,
                                @RequestParam("eastBoundLongitude") double eastBoundLongitude,
                                @RequestParam("westBoundLongitude") double westBoundLongitude,
                                @RequestParam(required=false, value="mgaZone") Integer mgaZone) throws Exception {

        if (mgaZone == null) {
            mgaZone = calculateIdealMgaZone(northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude);
            if (mgaZone < 0) {
                logger.error("could not calculate MGA zone");
                return generateJSONResponseMAV(false, null, "Could not calculate MGA zone");
            }
        }

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
        Rectangle2D rect = new Rectangle2D.Double(eastBoundLongitude, southBoundLatitude, (westBoundLongitude - eastBoundLongitude), (northBoundLatitude - southBoundLatitude));
        Rectangle2D approxAreaMga = projection.transform(rect);

        // Calculate bounding box which fully encompasses this polygon
        ModelMap data = new ModelMap();
        data.put("minNorthing", (int)Math.floor(approxAreaMga.getMinY()));
        data.put("maxNorthing", (int)Math.ceil(approxAreaMga.getMaxY()));
        data.put("minEasting", (int)Math.floor(approxAreaMga.getMinX()));
        data.put("maxEasting", (int)Math.ceil(approxAreaMga.getMaxX()));
        data.put("mgaZone", mgaZone);
        return generateJSONResponseMAV(true, data, "");
    }
}

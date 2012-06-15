package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BaseCSWController;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.core.services.responses.csw.CSWGeographicElement;
import org.auscope.portal.core.services.responses.csw.CSWOnlineResourceImpl;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.services.responses.csw.CSWResponsibleParty;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesRecord;
import org.auscope.portal.core.services.responses.wms.GetCapabilitiesWMSLayerRecord;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles GetCapabilites (WFS)WMS queries.
 *
 * @author Jarek Sanders
 * @version $Id: WMSController.java 2256 2012-05-21 06:06:45Z JoshVote $
 */
@Controller
public class WMSController extends BaseCSWController {


    // ----------------------------------------------------- Instance variables

    private WMSService wmsService;
    private final Log log = LogFactory.getLog(getClass());

    // ----------------------------------------------------------- Constructors

    @Autowired
    public WMSController(WMSService wmsService, ViewCSWRecordFactory viewCSWRecordFactory, ViewKnownLayerFactory knownLayerFact) {
        super(viewCSWRecordFactory, knownLayerFact);
        this.wmsService = wmsService;
    }


    // ------------------------------------------- Property Setters and Getters

    /**
     * Gets all WMS data records from a discovery service, and then
     * creates JSON response for the WMS layers list in the portal
     *
     * @return a JSON representation of the CSWRecord equivalent records
     *
     * @throws Exception
     */
    @RequestMapping("/getCustomLayers.do")
    public ModelAndView getCustomLayers(@RequestParam("service_URL") String serviceUrl) throws Exception {

        CSWRecord[] records;
        int invalidLayerCount = 0;
        try {
            GetCapabilitiesRecord capabilitiesRec = wmsService.getWmsCapabilities(serviceUrl);

            List<CSWRecord> cswRecords = new ArrayList<CSWRecord>();

            if (capabilitiesRec != null) {
                //Make a best effort of parsing a WMS into a CSWRecord
                for (GetCapabilitiesWMSLayerRecord rec : capabilitiesRec.getLayers()) {
                    //to check if layers are EPSG: 4326 SRS
                    String[] uniqueSRSList = getSRSList(capabilitiesRec.getLayerSRS() , rec.getChildLayerSRS());
                    if (!((Arrays.binarySearch(uniqueSRSList, "EPSG:4326")) >= 0 || (Arrays.binarySearch(uniqueSRSList, "epsg:4326")) >= 0)) {
                        invalidLayerCount += 1;
                        continue;
                    }

                    if (rec.getName() == null || rec.getName().isEmpty()) {
                        continue;
                    }

                    String serviceName = rec.getTitle();
                    String fileId = "unique-id-" + rec.getName();
                    String recordInfoUrl = null;
                    String dataAbstract = rec.getAbstract();
                    CSWResponsibleParty responsibleParty = new CSWResponsibleParty();
                    responsibleParty.setOrganisationName(capabilitiesRec.getOrganisation());

                    CSWGeographicElement[] geoEls = null;
                    CSWGeographicBoundingBox bbox = rec.getBoundingBox();
                    if (bbox != null) {
                        geoEls = new CSWGeographicElement[] {bbox};
                    }

                    AbstractCSWOnlineResource[] onlineResources = new AbstractCSWOnlineResource[1];
                    onlineResources[0] = new CSWOnlineResourceImpl(new URL(capabilitiesRec.getMapUrl()),
                            "OGC:WMS-1.1.1-http-get-map",
                            rec.getName(),
                            rec.getTitle());

                    CSWRecord newRecord = new CSWRecord(serviceName, fileId, recordInfoUrl, dataAbstract, onlineResources, geoEls);
                    newRecord.setContact(responsibleParty);
                    cswRecords.add(newRecord);
                }
            }
            //generate the same response from a getCSWRecords call
            records = cswRecords.toArray(new CSWRecord[cswRecords.size()]);
        }
        catch (MalformedURLException e) {
            log.debug(e.getMessage());
            return generateJSONResponseMAV(false, "URL not well formed", null);
        }
        catch (Exception e) {
            log.debug(e.getMessage());
            return generateJSONResponseMAV(false, "Unable to process request", null);
        }

        ModelAndView mav = generateJSONResponseMAV(records);
        mav.addObject("invalidLayerCount", invalidLayerCount);
        return mav;
    }

    public String[] getSRSList(String[] layerSRS, String[] childLayerSRS) {
        try{
            int totalLength = layerSRS.length;
            totalLength += childLayerSRS.length;
            String[] totalSRS = new String[totalLength];
            System.arraycopy(layerSRS, 0, totalSRS, 0, layerSRS.length);
            System.arraycopy(childLayerSRS, 0, totalSRS, layerSRS.length, childLayerSRS.length);
            Arrays.sort(totalSRS);

            int k = 0;
            for (int i = 0; i < totalSRS.length; i++) {
                if (i > 0 && totalSRS[i].equals(totalSRS[i-1])) {
                    continue;
                }
                totalSRS[k++] = totalSRS[i];
            }
            String[] uniqueSRS = new String[k];
            System.arraycopy(totalSRS, 0, uniqueSRS, 0, k);
            return uniqueSRS;
        } catch (Exception e) {
            log.debug(e.getMessage());
            return null;
        }
    }

    /**
     * Gets all the valid GetMap formats that a service defines
     * @param serviceUrl The WMS URL to query
     */
    @RequestMapping("/getLayerFormats.do")
    public ModelAndView getLayerFormats(@RequestParam("serviceUrl") String serviceUrl) throws Exception {
        try {
            GetCapabilitiesRecord capabilitiesRec = wmsService.getWmsCapabilities(serviceUrl);

            List<ModelMap> data = new ArrayList<ModelMap>();
            for (String format : capabilitiesRec.getGetMapFormats()) {
                ModelMap formatItem = new ModelMap();
                formatItem.put("format", format);
                data.add(formatItem);
            }

            return generateJSONResponseMAV(true, data, "");
        } catch (Exception e) {
            log.warn(String.format("Unable to download WMS layer formats for '%1$s'", serviceUrl));
            log.debug(e);
            return generateJSONResponseMAV(false, "Unable to process request", null);
        }
    }

    /**
    *
    * @param request
    * @param response
    * @param wmsUrl
    * @param latitude
    * @param longitude
    * @param queryLayers
    * @param x
    * @param y
    * @param bbox A CSV string formatted in the form - longitude,latitude,longitude,latitude
    * @param width
    * @param height
    * @param infoFormat
    * @throws Exception
    */
   @RequestMapping("/wmsMarkerPopup.do")
   public void wmsUnitPopup(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam("WMS_URL") String wmsUrl,
                            @RequestParam("lat") String latitude,
                            @RequestParam("lng") String longitude,
                            @RequestParam("QUERY_LAYERS") String queryLayers,
                            @RequestParam("x") String x,
                            @RequestParam("y") String y,
                            @RequestParam("BBOX") String bbox,
                            @RequestParam("WIDTH") String width,
                            @RequestParam("HEIGHT") String height,
                            @RequestParam("INFO_FORMAT") String infoFormat) throws Exception {

      String[] bboxParts = bbox.split(",");
      double lng1 = Double.parseDouble(bboxParts[0]);
      double lng2 = Double.parseDouble(bboxParts[2]);
      double lat1 = Double.parseDouble(bboxParts[1]);
      double lat2 = Double.parseDouble(bboxParts[3]);

      String responseString = wmsService.getFeatureInfo(wmsUrl, infoFormat, queryLayers, "EPSG:4326", Math.min(lng1, lng2), Math.min(lat1, lat2), Math.max(lng1, lng2), Math.max(lat1, lat2), Integer.parseInt(width), Integer.parseInt(height), Double.parseDouble(longitude), Double.parseDouble(latitude), Integer.parseInt(x), Integer.parseInt(y), "");
      InputStream responseStream = new ByteArrayInputStream(responseString.getBytes());
      this.writeInputToOutputStream(responseStream, response.getOutputStream(), 1024*1024, true);
   }
}

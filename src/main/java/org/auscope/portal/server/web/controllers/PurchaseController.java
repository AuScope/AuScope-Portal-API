package org.auscope.portal.server.web.controllers;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpRequestBase;
import org.auscope.portal.core.server.OgcServiceProviderType;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.methodmakers.WCSMethodMaker;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;
import org.auscope.portal.server.vegl.VGLDataPurchase;
import org.auscope.portal.server.vegl.VGLJobPurchase;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.auscope.portal.server.web.service.VGLPurchaseService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.model.Charge;

@Controller
public class PurchaseController extends BasePortalController {

    public static final String SESSION_DOWNLOAD_LIST = "jobDownloadList";
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    @Value("${stripeApiKey}")
    private String stripeApiKey;
    
    @Value("${erddapservice.url}")
    private String erddapServiceUrl;
    
    @Autowired
    private ANVGLUserService userService;
    
    @Autowired
    private VGLPurchaseService purchaseService;
    
    private SimpleWfsService wfsService;
    
    private CSWFilterService cswFilterService;
    
    // @Autowired
    public PurchaseController(SimpleWfsService wfsService,  CSWFilterService cswFilterService) {
        this.wfsService = wfsService;
        this.cswFilterService = cswFilterService;
    }

    /**
     * handle exception
     * @param ex
     * @return
     */
    @ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        return ex.getMessage();
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
            erddapUrlList = new ArrayList<>();
        }

        logger.info("Adding download " + download.getUrl() + " to session download list");
        synchronized(erddapUrlList) {
            erddapUrlList.add(download);
        }
        logger.info("session download list now:");
        for (VglDownload dl: erddapUrlList) {
            logger.info(dl.getUrl());
        }
        request.getSession().setAttribute(SESSION_DOWNLOAD_LIST, erddapUrlList);
    }
    
    
    /**
     * process data payment request with Stripe
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/processDataPayment.do", method = RequestMethod.POST)
    public void processDataPayment(HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        
        Stripe.apiKey = this.stripeApiKey;
        logger.info("process data payment setting stripe api key to: " + this.stripeApiKey);
        
        String result = null;
        
        float amount = 0;
        String tokenId = null;
        String email = null;

        JsonArray dataToPurchase = null;

        try {
            // read the input data
            InputStream in = request.getInputStream();
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, "UTF-8"));
            final char[] buffer = new char[1024];
            int numRead;
            StringBuffer inputData = new StringBuffer("");
            while ((numRead = reader.read(buffer)) != -1) {
                inputData.append(new String(buffer, 0, numRead));
            }
            in.close();

            logger.info("got input data: " + inputData.toString());
            JsonParser parser = new JsonParser();
            JsonObject postData = (JsonObject) parser
                    .parse(inputData.toString());
            amount = postData.getAsJsonPrimitive("amount").getAsFloat();
            tokenId = postData.getAsJsonPrimitive("tokenId").getAsString();
            email = postData.getAsJsonPrimitive("email").getAsString();
            dataToPurchase = postData.getAsJsonArray("dataToPurchase");
            logger.info("amount = " + amount + ", tokenId = " + tokenId
                    + ", email = " + email);

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject err = new JsonObject();
            err.addProperty("message",
                    "failed to parse payment input data: " + e.getMessage());
            result = err.toString();
        }
        
        // confirm that user is logged in
        ANVGLUser user = userService.getLoggedInUser();
        if (user == null) {
            JsonObject err = new JsonObject();
            err.addProperty("message",
                    "Unable to process payment for anonymous user. Please log in to proceed with purchase.");
            result = err.toString();
        } else {
            logger.info("user: " + user.getId());
        }

        if (tokenId != null 
                && user != null
                && dataToPurchase != null && dataToPurchase.size() > 0) {

            Map<String, Object> chargeParams = new HashMap<String, Object>();
            chargeParams.put("amount", (int) (amount * 100)); // cents!
            chargeParams.put("currency", "aud");
            chargeParams.put("description", "Test Charge");
            chargeParams.put("source", tokenId);
            chargeParams.put("receipt_email", email);

            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put("order_id", "1234"); // TODO: put in a meaningful value
                                              // here!
            chargeParams.put("metadata", metadata);

            try {
                Charge charge = Charge.create(chargeParams);
                logger.info(
                        "Charge processed successfully, received charge object: "
                                + charge.toJson());
                String chargeJson = charge.toJson();
                JsonParser parser = new JsonParser();
                JsonObject chargeData = (JsonObject) parser.parse(chargeJson);
                JsonObject resultData = new JsonObject();
                resultData.add("charge", chargeData);
                JsonArray downloadUrls = new JsonArray();
                resultData.add("downloadUrls", downloadUrls);

                // store all transaction records in the database
                
                for (int i = 0; i < dataToPurchase.size(); i++) {
                    try {
                        
                        JsonObject dataset = dataToPurchase.get(i).getAsJsonObject();

                        JsonObject cswRecord = dataset.getAsJsonObject("cswRecord");
                        
                        JsonObject onlineResource =  dataset.getAsJsonObject("onlineResource");
                        String onlineResourceType = getAsString(onlineResource,"type");
                        String url = getAsString(onlineResource , "url");
                        String description = getAsString(onlineResource, "description");
                        
                        JsonObject downloadOptions =  dataset.getAsJsonObject("downloadOptions");
                        String localPath = getAsString(downloadOptions, "localPath");
                        String name = getAsString(cswRecord,"name");
                        Double northBoundLatitude = getAsDouble(downloadOptions, "northBoundLatitude");
                        Double southBoundLatitude =getAsDouble(downloadOptions, "southBoundLatitude");
                        Double eastBoundLongitude = getAsDouble(downloadOptions, "eastBoundLongitude");
                        Double westBoundLongitude = getAsDouble(downloadOptions, "westBoundLongitude");
                        
                        logger.info("to store in the purchases table: " + cswRecord + "," + onlineResourceType + "," 
                                +  url + "," +  localPath + "," +  name + "," +  description + "," 
                                +  northBoundLatitude + "," +  southBoundLatitude + "," 
                                +  eastBoundLongitude + "," +  westBoundLongitude);
                        
                        String downloadUrl = getDownloadUrl(onlineResourceType, downloadOptions, getAsString(cswRecord, "recordInfoUrl"));
                        
                        // TODO: should probably extract the timestamp from the strip result json
                        VGLDataPurchase vglPurchase = new VGLDataPurchase(new Date(), amount, downloadUrl, cswRecord.toString(), 
                                onlineResourceType, url, localPath, name, description, 
                                northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude, chargeJson, 
                                user);
                        Integer id = purchaseService.saveDataPurchase(vglPurchase);
                        logger.info("saved user purchase to database, purchase id is: " + id + ", download url is: " + downloadUrl);
                        JsonObject downloadUrlObj = new JsonObject();
                        downloadUrlObj.addProperty("url", downloadUrl);
                        downloadUrlObj.addProperty("name", name);
                        downloadUrls.add(downloadUrlObj);
                        
                        // save download to session
                        VglDownload newDownload = new VglDownload();
                        newDownload.setName(name);
                        newDownload.setDescription(description);
                        newDownload.setLocalPath(localPath);
                        newDownload.setUrl(downloadUrl);
                        newDownload.setNorthBoundLatitude(northBoundLatitude);
                        newDownload.setEastBoundLongitude(eastBoundLongitude);
                        newDownload.setSouthBoundLatitude(southBoundLatitude);
                        newDownload.setWestBoundLongitude(westBoundLongitude);
                        this.addDownloadToSession(request, newDownload);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                result = resultData.toString();
                
            } catch (CardException e) {
                // If it's a decline, CardException will be caught
                logger.warn("Card payment failed with error: " + e.getCode()
                        + " (" + e.getMessage() + ")");
                JsonObject err = new JsonObject();
                err.addProperty("message",
                        "Stripe card payment failed: " + e.getMessage());
                result = err.toString();

            } catch (Exception e) {
                logger.warn("Exception while processing stripe payment: "
                        + e.getMessage());
                JsonObject err = new JsonObject();
                err.addProperty("message",
                        "Stripe card payment failed: " + e.getMessage());
                result = err.toString();
            }
        }

        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.println(result);
        writer.close();
    }
    
    /**
     * process job payment request with Stripe
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/processJobPayment.do", method = RequestMethod.POST)
    public void processJobPayment(HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        
        Stripe.apiKey = this.stripeApiKey;
        logger.info("process job payment setting stripe api key to: " + this.stripeApiKey);
        
        String result = null;
        
        float amount = 0;
        String tokenId = null;
        String email = null;
        int jobId = 0;
        String jobName = null;

        try {
            // read the input data
            InputStream in = request.getInputStream();
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, "UTF-8"));
            final char[] buffer = new char[1024];
            int numRead;
            StringBuffer inputData = new StringBuffer("");
            while ((numRead = reader.read(buffer)) != -1) {
                inputData.append(new String(buffer, 0, numRead));
            }
            in.close();

            logger.info("got input data: " + inputData.toString());
            JsonParser parser = new JsonParser();
            JsonObject postData = (JsonObject) parser
                    .parse(inputData.toString());
            amount = postData.getAsJsonPrimitive("amount").getAsFloat();
            tokenId = postData.getAsJsonPrimitive("tokenId").getAsString();
            email = postData.getAsJsonPrimitive("email").getAsString();
            jobId = postData.getAsJsonPrimitive("jobId").getAsInt();
            jobName = postData.getAsJsonPrimitive("jobName").getAsString();
            
            logger.info("amount = " + amount + ", tokenId = " + tokenId
                    + ", email = " + email + ", jobId = " + jobId + ", jobName = " + jobName);

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject err = new JsonObject();
            err.addProperty("message",
                    "failed to parse payment input data: " + e.getMessage());
            result = err.toString();
        }
        
        // confirm that user is logged in
        ANVGLUser user = userService.getLoggedInUser();
        if (user == null) {
            JsonObject err = new JsonObject();
            err.addProperty("message",
                    "Unable to process payment for anonymous user. Please log in to proceed with purchase.");
            result = err.toString();
        } else {
            logger.info("user: " + user.getId());
        }

        if (tokenId != null && user != null) {

            Map<String, Object> chargeParams = new HashMap<String, Object>();
            chargeParams.put("amount", (int) (amount * 100)); // cents!
            chargeParams.put("currency", "aud");
            chargeParams.put("description", "Test Charge");
            chargeParams.put("source", tokenId);
            chargeParams.put("receipt_email", email);

            Map<String, String> metadata = new HashMap<String, String>();
            metadata.put("order_id", "1234"); // TODO: put in a meaningful value
                                              // here!
            chargeParams.put("metadata", metadata);

            try {
                Charge charge = Charge.create(chargeParams);
                logger.info(
                        "Charge processed successfully, received charge object: "
                                + charge.toJson());
                String chargeJson = charge.toJson();
                JsonParser parser = new JsonParser();
                JsonObject chargeData = (JsonObject) parser.parse(chargeJson);
                JsonObject resultData = new JsonObject();
                resultData.add("charge", chargeData);

                // store job transaction record in the database
                                 
                // TODO: should probably extract the timestamp from the strip result json
                VGLJobPurchase vglPurchase = new VGLJobPurchase(new Date(), amount, jobId, jobName, chargeJson, user);
                Integer id = purchaseService.saveJobPurchase(vglPurchase);
                logger.info("saved user job purchase to database, purchase id is: " + id );
                     
                result = resultData.toString();
                
            } catch (CardException e) {
                // If it's a decline, CardException will be caught
                logger.warn("Card payment failed with error: " + e.getCode()
                        + " (" + e.getMessage() + ")");
                JsonObject err = new JsonObject();
                err.addProperty("message",
                        "Stripe card payment failed: " + e.getMessage());
                result = err.toString();

            } catch (Exception e) {
                logger.warn("Exception while processing stripe payment: "
                        + e.getMessage());
                JsonObject err = new JsonObject();
                err.addProperty("message",
                        "Stripe card payment failed: " + e.getMessage());
                result = err.toString();
            }
        }

        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.println(result);
        writer.close();
    }
    
    
    
    /**
     * Retrieves all purchase made by user.
     * @param user
     * @return
     * @throws PortalServiceException
     */
    @RequestMapping("/getDataPurchases.do")
    public ModelAndView getPurchases() throws PortalServiceException {
        ANVGLUser user = userService.getLoggedInUser();
        List<VGLDataPurchase> purchases = purchaseService.getDataPurchasesByUser(user);
        return generateJSONResponseMAV(true, purchases, "");
    }

    private String getAsString(JsonObject parent, String name) {
        if (parent != null && parent.has(name)) {
            return parent.getAsJsonPrimitive(name).getAsString();
        }
        return null;
    }
    
    private Double getAsDouble(JsonObject parent, String name) {
        if (parent != null && parent.has(name)) {
            return parent.getAsJsonPrimitive(name).getAsDouble();
        }
        return null;
    }
    
    private Integer getAsInt(JsonObject parent, String name) {
        if (parent != null && parent.has(name)) {
            return parent.getAsJsonPrimitive(name).getAsInt();
        }
        return null;
    }
    
    // returns substring of the url in the form http(s)://server:port/endpoint (port may be missing)
    private String getBaseUrl(String url) {
        int startIndex = 7;
        if (url.startsWith("https")) {
            startIndex = 8;
        }
        int firstSlashIndex = url.indexOf("/", startIndex);
        if (firstSlashIndex != -1) {
            int end = url.indexOf("/", firstSlashIndex + 1);
            if (end == -1) {
                end = url.length();
            }
            return url.substring(0, end);
        }
        // if get here, then just return the url as is
        return url;
    }
    
    private OgcServiceProviderType getServiceType(String serviceUrl) {
        
        OgcServiceProviderType serviceType = null;
        String serviceBaseUrl = getBaseUrl(serviceUrl);
        log.info("service base url = " + serviceBaseUrl);
        CSWServiceItem[] serviceItems = this.cswFilterService.getCSWServiceItems();
        for (CSWServiceItem item: serviceItems) {
            String itemBaseUrl = getBaseUrl(item.getRecordInformationUrl());
            log.info("item base url = " + itemBaseUrl);
            if (itemBaseUrl.contentEquals(serviceBaseUrl)) {
                serviceType = item.getServerType();
                break;
            }
        }
        return serviceType;
    }
    
    
    private String getDownloadUrl(String onlineResourceType, JsonObject downloadOptions, String cswRecordInfoUrl) {
        
        String name = getAsString(downloadOptions, "name");
        String url = getAsString(downloadOptions, "url");

        log.info("downloadOptions url = " + url);
        Double northBoundLatitude = getAsDouble(downloadOptions, "northBoundLatitude");
        Double southBoundLatitude = getAsDouble(downloadOptions, "southBoundLatitude");
        Double eastBoundLongitude = getAsDouble(downloadOptions, "eastBoundLongitude");
        Double westBoundLongitude = getAsDouble(downloadOptions, "westBoundLongitude");
        
        switch (onlineResourceType) {
        
            case "WCS": {
                
                OgcServiceProviderType serviceType = getServiceType(cswRecordInfoUrl);
                log.info("WCS service type = " + serviceType);
                
                if (serviceType != null && (serviceType == OgcServiceProviderType.GeoServer || serviceType == OgcServiceProviderType.PyCSW)) {
                    //http://localhost:8090/geoserver/wcs?service=WCS&request=GetCoverage&coverageId=tasmax_djf&format=geotiff&srsName=EPSG%3A4326&bbox=-34.68404023638139%2C150.83192110061643%2C-34.66371104796619%2C150.86144685745234%2Curn%3Aogc%3Adef%3Acrs%3AEPSG%3A4326&&version=2.0.0
                
                    String layerName = getAsString(downloadOptions, "layerName");
                    String bboxCrs = "EPSG:4326";//getAsString(downloadOptions,"crs"); 

                    CSWGeographicBoundingBox bbox = new CSWGeographicBoundingBox(westBoundLongitude, eastBoundLongitude, southBoundLatitude, northBoundLatitude);
                    HttpRequestBase downloadUrl = null;
                    try {
                        WCSMethodMaker wcsMethodMaker = new WCSMethodMaker();
                        downloadUrl = wcsMethodMaker.getCoverageMethod(url, layerName, "geotiff", bboxCrs, new Dimension(1000,1000), 
                                null, bboxCrs, bbox, null, null);
                    } catch (Exception ex) {
                        log.warn(String.format("Exception generating service request for '%2$s' from '%1$s': %3$s", url, layerName, ex));
                        ex.printStackTrace();
                        return null;
                    }
                    return downloadUrl.getRequestLine().getUri();
                    
                } else {  // assume ERDDAP???
                
                    // Unfortunately ERDDAP requests that extend beyond the spatial bounds of the dataset
                    // will fail. To workaround this, we need to crop our selection to the dataset bounds
                    Double dsNorthBoundLatitude = getAsDouble(downloadOptions, "dsNorthBoundLatitude");
                    Double dsSouthBoundLatitude = getAsDouble(downloadOptions, "dsSouthBoundLatitude");
                    Double dsEastBoundLongitude = getAsDouble(downloadOptions, "dsEastBoundLongitude");
                    Double dsWestBoundLongitude = getAsDouble(downloadOptions, "dsWestBoundLongitude");
                
                    if (dsEastBoundLongitude != null && (dsEastBoundLongitude < eastBoundLongitude))
                        eastBoundLongitude = dsEastBoundLongitude;
                    if (dsWestBoundLongitude != null && (dsWestBoundLongitude > westBoundLongitude))
                        westBoundLongitude = dsWestBoundLongitude;
                    if (dsNorthBoundLatitude != null && (dsNorthBoundLatitude < northBoundLatitude))
                        northBoundLatitude = dsNorthBoundLatitude;
                    if (dsSouthBoundLatitude != null && (dsSouthBoundLatitude > southBoundLatitude))
                        southBoundLatitude = dsSouthBoundLatitude;
                
                    String layerName = getAsString(downloadOptions, "layerName");
                    String format = getAsString(downloadOptions, "format");
                
                    // convert bbox co-ordinates to ERDDAP an ERDDAP dimension string
                    String erddapDimensions = "%5B("+ southBoundLatitude +"):1:("+ northBoundLatitude 
                        + ")%5D%5B("+ westBoundLongitude +"):1:("+ eastBoundLongitude +")%5D";
                    return this.erddapServiceUrl + layerName + "." + format + "?" + layerName + erddapDimensions;
                }
            }
               
            case "WFS": {
                String serviceUrl = getAsString(downloadOptions, "serviceUrl");
                String featureType = getAsString(downloadOptions,"featureType");
                String srsName = getAsString(downloadOptions,"srsName");
                String outputFormat = getAsString(downloadOptions,"outputFormat");
                Integer maxFeatures = getAsInt(downloadOptions,"maxFeatures");
                String bboxCrs = getAsString(downloadOptions,"crs"); 
                
                FilterBoundingBox bbox = FilterBoundingBox.parseFromValues(bboxCrs, northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude);
                String downloadUrl = null;
                try {
                    downloadUrl = wfsService.getFeatureRequestAsString(serviceUrl, featureType, bbox, maxFeatures, srsName, outputFormat);
                } catch (Exception ex) {
                    log.warn(String.format("Exception generating service request for '%2$s' from '%1$s': %3$s", serviceUrl, featureType, ex));
                }
                return downloadUrl;
            }
            case "NCSS": {
                String netcdfsubsetserviceDimensions = "&spatial=bb" +
                        "&north="+ northBoundLatitude +
                        "&south=" + southBoundLatitude +
                        "&west=" + westBoundLongitude +
                        "&east="+ eastBoundLongitude;
                return url + "?var=" + name + netcdfsubsetserviceDimensions;   
            }
            default:
                return url;
        }
    }
    
  
}

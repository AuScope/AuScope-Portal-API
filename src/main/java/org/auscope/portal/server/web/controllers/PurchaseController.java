package org.auscope.portal.server.web.controllers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.server.vegl.VGLPurchase;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.auscope.portal.server.web.service.VGLPurchaseService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.model.Charge;

@Controller
public class PurchaseController extends BasePortalController {

    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${stripeApiKey}")
    private String stripeApiKey;
    
    @Autowired
    private ANVGLUserService userService;
    
    @Autowired
    private VGLPurchaseService purchaseService;
    
    // @Autowired
    public PurchaseController() {
        super();
    }

    @ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    @RequestMapping(value = "/processPayment.do", method = RequestMethod.POST)
    public void purchase(HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        
        Stripe.apiKey = this.stripeApiKey;
        logger.info("purchase method setting stripe api key to: " + this.stripeApiKey);
        
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

            log.info("got input data: " + inputData.toString());
            JsonParser parser = new JsonParser();
            JsonObject postData = (JsonObject) parser
                    .parse(inputData.toString());
            amount = postData.getAsJsonPrimitive("amount").getAsFloat();
            tokenId = postData.getAsJsonPrimitive("tokenId").getAsString();
            email = postData.getAsJsonPrimitive("email").getAsString();
            dataToPurchase = postData.getAsJsonArray("dataToPurchase");
            log.info("amount = " + amount + ", tokenId = " + tokenId
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
            log.info("user: " + user.getId());
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
                result = charge.toJson();
                
                // store all transaction records in the database
                
                for (int i = 0; i < dataToPurchase.size(); i++) {
                    try {
                        
                        JsonObject dataset = dataToPurchase.get(i).getAsJsonObject();

                        JsonObject cswRecord = dataset.getAsJsonObject("cswRecord");
                        String cswRecordId = getAsString(cswRecord,"id");
                        
                        JsonObject onlineResource =  dataset.getAsJsonObject("onlineResource");
                        String onlineResourceType = getAsString(onlineResource,"type");
                        String url = getAsString(onlineResource , "url");
                        String description = getAsString(onlineResource, "description");
                        
                        JsonObject downloadOptions =  dataset.getAsJsonObject("downloadOptions");
                        String localPath = getAsString(downloadOptions, "localPath");
                        String name = getAsString(downloadOptions, "name");
                        Double northBoundLatitude = getAsDouble(downloadOptions, "northBoundLatitude");
                        Double southBoundLatitude =getAsDouble(downloadOptions, "southBoundLatitude");
                        Double eastBoundLongitude = getAsDouble(downloadOptions, "eastBoundLongitude");
                        Double westBoundLongitude = getAsDouble(downloadOptions, "westBoundLongitude");
                        
                        logger.info("to store in the purchases table: " + cswRecordId + "," +  onlineResourceType + "," 
                                +  url + "," +  localPath + "," +  name + "," +  description + "," 
                                +  northBoundLatitude + "," +  southBoundLatitude + "," 
                                +  eastBoundLongitude + "," +  westBoundLongitude);
                        
                        // TODO uncomment the following when ready to test out database code
                        VGLPurchase vglPurchase = new VGLPurchase(cswRecordId, onlineResourceType, url, localPath, name, description, 
                                northBoundLatitude, southBoundLatitude, eastBoundLongitude, westBoundLongitude, result, 
                                user);
                        Integer id = purchaseService.savePurchase(vglPurchase);
                        log.info("saved user purchase to database, purchase id is: " + id);
                        
                        // TODO: should construct the download url here and append it to the json response
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

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
  
}

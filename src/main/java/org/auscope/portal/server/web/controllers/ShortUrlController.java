package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker.ResultType;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.server.bookmark.BookMark;
import org.auscope.portal.server.shorturl.ShortUrl;
import org.auscope.portal.server.web.controllers.BookMarksController.bookmark;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.service.ShortUrlService;
import org.auscope.portal.server.web.service.PortalUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A controller class for managing shortened urls
 * tries to "roll" entries in the table "shorturl" whenever a new "POST" is made
 * i.e. delete entries that are not "persist" and are older than "ROLL_DAYS"
 */
@RestController
@SecurityRequirement(name = "public")
@Tag(
        name= "shorturl",
        description = "Alows the user to manage shortened urls"
    )
public class ShortUrlController extends BasePortalController {

    private static final Integer EXPIRY_HOURS = 240 ; // how long non persistant shorurls stay in the db table
	
	@Autowired
	private PortalUserService userService;
	
	@Autowired
    private ShortUrlService shorturlService;
	
    @ResponseStatus(value =  org.springframework.http.HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        System.out.println("[ShortUrlController]handleException(IllegalArgumentException).ex.getMessage()="+ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(PortalServiceException.class)
    public ResponseEntity<String> handleSpecificException(PortalServiceException ex) {
        System.out.println("[ShortUrlController]handleException(PortalServiceException).ex.getMessage()="+ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getMessage());
    }
    
    // ShortUrl.class
    static class shorturl {
        public Boolean persist;
        public String url;
    }
    
    /**
     * Creates a new identifier for the given url
     * @param serviceId
     * @return
     * @throws PortalServiceException
     */
    @PostMapping("/shorturl")
    @Operation(summary = "Create a shorturl.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content( 
                    schema = @Schema(
                        implementation = shorturl.class
                    ),
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "list of short for the current user",
                            summary = "shorturl example",
                            value = "{\"url\": \"getViaProxy.do?usewhitelist=false&url=https://developers.google.com/kml/documentation/images/etna.jpg\",\"persist\": true}"
                        )
        }))
    public ModelAndView postShortUrl(@org.springframework.web.bind.annotation.RequestBody() Map<String, Object> su) throws PortalServiceException {
        ShortUrl shorturl = new ShortUrl();
        shorturl.setUrl(su.get("url").toString());
        if (su.get("persist") != null) {
            shorturl.setPersist((Boolean) su.get("persist"));
        }
        
        LocalDateTime cutoff = LocalDateTime.now().minus(EXPIRY_HOURS, ChronoUnit.HOURS);
        // List<Integer> expiredList = shorturlService.findExpired(cutoff);                
        //System.out.println("[postShortUrl]expiredList="+expiredList.toString());        
        //expiredList.forEach((esu) -> {        
        //    System.out.println("[postShortUrl]esu="+esu.toString());
        //});
        Integer delCount = shorturlService.deleteExpired(cutoff);
        //System.out.println("[postShortUrl]Number of expired (now dleeted) shorturl's="+delCount.toString());
        
        String urlName = "";

        try {
            urlName = getUniqueName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (urlName.length() > 0) {
            shorturl.setName(urlName);
            Integer id = shorturlService.saveShorturl(shorturl);
            return generateJSONResponseMAV(true, shorturl, "");
        } else {
            return generateJSONResponseMAV(false, null, "unable to generate unique name");
        }
    }
        

    private void getExpiredShorturls(Integer rollDays) {
        // TODO Auto-generated method stub
        
    }

    /**
     * getUniqueName to get a unique combination of two five letter words
     *
     * @return
     * @throws Exception
     */
    public String getUniqueName() throws Exception {
        String serviceUrl = "https://random-word-api.herokuapp.com/word";
        HttpGet method = new HttpGet();
        URIBuilder builder = new URIBuilder(serviceUrl);
        builder.addParameter("number", "2");
        builder.addParameter("length", "5");
        //builder.addParameter("diff", "2");
        method.setURI(builder.build());

        HttpServiceCaller httpServiceCaller = new HttpServiceCaller(90000);
        String response = httpServiceCaller.getMethodResponseAsString(method);

        String[] r = response.split(",");
        String n1 = StringUtils.capitalize(r[0].substring(2, r[0].length()-1));
        String n2 = StringUtils.capitalize(r[1].substring(1, r[1].length()-2));
        
        String urlName=n1+n2;
        
        return urlName;
    }
    


    /**
     * Retrieves a  list of short url 
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Retrieves a shorturl.")
    @GetMapping("/shorturl")
    //@ApiResponse( content = @Content( mediaType = "application/json" ) )
    @ApiResponse(
            content = @Content( 
                    schema = @Schema(
                        implementation = shorturl.class
                    ),
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "list of shorturls",
                            summary = "shorturl example",
                            value = "{\"data\": [{"
                                      +  "\"id\": 25,"
                                      +  "\"url\": \"getViaProxy.do?usewhitelist=false&url=https://developers.google.com/kml/documentation/images/etna.jpg\","
                                      + "\"name\": \"NemasRunic\","
                                      +  "\"timestamp\": \"2026-03-05 10:20:19\","
                                      +  "\"persist\": true},"
                                      +  "{\"id\": 26,"
                                      +  "\"url\": \"getViaProxy.do?usewhitelist=false&url=https://developers.google.com/kml/documentation/images/etna.jpg\","
                                      +  "\"name\": \"DunamGrime\","
                                      +  "\"timestamp\": \"2026-03-05 10:43:00\","
                                      +   "\"persist\": true}]}"
                        )
        }))
    public ModelAndView getshortUrls() throws PortalServiceException {
        List<ShortUrl> shorturls = shorturlService.getShortUrls();
        return generateJSONResponseMAV(true, shorturls, "");
    }
    
    
    /**
     * Retrieves a url stored for a short url (name)
     * @param name
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Retrieves a shorturl.")
    @GetMapping("/shorturl/{name}")
    @ApiResponse( content = @Content( mediaType = "application/json" ) )
    public ModelAndView getshortUrl(@PathVariable(value="name") String name) throws PortalServiceException {
        ShortUrl shorturl = shorturlService.getShorturlByName(name);
        if (shorturl == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Name Not Found");
        }
        return new ModelAndView("redirect:/"+shorturl.getUrl());
    }
    
    /**
     * Removes a short url given the id
     * @param id
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Removes a shorturl.")
    @DeleteMapping("/shorturl/{id}")
    @ApiResponse( content = @Content( mediaType = "application/json" ) )
    public ModelAndView deleteShortUrl(@PathVariable(value="id") Integer id) throws PortalServiceException {
        ShortUrl shorturl = new ShortUrl();
        shorturl.setId(id);
        Boolean status = shorturlService.deleteShorturl(shorturl);
        if (!status) {
            return generateJSONResponseMAV(status,null,"id not found");
        } else {
            return generateJSONResponseMAV(status);
        }
    }

}

package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.security.NCIDetails;
import org.auscope.portal.server.web.service.PortalUserService;
import org.auscope.portal.server.web.service.NCIDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller class for accessing/modifying user metadata
 * @author Josh Vote
 */
@Controller
public class UserController extends BasePortalController {
    private static final String CLOUD_FORMATION_RESOURCE = "org/auscope/portal/server/web/controllers/vl-cloudformation.json.tpl";

    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private PortalUserService userService;
    
    @Autowired
    private NCIDetailsService nciDetailsService;
    
    private VelocityEngine velocityEngine;
    private CloudComputeService[] cloudComputeServices;

    @Autowired
    private String awsAccount;

    private String tacVersion;
    
    @Autowired
    public UserController(VelocityEngine velocityEngine,
    		CloudComputeService[] cloudComputeServices,
                @Value("${termsconditions.version:1}") String tacVersion) throws PortalServiceException {
        super();        
        this.velocityEngine = velocityEngine;
        this.cloudComputeServices = cloudComputeServices;
        this.tacVersion = tacVersion;
    }


    /**
     * Gets user metadata for the currently logged in user
     * @param user
     * @return
     */
    @RequestMapping("/secure/getUser.do")
    public ModelAndView getUser() {
    	PortalUser user = userService.getLoggedInUser();
        if (user == null) {
            return generateJSONResponseMAV(false);
        }
        ModelMap userObj = new ModelMap();
        userObj.put("id", user.getId());
        userObj.put("email", user.getEmail());
        userObj.put("fullName", user.getFullName());
        userObj.put("arnExecution", user.getArnExecution());
        userObj.put("arnStorage", user.getArnStorage());
        userObj.put("acceptedTermsConditions", user.getAcceptedTermsConditions());
        userObj.put("awsKeyName", user.getAwsKeyName());

        return generateJSONResponseMAV(true, userObj, "");
    }

    /**
     * Sets a variety of parameters for the currently logged in user
     * @param user
     * @param arnExecution
     * @param arnStorage
     * @param acceptedTermsConditions
     * @return
     */
    @RequestMapping("/secure/setUser.do")
    public ModelAndView setUser(
            @RequestParam(required=false, value="arnExecution") String arnExecution,
            @RequestParam(required=false, value="arnStorage") String arnStorage,
            @RequestParam(required=false, value="acceptedTermsConditions") Integer acceptedTermsConditions,
            @RequestParam(required=false, value="awsKeyName") String awsKeyName) {
    	PortalUser user = userService.getLoggedInUser();
        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        boolean modified = false;
        if (!StringUtils.isEmpty(arnExecution)) {
            user.setArnExecution(arnExecution);
            modified = true;
        }

        if (!StringUtils.isEmpty(arnStorage)) {
            user.setArnStorage(arnStorage);
            modified = true;
        }

        if (acceptedTermsConditions != null) {
            user.setAcceptedTermsConditions(acceptedTermsConditions);
            modified = true;
        }

        if (!StringUtils.equals(user.getAwsKeyName(), awsKeyName)) {
            user.setAwsKeyName(awsKeyName);
            modified = true;
        }

        if (modified) {
        	userService.saveUser(user);
        }

        return generateJSONResponseMAV(true);
    }

    @RequestMapping("/getTermsConditions.do")
    public ModelAndView getTermsConditions() {
    	PortalUser user = userService.getLoggedInUser();
        try {
            String tcs = IOUtils.toString(this.getClass().getResourceAsStream("vl-termsconditions.html"), StandardCharsets.UTF_8);

            ModelMap response = new ModelMap();
            response.put("html", tcs);
            response.put("currentVersion", Integer.parseInt(tacVersion));
            if (user != null) {
                response.put("acceptedVersion", user.getAcceptedTermsConditions());
            }

            return generateJSONResponseMAV(true, response, "");
        } catch (IOException ex) {
            log.error("Unable to read terms and conditions resource", ex);
            return generateJSONResponseMAV(false);
        }
    }

    @RequestMapping("/secure/getCloudFormationScript.do")
    public void getCloudFormationScript(HttpServletResponse response) throws IOException {
    	PortalUser user = userService.getLoggedInUser();
        if (user == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("s3Bucket", user.getS3Bucket());
        model.put("awsSecret", user.getAwsSecret());
        model.put("awsAccount", awsAccount);
        VelocityContext velocityContext = new VelocityContext(model);
        StringWriter stringWriter = new StringWriter();
        velocityEngine.mergeTemplate(CLOUD_FORMATION_RESOURCE, "UTF-8", velocityContext, stringWriter);

        String cloudFormationScript = stringWriter.toString();

        response.setContentType("application/octet");
        response.setHeader("Content-Disposition", "inline; filename=vgl-cloudformation.json;");

        try {
            response.getOutputStream().write(cloudFormationScript.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    
    @RequestMapping("/secure/getNCIDetails.do")
    public ModelAndView getNCIDetails() throws PortalServiceException {
    	PortalUser user = userService.getLoggedInUser();
        if (user == null) {
            return generateJSONResponseMAV(false);
        }
        ModelMap detailsObj = new ModelMap();
        NCIDetails details = nciDetailsService.getByUser(user);
        if(details != null) {
            try {
                detailsObj.put("nciUsername", details.getUsername());
                detailsObj.put("nciProject", details.getProject());
                detailsObj.put("nciKey", details.getKey());
            } catch(Exception e) {
                logger.error("Unable to decrypt NCI details: " + e.getLocalizedMessage());
            }
            return generateJSONResponseMAV(true, detailsObj, "");
        }
        return generateJSONResponseMAV(false);
    }
    
    @RequestMapping("/secure/setNCIDetails.do")
    public ModelAndView setNCIDetails(
            @RequestParam(required=false, value="nciUsername") String username,
            @RequestParam(required=false, value="nciProject") String project,
            @RequestParam(required=false, value="nciKey") CommonsMultipartFile key) throws PortalServiceException {
    	PortalUser user = userService.getLoggedInUser();
        if (user == null) {
            return generateJSONResponseMAV(false);
        }
        NCIDetails details = nciDetailsService.getByUser(user);
        if(details == null) {
            details = new NCIDetails();
            details.setUser(user);
        }
        boolean modified = false;
        try {
            if (!StringUtils.isEmpty(username) || !StringUtils.equals(details.getUsername(), username)) {
                details.setUsername(username);
                modified = true;
            }
            if (!StringUtils.isEmpty(project) || !StringUtils.equals(details.getProject(), project)) {
                details.setProject(project);
                modified = true;
            }
            if (key != null ) {
                String keyString = key.getFileItem().getString();
                if (!StringUtils.isEmpty(keyString) || !StringUtils.equals(details.getKey(), keyString)) {            
                    details.setKey(keyString);
                    modified = true;
                }
            }
        } catch(Exception e) {
            logger.error(e.getLocalizedMessage());
        }
            
        if (modified) {
        	nciDetailsService.saveNCIDetails(details);
        }
        return generateJSONResponseMAV(true);        
    }
    
    @RequestMapping("/secure/getHasConfiguredComputeServices.do")
    public ModelAndView getHasConfiguredComputeServices() throws PortalServiceException {
    	PortalUser user = userService.getLoggedInUser();
        if (user == null) {
            return generateJSONResponseMAV(false);
        }
        boolean hasConfigured = user.configuredServicesStatus(nciDetailsService, cloudComputeServices);
        return generateJSONResponseMAV(hasConfigured);
    }

}

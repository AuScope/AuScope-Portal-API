package org.auscope.portal.server.web.controllers;

import java.awt.Menu;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

/**
 * Controller that handles all {@link Menu}-related requests,
 *
 * @author Jarek Sanders
 * @author Josh Vote
 */
@Controller
public class MenuController {

   protected final Log logger = LogFactory.getLog(getClass());

   private PortalPropertyPlaceholderConfigurer hostConfigurer;

   @Autowired
   public MenuController(PortalPropertyPlaceholderConfigurer hostConfigurer) {
       this.hostConfigurer = hostConfigurer;
   }

   /**
    * Adds the google maps/analytics keys to the specified model
    * @param mav
    */
   private void addGoogleKeys(ModelAndView mav) {
       String googleKey = hostConfigurer.resolvePlaceholder("HOST.googlemap.key");
       String analyticKey = hostConfigurer.resolvePlaceholder("HOST.google.analytics.key");

       mav.addObject("googleKey", googleKey);
       if (analyticKey != null && !analyticKey.isEmpty()) {
           mav.addObject("analyticKey", analyticKey);;
       }
   }

   /**
    * Adds a number of manifest specific variables to the model
    * @param mav
    * @param request
    */
   private void addManifest(ModelAndView mav, HttpServletRequest request) {
       String appServerHome = request.getSession().getServletContext().getRealPath("/");
       File manifestFile = new File(appServerHome,"META-INF/MANIFEST.MF");
       Manifest mf = new Manifest();
       try {
          mf.read(new FileInputStream(manifestFile));
          Attributes atts = mf.getMainAttributes();
          if (mf != null) {
             mav.addObject("specificationTitle", atts.getValue("Specification-Title"));
             mav.addObject("implementationVersion", atts.getValue("Implementation-Version"));
             mav.addObject("implementationBuild", atts.getValue("Implementation-Build"));
             mav.addObject("buildDate", atts.getValue("buildDate"));
             mav.addObject("buildJdk", atts.getValue("Build-Jdk"));
             mav.addObject("javaVendor", atts.getValue("javaVendor"));
             mav.addObject("builtBy", atts.getValue("Built-By"));
             mav.addObject("osName", atts.getValue("osName"));
             mav.addObject("osVersion", atts.getValue("osVersion"));

             mav.addObject("serverName", request.getServerName());
             mav.addObject("serverInfo", request.getSession().getServletContext().getServerInfo());
             mav.addObject("serverJavaVersion", System.getProperty("java.version"));
             mav.addObject("serverJavaVendor", System.getProperty("java.vendor"));
             mav.addObject("javaHome", System.getProperty("java.home"));
             mav.addObject("serverOsArch", System.getProperty("os.arch"));
             mav.addObject("serverOsName", System.getProperty("os.name"));
             mav.addObject("serverOsVersion", System.getProperty("os.version"));
          }
       } catch (IOException e) {
           /* ignore, since we'll just leave an empty form */
           logger.info("Error accessing manifest: " + e.getMessage());
           logger.debug("Exception:", e);
       }
   }

   /**
    * Handles all HTML page requests by mapping them to an appropriate view (and adding other details).
    * @param request
    * @param response
    * @return
    * @throws IOException
    */
   @RequestMapping("/*.html")
   public ModelAndView handleHtmlToView(HttpServletRequest request, HttpServletResponse response) throws IOException {

       //Decode our request to get the view name we are actually requesting
       String requestUri = request.getRequestURI();
       String[] requestComponents = requestUri.split("/");
       if (requestComponents.length == 0) {
           logger.debug(String.format("request '%1$s' doesnt contain any extractable components", requestUri));
           response.sendError(HttpStatus.SC_NOT_FOUND, "Resource not found : " + requestUri);
           return null;
       }
       String requestedResource = requestComponents[requestComponents.length - 1];
       String resourceName = requestedResource.replace(".html", "");

       logger.trace(String.format("view name '%1$s' extracted from request '%2$s'", resourceName, requestUri));

       //Give the user the view they are actually requesting
       ModelAndView mav = new ModelAndView(resourceName);

       //Customise the model as required
       addGoogleKeys(mav); //always add the google keys
       if (resourceName.equals("about") || resourceName.equals("admin")) {
           addManifest(mav, request); //The manifest details aren't really required by much
       }

       return mav;
   }
}

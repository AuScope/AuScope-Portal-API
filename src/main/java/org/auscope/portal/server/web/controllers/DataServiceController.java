package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.gridjob.GridAccessController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller that handles all Data Service related requests,
 *
 * @author Abdi Jama
 */
@Controller
public class DataServiceController {
   
   protected final Log logger = LogFactory.getLog(getClass());
  
   @Autowired
   private GridAccessController gridAccess;
   
   @RequestMapping("/saveSelection.do")
   public ModelAndView saveSelection(HttpServletRequest request, String selectedList) {
	   return new ModelAndView("saveSelection");
	   
   } 

   @RequestMapping("/sendToGrid.do")
   public ModelAndView sendToGrid(HttpServletRequest request) {
	   return new ModelAndView("sendToGrid");
       // Ensure user has valid grid credentials
      /* if (gridAccess.isProxyValid(
                   request.getSession().getAttribute("userCred"))) {
           logger.debug("No/invalid action parameter; returning gridsubmit view.");
           return new ModelAndView("gridsubmit");
       } else {
           request.getSession().setAttribute(
                   "redirectAfterLogin", "/gridsubmit.html");
           logger.warn("Proxy not initialized. Redirecting to gridLogin.");
           return new ModelAndView(
                   new RedirectView("/gridLogin.html", true, false, false));
       }*/	   
   }
   
   @RequestMapping("/zipDownload.do")
   public ModelAndView zipDownload(HttpServletRequest request) {
	   return new ModelAndView("zipDownload");
       // Ensure user has valid grid credentials
      /* if (gridAccess.isProxyValid(
                   request.getSession().getAttribute("userCred"))) {
           logger.debug("No/invalid action parameter; returning gridsubmit view.");
           return new ModelAndView("gridsubmit");
       } else {
           request.getSession().setAttribute(
                   "redirectAfterLogin", "/gridsubmit.html");
           logger.warn("Proxy not initialized. Redirecting to gridLogin.");
           return new ModelAndView(
                   new RedirectView("/gridLogin.html", true, false, false));
       }*/	   
   }   
}

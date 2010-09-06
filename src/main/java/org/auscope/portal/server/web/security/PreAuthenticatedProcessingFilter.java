package org.auscope.portal.server.web.security;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * This AbstractPreAuthenticatedProcessingFilter implementation 
 * obtains the username from request header pre-populated by an 
 * external Shibboleth authentication system.
 * 
 * 
 * @author san218
 * @version $Id$
 */
public class PreAuthenticatedProcessingFilter 
   extends AbstractPreAuthenticatedProcessingFilter {

   protected final Logger logger = Logger.getLogger(getClass());
   
   protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
      java.util.Enumeration eHeaders = request.getHeaderNames();
      while(eHeaders.hasMoreElements()) {
         String name = (String) eHeaders.nextElement();
         if ( ( name.matches(".*Shib.*") || name.matches(".*shib.*") ) && 
              !name.equals("HTTP_SHIB_ATTRIBUTES") && 
              !name.equals("Shib-Attributes") ) 
         {
               Object object = request.getHeader(name);
               String value = object.toString();
               logger.debug(name + " : " + value);
         }
      }
      
      //Extract the token we will use to identify this user (it will only be unique within an IDP)
      String targetedToken = request.getHeader("Shib-Targeted-Token-v2");
      if (targetedToken == null || targetedToken.length() == 0)
      {
    	  //This is for legacy support
    	  targetedToken = request.getHeader("Shib-Targeted-Token-v1");
    	  if (targetedToken != null && targetedToken.length() > 0) {
	    	  logger.info("Using legacy targeted ID...");
	    	  
	    	  //Legacy unfortunately gives a value specific to an IDP (without any information describing the IDP)
	    	  String homeOrg = request.getHeader("Shib-Home-Organization");
	          if (homeOrg == null)
	        	  homeOrg = "unknown";
	          
	          //So lets make our own frankenstein ID from the IDP organisation name AND the token 
	          targetedToken += "!!" + homeOrg;
    	  }
      }
      
      logger.info("Token: " + targetedToken);
      if (targetedToken == null || targetedToken.length() == 0)
    	  return null;
      
      //Combine the token with the home organisation to get a unique ID for this user across all IDP's
      //TODO: We will likely have to make this return an object that can fetch all of these attributes for later usage within the portal.
      return targetedToken;
   }
   
   protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
      // no password - user is already authenticated
      return "NONE";
   }
/*
   public int getOrder() {
      return FilterChainOrder.PRE_AUTH_FILTER;
   }
*/
}
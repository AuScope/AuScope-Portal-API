package org.auscope.portal.server.web.security;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
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
	
	SecurityContextImpl context = (SecurityContextImpl)request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
	
	if (context != null) {
		OpenIDAuthenticationToken auth = (OpenIDAuthenticationToken)context.getAuthentication();
		List<OpenIDAttribute> attribs = auth.getAttributes();
		String userEmail = null;
		
		// get the openid email address from
		for (OpenIDAttribute attrib : attribs){
			if ("email".equals(attrib.getName())){
				List<String> values = attrib.getValues();
				userEmail = values.get(0);
				request.getSession().setAttribute("openID-Email", userEmail);
				logger.info("openID email: " + userEmail);
			}
		}
		
		if (userEmail == null)
		{
			logger.error("Could not get openid email attribute");
			return false;
		}
	}
	
	/*java.util.Enumeration eHeaders = request.getHeaderNames();
      while(eHeaders.hasMoreElements()) {
         String name = (String) eHeaders.nextElement();
         logger.error("header name: " + name + ", value: " + request.getHeader(name));
         
         if ( ( name.matches(".*Shib.*") || name.matches(".*shib.*") ) && 
              !name.equals("HTTP_SHIB_ATTRIBUTES") && 
              !name.equals("Shib-Attributes") ) 
         {
               Object object = request.getHeader(name);
               String value = object.toString();
               logger.debug("Shib header - " + name + " : " + value);
         }
      }
      
      if (request.getHeader("Shib-Shared-Token") != null) {
    	  
	      logger.info("Shib-Person-mail: " + request.getHeader("Shib-Person-mail"));
	      request.getSession().setAttribute("Shib-Person-mail", request.getHeader("Shib-Person-mail"));
	      request.getSession().setAttribute("Shib-Shared-Token", request.getHeader("Shib-Shared-Token"));
	      request.getSession().setAttribute("Shib-Person-commonName", request.getHeader("Shib-Person-commonName"));
      }else if (request.getHeader("shared-token") != null) {
    	  
	      logger.info("mail: " + request.getHeader("mail"));
	      request.getSession().setAttribute("Shib-Person-mail", request.getHeader("mail"));
	      request.getSession().setAttribute("Shib-Shared-Token", request.getHeader("shared-token"));
	      request.getSession().setAttribute("Shib-Person-commonName", request.getHeader("cn"));
      }*/
      
      return request.getSession().getAttribute("openID-Email");
   }
   
   protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
      // no password - user is already authenticated
      return "NONE";
   }
}
package org.auscope.portal.server.web.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.gridjob.GeodesyGridInputFile;
import org.auscope.portal.server.gridjob.GridAccessController;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller that handles debug queries
 * 
 *  All responses come in the JSONForm
 *  
 *  {
 *  	success : true/false
 *  	notes : Can be null, will specify a user friendly description of the test result
 *  }
 *
 * @author Josh Vote
 */
@Controller
public class DebugController {
	protected final Log logger = LogFactory.getLog(getClass());
	   
	@Autowired
	@Qualifier(value = "propertyConfigurer")
	private PortalPropertyPlaceholderConfigurer hostConfigurer;
	
	@Autowired
	private GridAccessController gridAccess;
	
	private ModelAndView generateResponse(boolean success, String notes) {
		ModelAndView mav = new ModelAndView("jsonView");
		
		mav.addObject("success", success);
		mav.addObject("notes", notes);
		
		return mav;
	}
	
	@RequestMapping("/dbg/checkUsername.do")
	public ModelAndView checkUsername(HttpServletRequest request) {
		final String user = request.getRemoteUser();
		
		if (user == null || user.length() == 0) {
			return generateResponse(false, "Username is null or empty");
		}
		
		return generateResponse(true, "Username is correct");
	} 
	
	@RequestMapping("/dbg/checkCredentials.do")
	public ModelAndView checkCredentials(HttpServletRequest request) {
		Object credentials = request.getSession().getAttribute("userCred");
		
		if (credentials == null) {
			return generateResponse(false, "Credentials are null");
		}
		
		if (!gridAccess.isProxyValid(credentials)) {
			return generateResponse(false, "Credentials are invalid");
		}
		
		return generateResponse(true, "Credentials are correct");
	}
}

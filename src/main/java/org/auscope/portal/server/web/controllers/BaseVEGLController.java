package org.auscope.portal.server.web.controllers;

import java.util.HashMap;
import java.util.Map;

import org.auscope.portal.server.web.view.JSONView;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

/**
 * Abstract class that provides VEGL specific utility functions for controller classes
 * @author Josh Vote
 *
 */
public abstract class BaseVEGLController {
	/**
     * Utility method to generate a standard JSON MAV response 
     * @param success
     * @param data
     * @param message
     * @return
     */
    protected ModelAndView generateJSONResponseMAV(boolean success, Object data, String message) {
    	ModelAndView result = new ModelAndView("jsonView");
    	
    	result.addObject("data", data);
        result.addObject("success", success);
        result.addObject("msg", success);
        
        return result;
    }
    
    /**
     * Utility method to generate a HTML MAV response. This will be identical in content to generateJSONResponseMAV but
     * will be set to use a HTML content type. Use this for overcoming weirdness with Ext JS and file uploads.   
     * @param success
     * @param data
     * @param message
     * @return
     */
    protected ModelAndView generateHTMLResponseMAV(boolean success, Object data, String message) {
    	JSONView view = new JSONView();
        view.setContentType("text/html");
        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("success", true);
        responseMap.put("data", new Object[] {data});
        responseMap.put("msg", message);
        return new ModelAndView(view, responseMap);
    }
}

package org.auscope.portal.server.web.controllers;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.params.HashmapParams;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.service.PortalParamsService;
import org.auscope.portal.server.web.service.PortalUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller class for accessing/modifying a user's Params 
 */
@Controller
public class PortalParamsController extends BasePortalController {

	@Autowired
	PortalParamsService paramsService;

	@Autowired    
    PortalUserService userService;

	/**
	 * Retrieve the params for the current user
	 * @param key 
	 *
	 * @return a list of Params objects, provided the user has any
	 */
	@RequestMapping("/secure/getUserParams.do")
	public ModelAndView getUserParams(
        @RequestParam(value="key") String key) throws PortalServiceException {

        PortalUser user = userService.getLoggedInUser();
    	if (user == null) {
            return generateJSONResponseMAV(false);
        }
    	HashmapParams params = paramsService.getParamsByKey(key);
		return generateJSONResponseMAV(true, params, "");
	}

	/**
	 * Add a new params for an authenticated user
	 *
	 * @param key 
	 * @param value 
	 * @throws PortalServiceException
	 */
	@RequestMapping("/secure/saveUserParams.do")
    public ModelAndView addParamsForAuthenticatedUser(
    		@RequestParam(value="key") String key,
    		@RequestParam(value="value") String value) throws PortalServiceException {
    	PortalUser user = userService.getLoggedInUser();
    	if (user == null) {
            return generateJSONResponseMAV(false);
        }		
    	HashmapParams params = new HashmapParams();
    	params.setKey(key);
    	params.setValue(value);
    	String newKey = paramsService.saveParams(params);        
        return generateJSONResponseMAV(true, newKey, "");
	}
	
	/**
	 * Update an existing params
	 *
	 * @param key 
	 * @param value 
	 * @return json with key
	 * @throws PortalServiceException
	 */
	@RequestMapping("/secure/updateUserParams.do")
    public ModelAndView updateUserParams(
        @RequestParam(value="key") String key,
        @RequestParam(value="value") String value) throws PortalServiceException {
		// Get existing params and make sure current user is owner
		PortalUser user = userService.getLoggedInUser();
		HashmapParams params = paramsService.getParamsByKey(key);
		
		if (params == null || user == null ) {
			return generateJSONResponseMAV(false);
		}
		// Update params
    	params.setKey(key);
    	params.setValue(value);
    	String newKey = paramsService.saveParams(params);        
        return generateJSONResponseMAV(true, newKey, "");
	}
	
	/**
	 * Delete a params from the DB
	 *
	 * @param key 
	 * @return a true response if successful, a PortalServiceException will be thrown if not
	 * @throws PortalServiceException
	 */
	@RequestMapping("/secure/deleteUserParams.do")
    public ModelAndView deleteUserParams(
		@RequestParam(value="key") String key) throws PortalServiceException {
		PortalUser user = userService.getLoggedInUser();
    	if (user == null) {
            return generateJSONResponseMAV(false);
        }
    	HashmapParams params = new HashmapParams();
		params.setKey(key);
		paramsService.deleteParams(params);
		return generateJSONResponseMAV(true);
	}

}

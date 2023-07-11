package org.auscope.portal.server.web.controllers;

import java.util.Date;
import java.util.List;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.state.PortalState;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.service.PortalStateService;
import org.auscope.portal.server.web.service.PortalUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller class for accessing/modifying a user's portal states 
 */
@Controller
public class PortalStateController extends BasePortalController {

	@Autowired
	PortalStateService stateService;
	
	@Autowired
	PortalUserService userService;
	
	/**
	 * Retrieve the states for the current user
	 *
	 * @return a list of PortalState objects, provided the user has any
	 */
	@RequestMapping("/secure/getUserPortalStates.do")
	public ModelAndView getPortalStates() {
		PortalUser user = userService.getLoggedInUser();
    	if (user == null) {
            return generateJSONResponseMAV(false);
        }
    	List<PortalState> states = stateService.getStatesByUser(user);
		return generateJSONResponseMAV(true, states, "");
	}
	
	/**
	 * Retrieve a specific state for the current user
	 * 
	 * @param stateId the ID of the state
	 * @return PortalState object
	 */
	@RequestMapping("/getPortalState.do")
	public ModelAndView getPortalState(@RequestParam(value="stateId") String stateId) {
		PortalUser user = userService.getLoggedInUser();
    	PortalState state = stateService.getStateById(stateId);
    	// If state exists and is private make sure owner is current user 
    	if (state == null || (!state.getIsPublic() && (user == null || user.getId() != state.getParent().getId()))) {
    		return generateJSONResponseMAV(false);
    	}
		return generateJSONResponseMAV(true, state, "");
	}
	
	/**
	 * Add a new state for an anonymous user
	 *
	 * @param id state id
	 * @param name name of state
	 * @param description a brief description of the state (optional)
	 * @param jsonState the state as a JSON string
	 * @return integer ID of the state in the DB if successful
	 * @throws PortalServiceException
	 */
	@RequestMapping("/savePortalState.do")
    public ModelAndView addPortalState(
    		@RequestParam(value="id") String id,
    		@RequestParam(value="name") String name,
    		@RequestParam(value="description", required=false) String description,
    		@RequestParam(value="jsonState") String jsonState,
    		@RequestParam(value="isPublic") boolean isPublic) throws PortalServiceException {
    	PortalUser user = userService.getLoggedInUser();
    	PortalState state = new PortalState();
    	state.setId(id);
    	state.setName(name);
    	state.setDescription(description);
    	state.setCreationDate(new Date());
    	state.setJsonState(jsonState);
    	state.setIsPublic(isPublic);
    	if (user != null) {
    		state.setParent(user);
    	}
    	String newId = stateService.savePortalState(state);        
        return generateJSONResponseMAV(true, newId, "");
	}
	
	/**
	 * Add a new state for an authenticated user
	 *
	 * @param id state id
	 * @param name name of state
	 * @param description a brief description of the state (optional)
	 * @param jsonState the state as a JSON string
	 * @return integer ID of the state in the DB if successful
	 * @throws PortalServiceException
	 */
	@RequestMapping("/secure/savePortalState.do")
    public ModelAndView addPortalStateForAuthenticatedUser(
    		@RequestParam(value="id") String id,
    		@RequestParam(value="name") String name,
    		@RequestParam(value="description", required=false) String description,
    		@RequestParam(value="jsonState") String jsonState,
    		@RequestParam(value="isPublic") boolean isPublic) throws PortalServiceException {
    	PortalUser user = userService.getLoggedInUser();
    	PortalState state = new PortalState();
    	state.setId(id);
    	state.setName(name);
    	state.setDescription(description);
    	state.setCreationDate(new Date());
    	state.setJsonState(jsonState);
    	state.setIsPublic(isPublic);
    	state.setParent(user);
    	String newId = stateService.savePortalState(state);        
        return generateJSONResponseMAV(true, newId, "");
	}
	
	/**
	 * Update an existing state
	 *
	 * @param id state id
	 * @param name name of state
	 * @param description a brief description of the state (optional)
	 * @param jsonState the state as a JSON string
	 * @return integer ID of the state in the DB if successful
	 * @throws PortalServiceException
	 */
	@RequestMapping("/secure/updatePortalState.do")
    public ModelAndView updatePortalState(
    		@RequestParam(value="id") String id,
    		@RequestParam(value="userId") String userId,
    		@RequestParam(value="name") String name,
    		@RequestParam(value="description", required=false) String description,
    		@RequestParam(value="isPublic") boolean isPublic) throws PortalServiceException {
		// Get existing state and make sure current user is owner
		PortalUser user = userService.getLoggedInUser();
		PortalState state = stateService.getStateById(id);
		
		if (state == null || user == null || !userId.equals(user.getId())) {
			return generateJSONResponseMAV(false);
		}
		// Update state
    	state.setName(name);
    	state.setDescription(description);
    	state.setIsPublic(isPublic);
    	String newId = stateService.savePortalState(state);        
        return generateJSONResponseMAV(true, newId, "");
	}
	
	/**
	 * Delete a state from the DB
	 *
	 * @param id the ID of the state
	 * @return a true response if successful, a PortalServiceException will be thrown if not
	 * @throws PortalServiceException
	 */
	@RequestMapping("/secure/deletePortalState.do")
    public ModelAndView deletePortalState(@RequestParam(value="id") String id) throws PortalServiceException {
		PortalUser user = userService.getLoggedInUser();
    	if (user == null) {
            return generateJSONResponseMAV(false);
        }
    	PortalState state = new PortalState();
		state.setId(id);
    	state.setParent(user);
		stateService.deletePortalState(state);
		return generateJSONResponseMAV(true);
	}

}

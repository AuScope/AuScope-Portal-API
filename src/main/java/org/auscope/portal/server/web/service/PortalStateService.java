package org.auscope.portal.server.web.service;

import java.util.List;

import org.auscope.portal.server.state.PortalState;
import org.auscope.portal.server.web.repositories.PortalStateRepository;
import org.auscope.portal.server.web.security.PortalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortalStateService {
	
	@Autowired
	private PortalStateRepository stateRepository;
	
	public List<PortalState> getStatesByUser(PortalUser user) {
		return stateRepository.findByParent(user);
	}
	
	public PortalState getStateById(String stateId) {
		return stateRepository.findById(stateId);
	}
	
	public String savePortalState(PortalState state) {
		PortalState s = stateRepository.saveAndFlush(state);
		return s.getId();
	}
	
	public void deletePortalState(PortalState state) {
		stateRepository.delete(state);
	}
}

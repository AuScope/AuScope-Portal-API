package org.auscope.portal.server.web.service;
import org.auscope.portal.server.params.HashmapParams;
import org.auscope.portal.server.web.repositories.PortalParamsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortalParamsService {
	
	@Autowired
	private PortalParamsRepository paramsRepository;	

	public HashmapParams getParamsByKey(String key) {
		return paramsRepository.findByKey(key);
	}
	
	public String saveParams(HashmapParams params) {
		HashmapParams s = paramsRepository.saveAndFlush(params);
		return s.getKey();
	}
	
	public void deleteParams(HashmapParams params) {
		paramsRepository.delete(params);
	}
}

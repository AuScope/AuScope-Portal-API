package org.auscope.portal.server.web.service;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.repositories.NCIDetailsEncRepository;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.NCIDetails;
import org.auscope.portal.server.web.security.NCIDetailsEnc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class NCIDetailsService {
	
	@Autowired
	NCIDetailsEncRepository nciEncRepository;
	
	@Autowired
	ANVGLUserService userService;

	@Lazy
    @Autowired
	private VGLCryptoService encryptionService;
	
	public NCIDetails getByUser(ANVGLUser user) throws PortalServiceException {
		NCIDetailsEnc encRes = nciEncRepository.findByUser(user);
        NCIDetails res = new NCIDetails();
        if(encRes != null) {
	        res.setId(encRes.getId());
	        if(encRes.getKey() != null) {
	        	res.setKey(encryptionService.decrypt(encRes.getKey()));
	        }
	        if(encRes.getProject() != null) {
	        	res.setProject(encryptionService.decrypt(encRes.getProject()));
	        }
	        res.setUser(encRes.getUser());
	        if(encRes.getUsername() != null) {
	        	res.setUsername(encryptionService.decrypt(encRes.getUsername()));
	        }
        }
        return res;
	}
	
	public void saveNCIDetails(NCIDetails details) throws PortalServiceException {
		NCIDetailsEnc detailsEnc = new NCIDetailsEnc();
        detailsEnc.setId(details.getId());
        detailsEnc.setKey(encryptionService.encrypt(details.getKey()));
        detailsEnc.setProject(encryptionService.encrypt(details.getProject()));
        //detailsEnc.setUser(details.getUser());
        detailsEnc.setUser(userService.getLoggedInUser());
        detailsEnc.setUsername(encryptionService.encrypt(details.getUsername()));
		nciEncRepository.save(detailsEnc);
	}
	
	/*
	 private VGLCryptoService encryptionService;
    
    public NCIDetailsDao(VGLCryptoService encryptionService) {
        this.encryptionService=encryptionService;
    }
    
    public NCIDetails getByUser(ANVGLUser user) throws PortalServiceException {
        List<?> resList = getHibernateTemplate().findByNamedParam("from NCIDetailsEnc d where d.user =:p", "p", user);
        if(resList.isEmpty()) return null;
        NCIDetailsEnc encRes = (NCIDetailsEnc) resList.get(0);
        NCIDetails res = new NCIDetails();
        res.setId(encRes.getId());
        res.setKey(encryptionService.decrypt(encRes.getKey()));
        res.setProject(encryptionService.decrypt(encRes.getProject()));
        res.setUser(encRes.getUser());
        res.setUsername(encryptionService.decrypt(encRes.getUsername()));
        return res;
    }
    
    public void save(NCIDetails details) throws PortalServiceException { 
        NCIDetailsEnc detailsEnc = new NCIDetailsEnc();
        detailsEnc.setId(details.getId());
        detailsEnc.setKey(encryptionService.encrypt(details.getKey()));
        detailsEnc.setProject(encryptionService.encrypt(details.getProject()));
        detailsEnc.setUser(details.getUser());
        detailsEnc.setUsername(encryptionService.encrypt(details.getUsername()));
        
        getHibernateTemplate().saveOrUpdate(detailsEnc);
    }
	 */
}

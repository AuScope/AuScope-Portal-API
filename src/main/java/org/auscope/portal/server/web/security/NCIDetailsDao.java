package org.auscope.portal.server.web.security;

import java.util.List;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.service.VGLCryptoService;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class NCIDetailsDao extends HibernateDaoSupport {

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
}

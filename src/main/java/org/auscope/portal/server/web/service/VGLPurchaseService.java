package org.auscope.portal.server.web.service;

import java.util.List;

import org.auscope.portal.server.vegl.VGLBookMark;
import org.auscope.portal.server.vegl.VGLBookMarkDownload;
import org.auscope.portal.server.vegl.VGLPurchase;
import org.auscope.portal.server.web.repositories.VGLBookMarkDownloadRepository;
import org.auscope.portal.server.web.repositories.VGLBookMarkRepository;
import org.auscope.portal.server.web.repositories.VGLPurchaseRepository;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VGLPurchaseService {

	@Autowired
	private VGLPurchaseRepository purchaseRepository;
	
	
	/**
     * Retrieves the pourchases for a user
     * @param user 
     */
    public List<VGLPurchase> getPurchasesByUser(final ANVGLUser user) {
    	return purchaseRepository.findByParent(user);
    }
    
    
    /**
     * Saves a purchase.
     */
    public Integer savePurchase(final VGLPurchase purchase) {
    	VGLPurchase savedPurchase = purchaseRepository.saveAndFlush(purchase);
    	return savedPurchase.getId();
    }

}

package org.auscope.portal.server.web.service;

import java.util.List;


import org.auscope.portal.server.vegl.VGLDataPurchase;
import org.auscope.portal.server.vegl.VGLJobPurchase;
import org.auscope.portal.server.web.repositories.VGLDataPurchaseRepository;
import org.auscope.portal.server.web.repositories.VGLJobPurchaseRepository;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VGLPurchaseService {

	@Autowired
	private VGLDataPurchaseRepository dataPurchaseRepository;
	
	@Autowired
	private VGLJobPurchaseRepository jobPurchaseRepository;
	
	
	/**
     * Retrieves the data purchases for a user
     * @param user 
     */
    public List<VGLDataPurchase> getDataPurchasesByUser(final ANVGLUser user) {
    	return dataPurchaseRepository.findByParentOrderByDateDesc(user);
    }
    
    
    /**
     * Saves a data purchase.
     */
    public Integer saveDataPurchase(final VGLDataPurchase purchase) {
    	VGLDataPurchase savedPurchase = dataPurchaseRepository.saveAndFlush(purchase);
    	return savedPurchase.getId();
    }
    
    /**
     * Retrieves the data purchases for a user
     * @param user 
     */
    public List<VGLJobPurchase> getJobPurchasesByUser(final ANVGLUser user) {
        return jobPurchaseRepository.findByParent(user);
    }
    
    
    /**
     * Saves a data purchase.
     */
    public Integer saveJobPurchase(final VGLJobPurchase purchase) {
        VGLJobPurchase savedPurchase = jobPurchaseRepository.saveAndFlush(purchase);
        return savedPurchase.getId();
    }

}

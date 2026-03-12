package org.auscope.portal.server.web.service;

import java.time.LocalDateTime;
import java.util.List;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.shorturl.ShortUrl;
import org.auscope.portal.server.web.repositories.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShortUrlService {

    @Autowired
    private PortalUserService userService;
    
	@Autowired
	private ShortUrlRepository shorturlRepository;
    
    /**
     * Retrieves the short urls by id
     * @param id 
     */
    public ShortUrl getShorturlById(final String id) {// throws PortalServiceException {
        return shorturlRepository.findByName(id);
    }
    
    
    /**
     * Saves a short url.
     */
    public Integer saveShorturl(final ShortUrl shorturl)  throws PortalServiceException {
        ShortUrl savedShorturl = null;
        try {
            savedShorturl = shorturlRepository.saveAndFlush(shorturl);
        } catch (Exception e) {
            System.out.println("[ShortUrlRepository]saveShorturl(saveAndFlush).Exception="+e.getMessage());
        }
        if (savedShorturl != null) {
            return savedShorturl.getId();
        } else {
            return -1;
        }
    }

    /**
     * Delete the given short url by id
     * @param id 
     */
    public Boolean deleteShorturl(final ShortUrl shorturl) {
        Boolean status = true;
        if (shorturlRepository.existsById(shorturl.getId())) {
            shorturlRepository.deleteById(shorturl.getId());
        } else {
            System.out.println("[ShortUrlRepository]deleteShorturl(deleteById).Exception=does not exist, id=" + shorturl.getId());
            status = false;
        }
        return  status;
    }

    /**
     * get the given short url by name
     * @param id 
     */
    public ShortUrl getShorturlByName(String name) {
        ShortUrl shorturlRec = shorturlRepository.findByName(name);
        return shorturlRec;
    }


    public List<ShortUrl> getShortUrls() {
        return (List<ShortUrl>) shorturlRepository.findAll();
    }


    public List<Integer> findExpired(LocalDateTime cutoff) {
        return shorturlRepository.findExpired(cutoff);
    }

    public Integer deleteExpired(LocalDateTime cutoff) {
        return shorturlRepository.deleteExpired(cutoff);
    }
    
    

}

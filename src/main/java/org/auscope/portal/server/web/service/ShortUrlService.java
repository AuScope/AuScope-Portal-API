package org.auscope.portal.server.web.service;

import java.time.LocalDateTime;
import java.util.List;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.shorturl.ShortUrl;
import org.auscope.portal.server.web.repositories.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A utility class which provides methods for a shortend url service
 *
 * @author pet22a
 *
 */
@Service
public class ShortUrlService {

    @Autowired
    private PortalUserService userService;
    
	@Autowired
	private ShortUrlRepository shorturlRepository;
    
    /**
     * Retrieves the short url by id
     * @param id 
     */
    public ShortUrl getShorturlById(final String id) {// throws PortalServiceException {
        return shorturlRepository.findByName(id);
    }
    
    
    /**
     * Saves a short url.
     * @param shorturl object
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
     * @param shorturl 
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
     * @param name 
     */
    public ShortUrl getShorturlByName(String name) {
        ShortUrl shorturlRec = shorturlRepository.findByName(name);
        return shorturlRec;
    }


    /**
     * returns a list of all shorturls
     */
    public List<ShortUrl> getShortUrls() {
        return (List<ShortUrl>) shorturlRepository.findAll();
    }

    /**
     * returns a list of "ids" for "expired" shorturls (i.e. dates older than the cutoff date)
     * @param cutoff date
     */
    public List<Integer> findExpired(LocalDateTime cutoff) {
        return shorturlRepository.findExpired(cutoff);
    }

    /**
     * deletes shorturls that have a timestamp before the cutoff date
     * @param cutoff date
     */
    public Integer deleteExpired(LocalDateTime cutoff) {
        return shorturlRepository.deleteExpired(cutoff);
    }
    
    

}

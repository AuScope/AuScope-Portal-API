package org.auscope.portal.server.vegl;

import java.util.List;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * A data access object for VGLBookMark
 * @author Josh Vote
 *
 */
@Transactional
public class VGLBookMarkDao extends HibernateDaoSupport {	

    /**
     * Retrieves the bookmarks for a user
     * @param user 
     */
	//@SuppressWarnings("unchecked")
    public List<VGLBookMark> getByUser(final ANVGLUser user) throws PortalServiceException {
    	@SuppressWarnings("unchecked")
    	List<VGLBookMark> resList = (List<VGLBookMark>) getHibernateTemplate().findByNamedParam("from VGLBookMark d where d.parent =:p", "p", user);    	
        return resList;
    }
    
    

    /**
     * Saves a dataset as a bookmark.
     */
    public Integer save(final VGLBookMark bookmark) throws PortalServiceException {
    	Integer bookmarkId = (Integer)getHibernateTemplate().save(bookmark);
    	return  bookmarkId;
    }

    /**
     * Delete the given book marks.
     */
    public void delete(final VGLBookMark bookmark) {
        getHibernateTemplate().delete(bookmark);
    }


}

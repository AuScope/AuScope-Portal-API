package org.auscope.portal.server.vegl;

import java.util.List;

import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.NCIDetails;
import org.auscope.portal.server.web.security.NCIDetailsEnc;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class VGLBookMarkDao extends HibernateDaoSupport {	

    /**
     * Retrieves the bookmarks for a user
     * @param user 
     */
	//@SuppressWarnings("unchecked")
    public List<VGLBookMark> getByUser(final ANVGLUser user) throws PortalServiceException {
    	List<VGLBookMark> resList = (List<VGLBookMark>) getHibernateTemplate().findByNamedParam("from VGLBookMark d where d.parent =:p", "p", user);    	
        return resList;
    }
    
    

    /**
     * Saves or updates the given book marks.
     */
    public void save(final VGLBookMark bookmark) throws PortalServiceException {
        getHibernateTemplate().saveOrUpdate(bookmark);
    }

    /**
     * Delete the given book marks.
     */
    public void delete(final VGLBookMark bookmark) {
        getHibernateTemplate().delete(bookmark);
    }


}

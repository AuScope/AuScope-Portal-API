package org.auscope.portal.server.vegl;

import java.util.List;

import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.NCIDetails;
import org.auscope.portal.server.web.security.NCIDetailsEnc;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A data access object for VGLBookMark
 * @author Josh Vote
 *
 */
public class VGLBookMarkDownloadDao extends HibernateDaoSupport {	

    /**
     * Retrieves the bookmarks for a user
     * @param user 
     */
	//@SuppressWarnings("unchecked")
    public List<VGLBookMarkDownload> getByBookMark(final VGLBookMark bookmark) throws PortalServiceException {
    	@SuppressWarnings("unchecked")
    	List<VGLBookMarkDownload> resList = (List<VGLBookMarkDownload>) getHibernateTemplate().findByNamedParam("from VGLBookMarkDownload d where d.parent =:p", "p", bookmark);    	
        return resList;
    }
    
    

    /**
     * Saves or updates the given book marks.
     */
    public Integer save(final VGLBookMarkDownload bookmarkDownloads) throws PortalServiceException {
        Integer bookmarkDownloadsId = (Integer)getHibernateTemplate().save(bookmarkDownloads);
        return  bookmarkDownloadsId;
    }

    /**
     * Delete the given book marks.
     */
    public void delete(final VGLBookMarkDownload bookmarkDownloads) {
        getHibernateTemplate().delete(bookmarkDownloads);
    }


}

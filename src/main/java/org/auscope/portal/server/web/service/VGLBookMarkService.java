package org.auscope.portal.server.web.service;

import java.util.List;

import org.auscope.portal.server.vegl.VGLBookMark;
import org.auscope.portal.server.vegl.VGLBookMarkDownload;
import org.auscope.portal.server.web.repositories.VGLBookMarkDownloadRepository;
import org.auscope.portal.server.web.repositories.VGLBookMarkRepository;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VGLBookMarkService {

	@Autowired
	private VGLBookMarkRepository bookmarkRepository;
	
	@Autowired
	private VGLBookMarkDownloadRepository bookmarkDownloadRepository;
	
	
	/**
     * Retrieves the bookmarks for a user
     * @param user 
     */
    public List<VGLBookMark> getBookmarkByUser(final ANVGLUser user) {// throws PortalServiceException {
    	return bookmarkRepository.findByParent(user);
    }
    
    
    /**
     * Saves a bookmark.
     */
    public Integer saveBookmark(final VGLBookMark bookmark) {// throws PortalServiceException {
    	VGLBookMark savedBookmark = bookmarkRepository.saveAndFlush(bookmark);
    	return savedBookmark.getId();
    }

    /**
     * Delete the given book mark.
     */
    public void deleteBookmark(final VGLBookMark bookmark) {
    	bookmarkRepository.delete(bookmark);
    }
    
    
	/**
     * Retrieves the bookmark downloads for a user
     * @param user 
     */
    public List<VGLBookMarkDownload> getBookmarkDownloadsByBookMark(final VGLBookMark bookmark) {// throws PortalServiceException {
    	return bookmarkDownloadRepository.findByParent(bookmark);
    }
    
    

    /**
     * Saves or updates the given book mark.
     */
    public Integer saveBookmarkDownload(final VGLBookMarkDownload bookmarkDownload) {// throws PortalServiceException {
    	VGLBookMarkDownload saveDownload = bookmarkDownloadRepository.saveAndFlush(bookmarkDownload);
    	return saveDownload.getId();
    }

    /**
     * Delete the given book mark.
     */
    public void deleteBookmarkDownload(final VGLBookMarkDownload bookmarkDownload) {
    	bookmarkDownloadRepository.delete(bookmarkDownload);
    }
}

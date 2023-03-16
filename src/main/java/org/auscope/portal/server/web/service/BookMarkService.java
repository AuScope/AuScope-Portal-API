package org.auscope.portal.server.web.service;

import java.util.List;

import org.auscope.portal.server.bookmark.BookMark;
import org.auscope.portal.server.bookmark.BookMarkDownload;
import org.auscope.portal.server.web.repositories.BookMarkDownloadRepository;
import org.auscope.portal.server.web.repositories.BookMarkRepository;
import org.auscope.portal.server.web.security.PortalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookMarkService {

	@Autowired
	private BookMarkRepository bookmarkRepository;
	
	@Autowired
	private BookMarkDownloadRepository bookmarkDownloadRepository;
	
	
	/**
     * Retrieves the bookmarks for a user
     * @param user 
     */
    public List<BookMark> getBookmarkByUser(final PortalUser user) {// throws PortalServiceException {
    	return bookmarkRepository.findByParent(user);
    }
    
    
    /**
     * Saves a bookmark.
     */
    public Integer saveBookmark(final BookMark bookmark) {// throws PortalServiceException {
    	BookMark savedBookmark = bookmarkRepository.saveAndFlush(bookmark);
    	return savedBookmark.getId();
    }

    /**
     * Delete the given book mark.
     */
    public void deleteBookmark(final BookMark bookmark) {
    	bookmarkRepository.delete(bookmark);
    }
    
    
	/**
     * Retrieves the bookmark downloads for a user
     * @param user 
     */
    public List<BookMarkDownload> getBookmarkDownloadsByBookMark(final BookMark bookmark) {// throws PortalServiceException {
    	return bookmarkDownloadRepository.findByParent(bookmark);
    }
    
    

    /**
     * Saves or updates the given book mark.
     */
    public Integer saveBookmarkDownload(final BookMarkDownload bookmarkDownload) {// throws PortalServiceException {
    	BookMarkDownload saveDownload = bookmarkDownloadRepository.saveAndFlush(bookmarkDownload);
    	return saveDownload.getId();
    }

    /**
     * Delete the given book mark.
     */
    public void deleteBookmarkDownload(final BookMarkDownload bookmarkDownload) {
    	bookmarkDownloadRepository.delete(bookmarkDownload);
    }
}

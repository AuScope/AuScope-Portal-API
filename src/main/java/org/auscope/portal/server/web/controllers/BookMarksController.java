package org.auscope.portal.server.web.controllers;

import java.util.List;
import java.util.Map;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.bookmark.BookMark;
import org.auscope.portal.server.bookmark.BookMarkDownload;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.service.PortalUserService;
import org.auscope.portal.server.web.service.BookMarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;


/**
 * A controller class for accessing/modifying bookmark information for a user
 * @author san239
 */
//@Tag(name = "bookmarks", description="Manage Bookmarks and the download options")
@RestController
@SecurityRequirement(name = "public")
public class BookMarksController extends BasePortalController {
	
	@Autowired
	private PortalUserService userService;
	
	@Autowired
	private BookMarkService bookmarkService;
	
    @ResponseStatus(value =  org.springframework.http.HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    /*
    /**
     * Adds a dataset as a book mark. Uses fileIdentifier and service id from CSW record.
     * @param fileIdentifier
     * @param serviceId
     * @return
     * @throws PortalServiceException
     */
    @Deprecated
    @Operation(summary = "Adds a dataset as a book mark.",
               description = "Uses fileIdentifier and service id from CSW record.")
    @PostMapping("/secure/addBookMark.do")  
    public ModelAndView addBookMark(@RequestBody Map<String, Object> bm) throws PortalServiceException {
        PortalUser user = userService.getLoggedInUser();
        BookMark bookMark = new BookMark();
        bookMark.setParent(userService.getLoggedInUser());
        bookMark.setFileIdentifier(bm.get("fileIdentifier").toString());
        bookMark.setServiceId(bm.get("serviceId").toString());
        bookMark.setParent(user);
        Integer id = bookmarkService.saveBookmark(bookMark);        
        return generateJSONResponseMAV(true, id, "");
    }
    
    /**
     * Adds a dataset as a book mark. Uses fileIdentifier and service id from CSW record.
     * @param fileIdentifier
     * @param serviceId
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Adds a dataset as a book mark.",
            description = "Uses fileIdentifier and service id from CSW record.")
    @PostMapping("/bookmarks")
    public ModelAndView postBookMark(@RequestBody Map<String, Object> bm) throws PortalServiceException {
        return addBookMark(bm);
    }

	/**
	 * Retrieves book information for a user.
	 * @return
	 * @throws PortalServiceException
	 */
    @Deprecated
    @Operation(summary = "Retrieves book information for a user.")
    @GetMapping("/secure/getBookMarks.do")
	public ModelAndView getbookMarks() throws PortalServiceException {
		PortalUser user = userService.getLoggedInUser();
		List<BookMark> bookMarks = bookmarkService.getBookmarkByUser(user);
		return generateJSONResponseMAV(true, bookMarks, "");
	}

    /**
     * Retrieves book information for a user.
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Retrieves book information for a user.")
	@GetMapping("/bookmarks")
    public ModelAndView getbookMarksNew() throws PortalServiceException {
        PortalUser user = userService.getLoggedInUser();
        List<BookMark> bookMarks = bookmarkService.getBookmarkByUser(user);
        return generateJSONResponseMAV(true, bookMarks, "");
    }

	/**
	 * Removes a book mark.
	 * @param id
	 * @return
	 * @throws PortalServiceException
	 */
    @Deprecated
    @Operation(summary = "Removes a book mark.")
    @DeleteMapping("/secure/deleteBookMark.do")
	public ModelAndView deleteBookMark(@RequestParam(value="id") Integer id) throws PortalServiceException {
		PortalUser user = userService.getLoggedInUser();
		BookMark bookMark = new BookMark();
		bookMark.setId(id);
		bookMark.setParent(user);		
		bookmarkService.deleteBookmark(bookMark);
		return generateJSONResponseMAV(true);
	}

    /**
     * Removes a book mark.
     * @param id
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Removes a book mark.")
    @DeleteMapping("/bookmarks/{id}")
    public ModelAndView deleteBookMarkNew(@PathVariable(value="id") Integer id) throws PortalServiceException {;
        return deleteBookMark(id);
    }

	/**
	 * Retrieves download options stored for a book mark
	 * @param bookmarkId
	 * @return
	 * @throws PortalServiceException
	 */
    @Deprecated
    @Operation(summary = "Retrieves download options stored for a book mark.")
    @GetMapping("/secure/getDownloadOptions.do")
    public ModelAndView getDownloadOptions(@RequestParam(value="bookmarkId") Integer bookmarkId) throws PortalServiceException {
		BookMark bookmark = new BookMark();
		bookmark.setId(bookmarkId);
		List<BookMarkDownload> bookMarkDownloads = bookmarkService.getBookmarkDownloadsByBookMark(bookmark);		 
		return generateJSONResponseMAV(true, bookMarkDownloads, "");	
    }

    /**
     * Retrieves download options stored for a book mark
     * @param bookmarkId
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Retrieves download options stored for a book mark.")
    @GetMapping("/bookmarks/{id}/downloadOptions")
    public ModelAndView getDownloadOptionsNew(@PathVariable(value="id") Integer bookmarkId) throws PortalServiceException {
        BookMark bookmark = new BookMark();
        bookmark.setId(bookmarkId);
        List<BookMarkDownload> bookMarkDownloads = bookmarkService.getBookmarkDownloadsByBookMark(bookmark);         
        return generateJSONResponseMAV(true, bookMarkDownloads, "");    
    }
    
	/**
	 * Adds the download options for a book mark
	 * @param bookmarkId
	 * @param bookmarkOptionName
	 * @param url
	 * @param localPath
	 * @param name
	 * @param description
	 * @param northBoundLatitude
	 * @param eastBoundLongitude
	 * @param southBoundLatitude
	 * @param westBoundLongitude
	 * @return
	 * @throws PortalServiceException
	 */
    @Deprecated
    @Operation(summary = "Adds the download options for a book mark.")
    @PostMapping("/secure/saveDownloadOptions.do")
    public ModelAndView saveDownloadOptions(@RequestBody Map<String, Object> bm) throws PortalServiceException {
		PortalUser user = userService.getLoggedInUser();
		BookMarkDownload bookMarkDownload = new BookMarkDownload();			
		BookMark bookmark = new BookMark();
        bookmark.setId((Integer) bm.get("bookmarkId"));
        bookmark.setParent(user);
        bookMarkDownload.setParent(bookmark);
        bookMarkDownload.setBookmarkOptionName(bm.get("bookmarkOptionName").toString());
        bookMarkDownload.setUrl(bm.get("url").toString());
        bookMarkDownload.setLocalPath(bm.get("localPath").toString());
        bookMarkDownload.setName(bm.get("name").toString());
        bookMarkDownload.setDescription(bm.get("description").toString());
        bookMarkDownload.setEastBoundLongitude((Double) bm.get("eastBoundLongitude")); 
        bookMarkDownload.setNorthBoundLatitude((Double) bm.get("northBoundLatitude"));     
        bookMarkDownload.setWestBoundLongitude((Double) bm.get("westBoundLongitude"));     
        bookMarkDownload.setSouthBoundLatitude((Double) bm.get("southBoundLatitude")); 
		Integer id = bookmarkService.saveBookmarkDownload(bookMarkDownload);
        return generateJSONResponseMAV(true, id, "");
    }
    
    /**
     * Adds the download options for a book mark
     * @param bookmarkId
     * @param bookmarkOptionName
     * @param url
     * @param localPath
     * @param name
     * @param description
     * @param northBoundLatitude
     * @param eastBoundLongitude
     * @param southBoundLatitude
     * @param westBoundLongitude
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Adds the download options for a book mark.")
    @PostMapping("/booksmarks/{id}/downloadOptions")
    public ModelAndView saveDownloadOptionsNew(@PathVariable(value="id") Integer bookmarkId, @RequestBody Map<String, Object> bm) throws PortalServiceException {
        if (!bm.containsKey("bookmarkId")) {
            bm.put("bookmarkId", bookmarkId);
        }
        return saveDownloadOptions(bm);
    }

	/**
	 * Removes a download option stored as a book mark for the user.
	 * @param id
	 * @return
	 * @throws PortalServiceException
	 */
    @Deprecated
    @Operation(summary = "Removes a download option stored as a book mark for the user.")
    @DeleteMapping("/secure/deleteDownloadOptions.do")
	public ModelAndView deleteDownloadOptions(@RequestParam(value="id") Integer id) throws PortalServiceException {
		BookMarkDownload bookMarkDownload = new BookMarkDownload();	
		bookMarkDownload.setId(id);
		bookmarkService.deleteBookmarkDownload(bookMarkDownload);
		return generateJSONResponseMAV(true);
	}

    /**
     * Removes a download option stored as a book mark for the user.
     * @param id
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Removes a download option stored as a book mark for the user.")
    @DeleteMapping("/bookmarks/{id}/downloadOptions")
    public ModelAndView deleteDownloadOptionsNew(@RequestParam(value="id") Integer id) throws PortalServiceException {
        BookMarkDownload bookMarkDownload = new BookMarkDownload(); 
        bookMarkDownload.setId(id);
        bookmarkService.deleteBookmarkDownload(bookMarkDownload);
        return generateJSONResponseMAV(true);
    }
}

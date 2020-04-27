package org.auscope.portal.server.web.controllers;

import java.util.List;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.vegl.VGLBookMark;
import org.auscope.portal.server.vegl.VGLBookMarkDownload;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.auscope.portal.server.web.service.VGLBookMarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;


/**
 * A controller class for accessing/modifying bookmark information for a user
 * @author san239
 */
@Controller
public class BookMarksController extends BasePortalController {
	
	@Autowired
	private ANVGLUserService userService;
	
	@Autowired
	private VGLBookMarkService bookmarkService;
	
	/*
	@Autowired
	public BookMarksController(VGLBookMarkDao vGLBookMarkDao, VGLBookMarkDownloadDao vGLBookMarkDownloadDao) {
		 super();
        this.vGLBookMarkDao = vGLBookMarkDao;
        this.vGLBookMarkDownloadDao = vGLBookMarkDownloadDao;
    }
    */

    @ResponseStatus(value =  org.springframework.http.HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

    /**
     * adds a dataset as a book mark. Uses fileIdentifier and service id from csw record. 
     * @param fileIdentifier
     * @param serviceId
     * @param user
     * @return
     * @throws PortalServiceException
     */
	@RequestMapping("/addBookMark.do")
    public ModelAndView addBookMark(@RequestParam(value="fileIdentifier") String fileIdentifier,
            @RequestParam(value="serviceId") String serviceId/*,           
            @AuthenticationPrincipal ANVGLUser user*/) throws PortalServiceException {
		ANVGLUser user = userService.getLoggedInUser();
		VGLBookMark bookMark = new VGLBookMark();
		bookMark.setParent(userService.getLoggedInUser());
		bookMark.setFileIdentifier(fileIdentifier);
		bookMark.setServiceId(serviceId);
		bookMark.setParent(user);
		Integer id = bookmarkService.saveBookmark(bookMark);        
        return generateJSONResponseMAV(true, id, "");
    }
	/**
	 * Retrieves book information for a user.
	 * @param user
	 * @return
	 * @throws PortalServiceException
	 */
	
	@RequestMapping("/getBookMarks.do")
	//public ModelAndView getbookMarks(@AuthenticationPrincipal ANVGLUser user) throws PortalServiceException {
	public ModelAndView getbookMarks() throws PortalServiceException {
		ANVGLUser user = userService.getLoggedInUser();
		List<VGLBookMark> bookMarks = bookmarkService.getBookmarkByUser(user);
		return generateJSONResponseMAV(true, bookMarks, "");
	}
	
	/**
	 * removes a book mark. Uses fileIdentifier and service id from csw record. 
	 * @param fileIdentifier
	 * @param serviceId
	 * @param user
	 * @return
	 * @throws PortalServiceException
	 */
	@RequestMapping("/deleteBookMark.do")
	public ModelAndView deleteBookMark(@RequestParam(value="id") Integer id/*,                       
            @AuthenticationPrincipal ANVGLUser user*/) throws PortalServiceException {
		ANVGLUser user = userService.getLoggedInUser();
		VGLBookMark bookMark = new VGLBookMark();
		bookMark.setId(id);
		bookMark.setParent(user);		
		bookmarkService.deleteBookmark(bookMark);
		return generateJSONResponseMAV(true);
	}
		
	/**
	 * Retrieves download options stored for a book mark
	 * @param bookMark
	 * @param user
	 * @return
	 * @throws PortalServiceException
	 */
	@RequestMapping("/getDownloadOptions.do")
    public ModelAndView getDownloadOptions(@RequestParam(value="bookmarkId") Integer bookmarkId/*,
            @AuthenticationPrincipal ANVGLUser user*/) throws PortalServiceException {
		//ANVGLUser user = userService.getLoggedInUser();
		VGLBookMark bookmark = new VGLBookMark();
		bookmark.setId(bookmarkId);
		List<VGLBookMarkDownload> bookMarkDownloads = bookmarkService.getBookmarkDownloadsByBookMark(bookmark);		 
		return generateJSONResponseMAV(true, bookMarkDownloads, "");
		
    }
	
	/**
	 * Adds the download options for a book mark
	 * @param bookMark
	 * @param user
	 * @return
	 * @throws PortalServiceException
	 */
	@RequestMapping("/saveDownloadOptions.do")
    public ModelAndView saveDownloadOptions(@RequestParam(value="bookmarkId") Integer bookmarkId,
            								@RequestParam(value="bookmarkOptionName") String bookmarkOptionName,    		
								            @RequestParam(value="url") final String url,
								            @RequestParam(value="localPath") final String localPath,            
								            @RequestParam(value="name") final String name,
								            @RequestParam(value="description") final String description,
								            @RequestParam(value="northBoundLatitude", required=false) final Double northBoundLatitude,
								            @RequestParam(value="eastBoundLongitude", required=false) final Double eastBoundLongitude,
								            @RequestParam(value="southBoundLatitude", required=false) final Double southBoundLatitude,
								            @RequestParam(value="westBoundLongitude", required=false) final Double westBoundLongitude/*,
								            @AuthenticationPrincipal ANVGLUser user*/) throws PortalServiceException {
		ANVGLUser user = userService.getLoggedInUser();
		VGLBookMarkDownload bookMarkDownload = new VGLBookMarkDownload();			
		VGLBookMark bookmark = new VGLBookMark();
		bookmark.setId(bookmarkId);
		bookmark.setParent(user);
		bookMarkDownload.setParent(bookmark);
		bookMarkDownload.setBookmarkOptionName(bookmarkOptionName);
		bookMarkDownload.setUrl(url);
		bookMarkDownload.setLocalPath(localPath);
		bookMarkDownload.setName(name);
		bookMarkDownload.setDescription(description);
		bookMarkDownload.setEastBoundLongitude(eastBoundLongitude);	
		bookMarkDownload.setNorthBoundLatitude(northBoundLatitude);		
		bookMarkDownload.setWestBoundLongitude(westBoundLongitude);		
		bookMarkDownload.setSouthBoundLatitude(southBoundLatitude);		
		Integer id = bookmarkService.saveBookmarkDownload(bookMarkDownload);
        return generateJSONResponseMAV(true, id, "");
    }
	
	/**
	 * removes a download option stored as a book mark for the user.  
	 * @param fileIdentifier
	 * @param serviceId
	 * @param user
	 * @return
	 * @throws PortalServiceException
	 */
	@RequestMapping("/deleteDownloadOptions.do")
	public ModelAndView deleteDownloadOptions(@RequestParam(value="id") Integer id/*,			
            @AuthenticationPrincipal ANVGLUser user*/) throws PortalServiceException {
		//ANVGLUser user = userService.getLoggedInUser();
		VGLBookMarkDownload bookMarkDownload = new VGLBookMarkDownload();	
		bookMarkDownload.setId(id);
		bookmarkService.deleteBookmarkDownload(bookMarkDownload);
		return generateJSONResponseMAV(true);
	}
}

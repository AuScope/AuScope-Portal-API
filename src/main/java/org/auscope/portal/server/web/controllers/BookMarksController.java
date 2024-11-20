package org.auscope.portal.server.web.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.bookmark.BookMark;
import org.auscope.portal.server.bookmark.BookMarkDownload;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.service.BookMarkService;
import org.auscope.portal.server.web.service.PortalUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * A controller class for accessing/modifying bookmark information for a user
 * @author san239
 */
@RestController
@SecurityRequirement(name = "public")
@Tag(
        name= "bookmarks",
        description = "Alows the user to manage bookmarks for layers"
    )
public class BookMarksController extends BasePortalController {
	
	@Autowired
	private PortalUserService userService;
	
	@Autowired
	private BookMarkService bookmarkService;
	
    @ResponseStatus(value =  org.springframework.http.HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        return ex.getMessage();
    }
    
    /**
     * Adds a dataset as a book mark. Uses fileIdentifier and service id from CSW record.
     * @param fileIdentifier
     * @param serviceId
     * @return
     * @throws PortalServiceException
     */
    @PostMapping("/bookmarks")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content( 
                    schema = @Schema(
                        implementation = bookmark.class
                    ),
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "list of bookmarks for the current user",
                            summary = "bookmarks example",
                            value = "{\"fileIdentifier\": \"remanent-anomalies\"," 
                                    + "\"serviceId\": \"\"}"
                        )
        }))
    public ModelAndView postBookMark(@org.springframework.web.bind.annotation.RequestBody() Map<String, Object> bm) throws PortalServiceException {
        PortalUser user = userService.getLoggedInUser();
        BookMark bookMark = new BookMark();
        bookMark.setParent(userService.getLoggedInUser());
        bookMark.setFileIdentifier(bm.get("fileIdentifier").toString());
        bookMark.setServiceId(bm.get("serviceId").toString());
        bookMark.setParent(user);
        Integer id = bookmarkService.saveBookmark(bookMark);        
        return generateJSONResponseMAV(true, id, "");
    }


    // BookMark.class without the user (which is not passed fromt he frontend)
    static class bookmark {
        public int id;
        public String fileIdentifier;
        public int serviceId;
    }
    
    /**
     * Retrieves bookmark information for a user.
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Retrieves bookmark information for a user.")
    @GetMapping("/bookmarks")
    @ApiResponse(
            content = @Content( 
                    schema = @Schema(
                        implementation = bookmark.class
                    ),
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "list of bookmarks for the current user",
                            summary = "bookmarks example",
                            value = "{\"data\": [{"
                                    + "\"id\": 59,"
                                    + "\"fileIdentifier\": \"remanent-anomalies\"," 
                                    + "\"serviceId\": \"\","
                                    + "\"bookMarkDownloads\": [ ]},"
                                    + "{\"id\": 72,"
                                    + "\"fileIdentifier\": \"regolith-depth-layer\"," 
                                    + "\"serviceId\": \"\","
                                    + "\"bookMarkDownloads\": [ ]}],"
                                    + "\"msg\": \"\","
                                    + "\"success\": true}"
                        )
        }))    
    public ModelAndView getbookMarks() throws PortalServiceException {
        PortalUser user = userService.getLoggedInUser();
        List<BookMark> bookMarks = bookmarkService.getBookmarkByUser(user);
        return generateJSONResponseMAV(true, bookMarks, "");
    }

    /**
     * Retrieves download options stored for a book mark
     * @param bookmarkId
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Retrieves a bookmark.")
    @GetMapping("/bookmarks/{id}")
    @ApiResponse( content = @Content( mediaType = "application/json" ) )
    public ModelAndView getbookMarks(@PathVariable(value="id") Integer id) throws PortalServiceException {
        Optional<BookMark> bookmark = bookmarkService.getBookmarkById(id);

        return generateJSONResponseMAV(true, bookmark, "");    
    }
    
    /**
     * Removes a book mark.
     * @param id
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Removes a bookmark.")
    @DeleteMapping("/bookmarks/{id}")
    @ApiResponse( content = @Content( mediaType = "application/json" ) )
    public ModelAndView deleteBookMark(@PathVariable(value="id") Integer id) throws PortalServiceException {
        PortalUser user = userService.getLoggedInUser();
        BookMark bookMark = new BookMark();
        bookMark.setId(id);
        bookMark.setParent(user);       
        bookmarkService.deleteBookmark(bookMark);
        return generateJSONResponseMAV(true);
    }

    /**
     * Retrieves download options stored for a book mark
     * @param bookmarkId
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Retrieves download options stored for a bookmark.")
    @GetMapping("/bookmarks/{id}/downloadOptions")
    @ApiResponse( content = @Content( mediaType = "application/json" ) )
    public ModelAndView getDownloadOptions(@PathVariable(value="id") Integer bookmarkId) throws PortalServiceException {
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
    @Operation(summary = "Adds the download options for a bookmark.")
    @PostMapping("/booksmarks/{id}/downloadOptions")
    @ApiResponse( content = @Content( mediaType = "application/json" ) )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content( 
                    schema = @Schema(
                        implementation = BookMarkDownload.class
                    ),
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "list of bookmarks for the current user",
                            summary = "bookmarks example",
                            value = "{\"id\": 59,"
                                    + "\"fileIdentifier\": \"remanent-anomalies\"," 
                                    + "\"serviceId\": \"\","
                                    + "\"bookMarkDownloads\": [ ]}"
                        )
        }))
    public ModelAndView saveDownloadOptions(@PathVariable(value="id") Integer bookmarkId, @org.springframework.web.bind.annotation.RequestBody Map<String, Object> bm) throws PortalServiceException {
        if (!bm.containsKey("bookmarkId")) {
            bm.put("bookmarkId", bookmarkId);
        }
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
     * Removes a download option stored as a book mark for the user.
     * @param id
     * @return
     * @throws PortalServiceException
     */
    @Operation(summary = "Removes a download option stored as a bookmark for the user.")
    @DeleteMapping("/bookmarks/{id}/downloadOptions")
    @ApiResponse( content = @Content( mediaType = "application/json" ) )
    public ModelAndView deleteDownloadOptions(@RequestParam(value="id") Integer id) throws PortalServiceException {
        BookMarkDownload bookMarkDownload = new BookMarkDownload(); 
        bookMarkDownload.setId(id);
        bookmarkService.deleteBookmarkDownload(bookMarkDownload);
        return generateJSONResponseMAV(true);
    }
    
}

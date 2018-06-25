package org.auscope.portal.server.web.controllers;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.server.vegl.VGLBookMark;
import org.auscope.portal.server.vegl.VGLBookMarkDao;
import org.auscope.portal.server.web.security.ANVGLUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class BookMarksController  extends BasePortalController {
	
	private VGLBookMarkDao vGLBookMarkDao;
	
	@Autowired
	public BookMarksController(VGLBookMarkDao vGLBookMarkDao) {
		 super();
        this.vGLBookMarkDao = vGLBookMarkDao;
    }

    @ResponseStatus(value =  org.springframework.http.HttpStatus.BAD_REQUEST)
    public @ResponseBody String handleException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

	@RequestMapping("/addBookMark.do")
    public ModelAndView addBookMark(@RequestParam(value="fileIdentifier") String fileIdentifier,
            @RequestParam(value="serviceId") String serviceId,           
            @AuthenticationPrincipal ANVGLUser user) throws PortalServiceException {		
		VGLBookMark bookMark = new VGLBookMark();
		bookMark.setFileIdentifier(fileIdentifier);
		bookMark.setServiceId(serviceId);
		bookMark.setParent(user);
		vGLBookMarkDao.save(bookMark);
        return generateJSONResponseMAV(true);
    }
	
	@RequestMapping("/getBookMarks.do")
	public ModelAndView getbookMarks( @AuthenticationPrincipal ANVGLUser user) throws PortalServiceException {	
		VGLBookMark bookMark = new VGLBookMark();
		bookMark.setParent(user);
		List<VGLBookMark> bookMarks = vGLBookMarkDao.getByUser(user);
		return generateJSONResponseMAV(true, bookMarks, "");
	}
	
	@RequestMapping("/deleteBookMark.do")
	public ModelAndView deleteBookMark(@RequestParam(value="fileIdentifier") String fileIdentifier,
            @RequestParam(value="serviceId") String serviceId,           
            @AuthenticationPrincipal ANVGLUser user) throws PortalServiceException {	
		VGLBookMark bookMark = new VGLBookMark();
		bookMark.setFileIdentifier(fileIdentifier);
		bookMark.setServiceId(serviceId);
		bookMark.setParent(user);		
		vGLBookMarkDao.delete(bookMark);
		return generateJSONResponseMAV(true);
	}

}

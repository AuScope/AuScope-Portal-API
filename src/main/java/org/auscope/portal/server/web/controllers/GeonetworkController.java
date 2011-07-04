package org.auscope.portal.server.web.controllers;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.csw.CSWGeographicElement;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWOnlineResourceImpl;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.CSWRecordTransformer;
import org.auscope.portal.server.domain.xml.XMLStreamAttributeExtractor;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.auscope.portal.server.gridjob.GeodesyJobManager;
import org.auscope.portal.server.gridjob.GeodesySeries;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.util.Util;
import org.auscope.portal.server.web.GeonetworkDetails;
import org.auscope.portal.server.web.GeonetworkMethodMaker;
import org.auscope.portal.server.web.service.GeonetworkService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.JobStorageService;
import org.eclipse.emf.ecore.xml.type.internal.RegEx;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.ProviderCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A controller class for marshalling a users interactions with Geonetwork
 * @author Josh Vote
 *
 */
@Controller
public class GeonetworkController {

	protected final Log logger = LogFactory.getLog(getClass());
	
    private GeodesyJobManager jobManager;
    private GeonetworkService gnService;
    private JobStorageService jobStorageService;
    
    @Autowired
    public GeonetworkController(GeodesyJobManager jobManager, GeonetworkService gnService, JobStorageService jobStorageService) {
        this.jobManager = jobManager;
        this.gnService = gnService;
        this.jobStorageService = jobStorageService;
    }
    
    
	
    /**
     * Converts a job into a CSWRecord that can be stored in a registry
     * @param job
     * @return
     * @throws MalformedURLException
     * @throws S3ServiceException 
     */
    private CSWRecord jobToCSWRecord(GeodesyJob job, GeodesySeries series) throws MalformedURLException, S3ServiceException {
    	FileInformation[] outputFiles = jobStorageService.getOutputFileDetails(job);
    	List<CSWOnlineResource> onlineResources = new ArrayList<CSWOnlineResource>();
    	
    	if (outputFiles != null) {
    		for (FileInformation obj : outputFiles) {
    			String url = String.format("http://%1$s.s3.amazonaws.com/%2$s", job.getS3OutputBucket(), obj.getS3Key());
    			onlineResources.add(new CSWOnlineResourceImpl(new URL(url), "WWW:DOWNLOAD-1.0-ftp--download", obj.getName(), obj.getName()));
    		}
    	}
    	
    	CSWGeographicElement[] geoEls = new CSWGeographicElement[] {new CSWGeographicBoundingBox(-30, 60, -30, 60)};
    	
    	CSWRecord rec = new CSWRecord("VEGL Workflow Portal", "CSIRO", "", "", "TODO:", onlineResources.toArray(new CSWOnlineResource[onlineResources.size()]), geoEls);
    	
    	rec.setContactEmail(job.getEmailAddress());
    	
    	return rec;
    }
    
	/**
     * Requests that the portal should insert details of a job into GeoNetwork
     * @param jobId
     * @param request
     * @return JasonArray with success set to true or false.
     * @throws Exception
     */
    @RequestMapping("/insertRecord.do")
    public ModelAndView insertRecord(@RequestParam("jobId") final String jobId) throws Exception {
    	ModelAndView mav = new ModelAndView("jsonView");
    	
    	try {
    		//Lookup our appropriate job
    		GeodesyJob job = jobManager.getJobById(Integer.parseInt(jobId));
    		GeodesySeries jobSeries = jobManager.getSeriesById(job.getSeriesId());
    		
    		//Create an instance of our CSWRecord and transform it to a <gmd:MD_Metadata> record
    		CSWRecord record = jobToCSWRecord(job, jobSeries);
    		
    		//Lets connect to geonetwork and then send our new record
    		String metadataRecordUrl = gnService.makeCSWRecordInsertion(record);
    		job.setRegistered(metadataRecordUrl);
        	jobManager.saveJob(job);
        	
        	mav.addObject("success", true );
    	} catch (Exception ex) {
    		logger.warn("Error sending job to Geonetwork for jobId=" + jobId, ex);
    		mav.addObject("success", "false");
    		mav.addObject("error", "Internal error");
    	}

		return mav;
    }
    
    
    
}

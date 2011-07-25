package org.auscope.portal.server.web.controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.auscope.portal.csw.CSWGeographicElement;
import org.auscope.portal.csw.CSWOnlineResource;
import org.auscope.portal.csw.CSWOnlineResourceImpl;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.cloud.S3FileInformation;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.web.service.GeonetworkService;
import org.auscope.portal.server.web.service.JobStorageService;
import org.jets3t.service.S3ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller class for marshalling a users interactions with Geonetwork
 * @author Josh Vote
 *
 */
@Controller
public class GeonetworkController extends BaseVEGLController {

	protected final Log logger = LogFactory.getLog(getClass());
	
    private VEGLJobManager jobManager;
    private GeonetworkService gnService;
    private JobStorageService jobStorageService;
    
    @Autowired
    public GeonetworkController(VEGLJobManager jobManager, GeonetworkService gnService, JobStorageService jobStorageService) {
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
    private CSWRecord jobToCSWRecord(VEGLJob job, VEGLSeries series) throws MalformedURLException, S3ServiceException {
    	S3FileInformation[] outputFiles = jobStorageService.getOutputFileDetails(job);
    	List<CSWOnlineResource> onlineResources = new ArrayList<CSWOnlineResource>();
    	
    	if (outputFiles != null) {
    		for (S3FileInformation obj : outputFiles) {
    			onlineResources.add(new CSWOnlineResourceImpl(new URL(obj.getPublicUrl()), "WWW:DOWNLOAD-1.0-ftp--download", obj.getName(), obj.getName()));
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
     * @return A generic VEGL JSON response with the data element populated with the geonetwork URL string (on success)
     * @throws Exception
     */
    @RequestMapping("/insertRecord.do")
    public ModelAndView insertRecord(@RequestParam("jobId") final Integer jobId) throws Exception {
    	
        //Lookup our appropriate job
        VEGLJob job = jobManager.getJobById(jobId);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The specified job does not exist.");
        }
        
        //Lookup our series
        VEGLSeries jobSeries = jobManager.getSeriesById(job.getSeriesId());
        if (jobSeries == null) {
            return generateJSONResponseMAV(false, null, "The specified job does not belong to a series.");
        }
        
    	try {
    		//Create an instance of our CSWRecord and transform it to a <gmd:MD_Metadata> record
    		CSWRecord record = jobToCSWRecord(job, jobSeries);
    		
    		//Lets connect to geonetwork and then send our new record
    		String metadataRecordUrl = gnService.makeCSWRecordInsertion(record);
    		job.setRegisteredUrl(metadataRecordUrl);
        	jobManager.saveJob(job);
        
        	return generateJSONResponseMAV(true, metadataRecordUrl, "");
    	} catch (Exception ex) {
    		logger.warn("Error sending job to Geonetwork for jobId=" + jobId, ex);
    		return generateJSONResponseMAV(false, null, "Internal error");
    	}
    }
    
    
    
}

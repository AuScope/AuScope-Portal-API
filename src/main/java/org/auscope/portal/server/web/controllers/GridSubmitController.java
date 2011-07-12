package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.auscope.portal.server.web.service.JobFileService;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.ProviderCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;


/**
 * Controller for the job submission view.
 *
 * @author Cihan Altinay
 * @author Abdi Jama
 * @author Josh Vote
 */
@Controller
public class GridSubmitController extends BaseVEGLController {

    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private VEGLJobManager jobManager;
    @Autowired
    private JobFileService jobFileService;
    
    @Autowired
    @Qualifier(value = "propertyConfigurer")
    private PortalPropertyPlaceholderConfigurer hostConfigurer;
    
    //This is a file path for a CENTOS VM
    private static final String ERRDAP_SUBSET_VM_FILE_PATH = "/tmp/vegl-subset.csv";
    
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_DONE = "Done";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_UNSUBMITTED = "Unsubmitted";
    
    
    
    /**
     * Returns a JSON object containing a populated VEGLJob object.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a data attribute containing a populated
     *         VEGLJob object and a success attribute.
     */
    @RequestMapping("/getJobObject.do")    
    public ModelAndView getJobObject(@RequestParam("jobId") String jobId) {
        try {
        	VEGLJob job = jobManager.getJobById(Integer.parseInt(jobId));
        	return generateJSONResponseMAV(true, job, "");
        } catch (Exception ex) {
        	logger.error("Error fetching job with id " + jobId, ex);
    		return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }
    }
    
    /**
     * Returns a JSON object containing a newly created VEGLJob object.
     * @param request
     * @return
     */
    @RequestMapping("/createJobObject.do")    
    public ModelAndView createJobObject(HttpServletRequest request) {
    	
    	//Create our initial job object
    	VEGLJob newJob = createDefaultVEGLJob(request.getSession());
    	
    	//Generate our stage in directory
    	try {
    		jobFileService.generateStageInDirectory(newJob);
    	} catch (Exception ex) {
    		logger.error("Error creating input dir", ex);
    		return generateJSONResponseMAV(false, null, "Error creating input directory");
    	}
    	
    	//Create the subset file and dump it in our stage in directory
    	HashMap<String,String> erdapUrlMap = (HashMap) request.getSession().getAttribute(ERRDAPController.SESSION_ERRDAP_URL_MAP);
    	if (erdapUrlMap != null) {
    		createSubsetScriptFile(newJob, erdapUrlMap);
    	} else {
    		logger.warn("No subset area selected in session!");
    	}
    	
    	//Save our job to the database before returning it to the user
    	try {
    		jobManager.saveJob(newJob);
    		return generateJSONResponseMAV(true, newJob, "");
    	} catch (Exception ex) {
    		//On failure make sure we delete the new directory
    		jobFileService.deleteStageInDirectory(newJob);
    		logger.error("Error saving newly created job", ex);
    		return generateJSONResponseMAV(false, null, "Error saving newly created job");
    	}
    }
    
    /**
     * Returns a JSON object containing an array of filenames and sizes which
     * are currently in the job's stage in directory.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a files attribute which is an array of
     *         filenames.
     */
    @RequestMapping("/listJobFiles.do")    
    public ModelAndView listJobFiles(@RequestParam("jobId") String jobId) {

    	//Lookup our job
    	VEGLJob job = null;
    	try {
        	job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
        	logger.error("Error fetching job with id " + jobId, ex);
    		return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }

        //Get our files
        File[] files = null;
		try {
			files = jobFileService.listStageInDirectoryFiles(job);
		} catch (IOException ex) {
			logger.error("Error listing job stage in directory", ex);
			return generateJSONResponseMAV(false, null, "Error reading job stage in directory");
		}
		List<FileInformation> fileInfos = new ArrayList<FileInformation>();
		for (File file : files) {
			fileInfos.add(new FileInformation(file));
		}
        
        return generateJSONResponseMAV(true, fileInfos, "");
    }

    /**
     * Sends the contents of a input job file to the client.
     *
     * @param request The servlet request including a filename parameter
     *                
     * @param response The servlet response receiving the data
     *
     * @return null on success or the joblist view with an error parameter on
     *         failure.
     * @throws IOException 
     */
    @RequestMapping("/downloadInputFile.do")
    public ModelAndView downloadFile(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam("jobId") String jobId,
                                     @RequestParam("filename") String filename) throws Exception {

    	//Lookup our job and download the specified files (any exceptions will return a HTTP 503)
    	VEGLJob job = jobManager.getJobById(Integer.parseInt(jobId));
        jobFileService.handleFileDownload(job, filename, response);
		return null;
    }
    
    /**
     * Processes a file upload request returning a JSON object which indicates
     * whether the upload was successful and contains the filename and file
     * size.
     *
     * @param request The servlet request
     * @param response The servlet response containing the JSON data
     *
     * @return null
     */
    @RequestMapping("/uploadFile.do")    
    public ModelAndView uploadFile(HttpServletRequest request,
    								HttpServletResponse response,
                                   @RequestParam("jobId") String jobId) {
    	
    	//Lookup our job
    	VEGLJob job = null;
    	try {
        	job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
        	logger.error("Error fetching job with id " + jobId, ex);
    		return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }
        
        //Handle incoming file
        File file = null;
        try {
        	file = jobFileService.handleFileUpload(job, (MultipartHttpServletRequest) request);
		} catch (IOException ex) {
			logger.error("Error uploading file", ex);
    		return generateJSONResponseMAV(false, null, "Error uploading file");
		}
		FileInformation fileInfo = new FileInformation(file);
		
		//We have to use a HTML response due to ExtJS's use of a hidden iframe for file uploads
		//Failure to do this will result in the upload working BUT the user will also get prompted
		//for a file download containing the encoded response from this function (which we don't want).
		return generateHTMLResponseMAV(true, fileInfo, "");
    }

    /**
     * Deletes one or more uploaded files of the current job.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute that indicates whether
     *         the files were successfully deleted.
     */
    @RequestMapping("/deleteFiles.do")    
    public ModelAndView deleteFiles(@RequestParam("jobId") String jobId,
    								@RequestParam("fileName") String[] fileNames) {

    	VEGLJob job = null;
    	try {
        	job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
        	logger.error("Error fetching job with id " + jobId, ex);
    		return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }
    	
        for (String fileName : fileNames) {
        	boolean success = jobFileService.deleteStageInFile(job, fileName);
        	logger.debug("Deleting " + fileName + " success=" + success);
        }
        
        return generateJSONResponseMAV(true, null, "");
    }
    
    /**
     * Get status of the current job submission.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute that indicates the status.
     *         
     */
    @RequestMapping("/getJobStatus.do")  
    public ModelAndView getJobStatus(@RequestParam("jobId") String jobId) {
    	
    	//Get our job
    	VEGLJob job = null;
    	try {
        	job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
        	logger.error("Error fetching job with id " + jobId, ex);
    		return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }
        
        return generateJSONResponseMAV(true, job.getStatus(), "");
    }
    
    /**
     * Cancels the current job submission. Called to clean up temporary files.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return null
     */
    @RequestMapping("/cancelSubmission.do")    
    public ModelAndView cancelSubmission(@RequestParam("jobId") String jobId) {

    	//Get our job
    	VEGLJob job = null;
    	try {
        	job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
        	logger.error("Error fetching job with id " + jobId, ex);
    		return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }
        
        boolean success = jobFileService.deleteStageInDirectory(job);
        return generateJSONResponseMAV(success, null, "");
    }
    
    /**
     * Given an entire job object this function attempts to save the specified job with ID
     * to the internal database.
     * 
     * @return A JSON object with a success attribute that indicates whether
     *         the job was successfully updated.
     * @param job
     * @return
     */
    @RequestMapping("/updateJob.do")
    public ModelAndView updateJob(VEGLJob job) {
    	try {
    		jobManager.saveJob(job);
    	} catch (Exception ex) {
        	logger.error("Error updating job " + job, ex);
    		return generateJSONResponseMAV(false, null, "Error saving job");
        }
    	
    	return generateJSONResponseMAV(true, null, "");
    }
    
    /**
     * Processes a job submission request.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute that indicates whether
     *         the job was successfully submitted.
     */
    @RequestMapping("/submitJob.do")    
    public ModelAndView submitJob(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam("jobId") String jobId) {

    	//Get our job
    	VEGLJob job = null;
    	try {
        	job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
        	logger.error("Error fetching job with id " + jobId, ex);
    		return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }
        
    	//Check we have S3 credentials otherwise there is no point in continuing
        if (job.getS3OutputAccessKey() == null || job.getS3OutputAccessKey().isEmpty() ||
        	job.getS3OutputSecretKey() == null || job.getS3OutputSecretKey().isEmpty() ||
        	job.getS3OutputBucket() == null || job.getS3OutputBucket().isEmpty()) {
        	logger.error("No output S3 credentials found. NOT submitting job!");
        	
            job.setStatus(STATUS_FAILED);
            jobManager.saveJob(job);
            
            return generateJSONResponseMAV(false, null, "No output S3 credentials found. NOT submitting job!");
        }  
        

        logger.info("Submitting job " + job);
        
        //Lets lookup our credentials
        AWSCredentials credentials = (AWSCredentials)request.getSession().getAttribute("AWSCred");
        ProviderCredentials outputS3StorageCreds = new org.jets3t.service.security.AWSCredentials(job.getS3OutputAccessKey(), job.getS3OutputSecretKey());
        
        // copy files to S3 storage for processing 
        try {
        
			// get job files from local directory
        	File[] files = jobFileService.listStageInDirectoryFiles(job);
        	if (files.length == 0) {
        		job.setStatus(STATUS_FAILED);
                jobManager.saveJob(job);
        		return generateJSONResponseMAV(false, null, "No input files found. NOT submitting job!");
        	}
        	
			
			// set the output directory for results to be transferred to
			job.setS3OutputBaseKey(job.getFileStorageId() + "/output");
			
			// copy job files to S3 storage service. 
			S3Bucket bucket = new S3Bucket(job.getS3OutputBucket());
			RestS3Service s3Service = new RestS3Service(outputS3StorageCreds);
			for (File file : files) {
				String fileKeyPath = String.format("%1$s/%2$s", job.getS3OutputBaseKey(), file.getName());
				logger.info("Uploading " + fileKeyPath);
				S3Object obj = new S3Object(bucket, file);
				obj.setKey(fileKeyPath);
				s3Service.putObject(bucket, obj);
				logger.info(fileKeyPath + " uploaded to " + bucket.getName() + " S3 bucket");
			}
		} catch (Exception e) {
			//We failed uploading
			logger.error("Job submission failed.", e);
			job.setStatus(STATUS_FAILED);
			jobManager.saveJob(job);
			return generateJSONResponseMAV(false, null, "Failed uploading files to S3");
		}
        
		// launch the ec2 instance
		AmazonEC2 ec2 = new AmazonEC2Client(credentials);
		ec2.setEndpoint(job.getEc2Endpoint());
    	String imageId = job.getEc2AMI();
		RunInstancesRequest instanceRequest = new RunInstancesRequest(imageId, 1, 1);
		
		// user data is passed to the instance on launch. This will be the path to the S3 directory 
		// where the input files are stored. The instance will download the input files and attempt
		// to run the vegl_script.sh that was built in the Script Builder.
		JSONObject encodedUserData = new JSONObject();
		encodedUserData.put("s3OutputBucket", job.getS3OutputBucket());
		encodedUserData.put("s3OutputBaseKeyPath", job.getS3OutputBaseKey().replace("//", "/"));
		encodedUserData.put("s3OutputAccessKey", job.getS3OutputAccessKey());
		encodedUserData.put("s3OutputSecretKey", job.getS3OutputSecretKey());
		
		String base64EncodedUserData = new String(Base64.encodeBase64(encodedUserData.toString().getBytes()));
		instanceRequest.setUserData(base64EncodedUserData);
		instanceRequest.setInstanceType("m1.large");
		instanceRequest.setKeyName("terry-key"); //TODO - bacon - DELETE THIS CODE - it's for testing
		RunInstancesResult result = ec2.runInstances(instanceRequest);
		List<Instance> instances = result.getReservation().getInstances();
		
		//We should get a single item on success
		if (instances.size() == 0) {
			logger.error("Failed to launch instance to run job " + job.getId());
			job.setStatus(STATUS_FAILED);
			jobManager.saveJob(job);
			return generateJSONResponseMAV(false, null, "Failed submitting to EC2");
		}
		
		Instance instance = instances.get(0);
		String instanceId = instance.getInstanceId();
		logger.info("Launched instance: " + instanceId);
			
		// set reference as instanceId for use when killing a job 
		job.setEc2InstanceId(instanceId);
		job.setStatus(STATUS_ACTIVE);
		job.setSubmitDate(new Date());
		jobManager.saveJob(job);
		

        // Save in session for status update request for this job.
		return generateJSONResponseMAV(true, null, "");
    }
    
    /**
     * Creates a new VEGL job initialised with the default configuration values
     * @param email
     * @return
     */
    private VEGLJob createDefaultVEGLJob(HttpSession session) {
    	VEGLJob job = new VEGLJob(); 
    	
    	//Load details from
    	job.setUser((String) session.getAttribute("openID-Email"));
    	job.setEmailAddress((String) session.getAttribute("openID-Email"));
    	job.setSelectionMinEasting((Double) session.getAttribute(ERRDAPController.SESSION_SELECTION_MIN_EASTING));
    	job.setSelectionMaxEasting((Double) session.getAttribute(ERRDAPController.SESSION_SELECTION_MAX_EASTING));
    	job.setSelectionMinNorthing((Double) session.getAttribute(ERRDAPController.SESSION_SELECTION_MIN_NORTHING));
    	job.setSelectionMaxNorthing((Double) session.getAttribute(ERRDAPController.SESSION_SELECTION_MAX_NORTHING));
    	job.setPaddingMinEasting((Double) session.getAttribute(ERRDAPController.SESSION_PADDED_MIN_EASTING));
    	job.setPaddingMaxEasting((Double) session.getAttribute(ERRDAPController.SESSION_PADDED_MAX_EASTING));
    	job.setPaddingMinNorthing((Double) session.getAttribute(ERRDAPController.SESSION_PADDED_MIN_NORTHING));
    	job.setPaddingMaxNorthing((Double) session.getAttribute(ERRDAPController.SESSION_PADDED_MAX_NORTHING));
    	job.setMgaZone((String)session.getAttribute(ERRDAPController.SESSION_MGA_ZONE));
    	job.setCellX((Integer) session.getAttribute(ERRDAPController.SESSION_CELL_X));
    	job.setCellY((Integer) session.getAttribute(ERRDAPController.SESSION_CELL_Y));
    	job.setCellZ((Integer) session.getAttribute(ERRDAPController.SESSION_CELL_Z));
    	job.setInversionDepth((Integer) session.getAttribute(ERRDAPController.SESSION_INVERSION_DEPTH));
    	
    	job.setVmSubsetFilePath(ERRDAP_SUBSET_VM_FILE_PATH);
    	job.setEc2AMI(hostConfigurer.resolvePlaceholder("ami.id"));
    	job.setEc2Endpoint(hostConfigurer.resolvePlaceholder("ec2.endpoint"));
    	job.setS3OutputBucket("vegl-portal");
    	job.setName("VEGL-Job");
    	job.setDescription("");
    	job.setStatus(STATUS_UNSUBMITTED);
    	
    	//We need an ID for storing our job file that won't collide with other storage ID's
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    	Date date = new Date(); //Use the current date to generate an id
    	String fileStorageId = String.format("VEGL-%1$s-%2$s", job.getUser(), sdf.format(date));
    	fileStorageId = fileStorageId.replaceAll("[=/\\, @:]", "_"); //get rid of some obvious nasty characters
        job.setFileStorageId(fileStorageId);
    	
    	return job;
    }
    
    /**
     * Creates a new subset_request.sh script file that will get the subset files for the area selected
     * on the map and save them to the input directory on the Eucalyptus instance. This script will 
     * be executed on launch of the instance prior to the vegl processing script.
     * 
     * @param request The HTTPServlet request
     */
    private boolean createSubsetScriptFile(VEGLJob job, HashMap<String,String> erddapUrlMap) {
    	// create new subset request script file
		File subsetRequestScript = jobFileService.createStageInDirectoryFile(job, "subset_request.sh");
		
		// iterate through the map of subset request URL's
		Set<String> keys = erddapUrlMap.keySet();
		Iterator<String> i = keys.iterator();
		
		try {
			FileWriter out = new FileWriter(subsetRequestScript);
			
			while (i.hasNext()) {
				
				// get the ERDDAP subset request url and layer name
				String fileName = (String)i.next();
				String url = (String)erddapUrlMap.get(fileName);
				
				// add the command for making the subset request
				out.write(String.format("curl -L '%1$s' > \"%2$s\"\n", url, ERRDAP_SUBSET_VM_FILE_PATH));
			}
			
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error creating subset request script", e);
			return false;
		} 
		
		return true;
    }
}
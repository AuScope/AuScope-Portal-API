/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.auscope.portal.server.gridjob.GeodesyJobManager;
import org.auscope.portal.server.gridjob.GeodesySeries;
import org.auscope.portal.server.gridjob.GridAccessController;
import org.auscope.portal.server.gridjob.Util;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.jets3t.service.S3Service;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.ProviderCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.AmazonClientException;
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
 */
/**
 * @author jam19d
 *
 */
@Controller
public class GridSubmitController {

    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private GridAccessController gridAccess;
    @Autowired
    private GeodesyJobManager jobManager;
    @Autowired
    private HttpServiceCaller serviceCaller;
    
    @Autowired
    @Qualifier(value = "propertyConfigurer")
    private PortalPropertyPlaceholderConfigurer hostConfigurer;

    public static final String S3_BUCKET_NAME = "vegl-portal";
    public static final String TABLE_DIR = "tables";
    public static final String PRE_STAGE_IN_TABLE_FILES = "/home/vegl-portal/tables/";
    public static final String FOR_ALL = "Common";

    //Grid File Transfer messages
    private static final String INTERNAL_ERROR= "Job submission failed due to INTERNAL ERROR";
    private static final String TRANSFER_COMPLETE = "Transfer Complete";
    
    // AWS error messages
    private static final String S3_FILE_COPY_ERROR = "Unable to upload file to S3 bucket, upload was aborted.";
    
    /**
     * Returns a JSON object containing a list of the current user's series.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         GeodesySeries objects.
     */
    @RequestMapping("/mySeries.do")
    public ModelAndView mySeries(HttpServletRequest request,
                                 HttpServletResponse response) {

        String user = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
        List<GeodesySeries> series = jobManager.querySeries(user, null, null);

        logger.debug("Returning list of "+series.size()+" series.");
        return new ModelAndView("jsonView", "series", series);
    }

    /**
     * Very simple helper class (bean).
     */
    public class SimpleBean {
        private String value;
        public SimpleBean(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    /**
     * Returns a JSON object containing a list of code.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         SimpleBean objects contain available code.
     */
    @RequestMapping("/getCodeObject.do")
    public ModelAndView getCodeObject(HttpServletRequest request,
                                 HttpServletResponse response) {

        String user = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
        logger.error("Querying code list for "+user);
        List<SimpleBean> code = new ArrayList<SimpleBean>();
        code.add(new SimpleBean("Gamit"));
        code.add(new SimpleBean("Burmese"));
        code.add(new SimpleBean("UBC-GIF"));

        logger.error("Returning list of "+code.size()+" codeObject.");
        return new ModelAndView("jsonView", "code", code);
    }    

    /**
     * Returns a JSON object containing a list of jobTypes.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         SimpleBean objects contain available jobTypes.
     */
    @RequestMapping("/listJobTypes.do")
    public ModelAndView listJobTypes(HttpServletRequest request,
                                 HttpServletResponse response) {

        String user = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
        logger.debug("Querying job types list for "+user);
        List<SimpleBean> jobType = new ArrayList<SimpleBean>();
        jobType.add(new SimpleBean("multi"));
        jobType.add(new SimpleBean("single"));

        logger.debug("Returning list of "+jobType.size()+" jobType.");
        return new ModelAndView("jsonView", "jobType", jobType);
    }    

    /**
     * Returns a JSON object containing a list of jobTypes.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         SimpleBean objects contain available jobTypes.
     */
    @RequestMapping("/getGetArguments.do")
    public ModelAndView getGetArguments(HttpServletRequest request,
                                 HttpServletResponse response) {

        String user = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
        logger.debug("Querying param for "+user);
        List<SimpleBean> params = new ArrayList<SimpleBean>();
        params.add(new SimpleBean("enter args ..."));

        logger.debug("Returning list of "+params.size()+" params.");
        return new ModelAndView("jsonView", "paramLines", params);
    }
    

    /**
     * Returns a JSON object containing an array of sites that have the code.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a sites attribute which is an array of
     *         sites on the grid that have an installation of the selected code.
     */
    @RequestMapping("/listSites.do")    
    public ModelAndView listSites(HttpServletRequest request,
                                  HttpServletResponse response) {
    	String myCode = request.getParameter("code");
        logger.debug("Retrieving sites with "+myCode+" installations.");
        List<SimpleBean> sites = new ArrayList<SimpleBean>();
        sites.add(new SimpleBean("iVEC"));
        sites.add(new SimpleBean("eRSA"));

        logger.debug("Returning list of "+sites.size()+" sites.");
        return new ModelAndView("jsonView", "sites", sites);
    }
    
    /**
     * Returns a JSON object containing an array of job manager queues at
     * the specified site.
     *
     * @param request The servlet request including a site parameter
     * @param response The servlet response
     *
     * @return A JSON object with a queues attribute which is an array of
     *         job queues available at requested site.
     */
    @RequestMapping("/listSiteQueues.do")    
    public ModelAndView listSiteQueues(HttpServletRequest request,
                                       HttpServletResponse response) {

        List<SimpleBean> queues = new ArrayList<SimpleBean>();
        queues.add(new SimpleBean("normal"));

        logger.debug("Returning list of "+queues.size()+" queue names.");
        return new ModelAndView("jsonView", "queues", queues);
    }

    /**
     * Returns a JSON object containing an array of versions at
     * the specified site.
     *
     * @param request The servlet request including a site parameter
     * @param response The servlet response
     *
     * @return A JSON object with a versions attribute which is an array of
     *         versions installed at requested site.
     */
    @RequestMapping("/listSiteVersions.do")    
    public ModelAndView listSiteVersions(HttpServletRequest request,
                                         HttpServletResponse response) {

        List<SimpleBean> versions = new ArrayList<SimpleBean>();
        versions.add(new SimpleBean("10.35"));

        logger.debug("Returning list of "+versions.size()+" versions.");
        return new ModelAndView("jsonView", "versions", versions);
    }

    /**
     * Returns a JSON object containing a populated GeodesyJob object.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a data attribute containing a populated
     *         GeodesyJob object and a success attribute.
     */
    @RequestMapping("/getJobObject.do")    
    public ModelAndView getJobObject(HttpServletRequest request,
                                     HttpServletResponse response) {

        GeodesyJob job = prepareModel(request);

        logger.debug("Returning job.");
        ModelAndView result = new ModelAndView("jsonView");
        
        if(job == null){
            logger.error("Job setup failure.");
            result.addObject("success", false);
        }else{
            logger.debug("Job setup success.");
            result.addObject("data", job);
            result.addObject("success", true);
        }

        return result;
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
    public ModelAndView listJobFiles(HttpServletRequest request,
                                     HttpServletResponse response) {

        String jobInputDir = (String) request.getSession()
            .getAttribute("localJobInputDir");
        String jobType = (String) request.getParameter("jobType");
        logger.debug("Inside listJobFiles.do");
        List<FileInformation> files = new ArrayList<FileInformation>();

        if (jobInputDir != null) {
        	if(jobType != null ){
	            String filePath = jobInputDir+GridSubmitController.TABLE_DIR;
	            File dir = new File(filePath+File.separator);
	            addFileNamesOfDirectory(files, dir, filePath);
        	}
        }

        logger.debug("Returning list of "+files.size()+" files.");
        return new ModelAndView("jsonView", "files", files);
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
     */
    @RequestMapping("/downloadInputFile.do")
    public ModelAndView downloadFile(HttpServletRequest request,
                                     HttpServletResponse response) {

        String dirPathStr = request.getParameter("dirPath");
        String fileName = request.getParameter("filename");
        String errorString = null;
        
        if (dirPathStr != null && fileName != null) {
            logger.debug("Downloading: "+dirPathStr+File.separator+fileName+".");
            File f = new File(dirPathStr+File.separator+fileName);
            if (!f.canRead()) {
                logger.error("File "+f.getPath()+" not readable!");
                errorString = new String("File could not be read.");
            } else {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\""+fileName+"\"");

                try {
                    byte[] buffer = new byte[16384];
                    int count = 0;
                    OutputStream out = response.getOutputStream();
                    FileInputStream fin = new FileInputStream(f);
                    while ((count = fin.read(buffer)) != -1) {
                        out.write(buffer, 0, count);
                    }
                    out.flush();
                    return null;

                } catch (IOException e) {
                    errorString = new String("Could not send file: " +
                            e.getMessage());
                    logger.error(errorString);
                }
            }
        }

        // We only end up here in case of an error so return a suitable message
        if (errorString == null) {
            if (dirPathStr == null) {
                errorString = new String("Invalid input job file path specified!");
                logger.error(errorString);
            } else if (fileName == null) {
                errorString = new String("No filename provided!");
                logger.error(errorString);
            } else {
                // should never get here
                errorString = new String("Something went wrong.");
                logger.error(errorString);
            }
        }
        return new ModelAndView("jsonView", "error", errorString);
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
                                   HttpServletResponse response) {
    	logger.debug("Entering upload.do ");
        String jobInputDir = (String) request.getSession()
            .getAttribute("localJobInputDir");
        logger.debug("jobInputDir: + " + jobInputDir);
        
        MultipartHttpServletRequest mfReq =
            (MultipartHttpServletRequest) request; 
        String subJobId = (String) mfReq.getParameter("subJobId");
        
        boolean success = true;
        String error = null;
        FileInformation fileInfo = null;
        String destinationPath = null;

        MultipartFile f = mfReq.getFile("file");
        
        if (f != null) {        	
            
           	logger.debug("uploading file for single job ");
           	subJobId = GridSubmitController.FOR_ALL;
           	destinationPath = jobInputDir+GridSubmitController.TABLE_DIR+File.separator;
            
            if (jobInputDir != null && success) {

                    logger.info("Saving uploaded file "+f.getOriginalFilename());
                    //TO-DO allow to upload on tables directory as well. GUI functions to be added.
                    File destination = new File(destinationPath+f.getOriginalFilename());
                    if (destination.exists()) {
                        logger.debug("Will overwrite existing file.");
                    }
                    try {
                        f.transferTo(destination);
                    } catch (IOException e) {
                        logger.error("Could not move file: "+e.getMessage());
                        success = false;
                        error = new String("Could not process file.");
                    }
                    fileInfo = new FileInformation(
                            f.getOriginalFilename(), f.getSize());

            } else {
                logger.error("Input directory not found or couldn't be created in current session!");
                success = false;
                error = new String("Internal error. Please reload the page.");
            }        	
        }else{
            logger.error("No file parameter provided.");
            success = false;
            error = new String("Invalid request.");
        }        


        // We cannot use jsonView here since this is a file upload request and
        // ExtJS uses a hidden iframe which receives the response.
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            PrintWriter pw = response.getWriter();
            pw.print("{success:'"+success+"'");
            if (error != null) {
                pw.print(",error:'"+error+"'");
            }
            if (fileInfo != null) {
                pw.print(",name:'"+fileInfo.getName()+"',size:"+fileInfo.getSize()+",parentPath:'"+destinationPath+"',subJob:'"+subJobId+"'");
            }
            pw.print("}");
            pw.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
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
    public ModelAndView deleteFiles(HttpServletRequest request,
                                    HttpServletResponse response) {

        String jobInputDir = (String) request.getSession()
            .getAttribute("localJobInputDir");
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success;
        
        if (jobInputDir != null) {
            success = true;
            String filesPrm = request.getParameter("files");
            String subJobPrm = (String) request.getParameter("subJobId");
            
            logger.debug("Request to delete "+filesPrm);
            String[] files = (String[]) JSONArray.toArray(
                    JSONArray.fromObject(filesPrm), String.class);
            String[] subJobId = (String[]) JSONArray.toArray(
                    JSONArray.fromObject(subJobPrm), String.class);
            int i =0;
            for (String filename: files) {
            	String fullFilename = null;
            	
            	if(subJobId[i] == null || subJobId[i].equals(""))
            		subJobId[i] = GridSubmitController.FOR_ALL;
            	
            	if(subJobId[i].equals(GridSubmitController.FOR_ALL)){
            		logger.debug("Deleting "+filename+" for subJob"+subJobId[i]);
               		fullFilename = jobInputDir+GridSubmitController.TABLE_DIR
                		                          +File.separator+filename;
            	}else{
            		logger.debug("Deleting "+filename+" for subJob"+subJobId[i]);
                	fullFilename = jobInputDir+subJobId[i]+File.separator
                		               +GridSubmitController.TABLE_DIR+File.separator+filename;
            	}

                File f = new File(fullFilename);
                if (f.exists() && f.isFile()) {
                    logger.debug("Deleting "+f.getPath());
                    boolean lsuccess = f.delete();
                    if (!lsuccess) {
                        logger.warn("Unable to delete "+f.getPath());
                        success = false;
                    }
                } else {
                    logger.warn(f.getPath()+" does not exist or is not a file!");
                }
                i++;
            }
        } else {
            success = false;
        }

        mav.addObject("success", success);
        return mav;
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
    public ModelAndView getJobStatus(HttpServletRequest request,
                                    HttpServletResponse response) {

        ModelAndView mav = new ModelAndView("jsonView");
        GridTransferStatus jobStatus = (GridTransferStatus)request.getSession().getAttribute("gridStatus");
        
        if (jobStatus != null) {
        	mav.addObject("data", jobStatus.currentStatusMsg);
        	mav.addObject("jobStatus", jobStatus.jobSubmissionStatus);
        } else {
        	mav.addObject("data", "Cloud File Transfer failed.");
        	mav.addObject("jobStatus", JobSubmissionStatus.Failed);
        }

        mav.addObject("success", true);
        return mav;
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
    public ModelAndView cancelSubmission(HttpServletRequest request,
                                         HttpServletResponse response) {

        String jobInputDir = (String) request.getSession()
            .getAttribute("localJobInputDir");

        if (jobInputDir != null) {
            logger.debug("Deleting temporary job files.");
            File jobDir = new File(jobInputDir);
            Util.deleteFilesRecursive(jobDir);
            request.getSession().removeAttribute("localJobInputDir");
        }

        return null;
    }

    /**
     * Given a credential, this function generates a directory name based on the distinguished name of the credential
     * @param credential
     * @return
     */
    public static String generateCertDNDirectory(Object credential) throws GSSException {
    	GSSCredential cred = (GSSCredential)credential;
        return cred.getName().toString().replaceAll("=", "_").replaceAll("/", "_").replaceAll(" ", "_").substring(1);//certDN.replaceAll("=", "_").replaceAll(" ", "_").replaceAll(",", "_");
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
                                  GeodesyJob job) {

    	boolean success = true;
    	GeodesySeries series = null;
    	final String user = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
    	String newSeriesName = request.getParameter("seriesName");
    	String seriesIdStr = request.getParameter("seriesId");
    	ModelAndView mav = new ModelAndView("jsonView");
    	
    	//Used to store Job Submission status, because there will be another request checking this.
		GridTransferStatus gridStatus = new GridTransferStatus();
		gridStatus.jobSubmissionStatus = JobSubmissionStatus.Pending;
		request.getSession().setAttribute("gridStatus", gridStatus);
    	
    	// if seriesName parameter was provided then we create a new series
        // otherwise seriesId contains the id of the series to use.
        if (newSeriesName != null && newSeriesName != "") {
            String newSeriesDesc = request.getParameter("seriesDesc");

            logger.debug("Creating new series '"+newSeriesName+"'.");
            series = new GeodesySeries();
            series.setUser(user);
            series.setName(newSeriesName);
            if (newSeriesDesc != null) {
                series.setDescription(newSeriesDesc);
            }
            jobManager.saveSeries(series);
            // Note that we can now access the series' new ID

        } else if (seriesIdStr != null && seriesIdStr != "") {
            try {
                int seriesId = Integer.parseInt(seriesIdStr);
                series = jobManager.getSeriesById(seriesId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing series ID!");
            }
        }
        
        if (series == null) {
            success = false;
            final String msg = "No valid series found. NOT submitting job!";
            logger.error(msg);
            gridStatus.currentStatusMsg = msg;
            gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;

        } else {
        	
        	job.setSeriesId(series.getId());
            job.setJobType(job.getJobType().replace(",", ""));
            job.setScriptFile(job.getScriptFile().replace(",", ""));
            JSONArray args = JSONArray.fromObject(request.getParameter("arguments"));
			logger.info("Args count: " + job.getArguments().length
					+ " | Args in Json : " + args.toArray().length);
            job.setArguments((String[])args.toArray(new String [args.toArray().length]));
            job.setEmailAddress(user);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String dateFmt = sdf.format(new Date());
            
            logger.info("Submitting job with name " + job.getName());
            AWSCredentials credentials = (AWSCredentials)request.getSession().getAttribute("AWSCred");
            ProviderCredentials provCreds = new org.jets3t.service.security.AWSCredentials(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey()); 
            String keyPath = new String();
            
            // copy files to S3 storage for processing 
	        try {
	        	
				// get job files from local directory
				String localJobInputDir = (String) request.getSession().getAttribute("localJobInputDir");
				logger.info("uploading files from " + localJobInputDir + GridSubmitController.TABLE_DIR);
				File dir = new File(localJobInputDir + GridSubmitController.TABLE_DIR); 
				File[] files = dir.listFiles(); 
				
				// check if the vegl script and erddap request exist for the job. If not it can't be submitted
				if (files.length == 0)
				{
					mav.addObject("msg", "Job must have a vegl_script and subset_request script file in order to submit");
					return mav;
				}
				
				// create the base S3 object storage key path. The final key will be this with
				// the filename appended.
				keyPath = user + "-" + job.getName() + "-" + dateFmt;
				// set the output directory for results to be transferred to
				job.setOutputDir(keyPath + "/output");
				
				// copy job files to S3 storage service. 
				S3Bucket bucket = new S3Bucket(S3_BUCKET_NAME);
				S3Service s3Service = new RestS3Service(provCreds);
				
				for (File file : files){
					logger.info("Uploading " + keyPath + "/" + file.getName());
					S3Object obj = new S3Object(bucket, file);
					obj.setKey(keyPath + "/" + file.getName());
					s3Service.putObject(bucket, obj);
					logger.info(keyPath + "/" + file.getName() +" uploaded to " + S3_BUCKET_NAME + "S3 bucket");
				}
				
				gridStatus.currentStatusMsg = GridSubmitController.TRANSFER_COMPLETE;
				
			} catch (AmazonClientException amazonClientException) {
	        	logger.error(GridSubmitController.S3_FILE_COPY_ERROR);
	        	gridStatus.currentStatusMsg = GridSubmitController.S3_FILE_COPY_ERROR;
	            gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
	        	amazonClientException.printStackTrace();
	        	success = false;
	        } catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Job submission failed.");
				e.printStackTrace();
				success = false;
			}
	        
			// launch the ec2 instance
			AmazonEC2 ec2 = new AmazonEC2Client(credentials);
			ec2.setEndpoint(hostConfigurer.resolvePlaceholder("ec2.endpoint"));
        	String imageId = hostConfigurer.resolvePlaceholder("emi.id");
			RunInstancesRequest instanceRequest = new RunInstancesRequest(imageId, 1, 1);
			
			// user data is passed to the instance on launch. This will be the path to the S3 directory 
			// where the input files are stored. The instance will download the input files and attempt
			// to run the vegl_script.sh that was built in the Script Builder.
			String encodedUserData = new String(Base64.encodeBase64(keyPath.toString().getBytes()));
			instanceRequest.setUserData(encodedUserData);
			instanceRequest.setInstanceType("m1.large");
			RunInstancesResult result = ec2.runInstances(instanceRequest);
			List<Instance> instances = result.getReservation().getInstances();
			
			if (instances.size() > 0)
			{
				Instance instance = instances.get(0);
				String instanceId = instance.getInstanceId();
   				logger.info("Launched instance: " + instanceId);
   				success = true;
   				
   				// set reference as instanceId for use when killing a job 
   				job.setReference(instanceId);
			}
			else
			{
				logger.error("Failed to launch instance to run job " + job.getId());
				success = false;
			}
			
   			if (success) {
                job.setSubmitDate(dateFmt);
                job.setStatus("Active");
                jobSupplementInfo(job);
                jobManager.saveJob(job);
                gridStatus.jobSubmissionStatus = JobSubmissionStatus.Running;
	        } else {
	        	gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
   				gridStatus.currentStatusMsg = GridSubmitController.INTERNAL_ERROR;
	        }
	        
	        request.getSession().removeAttribute("jobInputDir");
            request.getSession().removeAttribute("localJobInputDir");
        }
        
        // Save in session for status update request for this job.
        request.getSession().setAttribute("gridStatus", gridStatus);
        
        mav.addObject("success", success);
        
        return mav;
    }
    
    /**
     * Method that store extra job info required for registering into Geonetwork
     * @param job
     */
    private void jobSupplementInfo(GeodesyJob job){
    	StringBuilder detail = new StringBuilder();
    	detail.append("ExecutedCode: "+job.getCode()+"\n");
    	detail.append("Version: "+job.getVersion()+"\n");
    	detail.append("Site: "+job.getSite()+"\n");
    	detail.append("QueueOnSite: "+job.getQueue()+"\n");
    	detail.append("Walltime: "+job.getMaxWallTime()+"\n");
    	detail.append("MaxMemory: "+job.getMaxMemory()+"\n");
    	detail.append("NumberOfCPUs: "+job.getCpuCount()+"\n");
    	detail.append("JobType: "+job.getJobType()+"\n");
    	detail.append("Arguments: "+job.getArguments()[0]);
    	
    	//We need to store this for when register
    	job.setExtraJobDetails(detail.toString());
    }
    
    /**
     * Creates a new Job object with predefined values for some fields.
     *
     * @param request The servlet request containing a session object
     *
     * @return The new job object.
     */
    private GeodesyJob prepareModel(HttpServletRequest request) {
        final String user = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
        final String maxWallTime = "60"; // in minutes
        final String maxMemory = "2048"; // in MB
        final String stdInput = "";
        final String stdOutput = "stdOutput.txt";
        final String stdError = "stdError.txt";
        final String[] arguments = new String[0];
        final String[] inTransfers = new String[0];
        final String[] outTransfers = new String[0];
        String name = "VEGLJob";
        String site = "iVEC";
        Integer cpuCount = 1;
        String version = "10.35";
        String queue = "normal";
        String description = "";
        String scriptFile = "";
      
        //Create local stageIn directory.
        boolean success = createLocalDir(request);
        if(!success){
        	logger.error("Setting up local StageIn directory failed.");
        	return null;
        }        

        logger.debug("Creating new GeodesyJob instance");
        GeodesyJob job = new GeodesyJob(site, name, version, arguments, queue,
                maxWallTime, maxMemory, cpuCount, inTransfers, outTransfers,
                user, stdInput, stdOutput, stdError);
        
        // create subset request script file
        success = createSubsetScriptFile(request);
        
        if (!success){
        	logger.error("No subset area has been selected.");
        	return null;
        }
        
        // Check if the ScriptBuilder was used. If so, there is a file in the
        // system temp directory which needs to be staged in. 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateFmt = sdf.format(new Date());
        String jobID = user + "-" + dateFmt + File.separator;
        String jobInputDir = gridAccess.getlocalGridStageInDir() + jobID;
        String newScript = (String) request.getSession().getAttribute("scriptFile");
	    
        if (newScript != null) {
	        logger.debug("Adding "+newScript+" to stage-in directory");
	        File tmpScriptFile = new File(System.getProperty("java.io.tmpdir") + File.separator+newScript+".sh");
	        File newScriptFile = new File(jobInputDir+GridSubmitController.TABLE_DIR, tmpScriptFile.getName());
	        success = Util.moveFile(tmpScriptFile, newScriptFile);
	        
	        if (!success){
	            logger.error("Could not move "+newScript+" to stage-in!");
	        }
	    }

        job.setScriptFile(scriptFile);
        job.setDescription(description);

        return job;
    }
    
    /**
     * Creates a new subset_request.sh script file that will get the subset files for the area selected
     * on the map and save them to the input directory on the Eucalyptus instance. This script will 
     * be executed on launch of the instance prior to the vegl processing script.
     * 
     * @param request The HTTPServlet request
     */
    private boolean createSubsetScriptFile(HttpServletRequest request) {
    	
    	// check if subset areas have been selected
    	HashMap<String,String> erddapUrlMap = (HashMap)request.getSession().getAttribute("erddapUrlMap");
    	
    	if (erddapUrlMap != null) {
    		// create new subset request script file
        	String localJobInputDir = (String) request.getSession().getAttribute("localJobInputDir");
    		File subsetRequestScript = new File(localJobInputDir+GridSubmitController.TABLE_DIR+File.separator+"subset_request.sh");
    		
    		// iterate through the map of subset request URL's
    		Set<String> keys = erddapUrlMap.keySet();
    		Iterator<String> i = keys.iterator();
    		
    		try {
    			FileWriter out = new FileWriter(subsetRequestScript);
    			out.write("cd /tmp/input\n");
    			
    			while (i.hasNext()) {
    				
    				// get the ERDDAP subset request url and layer name
    				String fileName = (String)i.next();
    				String url = (String)erddapUrlMap.get(fileName);
    				
    				// add the command for making the subset request
    				out.write("wget '" + url +"'\n");
    			}
    			
    			out.close();
    			
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			logger.error("Error creating subset request script");
    			e.printStackTrace();
    			return false;
    		} 
    		
    		// clear the subset URL's from the session so they aren't created again if the 
            // user submits another job
            request.getSession().setAttribute("erddapUrlMap", new HashMap<String,String>());

    	} else {
    		logger.warn("No subset area selected");
    	}
    	
		return true;
    }
    
	/** 
     * Create stageIn directories on portal host, so user can upload files easy.
     *
     */
	private boolean createLocalDir(HttpServletRequest request) {
		
		GridTransferStatus status = new GridTransferStatus();
		final String user = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
		
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateFmt = sdf.format(new Date());
        String jobID = user + "-" + dateFmt + File.separator;
        String jobInputDir = gridAccess.getlocalGridStageInDir() + jobID;
        
        boolean success = (new File(jobInputDir)).mkdir();
        
        // create stageIn directory
        success = (new File(jobInputDir+GridSubmitController.TABLE_DIR+File.separator)).mkdir();
        
        if (!success) {
            logger.error("Could not create local stageIn directories ");
        }
        // Save in session to use it when submitting job
        request.getSession().setAttribute("localJobInputDir", jobInputDir);
        // Save in session for status update request for this job.
        request.getSession().setAttribute("gridStatus", status);
        return success;
	}

	/**
	 * Funtion th
	 * @param files
	 * @param dir
	 * @param subJob
	 */
	private void addFileNamesOfDirectory(List files, File dir, String filePath){
        String fileNames[] = dir.list();
        
        for (int i=0; i<fileNames.length; i++) {
            File f = new File(dir, fileNames[i]);
            FileInformation fileInfo = new FileInformation(fileNames[i], f.length());
            fileInfo.setParentPath(filePath);
            logger.debug("File path is:"+filePath);
            files.add(fileInfo);     
        }
	}

	/**
	 * Simple object to hold Grid file transfer status.
	 * @author jam19d
	 *
	 */
	class GridTransferStatus {
		
		public int numFileCopied = 0;
		public String file = "";
		public String gridFullURL = "";
		public String gridServer = "";
		public String currentStatusMsg = "";
		public JobSubmissionStatus jobSubmissionStatus = JobSubmissionStatus.Pending;				
	}
	

	/**
	 * Enum to indicate over all job submission status.
	 */
	public enum JobSubmissionStatus{Pending,Running,Done,Failed }
}
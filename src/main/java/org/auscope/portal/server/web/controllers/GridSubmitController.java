/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.auscope.portal.server.gridjob.GeodesyJobManager;
import org.auscope.portal.server.gridjob.GeodesySeries;
import org.auscope.portal.server.gridjob.GridAccessController;
import org.auscope.portal.server.gridjob.ScriptParser;
import org.auscope.portal.server.gridjob.Util;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.wsrf.utils.FaultHelper;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;


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

    public static final String TABLE_DIR = "tables";
    public static final String PRE_STAGE_IN_TABLE_FILES = "/home/grid-auscope/tables/";
    public static final String IVEC_MIRROR_URL = "http://files.ivec.org/geodesy/";
    public static final String FOR_ALL = "Common";

    //Grid File Transfer messages
    private static final String FILE_COPY_ERROR = "Job submission failed due to file transfer Error.";
    private static final String INTERNAL_ERROR= "Job submission failed due to INTERNAL ERROR";
    private static final String GRID_LINK = "Job submission failed due to GRID Link Error";
    private static final String TRANSFER_COMPLETE = "Transfer Complete";
    private static final String CREDENTIAL_ERROR = "Job submission failed due to Invalid Credential Error";
    
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

        String user = request.getRemoteUser();
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

        String user = request.getRemoteUser();
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

        String user = request.getRemoteUser();
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

        String user = request.getRemoteUser();
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
        String[] particleSites = gridAccess.
                retrieveSitesWithSoftwareAndVersion(myCode, "");

        List<SimpleBean> sites = new ArrayList<SimpleBean>();
        for (int i=0; i<particleSites.length; i++) {
            sites.add(new SimpleBean(particleSites[i]));
            logger.debug("Site name: "+particleSites[i]);
        }

        logger.debug("Returning list of "+particleSites.length+" sites.");
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

        String site = request.getParameter("site");
        List<SimpleBean> queues = new ArrayList<SimpleBean>();

        if (site != null) {
            logger.debug("Retrieving queue names at "+site);

            String[] siteQueues = gridAccess.
                    retrieveQueueNamesAtSite(site);

            for (int i=0; i<siteQueues.length; i++) {
                queues.add(new SimpleBean(siteQueues[i]));
            }
        } else {
            logger.warn("No site specified!");
        }

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

        String site = request.getParameter("site");
        String myCode = request.getParameter("code");
        List<SimpleBean> versions = new ArrayList<SimpleBean>();

        if (site != null || myCode != null ) {
            logger.debug("Retrieving versions at "+site);

            String[] siteVersions = gridAccess.
                    retrieveCodeVersionsAtSite(site, myCode);

            for (int i=0; i<siteVersions.length; i++) {
                versions.add(new SimpleBean(siteVersions[i]));
            }
        } else {
            logger.warn("No site or code are specified!");
        }

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

        GridTransferStatus status = (GridTransferStatus)request.getSession().getAttribute("gridStatus");
        if(status == null || job == null){
            logger.error("Job setup failure.");
            result.addObject("success", false);
        }else{
        	if(status.jobSubmissionStatus == JobSubmissionStatus.Failed){
                logger.error("Job setup failure.");
                result.addObject("success", false);        		
        	}
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
        List files = new ArrayList<FileInformation>();

        if (jobInputDir != null) {
        	if(jobType != null ){
	            String filePath = jobInputDir+GridSubmitController.TABLE_DIR;
	            File dir = new File(filePath+File.separator);
	            addFileNamesOfDirectory(files, dir, GridSubmitController.FOR_ALL, filePath);
	            logger.debug("Inside listJobFiles.do multi job");
	            boolean subJobExist = true;
	            int i = 0;
	            while(subJobExist){
	            	String subDirID = "subJob_"+i;
	            	File subDir = new File(jobInputDir+subDirID+File.separator);
	            	if(subDir.exists()){
	            		//list tables dir
	            		filePath = jobInputDir+subDirID+File.separator+GridSubmitController.TABLE_DIR;
	            		subDir = new File(filePath+File.separator);
	            		if(subDir.exists()){
	            			addFileNamesOfDirectory(files, dir, subDirID, filePath);
	            		}
	            	}
	            	else
	            	{
	            		//exit loop
	            		subJobExist = false;
	            	}	            	
	            	i++;
	            }
	             
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
        String jobType = (String) mfReq.getParameter("jobType");
        String subJobId = (String) mfReq.getParameter("subJobId");
        
        boolean success = true;
        String error = null;
        FileInformation fileInfo = null;
        String destinationPath = null;

        MultipartFile f = mfReq.getFile("file");
        
        if (f != null) {        	
            //check if multiJob or not
            if(jobType.equals("single") || subJobId.equals(GridSubmitController.FOR_ALL)){
            	logger.debug("uploading file for single job ");
            	subJobId = GridSubmitController.FOR_ALL;
            	destinationPath = jobInputDir+GridSubmitController.TABLE_DIR+File.separator;
            }
            else{
            	logger.debug("uploading file for multi job ");
                
                String subJobInputDir = jobInputDir+subJobId.trim()+File.separator;
            	if(createLocalSubJobDir(request, subJobInputDir, GridSubmitController.TABLE_DIR, subJobId.trim())){
           			destinationPath = subJobInputDir+GridSubmitController.TABLE_DIR+File.separator;
            	}
            	else{

                    logger.error("Could not create local subJob Directories.");
                    success = false;
                    error = new String("Could not create local subJob Directories.");        		
            	}        	
            }
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
        	mav.addObject("data", "Grid File Transfere failed.");
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

        logger.debug("Job details:\n"+job.toString());

        GeodesySeries series = null;
        boolean success = true;
        final String user = request.getRemoteUser();
        String jobInputDir = (String) request.getSession()
            .getAttribute("jobInputDir");
        String newSeriesName = request.getParameter("seriesName");
        String seriesIdStr = request.getParameter("seriesId");
        ModelAndView mav = new ModelAndView("jsonView");
        Object credential = request.getSession().getAttribute("userCred");
        String localJobInputDir = (String) request.getSession().getAttribute("localJobInputDir");
    	
        //Used to store Job Submission status, because there will be another request checking this.
		GridTransferStatus gridStatus = new GridTransferStatus();
		
        if (credential == null) {
            logger.error(GridSubmitController.CREDENTIAL_ERROR);
            gridStatus.currentStatusMsg = GridSubmitController.CREDENTIAL_ERROR;
            gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
            
            // Save in session for status update request for this job.
            request.getSession().setAttribute("gridStatus", gridStatus);
            mav.addObject("success", false);
            return mav;
        }

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
    		    		
    		if(gridStatus.jobSubmissionStatus != JobSubmissionStatus.Failed){
    			
                job.setSeriesId(series.getId());
                job.setJobType(job.getJobType().replace(",", ""));
                JSONArray args = JSONArray.fromObject(request.getParameter("arguments"));
				logger.info("Args count: " + job.getArguments().length
						+ " | Args in Json : " + args.toArray().length);
                job.setArguments((String[])args.toArray(new String [args.toArray().length]));
                
                // Create a new directory for the output files of this job
                //String certDN = (String)request.getSession().getAttribute("certDN");
                String certDN_DIR = "";
                try {
                    certDN_DIR = generateCertDNDirectory(credential);
                    logger.debug("certDN_DIR: "+certDN_DIR);
        		} catch (GSSException e) {
                    logger.error(FaultHelper.getMessage(e));
                }
        		
                success = createGridDir(request, gridAccess.getGridFtpStageOutDir()+certDN_DIR+File.separator);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String dateFmt = sdf.format(new Date());
                String jobID = user + "-" + job.getName() + "-" + dateFmt +
                    File.separator;
                String jobOutputDir = gridAccess.getGridFtpStageOutDir()+certDN_DIR+File.separator+jobID;
                
                // Add grid stage-in directory and local stage-in directory.
                String stageInURL = gridAccess.getGridFtpServer()+jobInputDir;
                logger.debug("stagInURL: "+stageInURL);
                                
                if(job.getJobType().equals("single")){
            		
                    String localStageInURL = gridAccess.getLocalGridFtpServer()+
                    (String) request.getSession().getAttribute("localJobInputDir");
                    job.setInTransfers(new String[]{stageInURL,localStageInURL});
                    
                    logger.debug("localStagInURL: "+localStageInURL);                	
                }
                else{
                	//Here see if date range is used and not parameter list from the gui for multi job
            		String strDateFrom = (String)request.getSession().getAttribute("dateFrom");
            		String strDateTo = (String)request.getSession().getAttribute("dateTo");
                	if(strDateFrom != null && strDateTo != null)
                	{	
                		String localStageInURL = gridAccess.getLocalGridFtpServer()+localJobInputDir;
                        job.setInTransfers(new String[]{localStageInURL});
                        gridStatus = (GridTransferStatus)request.getSession().getAttribute("gridStatus");
                	}
                	else
                	{
                		String localStageInURL = gridAccess.getLocalGridFtpServer()+localJobInputDir;
                		job.setInTransfers(new String[]{stageInURL, localStageInURL});
                	}

                	//create the base directory for multi job, because this fails on stage out.
                	success = createGridDir(request, jobOutputDir);
                    
                    //Add subJobStageIns
                	Hashtable localSubJobDir = (Hashtable) request.getSession().getAttribute("localSubJobDir");
                	if(localSubJobDir == null)
                		localSubJobDir = new Hashtable();
                	job.setSubJobStageIn(localSubJobDir);
                	request.getSession().removeAttribute("localSubJobDir");
                	logger.debug("localSubJobDir size: "+localSubJobDir.size());
                	
                	//Add grigSubJobStageIns
                	Hashtable gridSubJobStageInDir = (Hashtable) request.getSession().getAttribute("subJobStageInDir");
                	if(gridSubJobStageInDir == null)
                		gridSubJobStageInDir = new Hashtable();
                	job.setGridSubJobStageIn(gridSubJobStageInDir);
                	request.getSession().removeAttribute("subJobStageInDir");
                	logger.debug("gridSubJobStageInDir size: "+gridSubJobStageInDir.size());
                }
                
                String submitEPR = null;
                job.setEmailAddress(user);
                job.setOutputDir(jobOutputDir);
                job.setOutTransfers(new String[]
                        { gridAccess.getGridFtpServer() + jobOutputDir });

                logger.info("Submitting job with name " + job.getName() +
                        " to " + job.getSite());
                // ACTION!
                if(success && gridStatus.jobSubmissionStatus != JobSubmissionStatus.Failed)
                	submitEPR = gridAccess.submitJob(job, credential);

                if (submitEPR == null) {
                    success = false;
       				gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
       				gridStatus.currentStatusMsg = GridSubmitController.INTERNAL_ERROR; 
                } else {
                    logger.info("SUCCESS! EPR: "+submitEPR);
                    String status = gridAccess.retrieveJobStatus(
                            submitEPR, credential);
                    job.setReference(submitEPR);
                    job.setStatus(status);
                    job.setSubmitDate(dateFmt);
                    jobSupplementInfo(job);
                    jobManager.saveJob(job);
                    request.getSession().removeAttribute("jobInputDir");
                    request.getSession().removeAttribute("localJobInputDir");
                    
        			//This means job submission to the grid done.
       				gridStatus.jobSubmissionStatus = JobSubmissionStatus.Done;
       				gridStatus.currentStatusMsg = GridSubmitController.TRANSFER_COMPLETE; 
                }                   			
    		}else{
    			success = false;
    			logger.error(GridSubmitController.FILE_COPY_ERROR);
                gridStatus.currentStatusMsg = GridSubmitController.FILE_COPY_ERROR;
                gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
    			mav.addObject("error", GridSubmitController.FILE_COPY_ERROR);
    		}
        }
        // Save in session for status update request for this job.
        request.getSession().setAttribute("gridStatus", gridStatus);
        
        //reset the date range for next job
        request.getSession().removeAttribute("dateTo");
        request.getSession().removeAttribute("dateFrom");
        
        mav.addObject("success", success);

        return mav;
    }
    
    /**
     * create a subjob for each day in the date range, using first parameter as a template.
     * Then transfer all the rinex files for each subjob. 
     * @param strDateFrom
     * @param strDateTo
     * @param param
     * @return array of strings
     */
    
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
        final String user = request.getRemoteUser();
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
        String version = "";
        String queue = "";
        String description = "";
        String scriptFile = "";

        // Set a default version and queue
        String[] allVersions = gridAccess.retrieveCodeVersionsAtSite(
                site, GeodesyJob.CODE_NAME);
        if (allVersions.length > 0)
            version = allVersions[0];

        String[] allQueues = gridAccess.retrieveQueueNamesAtSite(site);
        if (allQueues.length > 0)
            queue = allQueues[0];

        // Create a new directory to put all files for this job into.
        // This directory will always be the first stageIn directive.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateFmt = sdf.format(new Date());
        String jobID = user + "-" + dateFmt + File.separator;
        String jobInputDir = gridAccess.getGridFtpStageInDir() + jobID;
        
        boolean success = createGridDir(request, jobInputDir);
        if(!success){
        	logger.error("Setting up Grid StageIn directory failed.");
        	return null;
        }
       
        //Create local stageIn directory.
        success = createLocalDir(request);
        if(!success){
        	logger.error("Setting up local StageIn directory failed.");
        	return null;
        }
        
        // Save in session to use it when submitting job
        request.getSession().setAttribute("jobInputDir", jobInputDir);
        
        // save subset files to stageIn directory
        logger.debug("saving subset to stageIn directory");
        String format = (String)request.getSession().getAttribute("subsetFormat");
        addSubsetFileToGridJob("dataSubset", "data"+ generateSubsetFileExtension(format), request);
        addSubsetFileToGridJob("bufferSubset", "buffer"+ generateSubsetFileExtension(format), request);
        addSubsetFileToGridJob("meshSubset", "mesh"+ generateSubsetFileExtension(format), request);

        // Check if the user requested to re-submit a previous job.
        String jobIdStr = (String) request.getSession().getAttribute("resubmitJob");
        GeodesyJob existingJob = null;
        if (jobIdStr != null) {
            request.getSession().removeAttribute("resubmitJob");
            logger.debug("Request to re-submit a job.");
            try {
                int jobId = Integer.parseInt(jobIdStr);
                existingJob = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        }

        if (existingJob != null) {
            logger.debug("Using attributes of "+existingJob.getName());
            site = existingJob.getSite();
            version = existingJob.getVersion();
            name = existingJob.getName()+"_resubmit";
            scriptFile = existingJob.getScriptFile();
            description = existingJob.getDescription();

            allQueues = gridAccess.retrieveQueueNamesAtSite(site);
            if (allQueues.length > 0)
                queue = allQueues[0];

            logger.debug("Copying files from old job to stage-in directory");
            File srcDir = new File(existingJob.getOutputDir());
            File destDir = new File(jobInputDir);
            success = Util.copyFilesRecursive(srcDir, destDir);
            if (!success) {
                logger.error("Could not copy all files!");
                // TODO: Let user know this didn't work
            }
        }

        // Check if the ScriptBuilder was used. If so, there is a file in the
        // system temp directory which needs to be staged in.
        String newScript = (String) request.getSession().getAttribute("scriptFile");
        if (newScript != null) {
            request.getSession().removeAttribute("scriptFile");
            logger.debug("Adding "+newScript+" to stage-in directory");
            File tmpScriptFile = new File(System.getProperty("java.io.tmpdir") +
                    File.separator+newScript+".py");
            File newScriptFile = new File(jobInputDir, tmpScriptFile.getName());
            success = Util.moveFile(tmpScriptFile, newScriptFile);
            if (success) {
                logger.info("Moved "+newScript+" to stageIn directory");
                scriptFile = newScript+".py";

                // Extract information from script file
                ScriptParser parser = new ScriptParser();
                try {
                    parser.parse(newScriptFile);
                    cpuCount = parser.getNumWorkerProcesses()+1;
                } catch (IOException e) {
                    logger.warn("Error parsing file: "+e.getMessage());
                }
            } else {
                logger.warn("Could not move "+newScript+" to stage-in!");
            }
        }

        logger.debug("Creating new GeodesyJob instance");
        GeodesyJob job = new GeodesyJob(site, name, version, arguments, queue,
                maxWallTime, maxMemory, cpuCount, inTransfers, outTransfers,
                user, stdInput, stdOutput, stdError);

        job.setScriptFile(scriptFile);
        job.setDescription(description);

        return job;
    }
    
    /**
     * Adds a coverage subset file to the stageIn directory for attachment to the grid job
     * 
     * @param sessionAttrName Name of the session attribute holding the subset data
     * @param fileName Name to give to the newly created file
     * @param request The HttpServletRequest
     */
    private void addSubsetFileToGridJob(String sessionAttrName, String fileName, HttpServletRequest request) {
    	
    	try {
    		InputStream is = (InputStream)request.getSession().getAttribute(sessionAttrName);
    		
    		if (is != null)
    		{
	    		byte[] iobuff = new byte[4096];
		        int bytes;
		        String localJobInputDir = (String) request.getSession().getAttribute("localJobInputDir");
		        File subsetFile = new File(localJobInputDir+GridSubmitController.TABLE_DIR+File.separator+fileName);
		        logger.debug("Data subset path:" + subsetFile.getPath());
		        FileOutputStream fos = new FileOutputStream(subsetFile);
		        
		        while ( (bytes = is.read( iobuff )) != -1 ) {
		            fos.write( iobuff, 0, bytes );
		        }
		        
		        is.close();
		        fos.close();
		        
		        logger.debug("Added subset file - " + subsetFile.getPath());
    		}
        }
        catch (Throwable e) {
            logger.error("Error writing subset file - " + e);
        }
    }
    
    /**
     * Convert the ERDDAP format to a valid file extension if it isn't one already
     * 
     * @param format The format parameter used in the ERDDAP subset call
     * @return A valid file extension. 
     */
    private String generateSubsetFileExtension(String format) {
        if (format.toLowerCase().equals("geotif")) {
        	return ".tiff";
        }
        else {
            return "."+format;
        }
    }

	/** 
     * Create stageIn directories on portal host, so user can upload files easy.
     *
     */
	private boolean createLocalDir(HttpServletRequest request) {
		
		final String user = request.getRemoteUser();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateFmt = sdf.format(new Date());
        String jobID = user + "-" + dateFmt + File.separator;
        String jobInputDir = gridAccess.getLocalGridFtpStageInDir() + jobID;
        
        boolean success = (new File(jobInputDir)).mkdir();
        
        // create stageIn directory
        success = (new File(jobInputDir+GridSubmitController.TABLE_DIR+File.separator)).mkdir();
        
        if (!success) {
            logger.error("Could not create local stageIn directories ");
            jobInputDir = gridAccess.getGridFtpStageInDir();
        }
        // Save in session to use it when submitting job
        request.getSession().setAttribute("localJobInputDir", jobInputDir);
        
        return success;
	}

	/** 
     * Create subJob stageIn directory on portal host, so user can upload files easy.
     * This method is called each time the user is uploading a file for a multiJob.
     *
     */
	private boolean createLocalSubJobDir(HttpServletRequest request, String subJobInputDir, String fileType, String subJobId) {
		
        boolean success = false;
        File subJobFile = new File(subJobInputDir); //create if subJob directory does not exist
        success = subJobFile.exists();
        if(!success){
        	success = subJobFile.mkdir();
        	Hashtable localSubJobDir = (Hashtable) request.getSession().getAttribute("localSubJobDir");
        	
        	if(localSubJobDir == null)
        		localSubJobDir = new Hashtable();
        	
        	if(!localSubJobDir.containsKey(subJobId)){
        		localSubJobDir.put(subJobId, gridAccess.getLocalGridFtpServer()+subJobInputDir);
        		request.getSession().setAttribute("localSubJobDir", localSubJobDir);
        	}
        }

        //create tables directory for the subJob.
        File subJobTablesDir = new File(subJobInputDir+GridSubmitController.TABLE_DIR+File.separator);
        success = subJobTablesDir.exists();
        if(!success){
            success = subJobTablesDir.mkdir();        	
        }	
        
        if (!success) {
            logger.error("Could not create local subJobStageIn directories ");
        }
        
        return success;
	}

    /**
     * Create a stageIn directories on Pbstore. If any errors update status.
     * @param the request to save created directories.
     * 
     */
	private boolean createGridDir(HttpServletRequest request, String myDir) {
		GridTransferStatus status = new GridTransferStatus();
        Object credential = request.getSession().getAttribute("userCred");
        boolean success = true;
        if (credential == null) {
        	status.currentStatusMsg = GridSubmitController.CREDENTIAL_ERROR;
        	return false;
        }
        		
		try {
			GridFTPClient gridStore = new GridFTPClient(gridAccess.getRepoHostName(), gridAccess.getRepoHostFTPPort());		
			gridStore.authenticate((GSSCredential)credential); //authenticating
			gridStore.setDataChannelAuthentication(DataChannelAuthentication.SELF);
			gridStore.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
			if (!gridStore.exists(myDir))
				gridStore.makeDir(myDir);

	        logger.debug("Created Grid Directory.");
	        gridStore.close();
			
		} catch (ServerException e) {
			logger.error("GridFTP ServerException: " + e.getMessage());
			status.currentStatusMsg = GridSubmitController.GRID_LINK;
			status.jobSubmissionStatus = JobSubmissionStatus.Failed;
			success = false;
		} catch (IOException e) {
			logger.error("GridFTP IOException: " + e.getMessage());
			status.currentStatusMsg = GridSubmitController.GRID_LINK;
			status.jobSubmissionStatus = JobSubmissionStatus.Failed;
			success = false;
		} catch (Exception e) {
			logger.error("GridFTP Exception: " + e.getMessage());
			status.currentStatusMsg = GridSubmitController.GRID_LINK;
			status.jobSubmissionStatus = JobSubmissionStatus.Failed;
			success = false;
		}
		
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
	private void addFileNamesOfDirectory(List files, File dir, String subJob, String filePath){
        String fileNames[] = dir.list();
        logger.debug("Inside listJobFiles.do adding files.");
        for (int i=0; i<fileNames.length; i++) {
            File f = new File(dir, fileNames[i]);
            FileInformation fileInfo = new FileInformation(fileNames[i], f.length(), subJob);
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
		public JobSubmissionStatus jobSubmissionStatus = JobSubmissionStatus.Running;				
	}
	

	/**
	 * Enum to indicate over all job submission status.
	 */
	public enum JobSubmissionStatus{Running,Done,Failed }
}
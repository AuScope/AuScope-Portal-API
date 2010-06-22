package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.rmi.ServerException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli2.validation.UrlValidator;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.ICSWMethodMaker;
import org.auscope.portal.server.gridjob.GeodesyGridInputFile;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.auscope.portal.server.gridjob.GridAccessController;
import org.auscope.portal.server.gridjob.ScriptParser;
import org.auscope.portal.server.gridjob.Util;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.controllers.GridSubmitController.GridTransferStatus;
import org.auscope.portal.server.web.controllers.GridSubmitController.JobSubmissionStatus;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.io.urlcopy.UrlCopy;
import org.globus.io.urlcopy.UrlCopyException;
import org.globus.util.GlobusURL;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Controller that handles debug queries
 * 
 *  All responses come in the JSONForm
 *  
 *  {
 *  	success : true/false
 *  	notes : Can be null, will specify a user friendly description of the test result
 *  	data : Can be null, will be set to any object (response specific)
 *  }
 *
 * @author Josh Vote
 */
@Controller
public class DebugController {
	protected final Log logger = LogFactory.getLog(getClass());
	   
	@Autowired
	@Qualifier(value = "propertyConfigurer")
	private PortalPropertyPlaceholderConfigurer hostConfigurer;
	
	@Autowired
	private GridAccessController gridAccess;
	
	@Autowired
    private HttpServiceCaller serviceCaller;
	
	@Autowired
	private CSWService cswService;
	
	/**
	 * Generates a constant JSON response representing a basic test result
	 * @param success Whether the test passed or failed
	 * @param notes Any notes that should be displayed to the end user
	 * @return
	 */
	private static ModelAndView generateBasicTestResponse(boolean success, String notes) {
		return generateBasicTestResponse(success, notes, null);
	}
	
	/**
	 * Generates a constant JSON response representing a basic test result
	 * @param success Whether the test passed or failed
	 * @param notes Any notes that should be displayed to the end user
	 * @param data Must be serializable, any extra response information (test specific)
	 * @return
	 */
	private static ModelAndView generateBasicTestResponse(boolean success, String notes, Object data) {
		ModelAndView mav = new ModelAndView("jsonView");
		
		mav.addObject("success", success);
		mav.addObject("notes", notes);
		mav.addObject("data", data);
		
		return mav;
	}
	
	/**
	 * A simple interface to inject a function into doGridOperation
	 * @author VOT002
	 *
	 */
	private interface GridOperation {
		public ModelAndView operation(HttpServletRequest request, GridFTPClient gridStore) throws Exception;
	}
	
	/**
	 * Opens a connection to the grid and handles any exceptions / closing of the gridStore.
	 */
	private ModelAndView doGridOperation(HttpServletRequest request,GridOperation operation) {
	    Object credential = request.getSession().getAttribute("userCred");
	    ModelAndView userResult = null;
	    boolean success = true;
	    
	    if (credential == null) {
	    	return generateBasicTestResponse(false, "User credentials are null (have you logged in yet?)");
	    }
	        
	    //Open the connection, let the user operation run and cleanup after it
	    GridFTPClient gridStore = null;
		try {
			gridStore = new GridFTPClient(gridAccess.getRepoHostName(), gridAccess.getRepoHostFTPPort());		
			gridStore.authenticate((GSSCredential)credential); //authenticating
			gridStore.setDataChannelAuthentication(DataChannelAuthentication.SELF);
			gridStore.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
			
			userResult = operation.operation(request, gridStore);
				        
		} catch (ServerException e) {
			return generateBasicTestResponse(false, "ServerException: " + e.toString()); 
		} catch (IOException e) {
			return generateBasicTestResponse(false, "IOException: " + e.toString());
		} catch (Exception e) {
			return generateBasicTestResponse(false, "Exception: " + e.toString());
		} finally {
			try {
				if (gridStore != null)
					gridStore.close();
			} catch (Exception ex) {
				return generateBasicTestResponse(false, "(Whilst closing store) Exception: " + ex.toString());
			}
		}
		
		//The the user operation didn't crash, return its result
		return userResult;
	}
	
	@RequestMapping("/dbg/checkUsername.do")
	public ModelAndView checkUsername(HttpServletRequest request) {
		final String user = request.getRemoteUser();
		
		if (user == null || user.length() == 0) {
			return generateBasicTestResponse(false, "Username is null or empty (have you logged in yet?)");
		}
		
		return generateBasicTestResponse(true, "Username has no apparent problems");
	} 
	
	@RequestMapping("/dbg/checkCredentials.do")
	public ModelAndView checkCredentials(HttpServletRequest request) {
		Object credentials = request.getSession().getAttribute("userCred");
		
		if (credentials == null) {
			return generateBasicTestResponse(false, "Credentials are null (have you logged in yet?)");
		}
		
		if (!gridAccess.isProxyValid(credentials)) {
			return generateBasicTestResponse(false, "Credentials are invalid (contact your IDP)");
		}
		
		return generateBasicTestResponse(true, "Credentials have no apparent problems");
	}
	
	@RequestMapping("/dbg/checkInputFiles.do")
	public ModelAndView checkInputFiles(HttpServletRequest request) {
		List<GeodesyGridInputFile> selectedList = (List<GeodesyGridInputFile>) request.getSession().getAttribute("selectedGPSfiles");
		List<GeodesyGridInputFile> inputList = (List<GeodesyGridInputFile>) request.getSession().getAttribute("gridInputFiles");
		
		if (selectedList == null && inputList == null) {
			return generateBasicTestResponse(false, "No input files have been selected (From map or station select)");
		} else if (selectedList != null && inputList == null) {
			return generateBasicTestResponse(false, String.format("%1$d files have been selected on the map, but they have not been confirmed in the data service tool", selectedList.size()));
		} else if (selectedList == null && inputList != null) {
			return generateBasicTestResponse(true, String.format("%1$d files have been selected using the multi station selection method", inputList.size()));
		}
	
		return generateBasicTestResponse(true, String.format("%1$d files have been selected using the map,  %2$d files have been confirmed", selectedList.size(), inputList.size()));
	}
	
	private ModelAndView checkLocalStageInDir_Recursive(HttpServletRequest request, File dir) {
		File[] files = dir.listFiles();
		
		for (File file : files) {
			if (file.isDirectory()) {
				ModelAndView mav = checkLocalStageInDir_Recursive(request, file);
				if (!((Boolean)mav.getModel().get("success"))) {
					return mav;
				}
			} else if (file.isFile()) {
				
				//Ensure we can open each file (We have witnessed a strange bug that allowed us to copy but disallowed us to reopen it due to a weird permissions problem)
				FileWriter fw = null;
				try {
					fw = new FileWriter(file,true);
				} catch (Exception e) {
					return generateBasicTestResponse(false, String.format("Exception with stage in file '%1$s' in dir '%2$s' error:%3$s", file.getName(), dir.getPath(), e.toString()));
				} finally {
					try {
						if (fw != null)
							fw.close();
					} catch (Exception ex) { }
				}
			}
		}
		
		return generateBasicTestResponse(true, "All files in stage in are valid");
	}
	
	@RequestMapping("/dbg/checkLocalStageInDir.do")
	public ModelAndView checkLocalStageInDir(HttpServletRequest request) {
		String localStageInDir = (String) request.getSession().getAttribute("localJobInputDir");
		
		//Check if its stored in the session
		if (localStageInDir == null || localStageInDir.length() == 0) {
			return generateBasicTestResponse(false, "Local stage in directory has not been set in this session (Have you visited JobSubmit yet?)"); 
		}
		
		//Run some checks on the actual directory
		File dir = new File(localStageInDir);
		if (!dir.exists()) {
			return generateBasicTestResponse(false, String.format("Local directory '%1$s' does not exist (log out and try again)", localStageInDir));
		}
		if (!dir.isDirectory()) {
			return generateBasicTestResponse(false, String.format("Local directory '%1$s' exists but is NOT a directory (log out and try again)", localStageInDir));
		}
		
		//Do some basic content checks
		File[] dirFiles = dir.listFiles();
		if (dirFiles == null) {
			return generateBasicTestResponse(false, String.format("Local directory '%1$s' cannot have its files listed (log out and try again)", localStageInDir));
		}
		if (dirFiles.length == 0) {
			return generateBasicTestResponse(false, String.format("Local directory '%1$s' is empty (log out and try again)", localStageInDir));
		}
		
		//Before looping through every file to ensure we have access to them
		return checkLocalStageInDir_Recursive(request, dir);
	}
	
	/**
	 * Recursively compares remote stage in contents against the local stage in directory (They should be identical)
	 * @param request
	 * @param gridStore
	 * @param localDir
	 * @param gridDir
	 * @return
	 */
	private ModelAndView checkRemoteStageInDirContents_Recursive(HttpServletRequest request, GridFTPClient gridStore, File localDir, String gridDir) {
		File[] localFiles = localDir.listFiles();
		
		//Move into our grid directory
		try {
			if (!gridStore.exists(gridDir))
				throw new Exception("Directory does not exist");
			
			gridStore.changeDir(gridDir);
		} catch (Exception e) {
			return generateBasicTestResponse(false, String.format("Couldn't change into gridDirectory '%1$s' Exception: %2$s", gridDir, e));
		}
		
		
		for (int i = 0; i < localFiles.length; i++) {
			File currentFile = localFiles[i];
			
			//Recurse into directories
			if (currentFile.isDirectory()) {
				ModelAndView mav = checkRemoteStageInDirContents_Recursive(request, gridStore, currentFile, gridDir + File.pathSeparator + currentFile.getName());
				if (!((Boolean)mav.getModel().get("success"))) {
					return mav;
				}
			//Compare files
			} else if (currentFile.isFile()) {
				try {
					String remoteFile = gridDir + File.separator + currentFile.getName();
					
					if (!gridStore.exists(remoteFile)) {
						throw new Exception("file does not exist");
					}
				} catch (Exception e) {
					return generateBasicTestResponse(false, String.format("Error with file '%1$s' in directory '%2$s' Exception: %3$s", currentFile.getName(), gridDir, e));
				}
			}
		}
		
		return generateBasicTestResponse(true, "Grid stageIn directory appears to match the local stageIn directory");
	}
	
	private ModelAndView checkRemoteStageInDirContents(HttpServletRequest request, GridFTPClient gridStore) {
		String localStageInDir = (String) request.getSession().getAttribute("localJobInputDir");
		String gridStageInDir = (String) request.getSession().getAttribute("jobInputDir");
		
		if (localStageInDir == null || localStageInDir.length() == 0) {
			return generateBasicTestResponse(false, "Local stage in directory has not been set in this session (cannot compare local to grid stageInDirectories) (Have you visited JobSubmit yet?)"); 
		}
		
		//Iterate through our local directory checking each file exists in the grid ('recurse' into directories)
		File rootLocalDir = new File(localStageInDir);

		return checkRemoteStageInDirContents_Recursive(request, gridStore, rootLocalDir, gridStageInDir);
	}
	
	@RequestMapping("/dbg/checkRemoteStageInDir.do")
	public ModelAndView checkRemoteStageInDir(HttpServletRequest request) {
		final String gridStageInDir = (String) request.getSession().getAttribute("jobInputDir");
		
		//Check if its stored in the session
		if (gridStageInDir == null || gridStageInDir.length() == 0) {
			return generateBasicTestResponse(false, "Grid stage in directory has not been set in this session (Have you visited JobSubmit yet?)"); 
		}
		
		return doGridOperation(request, new GridOperation() {
			
			public ModelAndView operation(HttpServletRequest request, GridFTPClient gridStore) throws Exception {
				if (!gridStore.exists(gridStageInDir)) {
					return generateBasicTestResponse(false, String.format("Grid directory '%1$s' does not exist (log out and try again)", gridStageInDir));
				}
				
				return checkRemoteStageInDirContents(request, gridStore); 
			}
		}); 
	}
	
	@RequestMapping("/dbg/urlCopyTest.do")
	public ModelAndView urlCopyTest(HttpServletRequest request, String fromUrl, String toUrl) {
		Object credentials = request.getSession().getAttribute("userCred");
		
        try {
    		GlobusURL from = new GlobusURL(fromUrl);
    		GlobusURL to = new GlobusURL(toUrl);
    		
    		//Not knowing how long UrlCopy will take, the UI request status update of 
    		//file transfer periodically
    		UrlCopy uCopy = new UrlCopy();
    		logger.error("Credentials: " + credentials);
    		uCopy.setCredentials((GSSCredential)credentials);
    		uCopy.setDestinationUrl(to);
    		uCopy.setSourceUrl(from);
    		// Disables usage of third party transfers, for grid security reasons.
    		uCopy.setUseThirdPartyCopy(false); 	
    		uCopy.copy();   		
    	} catch (UrlCopyException e) {
    		return generateBasicTestResponse(false,"Error! UrlCopyException: " + e.toString());
    	} catch (Exception e) {
    		return generateBasicTestResponse(false,"Error! Exception: " +  e.toString());
    	}
    	
    	
    	return generateBasicTestResponse(true, "Copy reported no errors");
	}
	
	/**
	 * Gets count arbitrary rinex file urls from geoserver 
	 * @param count the number of urls to fetch
	 * @return a list of urls (May not match count length if the response falls short or runs over for whatever reason)
	 */
	private List<GeodesyGridInputFile> getRinexFileUrls(final String serviceUrl, final int count) throws Exception {		
		String gmlResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
            public HttpMethodBase makeMethod() {
                GetMethod method = new GetMethod(serviceUrl);

                //attach them to the method
                method.setQueryString(new NameValuePair[]{new NameValuePair("request", "GetFeature"), 
                											new NameValuePair("outputFormat", "GML2"),
                											new NameValuePair("typeName", "geodesy:station_observations"),
                											new NameValuePair("maxFeatures", Integer.toString(count)),
                											new NameValuePair("PropertyName", "geodesy:url")});

                return method;
            }
        }.makeMethod(), serviceCaller.getHttpClient());
		
		return GeodesyGridInputFile.fromGmlString(gmlResponse);
	}
	
	private String getRinexFilesUrl() throws Exception {
		CSWRecord[] records = cswService.getWFSRecordsForTypename("ngcp:GnssStation");
		if (records == null || records.length == 0) {
			throw new Exception("No ngcp:GnssStation record in the CSW");
		}
		
		String serviceUrl = records[0].getServiceUrl();
		if (serviceUrl == null || serviceUrl.length() == 0) {
			throw new Exception("ngcp:GnssStation has no service url from its CSW");
		}
		
		return serviceUrl;
	}
	
	@RequestMapping("/dbg/testRinexUrlAvailability.do")
	public ModelAndView testRinexUrlAvailability(HttpServletRequest request) {
		String serviceUrl = null;
		try {
			serviceUrl = getRinexFilesUrl();
		} catch (Exception ex) {
			return generateBasicTestResponse(false, "Error extracting service url: " + ex);
		}
		
		//The test is to see if we can get a single arbitrary URL
		String url = null;
		try {
			List<GeodesyGridInputFile> ggifs = getRinexFileUrls(serviceUrl, 1);
			if (ggifs == null || ggifs.size() < 1) {
				throw new Exception("RINEX input file request returned empty list");
			}
			
			url = ggifs.get(0).getFileUrl();
			if (url == null || url.length() == 0) {
				throw new Exception("RINEX input file url is null or empty");
			}
			
			//This will throw a malformed url exception if its invalid
			URL toTest = new URL(url);
		} catch (Exception ex) {
			return generateBasicTestResponse(false, "Error getting RINEX url: " + ex);
		}
		
		//Now lets see if we can actually request the rinex file url from the server
		try {
			final String urlToSend = url;
			byte[] rinexBytes = serviceCaller.getMethodResponseInBytes(new ICSWMethodMaker() {
	            public HttpMethodBase makeMethod() {
	                GetMethod method = new GetMethod(urlToSend);
	                return method;
	            }
	        }.makeMethod(), serviceCaller.getHttpClient());
			
			if (rinexBytes == null || rinexBytes.length == 0) {
				throw new Exception("RINEX file returned an empty byte list");
			}
		} catch (Exception ex) {
			return generateBasicTestResponse(false, "Error getting RINEX data: " + ex, url);
		}
		
		return generateBasicTestResponse(true, "Able to fetch a RINEX file url and also download it", url);
	}
	
	private static String generateJobId(final String user) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateFmt = sdf.format(new Date());
        return user + "-" + dateFmt + File.separator;
	}
	
    private GeodesyJob prepareSimpleJob(final String user) throws Exception {
        final String maxWallTime = "60"; // in minutes
        final String maxMemory = "2048"; // in MB
        final String stdInput = "";
        final String stdOutput = "stdOutput.txt";
        final String stdError = "stdError.txt";
        final String[] arguments = new String[0];
        final String[] inTransfers = new String[0];
        final String[] outTransfers = new String[0];
        String name = "GeodesyJob";
        String site = "iVEC";
        Integer cpuCount = 1;
        String version = "";
        String queue = "";
        String description = "";
        String scriptFile = "";


        // Set a default version and queue
        String[] allVersions = gridAccess.retrieveCodeVersionsAtSite(site, GeodesyJob.CODE_NAME);
        if (allVersions.length > 0)
            version = allVersions[0];
        else
        	throw new Exception(String.format("Couldn't lookup version for %1$s at site %2$s",GeodesyJob.CODE_NAME,site));
        
        String[] allQueues = gridAccess.retrieveQueueNamesAtSite(site);
        if (allQueues.length > 0)
            queue = allQueues[0];
        else
        	throw new Exception("Couldn't lookup job queue at site:" + site);

        logger.debug("Creating new GeodesyJob instance");
        GeodesyJob job = new GeodesyJob(site, name, version, arguments, queue,
                maxWallTime, maxMemory, cpuCount, inTransfers, outTransfers,
                user, stdInput, stdOutput, stdError);

        job.setScriptFile(scriptFile);
        job.setDescription(description);

        return job;
    }
	
    /**
     * Creates a test job with default parameters and attempts to submit it to the grid
     * @param request
     * @return
     */
    @RequestMapping("/dbg/testRunJob.do")
	public ModelAndView runTestJob(HttpServletRequest request) {
		final String user = request.getRemoteUser();
		final Object credentials = request.getSession().getAttribute("userCred");
		final String jobId = "test" + generateJobId(user);
		final String jobRootDir = gridAccess.getGridFtpStageInDir() + jobId;
		final String jobRinexDir = jobRootDir + GridSubmitController.RINEX_DIR + File.separator;
		final String localDir = gridAccess.getLocalGridFtpStageInDir() + "test" + user;
		
		if (user == null || user.length() == 0) {
			return generateBasicTestResponse(false, "User is null or empty (Have you logged in?)");
		}
		
		if (credentials == null) {
			return generateBasicTestResponse(false, "User credentials are null (Have you logged in?)");
		}
		
		//Form our job object
		GeodesyJob job = null;
		try {
			job = prepareSimpleJob(user);
		} catch (Exception ex) {
			return generateBasicTestResponse(false, "Couldn't generate job object: " + ex.toString());
		}
		
		//Generate local "stage in dir"
		try {
			boolean success = (new File(localDir)).mkdir();
			if (!success)
				throw new Exception("Error creating local test dir");
			success = (new File(localDir+GridSubmitController.RINEX_DIR+File.separator)).mkdir();
			if (!success)
				throw new Exception("Error creating local test (rinex) dir");
			success = Util.copyFilesRecursive(new File(GridSubmitController.PRE_STAGE_IN_TABLE_FILES),
	                new File(localDir+GridSubmitController.TABLE_DIR+File.separator));
			if (!success)
				throw new Exception("Error creating local table (rinex) dir");
		} catch (Exception ex) {
			return generateBasicTestResponse(false, "Error creating a local stage in directory: " + ex.toString(), localDir);
		}
		
		//Generate our output dir name
		String temp = "";
		try {
			temp = gridAccess.getGridFtpStageOutDir()+ GridSubmitController.generateCertDNDirectory(credentials) + jobId + File.separator;
		} catch (GSSException ex) {
			return generateBasicTestResponse(false, "Unable to generate directory name based on credential DN: " + ex.toString());
		}
		final String jobOutputDir = temp; 
		
		//Create remote stage in / stage out directory
		ModelAndView mav = doGridOperation(request, new GridOperation() {
			public ModelAndView operation(HttpServletRequest request, GridFTPClient gridStore) throws Exception {
				
				if (!gridStore.exists(jobRootDir))
					gridStore.makeDir(jobRootDir);
				
				if (!gridStore.exists(jobRinexDir))
					gridStore.makeDir(jobRinexDir);
				
				if (!gridStore.exists(jobOutputDir))
					gridStore.makeDir(jobOutputDir);
				
				return generateBasicTestResponse(true, "Success creating grid directories");
			}
		});
		if (!(Boolean)mav.getModel().get("success")) 
			return mav;
		
		//Get the rinex files to process
		String rinexUrl = null;
		try {
			String serviceUrl = getRinexFilesUrl(); 
			List<GeodesyGridInputFile> rinexFiles = getRinexFileUrls(serviceUrl, 1);
			rinexUrl = rinexFiles.get(0).getFileUrl();
		} catch (Exception ex) {
			return generateBasicTestResponse(false, "Error extracting rinex url: " + ex);
		}
		
		//Copy the rinex file over
		mav = urlCopyTest(request,rinexUrl, jobRinexDir);
		if (!(Boolean)mav.getModel().get("success")) 
			return mav;
		
		//Final configuration of our job
		job.setInTransfers(new String[]{gridAccess.getGridFtpServer()+jobRootDir, gridAccess.getLocalGridFtpServer()+localDir});
		job.setEmailAddress(user);
        job.setOutputDir(jobOutputDir);
        job.setOutTransfers(new String[] { gridAccess.getGridFtpServer() + jobOutputDir });
        job.setArguments(new String[] {"-d", "2010", "80", "-expt", "grid", "-orbt", "IGSF", "-no_ftp", "-aprfile", "itrf05.apr"});
        job.setName("DebugPage-TestJob");
        job.setDescription("This job has been auto created as a test, it will not return meaningful results");
        
        //Do the submission
        String submitEpr = null;
        try {
        	submitEpr = gridAccess.submitJob(job, credentials);
        	if (submitEpr == null || submitEpr.length() == 0)
        		throw new Exception("returned EPR is null or empty");
        	
        } catch (Exception ex) {
        	return generateBasicTestResponse(false, "Failure during job submission: " + ex.toString());
        }
        
		
		return generateBasicTestResponse(true, "Successfully submitted job. EPR:" + submitEpr, submitEpr);
	}

}

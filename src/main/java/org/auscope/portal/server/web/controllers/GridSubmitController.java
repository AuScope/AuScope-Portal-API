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
import java.io.StringReader;
import java.net.URL;
import java.rmi.ServerException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.sf.json.JSONArray;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.csw.ICSWMethodMaker;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.gridjob.GeodesyGridInputFile;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.auscope.portal.server.gridjob.GeodesyJobManager;
import org.auscope.portal.server.gridjob.GeodesySeries;
import org.auscope.portal.server.gridjob.GridAccessController;
import org.auscope.portal.server.gridjob.ScriptParser;
import org.auscope.portal.server.gridjob.Util;
import org.auscope.portal.server.web.service.CSWService;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.FileInfo;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.io.urlcopy.UrlCopy;
import org.globus.io.urlcopy.UrlCopyException;
import org.globus.util.GlobusURL;
import org.globus.wsrf.utils.FaultHelper;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


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
    private CSWService cswService;
    @Autowired
    private HttpServiceCaller serviceCaller;

    private static final String TABLE_DIR = "tables";
    private static final String RINEX_DIR = "rinex";
    private static final String PRE_STAGE_IN_TABLE_FILES = "/home/grid-auscope/tables/";
    private static final String IVEC_MIRROR_URL = "http://files.ivec.org/geodesy/";
    private static final String PBSTORE_RINEX_PATH = "//pbstore/cg01/geodesy/ftp.ga.gov.au/gpsdata/";
    private static final String FOR_ALL = "Common";

    //Grid File Transfer messages
    private static final String FILE_COPIED = "Please wait while files being transfered.... ";
    private static final String FILE_COPY_ERROR = "Job submission failed due to file transfer Error.";
    private static final String INTERNAL_ERROR= "Job submission failed due to INTERNAL ERROR";
    private static final String GRID_LINK = "Job submission failed due to GRID Link Error";
    private static final String TRANSFER_COMPLETE = "Transfer Complete";
    private static final String CREDENTIAL_ERROR = "Job submission failed due to Invalid Credential Error";
    
    /**
     * Pattern to match compressed RINEX file name.
     * 
     * <p>
     * Groups in the pattern:
     * <ol>
     * <li>four character site id (should be lowercase)
     * <li>day of the year (1 is the first of January)
     * <li>two-digit year (we assume start at 1990)
     * </ol>
     */
    private static Pattern RINEX_FILENAME_PATTERN = Pattern
            .compile("(\\w{4})(\\d{3})\\d\\.(\\d{2})\\w\\.Z");

    
    
    
    /**
     * Given a list of stations and a date range this function queries the remote serviceUrl for a list of log files
     * and returns a JSON representation of the response
     * 
     * @param dateFrom The (inclusive) start of date range in YYYY-MM-DD format
     * @param dateTo The (inclusive) end of date range in YYYY-MM-DD format
     * @param serviceUrl The remote service URL to query
     * @param stationList a list (comma seperated) of the GPSSITEID to fetch log files for
     * 
     * Response Format
     * {
     *     success : (true/false),
     *     urlList : [{
     *         fileUrl    : (Mapped from url or empty string)
     *         fileDate   : (Mapped from date or empty string)
     *         stationId  : (Mapped from stationId or empty string)
     *         selected   : will be always set to true
     *     }]
     * }
     */
    @RequestMapping(value = "/getStationListUrls.do", params = {"dateFrom","dateTo", "stationList", "serviceUrl"})
    public ModelAndView getStationListUrls(@RequestParam("dateFrom") final String dateFrom,
								          @RequestParam("dateTo") final String dateTo,
								          @RequestParam("serviceUrl") final String serviceUrl,
								          @RequestParam("stationList") final String stationList,
								          HttpServletRequest request) {
    	boolean success = true;
    	ModelAndView jsonResponse = new ModelAndView("jsonView");
    	JSONArray urlList = new JSONArray();
    	
    	logger.debug("getStationListUrls : Requesting urls for " + stationList + " in the range " + dateFrom + " -> " + dateTo);
    	
    	try {
    		String gmlResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
                public HttpMethodBase makeMethod() {
                    GetMethod method = new GetMethod(serviceUrl);

                    //Generate our filter string based on date and station list
                    String cqlFilter = "(date>='" + dateFrom + "')AND(date<='" + dateTo + "')";
                    if (stationList != null && stationList.length() > 0) {
                    	
                    	String[] stations = stationList.split(",");
                    	
                    	cqlFilter += "AND(";
                    	
                    	for (int i = 0; i < stations.length; i++) {
                    		if (i > 0)
                    			cqlFilter += "OR";
                    		
                    		cqlFilter += "(id='" + stations[i] + "')";
                    	}
                    	
                    	cqlFilter += ")";
                    }

                    //attach them to the method
                    method.setQueryString(new NameValuePair[]{new NameValuePair("request", "GetFeature"), 
                    											new NameValuePair("outputFormat", "GML2"),
                    											new NameValuePair("typeName", "geodesy:station_observations"),
                    											new NameValuePair("PropertyName", "geodesy:date,geodesy:url,geodesy:id"),
                    											new NameValuePair("CQL_FILTER", cqlFilter)});

                    return method;
                }
            }.makeMethod(), serviceCaller.getHttpClient());
    		
    		//Parse our XML string into a document
    		XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document responseDocument = builder.parse(new InputSource(new StringReader(gmlResponse)));
    		
            //Extract the URL list and parse it into the JSON list
            HttpSession userSession = request.getSession();
            String featureMemberExpression = "/FeatureCollection/featureMember";
            NodeList nodes = (NodeList) xPath.evaluate(featureMemberExpression, responseDocument, XPathConstants.NODESET);
            if (nodes != null) {
	            for(int i=0; i < nodes.getLength(); i++ ) {
	            	GeodesyGridInputFile ggif = new GeodesyGridInputFile();
	            	
	            	Node tempNode = (Node) xPath.evaluate("station_observations/url", nodes.item(i), XPathConstants.NODE);
	            	ggif.setFileUrl(tempNode == null ? "" : tempNode.getTextContent());
	            	
	            	tempNode = (Node) xPath.evaluate("station_observations/date", nodes.item(i), XPathConstants.NODE);
	            	ggif.setFileDate(tempNode == null ? "" : tempNode.getTextContent());
	            	
	            	tempNode = (Node) xPath.evaluate("station_observations/id", nodes.item(i), XPathConstants.NODE);
	            	ggif.setStationId(tempNode == null ? "" : tempNode.getTextContent());
	            	
	            	ggif.setSelected(true);
	            	
	            	urlList.add(ggif);
	            }
            }
            
    	} catch (Exception ex) {
    		logger.warn("selectStationList.do : Error " + ex.getMessage());
    		urlList.clear();
            success = false;
    	}
    	//save the date range for later processing
    	request.getSession().setAttribute("dateFrom", dateFrom);
    	request.getSession().setAttribute("dateTo", dateTo);
    	
    	jsonResponse.addObject("success", success);
    	jsonResponse.addObject("urlList", urlList);
    	
        return jsonResponse;
    }
    
    /**
     * Returns every Geodesy station (and some extra descriptive info) in a JSON format.
     * 
     * Response Format
     * {
     *     success : (true/false),
     *     records : [{
     *         stationNumber : (Mapped from STATIONNO or empty string)
     *         stationName   : (Mapped from STATIONNAME or empty string)
     *         gpsSiteId     : (Mapped from GPSSITEID or empty string)
     *         countryId     : (Mapped from COUNTRYID or empty string)
     *         stateId       : (Mapped from STATEID or empty string)
     *     }]
     * }
     */
    @RequestMapping("/getStationList.do")
    public ModelAndView getStationList() {
        final String stationTypeName = "ngcp:GnssStation";
        ModelAndView jsonResponse = new ModelAndView("jsonView");
        JSONArray stationList = new JSONArray();
        boolean success = true;

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            //Query every geodesy station provider for the raw GML
            CSWRecord[] geodesyRecords = cswService.getWFSRecordsForTypename(stationTypeName);
            if (geodesyRecords == null || geodesyRecords.length == 0)
            	throw new Exception("No " + stationTypeName + " records available");
            
            //This makes the assumption of only a single geodesy WFS
            CSWRecord record = geodesyRecords[0];
            final String serviceUrl = record.getServiceUrl();
            
            jsonResponse.addObject("serviceUrl", serviceUrl);

            logger.debug("getStationListXML.do : Requesting " + stationTypeName + " for " + serviceUrl);

            String gmlResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
                public HttpMethodBase makeMethod() {
                    GetMethod method = new GetMethod(serviceUrl);

                    //set all of the parameters
                    NameValuePair request = new NameValuePair("request", "GetFeature");
                    NameValuePair elementSet = new NameValuePair("typeName", stationTypeName);

                    //attach them to the method
                    method.setQueryString(new NameValuePair[]{request, elementSet});

                    return method;
                }
            }.makeMethod(), serviceCaller.getHttpClient());
            
            //Parse the raw GML and generate some useful JSON objects
            Document doc = builder.parse(new InputSource(new StringReader(gmlResponse)));
            
            String serviceTitleExpression = "/FeatureCollection/featureMembers/GnssStation";
            NodeList nodes = (NodeList) xPath.evaluate(serviceTitleExpression, doc, XPathConstants.NODESET);

            //Lets pull some useful info out
            for(int i=0; nodes != null && i < nodes.getLength(); i++ ) {
                Node node = nodes.item(i);
                ModelMap stationMap = new ModelMap();

                Node tempNode = (Node) xPath.evaluate("STATIONNO", node, XPathConstants.NODE);
                stationMap.addAttribute("stationNumber", tempNode == null ? "" : tempNode.getTextContent());
                
                tempNode = (Node) xPath.evaluate("GPSSITEID", node, XPathConstants.NODE);
                stationMap.addAttribute("gpsSiteId", tempNode == null ? "" : tempNode.getTextContent());
                
                tempNode = (Node) xPath.evaluate("STATIONNAME", node, XPathConstants.NODE);
                stationMap.addAttribute("stationName", tempNode == null ? "" : tempNode.getTextContent());
                
                tempNode = (Node) xPath.evaluate("COUNTRYID", node, XPathConstants.NODE);
                stationMap.addAttribute("countryId", tempNode == null ? "" : tempNode.getTextContent());
                
                tempNode = (Node) xPath.evaluate("STATEID", node, XPathConstants.NODE);
                stationMap.addAttribute("stateId", tempNode == null ? "" : tempNode.getTextContent());

                stationList.add(stationMap);
            }
        } catch (Exception ex) {
            logger.warn("getStationListXML.do : Error " + ex.getMessage());
            success = false;
            stationList.clear();
        }

        //Form our response object
        jsonResponse.addObject("stations", stationList);
        jsonResponse.addObject("success", success);
        return jsonResponse;
    }

    /**
     * Sets the <code>GridAccessController</code> to be used for grid
     * activities.
     *
     * @param gridAccess the GridAccessController to use
     */
    /*public void setGridAccess(GridAccessController gridAccess) {
        this.gridAccess = gridAccess;
    }*/

    /**
     * Sets the <code>GeodesyJobManager</code> to be used to retrieve and store
     * series and job details.
     *
     * @param jobManager the JobManager to use
     */
    /*public void setJobManager(GeodesyJobManager jobManager) {
        this.jobManager = jobManager;
    }*/

    /*protected ModelAndView handleNoSuchRequestHandlingMethod(
            NoSuchRequestHandlingMethodException ex,
            HttpServletRequest request,
            HttpServletResponse response) {

        // Ensure user has valid grid credentials
        if (gridAccess.isProxyValid(
                request.getSession().getAttribute("userCred"))) {
        	logger.debug("No/invalid action parameter; returning gridsubmit view.");
        	return new ModelAndView("gridsubmit");
        } 
        else 
        {
        	request.getSession().setAttribute(
                "redirectAfterLogin", "/gridsubmit.html");
        	logger.warn("Proxy not initialized. Redirecting to gridLogin.");
        	return new ModelAndView(
                new RedirectView("/gridLogin.html", true, false, false));
        }
    }*/

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

        logger.debug("Querying series of "+user);
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
        logger.debug("Querying code list for "+user);
        List<SimpleBean> code = new ArrayList<SimpleBean>();
        code.add(new SimpleBean("Gamit"));
        code.add(new SimpleBean("Burmese"));
        

        logger.debug("Returning list of "+code.size()+" codeObject.");
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
        jobType.add(new SimpleBean("single"));
        jobType.add(new SimpleBean("multi"));
        

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
     * Returns a JSON object containing an array of ESyS-particle sites.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a sites attribute which is an array of
     *         sites on the grid that have an installation of ESyS-particle.
     */
   /* @RequestMapping("/listSites.do")    
    public ModelAndView listSites(HttpServletRequest request,
                                  HttpServletResponse response) {

        logger.debug("Retrieving sites with "+GeodesyJob.CODE_NAME+" installations.");
        String[] particleSites = gridAccess.
                retrieveSitesWithSoftwareAndVersion(GeodesyJob.CODE_NAME, "");

        List<SimpleBean> sites = new ArrayList<SimpleBean>();
        for (int i=0; i<particleSites.length; i++) {
            sites.add(new SimpleBean(particleSites[i]));
            logger.debug("Site name: "+particleSites[i]);
        }

        logger.debug("Returning list of "+particleSites.length+" sites.");
        return new ModelAndView("jsonView", "sites", sites);
    }*/

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
        		String filePath = jobInputDir+GridSubmitController.RINEX_DIR;
	            File dir = new File(filePath+File.separator);
	            addFileNamesOfDirectory(files, dir, GridSubmitController.FOR_ALL, filePath);
	            filePath = jobInputDir+GridSubmitController.TABLE_DIR;
	            dir = new File(filePath+File.separator);
	            addFileNamesOfDirectory(files, dir, GridSubmitController.FOR_ALL, filePath);
	            logger.debug("Inside listJobFiles.do multi job");
	            boolean subJobExist = true;
	            int i = 0;
	            while(subJobExist){
	            	String subDirID = "subJob_"+i;
	            	File subDir = new File(jobInputDir+subDirID+File.separator);
	            	if(subDir.exists()){
	            		//list rinex dir
	            		filePath = jobInputDir+subDirID+File.separator+GridSubmitController.RINEX_DIR;
	            		subDir = new File(filePath +File.separator);
	            		if(subDir.exists()){
	            			addFileNamesOfDirectory(files, dir, subDirID, filePath);
	            		}
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
	             
        	}/*else{
        		logger.debug("Inside listJobFiles.do single job");
        		String filePath = jobInputDir+GridSubmitController.RINEX_DIR;
	            File dir = new File(filePath+File.separator);
	            addFileNamesOfDirectory(files, dir, GridSubmitController.FOR_ALL, filePath);
	            
	            filePath = jobInputDir+GridSubmitController.TABLE_DIR;
	            dir = new File(filePath+File.separator);
	            addFileNamesOfDirectory(files, dir, GridSubmitController.FOR_ALL, filePath);
        	}*/
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
            logger.debug("Downloading: "+dirPathStr+fileName+".");
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
        	String fileType = checkFileType(f.getOriginalFilename());
            //check if multiJob or not
            if(jobType.equals("single") || subJobId.equals(GridSubmitController.FOR_ALL)){
            	logger.debug("uploading file for single job ");
            	subJobId = GridSubmitController.FOR_ALL;
            	if(fileType.equals(GridSubmitController.TABLE_DIR)){
            		destinationPath = jobInputDir+GridSubmitController.TABLE_DIR+File.separator;
            	}else{
            		destinationPath = jobInputDir+GridSubmitController.RINEX_DIR+File.separator;
            	}
            }
            else{
            	logger.debug("uploading file for multi job ");
                
                String subJobInputDir = jobInputDir+subJobId.trim()+File.separator;
            	if(createLocalSubJobDir(request, subJobInputDir, fileType, subJobId.trim())){
            		if(fileType.equals(GridSubmitController.TABLE_DIR)){
            			destinationPath = subJobInputDir+GridSubmitController.TABLE_DIR+File.separator;
            		}
            		else{
            			destinationPath = subJobInputDir+GridSubmitController.RINEX_DIR+File.separator;
            		}    		
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
                pw.print(",name:'"+fileInfo.getName()+"',size:"+fileInfo.getSize()+",subJob:'"+subJobId+"'");
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
            	String fileType = checkFileType(filename);
            	String fullFilename = null;
            	
            	if(subJobId[i] == null || subJobId[i].equals(""))
            		subJobId[i] = GridSubmitController.FOR_ALL;
            	
            	if(subJobId[i].equals(GridSubmitController.FOR_ALL)){
            		logger.debug("Deleting "+filename+" for subJob"+subJobId[i]);
                	if(fileType.equals(GridSubmitController.TABLE_DIR)){
                		fullFilename = jobInputDir+GridSubmitController.TABLE_DIR
                		                          +File.separator+filename;
                	}else{
                		fullFilename = jobInputDir+GridSubmitController.RINEX_DIR
                		                          +File.separator+filename;
                	}
            	}else{
            		logger.debug("Deleting "+filename+" for subJob"+subJobId[i]);
                	if(fileType.equals(GridSubmitController.TABLE_DIR)){
                		fullFilename = jobInputDir+subJobId[i]+File.separator
                		               +GridSubmitController.TABLE_DIR+File.separator+filename;
                	}else{
                		fullFilename = jobInputDir+subJobId[i]+File.separator
                		               +GridSubmitController.RINEX_DIR+File.separator+filename;
                	}           		
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
    	
    	
    	//Lets replace our sites.default with a new template we create dynamically 
        if (!templateSitesDefaults(request, localJobInputDir + GridSubmitController.TABLE_DIR + File.separator, job)) {
        	logger.error("Error creating template sites.default");
            gridStatus.currentStatusMsg = GridSubmitController.INTERNAL_ERROR;
            gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
                
                // Save in session for status update request for this job.
           request.getSession().setAttribute("gridStatus", gridStatus);
           mav.addObject("success", false);
           return mav;
        }
		
        if (credential == null) {
            //final String errorString = "Invalid grid credentials!";
            logger.error(GridSubmitController.CREDENTIAL_ERROR);
            gridStatus.currentStatusMsg = GridSubmitController.CREDENTIAL_ERROR;
            gridStatus.jobSubmissionStatus = JobSubmissionStatus.Failed;
            
            // Save in session for status update request for this job.
            request.getSession().setAttribute("gridStatus", gridStatus);
            //mav.addObject("error", errorString);
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
        	//Reduce our list of input files to an array of urls
        	List<GeodesyGridInputFile> gpsFiles = (List<GeodesyGridInputFile>)request.getSession().getAttribute("gridInputFiles");
            if (gpsFiles == null) {
            	logger.warn("gridInputFiles is null, using empty list instead");
            	gpsFiles = new ArrayList<GeodesyGridInputFile>();
            }
            
            String[] urlArray = new String[gpsFiles.size()];
            int urlArrayIndex = 0;
            for (GeodesyGridInputFile ggif : gpsFiles) {
            	urlArray[urlArrayIndex++] = ggif.getFileUrl();
            }
    		
    		//Transfer job input files to Grid StageInURL
    		//if(urlArray.length > 0){
        	//	gridStatus = urlCopy(urlArray, request);
    		//}    		
    		    		
    		if(gridStatus.jobSubmissionStatus != JobSubmissionStatus.Failed){
    			
                job.setSeriesId(series.getId());
                //job.setArguments(new String[] { job.getScriptFile() });
                logger.info("args count: "+job.getArguments().length);
                job.setJobType(job.getJobType().replace(",", ""));
                JSONArray args = JSONArray.fromObject(request.getParameter("arguments"));
                logger.info("Args in Json : "+args.toArray().length);
                job.setArguments((String[])args.toArray(new String [args.toArray().length]));
                
                // Create a new directory for the output files of this job
                //String certDN = (String)request.getSession().getAttribute("certDN");
                String certDN_DIR = "";
                try {
                    GSSCredential cred = (GSSCredential)credential;
                    certDN_DIR = cred.getName().toString().replaceAll("=", "_").replaceAll("/", "_").replaceAll(" ", "_").substring(1);//certDN.replaceAll("=", "_").replaceAll(" ", "_").replaceAll(",", "_");
                    
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
            		
            		//Transfer job input files to Grid StageInURL
            		if(urlArray != null && urlArray.length > 0){
                		// Full URL
                		// e.g. "gsiftp://pbstore.ivec.org:2811//pbstore/au01/grid-auscope/Abdi.Jama@csiro.au-20091103_163322/"
                		//       +"rinex/" + filename
                		String toURL = gridAccess.getGridFtpServer()+File.separator+ jobInputDir 
 		               +GridSubmitController.RINEX_DIR+File.separator;
                		gridStatus = urlCopy(urlArray, request, toURL);
            		}
            		
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
                		String[] params = createSubjobs(strDateFrom, strDateTo, job.getArguments()[0], request, gpsFiles);
                		
                		//overwrite job args
                		job.setArguments(params);
                		String localStageInURL = gridAccess.getLocalGridFtpServer()+localJobInputDir;
                        job.setInTransfers(new String[]{localStageInURL});
                        gridStatus = (GridTransferStatus)request.getSession().getAttribute("gridStatus");
                	}
                	else
                	{
                		if(urlArray != null && urlArray.length > 0){
                    		// Full URL
                    		// e.g. "gsiftp://pbstore.ivec.org:2811//pbstore/au01/grid-auscope/Abdi.Jama@csiro.au-20091103_163322/"
                    		//       +"rinex/" + filename
                			String toURL = gridAccess.getGridFtpServer()+File.separator+ jobInputDir 
      		               +GridSubmitController.RINEX_DIR+File.separator;
                    		gridStatus = urlCopy(urlArray, request, toURL );
                		}
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
    private String[] createSubjobs(String strDateFrom, String strDateTo, String param, 
    		                    HttpServletRequest request, List<GeodesyGridInputFile> gpsFiles){
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    	List<String> paramList = new ArrayList<String>();
    	// Save in session to use it when submitting job
        String jobInputDir = (String)request.getSession().getAttribute("jobInputDir");

    	//Use the first parameter from GUI as template for generating param for each day in the date range
        String paramTemp = param.substring(param.indexOf("-expt", 0), param.length());

    	try
        {
            Date dateFrom = df.parse(strDateFrom);
            Date dateTo = df.parse(strDateTo);
            
            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(dateFrom);
            
            Calendar calTo = Calendar.getInstance();
            calTo.setTime(dateTo);
            
            //String gpsFiles = (String)request.getSession().getAttribute("gridInputFiles");	
            //List<String> urlsList = GeodesyUtil.getSelectedGPSFiles(gpsFiles);
            
            //while dateFrom is less than or equal to dateTo
            int jobCount = 0;
            while((calFrom.compareTo(calTo))<= 0 ){
                //TO-DO check if this subJob has renix files available
            	int year = calFrom.get(Calendar.YEAR);
            	int doy = calFrom.get(Calendar.DAY_OF_YEAR);
            	String strParam = "-d "+year+" "+doy+" "+paramTemp;
            	paramList.add(strParam);
            	
            	String[] rinexOfDay = getRinexFilesOfDate(calFrom, gpsFiles);
            	if(rinexOfDay.length > 0)
            	{
            		//First create subJob/rinex/ directories path for this subJob
            		//Then urlcopy relevent rinex files for this sub job
            		String subJobId = "subJob_"+jobCount;
            		String subJobDir = jobInputDir+subJobId+File.separator;
            		if(createGridDir(request, subJobDir)){
            			String rinexSubJobDir = subJobDir+GridSubmitController.RINEX_DIR+File.separator;
            			if(createGridDir(request, rinexSubJobDir)){
                    		// Full URL
                    		// e.g. "gsiftp://pbstore.ivec.org:2811//pbstore/au01/grid-auscope/Abdi.Jama@csiro.au-20091103_163322/"
                    		//       +"rinex/" + filename
                			String toURL = gridAccess.getGridFtpServer()+File.separator+rinexSubJobDir;
                			urlCopy(rinexOfDay, request, toURL);
                			
                        	Hashtable subJobStageInDir = (Hashtable) request.getSession().getAttribute("subJobStageInDir");
                        	
                        	if(subJobStageInDir == null)
                        		subJobStageInDir = new Hashtable();
                        	
                        	if(!subJobStageInDir.containsKey(subJobId)){
                        		String gridSubjobDir = gridAccess.getGridFtpServer()+subJobDir;
                        		subJobStageInDir.put(subJobId, gridSubjobDir);
                        		request.getSession().setAttribute("subJobStageInDir", subJobStageInDir);
                        		logger.info("Added gridStageInDir: "+gridSubjobDir);
                        	}           				
            			}
                    	/*Hashtable subJobStageInDir = (Hashtable) request.getSession().getAttribute("subJobStageInDir");
                    	
                    	if(subJobStageInDir == null)
                    		subJobStageInDir = new Hashtable();
                    	
                    	if(!subJobStageInDir.containsKey(subJobId)){
                    		String gridSubjobDir = gridAccess.getGridFtpServer()+subJobDir;
                    		subJobStageInDir.put(subJobId, gridSubjobDir);
                    		request.getSession().setAttribute("subJobStageInDir", subJobStageInDir);
                    		logger.info("Added gridStageInDir: "+gridSubjobDir);
                    	}*/
            		}
            	}else
            	{
            		logger.info("No rinex files found for this day: "+year+"-"+doy);
            	}
            	
            	calFrom.add(Calendar.DATE, 1);
            	jobCount++;
            	logger.debug("Added param: "+strParam);
            }                        
        } 
    	catch (ParseException e)
        {
    		//do we need to pass this to the gui
    		logger.error("Error casting date: "+e.getMessage());
        }
    	String[] paramArray = new String[paramList.size()];
		paramArray = paramList.toArray(paramArray);
    	return paramArray;
    }

    
    private String[] getRinexFilesOfDate(Calendar currentDate, List<GeodesyGridInputFile> gpsFiles){

    	List<String> rinexFilesOfDate = new ArrayList<String>();
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    	Calendar fileDate = Calendar.getInstance();
    	for (GeodesyGridInputFile rinexUrl : gpsFiles){
    		try{
        		Date date = df.parse(rinexUrl.getFileDate());        		
        		fileDate.setTime(date);
	            
	            //now check if the file if for the current date, we don't care about which station
	            if(currentDate.compareTo(fileDate)==0){
	            	logger.debug("Date: "+currentDate.get(Calendar.YEAR)+"-"+currentDate.get(Calendar.DAY_OF_YEAR)+" is matched to rinex file: "+rinexUrl.getFileUrl());
	            	rinexFilesOfDate.add(rinexUrl.getFileUrl());
	            	//TO-DO it would be nice for performance if we could remove processed urls from the list
	            	//somehow this fails.
	            	//gpsFiles.remove(rinexUrl);
	            }
            } catch (Exception e)
            {
        		//do we need to pass this to the gui
        		logger.error("Error casting date of the rinex file: "+e.getMessage());
            }
    		
    	}
    	
    	String[] rinexArray = new String[rinexFilesOfDate.size()];
		rinexArray = rinexFilesOfDate.toArray(rinexArray);
    	return rinexArray;
    }
    
   /* private String[] getRinexFilesOfDate(int year, int doy, List<GeodesyGridInputFile> gpsFiles){

    	List<String> rinexFilesOfDate = new ArrayList<String>();
    	String filename = null;
    	for (GeodesyGridInputFile rinexUrl : gpsFiles){
    		try{
    			URL url = new URL(rinexUrl.getFileUrl());
    			filename = (new File(url.getPath())).getName();
    		}catch (Exception e){
    			//do we need to pass this to the gui
    			logger.error("Not valid url: "+e.getMessage());
    		}
    		
			if(filename != null)
			{
		        Matcher matcher = RINEX_FILENAME_PATTERN.matcher(filename);
		        
		        //the rinex file matches the pattern
		        if (matcher.matches()) {
		            int rinex_doy = Integer.parseInt(matcher.group(2));
		            int rinex_year = Integer.parseInt(matcher.group(3));

		            if (year < 90) {
		                rinex_year += 2000;
		            } else {
		                rinex_year += 1900;
		            }
		            
		            logger.debug("doy= ");
		            
		            //now check if the file if for the current date, we don't care about which station
		            if(year == rinex_year && doy == rinex_doy){
		            	logger.debug("Date"+year+"-"+doy+"is matched to rinex file: "+filename);
		            	rinexFilesOfDate.add(rinexUrl.getFileUrl());
		            	gpsFiles.remove(rinexUrl);
		            }
		        }else
		        {
		        	logger.debug("Rinex File name not in expected form: "+filename);
		        }
			}    		
    	}
    	
    	String[] rinexArray = new String[rinexFilesOfDate.size()];
		rinexArray = rinexFilesOfDate.toArray(rinexArray);
    	return rinexArray;
    }*/
    
    
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
        String name = "GeodesyJob";
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

        success = createGridDir(request, jobInputDir+GridSubmitController.RINEX_DIR+File.separator);
        if(!success){
        	logger.error("Setting up Grid Rinex StageIn directory failed.");
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
     * This function replaces the sites.defaults file in the specified directory
     * With a new file (based on a hardcoded template and the site's selected).
     * 
     * @param request
     * @param stageInDirectory 
     * @param job Will have its arguments parameter parsed for an -expt option
     * @return
     */
    private boolean templateSitesDefaults(HttpServletRequest request, String stageInDirectory, GeodesyJob job) {
    	String[] argumentList = job.getArguments();
    	if (argumentList == null || argumentList.length < 1) {
    		logger.warn("No job arguments specified");
    		return false;
    	}
    	
    	//Current strategy - Grab the FIRST argument line and parse out the value of the -expt option
    	String[] arguments = argumentList[0].split(" ");
    	String experimentName = null;
    	for (int i = 0; i < arguments.length; i++) {
    		if (arguments[i].equals("-expt") && i < (arguments.length - 1)) {
    			experimentName = arguments[i + 1];
    		}
    	}
    	
    	if (experimentName == null) {
    		logger.warn("No -expt option specified");
    		return false;
    	}
    	
    	return templateSitesDefaults(request, stageInDirectory, experimentName); 
    }
    
    /**
     * This function replaces the sites.defaults file in the specified directory
     * With a new file (based on a hardcoded template and the site's selected).
     * @return
     */
    private boolean templateSitesDefaults(HttpServletRequest request, String stageInDirectory, String experimentName) {
    	boolean success = true;
    	
    	FileWriter fw = null;
    	try {
    		fw = new FileWriter(stageInDirectory + "sites.defaults");

    		//We have a list of files each with a different station ID, we want only the list of station ID's (No Duplicates)
    		Map<String, String> stationMap = new HashMap<String, String>();
    		List<GeodesyGridInputFile> ggifs = (List<GeodesyGridInputFile>) request.getSession().getAttribute("gridInputFiles");
	    	for (GeodesyGridInputFile ggif : ggifs) {
	    		stationMap.put(ggif.getStationId(), null);
	    	}
	    	
	    	//Now iterate our map for the list of non duplicated station ID's
	    	for (String stationName : stationMap.keySet()) {
	    		fw.write(String.format("  %1$2s_gps  %2$2s localrx xstinfo  \n", stationName, experimentName));
	    	}
	    	
    	} catch (Exception ex) {
    		logger.error(ex);
    		success = false;
    	} finally {
    		if (fw != null) {
				try {
					fw.close();
				} catch (IOException ioex) {
					logger.error("Error closing: " + ioex);
					success = false;
				}
    		}
    	}
    	
    	return success;
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
        
        //create rinex directory.
        success = (new File(jobInputDir+GridSubmitController.RINEX_DIR+File.separator)).mkdir();

        
        //tables files.
        success = Util.copyFilesRecursive(new File(GridSubmitController.PRE_STAGE_IN_TABLE_FILES),
        		                new File(jobInputDir+GridSubmitController.TABLE_DIR+File.separator));

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

        if(fileType.equals(GridSubmitController.RINEX_DIR)){
            //create rinex directory for the subJob.
            File subJobRinexDir = new File(subJobInputDir+GridSubmitController.RINEX_DIR+File.separator);
            success = subJobRinexDir.exists();
            if(!success){
            	success = subJobRinexDir.mkdir();
            }     	
        }
        else{
            //create tables directory for the subJob.
            File subJobTablesDir = new File(subJobInputDir+GridSubmitController.TABLE_DIR+File.separator);
            success = subJobTablesDir.exists();
            if(!success){
                success = subJobTablesDir.mkdir();        	
            }	
        }
        
        if (!success) {
            logger.error("Could not create local subJobStageIn directories ");
        }
        
        return success;
	}
	/** 
	 * urlCopy
	 * 
     * Copy data to the Grid Storage using URLCopy.  
     * This is method which does authentication, remote create directory 
     * and files copying
     *
     * @param fromURLs	an array of URLs to copy to the storage
     * 
     * @return          GridTransferStatus of files copied
     * 
     */

	/** 
	 * urlCopy
	 * 
     * Copy data to the Grid Storage using URLCopy.  
     * This is method which does authentication, remote create directory 
     * and files copying
     *
     * @param fromURLs	an array of URLs to copy to the storage
     * 
     * @return          GridTransferStatus of files copied
     * 
     */
	private GridTransferStatus urlCopy(String[] fromURLs, HttpServletRequest request, String toURL) {

		Object credential = request.getSession().getAttribute("userCred");		
		String jobInputDir = (String) request.getSession().getAttribute("jobInputDir");				
        GridTransferStatus status = new GridTransferStatus();
		
		if( jobInputDir != null )
		{
			for (int i = 0; i < fromURLs.length; i++) {
				// StageIn to Grid etc
				int rtnValue = urlCopy(fromURLs[i], credential, jobInputDir, status, toURL );
				//This means time-out issue exception, so retry 2 more times.
				if(rtnValue == 1){
					logger.info("UrlCopy timed-out retry 2 for: " + fromURLs[i]);
					rtnValue = urlCopy(fromURLs[i], credential, jobInputDir, status, toURL );
					if(rtnValue == 1){
						logger.info("UrlCopy timed-out retry 3 for: " + fromURLs[i]);
						rtnValue = urlCopy(fromURLs[i], credential, jobInputDir, status, toURL );
						if(rtnValue == 1){
							status.numFileCopied = i;
							status.currentStatusMsg = GridSubmitController.FILE_COPY_ERROR;
							status.jobSubmissionStatus = JobSubmissionStatus.Failed;
							// Save in session for status update request for this job.
					        request.getSession().setAttribute("gridStatus", status);
					        logger.error("UrlCopy retry timed-out for: " + fromURLs[i]);
							break;
						}
					}
				}
				
				//This means bad exception like network error, so quit.
				if(rtnValue == 2){
					request.getSession().setAttribute("gridStatus", status);
					break;
				}
				
				status.numFileCopied++;
				status.currentStatusMsg = GridSubmitController.FILE_COPIED + status.numFileCopied
				+" of "+fromURLs.length+" files transfered.";
				logger.debug("Copy " + status.numFileCopied + " of " + fromURLs.length);
				
				// Save in session for status update request for this job.
		        request.getSession().setAttribute("gridStatus", status);															
			}
		}
		
		return status;
	}

	/**
     * Copy file to the Grid Storage using URLCopy.  
     * This is method which does authentication and transfers the file.
	 * @param fileUri The file to transfer
	 * @param credential Credential to use
	 * @param jobInputDir Path to copy to
	 * @param status holds status of the transfer
	 * @return
	 */
    private int urlCopy(String fileUri, Object credential, String jobInputDir, GridTransferStatus status, String toURL ) 
    {
        int rtnValue = 0;
        try {
    		GlobusURL from = new GlobusURL(fileUri.replace("http://files.ivec.org/geodesy/", "gsiftp://pbstore.ivec.org:2811//pbstore/cg01/geodesy/ftp.ga.gov.au/"));
    		logger.debug("fromURL is: " + from.getURL());
    		
    		String fullFilename = new URL(fileUri).getFile();

    		// Extract just the filename
    		String filename = new File(fullFilename).getName();	
    		status.file = filename;   		

    		toURL = toURL + filename;
    		GlobusURL to = new GlobusURL(toURL);		
    		logger.debug("toURL is: " + to.getURL());
    		
    		//Not knowing how long UrlCopy will take, the UI request status update of 
    		//file transfer periodically
    		UrlCopy uCopy = new UrlCopy();
    		uCopy.setCredentials((GSSCredential)credential);
    		uCopy.setDestinationUrl(to);
    		uCopy.setSourceUrl(from);
    		// Disables usage of third party transfers, for grid security reasons.
    		uCopy.setUseThirdPartyCopy(false); 	
    		uCopy.copy();   		
    	} catch (UrlCopyException e) {
    		logger.info("UrlCopy timed-out: " + e.getMessage());
    		rtnValue = 1;
    	} catch (Exception e) {
    		logger.error("Error: " + e.getMessage());
    		status.currentStatusMsg = GridSubmitController.INTERNAL_ERROR;
    		status.jobSubmissionStatus = JobSubmissionStatus.Failed;
    		rtnValue = 2;
    	}    	   
        return rtnValue;
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
	 * function that moves local GPS files at ivec to a separate list.
	 * @param list of selected GPS files
	 * @return list of local GPS files.
	 */
	private List<String> getLocalGPSFiles(List<String> list){
		List<String> ivecList = new ArrayList<String>();
		for(String fileName : list){
			if (fileName.contains(".ivec.org")){
				ivecList.add(convertFilePathToIvec(fileName));
				//The file can not be in two list
				list.remove(fileName);				
			}
		}		
		return ivecList;
	}

	/**
	 * 
	 * @param fileName file which to change it's path name
	 * @return
	 */
	private String convertFilePathToIvec(String fileName){
		//replace "http://files.ivec.org/geodesy/"  
		//with "gsiftp://pbstore.ivec.org:2811//pbstore/cg01/geodesy/ftp.ga.gov.au/gpsdata/"
		return fileName.replace(IVEC_MIRROR_URL, gridAccess.getGridFtpServer()+PBSTORE_RINEX_PATH);
	}

	/**
	 * function that checks the file type is rinex or not
	 * @param fileName
	 * @return
	 */
	private String checkFileType(String fileName){
	  String rtnValue = null;
	  if(fileName.trim().toLowerCase().endsWith(".z")){
		  logger.debug("file is of rinex.");
		  rtnValue = GridSubmitController.RINEX_DIR;
	  }else{
		  logger.debug("file is of tables.");
		  rtnValue = GridSubmitController.TABLE_DIR;
	  }
	  return rtnValue;
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
     * This method using GridFTP Client returns directory list of stageOut directory 
     * and sub directories.
     * @param fullDirname
     * @param credential
     * @return
     */
	private FileInformation[] getDirectoryListing(String fullDirname, Object credential){
		GridFTPClient gridStore = null;
		FileInformation[] fileDetails = new FileInformation[0];
		try {
			gridStore = new GridFTPClient(gridAccess.getRepoHostName(), gridAccess.getRepoHostFTPPort());		
			gridStore.authenticate((GSSCredential)credential); //authenticating
			gridStore.setDataChannelAuthentication(DataChannelAuthentication.SELF);
			gridStore.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
			logger.debug("Change to Grid StageOut dir:"+fullDirname);
			gridStore.changeDir(fullDirname);
			logger.debug("List files in StageOut dir:"+gridStore.getCurrentDir());
			gridStore.setType(GridFTPSession.TYPE_ASCII);
			gridStore.setPassive();
			gridStore.setLocalActive();
			
			Vector list = gridStore.list("*");

			if (list != null && !(list.isEmpty())) {
				fileDetails = new FileInformation[list.size()];
				for (int i = list.size() - 1; i >= 0; i--) {
					FileInfo fInfo = (FileInfo) list.get(i);
		            fileDetails[i] = new FileInformation(
		            		fInfo.getName(), fInfo.getSize(), fullDirname, fInfo.isDirectory());
				}                    
			} 
		} catch (ServerException e) {
			logger.error("GridFTP ServerException: " + e.getMessage());
		} catch (IOException e) {
			logger.error("GridFTP IOException: " + e.getMessage());
		} catch (Exception e) {
			logger.error("GridFTP Exception: " + e.getMessage());
		}
		finally{
			try{
				if(gridStore != null)
					gridStore.close();
			}catch (Exception e) {
				logger.error("GridFTP Exception: " + e.getMessage());
			}
		}
		return fileDetails;
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
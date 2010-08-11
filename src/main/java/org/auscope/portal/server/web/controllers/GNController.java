package org.auscope.portal.server.web.controllers;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.ICSWMethodMaker;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.auscope.portal.server.gridjob.GeodesyJobManager;
import org.auscope.portal.server.gridjob.GeodesyRecordInfo;
import org.auscope.portal.server.gridjob.GeodesySeries;
import org.auscope.portal.server.gridjob.PrepareCSWTransactionRecord;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.util.Util;
import org.auscope.portal.server.web.security.User;
import org.auscope.portal.server.web.security.UserDao;
import org.auscope.portal.server.web.service.HttpServiceCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Controller that handles insert records into Geonetwork request.
 *
 * @author Abdi Jama
 * Date: 13/01/2010
 * Time: 10:18:21 AM
 */
@Controller
public class GNController {
    protected final Log logger = LogFactory.getLog(getClass().getName());
    private HttpServiceCaller serviceCaller;
    private String serviceUrl = null;
    @Autowired
    @Qualifier(value = "propertyConfigurer")
    private PortalPropertyPlaceholderConfigurer hostConfigurer;
    
    @Autowired
    private GeodesyJobManager jobManager;
    
    @Autowired
    private UserDao userDao;
    
    @Autowired
    public GNController(HttpServiceCaller serviceCaller) {
        this.serviceCaller = serviceCaller;
    }

    /**
     * Request that inserts details of the job into GeoNetwork
     * @param jobId
     * @param request
     * @return JasonArray with success set to true or false.
     * @throws Exception
     */
    @RequestMapping("/insertRecord.do")
    public ModelAndView insertRecord(@RequestParam("jobId") final String jobId,
                                           HttpServletRequest request) throws Exception {
    	ModelAndView mav = new ModelAndView("jsonView");
    	
    	
		GeodesyJob job = jobManager.getJobById(Integer.parseInt(jobId));
		GeodesySeries jobSeries = jobManager.getSeriesById(job.getSeriesId());
		final User user = userDao.get((String)request.getSession().getAttribute("Shib-Person-mail"));
		
		if(user == null)
		{
			mav.addObject("error", "User is not registered on Portal");
			mav.addObject("success", false);
			return mav;
		}
		
    	//final String data = null;//createRecord();
    	String output = job.getOutputDir().substring(job.getOutputDir().indexOf("grid-auscope"), job.getOutputDir().length());
    	GeodesyRecordInfo info = new GeodesyRecordInfo(jobSeries.getUser(), jobSeries.getUser(), "idp.ivec.org",
    			jobSeries.getName(), job.getSubmitDate(), job.getDescription(), "keyWord", "Australia", "http://files.ivec.org/"+output, 112,
			154, -44, -9, job.getExtraJobDetails());
    	try{
    		
        	InputStream inFile = request.getSession().getServletContext().getResourceAsStream("/WEB-INF/xml/GNRecord.xml");
        	final PrepareCSWTransactionRecord tempRecord = new PrepareCSWTransactionRecord(info, inFile);
    	
        	if (tempRecord.isRecordLoaded())
        	{
                serviceUrl =
                	hostConfigurer.resolvePlaceholder("HOST.geodesy.csw");
                //Need to share same HttpClient for login and insert requests.
                
            	HttpClient httpClient = serviceCaller.getHttpClient();
            	// Logout first
            	// The url is like this "http://hostname/geonetwork/srv/en/xml.user.logout"
                String gnResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
                    public HttpMethodBase makeMethod() {
                    	GetMethod method = new GetMethod(serviceUrl.replaceFirst("csw", "xml.user.logout"));
                        return method;
                    }
                }.makeMethod(), httpClient);
                logger.debug("Logout in response: "+gnResponse);
                
                // Login
                // Login url is like this "http://hostname/geonetwork/srv/en/xml.user.login"
                gnResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
                    public HttpMethodBase makeMethod() {
                    	GetMethod method = new GetMethod(serviceUrl.replaceFirst("csw", "xml.user.login"));

                        //set all of the parameters
                        NameValuePair username = new NameValuePair("username", user.getId());
                        NameValuePair password = new NameValuePair("password", user.getPassword());

                        //attach them to the method
                        method.setQueryString(new NameValuePair[]{username, password});

                        return method;
                    }
                }.makeMethod(), httpClient);
                logger.debug("Login in response: "+gnResponse);
                logger.debug("Record to insert: "+tempRecord.createGNRecord());
                //Insert record
                gnResponse = serviceCaller.getMethodResponseAsString(new ICSWMethodMaker() {
                    public HttpMethodBase makeMethod() {
                    	PostMethod method = new PostMethod(serviceUrl);
                    	method.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                    	method.setRequestHeader("Content-Type", "application/xml; charset=UTF-8");
                    	try{
                    	    method.setRequestEntity(new StringRequestEntity(tempRecord.createGNRecord(), "application/xml", "UTF-8"));
                    	}catch(Exception e){
                    		logger.error("Insert record failed: "+e.getMessage());
                    	}
                        //attach them to the method
                        //method.setQueryString(new NameValuePair[]{service, version, request, transactionType, record});

                        return method;
                    }
                }.makeMethod(), httpClient);
                //check response
                String response = checkInsertResponse(gnResponse);
                if(response != ""){
                	//TODO get the GN url from config file
                	//update job record
                	job.setRegistered(serviceUrl.replaceFirst("csw", "metadata.show?uuid="+response));
                	jobManager.saveJob(job);
                	mav.addObject("success", true );
                }else
                {
                	mav.addObject("success", false);
                }
                logger.debug("serviceUrl:"+serviceUrl);
                logger.debug("Insert in response: "+gnResponse);
        	}else{
        		logger.error("Can not create Geonetwork record for job: "+jobId);
        	}
    	}catch(Exception e){
    		mav.addObject("error", e.getMessage());
			mav.addObject("success", false);
    	}    	
        
        
        return mav;
    }

    /**
     * Returns the record id if insert was success otherwise empty string
     *  
     * @param response
     * @return
     */
    private String checkInsertResponse(String response){
    	String rtnValue = "";
    	if(response != null || response != ""){
    		Util util = new Util();
    		try{
    			Document doc = util.buildDomFromString(response);

    			NodeList insertNode = doc.getElementsByTagName("csw:totalInserted");
    			Node n1 = insertNode.item(0).getFirstChild();
    			if(n1.getNodeValue().equals("1")){
    				NodeList idNode = doc.getElementsByTagName("identifier");
    				Node n2 = idNode.item(0).getFirstChild();
    				rtnValue = n2.getNodeValue();
    				logger.debug("Insert response id: "+rtnValue);
    			}
    		}catch(Exception e){
        		 			
    		}    		
    	}
    	return rtnValue;
    }
}
/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.cloud.S3FileInformation;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.service.JobFileService;
import org.auscope.portal.server.web.service.JobStorageService;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.ProviderCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

/**
 * Controller for the job list view.
 *
 * @author Cihan Altinay
 * @author Abdi Jama
 * @author Josh Vote
 */
@Controller
public class JobListController extends BaseVEGLController  {

    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private VEGLJobManager jobManager;
    @Autowired
    @Qualifier(value = "propertyConfigurer")
    private PortalPropertyPlaceholderConfigurer hostConfigurer;
    @Autowired
    private JobStorageService jobStorageService;
    @Autowired
    private JobFileService jobFileService;
    
    private AmazonEC2 ec2;

    /**
     * Returns a JSON object containing a list of the current user's series.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         VEGLSeries objects.
     */
    @RequestMapping("/mySeries.do")
    public ModelAndView mySeries(HttpServletRequest request,
                                 HttpServletResponse response) {

        String user = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
        List<VEGLSeries> series = jobManager.querySeries(user, null, null);

        logger.debug("Returning " + series);
        return new ModelAndView("jsonView", "series", series);
    }
    
    /**
     * Delete the job given by its reference.
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the job was not found or can not be deleted.
     */
    @RequestMapping("/deleteJob.do")
    public ModelAndView deleteJob(HttpServletRequest request,
                                HttpServletResponse response) {
    	String userEmail = (String)request.getSession().getAttribute("openID-Email");
        String jobIdStr = request.getParameter("jobId");
        VEGLJob job = null;
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success = false;

        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        } else {
            logger.warn("No job ID specified!");
        }

        if (job == null) {
            final String errorString = "The requested job was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);

        } else {
            // check if current user is the owner of the job
            VEGLSeries s = jobManager.getSeriesById(job.getSeriesId());
            if (userEmail.equals(s.getUser())) {
                logger.info("Deleting job with ID "+jobIdStr);
                jobFileService.deleteStageInDirectory(job);
                jobManager.deleteJob(job);
                success = true;
            } else {
                logger.warn(userEmail+"'s attempt to kill "+
                        s.getUser()+"'s job denied!");
                mav.addObject("error", "You are not authorised to delete this job.");
            }
        }
        mav.addObject("success", success);

        return mav;
    }
    /**
     * delete all jobs of given series.
     *
     * @param request The servlet request including a seriesId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the series was not found in the job manager.
     */
    @RequestMapping("/deleteSeriesJobs.do")
    public ModelAndView deleteSeriesJobs(HttpServletRequest request,
                                       HttpServletResponse response) {
    	
    	String userEmail = (String)request.getSession().getAttribute("openID-Email");
        String seriesIdStr = request.getParameter("seriesId");
        List<VEGLJob> jobs = null;
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success = false;
        int seriesId = -1;

        if (seriesIdStr != null) {
            try {
                seriesId = Integer.parseInt(seriesIdStr);
                jobs = jobManager.getSeriesJobs(seriesId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing series ID!");
            }
        } else {
            logger.warn("No series ID specified!");
        }

        if (jobs == null) {
            final String errorString = "The requested series was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);

        } else {
            // check if current user is the owner of the series
            VEGLSeries s = jobManager.getSeriesById(seriesId);
            if (userEmail.equals(s.getUser())) {
                logger.info("Deleting jobs of series "+seriesIdStr);
                boolean jobsDeleted = true;
                for (VEGLJob job : jobs) {
                    String oldStatus = job.getStatus();
                    if (oldStatus.equals(GridSubmitController.STATUS_FAILED) || oldStatus.equals(GridSubmitController.STATUS_DONE) ||
                    		oldStatus.equals(GridSubmitController.STATUS_CANCELLED) || oldStatus.equals(GridSubmitController.STATUS_UNSUBMITTED)) {
                    	jobFileService.deleteStageInDirectory(job);
                        jobManager.deleteJob(job);
                        
                    }else{
                    	logger.debug("Skipping running job "+job.getId());
                    	if(jobsDeleted){
                    		jobsDeleted = false;
                    		mav.addObject("error", "Can not delete series, there are running jobs.");
                    	}        	
                    	continue;                  	
                    }
                }
                if(jobsDeleted){
                	logger.info("Deleting series "+seriesIdStr);
                	jobManager.deleteSeries(s);
                	logger.info("Deleted series "+seriesIdStr);
                	success = true;
                }else{
                	success = false;
                }
            } else {
                logger.warn(userEmail+"'s attempt to delete "+
                        s.getUser()+"'s jobs denied!");
                mav.addObject("error", "You are not authorised to delete the jobs of this series.");
            }
        }

        mav.addObject("success", success);
        return mav;
    }
    
    /**
     * Kills the job given by its reference.
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the job was not found in the job manager.
     */
    @RequestMapping("/killJob.do")
    public ModelAndView killJob(HttpServletRequest request,
                                HttpServletResponse response) {

    	String userEmail = (String)request.getSession().getAttribute("openID-Email");
        String jobIdStr = request.getParameter("jobId");
        VEGLJob job = null;
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success = false;

        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        } else {
            logger.warn("No job ID specified!");
        }

        if (job == null) {
            final String errorString = "The requested job was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);

        } else {
            // check if current user is the owner of the job
            VEGLSeries s = jobManager.getSeriesById(job.getSeriesId());
            if (userEmail.equals(s.getUser())) {
                logger.info("Cancelling job with ID "+jobIdStr);
                
                // terminate the EMI instance
                try {
					terminateInstance(request, job);
	                success = true;
	                
				} catch (AmazonServiceException e) {
					final String errorString = "Failed to terminate instance with id: " + job.getEc2InstanceId();
					logger.error(errorString);
					mav.addObject(errorString);
				} 
            } else {
                logger.warn(userEmail+"'s attempt to kill "+
                        s.getUser()+"'s job denied!");
                mav.addObject("error", "You are not authorised to cancel this job.");
            }
        }
        mav.addObject("success", success);

        return mav;
    }
    
    
	/**
	 * Terminates the instance of an EMI that was launched by a job.
	 * 
	 * @param request The HttpServletRequest
	 * @param job The job linked the to instance that is to be terminated
	 */
	private void terminateInstance(HttpServletRequest request, VEGLJob job) {
		
		if (ec2 == null) {
			AWSCredentials credentials = (AWSCredentials)request.getSession().getAttribute("AWSCred");
			ec2 = new AmazonEC2Client(credentials);
			ec2.setEndpoint(hostConfigurer.resolvePlaceholder("ec2.endpoint"));
		}
		
		TerminateInstancesRequest termReq = new TerminateInstancesRequest();
		ArrayList<String> instanceIdList = new ArrayList<String>();
		instanceIdList.add(job.getEc2InstanceId());
		termReq.setInstanceIds(instanceIdList);
		ec2.terminateInstances(termReq);
							
		job.setStatus("Cancelled");
		jobManager.saveJob(job);
	}

    /**
     * Kills all jobs of given series.
     *
     * @param request The servlet request including a seriesId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the series was not found in the job manager.
     */
    @RequestMapping("/killSeriesJobs.do")
    public ModelAndView killSeriesJobs(HttpServletRequest request,
                                       HttpServletResponse response) {

    	String userEmail = (String)request.getSession().getAttribute("openID-Email");
        String seriesIdStr = request.getParameter("seriesId");
        List<VEGLJob> jobs = null;
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success = false;
        int seriesId = -1;
        
        if (seriesIdStr != null) {
            try {
                seriesId = Integer.parseInt(seriesIdStr);
                jobs = jobManager.getSeriesJobs(seriesId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing series ID!");
            }
        } else {
            logger.warn("No series ID specified!");
        }

        if (jobs == null) {
            final String errorString = "The requested series was not found.";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);

        } else {
            // check if current user is the owner of the series
            VEGLSeries s = jobManager.getSeriesById(seriesId);
            if (userEmail.equals(s.getUser())) {
                logger.info("Cancelling jobs of series "+seriesIdStr);
                for (VEGLJob job : jobs) {
                    String oldStatus = job.getStatus();
                    if (oldStatus.equals("Failed") || oldStatus.equals("Done") ||
                            oldStatus.equals("Cancelled")) {
                        logger.debug("Skipping finished job "+job.getId());
                        continue;
                    }
                    logger.info("Cancelling job with ID "+job.getId());
                    
                    // terminate the EMI instance
                    try {
    					terminateInstance(request, job);
    	                success = true;
    	                
    				} catch (AmazonServiceException e) {
    					final String errorString = "Failed to terminate instance with id: " + job.getEc2InstanceId();
    					logger.error(errorString);
    					mav.addObject(errorString);
    				} 
                }
                success = true;
            } else {
                logger.warn(userEmail+"'s attempt to kill "+
                        s.getUser()+"'s jobs denied!");
                mav.addObject("error", "You are not authorised to cancel the jobs of this series.");
            }
        }

        mav.addObject("success", success);
        return mav;
    }

    /**
     * Returns a JSON object containing an array of files belonging to a
     * given job.
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a files attribute which is an array of
     *         FileInformation objects. If the job was not found in the job
     *         manager the JSON object will contain an error attribute
     *         indicating the error.
     */
    @RequestMapping("/jobFiles.do")
    public ModelAndView jobFiles(HttpServletRequest request,
                                 HttpServletResponse response) {

    	VEGLJob job = null;
        ModelAndView mav = new ModelAndView("jsonView");
        String jobIdStr = request.getParameter("jobId");
        logger.debug("jobIdStr: " + jobIdStr);
        int  totalItems = 0;
        
        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        } else {
            logger.warn("No job ID specified!");
        }
        
        if (job == null) {
            final String errorString = "The requested job was not found.";
            logger.error("The requested job was not found.");
            mav.addObject("error", errorString);
        } else if (job.getS3OutputBaseKey() != null) {
        	// get results file information to display in the Files tab
        	S3FileInformation[] fileDetails = null;
			try {
				fileDetails = jobStorageService.getOutputFileDetails(job);
				logger.info(fileDetails.length + " job files located");
			} catch (S3ServiceException e) {
				logger.warn("Error fetching output directory information.", e);
				mav.addObject("error", "Error fetching output directory information.");
			}
        	mav.addObject("files", fileDetails);
        }
        
    	return mav;
    }
    

    /**
     * Sends the contents of a job file to the client.
     *
     * @param request The servlet request including a jobId parameter and a
     *                filename parameter
     * @param response The servlet response receiving the data
     *
     * @return null on success or the joblist view with an error parameter on
     *         failure.
     */
    @RequestMapping("/downloadFile.do")
    public ModelAndView downloadFile(HttpServletRequest request,
                                     HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");
        String fileName = request.getParameter("filename");
        String key = request.getParameter("key");
        VEGLJob job = null;
        String errorString = null;

        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        }

        if (job != null && fileName != null) {
        	
        	logger.debug("Download " + key);
        	try {
				InputStream is = jobStorageService.getJobFileData(job, key);
					
				if (is != null) {
					response.setContentType("application/octet-stream");
				    response.setHeader("Content-Disposition",
				            "attachment; filename=\""+fileName+"\"");
				    
				    try {
				    	OutputStream out = response.getOutputStream();
				        int n;
				        byte[] buffer = new byte[1024];
				     
				        while ((n = is.read(buffer)) != -1) {
				        	out.write(buffer, 0, n);
				        }
				        
				        out.flush();
				        return null;
				        
				    } catch(IOException e) {
				    	errorString = new String("Could not send file: " +
				                e.getMessage());
				        logger.error(errorString);
				    } finally {
				    	IOUtils.closeQuietly(is);
				    }
				}
				else{
					logger.error("inputstream is null");
				}
			} catch (S3ServiceException e) {
				errorString = new String("Error creating S3Service: " +
		                e.getMessage());
		        logger.error(errorString);
			} catch (ServiceException e) {
				errorString = new String("Error getting S3Object data: " +
		                e.getMessage());
		        logger.error(errorString);
			}
        }

        // We only end up here in case of an error so return a suitable message
        if (errorString == null) {
            if (job == null) {
                errorString = new String("Invalid job specified!");
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
        return new ModelAndView("joblist", "error", errorString);
    }

    /**
     * Sends the contents of one or more job files as a ZIP archive to the
     * client.
     *
     * @param request The servlet request including a jobId parameter and a
     *                files parameter with the filenames separated by comma
     * @param response The servlet response receiving the data
     *
     * @return null on success or the joblist view with an error parameter on
     *         failure.
     */
    @RequestMapping("/downloadAsZip.do")
    public ModelAndView downloadAsZip(HttpServletRequest request,
                                      HttpServletResponse response) {

        String jobIdStr = request.getParameter("jobId");
        String filesParam = request.getParameter("files");
        logger.debug("filesParam: " + filesParam);
        VEGLJob job = null;
        String errorString = null;

        if (jobIdStr != null) {
            try {
                int jobId = Integer.parseInt(jobIdStr);
                job = jobManager.getJobById(jobId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing job ID!");
            }
        }

        if (job != null && filesParam != null) {
            String[] fileKeys = filesParam.split(",");
            logger.debug("Archiving " + fileKeys.length + " file(s) of job " +
                    jobIdStr);

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"jobfiles.zip\"");
            
            try {

                boolean readOneOrMoreFiles = false;
                ZipOutputStream zout = new ZipOutputStream(
                        response.getOutputStream());
                for (String fileKey : fileKeys) {                	
               		InputStream is = jobStorageService.getJobFileData(job, fileKey);
                    
                    byte[] buffer = new byte[16384];
                    int count = 0;
                    zout.putNextEntry(new ZipEntry(fileKey));
                    while ((count = is.read(buffer)) != -1) {
                        zout.write(buffer, 0, count);
                    }
                    zout.closeEntry();
                    readOneOrMoreFiles = true;
                }
                if (readOneOrMoreFiles) {
                    zout.finish();
                    zout.flush();
                    zout.close();
                    return null;

                } else {
                    zout.close();
                    errorString = new String("Could not access the files!");
                    logger.error(errorString);
                }

            } catch (IOException e) {
                errorString = new String("Could not create ZIP file: " +
                        e.getMessage());
                logger.error(errorString);
            } catch (ServiceException e) {
            	errorString = new String("Error getting S3Object data: " +
		                e.getMessage());
		        logger.error(errorString);
			}
        }

        // We only end up here in case of an error so return a suitable message
        if (errorString == null) {
            if (job == null) {
                errorString = new String("Invalid job specified!");
                logger.error(errorString);
            } else if (filesParam == null) {
                errorString = new String("No filename(s) provided!");
                logger.error(errorString);
            } else {
                // should never get here
                errorString = new String("Something went wrong.");
                logger.error(errorString);
            }
        }
        return new ModelAndView("joblist", "error", errorString);
    }

    /**
     * Returns a JSON object containing an array of series that match the query
     * parameters.
     *
     * @param request The servlet request with query parameters
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         VEGLSeries objects matching the criteria.
     */
    @RequestMapping("/querySeries.do")
    public ModelAndView querySeries(HttpServletRequest request,
                                    HttpServletResponse response) {

        String qUser = request.getParameter("qUser");
        String qName = request.getParameter("qSeriesName");
        String qDesc = request.getParameter("qSeriesDesc");

        if (qUser == null && qName == null && qDesc == null) {
            qUser = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
            logger.debug("No query parameters provided. Will return "+qUser+"'s series.");
        }

        logger.debug("qUser="+qUser+", qName="+qName+", qDesc="+qDesc);
        List<VEGLSeries> series = jobManager.querySeries(qUser, qName, qDesc);

        logger.debug("Returning list of "+series.size()+" series.");
        return new ModelAndView("jsonView", "series", series);
    }

    /**
     * Attempts to creates a new series for the specified user.
     * 
     * The series object will be returned in a JSON response on success.
     * 
     * @param seriesName
     * @param seriesDescription
     * @return
     */
    @RequestMapping("/createSeries.do")
    public ModelAndView createSeries(HttpServletRequest request,
    								@RequestParam("seriesName") String seriesName,
						    		@RequestParam("seriesDescription") String seriesDescription) {
    	String openIdEmail = (String)request.getSession().getAttribute("openID-Email");
    	VEGLSeries series = new VEGLSeries();
    	series.setUser(openIdEmail);
    	series.setName(seriesName);
    	series.setDescription(seriesDescription);
    	
    	try {
    		jobManager.saveSeries(series);
    	} catch (Exception ex) {
    		logger.error("failure saving series", ex);
    		return generateJSONResponseMAV(false, null, "Failure saving series");
    	}
    	
    	return generateJSONResponseMAV(true, series, "");
    }
    
    /**
     * Returns a JSON object containing an array of jobs for the given series.
     *
     * @param request The servlet request including a seriesId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a jobs attribute which is an array of
     *         <code>VEGLJob</code> objects.
     */
    @RequestMapping("/listJobs.do")
    public ModelAndView listJobs(HttpServletRequest request,
                                 HttpServletResponse response) {

        String seriesIdStr = request.getParameter("seriesId");
        List<VEGLJob> seriesJobs = null;
        ModelAndView mav = new ModelAndView("jsonView");
        int seriesId = -1;

        if (seriesIdStr != null) {
            try {
                seriesId = Integer.parseInt(seriesIdStr);
                seriesJobs = jobManager.getSeriesJobs(seriesId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing series ID '"+seriesIdStr+"'");
            }
        } else {
            logger.warn("No series ID specified!");
        }
        
        // check to see if any active jobs have completed or failed so the status can be updated
        if (seriesJobs != null) {
        	for (VEGLJob job : seriesJobs) {
        		//Don't lookup files for jobs that haven't been submitted
        		if (!job.getStatus().equals(GridSubmitController.STATUS_UNSUBMITTED)) {
	        		S3FileInformation[] results = null;
					try {
						results = jobStorageService.getOutputFileDetails(job);
					} catch (S3ServiceException e) {
						logger.error("Unable to list output job files", e);
					}
	        		
	        		if (job.getStatus().equals(GridSubmitController.STATUS_ACTIVE) && results != null && results.length > 0) {
	        			// update status to done
	        			job.setStatus(GridSubmitController.STATUS_DONE);
	        			 
	        			// check if any errors occurred. Errors will be written to stderr.txt
	        			for (S3FileInformation result : results) {
	        				if (result.getName().endsWith("stderr.txt") && result.getSize() > 0) {
	        					// change status to failed
	        					job.setStatus(GridSubmitController.STATUS_FAILED);
	        					break;
	        				}
	        			}
	        			
	        			jobManager.saveJob(job);
	        		}
        		}
        	}
        	
        	mav.addObject("jobs", seriesJobs);
        }

        logger.debug("Returning series job list");
        return mav;
    }
    
    
}


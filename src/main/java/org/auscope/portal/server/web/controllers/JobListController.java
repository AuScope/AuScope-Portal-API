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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.server.security.oauth2.PortalUser;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.services.cloud.monitor.JobStatusException;
import org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.auscope.portal.server.vegl.VGLPollingJobQueueManager;
import org.auscope.portal.server.vegl.VGLQueueJob;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the job list view.
 *
 * @author Cihan Altinay
 * @author Abdi Jama
 * @author Josh Vote
 * @author Richard Goh
 */
@Controller
public class JobListController extends BaseCloudController  {

    /** The name of the log file that the job will use*/
    public static final String VGL_LOG_FILE = "vl.sh.log";

    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());

    private VEGLJobManager jobManager;
    private FileStagingService fileStagingService;
    private VGLJobStatusAndLogReader jobStatusLogReader;
    private JobStatusMonitor jobStatusMonitor;
    private VGLJobStatusChangeHandler vglJobStatusChangeHandler;
    private VGLPollingJobQueueManager vglPollingJobQueueManager;

    @Autowired
    public JobListController(VEGLJobManager jobManager, CloudStorageService[] cloudStorageServices,
            FileStagingService fileStagingService, CloudComputeService[] cloudComputeServices,
            VGLJobStatusAndLogReader jobStatusLogReader,
            JobStatusMonitor jobStatusMonitor,VGLJobStatusChangeHandler vglJobStatusChangeHandler,
            PortalPropertyPlaceholderConfigurer hostConfigurer,VGLPollingJobQueueManager vglPollingJobQueueManager) {
        super(cloudStorageServices, cloudComputeServices,hostConfigurer);
        this.jobManager = jobManager;
        this.fileStagingService = fileStagingService;
        this.jobStatusLogReader = jobStatusLogReader;
        this.jobStatusMonitor = jobStatusMonitor;
        this.vglPollingJobQueueManager =  vglPollingJobQueueManager;
        this.initializeQueue();
    }

    protected void initializeQueue() {
        try{

            if(vglPollingJobQueueManager.getQueue().hasJob()){
                //a fail safe catch all
                return;
            }
            List<VEGLJob> seriesJobs = jobManager.getInQueueJobs();
            for(VEGLJob curJob:seriesJobs){
                CloudComputeService cloudComputeService = getComputeService(curJob);
                String userDataString = null;
                userDataString = createBootstrapForJob(curJob);
                vglPollingJobQueueManager.addJobToQueue(new VGLQueueJob(jobManager,cloudComputeService,curJob,userDataString,vglJobStatusChangeHandler));
            }
        }catch(Exception e){
            logger.error("Error initializing job queue",e);
            e.printStackTrace();
        }

    }

    /**
     * Attempts to get a job with a particular ID. If the job ID does NOT belong to the current
     * user session null will be returned.
     *
     * This function will log all appropriate errors.
     * @param jobId
     * @return The VEGLJob object on success or null otherwise.
     */
    private VEGLJob attemptGetJob(Integer jobId, PortalUser user) {
        logger.info("Getting job with ID " + jobId);

        VEGLJob job = null;

        //Check we have a user email
        if (user == null || user.getEmail() == null) {
            logger.warn("The current session is missing an email attribute");
            return null;
        }

        //Attempt to fetch our job
        if (jobId != null) {
            try {
                job = jobManager.getJobById(jobId.intValue());
                logger.debug("Job [" + job.hashCode() + "] retrieved by jobManager [" + jobManager.hashCode() + "]");
            } catch (Exception ex) {
                logger.error(String.format("Exception when accessing jobManager for job id '%1$s'", jobId), ex);
                return null;
            }
        }

        if (job == null) {
            logger.warn(String.format("Job with ID '%1$s' does not exist", jobId));
            return null;
        }

        //Check user matches job
        if (!user.getEmail().equals(job.getUser())) {
            logger.warn(String.format("%1$s's attempt to fetch %2$s's job denied!", user, job.getUser()));
            return null;
        }

        return job;
    }

    /**
     * Attempts to get a series with a particular ID. If the series ID does NOT belong to the current
     * user session null will be returned.
     *
     * This function will log all appropriate errors.
     * @param jobId
     * @return The VEGLSeries object on success or null otherwise.
     */
    private VEGLSeries attemptGetSeries(Integer seriesId, PortalUser user) {
        VEGLSeries series = null;

        //Check we have a user email
        if (user == null || user.getEmail() == null) {
            logger.warn("The current session is missing an email attribute");
            return null;
        }

        //Attempt to fetch our job
        if (seriesId != null) {
            try {
                series = jobManager.getSeriesById(seriesId.intValue());
            } catch (Exception ex) {
                logger.error(String.format("Exception when accessing jobManager for series id '%1$s'", seriesId), ex);
                return null;
            }
        }

        if (series == null) {
            logger.warn(String.format("Series with ID '%1$s' does not exist", seriesId));
            return null;
        }

        //Check user matches job
        if (!user.getEmail().equals(series.getUser())) {
            logger.warn(String.format("%1$s's attempt to fetch %2$s's job denied!", user, series.getUser()));
            return null;
        }

        return series;
    }

    /**
     * Returns a JSON object containing a list of the current user's series.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         VEGLSeries objects.
     */
    @RequestMapping("/secure/mySeries.do")
    public ModelAndView mySeries(HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal PortalUser user) {

        if (user == null || user.getEmail() == null) {
            logger.warn("No email attached to session");
            return generateJSONResponseMAV(false, null, "No email attached to session");
        }
        List<VEGLSeries> series = jobManager.querySeries(user.getEmail(), null, null);

        logger.debug("Returning " + series);
        return generateJSONResponseMAV(true, series, "");
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
    @RequestMapping("/secure/deleteJob.do")
    public ModelAndView deleteJob(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") Integer jobId,
            @AuthenticationPrincipal PortalUser user) {
        logger.info("Deleting job with ID " + jobId);

        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The requested job was not found.");
        }

        String oldJobStatus = job.getStatus();
        job.setStatus(JobBuilderController.STATUS_DELETED);
        jobManager.saveJob(job);
        jobManager.createJobAuditTrail(oldJobStatus, job, "Job deleted.");
        // Failure here is NOT fatal - it will just result in some
        // residual files in staging directory and S3 cloud storage.
        cleanupDeletedJob(job);

        return generateJSONResponseMAV (true, null, "");
    }

    /**
     * delete all jobs of given series (and the series itself)
     *
     * @param request The servlet request including a seriesId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the series was not found in the job manager.
     */
    @RequestMapping("/secure/deleteSeriesJobs.do")
    public ModelAndView deleteSeriesJobs(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("seriesId") Integer seriesId,
            @AuthenticationPrincipal PortalUser user) {

        VEGLSeries series = attemptGetSeries(seriesId, user);
        if (series == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup series.");
        }

        List<VEGLJob> jobs = jobManager.getSeriesJobs(seriesId.intValue());
        if (jobs == null) {
            logger.warn(String.format("Unable to lookup jobs for series id '%1$s'", seriesId));
            return generateJSONResponseMAV(false, null, "Unable to lookup jobs of series.");
        }

        logger.info("Deleting jobs of series " + seriesId);
        for (VEGLJob job : jobs) {
            logger.debug(String.format("Deleting job %1$s",job));
            String oldJobStatus = job.getStatus();
            job.setStatus(JobBuilderController.STATUS_DELETED);
            jobManager.saveJob(job);
            jobManager.createJobAuditTrail(oldJobStatus, job, "Job deleted.");
            // Failure here is NOT fatal - it will just result in some
            // residual files in staging directory and S3 cloud storage.
            cleanupDeletedJob(job);
        }

        logger.info("Deleting series "+seriesId);
        jobManager.deleteSeries(series);

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Cleans up all the files for a deleted job from staging directory
     * and S3 cloud storage. Failure in cleaning up will not be propagated
     * back to the calling method.
     * @param job the job to be deleted.
     */
    private void cleanupDeletedJob(VEGLJob job) {
        try {
            // Remove files from staging directory
            fileStagingService.deleteStageInDirectory(job);
            // Remove files from S3 cloud storage if the job
            // hasn't been registered in GeoNetwork
            if (StringUtils.isEmpty(job.getRegisteredUrl())) {
                CloudStorageService cloudStorageService = getStorageService(job);
                if (cloudStorageService == null) {
                    logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cloud files (if any) will not be removed", job.getStorageServiceId(), job.getId()));
                } else {
                    cloudStorageService.deleteJobFiles(job);
                }
            }
        } catch (Exception ex) {
            logger.warn("Error cleaning up deleted job.", ex);
        }
    }

    /**
     * Kills or cancels the job given by its reference.
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute and an error attribute
     *         in case the job was not found in the job manager.
     */
    @RequestMapping("/secure/killJob.do")
    public ModelAndView killJob(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") Integer jobId,
            @AuthenticationPrincipal PortalUser user) {
        logger.info("Cancelling job with ID "+jobId);

        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job to kill.");
        }

        try {
            // we need to inform the user that the job cancelling is aborted
            // because the job has already been processed.
            if (job.getStatus().equals(JobBuilderController.STATUS_DONE)) {
                return generateJSONResponseMAV(false, null, "Cancelling of job aborted as it has already been processed.");
            }

            // terminate the EMI instance
            terminateInstance(job);
        } catch (Exception e) {
            logger.error("Failed to cancel the job.", e);
            return generateJSONResponseMAV(false, null, "There was a problem cancelling your job.",
                    "Please try again in a few minutes or report it to cg-admin@csiro.au.");
        }

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Terminates the instance of an EMI that was launched by a job.
     *
     * @param request The HttpServletRequest
     * @param job The job linked the to instance that is to be terminated
     */
    private void terminateInstance(VEGLJob job) {
        String oldJobStatus = job.getStatus();
        if (oldJobStatus.equals(JobBuilderController.STATUS_DONE) ||
                oldJobStatus.equals(JobBuilderController.STATUS_UNSUBMITTED)) {
            logger.debug("Skipping finished or unsubmitted job "+job.getId());
        }else if(oldJobStatus.equals(JobBuilderController.STATUS_INQUEUE)){
            VGLQueueJob dummyQueueJobForRemoval = new VGLQueueJob(null,null,job,"",null);
            vglPollingJobQueueManager.getQueue().remove(dummyQueueJobForRemoval);
            job.setStatus(JobBuilderController.STATUS_UNSUBMITTED);
            jobManager.saveJob(job);
            jobManager.createJobAuditTrail(oldJobStatus, job, "Job cancelled by user.");
        }else {
            try {
                // We allow the job to be cancelled and re-submitted regardless
                // of its termination status.
                job.setStatus(JobBuilderController.STATUS_UNSUBMITTED);
                jobManager.saveJob(job);
                jobManager.createJobAuditTrail(oldJobStatus, job, "Job cancelled by user.");
                CloudComputeService cloudComputeService = getComputeService(job);
                if (cloudComputeService == null) {
                    logger.error(String.format("No cloud compute service with id '%1$s' for job '%2$s'. Cloud VM cannot be terminated", job.getComputeServiceId(), job.getId()));
                } else {
                    cloudComputeService.terminateJob(job);
                }
            } catch (Exception e) {
                logger.warn("Failed to terminate instance with id: " + job.getComputeInstanceId(), e);
            }
        }
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
    @RequestMapping("/secure/killSeriesJobs.do")
    public ModelAndView killSeriesJobs(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("seriesId") Integer seriesId,
            @AuthenticationPrincipal PortalUser user) {

        VEGLSeries series = attemptGetSeries(seriesId, user);
        if (series == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup series.");
        }

        List<VEGLJob> jobs = jobManager.getSeriesJobs(seriesId.intValue());
        if (jobs == null) {
            logger.warn(String.format("Unable to lookup jobs for series id '%1$s'", seriesId));
            return generateJSONResponseMAV(false, null, "Unable to lookup jobs of series.");
        }

        //Iterate our jobs, terminating as we go (abort iteration on failure)
        for (VEGLJob job : jobs) {
            //terminate the EMI instance
            try {
                logger.info("Cancelling job with ID "+ job.getId());

                if (job.getStatus().equals(JobBuilderController.STATUS_DONE)) {
                    logger.info("Cancelling of job aborted as it has already been processed.");
                    continue;
                }

                // terminate the EMI instance
                terminateInstance(job);
            } catch (Exception e) {
                logger.error("Failed to cancel one of the jobs in a given series.", e);
                return generateJSONResponseMAV(false, null, "There was a problem cancelling one of your jobs in selected series.",
                        "Please try again in a few minutes or report it to cg-admin@csiro.au.");
            }
        }

        return generateJSONResponseMAV(true, null, "");
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
    @RequestMapping("/secure/jobFiles.do")
    public ModelAndView jobFiles(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") Integer jobId,
            @AuthenticationPrincipal PortalUser user) {
        logger.info("Getting job files for job ID " + jobId);

        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The requested job was not found.");
        }

        CloudFileInformation[] fileDetails = null;
        try {
            CloudStorageService cloudStorageService = getStorageService(job);
            if (cloudStorageService == null) {
                logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cloud files cannot be listed", job.getStorageServiceId(), job.getId()));
                return generateJSONResponseMAV(false, null, "No cloud storage service found for job");
            } else {
                fileDetails = cloudStorageService.listJobFiles(job);
                logger.info(fileDetails.length + " job files located");
            }
        } catch (Exception e) {
            logger.warn("Error fetching output directory information.", e);
            return generateJSONResponseMAV(false, null, "Error fetching output directory information");
        }

        return generateJSONResponseMAV(true, fileDetails, "");
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
    @RequestMapping("/secure/downloadFile.do")
    public ModelAndView downloadFile(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") Integer jobId,
            @RequestParam("filename") String fileName,
            @RequestParam("key") String key,
            @AuthenticationPrincipal PortalUser user) {

        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job object.");
        }

        logger.debug("Download " + key);

        //Get our Input Stream
        InputStream is = null;
        try {
            CloudStorageService cloudStorageService = getStorageService(job);
            if (cloudStorageService == null) {
                logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cloud file cannot be downloaded", job.getStorageServiceId(), job.getId()));
                return generateJSONResponseMAV(false, null, "No cloud storage service found for job");
            }
            is = cloudStorageService.getJobFile(job, key);
        } catch (Exception ex) {
            logger.warn(String.format("Unable to access '%1$s' from the cloud", key), ex);
            return generateJSONResponseMAV(false, null, "Unable to access file from the cloud");
        }

        //start writing our output stream
        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");

            //Ensure that our streams get closed
            OutputStream out = response.getOutputStream();
            try {

                int n;
                byte[] buffer = new byte[1024];

                while ((n = is.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }

                out.flush();
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(out);
            }
        } catch (Exception ex) {
            logger.warn("Error whilst writing to output stream", ex);
        }

        //The output is raw data down the output stream, just return null
        return null;
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
    @RequestMapping("/secure/downloadAsZip.do")
    public ModelAndView downloadAsZip(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") Integer jobId,
            @RequestParam("files") String filesParam,
            @AuthenticationPrincipal PortalUser user) {

        //Lookup our job and check input files
        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job object.");
        }

        CloudStorageService cloudStorageService = getStorageService(job);
        if (cloudStorageService == null) {
            logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cloud file cannot be downloaded as zip", job.getStorageServiceId(), job.getId()));
            return generateJSONResponseMAV(false, null, "No cloud storage service found for job");
        }

        logger.debug("filesParam: " + filesParam);
        if (filesParam == null || filesParam.isEmpty()) {
            return generateJSONResponseMAV(false, null, "No files have been selected.");
        }
        String[] fileKeys = filesParam.split(",");
        logger.debug("Archiving " + fileKeys.length + " file(s) of job " + jobId);

        //Create a filename that is semi-unique to the job (and slightly human readable)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String downloadFileName = String.format("jobfiles_%1$s_%2$s.zip", job.getName(), sdf.format(job.getSubmitDate()));
        downloadFileName = downloadFileName.replaceAll("[^0-9a-zA-Z_.]", "_");


        //Start writing our data to a zip archive (which is being streamed to user)
        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%1$s\"", downloadFileName));

            boolean readOneOrMoreFiles = false;
            ZipOutputStream zout = new ZipOutputStream(
                    response.getOutputStream());
            for (String fileKey : fileKeys) {
                InputStream is = cloudStorageService.getJobFile(job, fileKey);

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
                logger.warn("Could not access the files!");
            }

        } catch (IOException e) {
            logger.warn("Could not create ZIP file", e);
        } catch (Exception e) {
            logger.warn("Error getting cloudObject data", e);
        }

        return null;
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
    @RequestMapping("/secure/querySeries.do")
    public ModelAndView querySeries(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required=false, value="qSeriesName") String qName,
            @RequestParam(required=false, value="qSeriesDesc") String qDesc,
            @AuthenticationPrincipal PortalUser user) {

        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        //User can only query his/her own job series
        if (StringUtils.isEmpty(qName) && StringUtils.isEmpty(qDesc)) {
            logger.debug("No query parameters provided. Will return "+user+"'s series.");
        }

        logger.debug("qUser="+user.getEmail()+", qName="+qName+", qDesc="+qDesc);
        List<VEGLSeries> series = jobManager.querySeries(user.getEmail(), qName, qDesc);

        logger.debug("Returning list of "+series.size()+" series.");
        return generateJSONResponseMAV(true, series, "");
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
    @RequestMapping("/secure/createSeries.do")
    public ModelAndView createSeries(HttpServletRequest request,
            @RequestParam("seriesName") String seriesName,
            @RequestParam("seriesDescription") String seriesDescription,
            @AuthenticationPrincipal PortalUser user) {
        VEGLSeries series = new VEGLSeries();
        series.setUser(user.getEmail());
        series.setName(seriesName);
        series.setDescription(seriesDescription);

        try {
            jobManager.saveSeries(series);
        } catch (Exception ex) {
            logger.error("failure saving series", ex);
            return generateJSONResponseMAV(false, null, "Failure saving series");
        }

        return generateJSONResponseMAV(true, Arrays.asList(series), "");
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
    @RequestMapping("/secure/listJobs.do")
    public ModelAndView listJobs(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("seriesId") Integer seriesId,
            @RequestParam(required=false, value="forceStatusRefresh", defaultValue="false") boolean forceStatusRefresh,
            @AuthenticationPrincipal PortalUser user) {
        VEGLSeries series = attemptGetSeries(seriesId, user);
        if (series == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job series.");
        }

        List<VEGLJob> seriesJobs = jobManager.getSeriesJobs(seriesId.intValue());
        if (seriesJobs == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup jobs for the specified series.");
        }

        if (forceStatusRefresh) {
            try {
                jobStatusMonitor.statusUpdate(seriesJobs);
            } catch (JobStatusException e) {
                log.info("There was an error updating one or more jobs: " + e.getMessage());
                log.debug("Exception(s): ", e);
            }
        }

        return generateJSONResponseMAV(true, seriesJobs, "");
    }

    /**
     * Tests whether the specified cloud file appears in a list of fileNames
     *
     * If fileNames is null, true will be returned
     * @param files
     * @param fileName
     * @return
     */
    private boolean cloudFileIncluded(String[] fileNames, CloudFileInformation cloudFile) {
        if (fileNames == null) {
            return false;
        }

        for (String fileName : fileNames) {
            if (cloudFile.getName().endsWith(fileName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Duplicates the job given by its reference, the new job object is returned.
     *
     * Job files will be duplicated in LOCAL staging only. The files duplicated can be
     * controlled by a list of file names
     */
    @RequestMapping("/secure/duplicateJob.do")
    public ModelAndView duplicateJob(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") Integer jobId,
            @RequestParam(required=false, value="file") String[] files,
            @AuthenticationPrincipal PortalUser user) {
        logger.info("Duplicate a new job from job ID "+ jobId);

        //Lookup the job we are cloning
        VEGLJob oldJob = attemptGetJob(jobId, user);
        if (oldJob == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job to duplicate.");
        }

        CloudStorageService cloudStorageService = getStorageService(oldJob);
        if (cloudStorageService == null) {
            logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cannot duplicate", oldJob.getStorageServiceId(), oldJob.getId()));
            return generateJSONResponseMAV(false, null, "No cloud storage service found for job");
        }

        //Create a cloned job but make it 'unsubmitted'
        VEGLJob newJob = oldJob.safeClone();
        newJob.setSubmitDate((Date)null);
        newJob.setStatus(JobBuilderController.STATUS_UNSUBMITTED);
        newJob.setRegisteredUrl(null);

        //Attempt to save the new job to the DB
        try {
            jobManager.saveJob(newJob);
            //This needs to be set AFTER we first save the job (the ID will form part of the key)
            newJob.setStorageBaseKey(cloudStorageService.generateBaseKey(newJob));
            jobManager.saveJob(newJob);
        } catch (Exception ex) {
            log.error("Unable to save job to database: " + ex.getMessage(), ex);
            return generateJSONResponseMAV(false, null, "Unable to save new job.");
        }

        try {
            //Lets setup a staging area for the input files
            fileStagingService.generateStageInDirectory(newJob);
            //Write every file to the local staging area
            CloudFileInformation[] cloudFiles = cloudStorageService.listJobFiles(oldJob);
            for (CloudFileInformation cloudFile : cloudFiles) {
                if (cloudFileIncluded(files, cloudFile)) {
                    InputStream is = cloudStorageService.getJobFile(oldJob, cloudFile.getName());
                    OutputStream os = null;
                    try {
                        os = fileStagingService.writeFile(newJob, cloudFile.getName());

                        FileIOUtil.writeInputToOutputStream(is, os, 1024 * 1024, false);
                    } finally {
                        FileIOUtil.closeQuietly(os);
                        FileIOUtil.closeQuietly(is);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Unable to duplicate input files: " + ex.getMessage(), ex);
            //Tidy up after ourselves
            jobManager.deleteJob(newJob);
            // Tidy the stage in area (we don't need it any more - all files are replicated in the cloud)
            // Failure here is NOT fatal - it will just result in some residual files
            fileStagingService.deleteStageInDirectory(newJob);
            return generateJSONResponseMAV(false, null, "Unable to save new job.");
        }

        jobManager.createJobAuditTrail(null, newJob, "Job duplicated.");
        return generateJSONResponseMAV(true, Arrays.asList(newJob), "");
    }

    /**
     * Gets a pre parsed version of the internal logs. The resulting object will
     * contain the logs sectioned into 'named sections' eg: Section for python code, section for environment etc
     *
     * Will always contain a single section called "Full" containing the unsectioned original log
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/secure/getSectionedLogs.do")
    public ModelAndView getSectionedLogs(HttpServletRequest request, @RequestParam("jobId") Integer jobId, @AuthenticationPrincipal PortalUser user) {
        //Lookup the job whose logs we are accessing
        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The specified job does not exist.");
        }

        ModelMap namedSections = null;
        try {
            namedSections = (ModelMap)jobStatusLogReader.getSectionedLogs(job);
        } catch (PortalServiceException ex) {
            return generateJSONResponseMAV(false, null, ex.getMessage());
        }

        return generateJSONResponseMAV(true, Arrays.asList(namedSections), "");
    }
}
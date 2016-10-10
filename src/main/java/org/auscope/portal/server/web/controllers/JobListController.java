/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.services.cloud.monitor.JobStatusException;
import org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.core.util.TextUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.auscope.portal.server.vegl.VGLPollingJobQueueManager;
import org.auscope.portal.server.vegl.VGLQueueJob;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    public static final String VL_LOG_FILE = "vl.sh.log";

    /** The name of the termination file that the job will use*/
    public static final String VL_TERMINATION_FILE = "vl.end";

    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());

    private FileStagingService fileStagingService;
    private VGLJobStatusAndLogReader jobStatusLogReader;
    private JobStatusMonitor jobStatusMonitor;
    private VGLJobStatusChangeHandler vglJobStatusChangeHandler;
    private VGLPollingJobQueueManager vglPollingJobQueueManager;

    private String adminEmail=null;

    /**
     * @return the adminEmail
     */
    public String getAdminEmail() {
        return adminEmail;
    }

    /**
     * @param adminEmail the adminEmail to set
     */
    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    @Autowired
    public JobListController(VEGLJobManager jobManager, CloudStorageService[] cloudStorageServices,
            FileStagingService fileStagingService, CloudComputeService[] cloudComputeServices,
            VGLJobStatusAndLogReader jobStatusLogReader,
            JobStatusMonitor jobStatusMonitor,VGLJobStatusChangeHandler vglJobStatusChangeHandler,
            @Value("${vm.sh}") String vmSh, @Value("${vm-shutdown.sh}") String vmShutdownSh,
            VGLPollingJobQueueManager vglPollingJobQueueManager,
            @Value("${HOST.portalAdminEmail}") String adminEmail) {
        super(cloudStorageServices, cloudComputeServices, jobManager,vmSh,vmShutdownSh);
        this.jobManager = jobManager;
        this.fileStagingService = fileStagingService;
        this.jobStatusLogReader = jobStatusLogReader;
        this.jobStatusMonitor = jobStatusMonitor;
        this.vglPollingJobQueueManager =  vglPollingJobQueueManager;
        this.adminEmail=adminEmail;
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

//    /**
//     * Returns a JSON object containing a list of the current user's series.
//     *
//     * @param request The servlet request
//     * @param response The servlet response
//     *
//     * @return A JSON object with a series attribute which is an array of
//     *         VEGLSeries objects.
//     */
//    @RequestMapping("/secure/mySeries.do")
//    public ModelAndView mySeries(HttpServletRequest request,
//            HttpServletResponse response,
//            @AuthenticationPrincipal ANVGLUser user) {
//
//        if (user == null || user.getEmail() == null) {
//            logger.warn("No email attached to session");
//            return generateJSONResponseMAV(false, null, "No email attached to session");
//        }
//        List<VEGLSeries> series = jobManager.querySeries(user.getEmail(), null, null);
//
//        logger.debug("Returning " + series);
//        return generateJSONResponseMAV(true, series, "");
//    }

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
            @AuthenticationPrincipal ANVGLUser user) {
        logger.info("Deleting job with ID " + jobId);

        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The requested job was not found.");
        }

        String oldJobStatus = job.getStatus();

        //Always cleanup our compute resources (if there are any)
        terminateInstance(job, false);

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
            @AuthenticationPrincipal ANVGLUser user) {

        VEGLSeries series = attemptGetSeries(seriesId, user);
        if (series == null) {
            return generateJSONResponseMAV(false);
        }

        if (!series.getUser().equals(user.getEmail())) {
            return generateJSONResponseMAV(false);
        }

        List<VEGLJob> jobs = jobManager.getSeriesJobs(seriesId.intValue(), user);
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
            @AuthenticationPrincipal ANVGLUser user) {
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
            terminateInstance(job, true);
        } catch (Exception e) {
            logger.error("Failed to cancel the job.", e);
            String admin = getAdminEmail();
            if(TextUtil.isNullOrEmpty(admin)) {
                admin = "the portal admin";
            }
            String errorCorrection = "Please try again in a few minutes or report it to "+admin+".";

            return generateJSONResponseMAV(false, null, "There was a problem cancelling your job.",
                    errorCorrection);
        }

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Terminates the instance of an EMI that was launched by a job.
     *
     * @param request The HttpServletRequest
     * @param job The job linked the to instance that is to be terminated
     */
    private void terminateInstance(VEGLJob job, boolean includeAuditTrail) {
        String oldJobStatus = job.getStatus();
        if (oldJobStatus.equals(JobBuilderController.STATUS_DONE) ||
                oldJobStatus.equals(JobBuilderController.STATUS_UNSUBMITTED)) {
            logger.debug("Skipping finished or unsubmitted job "+job.getId());
        }else if(oldJobStatus.equals(JobBuilderController.STATUS_INQUEUE)){
            VGLQueueJob dummyQueueJobForRemoval = new VGLQueueJob(null,null,job,"",null);
            vglPollingJobQueueManager.getQueue().remove(dummyQueueJobForRemoval);

            if (includeAuditTrail) {
                job.setStatus(JobBuilderController.STATUS_UNSUBMITTED);
                jobManager.saveJob(job);
                jobManager.createJobAuditTrail(oldJobStatus, job, "Job cancelled by user.");
            }
        }else {
            try {
                // We allow the job to be cancelled and re-submitted regardless
                // of its termination status.
                if (includeAuditTrail) {
                    job.setStatus(JobBuilderController.STATUS_UNSUBMITTED);
                    jobManager.saveJob(job);
                    jobManager.createJobAuditTrail(oldJobStatus, job, "Job cancelled by user.");
                }
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
            @AuthenticationPrincipal ANVGLUser user) {

        VEGLSeries series = attemptGetSeries(seriesId, user);
        if (series == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup series.");
        }

        List<VEGLJob> jobs = jobManager.getSeriesJobs(seriesId.intValue(), user);
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
                terminateInstance(job, true);
            } catch (Exception e) {
                logger.error("Failed to cancel one of the jobs in a given series.", e);
                return generateJSONResponseMAV(false, null, "There was a problem cancelling one of your jobs in selected series.",
                        "Please try again in a few minutes or report it to "+getAdminEmail()+".");
            }
        }

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Returns a JSON object containing the latest copy of metadata for a given job's file
     *
     * @param jobId
     * @return
     */
    @RequestMapping("/secure/getCloudFileMetadata.do")
    public ModelAndView getCloudFileMetadata(@RequestParam("jobId") Integer jobId,
            @RequestParam("fileName") String fileName,
            @AuthenticationPrincipal ANVGLUser user) {
        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The requested job was not found.");
        }

        CloudFileInformation fileDetails = null;
        try {
            CloudStorageService cloudStorageService = getStorageService(job);
            if (cloudStorageService == null) {
                logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cloud files cannot be requested", job.getStorageServiceId(), job.getId()));
                return generateJSONResponseMAV(false, null, "No cloud storage service found for job");
            } else {
                fileDetails = cloudStorageService.getJobFileMetadata(job, fileName);
            }
        } catch (Exception e) {
            logger.warn("Error fetching job file metadata.", e);
            return generateJSONResponseMAV(false, null, "Error fetching file metadata");
        }

        return generateJSONResponseMAV(true, Arrays.asList(fileDetails), "");
    }

    /**
     * Returns a JSON object containing an array of files belonging to a
     * given job AND the associated download objects .
     *
     * @param request The servlet request including a jobId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a files attribute which is an array of
     *         FileInformation objects. If the job was not found in the job
     *         manager the JSON object will contain an error attribute
     *         indicating the error.
     */
    @RequestMapping("/secure/jobCloudFiles.do")
    public ModelAndView jobCloudFiles(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") Integer jobId,
            @AuthenticationPrincipal ANVGLUser user) {
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
            @AuthenticationPrincipal ANVGLUser user) {

        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job object.");
        }

        logger.debug("Download " + key);

        CloudStorageService cloudStorageService = getStorageService(job);
        if (cloudStorageService == null) {
            logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cloud file cannot be downloaded", job.getStorageServiceId(), job.getId()));
            return generateJSONResponseMAV(false, null, "No cloud storage service found for job");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");

        //Get our Input Stream
        try (InputStream is = cloudStorageService.getJobFile(job, key)) {
            try (OutputStream out = response.getOutputStream()) {
                int n;
                byte[] buffer = new byte[1024];
                while ((n = is.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
                out.flush();
            } catch (Exception e) {
                logger.warn("Error whilst writing to output stream", e);
            }
            // The output is raw data down the output stream, just return null
            return null;
        } catch (Exception ex) {
            logger.warn(String.format("Unable to access '%1$s' from the cloud", key), ex);
            return generateJSONResponseMAV(false, null, "Unable to access file from the cloud");
        }
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
            @AuthenticationPrincipal ANVGLUser user) {

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
                try (InputStream is = cloudStorageService.getJobFile(job, fileKey)) {
                    byte[] buffer = new byte[16384];
                    int count = 0;
                    zout.putNextEntry(new ZipEntry(fileKey));
                    while ((count = is.read(buffer)) != -1) {
                        zout.write(buffer, 0, count);
                    }
                    zout.closeEntry();
                    readOneOrMoreFiles = true;
                }
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
            @AuthenticationPrincipal ANVGLUser user) {

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
     * Attempts to creates a new folder for the specified user.
     * We are resusing existing code for series in place as folder
     * The series object will be returned in a JSON response on success.
     *
     * @param seriesName
     * @param seriesDescription
     * @return
     */
    @RequestMapping("/secure/createFolder.do")
    public ModelAndView createFolder(HttpServletRequest request,
            @RequestParam("seriesName") String seriesName,
            @RequestParam("seriesDescription") String seriesDescription,
            @AuthenticationPrincipal ANVGLUser user) {
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
        return generateJSONResponseMAV(true);

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
            @AuthenticationPrincipal ANVGLUser user) {
        VEGLSeries series = attemptGetSeries(seriesId, user);
        if (series == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job series.");
        }

        List<VEGLJob> seriesJobs = jobManager.getSeriesJobs(seriesId.intValue(), user);
        if (seriesJobs == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup jobs for the specified series.");
        }

//        for (VEGLJob veglJob : seriesJobs) {
//          veglJob.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
//          veglJob.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
//          veglJob.setProperty(CloudJob.PROPERTY_S3_ROLE, user.getArnStorage());
//        }
//
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
     * Sets a user's job series ID to a new ID (which can be null indicating default job)
     *
     * This will fail if user is not the owner of the job or the new series.
     *
     * @param request
     * @param jobId
     * @param seriesId
     * @param user
     * @return
     */
    @RequestMapping("/secure/setJobFolder.do")
    public ModelAndView setJobFolder(HttpServletRequest request,
            @RequestParam("jobIds") Integer[] jobIds,
            @RequestParam(required=false, value="seriesId") Integer seriesId,
            @AuthenticationPrincipal ANVGLUser user) {
        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        for(Integer jobId : jobIds) {
            VEGLJob job = attemptGetJob(jobId, user);
            if (job == null) {
                return generateJSONResponseMAV(false);
            }
    
            //We allow a null series ID
            if (seriesId != null) {
                VEGLSeries series = jobManager.getSeriesById(seriesId, user.getEmail());
                if (!series.getUser().equals(user.getEmail())) {
                    return generateJSONResponseMAV(false);
                }
            }
    
            job.setSeriesId(seriesId);
            jobManager.saveJob(job);
        }
        return generateJSONResponseMAV(true);
    }

    /**
     * Returns a JSON array of jobStatus and jobId tuples
     *
     */
    @RequestMapping("/secure/jobsStatuses.do")
    public ModelAndView jobStatuses(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required=false, value="forceStatusRefresh", defaultValue="false") boolean forceStatusRefresh,
            @AuthenticationPrincipal ANVGLUser user) {

        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        List<VEGLJob> userJobs = jobManager.getUserJobs(user);
        if (userJobs == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup jobs.");
        }

        if (forceStatusRefresh) {
            try {
                jobStatusMonitor.statusUpdate(userJobs);
            } catch (JobStatusException e) {
                log.info("There was an error updating one or more jobs: " + e.getMessage());
                log.debug("Exception(s): ", e);
            }
        }

        List<ModelMap> tuples = new ArrayList<>(userJobs.size());
        for (VEGLJob job : userJobs) {
            ModelMap tuple = new ModelMap();
            tuple.put("jobId", job.getId());
            tuple.put("status", job.getStatus());
            tuples.add(tuple);
        }

        return generateJSONResponseMAV(true, tuples, "");
    }

    /**
     * Returns a JSON object containing an tree of all jobs, grouped by series.
     * Also returns an array of job objects
     *
     * @param request The servlet request including a seriesId parameter
     * @param response The servlet response
     *
     * @return A JSON object with a jobs attribute which is an array of
     *         <code>VEGLJob</code> objects.
     */
    @SuppressWarnings("unchecked")
    @RequestMapping("/secure/treeJobs.do")
    public ModelAndView treeJobs(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required=false, value="forceStatusRefresh", defaultValue="false") boolean forceStatusRefresh,
            @AuthenticationPrincipal ANVGLUser user) {

        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        List<VEGLSeries> userSeries = jobManager.querySeries(user.getEmail(), null, null);
        List<VEGLJob> userJobs = jobManager.getUserJobs(user);
        if (userSeries == null || userJobs == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup jobs.");
        }

        if (forceStatusRefresh) {
            try {
                jobStatusMonitor.statusUpdate(userJobs);
            } catch (JobStatusException e) {
                log.info("There was an error updating one or more jobs: " + e.getMessage());
                log.debug("Exception(s): ", e);
            }
        }

        //Now we organise into a tree structure
        ModelMap rootNode = new ModelMap();
        rootNode.put("name", user.getEmail());
        rootNode.put("expanded", true);
        rootNode.put("expandable", true);
        rootNode.put("leaf", false);
        rootNode.put("root", true);
        rootNode.put("seriesId", null);
        rootNode.put("children", new ArrayList<ModelMap>());

        Map<Integer, ModelMap> nodeMap = new HashMap<>();
        for (VEGLSeries series : userSeries) {
            ModelMap node = new ModelMap();
            node.put("leaf", false);
            node.put("expanded", false);
            node.put("expandable", true);
            node.put("name", series.getName());
            node.put("seriesId", series.getId());
            node.put("children", new ArrayList<ModelMap>());

            nodeMap.put(series.getId(), node);
            ((ArrayList<ModelMap>) rootNode.get("children")).add(node);
        }

        for (VEGLJob job : userJobs) {
            ModelMap nodeParent = nodeMap.get(job.getSeriesId());
            if (nodeParent == null) {
                nodeParent = rootNode;
            }

            ModelMap node = new ModelMap();
            node.put("leaf", true);
            node.put("name", job.getName());
            node.put("id", job.getId());
            node.put("submitDate", job.getSubmitDate());
            node.put("status", job.getStatus());
            node.put("seriesId", job.getSeriesId());

            ((ArrayList<ModelMap>) nodeParent.get("children")).add(node);
        }

        ModelMap resultObj = new ModelMap();
        resultObj.put("nodes", rootNode);
        resultObj.put("jobs", userJobs);

        return generateJSONResponseMAV(true, resultObj, "");
    }

    /**
     * Tests whether the specified cloud file appears in a list of fileNames
     *
     * If fileNames is null, true will be returned
     * @param files
     * @param fileName
     * @return
     */
    private static boolean cloudFileIncluded(String[] fileNames, CloudFileInformation cloudFile) {
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
     * Copies job files and/or downloads from sourceJobId to targetJobId
     *
     * Job files will be duplicated in LOCAL staging only. The files duplicated can be
     * controlled by a list of file names
     *
     * Job downloads will be copied directly (but new IDs minted)
     */
    @RequestMapping("/secure/copyJobFiles.do")
    public ModelAndView copyJobFiles(HttpServletRequest request,
            @AuthenticationPrincipal ANVGLUser user,
            @RequestParam("targetJobId") Integer targetJobId,
            @RequestParam("sourceJobId") Integer sourceJobId,
            @RequestParam(required=false, value="fileKey") String[] fileKeys,
            @RequestParam(required=false, value="downloadId") Integer[] downloadIds) {

        VEGLJob sourceJob = attemptGetJob(sourceJobId, user);
        VEGLJob targetJob = attemptGetJob(targetJobId, user);

        if (sourceJob == null || targetJob == null) {
            logger.error(String.format("sourceJob %1$s or targetJob %2$s inaccessible to user %3$s", sourceJobId, targetJobId, user));
            return generateJSONResponseMAV(false, null, "Unable to copy files");
        }

        CloudStorageService cloudStorageService = getStorageService(sourceJob);
        if (cloudStorageService == null) {
            logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cannot copy files", sourceJob.getStorageServiceId(), sourceJob.getId()));
            return generateJSONResponseMAV(false, null, "No cloud storage service found for job");
        }

        try {
            InputStream is = null;
            OutputStream os = null;

            if (fileKeys != null) {
                for (String fileKey : fileKeys) {
                    try {
                        is = cloudStorageService.getJobFile(sourceJob, fileKey);
                        os = fileStagingService.writeFile(targetJob, fileKey);
                        IOUtils.copy(is, os);
                    } finally {
                        IOUtils.closeQuietly(is);
                        IOUtils.closeQuietly(os);
                    }
                }
            }

            List<VglDownload> targetDownloads = targetJob.getJobDownloads();
            if (downloadIds != null) {
                for (Integer downloadId : downloadIds) {
                    for (VglDownload download : sourceJob.getJobDownloads()) {
                        if (download.getId().equals(downloadId)) {
                            VglDownload newDownload = (VglDownload) download.clone();
                            newDownload.setParent(targetJob);
                            newDownload.setId(null);
                            targetDownloads.add(newDownload);
                        }
                    }
                }
                jobManager.saveJob(targetJob);
            }
            return generateJSONResponseMAV(true);
        } catch (Exception ex) {
            logger.error("Error copying files for job.", ex);
            return generateJSONResponseMAV(false);
        }
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
            @AuthenticationPrincipal ANVGLUser user) {
        logger.info("Duplicate a new job from job ID "+ jobId);

        //Lookup the job we are cloning
        VEGLJob oldJob;
        try {
             oldJob = attemptGetJob(jobId, user);
        } catch (AccessDeniedException e) {
            throw e;
        }

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
        newJob.setComputeInstanceId(null);

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
                    try (InputStream is = cloudStorageService.getJobFile(oldJob, cloudFile.getName());
                         OutputStream os = fileStagingService.writeFile(newJob, cloudFile.getName())) {

                        FileIOUtil.writeInputToOutputStream(is, os, 1024 * 1024, false);
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
    public ModelAndView getSectionedLogs(HttpServletRequest request,
            @RequestParam("jobId") Integer jobId,
            @RequestParam(value="file", required=false) String file,
            @AuthenticationPrincipal ANVGLUser user) {
        //Lookup the job whose logs we are accessing
        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The specified job does not exist.");
        }

        ModelMap namedSections = null;
        try {
            namedSections = jobStatusLogReader.getSectionedLogs(job, file == null ? VL_LOG_FILE : file);
        } catch (PortalServiceException ex) {
            return generateJSONResponseMAV(false, null, ex.getMessage());
        }

        return generateJSONResponseMAV(true, Arrays.asList(namedSections), "");
    }

    @RequestMapping("/secure/getPlaintextPreview.do")
    public ModelAndView getPlaintextPreview(
            @RequestParam("jobId") Integer jobId,
            @RequestParam("file") String file,
            @RequestParam("maxSize") Integer maxSize,
            @AuthenticationPrincipal ANVGLUser user) {

        if (maxSize > 512 * 1024) {
            maxSize = 512 * 1024; //Don't allow us to burn GB's on previews
        }

        //Lookup the job whose logs we are accessing
        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The specified job does not exist.");
        }

        CloudStorageService cloudStorageService = getStorageService(job);
        if (cloudStorageService == null) {
            logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cloud file cannot be downloaded", job.getStorageServiceId(), job.getId()));
            return generateJSONResponseMAV(false, null, "No cloud storage service found for job");
        }

        InputStream is = null;
        try {
            is = cloudStorageService.getJobFile(job, file);
            InputStreamReader reader = new InputStreamReader(is, Charsets.UTF_8);
            char[] buffer = new char[maxSize];
            int charsRead = reader.read(buffer);
            if (charsRead < 0) {
                return generateJSONResponseMAV(false, null, "Error reading file from cloud storage.");
            }
            return generateJSONResponseMAV(true, new String(buffer, 0, charsRead), "");
        } catch (Exception ex) {
            logger.error("Error accessing file:" + file, ex);
            return generateJSONResponseMAV(false);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @RequestMapping("/secure/getImagePreview.do")
    public void getImagePreview(
            HttpServletResponse response,
            @RequestParam("jobId") Integer jobId,
            @RequestParam("file") String file,
            @AuthenticationPrincipal ANVGLUser user) throws Exception {

        //Lookup the job whose logs we are accessing
        VEGLJob job = attemptGetJob(jobId, user);
        if (job == null) {
            response.sendError(HttpStatus.SC_NOT_FOUND);
            return;
        }

        CloudStorageService cloudStorageService = getStorageService(job);
        if (cloudStorageService == null) {
            logger.error(String.format("No cloud storage service with id '%1$s' for job '%2$s'. Cloud file cannot be downloaded", job.getStorageServiceId(), job.getId()));
            response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        InputStream is = null;
        ServletOutputStream os = null;
        try {
            is = cloudStorageService.getJobFile(job, file);
            response.setContentType("image");
            os = response.getOutputStream();
            IOUtils.copy(is, os);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value =  org.springframework.http.HttpStatus.FORBIDDEN)
    public @ResponseBody String handleException(AccessDeniedException e) {
        return e.getMessage();
    }
}

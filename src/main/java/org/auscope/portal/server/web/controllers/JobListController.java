/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the job list view.
 *
 * @author Cihan Altinay
 * @author Abdi Jama
 * @author Josh Vote
 */
@Controller
public class JobListController extends BasePortalController  {

    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());

    private VEGLJobManager jobManager;
    private CloudStorageService cloudStorageService;
    private FileStagingService fileStagingService;
    private CloudComputeService cloudComputeService;

    @Autowired
    public JobListController(VEGLJobManager jobManager, CloudStorageService cloudStorageService,
            FileStagingService fileStagingService, CloudComputeService cloudComputeService) {
        this.jobManager = jobManager;
        this.cloudStorageService = cloudStorageService;
        this.fileStagingService = fileStagingService;
        this.cloudComputeService = cloudComputeService;
    }

    /**
     * Attempts to get a job with a particular ID. If the job ID does NOT belong to the current
     * user session null will be returned.
     *
     * This function will log all appropriate errors.
     * @param jobId
     * @return The VEGLJob object on success or null otherwise.
     */
    private VEGLJob attemptGetJob(Integer jobId, HttpServletRequest request) {
        VEGLJob job = null;
        String user = (String)request.getSession().getAttribute("openID-Email");

        //Check we have a user email
        if (user == null || user.isEmpty()) {
            logger.warn("The current session is missing an email attribute");
            return null;
        }

        //Attempt to fetch our job
        if (jobId != null) {
            try {
                job = jobManager.getJobById(jobId.intValue());
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
        if (!user.equals(job.getUser())) {
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
    private VEGLSeries attemptGetSeries(Integer seriesId, HttpServletRequest request) {
        VEGLSeries series = null;
        String user = (String)request.getSession().getAttribute("openID-Email");

        //Check we have a user email
        if (user == null || user.isEmpty()) {
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
        if (!user.equals(series.getUser())) {
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
    @RequestMapping("/mySeries.do")
    public ModelAndView mySeries(HttpServletRequest request,
                                 HttpServletResponse response) {

        String user = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
        if (user == null || user.isEmpty()) {
            logger.warn("No email attached to session");
            return generateJSONResponseMAV(false, null, "No email attached to session");
        }
        List<VEGLSeries> series = jobManager.querySeries(user, null, null);

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
    @RequestMapping("/deleteJob.do")
    public ModelAndView deleteJob(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam("jobId") Integer jobId) {

        VEGLJob job = attemptGetJob(jobId, request);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The requested job was not found.");
        }

        logger.info("Deleting job with ID " + jobId);
        fileStagingService.deleteStageInDirectory(job);
        jobManager.deleteJob(job);

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
    @RequestMapping("/deleteSeriesJobs.do")
    public ModelAndView deleteSeriesJobs(HttpServletRequest request,
                                       HttpServletResponse response,
                                       @RequestParam("seriesId") Integer seriesId) {

        VEGLSeries series = attemptGetSeries(seriesId, request);
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

            fileStagingService.deleteStageInDirectory(job);
            jobManager.deleteJob(job);
        }

        logger.info("Deleting series "+seriesId);
        jobManager.deleteSeries(series);

        return generateJSONResponseMAV(true, null, "");
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
                                HttpServletResponse response,
                                @RequestParam("jobId") Integer jobId) {

        VEGLJob job = attemptGetJob(jobId, request);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job to kill.");
        }

        // terminate the EMI instance
        try {
            logger.info("Cancelling job with ID "+jobId);
            terminateInstance(job);
        } catch (Exception e) {
            logger.error("Failed to terminate instance with id: " + job.getComputeInstanceId(), e);
            return generateJSONResponseMAV(false, null, "Error killing running instance");
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
        cloudComputeService.terminateJob(job);
        job.setStatus(GridSubmitController.STATUS_CANCELLED);
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
                                       HttpServletResponse response,
                                       @RequestParam("seriesId") Integer seriesId) {

        VEGLSeries series = attemptGetSeries(seriesId, request);
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
            String oldStatus = job.getStatus();
            if (!oldStatus.equals(GridSubmitController.STATUS_ACTIVE)) {
                logger.debug("Skipping finished job "+job.getId());
                continue;
            }
            logger.info("Cancelling job with ID "+job.getId());

            //terminate the EMI instance
            try {
                logger.info("Cancelling job with ID "+ job.getId());
                terminateInstance(job);
            } catch (Exception e) {
                logger.error("Failed to terminate instance with id: " + job.getComputeInstanceId(), e);
                return generateJSONResponseMAV(false, null, "Error killing running instance");
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
    @RequestMapping("/jobFiles.do")
    public ModelAndView jobFiles(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @RequestParam("jobId") Integer jobId) {

        VEGLJob job = attemptGetJob(jobId, request);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "The requested job was not found.");
        }

        CloudFileInformation[] fileDetails = null;
        try {
            fileDetails = cloudStorageService.listJobFiles(job);
            logger.info(fileDetails.length + " job files located");
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
    @RequestMapping("/downloadFile.do")
    public ModelAndView downloadFile(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam("jobId") Integer jobId,
                                     @RequestParam("filename") String fileName,
                                     @RequestParam("key") String key) {


        VEGLJob job = attemptGetJob(jobId, request);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job object.");
        }

        logger.debug("Download " + key);

        //Get our Input Stream
        InputStream is = null;
        try {
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
    @RequestMapping("/downloadAsZip.do")
    public ModelAndView downloadAsZip(HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam("jobId") Integer jobId,
                                      @RequestParam("files") String filesParam) {

        //Lookup our job and check input files
        VEGLJob job = attemptGetJob(jobId, request);
        if (job == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job object.");
        }

        logger.debug("filesParam: " + filesParam);
        if (filesParam == null || filesParam.isEmpty()) {
            return generateJSONResponseMAV(false, null, "No files have been selected.");
        }
        String[] fileKeys = filesParam.split(",");
        logger.debug("Archiving " + fileKeys.length + " file(s) of job " + jobId);

        //Start writing our data to a zip archive (which is being streamed to user)
        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"jobfiles.zip\"");

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
    @RequestMapping("/querySeries.do")
    public ModelAndView querySeries(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(required=false, value="qUser") String qUser,
                                    @RequestParam(required=false, value="qSeriesName") String qName,
                                    @RequestParam(required=false, value="qSeriesDesc") String qDesc) {


        if (qUser == null && qName == null && qDesc == null) {
            qUser = (String)request.getSession().getAttribute("openID-Email");//request.getRemoteUser();
            logger.debug("No query parameters provided. Will return "+qUser+"'s series.");
        }

        logger.debug("qUser="+qUser+", qName="+qName+", qDesc="+qDesc);
        List<VEGLSeries> series = jobManager.querySeries(qUser, qName, qDesc);

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

        return generateJSONResponseMAV(true, Arrays.asList(series), "");
    }

    /**
     * Tests whether the specified list of files contain a non empty file with the specified file name
     * @param files
     * @param fileName
     * @return
     */
    private boolean containsFile(CloudFileInformation[] files, String fileName) {
        if (files == null) {
            return false;
        }

        for (CloudFileInformation file : files) {
            if (file.getName().endsWith(fileName) && file.getSize() > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Looks at the specified job and determines what status the job should be in
     * based upon uploaded files.
     * @param job
     */
    private void updateJobStatus(VEGLJob job) {
        String status = job.getStatus();
        if (status == null) {
            status = "";
        }

        //Don't lookup files for jobs that haven't been submitted
        if (status.equals(GridSubmitController.STATUS_UNSUBMITTED)) {
            return;
        }

        //Get the output files for this job
        CloudFileInformation[] results = null;
        try {
            results = cloudStorageService.listJobFiles(job);
        } catch (Exception e) {
            logger.error("Unable to list output job files", e);
        }

        boolean jobStarted = containsFile(results, "workflow-version.txt");
        boolean jobFinished = containsFile(results, "vegl.sh.log");

        String newStatus = status;
        if (jobFinished) {
            newStatus = GridSubmitController.STATUS_DONE;
        } else if (jobStarted) {
            newStatus = GridSubmitController.STATUS_ACTIVE;
        } else {
            newStatus = GridSubmitController.STATUS_PENDING;
        }

        if (!newStatus.equals(status)) {
            job.setStatus(newStatus);
            jobManager.saveJob(job);
        }
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
                                 HttpServletResponse response,
                                 @RequestParam("seriesId") Integer seriesId) {
        VEGLSeries series = attemptGetSeries(seriesId, request);
        if (series == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job series.");
        }

        List<VEGLJob> seriesJobs = jobManager.getSeriesJobs(seriesId.intValue());
        if (seriesJobs == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup jobs for the specified series.");
        }

        for (VEGLJob job : seriesJobs) {
            updateJobStatus(job);
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
    @RequestMapping("/duplicateJob.do")
    public ModelAndView duplicateJob(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam("jobId") Integer jobId,
                                @RequestParam(required=false, value="file") String[] files) {

        //Lookup the job we are cloning
        VEGLJob oldJob = attemptGetJob(jobId, request);
        if (oldJob == null) {
            return generateJSONResponseMAV(false, null, "Unable to lookup job to duplicate.");
        }

        //Create a cloned job but make it 'unsubmitted'
        VEGLJob newJob = oldJob.clone();
        newJob.setId(null);
        newJob.setStatus(GridSubmitController.STATUS_UNSUBMITTED);
        newJob.setSubmitDate((Date)null);
        newJob.setRegisteredUrl(null);

        //Attempt to save the new job to the DB
        try {
            jobManager.saveJob(newJob);

            //This needs to be set AFTER we first save the job (the ID will form part of the key)
            newJob.setStorageBaseKey(cloudStorageService.generateBaseKey(newJob));
            jobManager.saveJob(newJob);
        } catch (Exception ex) {
            log.error("Unable to save job to database: " + ex.getMessage());
            log.debug("Exception:", ex);
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
                    FileOutputStream os = null;
                    try {
                        File stagingFile = fileStagingService.createStageInDirectoryFile(newJob, cloudFile.getName());
                        os = new FileOutputStream(stagingFile);

                        writeInputToOutputStream(is, os, 1024 * 1024, false);
                    } finally {
                        FileIOUtil.closeQuietly(os);
                        FileIOUtil.closeQuietly(is);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Unable to duplicate input files: " + ex.getMessage());
            log.debug("Exception:", ex);

            //Tidy up after ourselves
            fileStagingService.deleteStageInDirectory(newJob);
            jobManager.deleteJob(newJob);

            return generateJSONResponseMAV(false, null, "Unable to save new job.");
        }

        return generateJSONResponseMAV(true, Arrays.asList(newJob), "");
    }


}


/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.gridjob.GridAccessController;
import org.auscope.portal.server.gridjob.ScriptParser;
import org.auscope.portal.server.gridjob.Util;
import org.auscope.portal.server.gridjob.VRLJob;
import org.auscope.portal.server.gridjob.VRLJobManager;
import org.auscope.portal.server.gridjob.VRLSeries;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for the job submission view.
 *
 * @author Cihan Altinay
 */
@Controller
public class GridSubmitController {

    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private GridAccessController gridAccess;
    @Autowired
    private VRLJobManager jobManager;

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
     * Sets the <code>VRLJobManager</code> to be used to retrieve and store
     * series and job details.
     *
     * @param jobManager the JobManager to use
     */
   /* public void setJobManager(VRLJobManager jobManager) {
        this.jobManager = jobManager;
    }*/

  /*  protected ModelAndView handleNoSuchRequestHandlingMethod(
            NoSuchRequestHandlingMethodException ex,
            HttpServletRequest request,
            HttpServletResponse response) {

        // Ensure user has valid grid credentials
        if (gridAccess.isProxyValid(
                    request.getSession().getAttribute("userCred"))) {
            logger.debug("No/invalid action parameter; returning gridsubmit view.");
            return new ModelAndView("gridsubmit");
        } else {
            request.getSession().setAttribute(
                    "redirectAfterLogin", "/gridsubmit.html");
            logger.debug("Proxy not initialized. Redirecting to login.");
            return new ModelAndView(
                    new RedirectView("/login.html", true, false, false));
        }
    }*/

    /**
     * Returns a JSON object containing a list of the current user's series.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a series attribute which is an array of
     *         VRLSeries objects.
     */
    @RequestMapping("/mySeries.do")
    public ModelAndView mySeries(HttpServletRequest request,
                                 HttpServletResponse response) {

        String user = request.getRemoteUser();

        logger.debug("Querying series of "+user);
        List<VRLSeries> series = jobManager.querySeries(user, null, null);

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
     * Returns a JSON object containing an array of ESyS-particle sites.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a sites attribute which is an array of
     *         sites on the grid that have an installation of ESyS-particle.
     */
    @RequestMapping("/listSites.do")    
    public ModelAndView listSites(HttpServletRequest request,
                                  HttpServletResponse response) {

        logger.debug("Retrieving sites with "+VRLJob.CODE_NAME+" installations.");
        String[] particleSites = gridAccess.
                retrieveSitesWithSoftwareAndVersion(VRLJob.CODE_NAME, "");

        List<SimpleBean> sites = new ArrayList<SimpleBean>();
        for (int i=0; i<particleSites.length; i++) {
            sites.add(new SimpleBean(particleSites[i]));
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
     * Returns a JSON object containing an array of ESyS-particle versions at
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
        List<SimpleBean> versions = new ArrayList<SimpleBean>();

        if (site != null) {
            logger.debug("Retrieving ESyS-Particle versions at "+site);

            String[] siteVersions = gridAccess.
                    retrieveCodeVersionsAtSite(site, VRLJob.CODE_NAME);

            for (int i=0; i<siteVersions.length; i++) {
                versions.add(new SimpleBean(siteVersions[i]));
            }
        } else {
            logger.warn("No site specified!");
        }

        logger.debug("Returning list of "+versions.size()+" versions.");
        return new ModelAndView("jsonView", "versions", versions);
    }

    /**
     * Returns a JSON object containing a populated VRLJob object.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a data attribute containing a populated
     *         VRLJob object and a success attribute.
     */
    @RequestMapping("/getJobObject.do")    
    public ModelAndView getJobObject(HttpServletRequest request,
                                     HttpServletResponse response) {

        VRLJob job = prepareModel(request);

        logger.debug("Returning job.");
        ModelAndView result = new ModelAndView("jsonView");
        result.addObject("data", job);
        result.addObject("success", true);

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
            .getAttribute("jobInputDir");

        List files = new ArrayList<FileInformation>();

        if (jobInputDir != null) {
            File dir = new File(jobInputDir);
            String fileNames[] = dir.list();
            for (int i=0; i<fileNames.length; i++) {
                File f = new File(dir, fileNames[i]);
                files.add(new FileInformation(fileNames[i], f.length()));
            }
        }

        logger.debug("Returning list of "+files.size()+" files.");
        return new ModelAndView("jsonView", "files", files);
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

        String jobInputDir = (String) request.getSession()
            .getAttribute("jobInputDir");

        boolean success = true;
        String error = null;
        FileInformation fileInfo = null;

        if (jobInputDir != null) {
            MultipartHttpServletRequest mfReq =
                (MultipartHttpServletRequest) request;

            MultipartFile f = mfReq.getFile("file");
            if (f == null) {
                logger.error("No file parameter provided.");
                success = false;
                error = new String("Invalid request.");
            } else {
                logger.info("Saving uploaded file "+f.getOriginalFilename());
                File destination = new File(
                        jobInputDir+f.getOriginalFilename());
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
            }

        } else {
            logger.error("Input directory not found in current session!");
            success = false;
            error = new String("Internal error. Please reload the page.");
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
                pw.print(",name:'"+fileInfo.getName()+"',size:"+fileInfo.getSize());
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
            .getAttribute("jobInputDir");
        ModelAndView mav = new ModelAndView("jsonView");
        boolean success;

        if (jobInputDir != null) {
            success = true;
            String filesPrm = request.getParameter("files");
            logger.debug("Request to delete "+filesPrm);
            String[] files = (String[]) JSONArray.toArray(
                    JSONArray.fromObject(filesPrm), String.class);

            for (String filename: files) {
                File f = new File(jobInputDir+filename);
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
            }
        } else {
            success = false;
        }

        mav.addObject("success", success);
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
            .getAttribute("jobInputDir");

        if (jobInputDir != null) {
            logger.debug("Deleting temporary job files.");
            File jobDir = new File(jobInputDir);
            Util.deleteFilesRecursive(jobDir);
            request.getSession().removeAttribute("jobInputDir");
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
                                  VRLJob job) {

        logger.debug("Job details:\n"+job.toString());

        VRLSeries series = null;
        boolean success = true;
        final String user = request.getRemoteUser();
        String jobInputDir = (String) request.getSession()
            .getAttribute("jobInputDir");
        String newSeriesName = request.getParameter("seriesName");
        String seriesIdStr = request.getParameter("seriesId");
        ModelAndView mav = new ModelAndView("jsonView");
        Object credential = request.getSession().getAttribute("userCred");

        if (credential == null) {
            final String errorString = "Invalid grid credentials!";
            logger.error(errorString);
            mav.addObject("error", errorString);
            mav.addObject("success", false);
            return mav;
        }

        // if seriesName parameter was provided then we create a new series
        // otherwise seriesId contains the id of the series to use.
        if (newSeriesName != null && newSeriesName != "") {
            String newSeriesDesc = request.getParameter("seriesDesc");

            logger.debug("Creating new series '"+newSeriesName+"'.");
            series = new VRLSeries();
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
            logger.error("No valid series found. NOT submitting job!");

        } else {
            job.setSeriesId(series.getId());
            job.setArguments(new String[] { job.getScriptFile() });

            // Add server part to local stage-in dir
            String stageInURL = gridAccess.getLocalGridFtpServer()+jobInputDir;
            job.setInTransfers(new String[] { stageInURL });

            // Create a new directory for the output files of this job
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String dateFmt = sdf.format(new Date());
            String jobID = user + "-" + job.getName() + "-" + dateFmt +
                File.separator;
            String jobOutputDir = gridAccess.getLocalGridFtpStageOutDir()+jobID;
            String submitEPR = null;

            job.setEmailAddress(user);
            job.setOutputDir(jobOutputDir);
            job.setOutTransfers(new String[]
                    { gridAccess.getLocalGridFtpServer() + jobOutputDir });

            logger.info("Submitting job with name " + job.getName() +
                    " to " + job.getSite());
            // ACTION!
            submitEPR = gridAccess.submitJob(job, credential);

            if (submitEPR == null) {
                success = false;
            } else {
                logger.info("SUCCESS! EPR: "+submitEPR);
                String status = gridAccess.retrieveJobStatus(
                        submitEPR, credential);
                job.setReference(submitEPR);
                job.setStatus(status);
                job.setSubmitDate(dateFmt);
                jobManager.saveJob(job);
                request.getSession().removeAttribute("jobInputDir");
            }
        }

        mav.addObject("success", success);

        return mav;
    }

    /**
     * Creates a new VRLJob object with predefined values for some fields.
     * If the ScriptBuilder was used the file is moved to the job input
     * directory whereas a resubmission request is handled by using the
     * attributes of the job to be resubmitted.
     *
     * @param request The servlet request containing a session object
     *
     * @return The new job object.
     */
    private VRLJob prepareModel(HttpServletRequest request) {
        final String user = request.getRemoteUser();
        final String maxWallTime = "3000"; // 50 hours
        final String maxMemory = "30720"; // 30 GB
        final String stdInput = "";
        final String stdOutput = "stdOutput.txt";
        final String stdError = "stdError.txt";
        final String[] arguments = new String[0];
        final String[] inTransfers = new String[0];
        final String[] outTransfers = new String[0];
        String name = "VRLjob";
        String site = "iVEC";
        Integer cpuCount = 2;
        Integer numBonds = 0;
        Integer numParticles = 0;
        Integer numTimesteps = 0;
        String version = "";
        String queue = "";
        String description = "";
        String scriptFile = "";
        String checkpointPrefix = "";

        // Set a default version and queue
        String[] allVersions = gridAccess.retrieveCodeVersionsAtSite(
                site, VRLJob.CODE_NAME);
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
        String jobInputDir = gridAccess.getLocalGridFtpStageInDir() + jobID;

        boolean success = (new File(jobInputDir)).mkdir();

        if (!success) {
            logger.error("Could not create directory "+jobInputDir);
            jobInputDir = gridAccess.getLocalGridFtpStageInDir();
        }

        // Save in session to use it when submitting job
        request.getSession().setAttribute("jobInputDir", jobInputDir);

        // Check if the user requested to re-submit a previous job.
        String jobIdStr = (String) request.getSession().
            getAttribute("resubmitJob");
        VRLJob existingJob = null;
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
            numBonds = existingJob.getNumBonds();
            numParticles = existingJob.getNumParticles();
            numTimesteps = existingJob.getNumTimesteps();
            checkpointPrefix = existingJob.getCheckpointPrefix();

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
        String newScript = (String) request.getSession().
            getAttribute("scriptFile");
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
                    numTimesteps = parser.getNumTimeSteps();
                } catch (IOException e) {
                    logger.warn("Error parsing file: "+e.getMessage());
                }
            } else {
                logger.warn("Could not move "+newScript+" to stage-in!");
            }
        }

        logger.debug("Creating new VRLJob instance");
        VRLJob job = new VRLJob(site, name, version, arguments, queue,
                maxWallTime, maxMemory, cpuCount, inTransfers, outTransfers,
                user, stdInput, stdOutput, stdError);

        job.setScriptFile(scriptFile);
        job.setDescription(description);
        job.setNumBonds(numBonds);
        job.setNumParticles(numParticles);
        job.setNumTimesteps(numTimesteps);
        job.setCheckpointPrefix(checkpointPrefix);

        return job;
    }
}


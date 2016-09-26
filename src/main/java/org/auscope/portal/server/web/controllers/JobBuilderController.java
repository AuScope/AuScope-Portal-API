package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.cloud.StagedFile;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudComputeServiceAws;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.util.TextUtil;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VEGLSeries;
import org.auscope.portal.server.vegl.VGLPollingJobQueueManager;
import org.auscope.portal.server.vegl.VGLQueueJob;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.vegl.VglMachineImage;
import org.auscope.portal.server.vegl.VglParameter.ParameterType;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ANVGLProvenanceService;
import org.auscope.portal.server.web.service.ScmEntryService;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler;
import org.auscope.portal.server.web.service.scm.Toolbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the job submission view.
 *
 * @author Cihan Altinay
 * @author Abdi Jama
 * @author Josh Vote
 */
@Controller
public class JobBuilderController extends BaseCloudController {


    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());

    private FileStagingService fileStagingService;
    private VGLPollingJobQueueManager vglPollingJobQueueManager;
    private ScmEntryService scmEntryService;
    private ANVGLProvenanceService anvglProvenanceService;
    private String adminEmail = null;
    private String defaultToolbox = null;

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

    public static final String STATUS_PENDING = "Pending";//VT:Request accepted by compute service
    public static final String STATUS_ACTIVE = "Active";//VT:Running
    public static final String STATUS_PROVISION = "Provisioning";//VT:awaiting response from compute service
    public static final String STATUS_DONE = "Done";//VT:Job done
    public static final String STATUS_DELETED = "Deleted";//VT:Job deleted
    public static final String STATUS_UNSUBMITTED = "Saved";//VT:Job saved, fail to submit for whatever reason.
    public static final String STATUS_INQUEUE = "In Queue";//VT: quota exceeded, placed in queue.
    public static final String STATUS_ERROR = "ERROR";//VT:Exception in job processing.
    public static final String STATUS_WALLTIME_EXCEEDED = "WALLTIME EXCEEDED";//VT:Walltime exceeded.

    public static final String SUBMIT_DATE_FORMAT_STRING = "yyyyMMdd_HHmmss";

    public static final String DOWNLOAD_SCRIPT = "vl-download.sh";
    VGLJobStatusChangeHandler vglJobStatusChangeHandler;

    @Autowired
    public JobBuilderController(@Value("${HOST.portalAdminEmail}") String adminEmail,
            @Value("${HOST.defaultToolbox}") String defaultToolbox,
            VEGLJobManager jobManager, FileStagingService fileStagingService,
            @Value("${vm.sh}") String vmSh, @Value("${vm-shutdown.sh}") String vmShutdownSh,
            CloudStorageService[] cloudStorageServices,
            CloudComputeService[] cloudComputeServices,VGLJobStatusChangeHandler vglJobStatusChangeHandler,
            VGLPollingJobQueueManager vglPollingJobQueueManager, ScmEntryService scmEntryService,
            ANVGLProvenanceService anvglProvenanceService) {
        super(cloudStorageServices, cloudComputeServices, jobManager,vmSh,vmShutdownSh);
        this.fileStagingService = fileStagingService;
        this.cloudStorageServices = cloudStorageServices;
        this.cloudComputeServices = cloudComputeServices;
        this.vglJobStatusChangeHandler=vglJobStatusChangeHandler;
        this.vglPollingJobQueueManager = vglPollingJobQueueManager;
        this.scmEntryService = scmEntryService;
        this.anvglProvenanceService = anvglProvenanceService;
        this.adminEmail=adminEmail;
        this.defaultToolbox = defaultToolbox;
    }


    /**
     * Returns a JSON object containing a populated VEGLJob object.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a data attribute containing a populated
     *         VEGLJob object and a success attribute.
     */
    @RequestMapping("/secure/getJobObject.do")
    public ModelAndView getJobObject(@RequestParam("jobId") String jobId, @AuthenticationPrincipal ANVGLUser user) {
        try {
            VEGLJob job = attemptGetJob(Integer.parseInt(jobId), user);
            if (job == null) {
                return generateJSONResponseMAV(false);
            }

            return generateJSONResponseMAV(true, Arrays.asList(job), "");
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }
    }

    /**
     * Utility for converting between a StagedFile and FileInformation object
     * @param file
     * @return
     */
    private static FileInformation stagedFileToFileInformation(StagedFile file) {
        File internalFile = file.getFile();
        long length = internalFile == null ? 0 : internalFile.length();
        return new FileInformation(file.getName(), length, false, "");
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
    @RequestMapping("/secure/stagedJobFiles.do")
    public ModelAndView stagedJobFiles(@RequestParam("jobId") String jobId, @AuthenticationPrincipal ANVGLUser user) {

        //Lookup our job
        VEGLJob job = null;
        try {
            job = attemptGetJob(Integer.parseInt(jobId), user);
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }

        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        //Get our files
        StagedFile[] files = null;
        try {
            files = fileStagingService.listStageInDirectoryFiles(job);
        } catch (Exception ex) {
            logger.error("Error listing job stage in directory", ex);
            return generateJSONResponseMAV(false, null, "Error reading job stage in directory");
        }
        List<FileInformation> fileInfos = new ArrayList<>();
        for (StagedFile file : files) {
            fileInfos.add(stagedFileToFileInformation(file));
        }

        return generateJSONResponseMAV(true, fileInfos, "");
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
     * @throws IOException
     */
    @RequestMapping("/secure/downloadInputFile.do")
    public ModelAndView downloadFile(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") String jobId,
            @RequestParam("filename") String filename, @AuthenticationPrincipal ANVGLUser user) throws Exception {

        //Lookup our job and download the specified files (any exceptions will return a HTTP 503)
        VEGLJob job = attemptGetJob(Integer.parseInt(jobId), user);
        if (job == null) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Couldnt access job with that ID");
            return null;
        }
        fileStagingService.handleFileDownload(job, filename, response);
        return null;
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
    @RequestMapping("/secure/uploadFile.do")
    public ModelAndView uploadFile(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") String jobId, @AuthenticationPrincipal ANVGLUser user) {

        //Lookup our job
        VEGLJob job = null;
        try {
            job = attemptGetJob(Integer.parseInt(jobId), user);
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }

        if (job == null) {
            return generateHTMLResponseMAV(false, null, "");
        }

        //Handle incoming file
        StagedFile file = null;
        try {
            file = fileStagingService.handleFileUpload(job, (MultipartHttpServletRequest) request);
        } catch (Exception ex) {
            logger.error("Error uploading file", ex);
            return generateJSONResponseMAV(false, null, "Error uploading file");
        }
        FileInformation fileInfo = stagedFileToFileInformation(file);

        //We have to use a HTML response due to ExtJS's use of a hidden iframe for file uploads
        //Failure to do this will result in the upload working BUT the user will also get prompted
        //for a file download containing the encoded response from this function (which we don't want).
        return generateHTMLResponseMAV(true, Arrays.asList(fileInfo), "");
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
    @RequestMapping("/secure/deleteFiles.do")
    public ModelAndView deleteFiles(@RequestParam("jobId") String jobId,
            @RequestParam("fileName") String[] fileNames, @AuthenticationPrincipal ANVGLUser user) {

        VEGLJob job = null;
        try {
            job = attemptGetJob(Integer.parseInt(jobId), user);
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }

        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        for (String fileName : fileNames) {
            boolean success = fileStagingService.deleteStageInFile(job, fileName);
            logger.debug("Deleting " + fileName + " success=" + success);
        }

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Deletes one or more job downloads for the current job.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object with a success attribute that indicates whether
     *         the downloads were successfully deleted.
     */
    @RequestMapping("/secure/deleteDownloads.do")
    public ModelAndView deleteDownloads(@RequestParam("jobId") String jobId,
            @RequestParam("downloadId") Integer[] downloadIds, @AuthenticationPrincipal ANVGLUser user) {

        VEGLJob job = null;
        try {
            job = attemptGetJob(Integer.parseInt(jobId), user);
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }

        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        //Delete the specified ID's
        Iterator<VglDownload> dlIterator = job.getJobDownloads().iterator();
        while (dlIterator.hasNext()) {
            VglDownload dl = dlIterator.next();
            for (Integer id : downloadIds) {
                if (id.equals(dl.getId())) {
                    dlIterator.remove();
                    break;
                }
            }
        }

        try {
            jobManager.saveJob(job);
        } catch (Exception ex) {
            logger.error("Error saving job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error saving job with id " + jobId);
        }

        return generateJSONResponseMAV(true, null, "");
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
    @RequestMapping("/secure/getJobStatus.do")
    public ModelAndView getJobStatus(@RequestParam("jobId") String jobId, @AuthenticationPrincipal ANVGLUser user) {

        //Get our job
        VEGLJob job = null;
        try {
            job = attemptGetJob(Integer.parseInt(jobId), user);
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }

        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        return generateJSONResponseMAV(true, job.getStatus(), "");
    }

    /**
     * Cancels the current job submission. Called to clean up temporary files.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return null
     */
    @RequestMapping("/secure/cancelSubmission.do")
    public ModelAndView cancelSubmission(@RequestParam("jobId") String jobId, @AuthenticationPrincipal ANVGLUser user) {

        //Get our job
        VEGLJob job = null;
        try {
            job = attemptGetJob(Integer.parseInt(jobId), user);
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }

        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        boolean success = fileStagingService.deleteStageInDirectory(job);
        return generateJSONResponseMAV(success, null, "");
    }

    /**
     * This is for converting our String dates (frontend) to actual data objects (backend).
     *
     * Date format will match CloudJob.DATE_FORMAT, null/empty strings will be bound as NULL
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        CustomDateEditor editor = new CustomDateEditor(new SimpleDateFormat(CloudJob.DATE_FORMAT), true);
        binder.registerCustomEditor(Date.class, editor);
    }


    /**
     * Given an entire job object this function attempts to save the specified job with ID
     * to the internal database. If the Job DNE (or id is null), the job will be created and
     * have it's staging area initialised and other creation specific tasks performed.
     *
     * @return A JSON object with a success attribute that indicates whether
     *         the job was successfully updated. The data object will contain the updated job
     * @return
     * @throws ParseException
     */
    @RequestMapping("/secure/updateOrCreateJob.do")
    public ModelAndView updateOrCreateJob(@RequestParam(value="id", required=false) Integer id,  //The integer ID if not specified will trigger job creation
            @RequestParam(value="name", required=false) String name,
            @RequestParam(value="description", required=false) String description,
            @RequestParam(value="seriesId", required=false) Integer seriesId,
            @RequestParam(value="computeServiceId", required=false) String computeServiceId,
            @RequestParam(value="computeVmId", required=false) String computeVmId,
            @RequestParam(value="computeTypeId", required=false) String computeTypeId,
            @RequestParam(value="storageServiceId", required=false) String storageServiceId,
            @RequestParam(value="registeredUrl", required=false) String registeredUrl,
            @RequestParam(value="emailNotification", required=false) boolean emailNotification,
            @RequestParam(value="walltime", required=false) Integer walltime,
            HttpServletRequest request,
            @AuthenticationPrincipal ANVGLUser user) {

        //Get our job
        VEGLJob job = null;
        try {
            //If we have an ID - look up the job, otherwise create a job
            if (id == null) {
                //Job creation involves a fair bit of initialisation on the server
                job = initialiseVEGLJob(request.getSession(), user);
            } else {
                job = attemptGetJob(id, user);
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception ex) {
            logger.error(String.format("Error creating/fetching job with id %1$s", id), ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + id);
        }

        if (job == null) {
            logger.error(String.format("Error creating/fetching job with id %1$s", id));
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + id);
        }

        //JSON encoding of series ID can sometimes turn a null into a 0. We will also never have a seriesId of 0
        if (seriesId != null && seriesId == 0) {
            seriesId = null;
        }

        //Update our job from the request parameters
        job.setSeriesId(seriesId);
        job.setName(name);
        job.setDescription(description);
        job.setComputeVmId(computeVmId);
        job.setComputeInstanceType(computeTypeId);
        job.setEmailNotification(emailNotification);
        job.setWalltime(walltime);

        //Updating the storage service means changing the base key
        if (storageServiceId != null) {
            CloudStorageService css = getStorageService(storageServiceId);
            if (css == null) {
                logger.error(String.format("Error fetching storage service with id %1$s", storageServiceId));
                return generateJSONResponseMAV(false, null, "Storage service does not exist");
            }

            job.setStorageServiceId(storageServiceId);
            job.setStorageBaseKey(css.generateBaseKey(job));
        } else {
            job.setStorageServiceId(null);
            job.setStorageBaseKey(null);
        }

        //Dont allow the user to specify a cloud compute service that DNE
        // Updating the compute service means updating the dev keypair
        if (computeServiceId != null) {
            CloudComputeService ccs = getComputeService(computeServiceId);
            if (ccs == null) {
                logger.error(String.format("Error fetching compute service with id %1$s", computeServiceId));
                return generateJSONResponseMAV(false, null, "No compute/storage service with those ID's");
            }

            job.setComputeServiceId(computeServiceId);
        } else {
            job.setComputeServiceId(null);
        }

        //Save the VEGL job
        try {
            jobManager.saveJob(job);
        } catch (Exception ex) {
            logger.error("Error updating job " + job, ex);
            return generateJSONResponseMAV(false, null, "Error saving job");
        }

        return generateJSONResponseMAV(true, Arrays.asList(job), "");
    }

    /**
     * Given an entire job object this function attempts to save the specified job with ID
     * to the internal database. If the Job DNE (or id is null), the job will be created and
     * have it's staging area initialised and other creation specific tasks performed.
     *
     * @return A JSON object with a success attribute that indicates whether
     *         the job was successfully updated. The data object will contain the updated job
     * @return
     * @throws ParseException
     */
    @RequestMapping("/secure/updateJobSeries.do")
    public ModelAndView updateJobSeries(@RequestParam(value="id", required=true) Integer id,  //The integer ID if not specified will trigger job creation
            @RequestParam(value="folderName", required=true) String folderName, //Name of the folder to move to
            HttpServletRequest request,
            @AuthenticationPrincipal ANVGLUser user) {

        //Get our job
        VEGLJob job = null;
        Integer seriesId=null;
        List<VEGLSeries> series = jobManager.querySeries(user.getEmail(), folderName, null);
        if(series.isEmpty()){
            return generateJSONResponseMAV(false, null,"Series not found");
        }else{
            seriesId=series.get(0).getId();
        }

        try {
            job = attemptGetJob(id, user);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception ex) {
            logger.error(String.format("Error creating/fetching job with id %1$s", id), ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + id);
        }

        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        //Update our job from the request parameters
        job.setSeriesId(seriesId);

        //Save the VEGL job
        try {
            jobManager.saveJob(job);
        } catch (Exception ex) {
            logger.error("Error updating series for job " + job, ex);
            return generateJSONResponseMAV(false, null, "Error updating series");
        }

        return generateJSONResponseMAV(true, Arrays.asList(job), "");
    }

    /**
     * Given a job with specified ID and a list of download objects,
     * save the download objects to the database.
     *
     * The download objects are defined piecewise as an array of name/description/url and localPath values.
     *
     * The Nth download object will be defined as a combination of
     * names[N], descriptions[N], urls[N] and localPaths[N]
     *
     * @param append If true, the parsed downloaded will append themselves to the existing job. If false, they will replace all downloads for the existing job
     * @return
     * @throws ParseException
     */
    @RequestMapping("/secure/updateJobDownloads.do")
    public ModelAndView updateJobDownloads(@RequestParam("id") Integer id,  //The integer ID is the only required value
            @RequestParam(required=false, value="append", defaultValue="false") String appendString,
            @RequestParam("name") String[] names,
            @RequestParam("description") String[] descriptions,
            @RequestParam("url") String[] urls,
            @RequestParam("localPath") String[] localPaths,
            @AuthenticationPrincipal ANVGLUser user) {

        boolean append = Boolean.parseBoolean(appendString);

        List<VglDownload> parsedDownloads = new ArrayList<>();
        for (int i = 0; i < urls.length && i < names.length && i < descriptions.length && i < localPaths.length; i++) {
            VglDownload newDl = new VglDownload();
            newDl.setName(names[i]);
            newDl.setDescription(descriptions[i]);
            newDl.setUrl(urls[i]);
            newDl.setLocalPath(localPaths[i]);
            parsedDownloads.add(newDl);
        }

        //Lookup the job
        VEGLJob job;
        try {
            job = attemptGetJob(id, user);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception ex) {
            logger.error("Error looking up job with id " + id + " :" + ex.getMessage());
            logger.debug("Exception:", ex);
            return generateJSONResponseMAV(false, null, "Unable to access job");
        }

        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        List<VglDownload> existingDownloads = job.getJobDownloads();
        if (append) {
            existingDownloads.addAll(parsedDownloads);
            job.setJobDownloads(existingDownloads);
        } else {
            job.setJobDownloads(parsedDownloads);
        }

        //Save the VEGL job
        try {
            jobManager.saveJob(job);
        } catch (Exception ex) {
            logger.error("Error updating job downloads" + job, ex);
            return generateJSONResponseMAV(false, null, "Error saving job");
        }

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Given the ID of a job - lookup the appropriate job object and associated list of downloads objects.
     *
     * Return them as an array of JSON serialised VglDownload objects.
     * @param jobId
     * @return
     */
    @RequestMapping("/secure/getJobDownloads.do")
    public ModelAndView getJobDownloads(@RequestParam("jobId") Integer jobId, @AuthenticationPrincipal ANVGLUser user) {
        //Lookup the job
        VEGLJob job;
        try {
            job = attemptGetJob(jobId, user);
        } catch (Exception ex) {
            logger.error("Error looking up job with id " + jobId + " :" + ex.getMessage());
            logger.debug("Exception:", ex);
            return generateJSONResponseMAV(false, null, "Unable to access job");
        }

        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        return generateJSONResponseMAV(true, job.getJobDownloads(), "");
    }


    //    /**
    //     * Gets the list of authorised images for the specified job owned by user
    //     * @param request The request (from a user) making the query
    //     * @param job The job for which the images will be tested
    //     * @return
    //     */
    //    private List<MachineImage> getImagesForJobAndUser(HttpServletRequest request, VEGLJob job) {
    //        return getImagesForJobAndUser(request, job.getComputeServiceId());
    //    }

    /**
     * Gets the list of authorised images for the specified job owned by user
     * @param request The request (from a user) making the query
     * @param computeServiceId The compute service ID to search for images
     * @return
     */
    private List<MachineImage> getImagesForJobAndUser(HttpServletRequest request, String computeServiceId) {
        CloudComputeService ccs = getComputeService(computeServiceId);
        if (ccs == null) {
            return new ArrayList<>();
        }

        List<MachineImage> authorisedImages = new ArrayList<>();

        for (MachineImage img : ccs.getAvailableImages()) {
            if (img instanceof VglMachineImage) {
                //If the image has no permission restrictions, add it. Otherwise
                //ensure that the user has a role matching something in the image permission list
                String[] permissions = ((VglMachineImage) img).getPermissions();
                if (permissions == null) {
                    authorisedImages.add(img);
                } else {
                    for (String validRole : permissions) {
                        if (request.isUserInRole(validRole)) {
                            authorisedImages.add(img);
                            break;
                        }
                    }
                }
            } else {
                authorisedImages.add(img);
            }
        }

        return authorisedImages;
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
    @RequestMapping("/secure/submitJob.do")
    public ModelAndView submitJob(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("jobId") String jobId,
            @AuthenticationPrincipal ANVGLUser user) {
        boolean succeeded = false;
        String oldJobStatus = null, errorDescription = null, errorCorrection = null;
        VEGLJob curJob = null;
        boolean containsPersistentVolumes = false;

        try {
            // Get our job
            curJob = attemptGetJob(Integer.parseInt(jobId), user);
            if (curJob == null) {
                logger.error("Error fetching job with id " + jobId);
                errorDescription = "There was a problem retrieving your job from the database.";
                String admin = getAdminEmail();
                if(TextUtil.isNullOrEmpty(admin)) {
                    admin = "the portal admin";
                }
                errorCorrection = "Please try again in a few minutes or report it to "+admin+".";
                return generateJSONResponseMAV(false, null, errorDescription, errorCorrection);
            } else {

                CloudStorageService cloudStorageService = getStorageService(curJob);
                CloudComputeService cloudComputeService = getComputeService(curJob);
                if (cloudStorageService == null || cloudComputeService == null) {
                    errorDescription = "One of the specified storage/compute services cannot be found.";
                    errorCorrection = "Consider changing the selected compute or storage service.";
                    return generateJSONResponseMAV(false, null, errorDescription, errorCorrection);
                }

                // we need to keep track of old job for audit trail purposes
                oldJobStatus = curJob.getStatus();
                // Assume user has permission since we're using images from the SSC
                boolean permissionGranted = true;

                // // check to ensure user has permission to run the job
                // // boolean permissionGranted = false;

                // String jobImageId = curJob.getComputeVmId();
                // List<MachineImage> images = getImagesForJobAndUser(request, curJob);
                // for (MachineImage vglMachineImage : images) {
                //     if (vglMachineImage.getImageId().equals(jobImageId)) {
                //         permissionGranted = true;
                //         break;
                //     }
                // }

                if (permissionGranted) {
                    // Right before we submit - pump out a script file for downloading every VglDownload object when the VM starts
                    if (!createDownloadScriptFile(curJob, DOWNLOAD_SCRIPT)) {
                        logger.error(String.format("Error creating download script '%1$s' for job with id %2$s", DOWNLOAD_SCRIPT, jobId));
                        errorDescription = "There was a problem configuring the data download script.";
                        String admin = getAdminEmail();
                        if(TextUtil.isNullOrEmpty(admin)) {
                            admin = "the portal admin";
                        }
                        errorCorrection = "Please try again in a few minutes or report it to "+admin+".";
                    } else {
                        // copy files to S3 storage for processing
                        // get job files from local directory
                        StagedFile[] stagedFiles = fileStagingService.listStageInDirectoryFiles(curJob);
                        if (stagedFiles.length == 0) {
                            errorDescription = "There wasn't any input files found for submitting your job for processing.";
                            errorCorrection = "Please upload your input files and try again.";
                        } else {
                            // Upload them to storage
                            File[] files = new File[stagedFiles.length];
                            for (int i = 0; i < stagedFiles.length; i++) {
                                files[i] = stagedFiles[i].getFile();
                            }

                            cloudStorageService.uploadJobFiles(curJob, files);

                            // create our input user data string
                            String userDataString = null;
                            userDataString = createBootstrapForJob(curJob);

                            // Provenance
                            anvglProvenanceService.setServerURL(request.getRequestURL().toString());
                            anvglProvenanceService.createActivity(curJob, scmEntryService.getJobSolutions(curJob), user);

                            //ANVGL-120 Check for persistent volumes
                            if (cloudComputeService instanceof CloudComputeServiceAws) {
                                containsPersistentVolumes = ((CloudComputeServiceAws) cloudComputeService).containsPersistentVolumes(curJob);
                                curJob.setContainsPersistentVolumes(containsPersistentVolumes);
                            }

                            oldJobStatus = curJob.getStatus();
                            curJob.setStatus(JobBuilderController.STATUS_PROVISION);
                            jobManager.saveJob(curJob);
                            jobManager.createJobAuditTrail(oldJobStatus, curJob, "Set job to provisioning");

                            ExecutorService es = Executors.newSingleThreadExecutor();
                            es.execute(new CloudThreadedExecuteService(cloudComputeService,curJob,userDataString));
                            succeeded = true;
                        }
                    }
                } else {
                    errorDescription = "You do not have the permission to submit this job for processing.";
                    String admin = getAdminEmail();
                    if(TextUtil.isNullOrEmpty(admin)) {
                        admin = "the portal admin";
                    }
                    errorCorrection = "If you think this is wrong, please report it to "+admin+".";
                }
            }
        } catch (PortalServiceException e) {
            errorDescription = e.getMessage();
            errorCorrection = e.getErrorCorrection();

            //These are our "STS specific" overrides to some error messages (not an ideal solution but I don't want to have to overhaul everything just to tweak a string).
            if (errorDescription.equals("Storage credentials are not valid.")) {
                errorDescription = "Unable to upload job script and/or input files";
                errorCorrection = "The most likely cause is that your user profile ARN's have been misconfigured.";
            }
        } catch (IOException e) {
            logger.error("Job bootstrap creation failed.", e);
            errorDescription = "There was a problem creating startup script.";
            errorCorrection = "Please report this error to "+getAdminEmail();
        } catch (AccessDeniedException e) {
            logger.error("Job submission failed.", e);
            if (curJob == null) {
                errorDescription = "You are not authorized to access the specified job";
            } else {
                errorDescription = "You are not authorized to access the specified job with id: "+ curJob.getId();
            }
            errorCorrection = "Please report this error to "+getAdminEmail();
        } catch (Exception e) {
            logger.error("Job submission failed.", e);
            errorDescription = "An unexpected error has occurred while submitting your job for processing.";
            errorCorrection = "Please report this error to "+getAdminEmail();
        }

        if (succeeded) {
            ModelMap responseModel = new ModelMap();
            responseModel.put("containsPersistentVolumes", containsPersistentVolumes);
            return generateJSONResponseMAV(true, responseModel, "");
        } else {
            jobManager.createJobAuditTrail(oldJobStatus, curJob, errorDescription);
            return generateJSONResponseMAV(false, null, errorDescription, errorCorrection);
        }
    }

    private class CloudThreadedExecuteService implements Runnable{
        CloudComputeService cloudComputeService;
        VEGLJob curJob;
        String userDataString;

        public CloudThreadedExecuteService(CloudComputeService cloudComputeService,VEGLJob curJob,String userDataString){
            this.cloudComputeService = cloudComputeService;
            this.curJob = curJob;
            this.userDataString = userDataString;
        }

        @Override
        public void run() {
            String instanceId = null;
            try{
                instanceId = cloudComputeService.executeJob(curJob, userDataString);
                logger.info("Launched instance: " + instanceId);
                // set reference as instanceId for use when killing a job
                curJob.setComputeInstanceId(instanceId);
                String oldJobStatus = curJob.getStatus();
                curJob.setStatus(STATUS_PENDING);
                jobManager.createJobAuditTrail(oldJobStatus, curJob, "Set job to Pending");
                curJob.setSubmitDate(new Date());
                jobManager.saveJob(curJob);

            }catch(PortalServiceException e){
                //only for this specific error we wanna queue the job
                if(e.getErrorCorrection()!= null && e.getErrorCorrection().contains("Quota exceeded")){
                    vglPollingJobQueueManager.addJobToQueue(new VGLQueueJob(jobManager,cloudComputeService,curJob,userDataString,vglJobStatusChangeHandler));
                    String oldJobStatus = curJob.getStatus();
                    curJob.setStatus(JobBuilderController.STATUS_INQUEUE);
                    jobManager.saveJob(curJob);
                    jobManager.createJobAuditTrail(oldJobStatus, curJob, "Job Placed in Queue");
                }else{
                    String oldJobStatus = curJob.getStatus();
                    curJob.setStatus(JobBuilderController.STATUS_ERROR);
                    jobManager.saveJob(curJob);
                    jobManager.createJobAuditTrail(oldJobStatus, curJob, e);
                    vglJobStatusChangeHandler.handleStatusChange(curJob,curJob.getStatus(),oldJobStatus);
                }
            }
        }
    }

    /**
     * Creates a new VEGL job initialised with the default configuration values. The job will be persisted into the database.
     *
     * The Job MUST be associated with a specific compute and storage service. Staging areas and other bits and pieces relating to the job will also be initialised.
     *
     * @param email
     * @return
     */
    private VEGLJob initialiseVEGLJob(HttpSession session, ANVGLUser user) throws PortalServiceException {
        VEGLJob job = new VEGLJob();

        //Start by saving our job to set its ID
        jobManager.saveJob(job);
        log.debug(String.format("Created a new job row id=%1$s", job.getId()));

        job.setProperty(CloudJob.PROPERTY_STS_ARN, user.getArnExecution());
        job.setProperty(CloudJob.PROPERTY_CLIENT_SECRET, user.getAwsSecret());
        job.setProperty(CloudJob.PROPERTY_S3_ROLE, user.getArnStorage());
        job.setComputeInstanceKey(user.getAwsKeyName());
        job.setStorageBucket(user.getS3Bucket());

        //Iterate over all session variables - set them up as job parameters
        @SuppressWarnings("rawtypes")
        final
        Enumeration sessionVariables = session.getAttributeNames();
        while (sessionVariables.hasMoreElements()) {
            String variableName = sessionVariables.nextElement().toString();
            Object variableValue = session.getAttribute(variableName);
            String variableStringValue = null;
            ParameterType variableType = null;

            //Only session variables of a number or string will be considered
            if (variableValue instanceof Integer || variableValue instanceof Double) {
                variableType = ParameterType.number;
                variableStringValue = variableValue.toString();
            } else if (variableValue instanceof String) {
                variableType = ParameterType.string;
                variableStringValue = (String) variableValue;
            } else {
                continue;//Don't bother with other types
            }

            job.setJobParameter(variableName, variableStringValue, variableType);
        }

        //Load details from
        job.setUser(user.getEmail());
        job.setEmailAddress(user.getEmail());
        // Get keypair name from CloudComputeService later
        // job.setComputeInstanceKey("vgl-developers");
        job.setName("VL-Job " + new Date().toString());
        job.setDescription("");
        job.setStatus(STATUS_UNSUBMITTED);
        job.setSubmitDate(new Date());

        //Transfer the 'session downloads' into actual download objects associated with a job
        @SuppressWarnings("unchecked")
        final
        List<VglDownload> erddapDownloads = (List<VglDownload>) session.getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);
        session.setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, null); //ensure we clear the list out in case the user makes more jobs
        if (erddapDownloads != null) {
            job.setJobDownloads(new ArrayList<>(erddapDownloads));
        } else {
            logger.warn("No downloads configured for user session!");
        }

        //Save our job to the database before setting up staging directories (we need an ID!!)
        jobManager.saveJob(job);
        jobManager.createJobAuditTrail(null, job, "Job created.");

        //Finally generate our stage in directory for persisting inputs
        fileStagingService.generateStageInDirectory(job);

        return job;
    }

    /**
     * This function creates a file "vgl_download.sh" which contains the bash script
     * for downloading every VglDownload associated with the specified job.
     *
     * The script file will be written to the staging area for job as
     * @param job The job to generate
     * @param fileName the file name of the generated script
     * @return
     */
    private boolean createDownloadScriptFile(VEGLJob job, String fileName) {
        try (OutputStream os = fileStagingService.writeFile(job,  fileName);
             OutputStreamWriter out = new OutputStreamWriter(os)) {

            for (VglDownload dl : job.getJobDownloads()) {
                out.write(String.format("#Downloading %1$s\n", dl.getName()));
                out.write(String.format("curl -f -L '%1$s' -o \"%2$s\"\n", dl.getUrl(), dl.getLocalPath()));
            }

            return true;
        } catch (Exception e) {
            logger.error("Error creating download script" +  e.getMessage());
            logger.debug("Error:", e);
            return false;
        }
    }

    /**
     * Request wrapper to get the default toolbox uri.
     *
     */
    @RequestMapping("/getDefaultToolbox.do")
    public ModelAndView doGetDefaultToolbox() {
        return generateJSONResponseMAV(true, new String[] {getDefaultToolbox()}, "");
    }

    /**
     * Gets the set of cloud images available for use by a particular user.
     *
     * If jobId is specified, limit the set to images that are
     * compatible with the solution selected for the job.
     *
     * @param request
     * @param computeServiceId
     * @param jobId (optional) id of a job to limit suitable images
     * @return
     */
    @RequestMapping("/secure/getVmImagesForComputeService.do")
    public ModelAndView getImagesForComputeService(
            HttpServletRequest request,
            @RequestParam("computeServiceId") String computeServiceId,
            @RequestParam(value="jobId", required=false) Integer jobId,
            @AuthenticationPrincipal ANVGLUser user) {
        try {
            // Assume all images are usable by the current user
            List<MachineImage> images = new ArrayList<>();

            if (jobId != null) {
                VEGLJob job = attemptGetJob(jobId, user);
                if (job == null) {
                    return generateJSONResponseMAV(false);
                }

                // Filter list to images suitable for job solutions, if specified.
                Set<Toolbox> toolboxes = scmEntryService.getJobToolboxes(job);

                // With multiple solutions and multiple toolboxes, do
                // not give the user the option of selecting the default
                // portal toolbox for utility functions unless it's the
                // only toolbox available.
                int numToolboxes = toolboxes.size();
                for (Toolbox toolbox: toolboxes) {
                    if ((numToolboxes == 1) ||
                            !toolbox.getUri().equals(this.defaultToolbox)) {
                        images.add(scmEntryService
                                .getToolboxImage(toolbox,
                                        computeServiceId));
                    }
                }
            }

            if (images.isEmpty()) {
                // Fall back on old behaviour based on configured images for now
                // Get images available to the current user
                images = getImagesForJobAndUser(request, computeServiceId);
            }

            if (images.isEmpty()) {
                // There are no suitable images at the specified compute service.
                log.warn("No suitable images at compute service (" + computeServiceId + ") for job (" + jobId + ")");
            }

            // return result
            return generateJSONResponseMAV(true, images, "");
        } catch (Exception ex) {
            log.error("Unable to access image list:" + ex.getMessage(), ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Return a JSON list of VM types available for the compute service.
     *
     * @param computeServiceId
     */
    @RequestMapping("/secure/getVmTypesForComputeService.do")
    public ModelAndView getTypesForComputeService(HttpServletRequest request,
            @RequestParam("computeServiceId") String computeServiceId,
            @RequestParam("machineImageId") String machineImageId) {
        try {
            CloudComputeService ccs = getComputeService(computeServiceId);
            if (ccs == null) {
                return generateJSONResponseMAV(false, null, "Unknown compute service");
            }

            List<MachineImage> images = getImagesForJobAndUser(request, computeServiceId);
            MachineImage selectedImage = null;
            for (MachineImage image : images) {
                if (image.getImageId().equals(machineImageId)) {
                    selectedImage = image;
                    break;
                }
            }

            ComputeType[] allTypes = null;
            if (selectedImage == null) {
                // Unknown image, presumably from the SSSC, so start with all
                // compute types for the selected compute service.
                allTypes = ccs.getAvailableComputeTypes();
            }
            else {
                //Grab the compute types that are compatible with our disk
                //requirements
                allTypes = ccs.getAvailableComputeTypes(null, null, selectedImage.getMinimumDiskGB());
            }

            return generateJSONResponseMAV(true, allTypes, "");
        } catch (Exception ex) {
            log.error("Unable to access compute type list:" + ex.getMessage(), ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Gets a JSON list of id/name pairs for every available compute service
     *
     * If a jobId parameter is provided, then return compute services
     * compatible with that job. Currently that is only those services
     * that have images available for the solution used for the job.
     *
     * @param jobId (optional) job id to limit acceptable services
     * @return
     */
    @RequestMapping("/secure/getComputeServices.do")
    public ModelAndView getComputeServices(@RequestParam(value="jobId",
    required=false) final
            Integer jobId,
            @AuthenticationPrincipal ANVGLUser user) {
        Set<String> jobCCSIds;
        try {
            jobCCSIds = scmEntryService.getJobProviders(jobId, user);
        } catch (AccessDeniedException e) {
            throw e;
        }

        List<ModelMap> simpleComputeServices = new ArrayList<>();

        for (CloudComputeService ccs : cloudComputeServices) {
            // Add the ccs to the list if it's valid for job or we have no job
            if (jobCCSIds == null || jobCCSIds.contains(ccs.getId())) {
                ModelMap map = new ModelMap();
                map.put("id", ccs.getId());
                map.put("name", ccs.getName());
                simpleComputeServices.add(map);
            }
        }

        return generateJSONResponseMAV(true, simpleComputeServices, "");
    }

    /**
     * Gets a JSON list of id/name pairs for every available storage service
     * @return
     */
    @RequestMapping("/secure/getStorageServices.do")
    public ModelAndView getStorageServices() {
        List<ModelMap> simpleStorageServices = new ArrayList<>();

        for (CloudStorageService ccs : cloudStorageServices) {
            ModelMap map = new ModelMap();
            map.put("id", ccs.getId());
            map.put("name", ccs.getName());
            simpleStorageServices.add(map);
        }

        return generateJSONResponseMAV(true, simpleStorageServices, "");
    }

    /**
     * A combination of getJobInputFiles and getJobDownloads. This function amalgamates the list
     * of remote service downloads and local file uploads into a single list of input files that
     * are available to a job at startup.
     *
     * The results will be presented in an array of VglDownload objects
     * @param jobId
     * @return
     */
    @RequestMapping("/secure/getAllJobInputs.do")
    public ModelAndView getAllJobInputs(@RequestParam("jobId") Integer jobId, @AuthenticationPrincipal ANVGLUser user) {
        VEGLJob job = null;
        try {
            job = attemptGetJob(jobId, user);
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }

        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        //Get our files
        StagedFile[] files = null;
        try {
            files = fileStagingService.listStageInDirectoryFiles(job);
        } catch (Exception ex) {
            logger.error("Error listing job stage in directory", ex);
            return generateJSONResponseMAV(false, null, "Error reading job stage in directory");
        }

        //Load the staged files
        List<VglDownload> allInputs = new ArrayList<>();
        int idCounter = Integer.MIN_VALUE;
        for (StagedFile file : files) {
            //we need unique ids - this is our simple way of generating them (low likelyhood of collision)
            //if we have a collision the GUI might not show one entry - it's not the end of the world
            VglDownload dl = new VglDownload(idCounter++);
            dl.setName(file.getName());
            dl.setLocalPath(file.getName());

            allInputs.add(dl);
        }

        //Load the job downloads
        allInputs.addAll(job.getJobDownloads());

        return generateJSONResponseMAV(true, allInputs, "");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value =  org.springframework.http.HttpStatus.FORBIDDEN)
    public @ResponseBody String handleException(AccessDeniedException e) {
        return e.getMessage();
    }

    /**
     * Return the default toolbox URI.
     *
     * @returns String with the URI for the default toolbox.
     */
    public String getDefaultToolbox() {
        return this.defaultToolbox;
    }

    /**
     * Set the default toolbox URI.
     *
     * @param defaultToolbox String containing the URI of the default toolbox to set
     */
    public void setDefaultToolbox(String defaultToolbox) {
        this.defaultToolbox = defaultToolbox;
    }
}

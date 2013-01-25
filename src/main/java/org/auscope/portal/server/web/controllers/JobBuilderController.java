package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.cloud.StagedFile;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.vegl.VglMachineImage;
import org.auscope.portal.server.vegl.VglParameter.ParameterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private VEGLJobManager jobManager;
    private FileStagingService fileStagingService;
    private PortalPropertyPlaceholderConfigurer hostConfigurer;

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_DONE = "Done";
    public static final String STATUS_DELETED = "Deleted";
    public static final String STATUS_UNSUBMITTED = "Saved";

    public static final String SUBMIT_DATE_FORMAT_STRING = "yyyyMMdd_HHmmss";

    public static final String DOWNLOAD_SCRIPT = "vgl-download.sh";

    @Autowired
    public JobBuilderController(VEGLJobManager jobManager, FileStagingService fileStagingService,
            PortalPropertyPlaceholderConfigurer hostConfigurer, CloudStorageService[] cloudStorageServices,
            CloudComputeService[] cloudComputeServices) {
        super(cloudStorageServices, cloudComputeServices);
        this.jobManager = jobManager;
        this.fileStagingService = fileStagingService;
        this.hostConfigurer = hostConfigurer;
        this.cloudStorageServices = cloudStorageServices;
        this.cloudComputeServices = cloudComputeServices;
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
    @RequestMapping("/getJobObject.do")
    public ModelAndView getJobObject(@RequestParam("jobId") String jobId) {
        try {
            VEGLJob job = jobManager.getJobById(Integer.parseInt(jobId));

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
    private FileInformation stagedFileToFileInformation(StagedFile file) {
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
    @RequestMapping("/listJobFiles.do")
    public ModelAndView listJobFiles(@RequestParam("jobId") String jobId) {

        //Lookup our job
        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
        }

        //Get our files
        StagedFile[] files = null;
        try {
            files = fileStagingService.listStageInDirectoryFiles(job);
        } catch (Exception ex) {
            logger.error("Error listing job stage in directory", ex);
            return generateJSONResponseMAV(false, null, "Error reading job stage in directory");
        }
        List<FileInformation> fileInfos = new ArrayList<FileInformation>();
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
    @RequestMapping("/downloadInputFile.do")
    public ModelAndView downloadFile(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam("jobId") String jobId,
                                     @RequestParam("filename") String filename) throws Exception {

        //Lookup our job and download the specified files (any exceptions will return a HTTP 503)
        VEGLJob job = jobManager.getJobById(Integer.parseInt(jobId));
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
    @RequestMapping("/uploadFile.do")
    public ModelAndView uploadFile(HttpServletRequest request,
                                    HttpServletResponse response,
                                   @RequestParam("jobId") String jobId) {

        //Lookup our job
        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
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
    @RequestMapping("/deleteFiles.do")
    public ModelAndView deleteFiles(@RequestParam("jobId") String jobId,
                                    @RequestParam("fileName") String[] fileNames) {

        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
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
    @RequestMapping("/deleteDownloads.do")
    public ModelAndView deleteDownloads(@RequestParam("jobId") String jobId,
                                    @RequestParam("downloadId") Integer[] downloadIds) {

        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
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
    @RequestMapping("/getJobStatus.do")
    public ModelAndView getJobStatus(@RequestParam("jobId") String jobId) {

        //Get our job
        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
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
    @RequestMapping("/cancelSubmission.do")
    public ModelAndView cancelSubmission(@RequestParam("jobId") String jobId) {

        //Get our job
        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
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
    @RequestMapping("/updateOrCreateJob.do")
    public ModelAndView updateOrCreateJob(@RequestParam(value="id", required=false) Integer id,  //The integer ID if not specified will trigger job creation
            @RequestParam(value="name", required=false) String name,
            @RequestParam(value="description", required=false) String description,
            @RequestParam(value="seriesId", required=false) Integer seriesId,
            @RequestParam(value="computeServiceId", required=false) String computeServiceId,
            @RequestParam(value="computeVmId", required=false) String computeVmId,
            @RequestParam(value="storageServiceId", required=false) String storageServiceId,
            @RequestParam(value="registeredUrl", required=false) String registeredUrl,
            HttpServletRequest request) throws ParseException {

        //Get our job
        VEGLJob job = null;
        try {
            //If we have an ID - look up the job, otherwise create a job
            if (id == null) {
                //Job creation involves a fair bit of initialisation on the server
                job = initialiseVEGLJob(request.getSession());
            } else {
                job = jobManager.getJobById(id);
            }
        } catch (Exception ex) {
            logger.error(String.format("Error creating/fetching job with id %1$s", id), ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + id);
        }

        //Update our job from the request parameters
        job.setSeriesId(seriesId);
        job.setName(name);
        job.setDescription(description);
        job.setComputeVmId(computeVmId);

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
    @RequestMapping("/updateJobDownloads.do")
    public ModelAndView updateJobDownloads(@RequestParam("id") Integer id,  //The integer ID is the only required value
            @RequestParam(required=false, value="append", defaultValue="false") String appendString,
            @RequestParam("name") String[] names,
            @RequestParam("description") String[] descriptions,
            @RequestParam("url") String[] urls,
            @RequestParam("localPath") String[] localPaths) throws ParseException {

        boolean append = Boolean.parseBoolean(appendString);

        List<VglDownload> parsedDownloads = new ArrayList<VglDownload>();
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
            job = jobManager.getJobById(id);
        } catch (Exception ex) {
            logger.error("Error looking up job with id " + id + " :" + ex.getMessage());
            logger.debug("Exception:", ex);
            return generateJSONResponseMAV(false, null, "Unable to access job");
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
    @RequestMapping("/getJobDownloads.do")
    public ModelAndView getJobDownloads(@RequestParam("jobId") Integer jobId) {
        //Lookup the job
        VEGLJob job;
        try {
            job = jobManager.getJobById(jobId);
        } catch (Exception ex) {
            logger.error("Error looking up job with id " + jobId + " :" + ex.getMessage());
            logger.debug("Exception:", ex);
            return generateJSONResponseMAV(false, null, "Unable to access job");
        }

        return generateJSONResponseMAV(true, job.getJobDownloads(), "");
    }

    /**
     * Loads the bootstrap shell script template as a string.
     * @return
     * @throws IOException
     */
    private String getBootstrapTemplate() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("vgl-bootstrap.sh");
        String template = IOUtils.toString(is);
        return template.replaceAll("\r", ""); //Windows style file endings have a tendency to sneak in via StringWriter and the like
    }

    /**
     * Creates a bootstrap shellscript for job that will be sent to
     * cloud VM instance to kick start the work for job.
     * @param job
     * @return
     * @throws IOException
     */
    public String createBootstrapForJob(VEGLJob job) throws IOException {
        String bootstrapTemplate = getBootstrapTemplate();
        CloudStorageService cloudStorageService = getStorageService(job);

        Object[] arguments = new Object[] {
            cloudStorageService.getBucket(), //STORAGE_BUCKET
            job.getStorageBaseKey().replace("//", "/"), //STORAGE_BASE_KEY_PATH
            cloudStorageService.getAccessKey(), //STORAGE_ACCESS_KEY
            cloudStorageService.getSecretKey(), //STORAGE_SECRET_KEY
            hostConfigurer.resolvePlaceholder("vm.sh"), //WORKFLOW_URL
            cloudStorageService.getEndpoint(), //STORAGE_ENDPOINT
            cloudStorageService.getProvider(), //STORAGE_TYPE
            cloudStorageService.getAuthVersion() == null ? "" : cloudStorageService.getAuthVersion() //STORAGE_AUTH_VERSION
        };

        String result = MessageFormat.format(bootstrapTemplate, arguments);
        return result;
    }

    /**
     * Gets the list of authorised images for the specified job owned by user
     * @param request The request (from a user) making the query
     * @param job The job for which the images will be tested
     * @return
     */
    private List<MachineImage> getImagesForJobAndUser(HttpServletRequest request, VEGLJob job) {
        return getImagesForJobAndUser(request, job.getComputeServiceId());
    }

    /**
     * Gets the list of authorised images for the specified job owned by user
     * @param request The request (from a user) making the query
     * @param computeServiceId The compute service ID to search for images
     * @return
     */
    private List<MachineImage> getImagesForJobAndUser(HttpServletRequest request, String computeServiceId) {
        CloudComputeService ccs = getComputeService(computeServiceId);
        if (ccs == null) {
            return new ArrayList<MachineImage>();
        }

        List<String> userRoles = Arrays.asList((String[])request.getSession().getAttribute("user-roles"));
        List<MachineImage> authorisedImages = new ArrayList<MachineImage>();

        for (MachineImage img : ccs.getAvailableImages()) {
            if (img instanceof VglMachineImage) {
                //If the image has no permission restrictions, add it. Otherwise
                //ensure that the user has a role matching something in the image permission list
                String[] permissions = ((VglMachineImage) img).getPermissions();
                if (permissions == null) {
                    authorisedImages.add(img);
                } else {
                    for (String validRole : permissions) {
                        if (userRoles.contains(validRole)) {
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
                                  @RequestParam("jobId") String jobId) {
        boolean succeeded = false;
        String instanceId = null, oldJobStatus = null, errorDescription = null, errorCorrection = null;
        VEGLJob curJob = null;

        try {
            // Get our job
            curJob = jobManager.getJobById(Integer.parseInt(jobId));
            if (curJob == null) {
                logger.error("Error fetching job with id " + jobId);
                errorDescription = "There was a problem retrieving your job from the database.";
                errorCorrection = "Please try again in a few minutes or report it to cg-admin@csiro.au.";
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

                // final check to ensure user has permission to run the job
                boolean permissionGranted = false;

                String jobImageId = curJob.getComputeVmId();
                List<MachineImage> images = getImagesForJobAndUser(request, curJob);
                for (MachineImage vglMachineImage : images) {
                    if (vglMachineImage.getImageId().equals(jobImageId)) {
                        permissionGranted = true;
                        break;
                    }
                }

                if (permissionGranted) {
                    // Right before we submit - pump out a script file for downloading every VglDownload object when the VM starts
                    if (!createDownloadScriptFile(curJob, DOWNLOAD_SCRIPT)) {
                        logger.error(String.format("Error creating download script '%1$s' for job with id %2$s", DOWNLOAD_SCRIPT, jobId));
                        errorDescription = "There was a problem configuring the data download script.";
                        errorCorrection = "Please try again in a few minutes or report it to cg-admin@csiro.au.";
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

                            // launch the ec2 instance
                            instanceId = cloudComputeService.executeJob(curJob, userDataString);
                            logger.info("Launched instance: " + instanceId);
                            // set reference as instanceId for use when killing a job
                            curJob.setComputeInstanceId(instanceId);
                            curJob.setStatus(STATUS_PENDING);
                            curJob.setSubmitDate(new Date());
                            jobManager.saveJob(curJob);
                            succeeded = true;
                        }
                    }
                } else {
                    errorDescription = "You do not have the permission to submit this job for processing.";
                    errorCorrection = "If you think this is wrong, please report it to cg-admin@csiro.au.";
                }
            }
        } catch (PortalServiceException e) {
            errorDescription = e.getMessage();
            errorCorrection = e.getErrorCorrection();
        } catch (IOException e) {
            logger.error("Job bootstrap creation failed.", e);
            errorDescription = "There was a problem creating startup script.";
            errorCorrection = "Please report this error to cg_admin@csiro.au";
        } catch (Exception e) {
            logger.error("Job submission failed.", e);
            errorDescription = "An unexpected error has occurred while submitting your job for processing.";
            errorCorrection = "Please report this error to cg_admin@csiro.au";
        }

        if (succeeded) {
            jobManager.createJobAuditTrail(oldJobStatus, curJob, "Job submitted.");
            return generateJSONResponseMAV(true, null, "");
        } else {
            jobManager.createJobAuditTrail(oldJobStatus, curJob, errorDescription);
            return generateJSONResponseMAV(false, null, errorDescription, errorCorrection);
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
    private VEGLJob initialiseVEGLJob(HttpSession session) throws PortalServiceException {
        VEGLJob job = new VEGLJob();

        //Start by saving our job to set its ID
        jobManager.saveJob(job);
        log.debug(String.format("Created a new job row id=%1$s", job.getId()));

        //Iterate over all session variables - set them up as job parameters
        @SuppressWarnings("rawtypes")
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
        job.setUser((String) session.getAttribute("openID-Email"));
        job.setEmailAddress((String) session.getAttribute("openID-Email"));
        job.setComputeInstanceType("m1.large");
        job.setComputeInstanceKey("vgl-developers");
        job.setName("VGL-Job " + new Date().toString());
        job.setDescription("");
        job.setStatus(STATUS_UNSUBMITTED);

        //Transfer the 'session downloads' into actual download objects associated with a job
        @SuppressWarnings("unchecked")
        List<VglDownload> erddapDownloads = (List<VglDownload>) session.getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);
        session.setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, null); //ensure we clear the list out in case the user makes more jobs
        if (erddapDownloads != null) {
            job.setJobDownloads(new ArrayList<VglDownload>(erddapDownloads));
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
        OutputStream os = null;
        OutputStreamWriter out = null;
        try {
            os = fileStagingService.writeFile(job,  fileName);
            out = new OutputStreamWriter(os);

            for (VglDownload dl : job.getJobDownloads()) {
                out.write(String.format("#Downloading %1$s\n", dl.getName()));
                out.write(String.format("curl -L '%1$s' > \"%2$s\"\n", dl.getUrl(), dl.getLocalPath()));
            }

            return true;
        } catch (Exception e) {
            logger.error("Error creating download script" +  e.getMessage());
            logger.debug("Error:", e);
            return false;
        } finally {
            FileIOUtil.closeQuietly(out);
            FileIOUtil.closeQuietly(os);
        }
    }

    /**
     * Gets the set of cloud images available for use by a particular user
     * @param request
     * @return
     */
    @RequestMapping("/getVmImagesForComputeService.do")
    public ModelAndView getImagesForComputeService(HttpServletRequest request,
                                        @RequestParam("computeServiceId") String computeServiceId) {
        try {
            List<MachineImage> images = getImagesForJobAndUser(request, computeServiceId);
            return generateJSONResponseMAV(true, images, "");
        } catch (Exception ex) {
            log.error("Unable to access image list:" + ex.getMessage(), ex);
            return generateJSONResponseMAV(false);
        }
    }

    /**
     * Gets a JSON list of id/name pairs for every available compute service
     * @return
     */
    @RequestMapping("/getComputeServices.do")
    public ModelAndView getComputeServices() {
        List<ModelMap> simpleComputeServices = new ArrayList<ModelMap>();

        for (CloudComputeService ccs : cloudComputeServices) {
            ModelMap map = new ModelMap();
            map.put("id", ccs.getId());
            map.put("name", ccs.getName());
            simpleComputeServices.add(map);
        }

        return generateJSONResponseMAV(true, simpleComputeServices, "");
    }

    /**
     * Gets a JSON list of id/name pairs for every available storage service
     * @return
     */
    @RequestMapping("/getStorageServices.do")
    public ModelAndView getStorageServices() {
        List<ModelMap> simpleStorageServices = new ArrayList<ModelMap>();

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
    @RequestMapping("/getAllJobInputs.do")
    public ModelAndView getAllJobInputs(@RequestParam("jobId") Integer jobId) {
        VEGLJob job = null;
        try {
            job = jobManager.getJobById(jobId);
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "Error fetching job with id " + jobId);
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
        List<VglDownload> allInputs = new ArrayList<VglDownload>();
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
}
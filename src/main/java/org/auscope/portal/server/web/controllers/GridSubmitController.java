package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VglMachineImage;
import org.auscope.portal.server.web.service.VglMachineImageService;
import org.jclouds.logging.Logger;
import org.jclouds.rest.AuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.AmazonServiceException;


/**
 * Controller for the job submission view.
 *
 * @author Cihan Altinay
 * @author Abdi Jama
 * @author Josh Vote
 */
@Controller
public class GridSubmitController extends BasePortalController {


    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());

    private VEGLJobManager jobManager;
    private FileStagingService fileStagingService;
    private PortalPropertyPlaceholderConfigurer hostConfigurer;
    private CloudStorageService cloudStorageService;
    private CloudComputeService cloudComputeService;
    private VglMachineImageService vglImageService;

    //This is a file path for a CENTOS VM
    private static final String ERRDAP_SUBSET_VM_FILE_PATH = "/tmp/vegl-subset.csv";

    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_DONE = "Done";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_UNSUBMITTED = "Unsubmitted";

    public static final String SUBMIT_DATE_FORMAT_STRING = "yyyyMMdd_HHmmss";


    @Autowired
    public GridSubmitController(VEGLJobManager jobManager, FileStagingService fileStagingService,
            PortalPropertyPlaceholderConfigurer hostConfigurer, CloudStorageService cloudStorageService,
            CloudComputeService cloudComputeService, VglMachineImageService imageService) {
        this.jobManager = jobManager;
        this.fileStagingService = fileStagingService;
        this.hostConfigurer = hostConfigurer;
        this.cloudStorageService = cloudStorageService;
        this.cloudComputeService = cloudComputeService;
        this.vglImageService = imageService;
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
     * Returns a JSON object containing a newly created VEGLJob object.
     * @param request
     * @return
     */
    @RequestMapping("/createJobObject.do")
    public ModelAndView createJobObject(HttpServletRequest request) {

        //Create our initial job object
        VEGLJob newJob = null;
        try {
             newJob = createDefaultVEGLJob(request.getSession());
        } catch (Exception ex) {
            logger.error("Error saving newly created job", ex);
            return generateJSONResponseMAV(false, null, "Error saving newly created job");
        }

        //Generate our stage in directory
        try {
            fileStagingService.generateStageInDirectory(newJob);
        } catch (Exception ex) {
            logger.error("Error creating input dir", ex);
            return generateJSONResponseMAV(false, null, "Error creating input directory");
        }

        //Create the subset file and dump it in our stage in directory
        HashMap<String,String> erdapUrlMap = (HashMap) request.getSession().getAttribute(ERRDAPController.SESSION_ERRDAP_URL_MAP);
        if (erdapUrlMap != null) {
            createSubsetScriptFile(newJob, erdapUrlMap);
        } else {
            logger.warn("No subset area selected in session!");
        }

        //Save our job to the database before setting up staging directories (we need an ID!!)
        try {
            jobManager.saveJob(newJob);
            return generateJSONResponseMAV(true, Arrays.asList(newJob), "");
        } catch (Exception ex) {
            //On failure make sure we delete the new directory
            fileStagingService.deleteStageInDirectory(newJob);
            logger.error("Error saving edited job", ex);
            return generateJSONResponseMAV(false, null, "Error saving edited job");
        }
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
        File[] files = null;
        try {
            files = fileStagingService.listStageInDirectoryFiles(job);
        } catch (Exception ex) {
            logger.error("Error listing job stage in directory", ex);
            return generateJSONResponseMAV(false, null, "Error reading job stage in directory");
        }
        List<FileInformation> fileInfos = new ArrayList<FileInformation>();
        for (File file : files) {
            fileInfos.add(new FileInformation(file));
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
        File file = null;
        try {
            file = fileStagingService.handleFileUpload(job, (MultipartHttpServletRequest) request);
        } catch (Exception ex) {
            logger.error("Error uploading file", ex);
            return generateJSONResponseMAV(false, null, "Error uploading file");
        }
        FileInformation fileInfo = new FileInformation(file);

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
     * to the internal database.
     *
     * @return A JSON object with a success attribute that indicates whether
     *         the job was successfully updated.
     * @param job
     * @return
     * @throws ParseException
     */
    @RequestMapping("/updateJob.do")
    public ModelAndView updateJob(@RequestParam(value="id", required=true) Integer id,  //The integer ID is the only required value
            @RequestParam(value="name", required=false) String name,
            @RequestParam(value="description", required=false) String description,
            @RequestParam(value="emailAddress", required=false) String emailAddress,
            @RequestParam(value="seriesId", required=false) Integer seriesId,
            @RequestParam(value="status", required=false) String status,
            @RequestParam(value="submitDate", required=false) Date submitDate,
            @RequestParam(value="user", required=false) String user,
            @RequestParam(value="computeInstanceId", required=false) String computeInstanceId,
            @RequestParam(value="computeInstanceKey", required=false) String computeInstanceKey,
            @RequestParam(value="computeInstanceType", required=false) String computeInstanceType,
            @RequestParam(value="computeVmId", required=false) String computeVmId,
            @RequestParam(value="storageAccessKey", required=false) String storageAccessKey,
            @RequestParam(value="storageBaseKey", required=false) String storageBaseKey,
            @RequestParam(value="storageBucket", required=false) String storageBucket,
            @RequestParam(value="storageEndpoint", required=false) String storageEndpoint,
            @RequestParam(value="storageProvider", required=false) String storageProvider,
            @RequestParam(value="storageSecretKey", required=false) String storageSecretKey,
            @RequestParam(value="cellX", required=false) Integer cellX,
            @RequestParam(value="cellY", required=false) Integer cellY,
            @RequestParam(value="cellZ", required=false) Integer cellZ,
            @RequestParam(value="inversionDepth", required=false) Integer inversionDepth,
            @RequestParam(value="mgaZone", required=false) String mgaZone,
            @RequestParam(value="registeredUrl", required=false) String registeredUrl,
            @RequestParam(value="paddingMaxEasting", required=false) Double paddingMaxEasting,
            @RequestParam(value="paddingMaxNorthing", required=false) Double paddingMaxNorthing,
            @RequestParam(value="paddingMinEasting", required=false) Double paddingMinEasting,
            @RequestParam(value="paddingMinNorthing", required=false) Double paddingMinNorthing,
            @RequestParam(value="selectionMaxEasting", required=false) Double selectionMaxEasting,
            @RequestParam(value="selectionMaxNorthing", required=false) Double selectionMaxNorthing,
            @RequestParam(value="selectionMinEasting", required=false) Double selectionMinEasting,
            @RequestParam(value="selectionMinNorthing", required=false) Double selectionMinNorthing,
            @RequestParam(value="vmSubsetFilePath", required=false) String vmSubsetFilePath,
            @RequestParam(value="vmSubsetUrl", required=false) String vmSubsetUrl) throws ParseException {

        //Build our VEGLJob from all of the specified parameters
        VEGLJob job = new VEGLJob(id);
        job.setCellX(cellX);
        job.setCellY(cellY);
        job.setCellZ(cellZ);
        job.setComputeInstanceId(computeInstanceId);
        job.setComputeInstanceKey(computeInstanceKey);
        job.setComputeInstanceType(computeInstanceType);
        job.setComputeVmId(computeVmId);
        job.setDescription(description);
        job.setEmailAddress(emailAddress);
        job.setInversionDepth(inversionDepth);
        job.setMgaZone(mgaZone);
        job.setName(name);
        job.setPaddingMaxEasting(paddingMaxEasting);
        job.setPaddingMaxNorthing(paddingMaxNorthing);
        job.setPaddingMinEasting(paddingMinEasting);
        job.setPaddingMinNorthing(paddingMinNorthing);
        job.setRegisteredUrl(registeredUrl);
        job.setSelectionMaxEasting(selectionMaxEasting);
        job.setSelectionMaxNorthing(selectionMaxNorthing);
        job.setSelectionMinEasting(selectionMinEasting);
        job.setSelectionMinNorthing(selectionMinNorthing);
        job.setSeriesId(seriesId);
        job.setStatus(status);
        job.setStorageAccessKey(storageAccessKey);
        job.setStorageBaseKey(storageBaseKey);
        job.setStorageBucket(storageBucket);
        job.setStorageEndpoint(storageEndpoint);
        job.setStorageProvider(storageProvider);
        job.setStorageSecretKey(storageSecretKey);
        job.setSubmitDate(submitDate);
        job.setUser(user);
        job.setVmSubsetFilePath(vmSubsetFilePath);
        job.setVmSubsetUrl(vmSubsetUrl);

        //Save the VEGL job
        try {
            jobManager.saveJob(job);
        } catch (Exception ex) {
            logger.error("Error updating job " + job, ex);
            return generateJSONResponseMAV(false, null, "Error saving job");
        }

        return generateJSONResponseMAV(true, null, "");
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

        Object[] arguments = new Object[] {
            job.getStorageBucket(), //STORAGE_BUCKET
            job.getStorageBaseKey().replace("//", "/"), //STORAGE_BASE_KEY_PATH
            job.getStorageAccessKey(), //STORAGE_ACCESS_KEY
            job.getStorageSecretKey(), //STORAGE_SECRET_KEY
            hostConfigurer.resolvePlaceholder("vm.sh"), //WORKFLOW_URL
            hostConfigurer.resolvePlaceholder("storage.endpoint"), //STORAGE_ENDPOINT
            "swift" //STORAGE_TYPE
        };

        String result = MessageFormat.format(bootstrapTemplate, arguments);
        return result;
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
                                  @RequestParam("jobId") String jobId) {
        // Get our job
        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId));
            if (job == null) {
                throw new Exception("Job not found.");
            }
        } catch (Exception ex) {
            logger.error("Error fetching job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, "There was a problem retrieving your job from the database.",
                    "Please try again in a few minutes or report it to cg-admin@csiro.au.");
        }

        // copy files to S3 storage for processing
        try {
            // get job files from local directory
            File[] files = fileStagingService.listStageInDirectoryFiles(job);
            if (files.length == 0) {
                job.setStatus(STATUS_FAILED);
                jobManager.saveJob(job);
                return generateJSONResponseMAV(false, null, "There was a problem submitting your job for processing.",
                        "Please upload your input files and try again.");
            }
            //Upload them to storage
            cloudStorageService.uploadJobFiles(job, files);
        } catch (PortalServiceException e) {
            logger.error("Files upload failed.", e);
            job.setStatus(STATUS_FAILED);
            jobManager.saveJob(job);
            return generateJSONResponseMAV(false, null, "There was a problem uploading files to S3 cloud storage.",
                    e.getMessage());
        } catch (Exception e) {
            logger.error("Files upload failed.", e);
            job.setStatus(STATUS_FAILED);
            jobManager.saveJob(job);
            return generateJSONResponseMAV(false, null, "There was a problem uploading files to S3 cloud storage.",
                    "Please report this error to cg_admin@csiro.au");
        }

        //create our input user data string
        String userDataString = null;
        try {
            userDataString = createBootstrapForJob(job);
        } catch (IOException e) {
            logger.error("Job bootstrap creation failed." + e.getMessage(), e);
            job.setStatus(STATUS_FAILED);
            jobManager.saveJob(job);
            return generateJSONResponseMAV(false, null, "There was a problem creating startup script.",
                    "Please report this error to cg_admin@csiro.au");
        }

        // launch the ec2 instance
        String instanceId = null;
        try {
            instanceId = cloudComputeService.executeJob(job, userDataString);
        } catch (PortalServiceException ex) {
            logger.error("Failed to launch instance to run job " + job.getId(), ex);
            job.setStatus(STATUS_FAILED);
            jobManager.saveJob(job);
            return generateJSONResponseMAV(false, null, "There was a problem submitting your job for processing.",
                    ex.getMessage());
        }

        logger.info("Launched instance: " + instanceId);

        // set reference as instanceId for use when killing a job
        job.setComputeInstanceId(instanceId);
        job.setStatus(STATUS_PENDING);
        job.setSubmitDate(new Date());
        jobManager.saveJob(job);

        //Tidy the stage in area (we don't need it any more - all files are replicated in the cloud)
        //Failure here is NOT fatal - it will just result in some residual files
        try {
            if (!fileStagingService.deleteStageInDirectory(job)) {
                logger.error(String.format("There was a problem wiping the stage in directory for job:  %1$s", job));
            }
        } catch (Exception ex) {
            logger.error(String.format("There was a problem wiping the stage in directory for job:  %1$s", job), ex);
        }

        // Save in session for status update request for this job.
        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Creates a new VEGL job initialised with the default configuration values
     * @param email
     * @return
     */
    private VEGLJob createDefaultVEGLJob(HttpSession session) {
        VEGLJob job = new VEGLJob();

        //Start by saving our job to set its ID
        jobManager.saveJob(job);
        log.debug(String.format("Created a new job row id=%1$s", job.getId()));

        //Load details from
        job.setUser((String) session.getAttribute("openID-Email"));
        job.setEmailAddress((String) session.getAttribute("openID-Email"));
        job.setSelectionMinEasting((Double) session.getAttribute(ERRDAPController.SESSION_SELECTION_MIN_EASTING));
        job.setSelectionMaxEasting((Double) session.getAttribute(ERRDAPController.SESSION_SELECTION_MAX_EASTING));
        job.setSelectionMinNorthing((Double) session.getAttribute(ERRDAPController.SESSION_SELECTION_MIN_NORTHING));
        job.setSelectionMaxNorthing((Double) session.getAttribute(ERRDAPController.SESSION_SELECTION_MAX_NORTHING));
        job.setPaddingMinEasting((Double) session.getAttribute(ERRDAPController.SESSION_PADDED_MIN_EASTING));
        job.setPaddingMaxEasting((Double) session.getAttribute(ERRDAPController.SESSION_PADDED_MAX_EASTING));
        job.setPaddingMinNorthing((Double) session.getAttribute(ERRDAPController.SESSION_PADDED_MIN_NORTHING));
        job.setPaddingMaxNorthing((Double) session.getAttribute(ERRDAPController.SESSION_PADDED_MAX_NORTHING));
        job.setMgaZone((String)session.getAttribute(ERRDAPController.SESSION_MGA_ZONE));
        job.setCellX((Integer) session.getAttribute(ERRDAPController.SESSION_CELL_X));
        job.setCellY((Integer) session.getAttribute(ERRDAPController.SESSION_CELL_Y));
        job.setCellZ((Integer) session.getAttribute(ERRDAPController.SESSION_CELL_Z));
        job.setInversionDepth((Integer) session.getAttribute(ERRDAPController.SESSION_INVERSION_DEPTH));

        job.setVmSubsetFilePath(ERRDAP_SUBSET_VM_FILE_PATH);
        job.setComputeInstanceType("m1.large");
        job.setComputeInstanceKey("vgl-developers");
        job.setStorageProvider(hostConfigurer.resolvePlaceholder("storage.provider"));
        job.setStorageEndpoint(hostConfigurer.resolvePlaceholder("storage.endpoint"));
        job.setStorageBucket("vegl-portal");
        job.setName("VEGL-Job");
        job.setDescription("");
        job.setStatus(STATUS_UNSUBMITTED);

        //We need an ID for storing our job file that won't collide with other storage ID's
        job.setStorageBaseKey(cloudStorageService.generateBaseKey(job));

        return job;
    }

    /**
     * Creates a new subset_request.sh script file that will get the subset files for the area selected
     * on the map and save them to the input directory on the Eucalyptus instance. This script will
     * be executed on launch of the instance prior to the vegl processing script.
     *
     * @param request The HTTPServlet request
     */
    private boolean createSubsetScriptFile(VEGLJob job, HashMap<String,String> erddapUrlMap) {
        // create new subset request script file
        File subsetRequestScript = fileStagingService.createStageInDirectoryFile(job, "subset_request.sh");

        // iterate through the map of subset request URL's
        Set<Entry<String, String>> entrySet = erddapUrlMap.entrySet();
        Iterator<Entry<String,String>> i = entrySet.iterator();

        FileWriter out = null;
        try {
            out = new FileWriter(subsetRequestScript);

            while (i.hasNext()) {

                // get the ERDDAP subset request url and layer name
                String url = i.next().getValue();

                // add the command for making the subset request
                out.write(String.format("curl -L '%1$s' > \"%2$s\"\n", url, ERRDAP_SUBSET_VM_FILE_PATH));

                job.setVmSubsetUrl(url);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("Error creating subset request script", e);
            return false;
        } finally {
            FileIOUtil.closeQuietly(out);
        }

        return true;
    }

    /**
     * Gets the set of cloud images available for use by a particular user
     * @param request
     * @return
     */
    @RequestMapping("/getVmImages.do")
    public ModelAndView getImagesForUser(HttpServletRequest request) {
        try {
            VglMachineImage[] images = vglImageService.getAllImages();
            return generateJSONResponseMAV(true, Arrays.asList(images), "");
        } catch (Exception ex) {
            log.error("Unable to access image list:" + ex.getMessage());
            log.debug("Exception:", ex);
            return generateJSONResponseMAV(false);
        }
    }
}
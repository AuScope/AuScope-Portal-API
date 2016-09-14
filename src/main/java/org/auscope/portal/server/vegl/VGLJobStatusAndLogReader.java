package org.auscope.portal.server.vegl;

import java.io.InputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.monitor.JobStatusReader;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.web.controllers.BaseCloudController;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.controllers.JobListController;
import org.springframework.ui.ModelMap;

public class VGLJobStatusAndLogReader extends BaseCloudController implements JobStatusReader {


    public VGLJobStatusAndLogReader(VEGLJobManager jobManager,
            CloudStorageService[] cloudStorageServices,
            CloudComputeService[] cloudComputeServices) {
        super(cloudStorageServices, cloudComputeServices, jobManager);
    }

    /**
     * Gets a pre parsed version of the internal logs. The resulting object will
     * contain the logs sectioned into 'named sections' e.g.: Section for python code,
     * section for environment etc.
     *
     * Will always contain a single section called "Full" containing the un-sectioned
     * original log.
     *
     * @param job
     * @return
     */
    public ModelMap getSectionedLogs(VEGLJob job) throws PortalServiceException {
        return getSectionedLogs(job, JobListController.VL_LOG_FILE);
    }

    /**
     * Gets a pre parsed version of the specified log file. The resulting object will
     * contain the logs sectioned into 'named sections' e.g.: Section for python code,
     * section for environment etc.
     *
     * Will always contain a single section called "Full" containing the un-sectioned
     * original log.
     *
     * @param job
     * @return
     */
    public ModelMap getSectionedLogs(VEGLJob job, String logFile) throws PortalServiceException {
        CloudStorageService cloudStorageService = getStorageService(job);
        if (cloudStorageService == null) {
            throw new PortalServiceException(
                    "The specified job doesn't have a storage service.",
                    "Please ensure you have chosen a storage provider for the job.");
        }

        //Download the logs from cloud storage
        String logContents = null;
        InputStream is = null;
        try {
            is = cloudStorageService.getJobFile(job, logFile);
            logContents = IOUtils.toString(is);
        } catch (Exception ex) {
            log.debug(String.format("The job %1$s hasn't uploaded %2$s yet", job.getId(), logFile));
        } finally {
            FileIOUtil.closeQuietly(is);
        }

        //If we fail at that, download direct from the running instance
        if (logContents == null) {
            CloudComputeService compute = getComputeService(job);
            if (compute == null) {
                throw new PortalServiceException(
                        "The specified job doesn't have a compute service.",
                        "Please ensure you have chosen a compute provider for the job.");
            }

            logContents = compute.getConsoleLog(job);
            if (logContents == null) {
                throw new PortalServiceException("The specified job hasn't uploaded any logs yet");
            }
        }

        ModelMap namedSections = new ModelMap();
        namedSections.put("Full", logContents); //always include the full log

        //Iterate through looking for start/end matches. All text between a start/end
        //tag will be snipped out and used in their own region/section
        Pattern p = Pattern.compile("^#### (.*) (.+) ####$[\\n\\r]*", Pattern.MULTILINE);
        Matcher m = p.matcher(logContents);
        int start = 0;
        String currentSectionName = null;
        while (m.find()) {
            String sectionName = m.group(1);
            String delimiter = m.group(2);

            //On a new match - record the location and name
            if (delimiter.equals("start")) {
                start = m.end();
                currentSectionName = sectionName;
            } else if (delimiter.equals("end")) {
                //On a closing pattern - ensure we are closing the current region (we don't support nesting)
                //Take the snippet of text and store it in our result map
                if (sectionName.equals(currentSectionName)) {
                    String regionText = logContents.substring(start, m.start());
                    namedSections.put(sectionName, regionText);
                    currentSectionName = null;
                    start = 0;
                }
            }
        }

        //We have an unfinished section... let's include it anyway
        if (currentSectionName != null) {
            String regionText = logContents.substring(start);
            namedSections.put(currentSectionName, regionText);
        }

        return namedSections;
    }

    /**
     *
     * @param job
     * @param sectionName
     * @return null if it doesn't have any log
     */
    public String getSectionedLog(VEGLJob job, String sectionName) {
        try {
            ModelMap sectLogs = getSectionedLogs(job);
            return (String)sectLogs.get(sectionName);
        } catch (PortalServiceException ex) {
            log.debug(ex.getMessage());
            return null;
        }
    }

    /**
     * Using the services internal to the class, determine the current status of this job. Service failure
     * will return the underlying job status
     */
    @Override
    public String getJobStatus(CloudJob cloudJob) {

        String stsArn = cloudJob.getProperty(CloudJob.PROPERTY_STS_ARN);
        String clientSecret = cloudJob.getProperty(CloudJob.PROPERTY_CLIENT_SECRET);
        String s3Role = cloudJob.getProperty(CloudJob.PROPERTY_S3_ROLE);

        //The service hangs onto the underlying job Object but the DB is the point of truth
        //Make sure we get an updated job object first!
        VEGLJob job = jobManager.getJobById(cloudJob.getId(), stsArn, clientSecret, s3Role, cloudJob.getEmailAddress());
        if (job == null) {
            return null;
        }

        //If the job is currently in the done/saved IN_QUEUE, ERROR or WALLTIME_EXCEEDED state - do absolutely nothing.
        if (job.getStatus().equals(JobBuilderController.STATUS_DONE) ||
                job.getStatus().equals(JobBuilderController.STATUS_UNSUBMITTED) ||
                        job.getStatus().equals(JobBuilderController.STATUS_INQUEUE) ||
                                job.getStatus().equals(JobBuilderController.STATUS_ERROR)||
                                    job.getStatus().equals(JobBuilderController.STATUS_WALLTIME_EXCEEDED)) {
            return job.getStatus();
        }

        //Get the output files for this job
        CloudStorageService cloudStorageService = getStorageService(job);
        if (cloudStorageService == null) {
            log.warn(String.format("No cloud storage service with id '%1$s' for job '%2$s'. cannot update job status", job.getStorageServiceId(), job.getId()));
            return job.getStatus();
        }
        CloudFileInformation[] results = null;
        try {
            results = cloudStorageService.listJobFiles(job);
        } catch (Exception e) {
            return job.getStatus();
        }

        boolean jobStarted = containsFile(results, "workflow-version.txt");
        boolean jobFinished = containsFile(results, JobListController.VL_TERMINATION_FILE);
        // VM side walltime exceeded
        boolean jobWalltimeExceeded = containsFile(results, "walltime-exceeded.txt");

        String expectedStatus = JobBuilderController.STATUS_PENDING;
        if (jobFinished) {
            expectedStatus = JobBuilderController.STATUS_DONE;
        } else if (jobStarted) {
            expectedStatus = JobBuilderController.STATUS_ACTIVE;
        } else if(jobWalltimeExceeded) {
            expectedStatus = JobBuilderController.STATUS_WALLTIME_EXCEEDED;
        }

        // If the walltime has exceeded and the VM side walltime check has
        // failed to shut the instance down, shut it down
        if(jobStarted && !jobFinished && job.isWalltimeSet()) {
            if(job.getSubmitDate().getTime() + (job.getWalltime()*60*1000) < new Date().getTime()) {
                try {
                    CloudComputeService cloudComputeService = getComputeService(job);
                    cloudComputeService.terminateJob(job);
                    return JobBuilderController.STATUS_WALLTIME_EXCEEDED;
                } catch(Exception e) {
                    log.warn("Exception shutting down terminal: " + job.toString(), e);
                    return JobBuilderController.STATUS_WALLTIME_EXCEEDED;
                }
            }
        }

        //There is also a possibility that the cloud has had issues booting the VM... lets see what we can dig up
        CloudComputeService cloudComputeService = getComputeService(job);
        try {
            switch (cloudComputeService.getJobStatus(job)) {
            case Missing:
                if (jobFinished) {
                    return JobBuilderController.STATUS_DONE;
                } else if (jobWalltimeExceeded) {
                    return JobBuilderController.STATUS_WALLTIME_EXCEEDED;
                } else {
                    return JobBuilderController.STATUS_ERROR;
                }
            case Pending:
            case Running:
                return expectedStatus;
            }
        } catch (Exception ex) {
            log.warn("Exception looking up job VM status:" + job.toString(), ex);
            return job.getStatus();
        }

        return expectedStatus;
    }

    private static boolean containsFile(CloudFileInformation[] files, String fileName) {
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
}
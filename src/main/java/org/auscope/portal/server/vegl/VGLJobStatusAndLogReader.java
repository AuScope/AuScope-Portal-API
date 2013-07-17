package org.auscope.portal.server.vegl;

import java.io.InputStream;
import java.util.HashMap;
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

    private VEGLJobManager jobManager;

    public VGLJobStatusAndLogReader(VEGLJobManager jobManager,
            CloudStorageService[] cloudStorageServices, CloudComputeService[] cloudComputeServices) {
        super(cloudStorageServices, cloudComputeServices);
        this.jobManager = jobManager;
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
    public HashMap getSectionedLogs(VEGLJob job) throws PortalServiceException {
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
            is = cloudStorageService.getJobFile(job, JobListController.VGL_LOG_FILE);
            logContents = IOUtils.toString(is);
        } catch (Exception ex) {
            throw new PortalServiceException("The specified job hasn't uploaded any logs yet.");
        } finally {
            FileIOUtil.closeQuietly(is);
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
            HashMap sectLogs = getSectionedLogs(job);
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
    public String getJobStatus(CloudJob cloudJob) {
        //The service hangs onto the underlying job Object but the DB is the point of truth
        //Make sure we get an updated job object first!
        VEGLJob job = jobManager.getJobById(cloudJob.getId());
        if (job == null) {
            return null;
        }

        //If the job is currently in the done/saved state - do absolutely nothing.
        if (job.getStatus().equals(JobBuilderController.STATUS_DONE) ||
                job.getStatus().equals(JobBuilderController.STATUS_UNSUBMITTED)) {
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
        boolean jobFinished = containsFile(results, JobListController.VGL_LOG_FILE);

        if (jobFinished) {
            return JobBuilderController.STATUS_DONE;
        } else if (jobStarted) {
            return JobBuilderController.STATUS_ACTIVE;
        } else {
            return JobBuilderController.STATUS_PENDING;
        }
    }

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
}
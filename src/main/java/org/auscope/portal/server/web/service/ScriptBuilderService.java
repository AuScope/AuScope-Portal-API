package org.auscope.portal.server.web.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class for providing functionality for saving 'scripts' against a particular job.
 *
 * @author Josh Vote
 *
 */
@Service
public class ScriptBuilderService {
    public static final String SCRIPT_FILE_NAME = "vegl_script.py";

    private final Log logger = LogFactory.getLog(getClass());

    /** For saving our files to a staging area*/
    private FileStagingService jobFileService;
    /** For accessing a job object*/
    private VEGLJobManager jobManager;

    /**
     * Creates a new instance
     * @param jobFileService
     * @param jobManager
     */
    @Autowired
    public ScriptBuilderService(FileStagingService jobFileService,
            VEGLJobManager jobManager) {
        super();
        this.jobFileService = jobFileService;
        this.jobManager = jobManager;
    }

    /**
     * Saves the specified script text as the primary script to be run by the job with specified ID
     * @param jobId
     * @param scriptText
     * @throws PortalServiceException
     */
    public void saveScript(String jobId, String scriptText) throws PortalServiceException {

        //Lookup our job
        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId));
        } catch (Exception ex) {
            logger.warn("Unable to lookup job with id " + jobId + ": " + ex.getMessage());
            logger.debug("exception:", ex);
            throw new PortalServiceException(null, "Unable to lookup job with id " + jobId, ex);
        }

        //Apply text contents to job stage in directory
        OutputStream scriptFile = null;
        try {
            scriptFile = jobFileService.writeFile(job, SCRIPT_FILE_NAME);
            PrintWriter writer = new PrintWriter(scriptFile);
            writer.print(scriptText);
            writer.close();
        } catch (Exception e) {
            logger.error("Couldn't write script file: " + e.getMessage());
            logger.debug("error: ", e);
            throw new PortalServiceException(null, "Couldn't write script file for job with id " + jobId, e);
        } finally {
            FileIOUtil.closeQuietly(scriptFile);
        }
    }

    /**
     * Loads the saved VGL script source with a specified job ID
     * @param jobId
     * @return the file contents if the script file exists otherwise an empty string if the script file doesn't exist or is empty.
     * @throws PortalServiceException
     */
    public String loadScript(String jobId) throws PortalServiceException {
        InputStream is = null;
        try {
            //Lookup our job
            VEGLJob job = jobManager.getJobById(Integer.parseInt(jobId));
            //Load script from VGL server's filesystem
            is = jobFileService.readFile(job, SCRIPT_FILE_NAME);
            String script = null;
            if (is == null) {
                logger.warn("User script file does not exist.");
                script = "";
            } else {
                script = FileIOUtil.convertStreamtoString(is);
            }
            return script;
        } catch (Exception ex) {
            logger.error("Error loading script.", ex);
            throw new PortalServiceException("There was a problem loading your script.", "Please report this error to cg_admin@csiro.au");
        } finally {
            FileIOUtil.closeQuietly(is);
        }
    }

    /**
     * A string format function supporting named placeholders in the form ${key}
     *
     * @param templateText the format string/template string to be used for replacement
     * @param values The key/value pairs to be used in replacing placeholders in templateText
     * @return
     */
    public String populateTemplate(String templateText, Map<String, Object> values) throws PortalServiceException {
        try {
            return StrSubstitutor.replace(templateText, values);
        } catch (IllegalArgumentException ex) {
            logger.warn("Unable to populate template: " + ex.getMessage());
            logger.debug("Exception:", ex);
            throw new PortalServiceException(null, "Unable to populate template", ex);
        }
    }
}

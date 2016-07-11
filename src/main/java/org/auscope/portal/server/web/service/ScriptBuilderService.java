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
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A service class for providing functionality for saving 'scripts' against a particular job.
 *
 * @author Josh Vote
 *
 */
@Service
public class ScriptBuilderService {
    public static final String SCRIPT_FILE_NAME = "vl_script.py";

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
    public ScriptBuilderService(final FileStagingService jobFileService,
            final VEGLJobManager jobManager) {
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
    public void saveScript(final String jobId, final String scriptText, final ANVGLUser user) throws PortalServiceException {

        //Lookup our job
        VEGLJob job = null;
        try {
            job = jobManager.getJobById(Integer.parseInt(jobId), user);
        } catch (final AccessDeniedException e) {
            throw e;
        } catch (final Exception ex) {
            logger.warn("Unable to lookup job with id " + jobId + ": " + ex.getMessage());
            logger.debug("exception:", ex);
            throw new PortalServiceException("Unable to lookup job with id " + jobId, ex);
        }

        //Apply text contents to job stage in directory
        try (OutputStream scriptFile = jobFileService.writeFile(job, SCRIPT_FILE_NAME)) {
            final PrintWriter writer = new PrintWriter(scriptFile);
            writer.print(scriptText);
            writer.close();
        } catch (final Exception e) {
            logger.error("Couldn't write script file: " + e.getMessage());
            logger.debug("error: ", e);
            throw new PortalServiceException("Couldn't write script file for job with id " + jobId, e);
        }
    }

    /**
     * Loads the saved VL script source with a specified job ID
     * @param jobId
     * @return the file contents if the script file exists otherwise an empty string if the script file doesn't exist or is empty.
     * @throws PortalServiceException
     */
    public String loadScript(final String jobId, final ANVGLUser user) throws PortalServiceException {
        //Lookup our job
        final VEGLJob job = jobManager.getJobById(Integer.parseInt(jobId), user);

        try (InputStream is = jobFileService.readFile(job, SCRIPT_FILE_NAME)){
            //Load script from VL server's filesystem

            String script = null;
            if (is == null) {
                logger.warn("User script file does not exist.");
                script = "";
            } else {
                script = FileIOUtil.convertStreamtoString(is);
            }
            return script;
        } catch (final Exception ex) {
            logger.error("Error loading script.", ex);
            throw new PortalServiceException("There was a problem loading your script.", "Please report this error to cg_admin@csiro.au");
        }
    }

    /**
     * A string format function supporting named placeholders in the form ${key}
     *
     * @param templateText the format string/template string to be used for replacement
     * @param values The key/value pairs to be used in replacing placeholders in templateText
     * @return
     */
    public String populateTemplate(final String templateText, final Map<String, Object> values) {
        return StrSubstitutor.replace(templateText, values);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value =  org.springframework.http.HttpStatus.FORBIDDEN)
    public @ResponseBody String handleException(final AccessDeniedException e) {
        return e.getMessage();
    }

}

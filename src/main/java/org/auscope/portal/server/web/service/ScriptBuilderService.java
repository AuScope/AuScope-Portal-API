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

    /**
     * Creates a new instance
     * @param jobFileService
     * @param jobManager
     */
    @Autowired
    public ScriptBuilderService(FileStagingService jobFileService) {
        super();
        this.jobFileService = jobFileService;
    }

    /**
     * Saves the specified script text as the primary script to be run by the job with specified ID
     * @param jobId
     * @param scriptText
     * @throws PortalServiceException
     */
    public void saveScript(VEGLJob job, String scriptText, ANVGLUser user) throws PortalServiceException {
        //Apply text contents to job stage in directory
        try (OutputStream scriptFile = jobFileService.writeFile(job, SCRIPT_FILE_NAME)) {
            PrintWriter writer = new PrintWriter(scriptFile);
            writer.print(scriptText);
            writer.close();
        } catch (Exception e) {
            logger.error("Couldn't write script file: " + e.getMessage());
            logger.debug("error: ", e);
            throw new PortalServiceException("Couldn't write script file for job " + job, e);
        }
    }

    /**
     * Loads the saved VL script source with a specified job ID
     * @param jobId
     * @return the file contents if the script file exists otherwise an empty string if the script file doesn't exist or is empty.
     * @throws PortalServiceException
     */
    public String loadScript(VEGLJob job, ANVGLUser user) throws PortalServiceException {
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
        } catch (Exception ex) {
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
    public String populateTemplate(String templateText, Map<String, Object> values) {
        return StrSubstitutor.replace(templateText, values);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value =  org.springframework.http.HttpStatus.FORBIDDEN)
    public @ResponseBody String handleException(AccessDeniedException e) {
        return e.getMessage();
    }

}

/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.web.service.ScriptBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the ScriptBuilder view.
 *
 * @author Cihan Altinay
 * @author Josh Vote - modified for usage with VEGL
 * @author Richard Goh
 */
@Controller
public class ScriptBuilderController extends BasePortalController {

    private final Log logger = LogFactory.getLog(getClass());

    /** Handles saving scripts against a job*/
    private ScriptBuilderService sbService;

    /**
     * Creates a new instance
     *
     * @param jobFileService
     * @param jobManager
     */
    @Autowired
    public ScriptBuilderController(ScriptBuilderService sbService) {
        super();
        this.sbService = sbService;
    }

    /**
     * Writes provided script text to a file in the specified jobs stage in directory
     *
     * @return A JSON encoded response with a success flag
     */
    @RequestMapping("/secure/saveScript.do")
    public ModelAndView saveScript(@RequestParam("jobId") String jobId,
                                  @RequestParam("sourceText") String sourceText) {

        if (sourceText == null || sourceText.isEmpty()) {
            return generateJSONResponseMAV(false, null, "No source text specified");
        }

        try {
            sbService.saveScript(jobId, sourceText);
        } catch (PortalServiceException ex) {
            logger.warn("Unable to save job script for job with id " + jobId + ": " + ex.getMessage());
            logger.debug("error:", ex);
            return generateJSONResponseMAV(false, null, "Unable to write script file");
        }

        return generateJSONResponseMAV(true, null, "");
    }

    /**
     * Gets the contents of a saved job's script file.
     * @param jobId
     * @return A JSON encoded response which contains the contents of a saved job's script file
     */
    @RequestMapping("/getSavedScript.do")
    public ModelAndView getSavedScript(@RequestParam("jobId") String jobId) {
        logger.debug("getSavedScript with jobId: " + jobId);
        String script = null;

        try {
            script = sbService.loadScript(jobId);
        } catch (PortalServiceException ex) {
            logger.error("Unable to load saved script for job with id " + jobId, ex);
            return generateJSONResponseMAV(false, null, ex.getMessage(), ex.getErrorCorrection());
        }

        return generateJSONResponseMAV(true, script, "");
    }

    /**
     * Gets a named script template and fills in all named placeholders with the matching key/value pairs
     * @param templateName Script name
     * @param keys Keys to lookup (corresponds 1-1 with values)
     * @param values Values to use in template for placeholders (corresponds 1-1 with keys)
     * @return
     */
    @RequestMapping("/getTemplatedScript.do")
    public ModelAndView getTemplatedScript(@RequestParam("templateName") String templateName,
                                  @RequestParam(value="key", required=false) String[] keys,
                                  @RequestParam(value="value", required=false) String[] values) {
        //Turn our KVP inputs into something that we can pass to our service
        Map<String, Object> kvpMapping = new HashMap<String, Object>();
        if (keys != null && values != null) {
            for (int i = 0; i < keys.length && i < values.length; i++) {
                kvpMapping.put(keys[i], values[i]);
            }
        }

        //Load our template file into a string
        String templateResource = "/org/auscope/portal/server/scriptbuilder/templates/" + templateName.replaceAll("\\.\\.", "").replaceAll("/","");
        String templateString = null;
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream(templateResource);
            if (is == null) {
                logger.error("Unable to find template resource - " + templateResource);
                return generateJSONResponseMAV(false, null, "Requested template does not exist");
            }
            templateString = FileIOUtil.convertStreamtoString(is);
        } catch (IOException e) {
            logger.error("Unable to read template resource - " + templateResource + ":" + e.getMessage());
            logger.debug("Exception:", e);
            return generateJSONResponseMAV(false, null, "Internal server error when loading template.");
        } finally {
            FileIOUtil.closeQuietly(is);
        }

        String finalTemplate = sbService.populateTemplate(templateString,
                kvpMapping);
        return generateJSONResponseMAV(true, finalTemplate, "");
    }
}
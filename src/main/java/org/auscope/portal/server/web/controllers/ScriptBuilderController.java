/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ScmEntryService;
import org.auscope.portal.server.web.service.ScriptBuilderService;
import org.auscope.portal.server.web.service.scm.Problem;
import org.auscope.portal.server.web.service.scm.Solution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the ScriptBuilder view.
 *
 * @author Cihan Altinay
 * @author Josh Vote - modified for usage with VEGL
 * @author Richard Goh
 */
@Controller
public class ScriptBuilderController extends BaseModelController {

    private final Log logger = LogFactory.getLog(getClass());

    /** Handles saving scripts against a job*/
    private ScriptBuilderService sbService;

    /** Handles SCM entries. */
    private ScmEntryService scmEntryService;

    /**
     * Creates a new instance
     *
     * @param jobFileService
     * @param jobManager
     */
    @Autowired
    public ScriptBuilderController(ScriptBuilderService sbService,
                                   VEGLJobManager jobManager,
                                   ScmEntryService scmEntryService) {
        super(jobManager);
        this.sbService = sbService;
        this.scmEntryService = scmEntryService;
    }

    /**
     * Writes provided script text to a file in the specified jobs stage in directory
     *
     * @param jobId
     * @param sourceText
     * @param solution
     * @return A JSON encoded response with a success flag
     */
    @RequestMapping("/secure/saveScript.do")
    public ModelAndView saveScript(@RequestParam("jobId") String jobId,
                                   @RequestParam("sourceText") String sourceText,
                                   @RequestParam("solutions") Set<String> solutions,
                                   @AuthenticationPrincipal ANVGLUser user) {

        if (sourceText == null || sourceText.trim().isEmpty()) {
            return generateJSONResponseMAV(false, null, "No source text specified");
        }

        VEGLJob job = attemptGetJob(Integer.parseInt(jobId), user);
        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        try {
            sbService.saveScript(job, sourceText, user);
        } catch (PortalServiceException ex) {
            logger.warn("Unable to save job script for job with id " + jobId + ": " + ex.getMessage());
            logger.debug("error:", ex);
            return generateJSONResponseMAV(false, null, "Unable to write script file");
        }

        // Update job with vmId for solution if we have one.
        try {
            scmEntryService.updateJobForSolution(job, solutions, user);
        }
        catch (PortalServiceException e) {
            logger.warn("Failed to update job (" + jobId + ") for solutions (" +
                        solutions + "): " + e.getMessage());
            logger.debug("error: ", e);
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
    public ModelAndView getSavedScript(@RequestParam("jobId") String jobId, @AuthenticationPrincipal ANVGLUser user) {
        logger.debug("getSavedScript with jobId: " + jobId);
        String script = null;

        VEGLJob job = attemptGetJob(Integer.parseInt(jobId), user);
        if (job == null) {
            return generateJSONResponseMAV(false);
        }

        try {
            script = sbService.loadScript(job, user);
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
        Map<String, Object> kvpMapping = new HashMap<>();
        if (keys != null && values != null) {
            for (int i = 0; i < keys.length && i < values.length; i++) {
                kvpMapping.put(keys[i], values[i]);
            }
        }

        //Load our template file into a string
        String templateResource = "/org/auscope/portal/server/scriptbuilder/templates/" + templateName.replaceAll("\\.\\.", "").replaceAll("/","");
        String templateString = null;

        try (InputStream is = this.getClass().getResourceAsStream(templateResource)) {
            if (is == null) {
                logger.error("Unable to find template resource - " + templateResource);
                return generateJSONResponseMAV(false, null, "Requested template does not exist");
            }
            templateString = FileIOUtil.convertStreamtoString(is);
        } catch (IOException e) {
            logger.error("Unable to read template resource - " + templateResource + ":" + e.getMessage());
            logger.debug("Exception:", e);
            return generateJSONResponseMAV(false, null, "Internal server error when loading template.");
        } 
        
        String finalTemplate = sbService.populateTemplate(templateString,
                kvpMapping);
        return generateJSONResponseMAV(true, finalTemplate, "");
    }

    /**
     * Return a JSON list of problems and their solutions.
     */
    @RequestMapping("/getProblems.do")
    public ModelAndView getProblems() {
        // Get the Solutions from the SSC
        List<Solution> solutions = scmEntryService.getSolutions();

        // Group solutions by the problem that they solve.
        HashMap<String, Problem> problems = new HashMap<>();

        for (Solution solution: solutions) {
            String problemId = solution.getProblem().getId();
            Problem problem = problems.get(problemId);

            if (problem == null) {
                problem = solution.getProblem();
                problem.setSolutions(new ArrayList<Solution>());
                problems.put(problem.getId(), problem);
            }
            problem.getSolutions().add(solution);
        }

        // Return the result
        return generateJSONResponseMAV(true, problems.values(), "");
    }

    /**
     * Return the details for a solution.
     *
     * @param solutionId String solution id
     *
     */
    @RequestMapping("/getSolution.do")
    public ModelAndView getSolution(String solutionId) {
        Solution solution = scmEntryService.getScmSolution(solutionId);

        // Wrap the data in an array or list until the JSON response
        // code is fixed.
        return generateJSONResponseMAV(true, new Solution[] {solution}, "");
    }

    /**
     * Return a list of solution objects for the corresponding uris.
     *
     * @param uris Collection<String> of uris to look up
     * @return List<Solution> solution objects
     */
    @RequestMapping("/getSolutions.do")
    public ModelAndView doGetSolutions(@RequestParam("uris") Set<String> uris) {
        ArrayList<Solution> solutions = new ArrayList<>();
        StringBuilder msg = new StringBuilder();

        for (String uri: uris) {
            Solution solution = scmEntryService.getScmSolution(uri);
            if (solution != null) {
                solutions.add(solution);
            }
            else {
                msg.append(String.format("No solution found (%s)", uri))
                    .append("; \n");
            }
        }

        return generateJSONResponseMAV(true, solutions, msg.toString());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value =  org.springframework.http.HttpStatus.FORBIDDEN)
    public @ResponseBody String handleException(AccessDeniedException e) {
        return e.getMessage();
    }

}

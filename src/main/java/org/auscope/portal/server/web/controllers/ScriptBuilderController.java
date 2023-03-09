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
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.csw.SearchFacet;
import org.auscope.portal.core.services.csw.SearchFacet.Comparison;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.service.PortalUserService;
import org.auscope.portal.server.web.service.LintResult;
import org.auscope.portal.server.web.service.NCIDetailsService;
import org.auscope.portal.server.web.service.ScmEntryService;
import org.auscope.portal.server.web.service.ScriptBuilderService;
import org.auscope.portal.server.web.service.SolutionResponse;
import org.auscope.portal.server.web.service.TemplateLintService;
import org.auscope.portal.server.web.service.TemplateLintService.TemplateLanguage;
import org.auscope.portal.server.web.service.scm.Problem;
import org.auscope.portal.server.web.service.scm.Solution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
public class ScriptBuilderController extends BaseCloudController {

    private final Log logger = LogFactory.getLog(getClass());

    /** Handles saving scripts against a job*/
    private ScriptBuilderService sbService;

    /** Handles SCM entries. */
    private ScmEntryService scmEntryService;

    /** Script checking */
    private TemplateLintService templateLintService;
    
    private PortalUserService userService;

    @Autowired
    private NCIDetailsService nciDetailsService;

    /**
     * Creates a new instance
     *
     * @param jobFileService
     * @param jobManager
     */
    @Autowired
    public ScriptBuilderController(ScriptBuilderService sbService,
    							   PortalUserService userService,
                                   VEGLJobManager jobManager,
                                   ScmEntryService scmEntryService,
                                   TemplateLintService templateLintService,
                                   CloudStorageService[] cloudStorageServices,
                                   CloudComputeService[] cloudComputeServices,
                                   @Value("${cloud.vm.sh}") String vmSh,
                                   @Value("${cloud.vm-shutdown.sh}") String vmShutdownSh) {
        super(cloudStorageServices, cloudComputeServices, jobManager,vmSh,vmShutdownSh);
        this.sbService = sbService;
        this.userService= userService;
        this.scmEntryService = scmEntryService;
        this.templateLintService = templateLintService;
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
                                   @RequestParam("solutions") Set<String> solutions) {
    	PortalUser user = userService.getLoggedInUser();
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
    public ModelAndView getSavedScript(@RequestParam("jobId") String jobId) {
        logger.debug("getSavedScript with jobId: " + jobId);
        PortalUser user = userService.getLoggedInUser();
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
     * Gets a JSON list of id/name pairs for every available compute service
     * that has been properly configured to run with the logged in user
     *
     * @return
     * @throws PortalServiceException
     */
    @RequestMapping("/secure/getConfiguredComputeServices.do")
    public ModelAndView getComputeServices() throws PortalServiceException {
    	PortalUser user = userService.getLoggedInUser();
        List<CloudComputeService> configuredServices = getConfiguredComputeServices(user, nciDetailsService);
        List<ModelMap> parsedItems = new ArrayList<ModelMap>();
        for (CloudComputeService ccs : configuredServices) {
            ModelMap mm = new ModelMap();
            mm.put("providerId", ccs.getId());
            mm.put("name", ccs.getName());
            parsedItems.add(mm);
        }
        ModelMap mm = new ModelMap();
        mm.put("providerId", "");
        mm.put("name", "All Providers");
        parsedItems.add(mm);

        return generateJSONResponseMAV(true, parsedItems, "");
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
     * @throws PortalServiceException
     */
    @RequestMapping("/secure/getProblems.do")
    public ModelAndView getProblems(
            @RequestParam(value="field", required=false) String[] rawFields,
            @RequestParam(value="value", required=false) String[] rawValues,
            @RequestParam(value="type", required=false) String[] rawTypes,
            @RequestParam(value="comparison", required=false) String[] rawComparisons) throws PortalServiceException {
    	PortalUser user = userService.getLoggedInUser();
        if (rawFields == null) {
            rawFields = new String[0];
        }

        if (rawValues == null) {
            rawValues = new String[0];
        }

        if (rawTypes == null) {
            rawTypes = new String[0];
        }

        if (rawComparisons == null) {
            rawComparisons = new String[0];
        }

        if (rawFields.length != rawValues.length || rawFields.length != rawTypes.length || rawFields.length != rawComparisons.length) {
            throw new IllegalArgumentException("field/value/type/comparison lengths mismatch");
        }

        //Parse our raw request info into a list of search facets
        List<SearchFacet<? extends Object>> facets = new ArrayList<SearchFacet<? extends Object>>();
        for (int i = 0; i < rawFields.length; i++) {
            Comparison cmp = null;
            switch(rawComparisons[i]) {
            case "gt":
                cmp = Comparison.GreaterThan;
                break;
            case "lt":
                cmp = Comparison.LessThan;
                break;
            case "eq":
                cmp = Comparison.Equal;
                break;
            default:
                throw new IllegalArgumentException("Unknown comparison type: " + rawComparisons[i]);
            }

            SearchFacet<? extends Object> newFacet = null;
            switch(rawTypes[i]) {
            case "string":
                newFacet = new SearchFacet<String>(rawValues[i], rawFields[i], cmp);
                break;
            }

            facets.add(newFacet);
        }


        // Get the Solutions from the SSC
        List<CloudComputeService> configuredServices = getConfiguredComputeServices(user, nciDetailsService);
        SolutionResponse solutions = scmEntryService.getSolutions(facets, configuredServices.toArray(new CloudComputeService[configuredServices.size()]));

        // Group solutions by the problem that they solve.
        HashMap<String, Problem> configuredProblems = new HashMap<>();
        HashMap<String, Problem> unconfiguredProblems = new HashMap<>();

        for (Solution solution: solutions.getConfiguredSolutions()) {
            String problemId = solution.getProblem().getId();
            Problem problem = configuredProblems.get(problemId);

            if (problem == null) {
                problem = solution.getProblem();
                problem.setSolutions(new ArrayList<Solution>());
                configuredProblems.put(problem.getId(), problem);
            }
            problem.getSolutions().add(solution);
        }

        for (Solution solution: solutions.getUnconfiguredSolutions()) {
            String problemId = solution.getProblem().getId();
            Problem problem = unconfiguredProblems.get(problemId);

            if (problem == null) {
                problem = solution.getProblem();
                problem.setSolutions(new ArrayList<Solution>());
                unconfiguredProblems.put(problem.getId(), problem);
            }
            problem.getSolutions().add(solution);
        }

        ModelMap result = new ModelMap();
        result.put("configuredProblems", configuredProblems.values());
        result.put("unconfiguredProblems", unconfiguredProblems.values());

        // Return the result
        return generateJSONResponseMAV(true, result, "");
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

    /**
     * Return a list of errors/warnings about a template.
     *
     * Passes the template string to pylint and turns the results into a list of
     * error objects. Each entry contains a severity (error, warning etc),
     * message string and the location in the code (from, to).
     *
     * The template language must be one of the following values:
     * - python3
     * - python2
     *
     * @param template String with template code
     * @param lang String identifying template language (default=python3)
     * @return List<LintResult> lint result objects
     */
    @RequestMapping("/lintTemplate.do")
    public ModelAndView doLintTemplate(@RequestParam("template") String template,
                                       @RequestParam(value="lang",
                                                     required=false) String lang) {
        TemplateLanguage templateLanguage = null;
        String msg = "No errors or warnings found.";
        List<LintResult> lints = null;

        // Make sure it's a supported language
        if (lang == null) {
            templateLanguage = TemplateLanguage.PYTHON3;
        }
        else {
            try {
                templateLanguage = TemplateLanguage.valueOf(lang.toUpperCase());
            }
            catch (IllegalArgumentException ex) {
                logger.error("Invalid template language for template linting", ex);
                return generateJSONResponseMAV(false, null, "Unable to check unsupported template language.");
            }
        }

        // Get the linter to do its thing
        try {
            lints = templateLintService.checkTemplate(template, templateLanguage);
            if (lints != null && lints.size() > 0) {
                msg = String.format("Found {} issues", lints.size());
            }
        }
        catch (PortalServiceException ex) {
            logger.warn("Template code check failed: " + ex.getMessage(), ex);
            return generateJSONResponseMAV(false, null, "Template code check failed: " + ex.getMessage());
        }

        return generateJSONResponseMAV(true, lints, msg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value =  org.springframework.http.HttpStatus.FORBIDDEN)
    public @ResponseBody String handleException(AccessDeniedException e) {
        return e.getMessage();
    }

}

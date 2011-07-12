/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.service.JobFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for the ScriptBuilder view.
 *
 * @author Cihan Altinay
 * @author Josh Vote - modified for usage with VEGL
 */
@Controller
public class ScriptBuilderController extends BaseVEGLController {

	public static final String SCRIPT_FILE_NAME = "vegl_script.py";  
	
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private JobFileService jobFileService;
    
    @Autowired
    private VEGLJobManager jobManager;
    
    /**
     * Writes provided script text to a file in the specified jobs stage in directory
     *
     * @return A JSON encoded response with a success flag
     */
    @RequestMapping("/saveScript.do")
    public ModelAndView saveScript(@RequestParam("jobId") String jobId,
                                  @RequestParam("sourceText") String sourceText) {

        
    	if (sourceText == null || sourceText.isEmpty()) {
    		return generateJSONResponseMAV(false, null, "No source text specified");
    	}
    	
    	//Lookup our job
    	VEGLJob job = null;
    	try {
    		job = jobManager.getJobById(Integer.parseInt(jobId));
    	} catch (Exception ex) {
    		logger.warn("Unable to lookup job with id " + jobId, ex);
    		return generateJSONResponseMAV(false, null, "Unable to lookup jobId");
    	}
    	
    	//Apply text contents to job stage in directory
        try {
        	File scriptFile = jobFileService.createStageInDirectoryFile(job, SCRIPT_FILE_NAME);
            PrintWriter writer = new PrintWriter(scriptFile);
            writer.print(sourceText);
            writer.close();
        } catch (IOException e) {
            logger.error("Could write script file", e);
            return generateJSONResponseMAV(false, null, "Unable to write script file");
        }

        return generateJSONResponseMAV(true, null, "");
    }
}


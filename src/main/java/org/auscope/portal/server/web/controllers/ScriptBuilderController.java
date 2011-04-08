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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for the ScriptBuilder view.
 *
 * @author Cihan Altinay
 */
@Controller
public class ScriptBuilderController {//extends MultiActionController {

    private final Log logger = LogFactory.getLog(getClass());

    protected ModelAndView handleNoSuchRequestHandlingMethod(
            NoSuchRequestHandlingMethodException ex,
            HttpServletRequest request,
            HttpServletResponse response) {

        logger.debug("No/invalid action parameter; returning scriptbuilder view.");
        return new ModelAndView("scriptbuilder");
    }

    /**
     * Processes a script download request.
     *
     * @param request The servlet request including a sourcetext parameter
     * @param response The servlet response receiving the file
     *
     * @return null if successful, the scriptbuilder view otherwise.
     */
    @RequestMapping("/downloadScript.do")
    public ModelAndView downloadScript(HttpServletRequest request,
                                       HttpServletResponse response) {

        logger.debug("User requested script download");
        String script = request.getParameter("sourcetext");
        if (script != null) {
            String scriptName = request.getParameter("scriptname");
            if (scriptName == null) {
                scriptName = "particle_script";
            }
            response.setContentType("application/octet-stream");
            response.setContentLength(script.length());
            response.setHeader("Content-Disposition",
                    "attachment; filename=\""+scriptName+".py\"");

            try {
                PrintWriter writer = response.getWriter();
                writer.print(script);
                writer.close();
                return null;
            } catch (IOException e) {
                logger.error("Could not open output stream!");
            }
        }
        logger.debug("No source text provided. Returning scriptbuilder view.");
        return new ModelAndView("scriptbuilder");
    }

    /**
     * Writes provided script text to a file and redirects to the grid
     * submission interface.
     *
     * @param request The servlet request including a sourcetext parameter
     * @param response The servlet response
     *
     * @return The gridsubmit view if successful, the scriptbuilder view
     *         otherwise.
     */
    @RequestMapping("/useScript.do")
    public ModelAndView useScript(HttpServletRequest request,
                                  HttpServletResponse response) {

        logger.debug("User requested script use");
        String script = request.getParameter("sourcetext");
        if (script != null) {
            String scriptName = request.getParameter("scriptname");
            if (scriptName == null) {
                scriptName = "particle_script";
            }

            try {
                String tempDir = System.getProperty("java.io.tmpdir");
                File scriptFile = new File(
                        tempDir+File.separator+scriptName+".py");
                scriptFile.deleteOnExit();
                PrintWriter writer = new PrintWriter(scriptFile);
                writer.print(script);
                writer.close();
                logger.info("saved script file: " + scriptFile.getAbsolutePath());

            } catch (IOException e) {
                logger.error("Could not create temp file: " + e.getMessage());
            }

            request.getSession().setAttribute("scriptFile", scriptName);
            return new ModelAndView(new RedirectView("gridsubmit.html"));
        }
        logger.debug("No source text provided. Returning scriptbuilder view.");
        return new ModelAndView("scriptbuilder");
    }

    /**
     * Returns the contents of a script file to be edited.
     *
     * @param request The servlet request
     * @param response The servlet response
     *
     * @return A JSON object containing a scriptName attribute and a
     *         scriptText attribute.
     */
    @RequestMapping("/getScriptText.do")
    public ModelAndView getScriptText(HttpServletRequest request,
                                      HttpServletResponse response) {

        ModelAndView mav = new ModelAndView("jsonView");
        String scriptFile = (String) request.getSession()
            .getAttribute("scriptFile");

        String scriptName = null;
        String scriptText = null;

        if (scriptFile != null) {
            logger.debug("Reading script source.");
            String tempDir = System.getProperty("java.io.tmpdir");
            try {
                BufferedReader input = new BufferedReader(
                    new FileReader(tempDir+File.separator+scriptFile));
                StringBuffer contents = new StringBuffer();
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line).append(
                            System.getProperty("line.separator"));
                }
                input.close();
                if (scriptFile.lastIndexOf(".py") > 0) {
                    scriptName = scriptFile.substring(0,
                            scriptFile.lastIndexOf(".py"));
                } else {
                    scriptName = scriptFile;
                }
                scriptText = contents.toString();

            } catch (IOException e) {
                logger.error("Error reading file.");
            }
            request.getSession().removeAttribute("scriptFile");
        }

        if (scriptText != null) {
            mav.addObject("scriptName", scriptName);
            mav.addObject("scriptText", scriptText);
        }
        return mav;
    }
}


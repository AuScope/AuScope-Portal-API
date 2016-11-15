/*

import org.springframework.stereotype.Service;

 * This file is part of the Virtual Geophysics Laboratory (VGL) project.
 * Copyright (c) 2016, CSIRO
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aopalliance.reflect.Code;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.v2.codegen.bean.CompleteConstructorGeneratorExtension;

/**
 * Checks a template script for errors or other issues.
 *
 * @author Geoff Squire
 */
@Service
public class TemplateLintService {
    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Enumerate template languages we support.
     */
    public enum TemplateLanguage { PYTHON3, PYTHON2 }

    /**
     * Default time to wait for lint process to complete in seconds.
     *
     * TODO: Make this configurable.
     */
    public static final long DEFAULT_TIMEOUT = 5l;

    /**
     * Create a new instance.
     */
    @Autowired
    public TemplateLintService() {
        super();
    }

    /**
     * Check a template script code and return any errors or other issues found.
     *
     * @param template String containing template code
     * @param language TemplateLanguage language used for template
     * @return List<LintResult> containing errors/issues found, if any
     */
    public List<LintResult> checkTemplate(String template, TemplateLanguage language)
        throws PortalServiceException
    {
        List<LintResult> lints = null;

        switch (language) {
        case PYTHON3:
            // fall through
            // TODO: support py2/3 environments
        case PYTHON2:
            lints = pylint(template);
            break;
        default:
            throw new PortalServiceException(String.format("Unsupported template language ({})",
                                                           language));
        }

        return lints;
    }

    private List<LintResult> pylint(String template)
        throws PortalServiceException
    {
        List<LintResult> lints = new ArrayList<LintResult>();

        // Save template as a temporary python file
        Path f;
        try {
            f = Files.createTempFile("contemplate", ".py");
            BufferedWriter writer = Files.newBufferedWriter(f);
            writer.write(template);
            writer.close();
        }
        catch (Exception ex) {
            throw new PortalServiceException("Failed to write template to temporary file.", ex);
        }

        // Run pylint in the temp file's directory
        BufferedInputStream stdout;
        try {
            ProcessBuilder pb =
            new ProcessBuilder("pylint", "-r", "n", f.getFileName().toString())
            .directory(f.getParent().toFile());

            Process p = pb.start();
            stdout = new BufferedInputStream(p.getInputStream());
            BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            if (!p.waitFor(DEFAULT_TIMEOUT, TimeUnit.SECONDS)) {
                // Timed out
                throw new PortalServiceException(String.format("pylint process failed to complete before {} second timeout elapsed", DEFAULT_TIMEOUT));
            }

            // Finished successfully?
            int rv = p.exitValue();
            if (rv != 0) {
                logger.debug("pylint stderr:");
                String line = stderr.readLine();
                while (line != null) {
                    logger.debug(line);
                    line = stderr.readLine();
                }
                throw new PortalServiceException(String.format("pylint process returned non-zero exit value: {}", rv));
            }
        }
        catch (PortalServiceException pse) {
            throw pse;
        }
        catch (Exception ex) {
            throw new PortalServiceException("Failed to run pylint on template", ex);
        }

        // Parse results into LintResult objects
        lints = parsePylintResults(stdout);

        // Clean up
        try {
            Files.delete(f);
        }
        catch (Exception ex) {
            throw new PortalServiceException("Failed to delete temporary template file.", ex);
        }

        return lints;
    }

    /**
     * Parse pylint results into LintResult objects.
     *
     * @param input InputStream with results text
     * @return List<LintResult> with issues
     */
    private List<LintResult> parsePylintResults(InputStream input)
        throws PortalServiceException
    {
        List<LintResult> lints = new ArrayList<LintResult>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(input);
        }
        catch (Exception ex) {
            throw new PortalServiceException("Failed to parse pylint result json", ex);
        }
        if (root == null) {
            throw new PortalServiceException("No JSON content found in pylint results");
        }
        else if (!root.isArray()) {
            throw new PortalServiceException
                (String.format("Unsupported pylint results: {}",
                               root.toString()));
        }

        // Parsed json, so extract LintResult objects
        for (JsonNode result: root) {
            LintResult.Severity severity =
                result.get("type").asText().equals("error")
                ? LintResult.Severity.ERROR
                : LintResult.Severity.WARNING;
            lints.add(new LintResult(severity,
                                     result.get("message").asText(),
                                     new int[] {
                                         result.get("line").asInt(),
                                         result.get("column").asInt()
                                     }));
        }

        return lints;
    }
}

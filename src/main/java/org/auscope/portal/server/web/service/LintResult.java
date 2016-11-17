/*
 * This file is part of the Virtual Geophysics Laboratory (VGL) project.
 * Copyright (c) 2016, CSIRO
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.service;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single result from the TemplateLintService
 *
 * Contains a short description of the issue, severity (error or warning) and
 * the location in the code.
 *
 * Locations are captured as Location instances with 0-based line and column
 * numbers. A result will always have a "from" location, but the "to" location
 * is optional, only used if the issue covers a range in the document.
 *
 * @author Geoff Squire
 *
 */
public class LintResult {

    /**
     * Enumerates possible severity of issues.
     */
    public enum Severity {
        @JsonProperty("error") ERROR,
        @JsonProperty("warning") WARNING
    }

    public static class Location {
        int line;
        int column;

        public Location(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public int getLine() {
            return this.line;
        }

        public int getColumn() {
            return this.column;
        }
    }

    /**
     * Description of the issue.
     */
    public String message;

    /**
     * Severity of the issue.
     */
    public Severity severity;

    /**
     * Location in the document where the issue begins.
     *
     */
    public Location from;

    /**
     * (optional) Location in the document where the issue ends.
     */
    public Location to;

    /**
     * Create a new, empty instance.
     */
    public LintResult() {
        this.message = null;
        this.severity = null;
        this.from = null;
        this.to = null;
    }

    /**
     * Create and initialise a new instance.
     *
     * @param severity Severity of the issue
     * @param message String description of the issue
     * @param from Location start of the issue in the source
     * @param to Location end of the issue in the source
     */
    public LintResult(Severity severity, String message, Location from, Location to) {
        this.severity = severity;
        this.message = message;
        this.from = from;
        this.to = to;
    }

    /**
     * Create and initialise a new instance with no "to" Location specified.
     *
     * @param severity Severity of the issue
     * @param message String description of the issue
     * @param from Location start of the issue in the source
     */
    public LintResult(Severity severity, String message, Location from) {
        this(severity, message, from, null);
    }
}


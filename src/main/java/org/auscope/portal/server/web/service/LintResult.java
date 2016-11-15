/*
 * This file is part of the Virtual Geophysics Laboratory (VGL) project.
 * Copyright (c) 2016, CSIRO
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.service;

import java.util.Arrays;

/**
 * A single result from the TemplateLintService
 *
 * Contains a short description of the issue, severity (error or warning) and
 * the location in the code.
 *
 * Locations are captured as 2-element arrays, consisting of [line, column]
 * (counting starts from 1 in each case). A result will always have a "from"
 * location, but the "to" location is optional, only used if the issue covers a
 * range in the document.
 *
 * @author Geoff Squire
 *
 */
public class LintResult {

    /**
     * Enumerates possible severity of issues.
     */
    public static enum Severity {ERROR, WARNING}

    public static final int LINE = 0;
    public static final int COL = 1;

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
    public int[] from;

    /**
     * (optional) Location in the document where the issue ends.
     */
    public int[] to;

    /**
     * Create a new, empty instance.
     */
    public LintResult() {}

    /**
     * Create and initialise a new instance.
     *
     * @param severity Severity of the issue
     * @param message String description of the issue
     * @param from int[] start of the issue in the source [line, column]
     * @param to int[] end of the issue in the source [line, column]
     */
    public LintResult(Severity severity, String message, int[] from, int[] to) {
        this.severity = severity;
        this.message = message;
        this.from = Arrays.copyOf(from, 2);

        if (to != null) {
            this.to = Arrays.copyOf(to, 2);
        }
    }

    /**
     * Create and initialise a new instance.
     *
     * @param severity String severity of the issue
     * @param message String description of the issue
     * @param from int[] start of the issue in the source [line, column]
     * @param to int[] end of the issue in the source [line, column]
     */
    public LintResult(String severity, String message, int[] from, int[] to) {
        this(Severity.valueOf(severity.toUpperCase()), message, from, to);
    }

    /**
     * Create and initialise a new instance with no to location specified.
     *
     * @param severity Severity of the issue
     * @param message String description of the issue
     * @param from int[] start of the issue in the source [line, column]
     */
    public LintResult(Severity severity, String message, int[] from) {
        this(severity, message, from, null);
    }

    /**
     * Create and initialise a new instance with no to location specified.
     *
     * @param severity String severity of the issue
     * @param message String description of the issue
     * @param from int[] start of the issue in the source [line, column]
     */
    public LintResult(String severity, String message, int[] from) {
        this(Severity.valueOf(severity.toUpperCase()), message, from);
    }
}


/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.server.gridjob.GridAccessController;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller that handles MyProxy logins.
 *
 * @author Cihan Altinay
 */
public class MyProxyLoginController implements Controller {

    protected final Log logger = LogFactory.getLog(getClass());
    private static final int PROXY_LIFETIME = 10*24*60*60; // 10 days
    private GridAccessController gridAccess;

    /**
     * Sets the <code>GridAccessController</code> to be used for proxy checking
     * and initialisation.
     *
     * @param gridAccess the GridAccessController to use
     */
    public void setGridAccess(GridAccessController gridAccess) {
        this.gridAccess = gridAccess;
    }

    /**
     * Main entry point which extracts username and password from the
     * request and uses this information to initialize a grid proxy.
     * If the proxy could not be initialized an error message is included in
     * the response.
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) {

        String user;
        String pass;
        String error = null;

        user = request.getParameter("username");

        if (user != null) {
            pass = request.getParameter("password");
            if (pass == null) {
                pass = "";
            }

            logger.info("Trying to initialize proxy with MyProxy details");
            Object credential = gridAccess.initProxy(user, pass, PROXY_LIFETIME);
            if (credential != null) {
                logger.info("Storing credentials in session.");
                request.getSession().setAttribute("userCred", credential);
                return new ModelAndView(
                        new RedirectView("joblist.html", true, false, false));
            } else {
                logger.info("Proxy initialisation failed.");
                error = new String("Could not initialise grid proxy with entered MyProxy details!");
            }

        }

        return new ModelAndView("myproxylogin", "error", error);
    }
}


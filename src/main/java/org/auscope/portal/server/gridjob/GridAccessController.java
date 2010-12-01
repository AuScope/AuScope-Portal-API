/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.gridtools.MyProxyManager;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.X509ExtensionSet;
import org.globus.gsi.bc.BouncyCastleCertProcessingFactory;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.wsrf.utils.FaultHelper;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;


/**
 * Following the MVC pattern, this class acts on events received by the UI,
 * and calls the methods in the Models (which actually do the work).
 *
 * @author Ryan Fraser
 * @author Terry Rankine
 * @author Darren Kidd
 * @author Cihan Altinay
 */
public class GridAccessController {

    /** The logger for this class */
    private static Log logger = LogFactory.getLog(GridAccessController.class);

    private String localGridStageInDir = "";
    
    /** MyProxy server to connect to */
    private static final String myProxyServer = "myproxy.arcs.org.au";

    /** MyProxy port to use */
    private static final int myProxyPort = 7512;

    /** Minimum lifetime for a proxy to be considered valid */
    private static final int MIN_LIFETIME = 5*60;

	/**
	 * @param localGridStageInDir the localGridStageInDir to set
	 */
	public void setlocalGridStageInDir(String localGridStageInDir) {
		this.localGridStageInDir = localGridStageInDir;
	}

	/**
	 * @return the localGridStageInDir
	 */
	public String getlocalGridStageInDir() {
		return localGridStageInDir;
	}

    /**
     * Kills a running grid job.
     *
     * @param reference The reference of the job to kill
     *
     * @return The status of the job after killing (a
     *         <code>StateEnumeration</code> string)
     */
    public String killJob(String reference, Object credential) {
        /*GramJobControl gjc = new GramJobControl((GSSCredential) credential);
        return gjc.killJob(reference);*/
    	return("Job killed");
    }

    /**
     * Checks the status of a job.
     *
     * @param reference The reference of the job to check the status of
     *
     * @return The status of the job (a <code>StateEnumeration</code> string)
     */
    public String retrieveJobStatus(String reference, Object credential) {
        /*GramJobControl gjc = new GramJobControl((GSSCredential) credential);
        return gjc.getJobStatus(reference);*/
    	return("Done");
    }

    /**
     * Starts a new job that transfers current files from given job
     * to the stage-out location.
     *
     * @param reference The reference of the job to get results from
     *
     * @return true if successful, false otherwise
     */
    public boolean retrieveJobResults(String reference, Object credential) {
        /*GramJobControl gjc = new GramJobControl((GSSCredential) credential);
        return (gjc.getJobResults(reference) != null);*/
    	return true;
    }

    /**
     * Initializes a grid proxy which will be used to authenticate the user
     * for all grid activities. Uses private key and certificate to generate
     * a proxy. These might have been obtained through a SLCS server.
     *
     * @param key The private key
     * @param certificate The certificate
     * @param lifetime Desired lifetime in seconds of the new proxy
     *
     * @return the grid credential object (which can be null)
     */
    public static Object initProxy(PrivateKey key, X509Certificate certificate,
                                   int lifetime) {
        GSSCredential credential = null;
        int bits = 512;
        int proxyType = GSIConstants.DELEGATION_FULL;
        X509ExtensionSet extSet = null;
        BouncyCastleCertProcessingFactory factory =
            BouncyCastleCertProcessingFactory.getDefault();
        try {
            GlobusCredential proxy = factory.createCredential(
                    new X509Certificate[] { certificate },
                    key, bits, lifetime, proxyType, extSet);
            credential = new GlobusGSSCredentialImpl(
                    proxy, GSSCredential.INITIATE_AND_ACCEPT);
            if (isProxyValid(credential)) {
                logger.info("Acquired valid credentials.");
            }
        } catch (Exception e) {
            logger.error("create user proxy error: "+e.toString(), e);
        }
        return credential;
    }

    /**
     * Initializes a grid proxy which will be used to authenticate the user
     * for all grid activities. Uses a username and password for MyProxy
     * authentication.
     *
     * @param proxyUser MyProxy username
     * @param proxyPass MyProxy password
     * @param lifetime  Desired lifetime in seconds of the new proxy
     * 
     * @return the grid credential object (which can be null)
     */
    public static Object initProxy(String proxyUser, String proxyPass,
                                   int lifetime) {
        GSSCredential credential = null;
        try {
            credential = MyProxyManager.getDelegation(
                    myProxyServer, myProxyPort,
                    proxyUser, proxyPass.toCharArray(),
                    lifetime);

            if (isProxyValid(credential)) {
                logger.info("Got credential from "+myProxyServer);
            }
        } catch (Exception e) {
            logger.error("Could not get delegated proxy from server: " +
                    e.getMessage());
        }
        return credential;
    }

    /**
     * Initializes a grid proxy which will be used to authenticate the user
     * for all grid activities. This method requires an existing proxy file of
     * the current user.
     *
     * @return the grid credential object (which can be null)
     */
    public static Object initProxy() {
        GSSCredential credential = null;
        try {
            GSSManager manager = ExtendedGSSManager.getInstance();
            credential = manager.createCredential(
                    GSSCredential.INITIATE_AND_ACCEPT);

            if (isProxyValid(credential)) {
                logger.info("Created credential from file.");
            }
        } catch (GSSException e) {
            logger.error(FaultHelper.getMessage(e));
        }
        return credential;
    }

    /**
     * Checks the validity of currently set grid credentials. To be considered
     * valid, the grid proxy must exist and have a minimum remaining lifetime
     * (5 minutes by default).
     *
     * @return true if and only if the current credentials are valid
     */
    public static boolean isProxyValid(Object credential) {
        if (credential != null) {
            try {
                GSSCredential cred = (GSSCredential) credential;
                int lifetime = cred.getRemainingLifetime();
                logger.debug("Name: " + cred.getName().toString() +
                        ", Lifetime: " + lifetime + " seconds");
                if (lifetime > MIN_LIFETIME) {
                    return true;
                }
            } catch (GSSException e) {
                logger.error(FaultHelper.getMessage(e));
            }
        }
        return false;
    }
}


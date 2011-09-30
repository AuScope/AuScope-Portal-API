package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.cloud.CloudFileInformation;
import org.auscope.portal.server.vegl.VEGLJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class for interacting with a Simple Storage Service (S3)
 *
 * @author Josh Vote
 *
 */
@Service
public class JobStorageService {

    protected final Log logger = LogFactory.getLog(getClass());
    IStorageStrategy delegate;



    @Autowired
    public JobStorageService(IStorageStrategy strategy) {
        this.delegate = strategy;
    }

    /**
     * Gets an input stream to the file specified by key
     *
     * @param job
     *            The job that owns the files
     * @param key
     *            The key of the file to download
     * @return
     * @throws Exception
     */
    public InputStream getJobFileData(VEGLJob job, String key)
            throws CloudStorageException {
        return delegate.getJobFileData(job, key);
    }

    /**
     * Gets information about all output files for a given job
     *
     * @param job
     *            The job to examine
     * @return
     * @throws Exception
     */
    public CloudFileInformation[] getOutputFileDetails(VEGLJob job)
            throws CloudStorageException {
        return delegate.getOutputFileDetails(job);
    }

    /**
     * Uploads the specified files to the job's input storage area
     *
     * @param job
     *            The job who will 'own' these input files
     * @param files
     *            The input files to be uploaded.
     * @throws Exception
     */
    public void uploadInputJobFiles(VEGLJob job, File[] files)
            throws CloudStorageException, NoSuchAlgorithmException, IOException {
        delegate.uploadInputJobFiles(job, files);
    }
}

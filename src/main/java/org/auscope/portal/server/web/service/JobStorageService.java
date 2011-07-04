package org.auscope.portal.server.web.service;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.gridjob.GeodesyJob;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.ProviderCredentials;
import org.springframework.stereotype.Service;

/**
 * A service class for interacting with a Simple Storage Service (S3)
 * @author Josh Vote
 *
 */
@Service
public class JobStorageService {

	protected final Log logger = LogFactory.getLog(getClass());
	
	/**
     * Generates a connection to the appropriate S3 that was used for this particular job
     * @param job
     * @return
     * @throws S3ServiceException
     */
    private S3Service generateS3ServiceForJob(GeodesyJob job) throws S3ServiceException {
        ProviderCredentials provCreds = new org.jets3t.service.security.AWSCredentials(job.getS3OutputAccessKey(), job.getS3OutputSecretKey());
        
        return new RestS3Service(provCreds);
    }
    
    /**
     * Gets the details of any files in the jobs output directory.
     * 
     * @param request The HttpServletRequest
     * @param job The job that that we want results for
     * @return Array of S3Objects, or null if no results available.
     * @throws S3ServiceException 
     */
    private S3Object[] getOutputS3Objects(GeodesyJob job) throws S3ServiceException {
    	S3Service s3Service = generateS3ServiceForJob(job);
		String outputDir = job.getOutputDir();
		String bucket = job.getS3OutputBucket();
		
		logger.debug(String.format("bucket='%1$s' outputDir='%2$s'", bucket, outputDir));
		
		S3Object[] objs = s3Service.listObjects(bucket,outputDir,null);
		if (objs == null) {
			return new S3Object[0];
		} else {
			return objs;
		}
    }
    
    /**
     * Gets an input stream to the file specified by key 
     * @param job The job that owns the files
     * @param key The key of the file to download
     * @return
     * @throws ServiceException
     */
    public InputStream getJobFileData(GeodesyJob job, String key) throws ServiceException {
    	S3Service service = generateS3ServiceForJob(job);
    	S3Object s3obj = service.getObject(job.getS3OutputBucket(), key);
		return s3obj.getDataInputStream();
    }
    
    /**
     * Gets information about all output files for a given job
     * @param job The job to examine
     * @return
     * @throws S3ServiceException
     */
    public FileInformation[] getOutputFileDetails(GeodesyJob job) throws S3ServiceException {
    	S3Object[] results = getOutputS3Objects(job);
    	FileInformation[] fileDetails = new FileInformation[results.length];
    	
    	int i = 0;
    	// get file information from s3 objects
    	for (S3Object object : results) {
    		fileDetails[i++] = new FileInformation(object.getKey(), object.getContentLength());
    	}
    	
    	return fileDetails;
    }
    
}

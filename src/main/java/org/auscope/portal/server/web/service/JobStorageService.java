package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.cloud.S3FileInformation;
import org.auscope.portal.server.vegl.VEGLJob;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
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
     * @param job The job you want a connection for
     * @return
     * @throws S3ServiceException
     */
    protected S3Service generateS3ServiceForJob(VEGLJob job) throws S3ServiceException {
        ProviderCredentials provCreds = new org.jets3t.service.security.AWSCredentials(job.getS3OutputAccessKey(), job.getS3OutputSecretKey());
        
        return new RestS3Service(provCreds);
    }
    
    /**
     * Generates a S3Object that can be used for uploading file
     * @param job The job who will 'own' the file
     * @param file The file to upload
     * @return
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     */
    protected S3Object generateS3ObjectForFile(VEGLJob job, S3Bucket bucket, File file) throws NoSuchAlgorithmException, IOException {
        String fileKeyPath = String.format("%1$s/%2$s", job.getS3OutputBaseKey(), file.getName());
        S3Object obj = new S3Object(bucket, file);
        obj.setKey(fileKeyPath);
        return obj;
    }
    
    /**
     * Gets the details of any files in the jobs output directory.
     * 
     * @param request The HttpServletRequest
     * @param job The job that that we want results for
     * @return Array of S3Objects, or null if no results available.
     * @throws S3ServiceException 
     */
    private S3Object[] getOutputS3Objects(VEGLJob job) throws S3ServiceException {
    	S3Service s3Service = generateS3ServiceForJob(job);
		String baseKey = job.getS3OutputBaseKey();
		String bucket = job.getS3OutputBucket();
		
		logger.debug(String.format("bucket='%1$s' baseKey='%2$s'", bucket, baseKey));
		
		S3Object[] objs = s3Service.listObjects(bucket,baseKey,null);
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
    public InputStream getJobFileData(VEGLJob job, String key) throws ServiceException {
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
    public S3FileInformation[] getOutputFileDetails(VEGLJob job) throws S3ServiceException {
    	S3Object[] results = getOutputS3Objects(job);
    	S3FileInformation[] fileDetails = new S3FileInformation[results.length];
    	
    	int i = 0;
    	// get file information from s3 objects
    	for (S3Object object : results) {
    		fileDetails[i++] = new S3FileInformation(object.getKey(), object.getContentLength());
    	}
    	
    	return fileDetails;
    }
    
    /**
     * Uploads the specified files to the job's input storage area
     * @param job The job who will 'own' these input files
     * @param files The input files to be uploaded.
     * @throws S3ServiceException 
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     */
    public void uploadInputJobFiles(VEGLJob job, File[] files) throws S3ServiceException, NoSuchAlgorithmException, IOException {
        S3Service s3Service = generateS3ServiceForJob(job);
        
        S3Bucket bucket = s3Service.getOrCreateBucket(job.getS3OutputBucket());
        if (bucket == null) {
            throw new S3ServiceException(String.format("Unable to get/create bucket '%1$s'", job.getS3OutputBucket()));
        }
        
        // copy job files to S3 storage service.
        for (File file : files) {
            S3Object obj = generateS3ObjectForFile(job, bucket, file);
            s3Service.putObject(bucket, obj);
            logger.info(obj.getKey() + " uploaded to " + bucket.getName() + " S3 bucket");
        }
    }
}

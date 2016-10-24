/**
 * 
 */
package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.InputStream;

import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudFileOwner;
import org.auscope.portal.core.services.PortalServiceException;

/**
 * @author fri096
 *
 */
public class CloudStorageServiceNCI extends CloudStorageService {
    public CloudStorageServiceNCI(String endpoint, String provider) {
        super(endpoint, provider);
    }

    public static final String JOB_DIR_PREFIX = "/short/gv3/";
    private static String defaultBucket = "vgl";
    
    public static String getJobDirectory(CloudFileOwner job) {
        return JOB_DIR_PREFIX+getBucket(job)+"/job-"+job.getId();
    }
    
    /* (non-Javadoc)
     * @see org.auscope.portal.core.services.cloud.CloudStorageService#getJobFile(org.auscope.portal.core.cloud.CloudFileOwner, java.lang.String)
     */
    @Override
    public InputStream getJobFile(CloudFileOwner job, String fileName) throws PortalServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.auscope.portal.core.services.cloud.CloudStorageService#listJobFiles(org.auscope.portal.core.cloud.CloudFileOwner)
     */
    @Override
    public CloudFileInformation[] listJobFiles(CloudFileOwner job) throws PortalServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.auscope.portal.core.services.cloud.CloudStorageService#deleteJobFiles(org.auscope.portal.core.cloud.CloudFileOwner)
     */
    @Override
    public void deleteJobFiles(CloudFileOwner job) throws PortalServiceException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.auscope.portal.core.services.cloud.CloudStorageService#getJobFileMetadata(org.auscope.portal.core.cloud.CloudFileOwner, java.lang.String)
     */
    @Override
    public CloudFileInformation getJobFileMetadata(CloudFileOwner job, String fileName) throws PortalServiceException {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.auscope.portal.core.services.cloud.CloudStorageService#uploadJobFiles(org.auscope.portal.core.cloud.CloudFileOwner, java.io.File[])
     */
    @Override
    public void uploadJobFiles(CloudFileOwner curJob, File[] files) throws PortalServiceException {
        // TODO Auto-generated method stub

    }
}

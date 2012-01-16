package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.cloud.CloudFileInformation;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.vegl.VEGLJob;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.InputStreamMap;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.BlobMetadataImpl;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.io.ContentMetadata;



public class JCloudSwiftStorageService implements IStorageStrategy {

    protected final Log logger = LogFactory.getLog(getClass());
    private ListContainerOptions lco=new ListContainerOptions();
    private BlobStoreContextFactory blobStoreContextFactory;
    Properties swiftBlobStore;

    public JCloudSwiftStorageService(
            PortalPropertyPlaceholderConfigurer hostConfigurer,
            BlobStoreContextFactory blobStoreContextFactory) {
        this.blobStoreContextFactory = blobStoreContextFactory;
        swiftBlobStore = new Properties();
        swiftBlobStore.setProperty("swift.endpoint", hostConfigurer
                .resolvePlaceholder("storage.endpoint"));
        swiftBlobStore.setProperty("jclouds.relax-hostname", "true");
    }

    public InputStream getJobFileData(VEGLJob job, String key)
            throws CloudStorageException {
        BlobStoreContext context = this.getBlobStoreContext(job);
        try {
             InputStreamMap map = context.createInputStreamMap(job
                     .getCloudOutputBucket(),lco.inDirectory(job.getCloudOutputBaseKey()));
            return map.get(key);
        } finally {
            context.close();
        }
    }


    public CloudFileInformation[] getOutputFileDetails(VEGLJob job)
            throws CloudStorageException {
        BlobStoreContext context = this.getBlobStoreContext(job);
        InputStreamMap map = context.createInputStreamMap(job
                .getCloudOutputBucket(),lco.inDirectory(job.getCloudOutputBaseKey()));
        CloudFileInformation[] fileDetails = new CloudFileInformation[map.size()];
        Iterable<? extends StorageMetadata> fileMetaDataList = map.list();

        try {
            int i = 0;
            for (StorageMetadata fileMetadata : fileMetaDataList) {
                fileDetails[i++] = new CloudFileInformation(
                        fileMetadata.getName(), getFileSize(fileMetadata),
                        fileMetadata.getUri().toString());
            }

            return fileDetails;
        } catch (Exception se) {
            throw new CloudStorageException(
                    "Error retriving output file details", se);
        } finally {
            context.close();
        }
    }


    public void uploadInputJobFiles(VEGLJob job, File[] files)
            throws CloudStorageException {
        BlobStoreContext context = this.getBlobStoreContext(job);
        try {
            BlobStore blobStore = context.getBlobStore();
            // create container;
             blobStore.createDirectory(job.getCloudOutputBucket(), job.getCloudOutputBaseKey());

            InputStreamMap map = context.createInputStreamMap(job
                    .getCloudOutputBucket(),lco.inDirectory(job.getCloudOutputBaseKey()));

            for (File file : files) {
                map.putFile(file.getName(), file);
                logger.info(file.getName() + " uploaded to " + job.getCloudOutputBucket()
                        + " swift container");
            }
        } catch (Exception e) {
            throw new CloudStorageException(
                    "Error in uploading file to swift storage", e);
        } finally {
            context.close();
        }

    }

    private Long getFileSize(StorageMetadata smd) {
        if (smd instanceof BlobMetadataImpl) {
            ContentMetadata cmd = ((BlobMetadataImpl) smd).getContentMetadata();
            return cmd.getContentLength();
        }else if (smd instanceof MutableBlobMetadataImpl){
            ContentMetadata cmd = ((MutableBlobMetadataImpl) smd).getContentMetadata();
            return cmd.getContentLength();
        }else{
            return 1L;
        }
    }



    private BlobStoreContext getBlobStoreContext(VEGLJob job) {
        swiftBlobStore.setProperty("swift.identity", job
                .getCloudOutputAccessKey());
        swiftBlobStore.setProperty("swift.credential", job
                .getCloudOutputSecretKey());
        BlobStoreContext context = blobStoreContextFactory.createContext(
                "swift", swiftBlobStore);
        return context;
    }

}

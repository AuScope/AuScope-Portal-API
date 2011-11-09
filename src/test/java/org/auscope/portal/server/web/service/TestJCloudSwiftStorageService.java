package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.auscope.portal.server.cloud.CloudFileInformation;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.vegl.VEGLJob;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.InputStreamMap;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.internal.MutableBlobMetadataImpl;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.io.ContentMetadata;
import org.jclouds.io.MutableContentMetadata;
import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.services.ec2.model.RunInstancesRequest;

/**
 * Unit tests for S3JobStorageService
 * @author Josh Vote
 *
 */
@RunWith(JMock.class)
public class TestJCloudSwiftStorageService {

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private BlobStoreContextFactory mockBlobStoreContextFactory = context.mock(BlobStoreContextFactory.class);
    private BlobStoreContext mockBlobStoreContext = context.mock(BlobStoreContext.class);
    private VEGLJob job;
    private  PortalPropertyPlaceholderConfigurer mockConfigurer=context.mock(PortalPropertyPlaceholderConfigurer.class);
    private JCloudSwiftStorageService jCloudSwiftStorageService;

    @Before
    public void initJobObject() {
        job = new VEGLJob(new Integer(13), "jobName", "jobDesc", "user",
                "user@email.com", null, null, "ec2InstanceId",
                "http://ec2.endpoint", "ec2Ami", "s3AccessKey", "s3SecretKey",
                "s3Bucket", "s3BaseKey", null, new Integer(45),
                "file-storage-id", "vm-subset-filepath", "http://vm.subset.url");

        context.checking(new Expectations() {{
           oneOf(mockConfigurer).resolvePlaceholder("storage.endpoint");will(returnValue("http://example.com"));
        }});
        jCloudSwiftStorageService=new JCloudSwiftStorageService(mockConfigurer,mockBlobStoreContextFactory);
    }


    @Test
    public void testGetJobFileData() throws Exception {
          final String myKey = "my/key";
          final InputStreamMap mockInputStream = context.mock(InputStreamMap.class);
          final InputStream mockReturnedInputStream = context.mock(InputStream.class);

          context.checking(new Expectations() {{
              oneOf(mockBlobStoreContextFactory).createContext(with(any(String.class)),with(any(Properties.class)));will(returnValue(mockBlobStoreContext));
              oneOf(mockBlobStoreContext).createInputStreamMap(with(any(String.class)),with(any(ListContainerOptions.class)));will(returnValue(mockInputStream));
              oneOf(mockBlobStoreContext).close();
              oneOf(mockInputStream).get(myKey);will(returnValue(mockReturnedInputStream));
          }});

          InputStream actualInputStream = this.jCloudSwiftStorageService.getJobFileData(job, myKey);
          Assert.assertSame(mockReturnedInputStream, actualInputStream);
    }

    /**
     * Tests that requests for listing files successfully call all dependencies
     * @throws Exception
     */
    @Test
    public void testListOutputJobFiles() throws Exception {
          final InputStreamMap mockInputStreamMap = context.mock(InputStreamMap.class);
          final MutableContentMetadata mockObj1ContentMetadata=context.mock(MutableContentMetadata.class,"mockObj1Md");
          final MutableContentMetadata mockObj2ContentMetadata=context.mock(MutableContentMetadata.class,"mockObj2Md");

          LinkedList<MutableBlobMetadataImpl> ls=new LinkedList<MutableBlobMetadataImpl>();
          ls.add(context.mock(MutableBlobMetadataImpl.class,"mockObj1"));
          ls.add(context.mock(MutableBlobMetadataImpl.class,"mockObj2"));

          final Iterable<? extends MutableBlobMetadataImpl> mockFileMetaDataList=ls;

          final String obj1Key = "key/obj1";
          final String obj1Bucket = "bucket1";
          final String obj1PublicUrl = "http://obj1.url.public";
          final long obj1Length = 1234L;
          final String obj2Key = "key/obj2";
          final String obj2Bucket = "bucket2";
          final String obj2PublicUrl = "http://obj1.url.public";
          final long obj2Length = 4567L;

          context.checking(new Expectations() {{
              oneOf(mockBlobStoreContextFactory).createContext(with(any(String.class)),with(any(Properties.class)));will(returnValue(mockBlobStoreContext));
              oneOf(mockBlobStoreContext).createInputStreamMap(with(any(String.class)),with(any(ListContainerOptions.class)));will(returnValue(mockInputStreamMap));
              oneOf(mockInputStreamMap).size();will(returnValue(2));
              oneOf(mockInputStreamMap).list();will(returnValue(mockFileMetaDataList));
              allowing(mockBlobStoreContext).close();
                int i = 0;
                for (MutableBlobMetadataImpl fileMetadata : mockFileMetaDataList) {
                    if (i == 0) {
                        allowing(fileMetadata).getName();will(returnValue(obj1Key));
                        allowing(fileMetadata).getUri();will(returnValue(new URI(obj1Bucket)));
                        allowing(fileMetadata).getContentMetadata();will(returnValue(mockObj1ContentMetadata));
                        allowing(mockObj1ContentMetadata).getContentLength();will(returnValue(obj1Length));
                    } else {
                        allowing(fileMetadata).getName();will(returnValue(obj2Key));
                        allowing(fileMetadata).getUri();will(returnValue(new URI(obj2Bucket)));
                        allowing(fileMetadata).getContentMetadata();will(returnValue(mockObj2ContentMetadata));
                        allowing(mockObj2ContentMetadata).getContentLength();will(returnValue(obj2Length));
                    }
                    i++;
                }

          }});

          CloudFileInformation[] fileInfo = this.jCloudSwiftStorageService.getOutputFileDetails(job);
          Assert.assertNotNull(fileInfo);
          Assert.assertEquals(ls.size(), fileInfo.length);
          Assert.assertEquals(obj1Key, fileInfo[0].getCloudKey());
          Assert.assertEquals(obj1Length, fileInfo[0].getSize());
          Assert.assertEquals(obj2Key, fileInfo[1].getCloudKey());
          Assert.assertEquals(obj2Length, fileInfo[1].getSize());

    }


    /**
     * Tests that requests for uploading files successfully call all dependencies
     * @throws Exception
     */
    @Test
    public void testUploadJobFiles() throws Exception {
        final InputStreamMap mockInputStreamMap = context.mock(InputStreamMap.class);
        final BlobStore mockBlobStore = context.mock(BlobStore.class);

        final File[] mockFiles = new File[] {
                context.mock(File.class, "mockFile1"),
                context.mock(File.class, "mockFile2"),
        };

        context.checking(new Expectations() {{
            oneOf(mockBlobStoreContextFactory).createContext(with(any(String.class)),with(any(Properties.class)));will(returnValue(mockBlobStoreContext));
            oneOf(mockBlobStoreContext).createInputStreamMap(with(any(String.class)),with(any(ListContainerOptions.class)));will(returnValue(mockInputStreamMap));
            oneOf(mockBlobStoreContext).getBlobStore();will(returnValue(mockBlobStore));
            allowing(mockBlobStore).createDirectory(job.getCloudOutputBucket(), job.getCloudOutputBaseKey());
            allowing(mockFiles[0]).getName();will(returnValue("file1Name"));
            allowing(mockFiles[1]).getName();will(returnValue("file2Name"));
            oneOf(mockInputStreamMap).putFile("file1Name", mockFiles[0]);
            oneOf(mockInputStreamMap).putFile("file2Name", mockFiles[1]);
            allowing(mockBlobStoreContext).close();
        }});

        this.jCloudSwiftStorageService.uploadInputJobFiles(job, mockFiles);
    }

}

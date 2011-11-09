package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.auscope.portal.server.cloud.CloudFileInformation;
import org.auscope.portal.server.vegl.VEGLJob;
import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.RunInstancesRequest;

/**
 * Unit tests for S3JobStorageService
 * @author Josh Vote
 *
 */
public class TestJobStorageService extends S3JobStorageService {

    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private S3Service mockS3Service = context.mock(S3Service.class);
    private VEGLJob job;
    private Map<String, S3Object> fileToS3ObjectMap;

    public TestJobStorageService() {
        super();
    }

    @Before
    public void initJobObject() {
        job = new VEGLJob(new Integer(13), "jobName", "jobDesc", "user",
                "user@email.com", null, null, "ec2InstanceId",
                "http://ec2.endpoint", "ec2Ami", "s3AccessKey", "s3SecretKey",
                "s3Bucket", "s3BaseKey", null, new Integer(45),
                "file-storage-id", "vm-subset-filepath", "http://vm.subset.url");
        fileToS3ObjectMap = new HashMap<String, S3Object>();
    }

    /**
     * This is so we can inject our own mock S3Service
     */
    @Override
    protected S3Service generateS3ServiceForJob(VEGLJob job) {
        return mockS3Service;
    }

    /**
     * This is so we can inject our own mock S3Objects (drawn from fileToS3ObjectMap based on file.getPath())
     */
    @Override
    protected S3Object generateS3ObjectForFile(VEGLJob job, S3Bucket bucket, File file) {
        return fileToS3ObjectMap.get(file.getPath());
    }

    /**
     * Tests that requests for file data successfully call all dependencies
     * @throws Exception
     */
    @Test
    public void testGetJobFileData() throws Exception {
        final String myKey = "my/key";
        final S3Object mockS3Obj = context.mock(S3Object.class);
        final InputStream mockInputStream = context.mock(InputStream.class);

        context.checking(new Expectations() {{
            oneOf(mockS3Service).getObject(job.getCloudOutputBucket(), myKey);will(returnValue(mockS3Obj));
            oneOf(mockS3Obj).getDataInputStream();will(returnValue(mockInputStream));
        }});

        InputStream actualInputStream = this.getJobFileData(job, myKey);
        Assert.assertSame(mockInputStream, actualInputStream);
    }

    /**
     * Tests that requests for listing files successfully call all dependencies
     * @throws Exception
     */
    @Test
    public void testListOutputJobFiles() throws Exception {
        final S3Object[] mockS3Objects = new S3Object[] {
                context.mock(S3Object.class, "mockS3Obj1"),
                context.mock(S3Object.class, "mockS3Obj2"),
        };
        final String obj1Key = "key/obj1";
        final String obj1Bucket = "bucket1";
        final String obj1PublicUrl = "http://obj1.url.public";
        final long obj1Length = 1234L;
        final String obj2Key = "key/obj2";
        final String obj2Bucket = "bucket2";
        final String obj2PublicUrl = "http://obj1.url.public";
        final long obj2Length = 4567L;

        context.checking(new Expectations() {{
            oneOf(mockS3Service).listObjects(job.getCloudOutputBucket(), job.getCloudOutputBaseKey(), null);will(returnValue(mockS3Objects));

            //We will be also creating URL's for the files to be publicly listed (assuming ACL priv's are set)
            oneOf(mockS3Service).createUnsignedObjectUrl(obj1Bucket, obj1Key, false, false, false);will(returnValue(obj1PublicUrl));
            oneOf(mockS3Service).createUnsignedObjectUrl(obj2Bucket, obj2Key, false, false, false);will(returnValue(obj2PublicUrl));

            allowing(mockS3Objects[0]).getKey();will(returnValue(obj1Key));
            allowing(mockS3Objects[0]).getBucketName();will(returnValue(obj1Bucket));
            allowing(mockS3Objects[0]).getContentLength();will(returnValue(obj1Length));
            allowing(mockS3Objects[1]).getKey();will(returnValue(obj2Key));
            allowing(mockS3Objects[1]).getBucketName();will(returnValue(obj2Bucket));
            allowing(mockS3Objects[1]).getContentLength();will(returnValue(obj2Length));
        }});

        CloudFileInformation[] fileInfo = this.getOutputFileDetails(job);

        Assert.assertNotNull(fileInfo);
        Assert.assertEquals(mockS3Objects.length, fileInfo.length);
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
        final File[] mockFiles = new File[] {
                context.mock(File.class, "mockFile1"),
                context.mock(File.class, "mockFile2"),
        };
        final S3Object[] mockObjects = new S3Object[] {
                context.mock(S3Object.class, "mockObj1"),
                context.mock(S3Object.class, "mockObj2"),
        };
        final S3Bucket mockBucket = context.mock(S3Bucket.class);

        context.checking(new Expectations() {{
            oneOf(mockS3Service).getOrCreateBucket(job.getCloudOutputBucket());will(returnValue(mockBucket));

            allowing(mockBucket).getName();will(returnValue("bucketName"));

            allowing(mockFiles[0]).getName();will(returnValue("file1Name"));
            allowing(mockFiles[0]).length();will(returnValue(1234L));
            allowing(mockFiles[0]).exists();will(returnValue(true));
            allowing(mockFiles[0]).getPath();will(returnValue("my/path/file1Name"));
            allowing(mockFiles[1]).getName();will(returnValue("file2Name"));
            allowing(mockFiles[1]).length();will(returnValue(4567L));
            allowing(mockFiles[1]).exists();will(returnValue(true));
            allowing(mockFiles[1]).getPath();will(returnValue("my/path/file2Name"));

            allowing(mockObjects[0]).getKey();will(returnValue("key/mockObj1"));
            allowing(mockObjects[1]).getKey();will(returnValue("key/mockObj2"));

            oneOf(mockS3Service).putObject(mockBucket, mockObjects[0]);
            oneOf(mockS3Service).putObject(mockBucket, mockObjects[1]);
        }});

        //Populate our File -> S3Object map (a necessary evil to emulate our files
        //being correctly created into S3Object's)
        for (int i = 0; i < mockFiles.length; i++) {
            fileToS3ObjectMap.put(mockFiles[i].getPath(), mockObjects[i]);
        }

        this.uploadInputJobFiles(job, mockFiles);
    }
}

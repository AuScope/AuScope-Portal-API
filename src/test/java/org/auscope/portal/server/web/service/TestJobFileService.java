package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.jmock.FileWithNameMatcher;
import org.auscope.portal.jmock.ReadableServletOutputStream;
import org.auscope.portal.server.cloud.StagingInformation;
import org.auscope.portal.server.util.FileUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Unit tests for JobFileService
 * @author Josh Vote
 *
 */
public class TestJobFileService extends JobFileService {
    
    private static StagingInformation testStagingInfo;
    private static int testCounter = 0;
    
    private VEGLJob job;
    
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    public TestJobFileService() {
        super(testStagingInfo);
    }
    
    /**
     * This sets up a temporary directory in the target directory for the JobFileService
     * to utilise as a staging area
     */
    @BeforeClass 
    public static void setup() {
        testStagingInfo = new StagingInformation();
        
        testStagingInfo.setStageInDirectory(String.format(
                "target%1$sTestJobFileService-%2$s%1$s", 
                File.separator, new Date().getTime()));
        
        File dir = new File(testStagingInfo.getStageInDirectory());
        Assert.assertTrue("Failed setting up staging directory", dir.mkdirs());
    }
    
    /**
     * This tears down the staging area used by the tests
     */
    @AfterClass
    public static void tearDown() {
        File dir = new File(testStagingInfo.getStageInDirectory());
        FileUtil.deleteFilesRecursive(dir);
    }
    
    /**
     * Creates a fresh job object for each unit test (with a unique fileStorageID).
     */
    @Before
    public void setupJobObj() {
        String fileStorageId = "file-storage-" + testCounter;
        testCounter++;
        job = new VEGLJob(new Integer(13), "jobName", "jobDesc", "user",
                "user@email.com", null, null, "ec2InstanceId",
                "http://ec2.endpoint", "ec2Ami", "s3AccessKey", "s3SecretKey",
                "s3Bucket", "s3BaseKey", null, new Integer(45),
                fileStorageId, "vm-subset-filepath");
        
    }
    
    /**
     * Asserts that the shared staging directory (not the job staging directory) still exists after
     * a unit test is run 
     */
    @After
    public void ensureStagingDirectoryPreserved() {
        File dir = new File(testStagingInfo.getStageInDirectory());
        Assert.assertTrue(dir.exists());
        Assert.assertTrue(dir.isDirectory());
    }
    
    /**
     * Tests for the pathConcat utility method 
     */
    @Test
    public void testPathConcat() {
        Assert.assertEquals("p1" + File.separator + "p2", pathConcat("p1", "p2"));
        Assert.assertEquals("p1" + File.separator + "p2", pathConcat("p1" + File.separator, "p2"));
        Assert.assertEquals("p1" + File.separator + "p2", pathConcat("p1", File.separator + "p2"));
        Assert.assertEquals("p1" + File.separator + "p2", pathConcat("p1" + File.separator, File.separator + "p2"));
    }
    
    /**
     * Tests the existence/nonexistence of job's stage in directory
     * @param job
     * @param exists
     */
    private void assertStagedDirectory(VEGLJob job, boolean exists) {
        File stageInDir = new File(pathConcat(stagingInformation.getStageInDirectory(), job.getFileStorageId()));
        Assert.assertEquals(exists, stageInDir.exists());
        if (exists) {
            Assert.assertEquals(true, stageInDir.isDirectory());
        }
    }
    
    /**
     * Tests the existence/nonexistence of job's stage in file
     * @param job
     * @param exists
     */
    private void assertStagedFile(VEGLJob job, String fileName, boolean exists) {
        String stageInDir = pathConcat(stagingInformation.getStageInDirectory(), job.getFileStorageId());
        File stageInFile = new File(pathConcat(stageInDir, fileName));
        
        Assert.assertEquals(exists, stageInFile.exists());
        if (exists) {
            Assert.assertEquals(true, stageInFile.isFile());
        }
    }
    
    private static FileWithNameMatcher aFileWithName(String fileName) {
        return new FileWithNameMatcher(fileName);
    }
    
    /**
     * Tests that creating/deleting an empty job staging area works
     * @throws IOException 
     */
    @Test
    public void testEmptyStartupTeardown() throws IOException {
        this.generateStageInDirectory(job);
        
        assertStagedDirectory(job, true);
        
        this.deleteStageInDirectory(job);
        
        assertStagedDirectory(job, false);
    }
    
    /**
     * Tests that creating and listing files in a job staging area works
     * @throws IOException 
     */
    @Test
    public void testFileCreationAndListing() throws IOException {
        this.generateStageInDirectory(job);
        
        File file1 = this.createStageInDirectoryFile(job, "testFile1");
        File file2 = this.createStageInDirectoryFile(job, "testFile2");
        
        Assert.assertTrue(file1.createNewFile());
        Assert.assertTrue(file2.createNewFile());
        
        assertStagedDirectory(job, true);
        assertStagedFile(job, "testFile1", true);
        assertStagedFile(job, "testFile2", true);
        
        //Ensure that listing returns all the files (in no particular order)
        File[] expectedFiles = new File[] {file1, file2};
        File[] listedFiles = this.listStageInDirectoryFiles(job);
        Assert.assertNotNull(listedFiles);
        Assert.assertEquals(expectedFiles.length, listedFiles.length);
        for (File expectedFile : expectedFiles) {
            boolean foundFile = false;
            for (File listedFile : listedFiles) {
                if (listedFile.getAbsoluteFile().equals(expectedFile.getAbsoluteFile())) {
                    foundFile = true;
                    break;
                }
            }
            
            Assert.assertTrue(String.format("File '%1$s' not listed", expectedFile.getAbsoluteFile()), foundFile);
        }
        
        this.deleteStageInDirectory(job);
        assertStagedDirectory(job, false);
        assertStagedFile(job, "testFile1", false);
        assertStagedFile(job, "testFile2", false);
    }
    
    /**
     * Tests that using relative paths in a filename will generate exceptions
     * @throws IOException 
     */
    @Test
    public void testBadFilenames() throws IOException {
        this.generateStageInDirectory(job);
        
        //Should either return null or throw exception
        try {
            File file = this.createStageInDirectoryFile(job, pathConcat("..", "testFile1"));
            Assert.assertNull(file);
        } catch (Exception ex) { }
        try {
            File file = this.createStageInDirectoryFile(job, "testFile1" + File.pathSeparator + "testFile2");
            Assert.assertNull(file);
        } catch (Exception ex) { }
        
        this.deleteStageInDirectory(job);
    }
    
    /**
     * Tests that file uploads can be handled
     * @throws Exception 
     */
    @Test
    public void testFileUpload() throws Exception {
        final MultipartHttpServletRequest request = context.mock(MultipartHttpServletRequest.class);
        final MultipartFile file = context.mock(MultipartFile.class);
        final String fileName = "myFileName";
        
        context.checking(new Expectations() {{
            oneOf(request).getFile("file");will(returnValue(file));
            oneOf(file).getOriginalFilename();will(returnValue(fileName));
            oneOf(file).transferTo(with(aFileWithName(fileName)));
        }});
        
        this.generateStageInDirectory(job);
        
        //"Upload" the file and check it gets created
        File newlyStagedFile = this.handleFileUpload(job, request);
        Assert.assertNotNull(newlyStagedFile);
        Assert.assertEquals(fileName, newlyStagedFile.getName());
        
        this.deleteStageInDirectory(job);
    }
    
    /**
     * Tests that file downloads are handled correctly
     * @throws Exception
     */
    @Test
    public void testFileDownload() throws Exception {
        final ReadableServletOutputStream outStream = new ReadableServletOutputStream();
        final byte[] data = new byte[] {1,2,3,4,5,6,7,8,8,5,8,9,9,9,91,1,1};
        final HttpServletResponse mockResponse = context.mock(HttpServletResponse.class);
        final String fileName = "myFileName";
        
        //Start by creating our file that we want to download
        this.generateStageInDirectory(job);
        File file = this.createStageInDirectoryFile(job, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
        
        context.checking(new Expectations() {{
            //This is so we can inject our own fake output stream so we can inspect the result
            oneOf(mockResponse).getOutputStream();will(returnValue(outStream));
            oneOf(mockResponse).setContentType("application/octet-stream");
            allowing(mockResponse).setHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");
         }});
        
        //'Download' the file
        this.handleFileDownload(job, fileName, mockResponse);
        
        //Inspect the data we downloaded
        Assert.assertArrayEquals(data, outStream.getDataWritten());
        
        this.deleteStageInDirectory(job);
    }
}

    

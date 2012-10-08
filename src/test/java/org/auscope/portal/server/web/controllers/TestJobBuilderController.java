package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.auscope.portal.core.cloud.StagedFile;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.test.ResourceUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.vegl.VglMachineImage;
import org.auscope.portal.server.vegl.VglParameter;
import org.auscope.portal.server.web.service.VglMachineImageService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * Unit tests for GridSubmitController
 * @author Josh Vote
 *
 */
public class TestJobBuilderController {
    private Mockery context = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private VEGLJobManager mockJobManager;
    private FileStagingService mockFileStagingService;
    private CloudStorageService mockCloudStorageService;
    private PortalPropertyPlaceholderConfigurer mockHostConfigurer;
    private CloudComputeService mockCloudComputeService;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private VglMachineImageService mockImageService;
    private HttpSession mockSession;

    private JobBuilderController controller;

    @Before
    public void init() {
        mockJobManager = context.mock(VEGLJobManager.class);
        mockFileStagingService = context.mock(FileStagingService.class);
        mockHostConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);
        mockCloudStorageService = context.mock(CloudStorageService.class);
        mockCloudComputeService = context.mock(CloudComputeService.class);
        mockRequest = context.mock(HttpServletRequest.class);
        mockResponse = context.mock(HttpServletResponse.class);
        mockImageService = context.mock(VglMachineImageService.class);
        mockSession = context.mock(HttpSession.class);

        controller = new JobBuilderController(mockJobManager, mockFileStagingService, mockHostConfigurer, mockCloudStorageService, mockCloudComputeService, mockImageService);
    }

    /**
     * Tests that job submission correctly interacts with all dependencies
     * @throws Exception
     */
    @Test
    public void testJobSubmission() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(new Integer(13));
        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(jobObj, "mockFile1", mockFile1), new StagedFile(jobObj, "mockFile2", mockFile2)};
        final String instanceId = "new-instance-id";
        final Sequence jobFileSequence = context.sequence("jobFileSequence"); //this makes sure we aren't deleting directories before uploading (and other nonsense)
        
        final OutputStream mockOutputStream = context.mock(OutputStream.class);

        jobObj.setStorageBaseKey("base/key");
        jobObj.setStorageAccessKey("accessKey");
        jobObj.setStorageBucket("bucket");
        jobObj.setStorageEndpoint("http://example.com/storage");
        jobObj.setStorageProvider("example-storage-provider");
        jobObj.setStorageSecretKey("secretKey");

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            oneOf(mockFileStagingService).writeFile(jobObj, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(mockOutputStream));

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));
            inSequence(jobFileSequence);

            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));

            //And one call to upload them
            oneOf(mockCloudStorageService).uploadJobFiles(with(equal(jobObj)), with(equal(new File[] {mockFile1, mockFile2})));
            inSequence(jobFileSequence);

            //And finally 1 call to execute the job
            oneOf(mockCloudComputeService).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(returnValue(instanceId));

            //This MUST occur - it cleans up after upload
            oneOf(mockFileStagingService).deleteStageInDirectory(jobObj);will(returnValue(true));
            inSequence(jobFileSequence);
            
            oneOf(mockOutputStream).close();
        }});

        
        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(instanceId, jobObj.getComputeInstanceId());

    }

    /**
     * Tests that job submission correctly interacts with all dependencies
     * @throws Exception
     */
    @Test
    public void testJobSubmission_JobDNE() throws Exception {
        final String jobId = "24";
        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(Integer.parseInt(jobId));will(returnValue(null));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobId);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that job submission fails correctly when files cannot be uploaded to S3
     * @throws Exception
     */
    @Test
    public void testJobSubmission_S3Failure() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(13);
        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(jobObj, "mockFile1", mockFile1), new StagedFile(jobObj, "mockFile2", mockFile2)};

        final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            oneOf(mockFileStagingService).writeFile(jobObj, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(bos));

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //And one call to upload them (which we will mock as failing)
            oneOf(mockCloudStorageService).uploadJobFiles(with(equal(jobObj)), with(any(File[].class)));will(throwException(new PortalServiceException("")));
        }});

        
        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_FAILED, jobObj.getStatus());
    }

    /**
     * Tests that job submission fails correctly when files cannot be uploaded to S3
     * @throws Exception
     */
    @Test
    public void testJobSubmission_ExecuteFailure() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(13);
        //As submitJob method no longer explicitly checks for empty storage credentials,
        //we need to manually set the storageBaseKey property to avoid NullPointerException
        jobObj.setStorageBaseKey("storageBaseKey");
        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(jobObj, "mockFile1", mockFile1), new StagedFile(jobObj, "mockFile2", mockFile2)};

        final OutputStream mockOutputStream = context.mock(OutputStream.class);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            oneOf(mockFileStagingService).writeFile(jobObj, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(mockOutputStream));

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));

            //And one call to upload them
            oneOf(mockCloudStorageService).uploadJobFiles(with(equal(jobObj)), with(any(File[].class)));

            //And finally 1 call to execute the job (which will throw PortalServiceException indicating failure)
            oneOf(mockCloudComputeService).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(throwException(new PortalServiceException("")));
            
            oneOf(mockOutputStream).close();
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_FAILED, jobObj.getStatus());
        
    }

    /**
     * Tests that the bootstrap resource is not too long and has unix line endings and other such
     * conditions.
     * @throws Exception
     */
    @Test
    public void testBootstrapResource() throws Exception {
        //see - http://docs.amazonwebservices.com/AutoScaling/latest/APIReference/API_CreateLaunchConfiguration.html
        final int maxFileSize = 21847;
        final int safeFileSize = maxFileSize - 1024; //arbitrary number to account for long strings being injected into bootstrap

        String contents = ResourceUtil.loadResourceAsString("org/auscope/portal/server/web/controllers/vgl-bootstrap.sh");

        Assert.assertNotNull(contents);
        Assert.assertTrue("Bootstrap is empty!", contents.length() > 0);
        Assert.assertTrue("Bootstrap is too big!", contents.length() < safeFileSize);
        Assert.assertFalse("Boostrap needs Unix style line endings!", contents.contains("\r"));
        Assert.assertEquals("Boostrap must start with '#'", '#', contents.charAt(0));

        //We can't use variables in the form ${name} as the {} conflict with java MessageFormat
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(contents);
        while (matcher.find()) {

            if (matcher.groupCount() != 1) {
                continue;
            }
            String name = matcher.group(1);

            try {
                Integer.parseInt(name);
            } catch (NumberFormatException ex) {
                Assert.fail(String.format("The variable ${%1$s} conflicts with java MessageFormat variables. Get rid of curly braces", name));
            }
        }
    }

    /**
     * Tests that Grid Submit Controller's usage of the bootstrap template
     * @throws Exception
     */
    @Test
    public void testCreateBootstrapForJob() throws Exception {
        final VEGLJob job = new VEGLJob(1234);

        context.checking(new Expectations() {{
            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));
        }});

        job.setStorageBucket("stora124e-Bucket");
        job.setStorageAccessKey("213-asd-54");
        job.setStorageBaseKey("test/key");
        job.setStorageSecretKey("tops3cret");

        String contents = controller.createBootstrapForJob(job);
        Assert.assertNotNull(contents);
        Assert.assertTrue("Bootstrap is empty!", contents.length() > 0);
        Assert.assertFalse("Boostrap needs Unix style line endings!", contents.contains("\r"));
        Assert.assertTrue(contents.contains(job.getStorageBucket()));
        Assert.assertTrue(contents.contains(job.getStorageAccessKey()));
        Assert.assertTrue(contents.contains(job.getStorageBaseKey()));
        Assert.assertTrue(contents.contains(job.getStorageSecretKey()));
    }

    /**
     * Tests that listing job images for a user works as expected
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testListImages() throws Exception {
        final VglMachineImage[] images = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        context.checking(new Expectations() {{
            //We allow calls to the Configurer which simply extract values from our property file
            oneOf(mockImageService).getAllImages();will(returnValue(images));
        }});

        ModelAndView mav = controller.getImagesForUser(mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertNotNull(mav.getModel().get("data"));
        Assert.assertEquals(images.length, ((List) mav.getModel().get("data")).size());
    }

    /**
     * Tests that listing job images for a user fails as expected
     * @throws Exception
     */
    @Test
    public void testListImagesError() throws Exception {
        context.checking(new Expectations() {{
            //We allow calls to the Configurer which simply extract values from our property file
            oneOf(mockImageService).getAllImages();will(throwException(new PortalServiceException("error")));
        }});

        ModelAndView mav = controller.getImagesForUser(mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests the creation of a new job object.
     * @throws Exception
     */
    @Test
    public void testCreateJobObject() throws Exception {
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        final Sequence sequence = context.sequence("setup-sequence"); //ensure we dont save the job until AFTER everything is setup
        final String storageProvider = "swift";
        final String storageEndpoint = "http://example.org/storage";
        final String baseKey = "base/key";


        sessionVariables.put("doubleValue", 123.45);
        sessionVariables.put("intValue", 123);
        sessionVariables.put("openID-Email", "email@example.org");
        sessionVariables.put("notExtracted", new Object()); //this should NOT be requested

        context.checking(new Expectations() {{
            //A whole bunch of parameters will be setup based on what session variables are set
            oneOf(mockRequest).getSession();inSequence(sequence);will(returnValue(mockSession));

            oneOf(mockSession).getAttributeNames();will(returnValue(new IteratorEnumeration(sessionVariables.keySet().iterator())));
            allowing(mockSession).getAttribute("doubleValue");will(returnValue(sessionVariables.get("doubleValue")));
            allowing(mockSession).getAttribute("intValue");will(returnValue(sessionVariables.get("intValue")));
            allowing(mockSession).getAttribute("openID-Email");will(returnValue(sessionVariables.get("openID-Email")));
            allowing(mockSession).getAttribute("notExtracted");will(returnValue(sessionVariables.get("notExtracted")));
            allowing(mockSession).getAttribute(ERRDAPController.SESSION_ERRDAP_DOWNLOAD_LIST);will(returnValue(null));

            oneOf(mockCloudStorageService).generateBaseKey(with(any(VEGLJob.class)));will(returnValue(baseKey));

            oneOf(mockHostConfigurer).resolvePlaceholder("storage.provider");will(returnValue(storageProvider));
            oneOf(mockHostConfigurer).resolvePlaceholder("storage.endpoint");will(returnValue(storageEndpoint));

            oneOf(mockJobManager).saveJob(with(any(VEGLJob.class)));inSequence(sequence); //one save job to get ID

            oneOf(mockFileStagingService).generateStageInDirectory(with(any(VEGLJob.class)));inSequence(sequence);

            oneOf(mockJobManager).saveJob(with(any(VEGLJob.class)));inSequence(sequence); //another to finish off
        }});

        ModelAndView mav = controller.createJobObject(mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        List<VEGLJob> data = (List<VEGLJob>)mav.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());

        VEGLJob newJob = data.get(0);
        Assert.assertNotNull(newJob);
        Assert.assertEquals(storageEndpoint, newJob.getStorageEndpoint());
        Assert.assertEquals(storageProvider, newJob.getStorageProvider());
        Assert.assertEquals(baseKey, newJob.getStorageBaseKey());

        Map<String, VglParameter> params = newJob.getJobParameters();
        Assert.assertNotNull(params);
        Assert.assertEquals(3, params.size());


        String paramToTest = "doubleValue";
        VglParameter param = params.get(paramToTest);
        Assert.assertNotNull(param);
        Assert.assertEquals("number", param.getType());
        Assert.assertEquals(sessionVariables.get(paramToTest).toString(), param.getValue());

        paramToTest = "intValue";
        param = params.get(paramToTest);
        Assert.assertNotNull(param);
        Assert.assertEquals("number", param.getType());
        Assert.assertEquals(sessionVariables.get(paramToTest).toString(), param.getValue());

        paramToTest = "openID-Email";
        param = params.get(paramToTest);
        Assert.assertNotNull(param);
        Assert.assertEquals("string", param.getType());
        Assert.assertEquals(sessionVariables.get(paramToTest), param.getValue());
    }

    /**
     * Tests that the updateJobDownloads works as expected when appending
     * @throws Exception
     */
    @Test
    public void testUpdateJobDownloads_Append() throws Exception {
        final Integer jobId = 124;
        final String append = "true";
        final String[] names = new String[] {"n1", "n2"};
        final String[] descriptions = new String[] {"d1", "d2"};
        final String[] urls = new String[] {"http://example.org/1", "http://example.org/2"};
        final String[] localPaths = new String[] {"p1", "p2"};

        final VEGLJob myJob = new VEGLJob(jobId);
        final VglDownload[] existingDownloads = new VglDownload[] {new VglDownload(12356)};


        myJob.setJobDownloads(new ArrayList<VglDownload>(Arrays.asList(existingDownloads)));

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(myJob));

            oneOf(mockJobManager).saveJob(myJob);
        }});

        ModelAndView mav = controller.updateJobDownloads(jobId, append, names, descriptions, urls, localPaths);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        //The resulting job should have 3 elements in its list (due to appending)
        List<VglDownload> dls = myJob.getJobDownloads();
        Assert.assertEquals(existingDownloads.length + names.length, dls.size());
        Assert.assertSame(existingDownloads[0], dls.get(0));
        for (int i = 0; i < names.length; i++) {
            VglDownload dlToTest = dls.get(existingDownloads.length + i);
            Assert.assertEquals(names[i], dlToTest.getName());
            Assert.assertEquals(descriptions[i], dlToTest.getDescription());
            Assert.assertEquals(urls[i], dlToTest.getUrl());
            Assert.assertEquals(localPaths[i], dlToTest.getLocalPath());
        }
    }

    /**
     * Tests that the updateJobDownloads works as expected when replacing
     * @throws Exception
     */
    @Test
    public void testUpdateJobDownloads_Replace() throws Exception {
        final Integer jobId = 124;
        final String append = "false";
        final String[] names = new String[] {"n1", "n2"};
        final String[] descriptions = new String[] {"d1", "d2"};
        final String[] urls = new String[] {"http://example.org/1", "http://example.org/2"};
        final String[] localPaths = new String[] {"p1", "p2"};

        final VEGLJob myJob = new VEGLJob(jobId);
        final VglDownload[] existingDownloads = new VglDownload[] {new VglDownload(12356)};


        myJob.setJobDownloads(new ArrayList<VglDownload>(Arrays.asList(existingDownloads)));

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(myJob));

            oneOf(mockJobManager).saveJob(myJob);
        }});

        ModelAndView mav = controller.updateJobDownloads(jobId, append, names, descriptions, urls, localPaths);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        //The resulting job should have 3 elements in its list (due to appending)
        List<VglDownload> dls = myJob.getJobDownloads();
        Assert.assertEquals(names.length, dls.size());
        for (int i = 0; i < names.length; i++) {
            VglDownload dlToTest = dls.get(i);
            Assert.assertEquals(names[i], dlToTest.getName());
            Assert.assertEquals(descriptions[i], dlToTest.getDescription());
            Assert.assertEquals(urls[i], dlToTest.getUrl());
            Assert.assertEquals(localPaths[i], dlToTest.getLocalPath());
        }
    }
}

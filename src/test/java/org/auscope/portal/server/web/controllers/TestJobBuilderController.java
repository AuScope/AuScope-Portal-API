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
        final String computeVmId = "compute-vmi-id";
        final String instanceId = "new-instance-id";
        final Sequence jobFileSequence = context.sequence("jobFileSequence"); //this makes sure we aren't deleting directories before uploading (and other nonsense)
        final OutputStream mockOutputStream = context.mock(OutputStream.class);
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final String storageBucket = "storage-bucket";
        final String storageAccess = "213-asd-54";
        final String storageSecret = "tops3cret";
        final String storageProvider = "provider";

        sessionVariables.put("user-roles", new String[] {"testRole1", "testRole2"});

        jobObj.setComputeVmId(computeVmId);
        jobObj.setStatus(jobInSavedState); // by default, the job is in SAVED state
        jobObj.setStorageBaseKey("base/key");

        context.checking(new Expectations() {{
            //We should have access control check to ensure user has permission to run the job
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("user-roles");will(returnValue(sessionVariables.get("user-roles")));
            oneOf(mockImageService).getImagesByRoles((String[])sessionVariables.get("user-roles"));will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));

            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            oneOf(mockFileStagingService).writeFile(jobObj, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(mockOutputStream));
            allowing(mockOutputStream).close();

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));
            inSequence(jobFileSequence);

            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));

            allowing(mockCloudStorageService).getBucket();will(returnValue(storageBucket));
            allowing(mockCloudStorageService).getAccessKey();will(returnValue(storageAccess));
            allowing(mockCloudStorageService).getSecretKey();will(returnValue(storageSecret));
            allowing(mockCloudStorageService).getProvider();will(returnValue(storageProvider));

            //We should have 1 call to upload them
            oneOf(mockCloudStorageService).uploadJobFiles(with(equal(jobObj)), with(equal(new File[] {mockFile1, mockFile2})));
            inSequence(jobFileSequence);

            //And finally 1 call to execute the job
            oneOf(mockCloudComputeService).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(returnValue(instanceId));

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, jobObj, "Job submitted.");
        }});


        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(instanceId, jobObj.getComputeInstanceId());
        Assert.assertEquals(JobBuilderController.STATUS_PENDING, jobObj.getStatus());
        Assert.assertNotNull(jobObj.getSubmitDate());
    }

    /**
     * Tests that job submission fails correctly when user doesn't have permission to use
     * the VMI.
     */
    @Test
    public void testJobSubmission_PermissionDenied() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(new Integer(13));
        final String injectedComputeVmId = "injected-compute-vmi-id";
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        final String errorDescription = "You do not have the permission to submit this job for processing.";

        sessionVariables.put("user-roles", new String[] {"testRole1", "testRole2"});
        jobObj.setComputeVmId(injectedComputeVmId);
        jobObj.setStatus(jobInSavedState); // by default, the job is in SAVED state

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("user-roles");will(returnValue(sessionVariables.get("user-roles")));
            oneOf(mockImageService).getImagesByRoles((String[])sessionVariables.get("user-roles"));will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, jobObj, errorDescription);
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_UNSUBMITTED, jobObj.getStatus());
    }

    /**
     * Tests that job submission fails correctly when the job doesn't exist
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
        final String computeVmId = "compute-vmi-id";
        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(jobObj, "mockFile1", mockFile1), new StagedFile(jobObj, "mockFile2", mockFile2)};
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();

        jobObj.setComputeVmId(computeVmId);
        jobObj.setStatus(jobInSavedState); // by default, the job is in SAVED state

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("user-roles");will(returnValue(sessionVariables.get("user-roles")));
            oneOf(mockImageService).getImagesByRoles((String[])sessionVariables.get("user-roles"));will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));

            oneOf(mockFileStagingService).writeFile(jobObj, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(bos));

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //And one call to upload them (which we will mock as failing)
            oneOf(mockCloudStorageService).uploadJobFiles(with(equal(jobObj)), with(any(File[].class)));will(throwException(new PortalServiceException("")));

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, jobObj, "");
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_UNSUBMITTED, jobObj.getStatus());
    }

    /**
     * Tests that job submission fails correctly when files cannot be uploaded to S3
     * @throws Exception
     */
    @Test
    public void testJobSubmission_ExecuteFailure() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(13);
        final String jobInSavedState = JobBuilderController.STATUS_UNSUBMITTED;
        //As submitJob method no longer explicitly checks for empty storage credentials,
        //we need to manually set the storageBaseKey property to avoid NullPointerException
        jobObj.setStorageBaseKey("storageBaseKey");
        //By default, a job is in SAVED state
        jobObj.setStatus(jobInSavedState);
        final String computeVmId = "compute-vmi-id";
        jobObj.setComputeVmId(computeVmId);
        final File mockFile1 = context.mock(File.class, "MockFile1");
        final File mockFile2 = context.mock(File.class, "MockFile2");
        final StagedFile[] stageInFiles = new StagedFile[] {new StagedFile(jobObj, "mockFile1", mockFile1), new StagedFile(jobObj, "mockFile2", mockFile2)};
        final OutputStream mockOutputStream = context.mock(OutputStream.class);
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        final VglMachineImage[] mockImages = new VglMachineImage[] {context.mock(VglMachineImage.class)};
        final String storageBucket = "storage-bucket";
        final String storageAccess = "213-asd-54";
        final String storageSecret = "tops3cret";
        final String storageProvider = "provider";
        sessionVariables.put("user-roles", new String[] {"testRole1", "testRole2"});

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));

            oneOf(mockFileStagingService).writeFile(jobObj, JobBuilderController.DOWNLOAD_SCRIPT);
            will(returnValue(mockOutputStream));
            allowing(mockOutputStream).close();

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));

            allowing(mockCloudStorageService).getBucket();will(returnValue(storageBucket));
            allowing(mockCloudStorageService).getAccessKey();will(returnValue(storageAccess));
            allowing(mockCloudStorageService).getSecretKey();will(returnValue(storageSecret));
            allowing(mockCloudStorageService).getProvider();will(returnValue(storageProvider));

            //We should have 1 call to upload them
            oneOf(mockCloudStorageService).uploadJobFiles(with(equal(jobObj)), with(any(File[].class)));

            //We should have access control check to ensure user has permission to run the job
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("user-roles");will(returnValue(sessionVariables.get("user-roles")));
            oneOf(mockImageService).getImagesByRoles((String[])sessionVariables.get("user-roles"));will(returnValue(mockImages));
            oneOf(mockImages[0]).getImageId();will(returnValue("compute-vmi-id"));

            //And finally 1 call to execute the job (which will throw PortalServiceException indicating failure)
            oneOf(mockCloudComputeService).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(throwException(new PortalServiceException("")));

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(jobInSavedState, jobObj, "");
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(JobBuilderController.STATUS_UNSUBMITTED, jobObj.getStatus());
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
        final String bucket = "stora124e-Bucket";
        final String access = "213-asd-54";
        final String secret = "tops3cret";
        final String provider = "provider";

        context.checking(new Expectations() {{
            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));
            allowing(mockCloudStorageService).getBucket();will(returnValue(bucket));
            allowing(mockCloudStorageService).getAccessKey();will(returnValue(access));
            allowing(mockCloudStorageService).getSecretKey();will(returnValue(secret));
            allowing(mockCloudStorageService).getProvider();will(returnValue(provider));
        }});

        job.setStorageBaseKey("test/key");

        String contents = controller.createBootstrapForJob(job);
        Assert.assertNotNull(contents);
        Assert.assertTrue("Bootstrap is empty!", contents.length() > 0);
        Assert.assertFalse("Boostrap needs Unix style line endings!", contents.contains("\r"));
        Assert.assertTrue(contents.contains(bucket));
        Assert.assertTrue(contents.contains(access));
        Assert.assertTrue(contents.contains(job.getStorageBaseKey()));
        Assert.assertTrue(contents.contains(secret));
        Assert.assertTrue(contents.contains(provider));
    }

    /**
     * Tests that listing job images for a user works as expected
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testListImages() throws Exception {
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        final VglMachineImage[] images = new VglMachineImage[] {context.mock(VglMachineImage.class)};

        sessionVariables.put("user-roles", new String[] {"testRole1", "testRole2"});

        context.checking(new Expectations() {{
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            oneOf(mockSession).getAttribute("user-roles");will(returnValue(sessionVariables.get("user-roles")));

            //We allow calls to the Configurer which simply extract values from our property file
            oneOf(mockImageService).getImagesByRoles((String[])sessionVariables.get("user-roles"));will(returnValue(images));
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
        final HashMap<String, Object> sessionVariables = new HashMap<String, Object>();
        sessionVariables.put("user-roles", new String[] {"testRole1", "testRole2"});

        context.checking(new Expectations() {{
            //We should have a call to servlet request object to get user session
            oneOf(mockRequest).getSession();will(returnValue(mockSession));
            //We should have a call to get user roles from user session object
            oneOf(mockSession).getAttribute("user-roles");will(returnValue(sessionVariables.get("user-roles")));

            //We allow calls to the Configurer which simply extract values from our property file
            oneOf(mockImageService).getImagesByRoles((String[])sessionVariables.get("user-roles"));will(throwException(new PortalServiceException("error")));
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
        final String storageServiceId = "storage-service";
        final String computeServiceId = "compute-service";

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
            allowing(mockSession).getAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST);will(returnValue(null));
            allowing(mockSession).setAttribute(JobDownloadController.SESSION_DOWNLOAD_LIST, null);

            allowing(mockCloudComputeService).getId();will(returnValue(computeServiceId));

            oneOf(mockCloudStorageService).generateBaseKey(with(any(VEGLJob.class)));will(returnValue(baseKey));
            allowing(mockCloudStorageService).getId();will(returnValue(storageServiceId));

            oneOf(mockHostConfigurer).resolvePlaceholder("storage.provider");will(returnValue(storageProvider));
            oneOf(mockHostConfigurer).resolvePlaceholder("storage.endpoint");will(returnValue(storageEndpoint));

            oneOf(mockJobManager).saveJob(with(any(VEGLJob.class)));inSequence(sequence); //one save job to get ID

            oneOf(mockFileStagingService).generateStageInDirectory(with(any(VEGLJob.class)));inSequence(sequence);

            oneOf(mockJobManager).saveJob(with(any(VEGLJob.class)));inSequence(sequence);

            //We should have 1 call to our job manager to create a job audit trail record
            oneOf(mockJobManager).createJobAuditTrail(with(any(String.class)), with(any(VEGLJob.class)), with(any(String.class)));
        }});

        ModelAndView mav = controller.createJobObject(mockRequest);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        List<VEGLJob> data = (List<VEGLJob>)mav.getModel().get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(1, data.size());

        VEGLJob newJob = data.get(0);
        Assert.assertNotNull(newJob);
        Assert.assertEquals(storageServiceId, newJob.getStorageServiceId());
        Assert.assertEquals(computeServiceId, newJob.getComputeServiceId());
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

    /**
     * Unit test for succesfull object conversion in getAllJobInputs
     * @throws Exception
     */
    @Test
    public void testGetAllJobInputs() throws Exception {
        final Integer jobId = 543231;
        final VEGLJob job = new VEGLJob(jobId);
        final VglDownload dl = new VglDownload(413);
        final File mockFile = context.mock(File.class);
        final long mockFileLength = 21314L;
        final StagedFile[] stagedFiles = new StagedFile[]{new StagedFile(job, "another/file.ext", mockFile)};

        dl.setDescription("desc");
        dl.setEastBoundLongitude(1.0);
        dl.setWestBoundLongitude(2.0);
        dl.setLocalPath("local/path/file.ext");
        dl.setName("myFile");
        dl.setNorthBoundLatitude(3.0);
        dl.setSouthBoundLatitude(4.0);
        dl.setParent(job);

        job.setJobDownloads(Arrays.asList(dl));

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId);will(returnValue(job));
            oneOf(mockFile).length();will(returnValue(mockFileLength));
            oneOf(mockFileStagingService).listStageInDirectoryFiles(job);will(returnValue(stagedFiles));
        }});

        ModelAndView mav = controller.getAllJobInputs(jobId);
        Assert.assertNotNull(mav);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));

        Assert.assertNotNull(mav.getModel().get("data"));
        @SuppressWarnings("unchecked")
        List<VglDownload> fileInfo = (List<VglDownload>) mav.getModel().get("data");

        Assert.assertEquals(2, fileInfo.size());

        Assert.assertEquals(stagedFiles[0].getName(), fileInfo.get(0).getLocalPath());
        Assert.assertEquals(dl.getLocalPath(), fileInfo.get(1).getLocalPath());
    }
}
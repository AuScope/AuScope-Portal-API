package org.auscope.portal.server.web.controllers;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.jmock.Expectations;
import org.jmock.Mockery;
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
public class TestGridSubmitController {
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

    private GridSubmitController controller;

    @Before
    public void init() {
        mockJobManager = context.mock(VEGLJobManager.class);
        mockFileStagingService = context.mock(FileStagingService.class);
        mockHostConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);
        mockCloudStorageService = context.mock(CloudStorageService.class);
        mockCloudComputeService = context.mock(CloudComputeService.class);
        mockRequest = context.mock(HttpServletRequest.class);
        mockResponse = context.mock(HttpServletResponse.class);

        controller = new GridSubmitController(mockJobManager, mockFileStagingService, mockHostConfigurer, mockCloudStorageService, mockCloudComputeService);
    }

    /**
     * Tests that job submission correctly interacts with all dependencies
     * @throws Exception
     */
    @Test
    public void testJobSubmission() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(new Integer(13));
        final File[] stageInFiles = new File[] {context.mock(File.class, "MockFile1"), context.mock(File.class, "MockFile2")};
        final String instanceId = "new-instance-id";

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

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));

            //And one call to upload them
            oneOf(mockCloudStorageService).uploadJobFiles(jobObj, stageInFiles);

            //And finally 1 call to execute the job
            oneOf(mockCloudComputeService).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(returnValue(instanceId));
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
     * Tests that job submission correctly interacts with all dependencies
     * @throws Exception
     */
    @Test
    public void testJobSubmission_JobNoCredentials() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(13);

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(GridSubmitController.STATUS_FAILED, jobObj.getStatus());
    }

    /**
     * Tests that job submission fails correctly when files cannot be uploaded to S3
     * @throws Exception
     */
    @Test
    public void testJobSubmission_S3Failure() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(13);
        final File[] stageInFiles = new File[] {context.mock(File.class, "MockFile1"), context.mock(File.class, "MockFile2")};

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //And one call to upload them (which we will mock as failing)
            oneOf(mockCloudStorageService).uploadJobFiles(jobObj, stageInFiles);will(throwException(new PortalServiceException("")));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(GridSubmitController.STATUS_FAILED, jobObj.getStatus());
    }

    /**
     * Tests that job submission fails correctly when files cannot be uploaded to S3
     * @throws Exception
     */
    @Test
    public void testJobSubmission_ExecuteFailure() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(13);
        final File[] stageInFiles = new File[] {context.mock(File.class, "MockFile1"), context.mock(File.class, "MockFile2")};

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            //We should have 1 call to get our stage in files
            oneOf(mockFileStagingService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));

            //And one call to upload them
            oneOf(mockCloudStorageService).uploadJobFiles(jobObj, stageInFiles);

            //And finally 1 call to execute the job (which will return null indicating failure)
            oneOf(mockCloudComputeService).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(returnValue(null));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(GridSubmitController.STATUS_FAILED, jobObj.getStatus());
    }
}

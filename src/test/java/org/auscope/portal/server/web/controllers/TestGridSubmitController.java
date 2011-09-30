package org.auscope.portal.server.web.controllers;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.service.JobExecutionService;
import org.auscope.portal.server.web.service.JobFileService;
import org.auscope.portal.server.web.service.JobStorageService;
import org.jets3t.service.S3ServiceException;
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
    private JobFileService mockJobFileService;
    private JobStorageService mockJobStorageService;
    private PortalPropertyPlaceholderConfigurer mockHostConfigurer;
    private JobExecutionService mockJobExecutionService;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;

    private GridSubmitController controller;

    @Before
    public void init() {
        mockJobManager = context.mock(VEGLJobManager.class);
        mockJobFileService = context.mock(JobFileService.class);
        mockHostConfigurer = context.mock(PortalPropertyPlaceholderConfigurer.class);
        mockJobStorageService = context.mock(JobStorageService.class);
        mockJobExecutionService = context.mock(JobExecutionService.class);
        mockRequest = context.mock(HttpServletRequest.class);
        mockResponse = context.mock(HttpServletResponse.class);

        controller = new GridSubmitController(mockJobManager, mockJobFileService, mockHostConfigurer, mockJobStorageService, mockJobExecutionService);
    }

    /**
     * Tests that job submission correctly interacts with all dependencies
     * @throws Exception
     */
    @Test
    public void testJobSubmission() throws Exception {
        //Instantiate our job object
        final VEGLJob jobObj = new VEGLJob(new Integer(13), "jobName", "jobDesc", "user",
                "user@email.com", null, null, "ec2InstanceId",
                "http://ec2.endpoint", "ec2Ami", "s3AccessKey", "s3SecretKey",
                "s3Bucket", "s3BaseKey", null, new Integer(45),
                "file-storage-id", "vm-subset-filepath");
        final File[] stageInFiles = new File[] {context.mock(File.class, "MockFile1"), context.mock(File.class, "MockFile2")};
        final String instanceId = "new-instance-id";

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            //We should have 1 call to get our stage in files
            oneOf(mockJobFileService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));

            //And one call to upload them
            oneOf(mockJobStorageService).uploadInputJobFiles(jobObj, stageInFiles);

            //And finally 1 call to execute the job
            oneOf(mockJobExecutionService).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(returnValue(instanceId));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(instanceId, jobObj.getEc2InstanceId());
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
        final VEGLJob jobObj = new VEGLJob(new Integer(13), "jobName", "jobDesc", "user",
                "user@email.com", null, null, "ec2InstanceId",
                "http://ec2.endpoint", "ec2Ami", "", "",
                "s3Bucket", "s3BaseKey", null, new Integer(45),
                "file-storage-id", "vm-subset-filepath");

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
        final VEGLJob jobObj = new VEGLJob(new Integer(13), "jobName", "jobDesc", "user",
                "user@email.com", null, null, "ec2InstanceId",
                "http://ec2.endpoint", "ec2Ami", "s3AccessKey", "s3SecretKey",
                "s3Bucket", "s3BaseKey", null, new Integer(45),
                "file-storage-id", "vm-subset-filepath");
        final File[] stageInFiles = new File[] {context.mock(File.class, "MockFile1"), context.mock(File.class, "MockFile2")};

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            //We should have 1 call to get our stage in files
            oneOf(mockJobFileService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //And one call to upload them (which we will mock as failing)
            oneOf(mockJobStorageService).uploadInputJobFiles(jobObj, stageInFiles);will(throwException(new S3ServiceException()));
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
        final VEGLJob jobObj = new VEGLJob(new Integer(13), "jobName", "jobDesc", "user",
                "user@email.com", null, null, "ec2InstanceId",
                "http://ec2.endpoint", "ec2Ami", "s3AccessKey", "s3SecretKey",
                "s3Bucket", "s3BaseKey", null, new Integer(45),
                "file-storage-id", "vm-subset-filepath");
        final File[] stageInFiles = new File[] {context.mock(File.class, "MockFile1"), context.mock(File.class, "MockFile2")};

        context.checking(new Expectations() {{
            //We should have 1 call to our job manager to get our job object and 1 call to save it
            oneOf(mockJobManager).getJobById(jobObj.getId());will(returnValue(jobObj));
            oneOf(mockJobManager).saveJob(jobObj);

            //We should have 1 call to get our stage in files
            oneOf(mockJobFileService).listStageInDirectoryFiles(jobObj);will(returnValue(stageInFiles));

            //We allow calls to the Configurer which simply extract values from our property file
            allowing(mockHostConfigurer).resolvePlaceholder(with(any(String.class)));

            //And one call to upload them
            oneOf(mockJobStorageService).uploadInputJobFiles(jobObj, stageInFiles);

            //And finally 1 call to execute the job (which will return null indicating failure)
            oneOf(mockJobExecutionService).executeJob(with(any(VEGLJob.class)), with(any(String.class)));will(returnValue(null));
        }});

        ModelAndView mav = controller.submitJob(mockRequest, mockResponse, jobObj.getId().toString());

        Assert.assertFalse((Boolean)mav.getModel().get("success"));
        Assert.assertEquals(GridSubmitController.STATUS_FAILED, jobObj.getStatus());
    }
}

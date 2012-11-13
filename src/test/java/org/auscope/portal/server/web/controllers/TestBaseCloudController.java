package org.auscope.portal.server.web.controllers;

import junit.framework.Assert;

import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VEGLJob;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

public class TestBaseCloudController extends PortalTestClass {
    /**
     * Dummy Implementation to test abstract class
     */
    private class TestableBaseCloudController extends BaseCloudController {
        public TestableBaseCloudController(
                CloudStorageService[] cloudStorageServices,
                CloudComputeService[] cloudComputeServices) {
            super(cloudStorageServices, cloudComputeServices);
        }
    }

    CloudStorageService[] mockStorageServices = new CloudStorageService[] {context.mock(CloudStorageService.class, "css1"),
                                                                            context.mock(CloudStorageService.class, "css2"),
                                                                            context.mock(CloudStorageService.class, "css3")};

    CloudComputeService[] mockComputeServices = new CloudComputeService[] {context.mock(CloudComputeService.class, "ccs1"),
                                                                            context.mock(CloudComputeService.class, "ccs2"),
                                                                            context.mock(CloudComputeService.class, "ccs3")};

    /**
     * Configure mock services
     */
    @Before
    public void setup() {
        context.checking(new Expectations() {{
            allowing(mockStorageServices[0]).getId();will(returnValue("oneId-s"));
            allowing(mockStorageServices[1]).getId();will(returnValue("anotherId-s"));
            allowing(mockStorageServices[2]).getId();will(returnValue("yetAnotherId-s"));

            allowing(mockComputeServices[0]).getId();will(returnValue("oneId-c"));
            allowing(mockComputeServices[1]).getId();will(returnValue("anotherId-c"));
            allowing(mockComputeServices[2]).getId();will(returnValue("yetAnotherId-c"));
        }});
    }

    /**
     * Tests getting a storage service works with a string
     * @throws Exception
     */
    @Test
    public void testGetStorageService() throws Exception {
        TestableBaseCloudController controller = new TestableBaseCloudController(mockStorageServices, mockComputeServices);

        String existingId = "anotherId-s";
        String nonExistingId = "DNE";
        String nullId = null;

        CloudStorageService result = controller.getStorageService(existingId);
        Assert.assertNotNull(result);
        Assert.assertEquals(existingId, result.getId());

        Assert.assertNull(controller.getStorageService(nonExistingId));
        Assert.assertNull(controller.getStorageService(nullId));
    }

    /**
     * Tests getting a storage service works with a job
     * @throws Exception
     */
    @Test
    public void testGetStorageService_Job() throws Exception {
        TestableBaseCloudController controller = new TestableBaseCloudController(mockStorageServices, mockComputeServices);

        String existingId = "anotherId-s";
        String nonExistingId = "DNE";
        String nullId = null;

        VEGLJob job = new VEGLJob(123);

        job.setStorageServiceId(existingId);
        CloudStorageService result = controller.getStorageService(job);
        Assert.assertNotNull(result);
        Assert.assertEquals(existingId, result.getId());

        job.setStorageServiceId(nonExistingId);
        Assert.assertNull(controller.getStorageService(job));
        job.setStorageServiceId(nullId);
        Assert.assertNull(controller.getStorageService(job));
    }

    /**
     * Tests getting a Compute service works with a string
     * @throws Exception
     */
    @Test
    public void testGetComputeService() throws Exception {
        TestableBaseCloudController controller = new TestableBaseCloudController(mockStorageServices, mockComputeServices);

        String existingId = "anotherId-c";
        String nonExistingId = "DNE";
        String nullId = null;

        CloudComputeService result = controller.getComputeService(existingId);
        Assert.assertNotNull(result);
        Assert.assertEquals(existingId, result.getId());

        Assert.assertNull(controller.getComputeService(nonExistingId));
        Assert.assertNull(controller.getStorageService(nullId));
    }

    /**
     * Tests getting a storage service works with a job
     * @throws Exception
     */
    @Test
    public void testGetComputeService_Job() throws Exception {
        TestableBaseCloudController controller = new TestableBaseCloudController(mockStorageServices, mockComputeServices);

        String existingId = "anotherId-c";
        String nonExistingId = "DNE";
        String nullId = null;

        VEGLJob job = new VEGLJob(123);

        job.setComputeServiceId(existingId);
        CloudComputeService result = controller.getComputeService(job);
        Assert.assertNotNull(result);
        Assert.assertEquals(existingId, result.getId());

        job.setComputeServiceId(nonExistingId);
        Assert.assertNull(controller.getComputeService(job));
        job.setComputeServiceId(nullId);
        Assert.assertNull(controller.getComputeService(job));
    }
}

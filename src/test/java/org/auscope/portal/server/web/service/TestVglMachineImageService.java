package org.auscope.portal.server.web.service;

import java.util.ArrayList;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VglMachineImage;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

/**
 * Unit tests for for VglMachineImageService
 * @author Josh Vote
 *
 */
public class TestVglMachineImageService extends PortalTestClass {

    private VglMachineImage[] allImages;
    private VglMachineImage testImage1, testImage2, testImage3;
    private VglMachineImageService serviceUnderTest;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Before
    public void setup() {
        //Setup VGL machine test images and their respective permissions
        testImage1 = new VglMachineImage("testImage1");
        testImage2 = new VglMachineImage("testImage2");
        testImage3 = new VglMachineImage("testImage3");

        allImages = new VglMachineImage[] {testImage1, testImage2, testImage3};

        ArrayList<VglMachineImage> vmiList = new ArrayList<VglMachineImage>();
        for (VglMachineImage img : allImages) {
            vmiList.add(img);
        }

        serviceUnderTest = new VglMachineImageService(vmiList);
    }

    /**
     * Utility for comparing 2 images based on image id
     * @param im1
     * @param im2
     * @return
     */
    private boolean machineImageEquals(VglMachineImage im1, VglMachineImage im2) {
        if (im1 == im2) {
            return true;
        }

        if (im1 == null || im2 == null) {
            return false;
        }

        return im1.getImageId().equals(im2.getImageId());
    }

    /**
     * Tests that the 'AllImages' function returns every image injected into it
     * @throws Exception
     */
    @Test
    public void testAllImages() throws Exception {
        VglMachineImage[] fetchedImages = serviceUnderTest.getAllImages();

        Assert.assertNotNull(fetchedImages);
        Assert.assertEquals(allImages.length, fetchedImages.length);

        //Ensure that the result is an unordered set of the images we injected
        for (int i = 0; i < fetchedImages.length; i++) {
            boolean found = false;
            for (int j = 0; !found && j < allImages.length; j++) {
                found = machineImageEquals(fetchedImages[i], allImages[j]);
            }

            Assert.assertTrue(found);
        }
    }

    /**
     * Test to ensure correct images get returned in accordance with
     * @throws Exception
     */
    @Test
    public void testImagesByRoles() throws Exception {
        //Setup test user roles data
        String[] testUserRoles0 = new String[] {};
        String[] testUserRoles1 = new String[] { "ROLE_USER" };
        String[] testUserRoles2 = new String[] { "ROLE_UBC" };
        String[] testUserRoles3 = new String[] { "ROLE_USER", "ROLE_UBC" };
        //Setup test images permissions
        testImage1.setPermissions(new String[] {"ROLE_UBC", "ROLE_ADMINISTRATOR"});
        testImage2.setPermissions(new String[] {"ROLE_USER"});
        testImage3.setPermissions(new String[] {"ROLE_USER"});

        VglMachineImage[] fetchedImages = null;
        VglMachineImage[] expectedImages = null;

        //No image should be fetched if the user doesn't have any role
        fetchedImages = serviceUnderTest.getImagesByRoles(testUserRoles0);
        expectedImages = new VglMachineImage[] {};
        Assert.assertArrayEquals(expectedImages, fetchedImages);

        //User that doesn't have the ROLE_UBC role shouldn't be seeing testImage1
        fetchedImages = serviceUnderTest.getImagesByRoles(testUserRoles1);
        expectedImages = new VglMachineImage[] { testImage2, testImage3 };
        Assert.assertArrayEquals(expectedImages, fetchedImages);

        fetchedImages = serviceUnderTest.getImagesByRoles(testUserRoles2);
        expectedImages = new VglMachineImage[] { testImage1 };
        Assert.assertArrayEquals(expectedImages, fetchedImages);

        //All images should be fetched if a user has
        fetchedImages = serviceUnderTest.getImagesByRoles(testUserRoles3);
        Assert.assertArrayEquals(allImages, fetchedImages);

        //Image that doesn't have any permissions set shouldn't be fetched
        testImage3.setPermissions(new String[] {}); // reset testImage3 permissions
        fetchedImages = serviceUnderTest.getImagesByRoles(testUserRoles1);
        expectedImages = new VglMachineImage[] { testImage2 };
        Assert.assertArrayEquals(expectedImages, fetchedImages);
    }

}
package org.auscope.portal.server.web.service;

import java.util.ArrayList;

import junit.framework.Assert;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VglMachineImage;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for for VglMachineImageService
 * @author Josh Vote
 *
 */
public class TestVglMachineImageService extends PortalTestClass {

    private VglMachineImage[] images;
    private VglMachineImageService service;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Before
    public void setup() {
        images = new VglMachineImage[] {};

        //This is a usecase within spring framework - can't use generics
        ArrayList untypedList = new ArrayList();
        for (VglMachineImage im : images) {
            untypedList.add(im);
        }

        service = new VglMachineImageService(untypedList);
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
        VglMachineImage[] fetchedImages = service.getAllImages();

        Assert.assertNotNull(fetchedImages);
        Assert.assertEquals(images.length, fetchedImages.length);

        //Ensure that the result is an unordered set of the images we injected
        for (int i = 0; i < fetchedImages.length; i++) {
            boolean found = false;
            for (int j = 0; !found && j < images.length; j++) {
                found = machineImageEquals(fetchedImages[i], images[j]);
            }

            Assert.assertTrue(found);
        }
    }

}

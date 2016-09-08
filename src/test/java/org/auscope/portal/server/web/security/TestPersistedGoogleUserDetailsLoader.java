package org.auscope.portal.server.web.security;

import java.util.HashSet;

import org.auscope.portal.core.test.PortalTestClass;
import org.junit.Assert;
import org.junit.Test;

public class TestPersistedGoogleUserDetailsLoader extends PortalTestClass {

    private PersistedGoogleUserDetailsLoader loader = new PersistedGoogleUserDetailsLoader("TEST_DEFAULT");

    /**
     * Throwaway test to ensure that we get slightly random results (i.e. - noone completely stuffed the implementation) that are valid from Amazon's point of view.
     */
    @Test
    public void testValidBucketNameGeneration(){
        final int ITERATION_COUNT = 10000;

        HashSet<String> previousNames = new HashSet<>(ITERATION_COUNT);
        for (int i = 0; i < ITERATION_COUNT; i++) {
            String bucketName = loader.generateRandomBucketName();

            Assert.assertNotNull(bucketName);
            Assert.assertTrue("Bucket name too long", bucketName.length() < 64);
            Assert.assertTrue("Bucket name too short", bucketName.length() >= 6);
            Assert.assertFalse(previousNames.contains(bucketName));
            Assert.assertTrue(bucketName.matches("[a-z0-9\\-]*"));

            previousNames.add(bucketName);
        }
    }
}

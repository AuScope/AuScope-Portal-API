package org.auscope.portal.server.web.controllers;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.web.service.VGLCryptoService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MenuController
 *
 */
public class TestEncryptionService extends PortalTestClass {
    private VGLCryptoService uc = null;
    final String PASSWORD = "testPassword1234";
    
    @Before
    public void setup() throws PortalServiceException {
        uc=  new VGLCryptoService(PASSWORD);
    }

    /**
     * Tests the existence of certain critical API keys + the correct view name being extracted
     * @throws Exception
     */
    @Test
    public void testPasswordEncryption() throws Exception {
        final String secret = "this is a secret for testing.";
        
        byte[] enc = uc.encrypt(secret);

        String dec = uc.decrypt(enc);
        
        Assert.assertEquals(secret, dec);
    }
}

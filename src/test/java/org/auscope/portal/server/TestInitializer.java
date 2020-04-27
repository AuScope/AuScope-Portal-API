package org.auscope.portal.server;

import java.io.File;
import java.io.IOException;

import org.junit.Assert; 

import org.junit.Test;

public class TestInitializer {

    @Test
    public void testCleanUpTempDirectory() throws IOException {

        String tempDir = System.getProperty("java.io.tmpdir");
        File tempDirFile = new File(tempDir);
        File f = File.createTempFile("APT_", ".xml");
        Assert.assertTrue(f.getParentFile().getName().equals(tempDirFile.getName()));

        Initializer init = new Initializer();
        init.run();
        boolean fileFound = false;

        Assert.assertTrue(f.getParentFile().isDirectory());
        File[] files = f.getParentFile().listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().startsWith("APT_")) {
                fileFound = true;
                break;
            }
        }

        Assert.assertFalse(fileFound);

    }
}

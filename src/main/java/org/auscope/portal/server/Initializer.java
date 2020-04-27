package org.auscope.portal.server;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.exceptions.InitializationException;
import org.auscope.portal.core.util.FileIOUtil;

public class Initializer {
    private final Log log = LogFactory.getLog(getClass());

    public Initializer() {

    }

    public void run() {
        this.cleanUpTempDirectory();

    }

    private void cleanUpTempDirectory() {
        try {
            // Get the temporary directory and print it.
            String tempDir = System.getProperty("java.io.tmpdir");
            File dir = new File(tempDir);
            if (!dir.isDirectory()) {
                throw new InitializationException(
                        "Error in cleaning up temp files");
            } else {
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().startsWith("APT_")) {
                        FileIOUtil.deleteFilesRecursive(files[i]);
                    }
                }

            }
        } catch (Exception e) {
            log.error(e);
        }

    }

}

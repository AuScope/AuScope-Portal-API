package org.auscope.portal.server.web.service.scm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScmLoaderFactory {
    private static ScmLoader instance;
    private static final Log logger = LogFactory.getLog(ScmLoaderFactory.class);

    public static ScmLoader getInstance() {
        return instance;
    }

    public static void registerLoader(ScmLoader instance) {
        if (ScmLoaderFactory.instance != null) {
            logger.warn("Registered multiple instances of ScmLoader with factory.");
        }
        ScmLoaderFactory.instance = instance;
    }
}

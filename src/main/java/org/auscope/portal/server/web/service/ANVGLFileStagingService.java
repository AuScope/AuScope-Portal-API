package org.auscope.portal.server.web.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.StagingInformation;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.vegl.VEGLJob;

import java.io.File;

/**
 * Created by wis056 on 3/10/2014.
 * 
 * XXX Check needed
 * 
 */
public class ANVGLFileStagingService extends FileStagingService {
    private final Log logger = LogFactory.getLog(getClass());

    public ANVGLFileStagingService(StagingInformation stagingInformation) {
        super(stagingInformation);
    }

    public File createLocalFile(String fileName, VEGLJob job) {
        String directory = FileStagingService.pathConcat(stagingInformation.getStageInDirectory(), FileStagingService.getBaseFolderForJob(job));
        String destinationPath = pathConcat(directory, fileName);
        File file = new File(destinationPath);
        return file;
    }
}

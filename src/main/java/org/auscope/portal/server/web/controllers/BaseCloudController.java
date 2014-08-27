package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.server.vegl.VEGLJob;

/**
 * Methods and variables common to any controller wishing to access
 * the cloud
 *
 * @author Josh Vote
 *
 */
public abstract class BaseCloudController extends BasePortalController {
    /** All cloud storage services that are available to this controller */
    protected CloudStorageService[] cloudStorageServices;
    /** All cloud compute services that are available to this controller */
    protected CloudComputeService[] cloudComputeServices;

    protected PortalPropertyPlaceholderConfigurer hostConfigurer;

    /**
     * @param cloudStorageServices All cloud storage services that are available to this controller
     * @param cloudComputeServices All cloud compute services that are available to this controller
     */
    public BaseCloudController(CloudStorageService[] cloudStorageServices, CloudComputeService[] cloudComputeServices) {
        this(cloudStorageServices,cloudComputeServices,null);
    }


    public BaseCloudController(CloudStorageService[] cloudStorageServices, CloudComputeService[] cloudComputeServices,PortalPropertyPlaceholderConfigurer hostConfigurer) {
        super();
        this.cloudComputeServices = cloudComputeServices;
        this.cloudStorageServices = cloudStorageServices;
        this.hostConfigurer=hostConfigurer;
    }

    /**
     * Lookup a cloud storage service by ID. Returns null if the service DNE
     * @param id The name of the service to lookup
     * @return
     */
    protected CloudStorageService getStorageService(String id) {
        for (CloudStorageService s : cloudStorageServices) {
            if (s.getId().equals(id)) {
                return s;
            }
        }

        log.warn(String.format("CloudStorageService with ID '%1$s' doesn't exist", id));
        return null;
    }

    /**
     * Lookup a cloud storage service by ID (as configured in job). Returns null if the service DNE
     * @param id The job whose storage service id will be used in the lookup
     * @return
     */
    protected CloudStorageService getStorageService(VEGLJob job) {
        return getStorageService(job.getStorageServiceId());
    }

    /**
     * Lookup a cloud compute service by ID. Returns null if the service DNE
     * @param id The name of the service to lookup
     * @return
     */
    protected CloudComputeService getComputeService(String id) {
        for (CloudComputeService s : cloudComputeServices) {
            if (s.getId().equals(id)) {
                return s;
            }
        }

        log.warn(String.format("CloudComputeService with ID '%1$s' doesn't exist", id));
        return null;
    }

    /**
     * Lookup a cloud compute service by ID (as configured in job). Returns null if the service DNE
     * @param id The job whose compute service id will be used in the lookup
     * @return
     */
    protected CloudComputeService getComputeService(VEGLJob job) {
        return getComputeService(job.getComputeServiceId());
    }

    /**
     * Loads the bootstrap shell script template as a string.
     * @return
     * @throws IOException
     */
    private String getBootstrapTemplate() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("vl-bootstrap.sh");
        String template = IOUtils.toString(is);
        return template.replaceAll("\r", ""); //Windows style file endings have a tendency to sneak in via StringWriter and the like
    }



    /**
     * Creates a bootstrap shellscript for job that will be sent to
     * cloud VM instance to kick start the work for job.
     * @param job
     * @return
     * @throws IOException
     */
    public String createBootstrapForJob(VEGLJob job) throws IOException {
        String bootstrapTemplate = getBootstrapTemplate();
        CloudStorageService cloudStorageService = getStorageService(job);

        Object[] arguments = new Object[] {
            cloudStorageService.getBucket(), //STORAGE_BUCKET
            job.getStorageBaseKey().replace("//", "/"), //STORAGE_BASE_KEY_PATH
            cloudStorageService.getAccessKey(), //STORAGE_ACCESS_KEY
            cloudStorageService.getSecretKey(), //STORAGE_SECRET_KEY
            hostConfigurer.resolvePlaceholder("vm.sh"), //WORKFLOW_URL
            cloudStorageService.getEndpoint(), //STORAGE_ENDPOINT
            cloudStorageService.getProvider(), //STORAGE_TYPE
            cloudStorageService.getAuthVersion() == null ? "" : cloudStorageService.getAuthVersion(), //STORAGE_AUTH_VERSION
            cloudStorageService.getRegionName() == null ? "" : cloudStorageService.getRegionName() //OS_REGION_NAME
        };

        String result = MessageFormat.format(bootstrapTemplate, arguments);
        return result;
    }



}

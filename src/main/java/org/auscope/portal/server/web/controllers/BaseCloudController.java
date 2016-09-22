package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.util.TextUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.springframework.beans.factory.annotation.Value;

/**
 * Methods and variables common to any controller wishing to access
 * the cloud
 *
 * @author Josh Vote
 *
 */
public abstract class BaseCloudController extends BaseModelController {
    /** All cloud storage services that are available to this controller */
    protected CloudStorageService[] cloudStorageServices;
    /** All cloud compute services that are available to this controller */
    protected CloudComputeService[] cloudComputeServices;
    private String vmSh, vmShutdownSh;


    /**
     * @param cloudStorageServices All cloud storage services that are available to this controller
     * @param cloudComputeServices All cloud compute services that are available to this controller
     */
    public BaseCloudController(CloudStorageService[] cloudStorageServices, CloudComputeService[] cloudComputeServices, VEGLJobManager jobManager) {
        this(cloudStorageServices,cloudComputeServices, jobManager,null,null);
    }

    public BaseCloudController(CloudStorageService[] cloudStorageServices, CloudComputeService[] cloudComputeServices, VEGLJobManager jobManager,
            @Value("${vm.sh}") String vmSh, @Value("${vm-shutdown.sh}") String vmShutdownSh) {
        super(jobManager);
        this.cloudComputeServices = cloudComputeServices;
        this.cloudStorageServices = cloudStorageServices;
        this.vmSh=vmSh;
        this.vmShutdownSh=vmShutdownSh;
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
        try (InputStream is = this.getClass().getResourceAsStream("vl-bootstrap.sh")) {
            String template = IOUtils.toString(is);
            return template.replaceAll("\r", ""); // Windows style file endings
                                                  // have a tendency to sneak in
                                                  // via StringWriter and the
                                                  // like
        }
    }

    /**
     * Return the provisioning template as a string.
     *
     * @return String template
     * @throws IOException if fails to load template resource
     */
    private String getProvisioningTemplate() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("vl-provisioning.sh")) {
            String template = IOUtils.toString(is);
            return template.replaceAll("\r", ""); // Windows style file endings
                                                  // have a tendency to sneak in
                                                  // via StringWriter and the
                                                  // like
        }
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
        boolean useSts = ! TextUtil.isNullOrEmpty(job.getProperty(CloudJob.PROPERTY_STS_ARN));

        Object[] arguments = new Object[] {
                job.getStorageBucket(), // STORAGE_BUCKET
                job.getStorageBaseKey().replace("//", "/"), // STORAGE_BASE_KEY_PATH
                useSts ? "" : cloudStorageService.getAccessKey(), // STORAGE_ACCESS_KEY
                useSts ? "" : cloudStorageService.getSecretKey(), // STORAGE_SECRET_KEY
                vmSh, // WORKFLOW_URL
                cloudStorageService.getEndpoint(), // STORAGE_ENDPOINT
                cloudStorageService.getProvider(), // STORAGE_TYPE
                cloudStorageService.getAuthVersion() == null ? "" : cloudStorageService.getAuthVersion(), // STORAGE_AUTH_VERSION
                cloudStorageService.getRegionName() == null ? "" : cloudStorageService.getRegionName(), // OS_REGION_NAME
                getProvisioningTemplate(), // PROVISIONING_TEMPLATE
                vmShutdownSh, // WORKFLOW_URL
                job.isWalltimeSet() ? job.getWalltime() : 0 // WALLTIME
        };

        String result = MessageFormat.format(bootstrapTemplate, arguments);
        return result;
    }
}

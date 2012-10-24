package org.auscope.portal.server.web.controllers;

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

    /**
     * @param cloudStorageServices All cloud storage services that are available to this controller
     * @param cloudComputeServices All cloud compute services that are available to this controller
     */
    public BaseCloudController(CloudStorageService[] cloudStorageServices, CloudComputeService[] cloudComputeServices) {
        super();
        this.cloudComputeServices = cloudComputeServices;
        this.cloudStorageServices = cloudStorageServices;
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
}

package org.auscope.portal.server.web.service;

import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.admin.AdminDiagnosticResponse;
import org.auscope.portal.core.services.admin.AdminService;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.server.vegl.VEGLJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Contains a number of administrative functions specific to the VGL Portal
 *
 * @author Josh Vote
 */
@Service
public class VglAdminService extends AdminService {

    /** The compute service for running images*/
    private CloudComputeService[] cloudComputeServices;
    /** Used for looking up some config (this service is dependent on some configuration options */
    private PortalPropertyPlaceholderConfigurer hostConfigurer;

    @Autowired
    public VglAdminService(HttpServiceCaller serviceCaller, CloudComputeService[] cloudComputeServices, PortalPropertyPlaceholderConfigurer hostConfigurer) {
        super(serviceCaller);
        this.cloudComputeServices = cloudComputeServices;
        this.hostConfigurer = hostConfigurer;
    }

    /**
     * Starts up a virtual machine and tests whether it gets going or not
     * @return
     */
    /*public AdminDiagnosticResponse runTestJob() {
        AdminDiagnosticResponse response = new AdminDiagnosticResponse();

        //Our strategy is to start a job to run an echo script
        String echoString = "randomString-" + Double.toHexString(Math.random());
        String scriptToRun = String.format("echo \"%1$s\"\n", echoString);

        response.addDetail(String.format("Starting a VM to run the script '%1$s'", scriptToRun));

        //We need a fake job object in order to work with the compute service
        VEGLJob fakeJob = new VEGLJob(42);
        fakeJob.setComputeInstanceType("m1.large");

        //Run the fake job
        try {
            String instanceId = cloudComputeService.executeJob(fakeJob, scriptToRun);
            fakeJob.setComputeInstanceId(instanceId);
            response.addDetail(String.format("VM started with ID '%1$s'", instanceId));
        } catch (Exception ex) {
            response.addError(String.format("Error starting job: %1$s", ex));
            return response;
        }

        //Wait for it to start

        return response;
    }*/
}

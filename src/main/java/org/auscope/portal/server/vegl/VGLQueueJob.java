package org.auscope.portal.server.vegl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.util.structure.Job;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler;

public class VGLQueueJob implements Job {

    VEGLJobManager jobManager;
    CloudComputeService cloudComputeService;
    VEGLJob curJob;
    String userDataString;
    private final Log logger = LogFactory.getLog(getClass());
    VGLJobStatusChangeHandler vglJobStatusChangeHandler;



    public VGLQueueJob(VEGLJobManager jobManager,
            CloudComputeService cloudComputeService, VEGLJob curJob,
            String userDataString, VGLJobStatusChangeHandler vglJobStatusChangeHandler) {
        this.jobManager = jobManager;
        this.cloudComputeService = cloudComputeService;
        this.curJob = curJob;
        this.userDataString = userDataString;
        this.vglJobStatusChangeHandler=vglJobStatusChangeHandler;

    }

    public void updateErrorStatus(Exception e){
        String oldStatus=curJob.getStatus();
        this.curJob.setStatus(JobBuilderController.STATUS_ERROR);
        jobManager.saveJob(curJob);
        jobManager.createJobAuditTrail(oldStatus, curJob, e);
        vglJobStatusChangeHandler.handleStatusChange(curJob,curJob.getStatus(),oldStatus);

    }

    @Override
    public boolean run() throws PortalServiceException {
        String instanceId;
        try {
            instanceId = cloudComputeService.executeJob(curJob, userDataString);
            logger.info("Launched instance: " + instanceId);
            // set reference as instanceId for use when killing a job
            curJob.setComputeInstanceId(instanceId);
            curJob.setStatus(JobBuilderController.STATUS_PENDING);
            jobManager.createJobAuditTrail(JobBuilderController.STATUS_INQUEUE, curJob, "Job submitted.");
            curJob.setSubmitDate(new Date());
            jobManager.saveJob(curJob);
            return true;
        } catch (PortalServiceException e) {
            throw e;
        }
    }

    @Override
    public String toString(){
        return curJob.toString();
    }

    public VEGLJob getVEGLJob(){
        return curJob;
    }

    @Override
    public boolean equals(Object j){
        if(!(j instanceof VGLQueueJob)){
            return false;
        }else{
            VGLQueueJob job=(VGLQueueJob)j;
            if(this.curJob.getId()==job.getVEGLJob().getId()){
                return true;
            }else{
                return false;
            }
        }
    }

}
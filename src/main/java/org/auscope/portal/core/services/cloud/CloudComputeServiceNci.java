package org.auscope.portal.core.services.cloud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.SshCloudConnector.ExecResult;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Service class wrapper for interacting with a remote cloud compute service
 * using CloudJob objects.
 *
 * @author Carsten Friedrich
 */
public class CloudComputeServiceNci extends CloudComputeService {

    /**
     * Any getStatus request on a job whose submission time is less than
     * STATUS_PENDING_SECONDS seconds away from the current time will be forced
     * to return a Pending status (ignoring any status checks)
     *
     * This is to avoid missing errors occurring when AWS hasn't fully caught up
     * to the new VM.
     */
    public static final long STATUS_PENDING_SECONDS = 30;

    @SuppressWarnings("unused")
    private final Log logger = LogFactory.getLog(getClass());
    private CloudStorageServiceNci storageService;
    private SshCloudConnector sshCloudConnector;

    /**
     * Creates a new instance with the specified credentials
     *
     * @param endpoint
     *            (URL) The location of the Compute (Nova) service
     * @param accessKey
     *            The Compute Access key (user name)
     * @param secretKey
     *            The Compute Secret key (password)
     * @param apiVersion
     *            The API version
     */
    public CloudComputeServiceNci(CloudStorageServiceNci storageService, String endpoint) {
        super(ProviderType.RAIJIN, endpoint, null);
        this.storageService=storageService;
        this.sshCloudConnector = new SshCloudConnector(endpoint);
    }

    
    @Override
    public String executeJob(CloudJob job, String userDataString) throws PortalServiceException {
        String workingDir = storageService.getJobDirectory(job);
        Session session= null;
        try {
            session = sshCloudConnector.getSession(job);
            ExecResult res = sshCloudConnector.executeCommand(session, "qsub staging.pbs", workingDir);
            if(res.getExitStatus()!=0) {
                throw new PortalServiceException("Could not start data staging: "+res.getErr());
            } 
            
            String stagingJobId = res.getOut().substring(0, res.getOut().indexOf(".")); 
            logger.debug("Staging job id: "+stagingJobId);
            res = sshCloudConnector.executeCommand(session, "qsub -W depend=afterok:"+stagingJobId+" job.pbs", workingDir);
            if(res.getExitStatus()==0) {
                return res.getOut();
            }
            
            throw new PortalServiceException("Error executing PBS job: "+res.getErr());
        } catch (JSchException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    /**
     * Makes a request that the VM started by job be terminated
     *
     * @param job
     *            The job whose execution should be terminated
     * @throws PortalServiceException
     */
    @Override
    public void terminateJob(CloudJob job) throws PortalServiceException {
        Session session= null;
        try {
            session = sshCloudConnector.getSession(job);
            ExecResult res = sshCloudConnector.executeCommand(session, "qdel "+job.getComputeInstanceId());
            if(res.getExitStatus()!=0) {
                throw new PortalServiceException("Could not delete job: "+res.getErr());
            }             
        } catch (JSchException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    /**
     * An array of compute types that are available through this compute service
     */
    @Override
    public ComputeType[] getAvailableComputeTypes(Integer minimumVCPUs, Integer minimumRamMB,
            Integer minimumRootDiskGB) {
        return new ComputeType[0];
    }

    /**
     * Will attempt to tail and return the last {@code numLines} from the given
     * servers console.
     *
     * @param job
     *            the job which has been executed by this service
     * @param numLines
     *            the number of console lines to return
     * @return console output as string or null
     * @return
     */
    @Override
    public String getConsoleLog(CloudJob job, int numLines) throws PortalServiceException {

        Session session= null;
        try {
            session = sshCloudConnector.getSession(job);
            ExecResult res = sshCloudConnector.executeCommand(session, "qcat "+job.getComputeInstanceId());
            if(res.getExitStatus()!=0) {
                if(res.getOut().contains("Job is not running")) 
                    return "";
                
                throw new PortalServiceException("Could not query job log: "+res.getErr());
            }             
            
            return res.getOut();
        } catch (JSchException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    /**
     * Attempts to lookup low level status information about this job's compute
     * instance from the remote cloud.
     *
     * Having no computeInstanceId set will result in an exception being thrown.
     *
     * @param job
     * @return
     * @throws PortalServiceException
     */
    @Override
    public InstanceStatus getJobStatus(CloudJob job) throws PortalServiceException {

        Session session= null;
        try {
            session = sshCloudConnector.getSession(job);
            ExecResult res = sshCloudConnector.executeCommand(session, "qstat -s "+job.getComputeInstanceId());
            if(res.getExitStatus()!=0) {
                if(res.getErr().contains("Job has finished")) {
                    return InstanceStatus.Missing;
                }
                throw new PortalServiceException("Could not query job status for job '"+job.getComputeInstanceId()+"': "+res.getErr());
            }             
            
            if(res.getOut().contains("Job has finished")) {
                return InstanceStatus.Missing;
            }

            return InstanceStatus.Running;
        } catch (JSchException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }
}

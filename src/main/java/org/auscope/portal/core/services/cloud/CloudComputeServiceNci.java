package org.auscope.portal.core.services.cloud;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.SshCloudConnector.ExecResult;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.web.security.NCIDetails;

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

    public static final String JOB_ID_FILE = ".jobid";

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

    /**
     * We cant rely on the submitted instance ID to be the actual running job ID as our jobs will
     * finish and spawn followup jobs on different queues. This method will go to the underlying storage
     * service and retrieve the ID of the latest running job (as reported by the jobs themselves)
     * @param job
     * @return
     * @throws PortalServiceException
     */
    private String getJobLastInstanceId(CloudJob job) throws PortalServiceException {
        try (InputStream is = storageService.getJobFile(job, JOB_ID_FILE)) {
            if (is == null) {
                return job.getComputeInstanceId();
            }
            return IOUtils.toString(is);
        } catch (IOException e) {
            throw new PortalServiceException("Unable to access job ID file for " + job.getId(), e);
        }
    }

    /**
     * Loads the bootstrap shell script template as a string.
     * @return
     * @throws IOException
     */
    private String getNamedResourceString(String name) throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream(name)) {
            String template = IOUtils.toString(is);
            return template.replaceAll("\r", ""); // Normalise to Unix style line endings
        }
    }

    /**
     * Takes a walltime of minutes an converts it to a PBS walltime string in the form of HH:MM:SS
     * @param minutes
     * @return
     */
    private String wallTimeToString(Integer minutes) {
        if (minutes == null || minutes == 0) {
            return "01:00:00"; //default to 1 hour
        }
        int seconds = minutes * 60;

        return String.format("%1$02d:%2$02d:%3$02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    /**
     * Takes an encoding of a compute type in the form of param1=value1&param2=value2 (URL parameter encoding)
     * and returns the value for the specified param (returns empty string if it DNE)
     * @param param
     * @param computeType
     * @return
     */
    private String extractParamFromComputeType(String param, String computeType) {
        String[] parts = computeType.split("&");

        for (String part : parts) {
            String[] kvp = part.split("=");

            if (kvp[0].equals(param)) {
                try {
                    return URLDecoder.decode(kvp[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error("Unable to decode compute type into UTF8", e);
                }
            }
        }

        return "";
    }

    /**
     * Creates our bootstrap jobs/files templated for the specified job in the specified job's storage location
     * @param job
     * @throws PortalServiceException
     * @throws IOException
     */
    private void initialiseWorkingDirectory(VEGLJob job) throws PortalServiceException, IOException {
        String runCommand = StringUtils.isEmpty(job.getComputeVmRunCommand()) ? "python" : job.getComputeVmRunCommand();
        String utilFileContents = getNamedResourceString("nci-util.sh");
        String wallTimeString = wallTimeToString(job.getWalltime());
        String downloadJobContents = MessageFormat.format(getNamedResourceString("nci-download.job.tpl"), new Object[] {
            job.getProperty(NCIDetails.PROPERTY_NCI_PROJECT),
            job.getId(),
            storageService.getWorkingJobDirectory(job),
            storageService.getOutputJobDirectory(job),
            wallTimeString
        });

        String runJobContents = MessageFormat.format(getNamedResourceString("nci-run.job.tpl"), new Object[] {
            job.getProperty(NCIDetails.PROPERTY_NCI_PROJECT),
            job.getId(),
            storageService.getWorkingJobDirectory(job),
            storageService.getOutputJobDirectory(job),
            wallTimeString,
            extractParamFromComputeType("ncpus", job.getComputeInstanceType()),
            extractParamFromComputeType("mem", job.getComputeInstanceType()),
            extractParamFromComputeType("jobfs", job.getComputeInstanceType()),
            "module load escript/5.0", //TODO: Extract these from the solution centre
            runCommand + " -n $VL_TOTAL_NODES -p $VL_CPUS_PER_NODE" //TODO: Extract these from the solution centre
        });

        //storageService.uploadJobFile(job, files);
        storageService.uploadJobFile(job, "nci-util.sh", new ByteArrayInputStream(utilFileContents.getBytes(StandardCharsets.UTF_8)));
        storageService.uploadJobFile(job, "nci-download.job", new ByteArrayInputStream(downloadJobContents.getBytes(StandardCharsets.UTF_8)));
        storageService.uploadJobFile(job, "nci-run.job", new ByteArrayInputStream(runJobContents.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public String executeJob(CloudJob job, String userDataString) throws PortalServiceException {
        String workingDir = storageService.getOutputJobDirectory(job);
        Session session= null;

        if (!(job instanceof VEGLJob)) {
            throw new PortalServiceException("job must be an instance of VEGLJob");
        }

        try {
            session = sshCloudConnector.getSession(job);

            initialiseWorkingDirectory((VEGLJob) job);
            ExecResult res = sshCloudConnector.executeCommand(session, "qsub nci-download.job", workingDir);
            if(res.getExitStatus() != 0) {
                throw new PortalServiceException("Could not submit job file: " + res.getErr());
            }

            return res.getOut();
        } catch (IOException e) {
            throw new PortalServiceException("Error executing job " + job.getId(), e);
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
            String runningJobId = getJobLastInstanceId(job);

            session = sshCloudConnector.getSession(job);

            ExecResult res = sshCloudConnector.executeCommand(session, "qdel " + runningJobId);
            if(res.getExitStatus() != 0) {
                throw new PortalServiceException("Could not delete job: "+res.getErr());
            }
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
            String runningJobId = getJobLastInstanceId(job);

            session = sshCloudConnector.getSession(job);
            ExecResult res = sshCloudConnector.executeCommand(session, "qcat " + runningJobId);
            if (res.getExitStatus() != 0) {
                if (res.getOut().contains("Job is not running") ||
                    res.getOut().contains("Job has finished"))
                    return "";

                throw new PortalServiceException("Could not query job log: "+res.getErr());
            }

            return res.getOut();
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
            String runningJobId = getJobLastInstanceId(job);

            session = sshCloudConnector.getSession(job);
            ExecResult res = sshCloudConnector.executeCommand(session, "qstat -s " + runningJobId);
            if (res.getExitStatus() != 0) {
                if (res.getErr().contains("Job has finished")) {
                    return InstanceStatus.Missing;
                }
                throw new PortalServiceException("Could not query job status for job '"+job.getComputeInstanceId()+"': "+res.getErr());
            }

            if (res.getOut().contains("Job has finished")) {
                return InstanceStatus.Missing;
            }

            return InstanceStatus.Running;
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }
}

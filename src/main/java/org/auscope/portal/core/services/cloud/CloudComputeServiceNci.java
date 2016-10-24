package org.auscope.portal.core.services.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudJob;
import org.auscope.portal.core.cloud.ComputeType;
import org.auscope.portal.core.services.PortalServiceException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Service class wrapper for interacting with a remote cloud compute service
 * using CloudJob objects.
 *
 * @author Carsten Friedrich
 */
public class CloudComputeServiceNci extends CloudComputeService {
    public static final String NCI_USER_NAME = "nci_username";
    public static final String NCI_USER_KEY = "nci_userkey";

    /**
     * Any getStatus request on a job whose submission time is less than
     * STATUS_PENDING_SECONDS seconds away from the current time will be forced
     * to return a Pending status (ignoring any status checks)
     *
     * This is to avoid missing errors occurring when AWS hasn't fully caught up
     * to the new VM.
     */
    public static final long STATUS_PENDING_SECONDS = 30;

    private final Log logger = LogFactory.getLog(getClass());

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
    public CloudComputeServiceNci(String endpoint) {
        super(ProviderType.RAIJIN, endpoint, null);
    }

    class ExecResult {
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "ExecResult [out=" + out + ", err=" + err + ", exitStatus=" + exitStatus + "]";
        }

        private String out;

        public ExecResult(String out, String err, int exitStatus) {
            super();
            this.out = out;
            this.err = err;
            this.exitStatus = exitStatus;
        }

        /**
         * @return the out
         */
        public String getOut() {
            return out;
        }

        /**
         * @param out
         *            the out to set
         */
        public void setOut(String out) {
            this.out = out;
        }

        /**
         * @return the err
         */
        public String getErr() {
            return err;
        }

        /**
         * @param err
         *            the err to set
         */
        public void setErr(String err) {
            this.err = err;
        }

        /**
         * @return the exitStatus
         */
        public int getExitStatus() {
            return exitStatus;
        }

        /**
         * @param exitStatus
         *            the exitStatus to set
         */
        public void setExitStatus(int exitStatus) {
            this.exitStatus = exitStatus;
        }

        private String err;
        private int exitStatus;
    }

    String readStream(InputStream in, Channel channel) throws IOException, InterruptedException {
        StringBuilder res = new StringBuilder();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                res.append(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                break;
            }
            Thread.sleep(1000);
        }
        return res.toString();
    }

    ExecResult executeCommand(Session session, String command) throws PortalServiceException {
        ChannelExec channel = null;

        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(null);

            channel.connect();

            try (InputStream out = channel.getInputStream();
                    InputStream err = channel.getErrStream()) {
                String outStr = readStream(out, channel);
                String errStr = readStream(err, channel);
                return new ExecResult(outStr, errStr, channel.getExitStatus());
            } catch (IOException | InterruptedException e) {
                throw new PortalServiceException(e.getMessage(), e);
            }
        } catch (JSchException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    void createWorkingDirectory(Session session, String dirName) throws PortalServiceException {
        ExecResult res = executeCommand(session, dirName);
        logger.info(res);
    }

    private String workingDirPrefix = "/short/gv3/cxf599/vgl-";

    @Override
    public String executeJob(CloudJob job, String userDataString) throws PortalServiceException {
        JSch jsch = new JSch();
        Session session = null;
        String jobId = null;
        String workingDir = workingDirPrefix + job.getId();

        try {
            String prvkey = job.getProperty(NCI_USER_KEY);
            jsch.addIdentity(new IdentityString(jsch, prvkey), null);
            String userName = job.getProperty(NCI_USER_NAME);
            session = jsch.getSession(userName, getEndpoint(), 22);
            session.setConfig("StrictHostKeyChecking", "no");
            if (!session.isConnected()) {
                session.connect();
            }

            createWorkingDirectory(session, "mkdir " + workingDir);
            uploadPbs(session, workingDir, userDataString);

        } catch (JSchException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }

        return jobId;
    }

    static int checkAck(InputStream in) throws IOException, PortalServiceException {
        int b = in.read();
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0)
            return b;
        if (b == -1)
            return b;

        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                throw new PortalServiceException("SSH ACK error: " + sb.toString());
            }
            if (b == 2) { // fatal error
                throw new PortalServiceException("SSH ACK fatal error: " + sb.toString());
            }
        }
        return b;
    }

    void uploadPbs(Session session, String workingDir, String userDataString) throws PortalServiceException {
        String rfile = "job.pbs";
        String command = "scp -t " + workingDir+"/"+rfile;

        ChannelExec channel;
        try {
            channel = (ChannelExec) session.openChannel("exec");
        } catch (JSchException e1) {
            throw new PortalServiceException(e1.getMessage(), e1);
        }
        channel.setCommand(command);

        // get I/O streams for remote scp
        try (OutputStream out = channel.getOutputStream(); InputStream in = channel.getInputStream()) {
            channel.connect();

            checkAck(in);

            byte[] userData = userDataString.getBytes("UTF-8");

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = userData.length;
            command = "C0644 " + filesize + " " + rfile;
            // if (lfile.lastIndexOf('/') > 0) {
            // command += lfile.substring(lfile.lastIndexOf('/') + 1);
            // } else {
            // command += lfile;
            // }
            command += "\n";
            out.write(command.getBytes());
            out.flush();

            checkAck(in);

            out.write(userData);
            out.write(0);

            out.flush();
            checkAck(in);
            out.close();

        } catch (IOException | JSchException e) {
            throw new PortalServiceException(e.getMessage(), e);
        }

        channel.disconnect();
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
        // TODO
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
        return "Not implemented yet";
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
        return InstanceStatus.Missing;
    }
}

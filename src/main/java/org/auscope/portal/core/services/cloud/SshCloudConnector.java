/**
 *
 */
package org.auscope.portal.core.services.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileOwner;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.security.NCIDetails;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author fri096
 *
 */
public class SshCloudConnector {
    private final Log logger = LogFactory.getLog(getClass());

    private String endPoint;

    public SshCloudConnector(String endPoint) {
        this.endPoint= endPoint;
    }

    public class ExecResult {
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

    public ExecResult executeCommand(Session session, String command) throws PortalServiceException {
        return executeCommand(session, command, null);
    }

    /**
     * Retrieves the session for the specified Job.
     * @param job
     * @return
     * @throws PortalServiceException
     */
    public Session getSession(CloudFileOwner job) throws PortalServiceException {
        try {
            JSch jsch = new JSch();
            String prvkey = job.getProperty(NCIDetails.PROPERTY_NCI_KEY);
            jsch.addIdentity(new IdentityString(jsch, prvkey), null);
            String userName = job.getProperty(NCIDetails.PROPERTY_NCI_USER);
            Session session = jsch.getSession(userName, endPoint, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            if (!session.isConnected()) {
                session.connect();
            }

            return session;
        } catch (JSchException ex) {
            logger.error("Unable to retrieve SSH session for job " + job.getId() + ":" + ex.getMessage());
            logger.debug("Exception:", ex);
            throw new PortalServiceException("Unable to retrieve SSH session for job " + job.getId(), ex);
        }

    }

    public ExecResult executeCommand(Session session, String command, String workingDir) throws PortalServiceException {
        ChannelExec channel = null;
        if(workingDir!=null) {
            command = "cd "+workingDir+"; "+command;
        }

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

    public void createDirectory(Session session, String dirName) throws PortalServiceException {
        String command = "umask 002; mkdir -m 770 -p " + dirName;
        ExecResult res = executeCommand(session, command);
        if (res.getExitStatus() > 0) {
            throw new PortalServiceException("command '" + command + "' returned status" + res.getExitStatus() + " : stderr: " + res.getErr());
        }
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

    void scpStringToFile(Session session, String workingDir, String fileName, String userDataString) throws PortalServiceException {
        String command = "scp -t " + workingDir+"/"+fileName;

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
            command = "C0644 " + filesize + " " + fileName;
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

}

/**
 * 
 */
package org.auscope.portal.core.services.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.cloud.CloudFileOwner;
import org.auscope.portal.core.services.PortalServiceException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * @author fri096
 *
 */
public class CloudStorageServiceNci extends CloudStorageService {
    private final Log logger = LogFactory.getLog(getClass());

    private SshCloudConnector sshCloudConnector;

    public CloudStorageServiceNci(String endpoint, String provider) {
        super(endpoint, provider, null);
        this.sshCloudConnector = new SshCloudConnector(endpoint);
    }

    public static final String JOB_DIR_PREFIX = "/short/gv3/";

    public String getJobDirectory(CloudFileOwner job) {
        return JOB_DIR_PREFIX + job.getProperty(SshCloudConnector.SSH_USER_NAME) + "/vgl/job-" + job.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.auscope.portal.core.services.cloud.CloudStorageService#getJobFile(org
     * .auscope.portal.core.cloud.CloudFileOwner, java.lang.String)
     */
    @Override
    public InputStream getJobFile(CloudFileOwner job, String fileName) throws PortalServiceException {
        String fullPath = getJobDirectory(job) + "/" + fileName;
        try {
            Session session = sshCloudConnector.getSession(job);
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;
            return new SshInputStream(session, c, c.get(fullPath));
        } catch (JSchException | SftpException e) {
            throw new PortalServiceException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.auscope.portal.core.services.cloud.CloudStorageService#listJobFiles(
     * org.auscope.portal.core.cloud.CloudFileOwner)
     */
    @Override
    public CloudFileInformation[] listJobFiles(CloudFileOwner job) throws PortalServiceException {
        String fullPath = getJobDirectory(job);
        Session session = null;
        Channel channel = null;
        try {
            session = sshCloudConnector.getSession(job);
            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;
            Vector<LsEntry> files = c.ls(fullPath);
            ArrayList<CloudFileInformation> res = new ArrayList<>(files.size());
            for (LsEntry entry : files) {
                res.add(new CloudFileInformation(entry.getFilename(), entry.getAttrs().getSize(), null));
            }
            return res.toArray(new CloudFileInformation[0]);
        } catch (JSchException | SftpException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public void rmDirRecursive(Session session, ChannelSftp channel, String path) throws SftpException {
        Vector<LsEntry> files = channel.ls(path);

        for (LsEntry entry : files) {
            String filename = entry.getFilename();
            if(filename.equals(".")||filename.equals("..")) {
                continue;
            }

            String entryFullPath = path+"/"+filename;
            logger.debug("Deleting: "+entryFullPath);
            if (entry.getAttrs().isDir()) {
                rmDirRecursive(session, channel, entry.getLongname());
                channel.rmdir(entryFullPath);
            } else {
                channel.rm(entryFullPath);
            }
        }
        
        channel.rmdir(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.auscope.portal.core.services.cloud.CloudStorageService#deleteJobFiles
     * (org.auscope.portal.core.cloud.CloudFileOwner)
     */
    @Override
    public void deleteJobFiles(CloudFileOwner job) throws PortalServiceException {
        String fullPath = getJobDirectory(job);
        Session session = null;
        Channel channel = null;

        try {
            session = sshCloudConnector.getSession(job);
            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;

            rmDirRecursive(session, c, fullPath);
        } catch (JSchException | SftpException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.auscope.portal.core.services.cloud.CloudStorageService#
     * getJobFileMetadata(org.auscope.portal.core.cloud.CloudFileOwner,
     * java.lang.String)
     */
    @Override
    public CloudFileInformation getJobFileMetadata(CloudFileOwner job, String fileName) throws PortalServiceException {
        String fullPath = getJobDirectory(job)+"/"+fileName;
        Session session = null;
        Channel channel = null;
        try {
            session = sshCloudConnector.getSession(job);
            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;
            SftpATTRS attr = c.lstat(fullPath);
            return new CloudFileInformation(fileName, attr.getSize(), null);
        } catch (JSchException | SftpException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.auscope.portal.core.services.cloud.CloudStorageService#uploadJobFiles
     * (org.auscope.portal.core.cloud.CloudFileOwner, java.io.File[])
     */
    @Override
    public void uploadJobFiles(CloudFileOwner job, File[] files) throws PortalServiceException {
        String fullPath = getJobDirectory(job);
        Session session = null;
        Channel channel = null;

        try {
            session = sshCloudConnector.getSession(job);
            sshCloudConnector.createDirectory(session, fullPath);
            
            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;
            c.cd(fullPath);
            
            for (File file : files) {
                try (InputStream in= new FileInputStream(file)) {
                    c.put(in, file.getName());
                } catch (IOException e) {
                    throw new PortalServiceException(e.getMessage(),e);
                } 
            }
        } catch (JSchException | SftpException e) {
            throw new PortalServiceException(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
}

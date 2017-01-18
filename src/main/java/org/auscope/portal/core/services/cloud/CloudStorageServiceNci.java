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
import org.auscope.portal.server.web.security.NCIDetails;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * Cloud storage service for reading/writing files to NCI's lustre file system over SSH
 *
 *
 * @author fri096
 * @author Josh Vote (CSIRO)
 *
 */
public class CloudStorageServiceNci extends CloudStorageService {
    private final Log logger = LogFactory.getLog(getClass());

    private SshCloudConnector sshCloudConnector;

    public CloudStorageServiceNci(String endpoint, String provider) {
        super(endpoint, provider, null);
        this.sshCloudConnector = new SshCloudConnector(endpoint);
    }

    /**
     * Gets the full POSIX path to the working directory of the job (where the downloaded files will be stored)
     * @param job
     * @return
     */
    public String getWorkingJobDirectory(CloudFileOwner job) {
        return String.format("/short/%1$s/vl-workingdir/%2$s", job.getProperty(NCIDetails.PROPERTY_NCI_PROJECT), generateBaseKey(job));
    }

    /**
     * Gets the full POSIX path to the output directory of the job (where the scripts and output files will be stored)
     * @param job
     * @return
     */
    public String getOutputJobDirectory(CloudFileOwner job) {
        return String.format("/g/data/%1$s/vl-jobs/%2$s", job.getProperty(NCIDetails.PROPERTY_NCI_PROJECT), generateBaseKey(job));
    }

    private boolean jobFileExists(ChannelSftp c, String fullPath) throws PortalServiceException {
        try {
            return c.ls(fullPath).size() > 0; // JSCH always throws an exception on missing paths. This is a catchall in case the behavior changes
        } catch (SftpException ex) {
            return false;
        }
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
        String fullPath = getOutputJobDirectory(job) + "/" + fileName;
        try {
            Session session = sshCloudConnector.getSession(job);
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;
            if (jobFileExists(c, fullPath)) {
                return new SshInputStream(session, c, c.get(fullPath));
            } else {
                return null;
            }
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
        String fullPath = getOutputJobDirectory(job);
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
                String fileName = entry.getFilename();
                if (fileName.startsWith(".")) {
                    continue;
                }
                res.add(new CloudFileInformation(entry.getFilename(), entry.getAttrs().getSize(), null));
            }
            return res.toArray(new CloudFileInformation[0]);
        } catch (JSchException | SftpException e) {
            throw new PortalServiceException("Error listing job " + job.getId() + " files at " + fullPath , e);
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
        String fullPath = getOutputJobDirectory(job);
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
        String fullPath = getOutputJobDirectory(job)+"/"+fileName;
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
        String fullPath = getOutputJobDirectory(job);
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.auscope.portal.core.services.cloud.CloudStorageService#uploadJobFiles
     * (org.auscope.portal.core.cloud.CloudFileOwner, java.io.File[])
     */
    @Override
    public void uploadJobFile(CloudFileOwner job, String name, InputStream data) throws PortalServiceException {
        String fullPath = getOutputJobDirectory(job);
        Session session = null;
        Channel channel = null;

        try {
            session = sshCloudConnector.getSession(job);
            sshCloudConnector.createDirectory(session, fullPath);

            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;
            c.cd(fullPath);

            c.put(data, name);
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

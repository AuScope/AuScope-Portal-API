package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.cloud.StagingInformation;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.util.FileUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.web.controllers.GridSubmitController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * A service class for handling file uploads and storing them in a local staging directory
 * @author Josh Vote
 *
 */
@Service
public class JobFileService {
	private final Log logger = LogFactory.getLog(getClass());
    protected StagingInformation stagingInformation;
    
    @Autowired
    public JobFileService(StagingInformation stagingInformation) {
    	this.stagingInformation = stagingInformation;
    }
    
    /**
     * Given 2 path components this function will concatenate them together.
     * 
     * The result will ensure that you will not end up with 2 consecutive File.pathSeperator
     * characters at the place of the concatenation.
     * @param p1
     * @param p2
     * @return
     */
    protected static String pathConcat(String p1, String p2) {
    	if (p1.endsWith(File.separator) && p2.startsWith(File.separator)) {
    		return p1 + p2.substring(1);
    	} else if ((!p1.endsWith(File.separator) && p2.startsWith(File.separator)) ||
    			    (p1.endsWith(File.separator) && !p2.startsWith(File.separator))) {
    		return p1 + p2;
    	} else {
    		return p1 + File.separator + p2;
    	}
    }
    
    /**
     * Deletes the entire job stage in directory, returns true on success
     * @param job Must have its fileStorageId parameter set
     */
    public boolean deleteStageInDirectory(VEGLJob job) {
    	if (job.getFileStorageId() == null || job.getFileStorageId().isEmpty()) {
        	throw new IllegalArgumentException("Job has no FileStorageID");
        }
    	
    	File jobInputDir = new File(pathConcat(stagingInformation.getStageInDirectory(), job.getFileStorageId()));
    	logger.debug("Recursively deleting " + jobInputDir.getPath());
    	if (!jobInputDir.exists()) {
    		return true;
    	}
    	
    	return FileUtil.deleteFilesRecursive(jobInputDir);
    }
    
    /**
     * Deletes a specific file from the job stage in directory.
     * @param job Must have its fileStorageId parameter set
     * @param fileName
     * @return
     */
    public boolean deleteStageInFile(VEGLJob job, String fileName) {
    	if (job.getFileStorageId() == null || job.getFileStorageId().isEmpty()) {
        	throw new IllegalArgumentException("Job has no FileStorageID");
        }
    	if (fileName.contains(File.pathSeparator) || fileName.contains(File.separator)) {
    		throw new IllegalArgumentException("fileName cannot include " + File.pathSeparator + " or " + File.separator);
    	}
    	
    	String directoryPath = pathConcat(stagingInformation.getStageInDirectory(), job.getFileStorageId()); 
    	File file = new File(pathConcat(directoryPath, fileName));
    	logger.debug("deleting " + file.getPath());
    	if (!file.exists()) {
    		return true;
    	}
    	
    	return file.delete();
    }
    
    /**
     * Given a job create a folder that is unique to that job in the internal staging area.
     * 
     * @param job Must have its fileStorageId parameter set
     * @return
     * @throws IOException If the directory creation fails
     */
    public void generateStageInDirectory(VEGLJob job) throws IOException {
    	if (job.getFileStorageId() == null || job.getFileStorageId().isEmpty()) {
        	throw new IllegalArgumentException("Job has no FileStorageID");
        }
        String jobInputDir = pathConcat(stagingInformation.getStageInDirectory(), job.getFileStorageId());
        
        logger.debug("Attempting to generate job input dir " + jobInputDir);
        
        boolean success = new File(jobInputDir).mkdir();
        if (!success) {
        	throw new IOException("Failed to create stage in directory: " + jobInputDir);
        }
    }
    
    /**
     * Lists every file in the specified job's stage in directory
     * @param job Must have its fileStorageId parameter set
     * @return
     * @throws IOException 
     */
    public File[] listStageInDirectoryFiles(VEGLJob job) throws IOException {
    	if (job.getFileStorageId() == null || job.getFileStorageId().isEmpty()) {
        	throw new IllegalArgumentException("Job has no FileStorageID");
        }
    	
    	//List files in directory, add them to array
    	File directory = new File(pathConcat(stagingInformation.getStageInDirectory(), job.getFileStorageId()));
    	logger.debug("Attempting to list files at " + directory.getPath());
    	if (!directory.isDirectory()) {
    		throw new IOException("Not a directory: " + directory.getPath());
    	}
    	File[] files = directory.listFiles();
    	if (files == null) {
    		throw new IOException("Unable to list files in: " + directory.getPath());
    	}
    	
    	return files;
    }
    
    /**
     * Creates a new file object in the specified job's stage in directory
     * @param job Must have its fileStorageId parameter set
     * @param fileName
     * @return
     */
    public File createStageInDirectoryFile(VEGLJob job, String fileName) {
    	if (job.getFileStorageId() == null || job.getFileStorageId().isEmpty()) {
        	throw new IllegalArgumentException("Job has no FileStorageID");
        }
    	if (fileName.contains(File.pathSeparator) || fileName.contains(File.separator)) {
    		throw new IllegalArgumentException("fileName cannot include " + File.pathSeparator + " or " + File.separator);
    	}
    	
    	String directory = pathConcat(stagingInformation.getStageInDirectory(), job.getFileStorageId());
    	
    	return new File(pathConcat(directory, fileName));
    }
    
    /**
     * Given a MultipartHttpServletRequest with an internal file parameter, write that
     * file to the staging directory of the specified job
     * 
     * returns a FileInfo object describing the file on the file system
     * 
     * @param job Must have its fileStorageId parameter set
     * @param request
     * @throws IOException 
     */
    public File handleFileUpload(VEGLJob job, MultipartHttpServletRequest request) throws IOException {
        MultipartFile f = request.getFile("file");
        if (f == null) {
        	throw new IOException("No file parameter provided.");
        }
        if (job.getFileStorageId() == null || job.getFileStorageId().isEmpty()) {
        	throw new IllegalArgumentException("Job has no FileStorageID");
        }
        
        String directory = pathConcat(stagingInformation.getStageInDirectory(), job.getFileStorageId());
        String destinationPath = pathConcat(directory, f.getOriginalFilename());
    	logger.debug("Saving uploaded file to " + destinationPath);

    	File destination = new File(destinationPath);
    	if (destination.exists()) {
            logger.debug("Will overwrite existing file.");
        }
    	
    	f.transferTo(destination);
    	
    	return destination;
    }
    
    /**
     * This function will attempt to download fileName from job's staging directory by writing
     * directly to the output stream of response.
     * 
     * response will have its internal outputStream directly accessed and written to (if the internal
     * file request is successful).
     * 
     * @param job Must have its fileStorageId parameter set
     * @throws IOException 
     */
    public void handleFileDownload(VEGLJob job, String fileName, HttpServletResponse response) throws IOException {
    	if (job.getFileStorageId() == null || job.getFileStorageId().isEmpty()) {
        	throw new IllegalArgumentException("Job has no FileStorageID");
        }
    	
    	String directory = pathConcat(stagingInformation.getStageInDirectory(), job.getFileStorageId());
    	String filePath = pathConcat(directory, fileName);
    	
    	logger.debug("Downloading: " + filePath);
    	
    	//Simple sanity check
        File f = new File(filePath);
        if (!f.canRead()) {
            throw new IOException("File "+f.getPath()+" not readable!");
        } 

        //Start configuring our response for a download stream
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\""+fileName+"\"");

        //Then push all data down
        byte[] buffer = new byte[4096];
        int count = 0;
        OutputStream out = response.getOutputStream();
        FileInputStream fin = new FileInputStream(f);
        while ((count = fin.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
        out.flush();
    }
}

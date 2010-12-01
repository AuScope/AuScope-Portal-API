/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple bean class that stores information about a file.
 *
 * @author Cihan Altinay
 */
public class FileInformation implements Serializable {
    /** The filename */
    private String name;
    /** The file size in bytes */
    private long size;
    /** isDirectory flag */
    private boolean directoryFlag = false;
    /** parent directory path */
    private String parentPath = "";
    /** AWS S3 storage key */
    private String s3Key = "";
    
    /** Logger for this class */
    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Constructor with name and size
     */
    public FileInformation(String s3Key, long size) {
        this.s3Key = s3Key;
        this.size = size;
        // key will be in the format email@address-VEGLJob-timestamp/filename.format.
        // the file name will be the last part after the /
        String [] keyParts = s3Key.split("/");
        this.name = keyParts[keyParts.length-1];
        logger.debug("filename: " + name);
    }
    
    /**
     * Returns the filename.
     *
     * @return The filename.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the filename.
     *
     * @param name The filename.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the file size.
     *
     * @return The file size in bytes.
     */
    public long getSize() {
        return size;
    }

	/**
	 * @return the directoryFlag
	 */
	public boolean getDirectoryFlag() {
		return directoryFlag;
	}

	/**
	 * @param directoryFlag the directoryFlag to set
	 */
	public void setDirectoryFlag(boolean directoryFlag) {
		this.directoryFlag = directoryFlag;
	}

	/**
	 * @return the parentPath
	 */
	public String getParentPath() {
		return parentPath;
	}

	/**
	 * @param parentPath the parentPath to set
	 */
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

}


/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import java.io.Serializable;
import java.text.NumberFormat;

/**
 * Simple bean class that stores information about a file.
 *
 * @author Cihan Altinay
 */
public class FileInformation implements Serializable {
    /** The filename */
    private String name;
    /** The file size formatted to be readable */
    private String readableSize;
    /** The file size in bytes */
    private long size;
    /** The subJob the file belongs to */
    private String subJob = "";
    /** isDirectory flag */
    private boolean directoryFlag = false;
    /** parent directory path */
    private String parentPath = "";

    /**
	 * @return the subJob
	 */
	public String getSubJob() {
		return subJob;
	}

	/**
	 * @param subJob the subJob to set
	 */
	public void setSubJob(String subJob) {
		this.subJob = subJob;
	}

	/**
     * Constructor with name and size
     */
    public FileInformation(String name, long size) {
        this.name = name;
        setSize(size);
    }

	/**
     * Constructor with name and size
     */
    public FileInformation(String name, long size, String subJob) {
        this.name = name;
        setSize(size);
        this.subJob = subJob;
    }
    
	/**
     * Constructor with name and size
     */
    public FileInformation(String name, long size, String dirPath, boolean isDirectory) {
        this.name = name;
        setSize(size);
        this.parentPath = dirPath;
        this.directoryFlag = isDirectory;
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
     * Returns a readable form of the file size.
     *
     * @return The file size.
     */
    public String getReadableSize() {
        return readableSize;
    }

    /**
     * Sets the file size in bytes.
     *
     * @param size The file size.
     */
    public void setSize(long size) {
        this.size = size;
        readableSize = NumberFormat.getInstance().format(size);
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

}


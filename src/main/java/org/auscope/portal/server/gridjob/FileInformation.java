/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import java.io.File;
import java.io.Serializable;

/**
 * Simple bean class that stores basic information about a file. Designed to be passed between GUI and backend
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

    public FileInformation(File file) {
    	this.name = file.getName();
    	this.size = file.length();
    	this.directoryFlag = file.isDirectory();
    	this.parentPath = file.getParent();
    }
    
    public FileInformation(String name, long size, boolean directoryFlag,
			String parentPath) {
		super();
		this.name = name;
		this.size = size;
		this.directoryFlag = directoryFlag;
		this.parentPath = parentPath;
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
}


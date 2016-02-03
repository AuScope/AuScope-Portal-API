/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.gridjob;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.server.vegl.VEGLJob;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple bean class that stores basic information about a file. Designed to be passed between GUI and backend
 *
 * @author Cihan Altinay
 */
public class FileInformation implements Serializable {
    private static final long serialVersionUID = -7357222903106188095L;


    protected final Log logger = LogFactory.getLog(getClass());

    /** The primary key for this download*/
    private Integer id;
    /** The filename */
    private String fileName;
    /** The data name */
    private String name;
    /** The file size in bytes */
    private long size;
    /** isDirectory flag */
    private boolean directoryFlag = false;
    /** parent directory path */
    private String parentPath = "";
    /** owner */
    private String owner;
    /** date */
    private Date date;
    /** data description */
    private String description;
    /** copyright status */
    private String copyright;
    /** job */
    private VEGLJob parent;

    // Creates an un-initialized instance
    public FileInformation() {
        super();
    }


    public FileInformation(File file) {
    	this.name = file.getName();
    	this.size = file.length();
    	this.directoryFlag = file.isDirectory();
    	this.parentPath = file.getParent();
    }
    
    public FileInformation(String fileName, long size, boolean directoryFlag,
			String parentPath) {
		super();
		this.fileName = fileName;
		this.size = size;
		this.directoryFlag = directoryFlag;
		this.parentPath = parentPath;
	}

    public FileInformation(String fileName, long size, boolean directoryFlag, String parentPath, String owner, String date) {
        this(fileName, size, directoryFlag, parentPath);
        this.owner = owner;
        setDate(date);
    }

    public FileInformation(String fileName, String dataName, long size, boolean directoryFlag, String parentPath, String owner, String date, String description, String copyright) {
        this(fileName, size, directoryFlag, parentPath, owner, date);
        this.name = dataName;
        this.description = description;
        this.copyright = copyright;
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


    public VEGLJob getParent() {
        return parent;
    }

    public void setParent(VEGLJob parent) {
        this.parent = parent;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            this.date = format.parse(date);
        } catch (ParseException e) {
            logger.error(e);
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectoryFlag() {
        return directoryFlag;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
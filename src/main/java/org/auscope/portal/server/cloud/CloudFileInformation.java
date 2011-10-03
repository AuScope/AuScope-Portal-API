/*
 * This file is part of the AuScope Virtual Rock Lab (VRL) project.
 * Copyright (c) 2009 ESSCC, The University of Queensland
 *
 * Licensed under the terms of the GNU Lesser General Public License.
 */
package org.auscope.portal.server.cloud;

import java.io.Serializable;

/**
 * Simple bean class that stores information about a file in a cloud.
 *
 * @author Cihan Altinay
 * @author Joshua Vote
 */
public class CloudFileInformation implements Serializable {
	private static final long serialVersionUID = 6240194652206852285L;
	/** The file size in bytes */
    private long size;
    /** cloud storage key */
    private String cloudKey = "";
    /** URL where the file can be accessed by anyone (only valid if file is publicly readable) */
    private String publicUrl = "";

    /**
     * Constructor with name and size
     */
    public CloudFileInformation(String cloudKey, long size, String publicUrl) {
        this.cloudKey = cloudKey;
        this.size = size;
        this.publicUrl = publicUrl;
    }
    
    /**
     * Returns the filename.
     *
     * @return The filename.
     */
    public String getName() {
    	String [] keyParts = cloudKey.split("/");
    	return keyParts[keyParts.length-1];
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
	 * Gets the underlying cloud key representing this file
	 * @return
	 */
	public String getCloudKey() {
		return cloudKey;
	}

	/**
	 * Sets the underlying cloud key representing this file
	 * @param cloudKey
	 */
	public void setCloudKey(String cloudKey) {
		this.cloudKey = cloudKey;
	}

	/**
	 * Gets the public URL where this file can be accessed (assuming the file has its ACL set
	 * to public read)
	 * @return
	 */
    public String getPublicUrl() {
        return publicUrl;
    }

    /**
     * Sets the public URL where this file can be accessed (assuming the file has its ACL set
     * to public read)
     * @param publicUrl
     */
    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

}


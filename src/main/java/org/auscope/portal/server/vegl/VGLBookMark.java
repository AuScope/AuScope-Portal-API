package org.auscope.portal.server.vegl;

import java.io.Serializable;

import org.auscope.portal.server.web.security.ANVGLUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Simple class that stores information(file identifier, service id) about a dataset that is bookmarked by a user
 * @author san239
 *
 */
public class VGLBookMark  implements Serializable {	
   
	private static final long serialVersionUID = 8620093753366974702L;
	/** identifier of the dataset */
	private String fileIdentifier;
	/** service id of the dataset */
    private String serviceId;
    /** The user owning the book mark */
    @JsonIgnore
    private ANVGLUser parent;
    /** The actual URL that when accessed with a GET request will download data*/
    private String url;
    /** Where the downloaded data (on the job VM) will be downloaded to*/
    private String localPath;
    /** The descriptive name of this download*/
    private String name;
    /** The long description for this download*/
    private String description;
    /** If this download is for a spatial region this will represent the most northern bounds of the region in WGS:84*/
    private Double northBoundLatitude;
    /** If this download is for a spatial region this will represent the most southern bounds of the region in WGS:84*/
    private Double southBoundLatitude;
    /** If this download is for a spatial region this will represent the most eastern bounds of the region in WGS:84*/
    private Double eastBoundLongitude;
    /** If this download is for a spatial region this will represent the most western bounds of the region in WGS:84*/
    private Double westBoundLongitude;
    
    public VGLBookMark() {
    	super();
    }
    
    public VGLBookMark(String fileIdentifier, String serviceId, ANVGLUser user) {
        super();
        this.setFileIdentifier(fileIdentifier);
        this.setServiceId(serviceId);
        this.parent = user;
    }

    public String getFileIdentifier() {
		return fileIdentifier;
	}

	public void setFileIdentifier(String fileIdentifier) {
		this.fileIdentifier = fileIdentifier;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
    
   
    /**
     * User that owns the bookmarks
     * @return
     */
    public ANVGLUser getParent() {
        return parent;
    }

    /**
     * User that owns the book marks
     * @param parent
     */
    public void setParent(ANVGLUser parent) {
        this.parent = parent;
    }

    /**
     * The descriptive name of this download
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The descriptive name of this download
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *  The long description for this download
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *  The long description for this download
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The actual URL that when accessed with a GET request will download data
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * The actual URL that when accessed with a GET request will download data
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Where the downloaded data (on the job VM) will be downloaded to
     * @return
     */
    public String getLocalPath() {
        return localPath;
    }

    /**
     * Where the downloaded data (on the job VM) will be downloaded to
     * @param localPath
     */
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    /**
     * If this download is for a spatial region this will represent the most northern bounds of the region in WGS:84
     * @return
     */
    public Double getNorthBoundLatitude() {
        return northBoundLatitude;
    }

    /**
     * If this download is for a spatial region this will represent the most northern bounds of the region in WGS:84
     * @param northBoundLatitude
     */
    public void setNorthBoundLatitude(Double northBoundLatitude) {
        this.northBoundLatitude = northBoundLatitude;
    }

    /**
     * If this download is for a spatial region this will represent the most southern bounds of the region in WGS:84
     * @return
     */
    public Double getSouthBoundLatitude() {
        return southBoundLatitude;
    }

    /**
     * If this download is for a spatial region this will represent the most southern bounds of the region in WGS:84
     * @param southBoundLatitude
     */
    public void setSouthBoundLatitude(Double southBoundLatitude) {
        this.southBoundLatitude = southBoundLatitude;
    }

    /**
     * If this download is for a spatial region this will represent the most eastern bounds of the region in WGS:84
     * @return
     */
    public Double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    /**
     * If this download is for a spatial region this will represent the most eastern bounds of the region in WGS:84
     * @param eastBoundLongitude
     */
    public void setEastBoundLongitude(Double eastBoundLongitude) {
        this.eastBoundLongitude = eastBoundLongitude;
    }

    /**
     * If this download is for a spatial region this will represent the most western bounds of the region in WGS:84
     * @return
     */
    public Double getWestBoundLongitude() {
        return westBoundLongitude;
    }

    /**
     * If this download is for a spatial region this will represent the most western bounds of the region in WGS:84
     * @param westBoundLongitude
     */
    public void setWestBoundLongitude(Double westBoundLongitude) {
        this.westBoundLongitude = westBoundLongitude;
    }

	
}



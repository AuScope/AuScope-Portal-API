package org.auscope.portal.server.vegl;

import java.io.Serializable;

/**
 * Represents a remote file download step that occurs during a VGL job startup
 * @author Josh Vote
 *
 */
public class VglDownload implements Serializable, Cloneable {
    private static final long serialVersionUID = 5436097345907506395L;


    /** The primary key for this download*/
    private Integer id;
    /** The descriptive name of this download*/
    private String name;
    /** The long description for this download*/
    private String description;
    /** The actual URL that when accessed with a GET request will download data*/
    private String url;
    /** Where the downloaded data (on the job VM) will be downloaded to*/
    private String localPath;
    /** If this download is for a spatial region this will represent the most northern bounds of the region in WGS:84*/
    private Double northBoundLatitude;
    /** If this download is for a spatial region this will represent the most southern bounds of the region in WGS:84*/
    private Double southBoundLatitude;
    /** If this download is for a spatial region this will represent the most eastern bounds of the region in WGS:84*/
    private Double eastBoundLongitude;
    /** If this download is for a spatial region this will represent the most western bounds of the region in WGS:84*/
    private Double westBoundLongitude;
    /** The job that owns this download*/
    private VEGLJob parent;


    /**
     * Default constructor
     */
    public VglDownload() {
        this(null);
    }

    /**
     *
     * @param id The primary key for this download
     */
    public VglDownload(Integer id) {
        super();
        this.id = id;
    }

    /**
     * The primary key for this download
     * @return
     */
    public Integer getId() {
        return id;
    }

    /**
     * The primary key for this download
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
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

    /**
     * The job that owns this download
     * @return
     */
    public VEGLJob getParent() {
        return parent;
    }

    /**
     * The job that owns this download
     * @param parent
     */
    public void setParent(VEGLJob parent) {
        this.parent = parent;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this.id.equals(((VglDownload)obj).id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}

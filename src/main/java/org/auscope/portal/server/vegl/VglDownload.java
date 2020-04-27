package org.auscope.portal.server.vegl;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a remote file download step that occurs during a VL job startup
 * @author Josh Vote
 *
 */
@Entity
@Table(name="downloads")
public class VglDownload implements Serializable, Cloneable {
    private static final long serialVersionUID = 5436097345907506395L;

    /** The primary key for this download*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobId")
    private VEGLJob parent;

    /*
     * Removing these as can't see they're used and not defined in database,
     * also affects JobDownloadController, TestJobDownloadController
     */
    /** Organisation or person responsible for this data set */
    //private String owner;
    /** Url of the data this is a subset of (if applicable) */
    //private String parentUrl;
    /** Name of the data this is a subset of (if applicable) */
    //private String parentName;


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

    /*
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
    */

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

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof VglDownload)) {
            return false;
        }

        return this.id.equals(((VglDownload)obj).id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}

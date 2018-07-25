package org.auscope.portal.server.vegl;

import org.auscope.portal.server.web.security.ANVGLUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VGLBookMarkDownload {
	
	 /** The primary key for this bookmark download*/
    private Integer id;
   
	/** The descriptive name of the bookmark for this download*/
    private String bookmarkOptionName;
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
    @JsonIgnore
    private VGLBookMark parent;
        
    

    public VGLBookMarkDownload() {
    	super();
    }
    
    /**
    *
    * @param id The primary key for this book mark
    */
   public VGLBookMarkDownload(Integer id) {
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
    * The descriptive name of the bookmark for this download
    * @return
    */
   public String getBookmarkOptionName() {
       return bookmarkOptionName;
   }

   /**
    * The descriptive name of the bookmark for this download
    * @param name
    */
   public void setBookmarkOptionName(String bookmarkOptionName) {
       this.bookmarkOptionName = bookmarkOptionName;
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
    * data set having the bookmark for download options
    * @return
    */
   public VGLBookMark getParent() {
       return parent;
   }

   /**
    * data set having the bookmark for download options
    * @param parent
    */
   public void setParent(VGLBookMark parent) {
       this.parent = parent;
   }

}

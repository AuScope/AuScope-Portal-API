package org.auscope.portal.server.vegl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.server.web.security.ANVGLUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Simple class that stores information(file identifier, service id) about a dataset that is bookmarked by a user
 * @author san239
 *
 */
public class VGLBookMark  implements Serializable {	
   
	private static final long serialVersionUID = 8620093753366974702L;
	 /** The primary key for this book mark*/
    private Integer id;
	/** identifier of the dataset */
	private String fileIdentifier;
	/** service id of the dataset */
    private String serviceId;
    /** The user owning the book mark */
    @JsonIgnore
    private ANVGLUser parent;
    /** A List of download options associated with the bookmark */
    private List<VGLBookMarkDownload> bookMarkDownloads;
    
        
    public VGLBookMark() {
    	super();
    	this.bookMarkDownloads =  new ArrayList<>();
    }
    
    public VGLBookMark(String fileIdentifier, String serviceId, ANVGLUser user) {
        super();
        this.setFileIdentifier(fileIdentifier);
        this.setServiceId(serviceId);
        this.parent = user;
    }

    /**
    *
    * @param id The primary key for this book mark
    */
   public VGLBookMark(Integer id) {
       super();
       this.id = id;
   }

   /**
    * The primary key for this book mark
    * @return
    */
   public Integer getId() {
       return id;
   }

   /**
    * The primary key for this book mark
    * @param id
    */
   public void setId(Integer id) {
       this.id = id;
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
     * gets a list of book marked  download options
     * @return
     */

	public List<VGLBookMarkDownload> getBookMarkDownloads() {
		return bookMarkDownloads;
	}
	/**
	 * sets the list of download options for the book mark
	 * @param bookMarks
	 */

	public void setBookMarkDownloads(List<VGLBookMarkDownload> bookMarkDownloads) {
		this.bookMarkDownloads = bookMarkDownloads;
		for (VGLBookMarkDownload downloadOption : bookMarkDownloads) {
			downloadOption.setParent(this);
        }
	}
}



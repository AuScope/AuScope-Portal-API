package org.auscope.portal.server.vegl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.auscope.portal.server.web.security.ANVGLUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Simple class that stores information(file identifier, service id) about a dataset that is bookmarked by a user
 * @author san239
 *
 */
@Entity
@Table(name = "bookmarks")
public class VGLBookMark  implements Serializable {	
   
	private static final long serialVersionUID = 8620093753366974702L;
	 /** The primary key for this book mark*/
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	/** identifier of the dataset */
	@Column(nullable=false)
	private String fileIdentifier;
	/** service id of the dataset */
	@Column(nullable=false)
    private String serviceId;
    /** The user owning the book mark */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private ANVGLUser parent;
    /** A List of download options associated with the bookmark */
    @OneToMany(mappedBy = "parent", cascade=CascadeType.ALL, orphanRemoval=true)
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
		return (bookMarkDownloads != null) ? bookMarkDownloads : new ArrayList<VGLBookMarkDownload>();
	}
	/**
	 * sets the list of download options for the book mark
	 * @param bookMarks
	 */

	public void setBookMarkDownloads(List<VGLBookMarkDownload> bookMarkDownloads) {
		if(this.bookMarkDownloads == null) {
			this.bookMarkDownloads = bookMarkDownloads;
		} else {
			this.bookMarkDownloads.clear();
			if(bookMarkDownloads != null) {
				for (VGLBookMarkDownload downloadOption : bookMarkDownloads) {
					downloadOption.setParent(this);
					this.bookMarkDownloads.add(downloadOption);
		        }
			}
		}
	}
}



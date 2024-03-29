package org.auscope.portal.server.bookmark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.auscope.portal.server.web.security.PortalUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Simple class that stores information(file identifier, service id) about a dataset that is bookmarked by a user
 * @author san239
 *
 */
@Entity
@Table(name = "bookmarks")
public class BookMark  implements Serializable {	
   
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
    private PortalUser parent;
    /** A List of download options associated with the bookmark */
    @OneToMany(mappedBy = "parent", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<BookMarkDownload> bookMarkDownloads;
    
    
    public BookMark() {
    	super();
    	this.bookMarkDownloads =  new ArrayList<>();
    }
    
    public BookMark(String fileIdentifier, String serviceId, PortalUser user) {
        super();
        this.setFileIdentifier(fileIdentifier);
        this.setServiceId(serviceId);
        this.parent = user;
    }

    /**
    *
    * @param id The primary key for this book mark
    */
   public BookMark(Integer id) {
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
    public PortalUser getParent() {
        return parent;
    }

    /**
     * User that owns the book marks
     * @param parent
     */
    public void setParent(PortalUser parent) {
        this.parent = parent;
    }
   
    /**
     * gets a list of book marked  download options
     * @return
     */

	public List<BookMarkDownload> getBookMarkDownloads() {
		return (bookMarkDownloads != null) ? bookMarkDownloads : new ArrayList<BookMarkDownload>();
	}
	/**
	 * sets the list of download options for the book mark
	 * @param bookMarks
	 */

	public void setBookMarkDownloads(List<BookMarkDownload> bookMarkDownloads) {
		if(this.bookMarkDownloads == null) {
			this.bookMarkDownloads = bookMarkDownloads;
		} else {
			this.bookMarkDownloads.clear();
			if(bookMarkDownloads != null) {
				for (BookMarkDownload downloadOption : bookMarkDownloads) {
					downloadOption.setParent(this);
					this.bookMarkDownloads.add(downloadOption);
		        }
			}
		}
	}
}



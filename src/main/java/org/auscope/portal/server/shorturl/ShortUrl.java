package org.auscope.portal.server.shorturl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Simple class that stores information(file identifier, service id) about a dataset that is ShortUrl'ed by a user
 * @author san239
 *
 */
@Entity
@Table(name = "shorturl")
public class ShortUrl  implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; /** identifier of the dataset */
    private String url;
    private String name;
    private String timestamp;
    private Boolean persist;
    
    
    public ShortUrl() {
    	super(); 
    	this.timestamp  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    	this.persist = false;
    }
    
    public ShortUrl(String url, String name) {
        super();
        this.setUrl(url);
        this.setName(name);
        this.timestamp  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.persist = false;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getPersist() {
        return persist;
    }

    public void setPersist(Boolean persist) {
        this.persist = persist;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
  
    public String getUrl() {
        return url;
    }

    public void setId(Integer id) {
        this.id = id;        
    }
    
    public Integer getId() {
        return id;       
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp  = timestamp;
    }

    @Override
    public String toString() {
        return "id:" + getId() + ", timestamp:" + getTimestamp() + ", name:" + getName() + ", url:" + getUrl() + ", persist:" + getPersist();
    }
   
}



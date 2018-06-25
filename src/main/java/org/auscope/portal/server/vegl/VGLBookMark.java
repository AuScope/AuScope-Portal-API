package org.auscope.portal.server.vegl;

import java.io.Serializable;

import org.auscope.portal.server.web.security.ANVGLUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VGLBookMark  implements Serializable {	
   
	private static final long serialVersionUID = 8620093753366974702L;
	private String fileIdentifier;
    private String serviceId;
    @JsonIgnore
    private ANVGLUser parent;  
    
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

	
}



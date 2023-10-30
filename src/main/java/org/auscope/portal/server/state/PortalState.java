package org.auscope.portal.server.state;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.auscope.portal.server.web.security.PortalUser;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "states")
public class PortalState implements Serializable {	

	private static final long serialVersionUID = 8983518493963069533L;

	@Id
	@Column(nullable=false)
    private String id;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=true)
	private String description;
	
	@Column(nullable=false)
	private Date creationDate;
	
	@Column(nullable=false)
	private String jsonState;
	
	@Column(nullable=false)
	private boolean isPublic; 
	
	@JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private PortalUser parent;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getJsonState() {
		return jsonState;
	}

	public void setJsonState(String jsonState) {
		this.jsonState = jsonState;
	}
	
	public boolean getIsPublic() {
		return isPublic;
	}
	
	public void setIsPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public PortalUser getParent() {
		return parent;
	}

	public void setParent(PortalUser parent) {
		this.parent = parent;
	}

}

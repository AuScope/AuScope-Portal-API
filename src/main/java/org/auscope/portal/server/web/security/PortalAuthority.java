package org.auscope.portal.server.web.security;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An implementation of GrantedAuthority that can be persisted
 * @author Josh Vote (CSIRO)
 *
 */
@Entity
@Table(name = "authorities")
public class PortalAuthority implements GrantedAuthority {

	private static final long serialVersionUID = 5567656603347809418L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String authority;
    
    // TODO: Is this ManyToOne or ManyToMany... i.e. will a user have multiple authorities?
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private PortalUser parent;

    public PortalAuthority() {
        this(null, null);
    }

    public PortalAuthority(String authority) {
        this(null, authority);
    }

    public PortalAuthority(Integer id, String authority) {
        super();
        this.id = id;
        this.authority = authority;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    /**
     * User that owns this authority
     * @return
     */
    public PortalUser getParent() {
        return parent;
    }

    /**
     * User that owns this authority
     * @param parent
     */
    public void setParent(PortalUser parent) {
        this.parent = parent;
    }
}

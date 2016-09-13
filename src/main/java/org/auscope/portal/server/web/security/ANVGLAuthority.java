package org.auscope.portal.server.web.security;

import org.springframework.security.core.GrantedAuthority;

/**
 * An implementation of GrantedAuthority that can be persisted
 * @author Josh Vote (CSIRO)
 *
 */
public class ANVGLAuthority implements GrantedAuthority {

    private Integer id;
    private String authority;
    private ANVGLUser parent;

    public ANVGLAuthority() {
        this(null, null);
    }

    public ANVGLAuthority(String authority) {
        this(null, authority);
    }

    public ANVGLAuthority(Integer id, String authority) {
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
    public ANVGLUser getParent() {
        return parent;
    }

    /**
     * User that owns this authority
     * @param parent
     */
    public void setParent(ANVGLUser parent) {
        this.parent = parent;
    }
}

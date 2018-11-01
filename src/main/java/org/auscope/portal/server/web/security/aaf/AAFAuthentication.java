package org.auscope.portal.server.web.security.aaf;

import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.aaf.AAFAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by wis056 on 8/04/2015.
 */
public class AAFAuthentication implements Authentication {
    
    private static final long serialVersionUID = -2116084302655572118L;
    private final AAFAttributes attributes;
    private final AAFJWT jwt;
    private boolean isVoid = false;    
    private ANVGLUser principal; 

    public AAFAuthentication(ANVGLUser principal, AAFAttributes attributes, AAFJWT jwt, boolean valid) {
        this.attributes = attributes;
        this.jwt = jwt;
        if (!valid) isVoid = true;
        this.principal = principal;
    }

    @Override
    public AAFJWT getCredentials() {
        return this.jwt;
    }

    @Override
    public AAFAttributes getDetails() {
        return this.attributes;
    }

    @Override
    public ANVGLUser getPrincipal() {
        return principal;
    }
    
    @Override
    public String getName() {
        return this.attributes.email;
    }

    public String getUsername() { return this.getName(); }

    @Override
    public boolean isAuthenticated() {
        return !isVoid;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated)
            throw new IllegalArgumentException();
        isVoid = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<SimpleGrantedAuthority> auths = new ArrayList<>();
        SimpleGrantedAuthority auth = new SimpleGrantedAuthority("ROLE_USER"); // All AAF users are limited to user role.
        auths.add(auth);
        return auths;
    }

    @Override
    public String toString() {
        return "AAFAuthentication{" +
                "attributes=" + attributes +
                ", jwt=" + jwt +
                ", isVoid=" + isVoid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AAFAuthentication that = (AAFAuthentication) o;

        if (isVoid != that.isVoid) return false;
        if (!attributes.equals(that.attributes)) return false;
        return jwt.equals(that.jwt);

    }

    @Override
    public int hashCode() {
        int result = attributes.hashCode();
        result = 31 * result + jwt.hashCode();
        result = 31 * result + (isVoid ? 1 : 0);
        return result;
    }
}

package org.auscope.portal.server.web.security.aaf;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by wis056 on 7/04/2015.
 */
public class AAFAttributes implements UserDetails, Serializable {

    @JsonProperty("cn")
    public String commonName;

    @JsonProperty("mail")
    public String email;

    @JsonProperty("displayname")
    public String displayName;

    @JsonProperty("edupersontargetedid")
    public String targetedID;

    @JsonProperty("edupersonscopedaffiliation")
    public String scopedAffiliation;

    @JsonProperty("edupersonprincipalname")
    public String principalName;

    @JsonProperty("givenname")
    public String givenName;

    @JsonProperty("surname")
    public String surname;

    public String getUsername() {
        return this.email;
    }

    public String getName() { return this.displayName; }

    public AAFAttributes() {

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<SimpleGrantedAuthority> auths = new ArrayList<>();
        SimpleGrantedAuthority auth = new SimpleGrantedAuthority("ROLE_USER"); // All AAF users are limited to user role.
        auths.add(auth);
        return auths;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "AAFAttributes{" +
                "commonName='" + commonName + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", targetedID='" + targetedID + '\'' +
                ", scopedAffiliation='" + scopedAffiliation + '\'' +
                ", principalName='" + principalName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AAFAttributes that = (AAFAttributes) o;

        if (commonName != null ? !commonName.equals(that.commonName) : that.commonName != null) return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (givenName != null ? !givenName.equals(that.givenName) : that.givenName != null) return false;
        if (principalName != null ? !principalName.equals(that.principalName) : that.principalName != null)
            return false;
        if (scopedAffiliation != null ? !scopedAffiliation.equals(that.scopedAffiliation) : that.scopedAffiliation != null)
            return false;
        if (surname != null ? !surname.equals(that.surname) : that.surname != null) return false;
        return !(targetedID != null ? !targetedID.equals(that.targetedID) : that.targetedID != null);

    }

    @Override
    public int hashCode() {
        int result = commonName != null ? commonName.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (targetedID != null ? targetedID.hashCode() : 0);
        result = 31 * result + (scopedAffiliation != null ? scopedAffiliation.hashCode() : 0);
        result = 31 * result + (principalName != null ? principalName.hashCode() : 0);
        result = 31 * result + (givenName != null ? givenName.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        return result;
    }
}

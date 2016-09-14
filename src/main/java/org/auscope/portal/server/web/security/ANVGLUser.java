package org.auscope.portal.server.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Represents a persisted ANVGLUser
 * @author Josh Vote (CSIRO)
 *
 */
public class ANVGLUser implements UserDetails {

    private String id;
    private String fullName;
    private String email;
    private List<ANVGLAuthority> authorities;
    private String arnExecution;
    private String arnStorage;
    private String s3Bucket;
    private String awsSecret;
    private String awsKeyName;
    private Integer acceptedTermsConditions;

    public ANVGLUser() {
        this.authorities = new ArrayList<>();
    }

    public ANVGLUser(String id, String fullName, String email, List<ANVGLAuthority> authorities) {
        super();
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.authorities = authorities;
    }

    /**
     * Gets the ID as reported by the remote authentication service (Probably google)
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID as reported by the remote authentication service (Probably google)
     * @return
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the AWS S3 bucket where this user's job data will be written
     * @return
     */
    public String getS3Bucket() {
        return s3Bucket;
    }

    /**
     * The name of the AWS S3 bucket where this user's job data will be written
     * @param s3Bucket
     */
    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    /**
     * Gets a string representing the full name of the user
     *
     * @return
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets a string representing the full name of the user
     *
     * @param fullName
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets a contact email for this user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets a contact email for this user
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * The keyname to be used for VMs started by this user (can be null)
     * @return
     */
    public String getAwsKeyName() {
        return awsKeyName;
    }

    /**
     * The keyname to be used for VMs started by this user (can be null)
     * @param awsKeyName
     */
    public void setAwsKeyName(String awsKeyName) {
        this.awsKeyName = awsKeyName;
    }

    /**
     * The version of the T&Cs that the user has last accepted (or null if none)
     * @return
     */
    public Integer getAcceptedTermsConditions() {
        return acceptedTermsConditions;
    }

    /**
     * The version of the T&Cs that the user has last accepted (or null if none)
     * @return
     */
    public void setAcceptedTermsConditions(Integer acceptedTermsConditions) {
        this.acceptedTermsConditions = acceptedTermsConditions;
    }

    @Override
    public String toString() {
        return "ANVGLUser [id=" + id + ", fullName=" + fullName + ", authorities=" + authorities + "]";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<ANVGLAuthority> authorities) {
        this.authorities = authorities;
        for (ANVGLAuthority auth : authorities) {
            auth.setParent(this);
        }
    }

    public String getArnExecution() {
        return arnExecution;
    }

    public void setArnExecution(String arnExecution) {
        this.arnExecution = arnExecution;
    }

    public String getArnStorage() {
        return arnStorage;
    }

    public void setArnStorage(String arnStorage) {
        this.arnStorage = arnStorage;
    }

    public String getAwsSecret() {
        return awsSecret;
    }

    public void setAwsSecret(String awsSecret) {
        this.awsSecret = awsSecret;
    }

    /**
     * Returns true iff this ANVGLUser instance has all the relevant fields set that are required for
     * submitting an AWS job. Returning false would indicate that the user has more data to enter before
     * they can begin submitting jobs.
     */
    public boolean isFullyConfigured() {
        return StringUtils.isNotEmpty(arnStorage) &&
                StringUtils.isNotEmpty(awsSecret) &&
                StringUtils.isNotEmpty(arnExecution) &&
                acceptedTermsConditions != null &&
                acceptedTermsConditions > 0;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

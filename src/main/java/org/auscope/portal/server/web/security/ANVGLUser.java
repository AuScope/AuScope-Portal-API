package org.auscope.portal.server.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.server.vegl.VGLBookMark;
import org.auscope.portal.server.web.controllers.BaseCloudController;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Represents a persisted ANVGLUser
 * @author Josh Vote (CSIRO)
 *
 */
public class ANVGLUser implements UserDetails {

    // Authentication frameworks
    public enum AuthenticationFramework { GOOGLE, AAF }

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
    private AuthenticationFramework authentication;
    
    /** A List of book marks associated with the user */
    private List<VGLBookMark> bookMarks;

    public ANVGLUser() {
        this.authorities = new ArrayList<>();
        this.bookMarks =  new ArrayList<>();
    }

    public ANVGLUser(String id, String fullName, String email, List<ANVGLAuthority> authorities) {
        super();
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.authorities = authorities;
    } 
    

    /**
     * Gets the ID as reported by the remote authentication service (Probably google).
     * AAF doesn't return a unique ID so we use the user's email address in this case.
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID as reported by the remote authentication service (Probably google).
     * AAF doesn't return a unique ID so we use the user's email address in this case.
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

    public AuthenticationFramework getAuthentication() {
        return this.authentication;
    }

    public void setAuthentication(AuthenticationFramework authentication) {
        this.authentication = authentication;
    }
        
    /**
     * gets a list of book marks
     * @return
     */

	public List<VGLBookMark> getBookMarks() {
		return bookMarks;
	}
	/**
	 * sets the list of book marks
	 * @param bookMarks
	 */

	public void setBookMarks(List<VGLBookMark> bookMarks) {
		this.bookMarks = bookMarks;
		for (VGLBookMark bookmark : bookMarks) {
			bookmark.setParent(this);
        }
	}

    /**
     * Returns true iff this ANVGLUser instance has accepted the latest version of the terms and conditions.
     */
    // Carsten VGL-208: this method can not be named isAcceptedTermsConditions() or hasAcceptedTermsConditions()
    //                  as this can non-deterministicly cause hibernate to think it is a boolean - which causes all kinds of
    //                  type casting / mismatch issues.
    // https://jira.csiro.au/browse/VGL-208
    public boolean acceptedTermsConditionsStatus() {
        // Carsten
        return acceptedTermsConditions != null &&
                acceptedTermsConditions > 0;
    }

    /**
     * Returns true iff this ANVGLUser instance has at least 1 compute service
     * which has been properly configured.
     *
     * @param nciDetailsDao
     * @param cloudComputeServices
     * @return
     * @throws PortalServiceException
     */
    public boolean configuredServicesStatus(NCIDetailsDao nciDetailsDao, CloudComputeService[] cloudComputeServices) throws PortalServiceException {
        return !BaseCloudController.getConfiguredComputeServices(this, nciDetailsDao, cloudComputeServices).isEmpty();
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

package org.auscope.portal.server.web.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.server.vegl.VGLBookMark;
import org.auscope.portal.server.web.controllers.BaseCloudController;
import org.auscope.portal.server.web.service.NCIDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Represents a persisted ANVGLUser
 * @author Josh Vote (CSIRO)
 *
 */
@Entity
@Table(name = "users")
public class ANVGLUser implements UserDetails, Serializable {

	private static final long serialVersionUID = -8923427161200232245L;

	// Authentication frameworks
    public enum AuthenticationFramework { GOOGLE, AAF }

    @Id
    private String id;
    private String fullName;
    private String email;
    
    @OneToMany(mappedBy = "parent", cascade=CascadeType.ALL, orphanRemoval = true)
    private List<ANVGLAuthority> authorities;
    
    private String arnExecution;
    private String arnStorage;
    private String s3Bucket;
    private String awsSecret;
    private String awsKeyName;
    private Integer acceptedTermsConditions;
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private NCIDetailsEnc nciDetailsEnc;
    
    @Transient
    private AuthenticationFramework authentication;
    
    /** A List of book marks associated with the user */
    @OneToMany(mappedBy = "parent",	cascade=CascadeType.ALL, orphanRemoval = true)
    private List<VGLBookMark> bookMarks;

    public ANVGLUser() {
        this.authorities = new ArrayList<>();
        this.bookMarks =  new ArrayList<>();
    }

    public ANVGLUser(String id, String fullName, String email,
    		List<ANVGLAuthority> authorities, List<VGLBookMark> bookMarks) {
        super();
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.authorities = authorities;
        this.bookMarks = bookMarks;
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

    /**
     * 
     * @return
     */
    public NCIDetailsEnc getNciDetailsEnc() {
		return nciDetailsEnc;
	}

    /**
     * 
     * @param nciDetails
     */
	public void setNciDetailsEnc(NCIDetailsEnc nciDetails) {
		this.nciDetailsEnc = nciDetails;
	}

	@Override
    public String toString() {
		String strId = id == null ? "null" : id;
		String fnStr = fullName == null ? "null" : fullName;
		String authStr = authorities == null ? "null" : authorities.toString();
		
        return "ANVGLUser [id=" + strId + ", fullName=" + fnStr + ", authorities=" + authStr + "]";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return (authorities != null) ? authorities : new ArrayList<ANVGLAuthority>();
    }

    public void setAuthorities(List<ANVGLAuthority> authorities) {
    	if(this.authorities == null) {
    		this.authorities = authorities;
    	} else {
	        this.authorities.clear();
	        if(authorities != null) {
		        for (ANVGLAuthority auth : authorities) {
		            auth.setParent(this);
		            this.authorities.add(auth);
		        }
	        }
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
		return (bookMarks != null) ? bookMarks : new ArrayList<VGLBookMark>();
	}
	
	/**
	 * sets the list of book marks
	 * @param bookMarks
	 */

	public void setBookMarks(List<VGLBookMark> bookMarks) {
		if(this.bookMarks == null) {
			this.bookMarks = bookMarks;
		} else {
			this.bookMarks.clear();
			for (VGLBookMark bookmark : bookMarks) {
				bookmark.setParent(this);
				this.bookMarks.add(bookmark);
	        }
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
     * @param nciDetailsService
     * @param cloudComputeServices
     * @return
     * @throws PortalServiceException
     */
    public boolean configuredServicesStatus(NCIDetailsService nciDetailsService, CloudComputeService[] cloudComputeServices) throws PortalServiceException {
        return !BaseCloudController.getConfiguredComputeServices(this, nciDetailsService, cloudComputeServices).isEmpty();
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

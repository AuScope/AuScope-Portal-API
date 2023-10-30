package org.auscope.portal.server.web.security;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "nci_details")
public class NCIDetailsEnc implements Serializable {

	private static final long serialVersionUID = -1617534178282032823L;
	
	public final static String PROPERTY_NCI_USER = "nci_user";
    public final static String PROPERTY_NCI_KEY = "nci_key";
    public final static String PROPERTY_NCI_PROJECT = "nci_project";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    private PortalUser user;
    @Column(name="nci_username")
    private byte[] username;
    @Column(name="nci_project")
    private byte[] project;
    @Column(name="nci_key")
    private byte[] key;

    public NCIDetailsEnc() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * The associated PortalUser
     * @return
     */
    public PortalUser getUser() {
        return user;
    }

    /**
     * The associated PortalUser
     * @param user
     */
    public void setUser(PortalUser user) {
        this.user = user;
    }

    /**
     * The user's NCI username (encrypted)
     * @return
     */
    public byte[] getUsername() {
        return this.username;
    }

    /**
     * The user's NCI username (encrypted)
     * @param nciUsername
     */
    public void setUsername(byte[] username) {
        this.username = username;
    }

    /**
     * The default project for the NCI user (encrypted)
     * @return
     */
    public byte[] getProject() {
        return project;
    }

    /**
     * The default project for the NCI user (encrypted)
     * @param project
     */
    public void setProject(byte[] project) {
        this.project = project;
    }

    /**
     * The user's NCI key (encrypted)
     * @return
     */
    public byte[] getKey() {
        return this.key;
    }

    /**
     * The user's NCI key (encrypted)
     * @param nciUsername
     */
    public void setKey(byte[] key) {
        this.key = key;
    }

}

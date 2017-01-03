package org.auscope.portal.server.web.security;

import java.io.Serializable;

public class NCIDetailsEnc implements Serializable {
    public final static String PROPERTY_NCI_USER = "nci_user";
    public final static String PROPERTY_NCI_KEY = "nci_key";
    public final static String PROPERTY_NCI_PROJECT = "nci_project";

    private Integer id;
    private ANVGLUser user;
    private byte[] username;
    private byte[] project;
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
     * The associated ANVGLUSer
     * @return
     */
    public ANVGLUser getUser() {
        return user;
    }

    /**
     * The associated ANVGLUSer
     * @param user
     */
    public void setUser(ANVGLUser user) {
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

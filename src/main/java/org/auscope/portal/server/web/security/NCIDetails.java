package org.auscope.portal.server.web.security;

import java.io.Serializable;

import org.auscope.portal.core.cloud.CloudJob;


public class NCIDetails implements Serializable {

    private static final long serialVersionUID = -7219385540898450290L;

    public final static String PROPERTY_NCI_USER = "nci_user";
    public final static String PROPERTY_NCI_KEY = "nci_key";
    public final static String PROPERTY_NCI_PROJECT = "nci_project";

    private Integer id;
    private ANVGLUser user;
    private String username;
    private String project;
    private String key;

    public NCIDetails() {
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
    public String getUsername() {
        return this.username;
    }

    /**
     * The user's NCI username (encrypted)
     * @param nciUsername
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * The default project for the NCI user (encrypted)
     * @return
     */
    public String getProject() {
        return project;
    }

    /**
     * The default project for the NCI user (encrypted)
     * @param project
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * The user's NCI key (encrypted)
     * @return
     */
    public String getKey() {
        return this.key;
    }

    /**
     * The user's NCI key (encrypted)
     * @param nciUsername
     */
    public void setKey(String key) {
        this.key = key;
    }

    public void applyToJobProperties(CloudJob job) throws Exception {
        job.setProperty(PROPERTY_NCI_USER, getUsername());
        job.setProperty(PROPERTY_NCI_PROJECT, getProject());
        job.setProperty(PROPERTY_NCI_KEY, getKey());
    }
}

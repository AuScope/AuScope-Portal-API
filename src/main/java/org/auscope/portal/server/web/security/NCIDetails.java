package org.auscope.portal.server.web.security;

import java.io.Serializable;

public class NCIDetails implements Serializable {

    private static final long serialVersionUID = -7219385540898450290L;
    
    private Integer id;
    private ANVGLUser user;
    private String username;
    private String project;
    private String key;
    
    public NCIDetails(ANVGLUser user, String username, String project, String key) {
        this.user = user;
        this.username = username;
        this.project = project;
        this.key = key;
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
     * The user's NCI username
     * @return
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * The user's NCI username
     * @param nciUsername
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * The default project for the NCI user
     * @return
     */
    public String getProject() {
        return project;
    }

    /**
     * The default project for the NCI user
     * @param project
     */
    public void setProject(String project) {
        this.project = project;
    }
    
    /**
     * The user's NCI key
     * @return
     */
    public String getKey() {
        return this.key;
    }
    
    /**
     * The user's NCI key
     * @param nciUsername
     */
    public void setKey(String key) {
        this.key = key;
    }

}

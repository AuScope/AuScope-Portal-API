package org.auscope.portal.server.web.security;

import java.io.Serializable;

/**
 * Simple class that stores information about a user
 *
 * @author Abdi Jama
 */
public class User implements Serializable {

    private String  id;
    private String  password;
    private boolean  enabled;

    /**
     * Default constructor.
     */
    public User() {
        id = password = "";
        enabled = true;
    }



    /**
	 * @return the username
	 */
	public String getId() {
		return id;
	}



	/**
	 * @param username the username to set
	 */
	public void setId(String id) {
		this.id = id;
	}



	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}



	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}



	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}



	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}



	/**
     * Returns a String representing the state of this <code>User</code>
     * object.
     *
     * @return A summary of the values of this object's fields
     */
    public String toString() {
        return super.toString() +
               ",username=\"" + id + "\"" +
               ",password=\"" + password + "\"" +
               ",enabled=\"" + enabled + "\"";
    }
}


package org.auscope.portal.server.web.security;

import java.io.Serializable;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.auscope.portal.core.cloud.CloudJob;

public class NCIDetails implements Serializable {

    private static final long serialVersionUID = -7219385540898450290L;

    public final static String PROPERTY_NCI_USER = "nci_user";
    public final static String PROPERTY_NCI_KEY = "nci_key";
    public final static String PROPERTY_NCI_PROJECT = "nci_project";

    private Integer id;
    private ANVGLUser user;
    private byte[] username;
    private byte[] project;
    private byte[] key;

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
     * Sets the user's NCI username from an unencrypted username. Encrypts using keyString
     * @param keyString The actu
     * @throws Exception
     */
    public void setUnencryptedUsername(String keyString, String username) throws Exception {
        this.username = encrypt(keyString, username);
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
     * Sets the user's NCI project from an unencrypted project. Encrypts using keyString
     * @param keyString The actu
     * @throws Exception
     */
    public void setUnencryptedProject(String keyString, String project) throws Exception {
        this.project = encrypt(keyString, project);
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

    /**
     * Sets the user's NCI key from an unencrypted keyValue. Encrypts using keyString
     * @param keyString The actual encryption key to be used
     * @param keyValue The unencrypted key value to be encrypted using keyString and persisted
     * @throws Exception
     */
    public void setUnencryptedKey(String keyString, String keyValue) throws Exception {
        this.key = encrypt(keyString, keyValue);
    }

    private byte[] encrypt(String keyString, String message) throws Exception {
        Key key = new SecretKeySpec(keyString.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(message.getBytes());
    }

    private String decrypt(String keyString, byte[] encryptedText) throws Exception {
        Key key = new SecretKeySpec(keyString.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decValue = c.doFinal(encryptedText);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    public void applyToJobProperties(CloudJob job, String keyString) throws Exception {
        job.setProperty(PROPERTY_NCI_USER, decrypt(keyString, getUsername()));
        job.setProperty(PROPERTY_NCI_PROJECT, decrypt(keyString, getProject()));
        job.setProperty(PROPERTY_NCI_KEY, decrypt(keyString, getKey()));
    }
}

/**
 * 
 */
package org.auscope.portal.core.services.cloud;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

/**
 * @author fri096
 *
 */
public class IdentityString implements Identity {

    private KeyPair kpair;

    public IdentityString(JSch jsch, String prvkey) throws JSchException {
        kpair = KeyPair.load(jsch, prvkey.getBytes(), null);
    }

    /* (non-Javadoc)
     * @see com.jcraft.jsch.Identity#setPassphrase(byte[])
     */
    @Override
    public boolean setPassphrase(byte[] passphrase) throws JSchException {
        return kpair.decrypt(passphrase);
    }

    /* (non-Javadoc)
     * @see com.jcraft.jsch.Identity#getPublicKeyBlob()
     */
    @Override
    public byte[] getPublicKeyBlob() {
        return kpair.getPublicKeyBlob();
    }

    /* (non-Javadoc)
     * @see com.jcraft.jsch.Identity#getSignature(byte[])
     */
    @Override
    public byte[] getSignature(byte[] data) {
        return kpair.getSignature(data);
    }

    /* (non-Javadoc)
     * @see com.jcraft.jsch.Identity#decrypt()
     */
    @Override
    public boolean decrypt() {
        throw new RuntimeException("not implemented");
    }

    /* (non-Javadoc)
     * @see com.jcraft.jsch.Identity#getAlgName()
     */
    @Override
    public String getAlgName() {
        return "ssh-rsa";
//        return new String(kpair.getKeyTypeName());
    }

    /* (non-Javadoc)
     * @see com.jcraft.jsch.Identity#getName()
     */
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.jcraft.jsch.Identity#isEncrypted()
     */
    @Override
    public boolean isEncrypted() {
        return kpair.isEncrypted();
    }

    /* (non-Javadoc)
     * @see com.jcraft.jsch.Identity#clear()
     */
    @Override
    public void clear() {
        kpair.dispose();
        kpair = null;
    }

}

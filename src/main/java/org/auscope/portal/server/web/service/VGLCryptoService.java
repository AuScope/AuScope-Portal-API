package org.auscope.portal.server.web.service;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.PortalServiceException;

/**
 * Created by Carsten Friedrich (fri096) for VGL.
 *
 * A Service for basic encryption and decryption
 */

public class VGLCryptoService {
    /** Logger for this class. */
    private static final Log LOGGER = LogFactory.getLog(VGLCryptoService.class);

    public static final String SECRET_KEY_SPEC = "AES";
    public static final String CIPHER = "AES/CBC/PKCS5Padding";
    public static final String PASSWORD_BASED_ALGO = "PBKDF2WithHmacSHA1";
    public static final int KEY_SIZE = 128;
    public static final int CRYPTO_ITERATIONS = 1024;
    
    private String encryptionPassword;

    public VGLCryptoService(String encryptionPassword) throws PortalServiceException {
        if(encryptionPassword==null || encryptionPassword.isEmpty())
            throw new PortalServiceException("Configuration parameter env.encryption.password must not be empty!");
        this.encryptionPassword=encryptionPassword;
    }

    public static byte[] generateSalt(int size) {
        byte[] res = new byte[size];
        SecureRandom r = new SecureRandom();
        r.nextBytes(res);
        return res;
      }

    public String decrypt(byte[] data)
            throws PortalServiceException {
        try {
            String cryptoString = new String(data, StandardCharsets.UTF_8);
            String[] cyptoInfo = cryptoString.split("@");
            if(cyptoInfo.length!=3) 
                throw new PortalServiceException("Invalid crypto info: "+cryptoString);
            
            SecretKeyFactory kf = SecretKeyFactory.getInstance(PASSWORD_BASED_ALGO);
            PBEKeySpec keySpec = new PBEKeySpec(encryptionPassword.toCharArray(), Base64.getDecoder().decode(cyptoInfo[0]),
                    CRYPTO_ITERATIONS, KEY_SIZE);
            SecretKey tmp = kf.generateSecret(keySpec);

            byte[] endcoded = tmp.getEncoded();
            SecretKey key = new SecretKeySpec(endcoded, SECRET_KEY_SPEC);

            Cipher ciph = Cipher.getInstance(CIPHER);

            ciph.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(Base64.getDecoder().decode(cyptoInfo[1])));
            return  new String(ciph.doFinal(Base64.getDecoder().decode(cyptoInfo[2])), StandardCharsets.UTF_8);

        } catch (GeneralSecurityException e) {
            throw new PortalServiceException("Decryption error: " + e.getMessage(), e);
        }
    }

    public byte[] encrypt(String dataStr) throws PortalServiceException {
        try {
            byte[] salt = generateSalt(8);
            SecretKeyFactory kf = SecretKeyFactory.getInstance(PASSWORD_BASED_ALGO);

            PBEKeySpec keySpec = new PBEKeySpec(encryptionPassword.toCharArray(), salt, CRYPTO_ITERATIONS,
                    KEY_SIZE);

            SecretKey tmp = kf.generateSecret(keySpec);
            SecretKey key = new SecretKeySpec(tmp.getEncoded(), SECRET_KEY_SPEC);

            Cipher ciph = Cipher.getInstance(CIPHER);

            ciph.init(Cipher.ENCRYPT_MODE, key);
            AlgorithmParameters params = ciph.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

            byte[] cipherText = ciph.doFinal(dataStr.getBytes(StandardCharsets.UTF_8));
            String resultString = Base64.getEncoder().encodeToString(salt) + "@" +
                    Base64.getEncoder().encodeToString(iv) + "@" +
                    Base64.getEncoder().encodeToString(cipherText);
            return resultString.getBytes(StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new PortalServiceException("Encryption error: " + e.getMessage(), e);
        }
    }

}

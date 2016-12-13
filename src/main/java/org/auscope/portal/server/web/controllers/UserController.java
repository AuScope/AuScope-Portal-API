package org.auscope.portal.server.web.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.security.ANVGLUserDao;
import org.auscope.portal.server.web.security.NCIDetails;
import org.auscope.portal.server.web.security.NCIDetailsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller class for accessing/modifying user metadata
 * @author Josh Vote
 */
@Controller
public class UserController extends BasePortalController {
    private static final String CLOUD_FORMATION_RESOURCE = "org/auscope/portal/server/web/controllers/vl-cloudformation.json.tpl";

    protected final Log logger = LogFactory.getLog(getClass());

    private ANVGLUserDao userDao;
    private NCIDetailsDao nciDetailsDao;
    private VelocityEngine velocityEngine;

    private String awsAccount;

    private String tacVersion;
    
    private String encryptionPassword;

    @Autowired
    public UserController(ANVGLUserDao userDao, NCIDetailsDao nciDetailsDao,
            VelocityEngine velocityEngine, 
            @Value("${env.aws.account}") String awsAccount,
            @Value("${termsconditions.version}") String tacVersion,
            @Value("${env.encryption.password}") String encryptionPassword) throws PortalServiceException {
        super();
        
        if(encryptionPassword==null || encryptionPassword.isEmpty())
            throw new PortalServiceException("Configuration parameter env.encryption.password must not be empty!");
        this.userDao = userDao;
        this.nciDetailsDao = nciDetailsDao;
        this.velocityEngine = velocityEngine;
        this.awsAccount=awsAccount;
        this.tacVersion=tacVersion;
        this.encryptionPassword = encryptionPassword;
    }


    /**
     * Gets user metadata for the currently logged in user
     * @param user
     * @return
     */
    @RequestMapping("/secure/getUser.do")
    public ModelAndView getUser(@AuthenticationPrincipal ANVGLUser user) {
        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        ModelMap userObj = new ModelMap();
        userObj.put("id", user.getId());
        userObj.put("email", user.getEmail());
        userObj.put("fullName", user.getFullName());
        userObj.put("arnExecution", user.getArnExecution());
        userObj.put("arnStorage", user.getArnStorage());
        userObj.put("acceptedTermsConditions", user.getAcceptedTermsConditions());
        userObj.put("awsKeyName", user.getAwsKeyName());

        return generateJSONResponseMAV(true, userObj, "");
    }

    /**
     * Sets a variety of parameters for the currently logged in user
     * @param user
     * @param arnExecution
     * @param arnStorage
     * @param acceptedTermsConditions
     * @return
     */
    @RequestMapping("/secure/setUser.do")
    public ModelAndView setUser(@AuthenticationPrincipal ANVGLUser user,
            @RequestParam(required=false, value="arnExecution") String arnExecution,
            @RequestParam(required=false, value="arnStorage") String arnStorage,
            @RequestParam(required=false, value="acceptedTermsConditions") Integer acceptedTermsConditions,
            @RequestParam(required=false, value="awsKeyName") String awsKeyName) {

        if (user == null) {
            return generateJSONResponseMAV(false);
        }

        boolean modified = false;
        if (!StringUtils.isEmpty(arnExecution)) {
            user.setArnExecution(arnExecution);
            modified = true;
        }

        if (!StringUtils.isEmpty(arnStorage)) {
            user.setArnStorage(arnStorage);
            modified = true;
        }

        if (acceptedTermsConditions != null) {
            user.setAcceptedTermsConditions(acceptedTermsConditions);
            modified = true;
        }

        if (!StringUtils.equals(user.getAwsKeyName(), awsKeyName)) {
            user.setAwsKeyName(awsKeyName);
            modified = true;
        }

        if (modified) {
            userDao.save(user);
        }

        return generateJSONResponseMAV(true);
    }

    @RequestMapping("/getTermsConditions.do")
    public ModelAndView getTermsConditions(@AuthenticationPrincipal ANVGLUser user) {

        try {
            String tcs = IOUtils.toString(this.getClass().getResourceAsStream("vl-termsconditions.html"));

            ModelMap response = new ModelMap();
            response.put("html", tcs);
            response.put("currentVersion", Integer.parseInt(tacVersion));
            if (user != null) {
                response.put("acceptedVersion", user.getAcceptedTermsConditions());
            }

            return generateJSONResponseMAV(true, response, "");
        } catch (IOException ex) {
            log.error("Unable to read terms and conditions resource", ex);
            return generateJSONResponseMAV(false);
        }
    }

    @RequestMapping("/secure/getCloudFormationScript.do")
    public void getCloudFormationScript(@AuthenticationPrincipal ANVGLUser user, HttpServletResponse response) throws IOException {
        if (user == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("s3Bucket", user.getS3Bucket());
        model.put("awsSecret", user.getAwsSecret());
        model.put("awsAccount", awsAccount);

        String cloudFormationScript = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, CLOUD_FORMATION_RESOURCE, "UTF-8", model);

        response.setContentType("application/octet");
        response.setHeader("Content-Disposition", "inline; filename=anvgl-cloudformation.json;");

        try {
            response.getOutputStream().write(cloudFormationScript.getBytes(Charsets.UTF_8));
        } catch (IOException e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    
    @RequestMapping("/secure/getNCIDetails.do")
    public ModelAndView getNCIDetails(@AuthenticationPrincipal ANVGLUser user) {
        if (user == null) {
            return generateJSONResponseMAV(false);
        }
        ModelMap detailsObj = new ModelMap();
        NCIDetails details = nciDetailsDao.getByUser(user);
        if(details != null) {
            try {
                detailsObj.put("nciUsername", decrypt(details.getUsername()));
                detailsObj.put("nciProject", decrypt(details.getProject()));
                detailsObj.put("nciKey", decrypt(details.getKey()));
            } catch(Exception e) {
                logger.error("Unable to decrypt NCI details: " + e.getLocalizedMessage());
            }
            return generateJSONResponseMAV(true, detailsObj, "");
        }
        return generateJSONResponseMAV(false);
    }
    
    @RequestMapping("/secure/setNCIDetails.do")
    public ModelAndView setNCIDetails(@AuthenticationPrincipal ANVGLUser user,
            @RequestParam(required=false, value="nciUsername") String username,
            @RequestParam(required=false, value="nciProject") String project,
            @RequestParam(required=false, value="nciKey") CommonsMultipartFile key) {

        if (user == null) {
            return generateJSONResponseMAV(false);
        }
        NCIDetails details = nciDetailsDao.getByUser(user);
        if(details == null) {
            details = new NCIDetails();
            details.setUser(user);
        }
        boolean modified = false;
        try {
            if (!StringUtils.isEmpty(username) || !StringUtils.equals(decrypt(details.getUsername()), username)) {
                details.setUsername(encrypt(username));
                modified = true;
            }
            if (!StringUtils.isEmpty(project) || !StringUtils.equals(decrypt(details.getProject()), project)) {
                details.setProject(encrypt(project));
                modified = true;
            }
            if (key != null ) {
                String keyString = key.getFileItem().getString();
                if (!StringUtils.isEmpty(keyString) || !StringUtils.equals(decrypt(details.getKey()), keyString)) {            
                    details.setKey(encrypt(keyString));
                    modified = true;
                }
            }
        } catch(Exception e) {
            logger.error(e.getLocalizedMessage());
        }
            
        if (modified) {
            nciDetailsDao.save(details);
        }
        return generateJSONResponseMAV(true);        
    }
    
    public static final String SECRET_KEY_SPEC = "AES";
    public static final String CIPHER = "AES/CBC/PKCS5Padding";
    public static final String PASSWORD_BASED_ALGO = "PBKDF2WithHmacSHA1";
    public static final int KEY_SIZE = 128;
    public static final int CRYPTO_ITERATIONS = 1024;

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
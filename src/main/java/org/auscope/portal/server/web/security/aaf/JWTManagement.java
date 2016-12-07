package org.auscope.portal.server.web.security.aaf;

import com.fasterxml.jackson.databind.ObjectMapper;
//import org.auscope.portal.server.web.security.aaf.VFSException;
import org.auscope.portal.server.web.security.aaf.AAFAuthentication;
import org.auscope.portal.server.web.security.aaf.AAFJWT;
import org.auscope.portal.server.web.security.aaf.AAFAttributes;
//import org.auscope.portal.server.web.security.aaf.CryptographicToolBox;
//import org.auscope.portal.server.web.security.aaf.PKCS8GeneratorCF;
import org.apache.log4j.Logger;
//import org.bouncycastle.openssl.PEMWriter;
//import org.bouncycastle.util.io.pem.PemObject;
//import org.bouncycastle.util.io.pem.PemWriter;
//import org.spongycastle.util.io.pem.PemObject;
//import org.spongycastle.util.io.pem.PemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
//import java.io.StringWriter;
import java.security.*;
//import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wis056 on 7/04/2015.
 */

@Component
public class JWTManagement {
    final static Logger logger = Logger.getLogger(JWTManagement.class);
    final private String INSERT = "INSERT INTO aafreplay (token) values (?);";
    final String AAF_INSERT = "insert into users (username, public_key, private_key, isaaf, enabled, password) values (?, ?, ?, ?, ?, ?);";
    final String AAF_ROLES = "insert into authorities (username, authority) values (?, ?);";
    final String KEY_GET = "select public_key is not null from users where username=?;";

    private String jwtSecret;
    private String rootServiceUrl;

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public void setRootServiceUrl(String rootServiceUrl) {
        this.rootServiceUrl = rootServiceUrl;
    }

    /* XXX
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    */

    static private String AAF_PRODUCTION = "https://rapid.aaf.edu.au";
    static private String AAF_TEST = "https://rapid.test.aaf.edu.au";

    public AAFAuthentication parseJWT(String tokenString) throws AuthenticationException {
        if (tokenString == null)
            throw new AuthenticationCredentialsNotFoundException("Unable to authenticate. No AAF credentials found.");

        Jwt jwt = JwtHelper.decodeAndVerify(tokenString, new MacSigner(jwtSecret.getBytes()));

        String claims = jwt.getClaims();
        logger.debug(claims);

        ObjectMapper mapper = new ObjectMapper();
        try {
            AAFJWT token = mapper.readValue(claims, AAFJWT.class);

            if (!(token.aafServiceUrl.equals(AAF_PRODUCTION) || token.aafServiceUrl.equals(AAF_TEST)))
                throw new AuthenticationServiceException("Unable to authenticate. The AAF URL does not match the expected " +
                        "value for the test or production AAF Rapid Connect services. Expected " + AAF_PRODUCTION +
                        " or " + AAF_TEST + " but got " + token.aafServiceUrl);

            if (!(token.localServiceUrl.equals(rootServiceUrl)))
                throw new AuthenticationServiceException("Unable to authenticate. The URL of this server, "
                        + rootServiceUrl +
                        " does not match the URL registered with AAF " + token.localServiceUrl + " .");

            Date now = new Date();
            if (now.before(token.notBefore))
                throw new AuthenticationServiceException("Unable to authenticate. The authentication is " +
                        "marked to not be used before the current date, " + now.toString());

            if (token.expires.before(now))
                throw new AuthenticationServiceException("Unable to authenticate. The authentication has expired. " +
                        "Now: " + now.toString() + " expired: " + token.expires.toString() );

            /* XXX
            try {
                this.jdbcTemplate.update(INSERT, token.replayPreventionToken);
            } catch (DataAccessException e) {
                logger.error(e);
                throw new AuthenticationServiceException("Unable to authenticate. The replay attack prevention " +
                        "token already exists, so this is probably a replay attack.");
            }
            */

            boolean didAdd = registerAAFUser(token.attributes);

            logger.debug("First login for AAF user? " + Boolean.toString(didAdd));

            return new AAFAuthentication(token.attributes, token, true);

        } catch (IOException e) {
            throw new AuthenticationServiceException(e.getLocalizedMessage());
        }
    }

    private boolean checkUserExists(String username) {
        boolean hasKey = false;

        RowMapper<Boolean> mapper = new RowMapper<Boolean>() {
            public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getBoolean(1);
            }
        };

        /* XXX
        List<Boolean> keys = this.jdbcTemplate.query(KEY_GET, mapper, username);

        if(!keys.isEmpty()) {
            hasKey = keys.get(0);
        }
        */

        return hasKey;
        
    }

    private boolean registerAAFUser(AAFAttributes attributes) {
        boolean didRegister = false;

        boolean hasKey = checkUserExists(attributes.email);
        logger.debug("User " + attributes.email + " has public key? " + Boolean.toString(hasKey));
        if (!hasKey) {
            try {
                List<String> keypair = generateKeypair(attributes.email);
                String publicKey = keypair.get(0);
                String privateKey = keypair.get(1);
                /* XXX
                this.jdbcTemplate.update(AAF_INSERT, attributes.email, publicKey, privateKey, true, true, "");
                this.jdbcTemplate.update(AAF_ROLES, attributes.email, "ROLE_USER");
                */
                didRegister = true;
            } catch (IOException e) {
                logger.error(e.getMessage());
                // return false
            }
        }
        return didRegister;
    }

    // XXX Fix security
    private List<String> generateKeypair(String username) throws IOException {
        try {
            KeyPairGenerator keyGenerator;
            keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(4096);
            KeyPair keypair = keyGenerator.generateKeyPair();
            //PrivateKey privateKey = keypair.getPrivate();
            // XXX
            return new ArrayList<String>() {{
                add(keypair.getPublic().toString());
                add(keypair.getPrivate().toString());
            }};
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
            /*
            StringWriter stringer = new StringWriter();
            PemObject pemKey = new PKCS8GeneratorCF(privateKey).generate();
            try (PemWriter writer = new PemWriter(stringer)) {
                writer.writeObject(pemKey);
            }
            final String privateString = stringer.toString();

            X509Certificate certificate = CryptographicToolBox.generateCertificate(keypair, username);
            StringWriter sw = new StringWriter();
            try (PEMWriter pw = new PEMWriter(sw)) {
                pw.writeObject(certificate);
            }
            final String publicString = sw.toString();

            return new ArrayList<String>() {{
                add(publicString);
                add(privateString);
            }};

        } catch (NoSuchAlgorithmException | VFSException e) {
            throw new IOException(e);
        }
        */
    }
}

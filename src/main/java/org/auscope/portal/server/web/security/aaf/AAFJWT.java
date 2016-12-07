package org.auscope.portal.server.web.security.aaf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.auscope.portal.server.web.security.aaf.AAFAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by wis056 on 8/04/2015.
 */
public class AAFJWT implements Serializable {
    @JsonProperty("iss")
    public String aafServiceUrl;

    @JsonProperty("aud")
    public String localServiceUrl;

    @JsonProperty("nbf")
    @JsonDeserialize(using=UnixTimestampDeserializer.class)
    public Date notBefore;

    @JsonProperty("exp")
    @JsonDeserialize(using=UnixTimestampDeserializer.class)
    public Date expires;

    @JsonProperty("jti")
    public String replayPreventionToken;

    @JsonProperty("https://aaf.edu.au/attributes")
    public AAFAttributes attributes;

    @JsonProperty("iat")
    @JsonDeserialize(using=UnixTimestampDeserializer.class)
    public Date issuedAt;

    @JsonProperty("typ")
    public String claimType;

    @JsonProperty("sub")
    public String targetedID;



    public AAFJWT() {

    }

    @Override
    public String toString() {
        return "AAFJWT{" +
                "aafServiceUrl='" + aafServiceUrl + '\'' +
                ", localServiceUrl='" + localServiceUrl + '\'' +
                ", notBefore=" + notBefore +
                ", expires=" + expires +
                ", replayPreventionToken='" + replayPreventionToken + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}

class UnixTimestampDeserializer extends JsonDeserializer<Date> {
    Logger logger = LoggerFactory.getLogger(UnixTimestampDeserializer.class);

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String timestamp = jp.getText().trim();

        try {
            return new Date(Long.valueOf(timestamp + "000"));
        } catch (NumberFormatException e) {
            logger.warn("Unable to deserialize timestamp: " + timestamp, e);
            return null;
        }
    }
}

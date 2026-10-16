package org.auscope.portal.core.services.responses.csw;

import java.net.URL;
import java.util.List;

import org.auscope.portal.core.services.csw.URLToStringConverter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.ValueConverter;

/**
 * Represents a <gmd:CI_OnlineResource> element in a CSW response
 * 
 * @author vot002
 *
 */
public class CSWOnlineResourceImpl extends AbstractCSWOnlineResource {
	@Field(type = FieldType.Text)
	@ValueConverter(URLToStringConverter.class)
    private URL linkage;
    private String protocol;
    private String name;
    private String description;
    private String applicationProfile;
    private String protocolRequest;
    
    // Optional WMTS fields
    private String wmtsAccessMethod;
    private String wmtsTileTemplate;
    // Some WMTS GetCaps urls don't use KVP parameters but have a REST URL instead
    private String wmtsCapabilitiesUrl;
    private String wmtsStyle;
    private String wmtsTileMatrixSet;
    private List<String> wmtsTileMatrixSets;
    private String wmtsFormat;
    private List<String> wmtsTileMatrixLabels;
    
    /**
     * Default constructor required for deserialization
     */
    public CSWOnlineResourceImpl() {
    	super();
    }
    
    public CSWOnlineResourceImpl(URL linkage, String protocol, String name,
            String description) {
        this(linkage, protocol, name, description, "");
    }

    public CSWOnlineResourceImpl(URL linkage, String protocol, String name,
            String description, String applicationProfile) {
        super();
        this.linkage = linkage;
        this.protocol = protocol;
        this.name = name;
        this.description = description;
        this.applicationProfile = applicationProfile;
        this.protocolRequest = "";
    }
    
    public CSWOnlineResourceImpl(URL linkage, String protocol, String name, String description,
            String applicationProfile, String protocolRequest) {
        super();
        this.linkage = linkage;
        this.protocol = protocol;
        this.name = name;
        this.description = description;
        this.applicationProfile = applicationProfile;
        this.protocolRequest = protocolRequest;
    }

    @Override
    public String getApplicationProfile() {
        return applicationProfile;
    }

    @Override
    public URL getLinkage() {
        return linkage;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getProtocolRequest() {
    	return protocolRequest;
    }

    /**
     * provide the protocol version if possible. e.g. WMS 1.1.1 vs 1.3
     * 
     * @return version if possible
     */
    @Override
    public String getVersion() {
        //VT: Currently only cater to WMS. Can be expanded in the future for others such as WFS, WMTS etc.
        if (this.getType() == OnlineResourceType.WMS) {
            if (this.getProtocol().contains("1.3.0")) {
                return "1.3.0";
            } else {
                return "1.1.1";//VT:Default to 1.1.1 for WMS
            }
        } else {
            return "";
        }
    }

    // WMTS specific methods
    public String getWmtsAccessMethod() {
        return wmtsAccessMethod;
    }

    public void setWmtsAccessMethod(String wmtsAccessMethod) {
        this.wmtsAccessMethod = wmtsAccessMethod;
    }

    public String getWmtsTileTemplate() {
        return wmtsTileTemplate;
    }

    public void setWmtsTileTemplate(String wmtsTileTemplate) {
        this.wmtsTileTemplate = wmtsTileTemplate;
    }

    public String getWmtsCapabilitiesUrl() {
        return wmtsCapabilitiesUrl;
    }

    public void setWmtsCapabilitiesUrl(String wmtsCapabilitiesUrl) {
        this.wmtsCapabilitiesUrl = wmtsCapabilitiesUrl;
    }

    public String getWmtsStyle() {
        return wmtsStyle;
    }

    public void setWmtsStyle(String wmtsStyle) {
        this.wmtsStyle = wmtsStyle;
    }

    public String getWmtsTileMatrixSet() {
        return wmtsTileMatrixSet;
    }

    public void setWmtsTileMatrixSet(String wmtsTileMatrixSet) {
        this.wmtsTileMatrixSet = wmtsTileMatrixSet;
    }

    public List<String> getWmtsTileMatrixSets() {
        return wmtsTileMatrixSets;
    }

    public void setWmtsTileMatrixSets(List<String> wmstTileMatrixSets) {
        this.wmtsTileMatrixSets = wmstTileMatrixSets;
    }

    public String getWmtsFormat() {
        return wmtsFormat;
    }

    public void setWmtsFormat(String wmtsFormat) {
        this.wmtsFormat = wmtsFormat;
    }

    public List<String> getWmtsTileMatrixLabels() {
        return wmtsTileMatrixLabels;
    }

    public void setWmtsTileMatrixLabels(List<String> wmtsTileMatrixLabels) {
        this.wmtsTileMatrixLabels = wmtsTileMatrixLabels;
    }

}

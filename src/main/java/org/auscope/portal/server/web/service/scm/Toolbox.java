package org.auscope.portal.server.web.service.scm;

import java.util.List;
import java.util.Map;

import org.auscope.portal.core.services.PortalServiceException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Toolbox extends Entry {
    private Map<String, String> source;
    private List<SsscImage> images;
    private String puppet;
    private String puppetHash;
    private String command;

    public Toolbox() { super(); }
    public Toolbox(String id) { super(id); }

    public String getPuppet() {
        return this.puppet;
    }

    public void setPuppet(String puppet) {
        this.puppet = puppet;
    }

    @JsonProperty("puppet_hash")
    public String getPuppetHash() {
        return this.puppetHash;
    }

    public void setPuppetHash(String puppetHash) {
        this.puppetHash = puppetHash;
    }

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
     * @return the source
     */
    public Map<String, String> getSource() {
        return source;
    }
    /**
     * @param source the source to set
     */
    public void setSource(Map<String, String> source) {
        this.source = source;
    }

    public List<SsscImage> getImages() {
        return images;
    }

    public void setImages(List<SsscImage> images) {
        this.images = images;
    }

    @Override
    public void copyMissingProperties(Entry entry) throws PortalServiceException {
        super.copyMissingProperties(entry);
        Toolbox that = (Toolbox)entry;
        if (puppet == null) { setPuppet(that.getPuppet()); }
        if (puppetHash == null) { setPuppetHash(that.getPuppetHash()); }
        if (source == null) { setSource(that.getSource()); }
        if (images == null) { setImages(that.getImages()); }
    }
}

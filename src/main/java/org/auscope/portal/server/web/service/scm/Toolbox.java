package org.auscope.portal.server.web.service.scm;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Toolbox extends Entry {
    private Map<String, String> source;
    private List<Map<String, String>> dependencies;
    private List<Map<String, String>> images;

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
    /**
     * @return the dependencies
     */
    public List<Map<String, String>> getDependencies() {
        return dependencies;
    }
    /**
     * @param dependencies the dependencies to set
     */
    public void setDependencies(List<Map<String, String>> dependencies) {
        this.dependencies = dependencies;
    }

    public List<Map<String, String>> getImages() {
        return images;
    }

    public void setImages(List<Map<String, String>> images) {
        this.images = images;
    }
}

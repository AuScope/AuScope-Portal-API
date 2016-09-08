package org.auscope.portal.server.web.service.scm;

import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Solution extends Entry {
    private String template;
    private Problem problem;
    private Toolbox toolbox;
    private List<Map<String, String>> dependencies;
    private List<Map<String, Object>> variables;

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }
    /**
     * @param template the template to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * @return the toolbox
     */
    public Toolbox getToolbox() {
        return getToolbox(false);
    }

    /**
     * Return the Toolbox, ensuring full details if full is true.
     *
     * @param full Ensure full details are availble if true
     * @return the toolbox
     */
    public Toolbox getToolbox(boolean full) {
        if (full) {
            ensureToolbox();
        }
        return toolbox;
    }

    /**
     * @param toolbox the toolbox to set
     */
    public void setToolbox(Toolbox toolbox) {
        this.toolbox = toolbox;
    }

    /**
     * @return the dependencies
     */
    public List<Map<String, String>> getDependencies() {
        return dependencies;
    }

    public List<Map<String, Object>> getVariables() {
        return variables;
    }

    public void setVariables(List<Map<String, Object>> variables) {
        this.variables = variables;
    }

    /**
     * @param dependencies the dependencies to set
     */
    public void setDependencies(List<Map<String, String>> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Ensures full Toolbox details have been fetched.
     */
    public void ensureToolbox() {
        // Only fetch the toolbox detail if we haven't already
        if (this.toolbox.getSource() == null) {
            RestTemplate rest = new RestTemplate();
            Toolbox tb = rest.getForObject(this.toolbox.getUri(),
                                                Toolbox.class);
            setToolbox(tb);
        }
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }
}

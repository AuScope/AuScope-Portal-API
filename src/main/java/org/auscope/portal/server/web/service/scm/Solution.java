package org.auscope.portal.server.web.service.scm;

import java.util.List;
import java.util.Map;

import org.auscope.portal.core.services.PortalServiceException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Solution extends Entry {
    private String template;
    private String templateHash;

    @JsonSerialize(converter=ProblemURIConverver.class)
    private Problem problem;
    private List<Map<String, Object>> variables;

	  private static class ProblemURIConverver extends StdConverter<Problem, String> {

        @Override
        public String convert(Problem problem) {
            return problem.getId();
        }

    }

    public Solution() { super(); }
    public Solution(String id) { super(id); }

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
     * @return the template hash
     */
    @JsonProperty("template_hash")
    public String getTemplateHash() {
        return templateHash;
    }

    /**
     * @param templateHash the template hash to set
     */
    public void setTemplateHash(String templateHash) {
        this.templateHash = templateHash;
    }

    public List<Map<String, Object>> getVariables() {
        return variables;
    }

    public void setVariables(List<Map<String, Object>> variables) {
        this.variables = variables;
    }

    /**
     * Return the problem that this solves.
     *
     * @return Problem
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * Sets the problem that this is a solution for.
     *
     * @param problem Problem instance that this is a Solution for.
     */
    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    @Override
    public void copyMissingProperties(Entry entry) throws PortalServiceException {
        super.copyMissingProperties(entry);
        Solution that = (Solution)entry;
        if (template == null) { setTemplate(that.getTemplate()); }
        if (templateHash == null) { setTemplateHash(that.getTemplateHash()); }
        if (problem == null) { setProblem(that.getProblem()); }
        if (variables == null) { setVariables(that.getVariables()); }
    }
}

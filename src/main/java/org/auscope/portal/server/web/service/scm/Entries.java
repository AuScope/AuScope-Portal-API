package org.auscope.portal.server.web.service.scm;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Entries {
    private List<Problem> problems;
    private List<Solution> solutions;
    private List<Toolbox> toolboxes;

    public List<Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Solution> solutions) {
        this.solutions = solutions;
    }

    public List<Toolbox> getToolboxes() {
        return toolboxes;
    }

    public void setToolboxes(List<Toolbox> toolboxes) {
        this.toolboxes = toolboxes;
    }
}

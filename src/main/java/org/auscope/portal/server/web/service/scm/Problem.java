package org.auscope.portal.server.web.service.scm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Problem from the Scientific Solution Centre.
 *
 * Adds solutions to Entry members.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Problem extends Entry {
    private List<Solution> solutions;

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Solution> solutions) {
        this.solutions = solutions;
    }
}

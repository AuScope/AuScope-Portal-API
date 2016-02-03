package org.auscope.portal.server.web.service.scm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.web.client.RestTemplate;

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

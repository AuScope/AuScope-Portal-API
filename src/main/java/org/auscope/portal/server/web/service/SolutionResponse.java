package org.auscope.portal.server.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.server.web.service.scm.Solution;

/**
 * Represents a response to a request for solutions. The response is broken into three parts:
 * 1) Solutions that match the query AND have a toolbox image matching a computer provider that a user has configured
 * 2) Solutions that match the query AND DO NOT have a toolbox image matching a compute provider that the user has configured (but match one of the unconfigured compute providers)
 * 3) Solutions that match the query AND DO NOT have a toolbox image matching any known compute provider
 * @author Josh Vote (CSIRO)
 *
 */
public class SolutionResponse implements Serializable {
    private List<Solution> configuredSolutions;
    private List<Solution> unconfiguredSolutions;
    private List<Solution> otherSolutions;

    public SolutionResponse() {
        super();
        configuredSolutions = new ArrayList<Solution>();
        unconfiguredSolutions = new ArrayList<Solution>();
        otherSolutions = new ArrayList<Solution>();
    }

    /**
     * Solutions that match the query AND have a toolbox image matching a computer provider that a user has configured
     * @return
     */
    public List<Solution> getConfiguredSolutions() {
        return configuredSolutions;
    }

    /**
     * Solutions that match the query AND DO NOT have a toolbox image matching a compute provider that the user has configured (but match one of the unconfigured compute providers)
     * @return
     */
    public List<Solution> getUnconfiguredSolutions() {
        return unconfiguredSolutions;
    }

    /**
     * Solutions that match the query AND DO NOT have a toolbox image matching any known compute provider
     * @return
     */
    public List<Solution> getOtherSolutions() {
        return otherSolutions;
    }


}

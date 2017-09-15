package org.auscope.portal.server.web.service.scm;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.PortalServiceException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Problem from the Scientific Solution Centre.
 *
 * Adds solutions to Entry members.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Problem extends Entry {
    private List<Solution> solutions;

    public Problem() { super(); }
    public Problem(String id) { super(id); }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Solution> solutions) {
        this.solutions = solutions;

        if (this.solutions == null) {
            this.solutions = new ArrayList<Solution>();
        }
    }

    @Override
    public void copyMissingProperties(Entry entry) throws PortalServiceException {
        super.copyMissingProperties(entry);
        setSolutions(((Problem)entry).getSolutions());
    }
}

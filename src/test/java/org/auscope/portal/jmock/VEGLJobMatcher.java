package org.auscope.portal.jmock;

import org.auscope.portal.server.vegl.VEGLJob;
import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

/**
 * Matcher for matching VEGLJob objects
 * @author Josh Vote
 *
 */
public class VEGLJobMatcher extends TypeSafeMatcher<VEGLJob>{
    private Integer id;
    private boolean requireMismatch;

    public VEGLJobMatcher(Integer id) {
        this.id = id;
        this.requireMismatch = false;
    }

    public VEGLJobMatcher(Integer id, boolean requireMismatch) {
        this.id = id;
        this.requireMismatch = requireMismatch;
    }

    @Override
    public void describeTo(Description description) {
        if (requireMismatch) {
            description.appendText(String.format("a VEGLJob without id='%1$s'", id));
        } else {
            description.appendText(String.format("a VEGLJob with id='%1$s'", id));
        }
    }

    @Override
    public boolean matchesSafely(VEGLJob job) {
        if (id == null && job.getId() == null) {
            return !requireMismatch;
        }

        if ((id == null) ^ (job.getId() == null)) {
            return requireMismatch;
        }

        return requireMismatch ^ (id.intValue() == job.getId().intValue());
    }

}

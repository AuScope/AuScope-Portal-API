package org.auscope.portal.jmock;

import org.auscope.portal.server.vegl.VEGLSeries;
import org.hamcrest.Description;
import org.junit.matchers.TypeSafeMatcher;

/**
 * A JUnit Matcher for matching VEGLSeries objects
 * @author vot002
 *
 */
public class VEGLSeriesMatcher extends TypeSafeMatcher<VEGLSeries> {

    private String user;
    private String name;
    private String description;
    
    /**
     * Creates a new matcher that will only match a VEGLSeries object with specified
     * name, user and description
     * @param user Can be null
     * @param name Can be null
     * @param description Can be null
     */
    public VEGLSeriesMatcher(String user, String name, String description) {
        super();
        this.user = user;
        this.name = name;
        this.description = description;
    }

    private boolean nullStringComparison(String s1, String s2) {
        if ((s1 == null && s2 != null) || 
            (s1 != null && s2 == null)) {
            return false;
        }
        
        if (s1 == s2) {
            return true;
        }
        
        return s1.equals(s2); 
    }
    
    @Override
    public boolean matchesSafely(VEGLSeries series) {
        if (series == null) {
            return false;
        }
        
        if (!nullStringComparison(user, series.getUser())) {
            return false;
        }
        
        if (!nullStringComparison(name, series.getName())) {
            return false;
        }
        
        if (!nullStringComparison(description, series.getDescription())) {
            return false;
        }
        
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("a VEGLSeries with user='%1$s' name='%2$s' description='%3$s'", user, name, description));
    }

}

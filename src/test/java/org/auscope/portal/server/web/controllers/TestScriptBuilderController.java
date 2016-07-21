package org.auscope.portal.server.web.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.ScmEntryService;
import org.auscope.portal.server.web.service.ScriptBuilderService;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.web.servlet.ModelAndView;

@PrepareForTest({FileIOUtil.class})
public class TestScriptBuilderController extends PortalTestClass {
    private ScriptBuilderController controller;
    private ScriptBuilderService mockSbService = context.mock(ScriptBuilderService.class);
    private ScmEntryService mockScmEntryService = context.mock(ScmEntryService.class);

    @Before
    public void setup() {
        // Object Under Test
        controller = new ScriptBuilderController(mockSbService, mockScmEntryService);
    }

    /**
     * Tests that the saving of script for a given job succeeds.
     * @throws Exception
     */
    @Test
    public void testSaveScript() throws Exception {
        String jobId = "1";
        String sourceText = "print 'test'";
        Set<String> solutions = new HashSet<>();
        solutions.add("http://vhirl-dev.csiro.au/scm/solutions/1");
        ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockSbService).saveScript(jobId, sourceText, user);
            oneOf(mockScmEntryService).updateJobForSolution(jobId, solutions, user);
        }});

        ModelAndView mav = controller.saveScript(jobId, sourceText, solutions, user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that the saving of empty script for a given job fails.
     */
    @Test
    public void testSaveScript_EmptySourceText() {
        String jobId = "1";
        String sourceText = "";
        Set<String> solutions = new HashSet<>();
        solutions.add("http://vhirl-dev.csiro.au/scm/solutions/1");

        ModelAndView mav = controller.saveScript(jobId, sourceText, solutions,
                new ANVGLUser());
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that the saving of script for a given job fails
     * when the underlying save script service fails.
     * @throws Exception
     */
    @Test
    public void testSaveScript_Exception() throws Exception {
        String jobId = "1";
        String sourceText = "print 'test'";
        Set<String> solutions = new HashSet<>();
        solutions.add("http://vhirl-dev.csiro.au/scm/solutions/1");
        ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockSbService).saveScript(jobId, sourceText, user);
            will(throwException(new PortalServiceException("")));
        }});

        ModelAndView mav = controller.saveScript(jobId, sourceText, solutions, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that the loading of script succeeds.
     * @throws Exception
     */
    @Test
    public void testGetSavedScript() throws Exception {
        String jobId = "1";
        String expectedScriptText = "print 'test'";
        ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockSbService).loadScript(jobId, user);
            will(returnValue(expectedScriptText));
        }});

        ModelAndView mav = controller.getSavedScript(jobId, user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
        String script = (String)mav.getModel().get("data");
        Assert.assertEquals(expectedScriptText, script);
    }

    /**
     * Tests that the loading of script fails
     * when the underlying load script service fails.
     * @throws Exception
     */
    @Test
    public void testGetSavedScript_Exception() throws Exception {
        String jobId = "1";
        ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockSbService).loadScript(jobId, user);
            will(throwException(new PortalServiceException("")));
        }});

        ModelAndView mav = controller.getSavedScript(jobId, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that the denormalised key/value pairs are turned into an appropriate map
     */
    @Test
    public void testTemplateParameterParsing() {
        String[] keys = new String[] {"apple", "pear", "banana"};
        String[] values = new String[] {"2", "4", "6"};
        String templateName = "example.txt";

        //The test is that the above keys/values make their way into a valid map
        Map<String, Object> expectedMapping = new HashMap<>();
        expectedMapping.put(keys[0], values[0]);
        expectedMapping.put(keys[1], values[1]);
        expectedMapping.put(keys[2], values[2]);

        context.checking(new Expectations() {{
            oneOf(mockSbService).populateTemplate(with(any(String.class)), with(equal(expectedMapping)));
        }});

        controller.getTemplatedScript(templateName, keys, values);
    }
}

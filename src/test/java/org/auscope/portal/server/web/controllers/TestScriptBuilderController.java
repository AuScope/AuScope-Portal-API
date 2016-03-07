package org.auscope.portal.server.web.controllers;

import java.util.HashMap;
import java.util.Map;



import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.web.security.ANVGLUser;
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

    @Before
    public void setup() {
        // Object Under Test
        controller = new ScriptBuilderController(mockSbService);
    }

    /**
     * Tests that the saving of script for a given job succeeds.
     * @throws Exception
     */
    @Test
    public void testSaveScript() throws Exception {
        final String jobId = "1";
        final String sourceText = "print 'test'";
        final ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockSbService).saveScript(jobId, sourceText, user);
        }});

        ModelAndView mav = controller.saveScript(jobId, sourceText, user);
        Assert.assertTrue((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that the saving of empty script for a given job fails.
     * @throws Exception
     */
    @Test
    public void testSaveScript_EmptySourceText() throws Exception {
        final String jobId = "1";
        final String sourceText = "";

        ModelAndView mav = controller.saveScript(jobId, sourceText, new ANVGLUser());
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that the saving of script for a given job fails
     * when the underlying save script service fails.
     * @throws Exception
     */
    @Test
    public void testSaveScript_Exception() throws Exception {
        final String jobId = "1";
        final String sourceText = "print 'test'";
        final ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockSbService).saveScript(jobId, sourceText, user);
            will(throwException(new PortalServiceException("")));
        }});

        ModelAndView mav = controller.saveScript(jobId, sourceText, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that the loading of script succeeds.
     * @throws Exception
     */
    @Test
    public void testGetSavedScript() throws Exception {
        final String jobId = "1";
        final String expectedScriptText = "print 'test'";
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
        final String jobId = "1";
        final ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockSbService).loadScript(jobId, user);
            will(throwException(new PortalServiceException("")));
        }});

        ModelAndView mav = controller.getSavedScript(jobId, user);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }

    /**
     * Tests that the denormalised key/value pairs are turned into an appropriate map
     * @throws Exception
     */
    @Test
    public void testTemplateParameterParsing() throws Exception {
        final String[] keys = new String[] {"apple", "pear", "banana"};
        final String[] values = new String[] {"2", "4", "6"};
        final String templateName = "example.txt";

        //The test is that the above keys/values make their way into a valid map
        final Map<String, Object> expectedMapping = new HashMap<String, Object>();
        expectedMapping.put(keys[0], values[0]);
        expectedMapping.put(keys[1], values[1]);
        expectedMapping.put(keys[2], values[2]);

        context.checking(new Expectations() {{
            oneOf(mockSbService).populateTemplate(with(any(String.class)), with(equal(expectedMapping)));
        }});

        controller.getTemplatedScript(templateName, keys, values);
    }
}
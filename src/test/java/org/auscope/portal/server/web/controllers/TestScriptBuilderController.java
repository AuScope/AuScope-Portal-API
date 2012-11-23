package org.auscope.portal.server.web.controllers;

import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.test.VGLPortalTestClass;
import org.auscope.portal.server.web.service.ScriptBuilderService;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.web.servlet.ModelAndView;

@PrepareForTest({FileIOUtil.class})
public class TestScriptBuilderController extends VGLPortalTestClass {
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
        
        context.checking(new Expectations() {{
            oneOf(mockSbService).saveScript(jobId, sourceText);
        }});
        
        ModelAndView mav = controller.saveScript(jobId, sourceText);
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
                
        ModelAndView mav = controller.saveScript(jobId, sourceText);
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
        
        context.checking(new Expectations() {{
            oneOf(mockSbService).saveScript(jobId, sourceText);
            will(throwException(new PortalServiceException("")));
        }});
        
        ModelAndView mav = controller.saveScript(jobId, sourceText);
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
        
        context.checking(new Expectations() {{
            oneOf(mockSbService).loadScript(jobId);
            will(returnValue(expectedScriptText));
        }});
        
        ModelAndView mav = controller.getSavedScript(jobId);
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
        
        context.checking(new Expectations() {{
            oneOf(mockSbService).loadScript(jobId);
            will(throwException(new PortalServiceException("")));
        }});
        
        ModelAndView mav = controller.getSavedScript(jobId);
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
    
    @Test
    public void testGetTemplatedScript_IOException() throws Exception {
        final String[] keys = new String[] {"apple", "pear", "banana"};
        final String[] values = new String[] {"2", "4", "6"};
        final String templateName = "example.txt";

        //The test is that the above keys/values make their way into a valid map
        final Map<String, Object> expectedMapping = new HashMap<String, Object>();
        expectedMapping.put(keys[0], values[0]);
        expectedMapping.put(keys[1], values[1]);
        expectedMapping.put(keys[2], values[2]);
        
        // Stub the static method
        stub(method(FileIOUtil.class, "convertStreamtoString")).toThrow(new IOException());

        ModelAndView mav = controller.getTemplatedScript(templateName, keys, values);
        Assert.assertFalse((Boolean)mav.getModel().get("success"));
    }
}
package org.auscope.portal.server.web.controllers;

import java.util.HashMap;
import java.util.Map;

import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.web.service.ScriptBuilderService;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

public class TestScriptBuilderController extends PortalTestClass {
    private ScriptBuilderController controller;
    private ScriptBuilderService mockSbService = context.mock(ScriptBuilderService.class);

    @Before
    public void setup() {
        controller = new ScriptBuilderController(mockSbService);
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

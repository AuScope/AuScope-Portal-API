package org.auscope.portal.server.web.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ScriptBuilderService
 * @author Josh Vote
 * @author Richard Goh
 */
public class TestScriptBuilderService extends PortalTestClass {
    private ScriptBuilderService service;
    private FileStagingService mockFileStagingService = context.mock(FileStagingService.class);
    private VEGLJobManager mockJobManager = context.mock(VEGLJobManager.class);
    private VEGLJob mockJob = context.mock(VEGLJob.class);

    @Before
    public void init() {
        service = new ScriptBuilderService(mockFileStagingService, mockJobManager);
    }

    /**
     * Tests script saving calls appropriate dependencies
     * @throws Exception
     */
    @Test
    public void testSaveScript() throws Exception {
        final String script = "#a pretend script\n";
        final Integer jobId = 123;
        final ANVGLUser user = new ANVGLUser();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, user);
            will(returnValue(mockJob));

            oneOf(mockFileStagingService).writeFile(mockJob, ScriptBuilderService.SCRIPT_FILE_NAME);
            will(returnValue(bos));
        }});

        service.saveScript(jobId.toString(), script, user);
        final String actual = new String(bos.toByteArray());
        Assert.assertEquals(script, actual);
    }

    /**
     * Tests script saving errors appropriately
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testSaveScript_JobNotFoundException() throws Exception {
        final Integer jobId = 123;
        final String script = "#a pretend script\n";
        final ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, user);
            will(throwException(new ConnectException()));
        }});

        service.saveScript(jobId.toString(), script, user);
    }

    @Test(expected=PortalServiceException.class)
    public void testSaveScript_Exception() throws Exception {
        final String script = "#a pretend script\n";
        final Integer jobId = 123;
        final ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {
            {
                oneOf(mockJobManager).getJobById(jobId, user);
                will(returnValue(mockJob));

                oneOf(mockFileStagingService).writeFile(mockJob,
                        ScriptBuilderService.SCRIPT_FILE_NAME);
                will(throwException(new Exception()));
            }
        });

        service.saveScript(jobId.toString(), script, user);
    }

    /**
     * Tests script loading success scenario
     */
    @Test
    public void testLoadScript() throws Exception {
        final String script = "#a pretend script\n";
        final Integer jobId = 123;
        final ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, user);
            will(returnValue(mockJob));

            oneOf(mockFileStagingService).readFile(mockJob, ScriptBuilderService.SCRIPT_FILE_NAME);
            will(returnValue(new ByteArrayInputStream(script.getBytes())));
        }});

        final String actualScript = service.loadScript(jobId.toString(), user);
        Assert.assertEquals(script, actualScript);
    }

    /**
     * Tests to ensure empty string is return when the script file doesn't exist
     */
    @Test
    public void testLoadEmptyScript() throws Exception {
        final Integer jobId = 123;
        final ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, user);
            will(returnValue(mockJob));

            oneOf(mockFileStagingService).readFile(mockJob, ScriptBuilderService.SCRIPT_FILE_NAME);
            will(returnValue(null));
        }});

        final String actualScript = service.loadScript(jobId.toString(), user);
        Assert.assertEquals("", actualScript);
    }

    /**
     * Tests to ensure exception is handled properly
     */
    @Test(expected=PortalServiceException.class)
    public void testLoadScriptError() throws Exception {
        final Integer jobId = 123;
        final ANVGLUser user = new ANVGLUser();

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId, user);
            will(returnValue(mockJob));

            oneOf(mockFileStagingService).readFile(mockJob, ScriptBuilderService.SCRIPT_FILE_NAME);
            will(throwException(new PortalServiceException("Test load script exception")));
        }});

        service.loadScript(jobId.toString(), user);
    }

    /**
     * Tests templating on a valid template string
     */
    @Test
    public void testTemplating() {
        final String template = "I have ${dog-amount} dogs and ${cat-amount} cats";
        final Map<String, Object> values = new HashMap<>();
        values.put("dog-amount", 2);
        values.put("cat-amount", "3");
        values.put("bird-amount", 4);

        final String result = service.populateTemplate(template, values);

        Assert.assertEquals("I have 2 dogs and 3 cats", result);
    }

    /**
     * Tests templating on an invalid template string
     */
    @Test
    public void testTemplating_BadTemplate() {
        final String template = "I have ${dog-amount} dogs and ${cat-amount} cats";
        final Map<String, Object> values = new HashMap<>();
        values.put("dog-amount", 2);
        values.put("bird-amount", 4);

        final String result = service.populateTemplate(template, values);
        Assert.assertEquals("I have 2 dogs and ${cat-amount} cats", result);
    }
}
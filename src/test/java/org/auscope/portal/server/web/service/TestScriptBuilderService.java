package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.FileStagingService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.jmock.Expectations;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for ScriptBuilderService
 * @author Josh Vote
 *
 */
public class TestScriptBuilderService extends PortalTestClass {
    private ScriptBuilderService service;
    private FileStagingService mockFileStagingService = context.mock(FileStagingService.class);
    private VEGLJobManager mockJobManager = context.mock(VEGLJobManager.class);
    private VEGLJob mockJob = context.mock(VEGLJob.class);
    private static String stagingDirectory;

    /**
     * This sets up a temporary directory in the target directory
     * to utilise as a staging area
     */
    @BeforeClass
    public static void setup() {
        stagingDirectory = String.format(
                "target%1$sTestScriptBuilderService-%2$s%1$s",
                File.separator, new Date().getTime());

        File dir = new File(stagingDirectory);
        Assert.assertTrue("Failed setting up staging directory", dir.mkdirs());
    }

    /**
     * This tears down the staging area used by the tests
     */
    @AfterClass
    public static void tearDown() {
        File dir = new File(stagingDirectory);
        FileIOUtil.deleteFilesRecursive(dir);

        //Ensure cleanup succeeded
        Assert.assertFalse(dir.exists());
    }

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
        final File tempFile = new File(stagingDirectory + "testSaveScript");


        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId);
            will(returnValue(mockJob));

            oneOf(mockFileStagingService).createStageInDirectoryFile(mockJob, ScriptBuilderService.SCRIPT_FILE_NAME);
            will(returnValue(tempFile));
        }});

        service.saveScript(jobId.toString(), script);
        String actual = FileIOUtil.convertStreamtoString(new FileInputStream(tempFile));
        Assert.assertEquals(script, actual);
        Assert.assertTrue("Unit test cleanup failed", tempFile.delete());
    }

    /**
     * Tests script saving errors appropriately
     * @throws Exception
     */
    @Test(expected=PortalServiceException.class)
    public void testSaveScriptError() throws Exception {
        final String script = "#a pretend script\n";
        final Integer jobId = 123;

        context.checking(new Expectations() {{
            oneOf(mockJobManager).getJobById(jobId);
            will(throwException(new ConnectException()));
        }});

        service.saveScript(jobId.toString(), script);
    }

    /**
     * Tests templating on a valid template string
     * @throws Exception
     */
    @Test
    public void testTemplating() throws Exception {
        final String template = "I have ${dog-amount} dogs and ${cat-amount} cats";
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put("dog-amount", 2);
        values.put("cat-amount", "3");
        values.put("bird-amount", 4);

        String result = service.populateTemplate(template, values);

        Assert.assertEquals("I have 2 dogs and 3 cats", result);
    }

    /**
     * Tests templating on an invalid template string
     * @throws Exception
     */
    @Test
    public void testTemplating_BadTemplate() throws Exception {
        final String template = "I have ${dog-amount} dogs and ${cat-amount} cats";
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put("dog-amount", 2);
        values.put("bird-amount", 4);

        String result = service.populateTemplate(template, values);
        Assert.assertEquals("I have 2 dogs and ${cat-amount} cats", result);
    }
}

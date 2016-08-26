package org.auscope.portal.server.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.test.PortalTestClass;
import org.auscope.portal.server.gridjob.FileInformation;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import au.csiro.promsclient.Activity;
import au.csiro.promsclient.Entity;
import au.csiro.promsclient.ExternalReport;
import au.csiro.promsclient.ProvenanceReporter;
import junit.framework.Assert;

public class ANVGLProvenanceServiceTest extends PortalTestClass {
    VEGLJob preparedJob;
    final String serverURL = "http://portal-fake.anvgl.org";
    final Model plainModel = ModelFactory.createDefaultModel();
    final CloudFileInformation fileInformation = context.mock(CloudFileInformation.class);
    final int jobID = 1;
    final String cloudKey = "cloudKey";
    final String cloudServiceID = "fluffy Cloud";
    final String jobName = "Cool Job";
    final String jobDescription = "Some job I made.";
    final String activityFileName = "activity.ttl";
    final String PROMSURI = "http://ec2-54-213-205-234.us-west-2.compute.amazonaws.com/id/report/";
    final String mockUser = "jo@me.com";
    URI mockProfileUrl;
    ANVGLUser mockPortalUser;
    
    List<VglDownload> downloads = new ArrayList<>();
    VEGLJob turtleJob;

    final String initialTurtle = "<http://portal-fake.anvgl.org/secure/getJobObject.do?jobId=1>" + System.lineSeparator() +
            "      a       <http://www.w3.org/ns/prov#Activity> ;" + System.lineSeparator();

    final String intermediateTurtle =
            "      a       <http://www.w3.org/ns/prov#Entity> ;" + System.lineSeparator() +
            "      <http://www.w3.org/2000/01/rdf-schema#label>" + System.lineSeparator() +
            "              \"activity.ttl\"^^<http://www.w3.org/2001/XMLSchema#string> ;" + System.lineSeparator() +
            "      <http://www.w3.org/ns/dcat#downloadURL>" + System.lineSeparator() +
            "              \"http://portal-fake.anvgl.org/secure/jobFile.do?jobId=1&key=activity.ttl\"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;" + System.lineSeparator() +
            "      <http://www.w3.org/ns/prov#wasAttributedTo>" + System.lineSeparator() +
            "              <https://plus.google.com/1> .";

    final String endedTurtle = "<http://www.w3.org/ns/prov#endedAtTime>";
    final String serviceTurtle = "<http://promsns.org/def/proms#ServiceEntity>";

    final String file1Turtle =
            "      a       <http://www.w3.org/ns/prov#Entity> ;" + System.lineSeparator() +
            "      <http://www.w3.org/ns/dcat#downloadURL>" + System.lineSeparator() +
            "              \"http://portal-fake.anvgl.org/secure/jobFile.do?jobId=1&key=cloudKey\"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;" + System.lineSeparator() +
            "      <http://www.w3.org/ns/prov#wasAttributedTo>" + System.lineSeparator() +
            "              <https://plus.google.com/1> .";

    ANVGLProvenanceService anvglProvenanceService;
    final ProvenanceReporter reporter = context.mock(ProvenanceReporter.class);

    @Before
    public void setUp() throws Exception {
        preparedJob = context.mock(VEGLJob.class);
        mockPortalUser = context.mock(ANVGLUser.class);
        final CloudStorageService store = context.mock(CloudStorageService.class);
        final CloudStorageService[] storageServices = {store};
        final ANVGLFileStagingService fileServer = context.mock(ANVGLFileStagingService.class);
        final File activityFile = File.createTempFile("activity", ".ttl");
        URL turtleURL = getClass().getResource("/turtle.ttl");
        final File activityFile2 = new File(turtleURL.toURI());
        mockProfileUrl = new URI("https://plus.google.com/1");

        VglDownload download = new VglDownload(1);
        download.setUrl("http://portal-uploads.anvgl.org/file1?download=true");
        //download.setParentUrl("http://portal-uploads.vhirl.org/");
        download.setName("file1");
        downloads.add(download);
        CloudFileInformation cloudFileInformation = new CloudFileInformation(cloudKey, 0, "");
        CloudFileInformation cloudFileModel = new CloudFileInformation(activityFileName, 0, "");
        final CloudFileInformation[] cloudList = {cloudFileInformation, cloudFileModel};

        FileInformation input = new FileInformation(cloudKey, 0, false, "");

        turtleJob = context.mock(VEGLJob.class, "Turtle Mock Job");

        context.checking(new Expectations() {{
        	/*
            allowing(solution).getUri();
            will(returnValue("http://sssc.vhirl.org/solution1"));
            allowing(solution).getDescription();
            will(returnValue("A Fake Solution"));
            allowing(solution).getName();
            will(returnValue("FakeSol"));
            allowing(solution).getCreatedAt();
            will(returnValue(new Date()));
            */

            allowing(preparedJob).getId();
            will(returnValue(jobID));
            allowing(preparedJob).getStorageServiceId();
            will(returnValue(cloudServiceID));
            allowing(preparedJob).getJobDownloads();
            will(returnValue(downloads));
            allowing(preparedJob).getName();
            will(returnValue(jobName));
            allowing(preparedJob).getDescription();
            will(returnValue(jobDescription));
            allowing(preparedJob).getProcessDate();
            will(returnValue(new Date()));
            allowing(preparedJob).getUser();
            will(returnValue("foo@test.com"));
            allowing(preparedJob).getPromsReportUrl();
            will(returnValue("http://promsurl/id/report"));
            allowing(preparedJob).getExecuteDate();
            will(returnValue(new Date()));
            
            /*
            allowing(preparedJob).getJobFiles();
            will(returnValue(fileInfos));
            */

            allowing(fileInformation).getCloudKey();
            will(returnValue(cloudKey));

            allowing(fileServer).createLocalFile(activityFileName, preparedJob);
            will(returnValue(activityFile));

            allowing(store).getId();
            will(returnValue(cloudServiceID));
            allowing(store).listJobFiles(preparedJob);
            will(returnValue(cloudList));
            allowing(store).uploadJobFiles(with(any(VEGLJob.class)), with(any(File[].class)));
            allowing(store).getJobFile(preparedJob, activityFileName);
            will(returnValue(new FileInputStream(activityFile2)));

            allowing(turtleJob).getId();
            will(returnValue(1));
            
            allowing(mockPortalUser).getId();will(returnValue(mockUser));
            
            HttpResponseFactory factory = new DefaultHttpResponseFactory();
            HttpResponse response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, null), null);
            allowing(reporter).postReport(with(any(URI.class)), with(any(ExternalReport.class)));
            will(returnValue(response));
        }});
        
        anvglProvenanceService = new ANVGLProvenanceService(fileServer, storageServices, "http://mockurl", "http://mockreportingsystemuri");
        anvglProvenanceService.setServerURL(serverURL);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreateActivity() throws Exception {
        String graph = anvglProvenanceService.createActivity(preparedJob, null, mockPortalUser);
        Assert.assertTrue(graph.contains(initialTurtle));
        Assert.assertTrue(graph.contains(serviceTurtle));
        //Assert.assertTrue(graph.contains(intermediateTurtle));
    }

    @Test
    public void testUploadModel() throws Exception {
        anvglProvenanceService.uploadModel(plainModel, preparedJob);
    }

    @Test
    public void testJobURL() throws Exception {
        String url = anvglProvenanceService.jobURL(preparedJob, serverURL);
        Assert.assertEquals(serverURL + "/secure/getJobObject.do?jobId=1", url);
    }

    @Test
    public void testOutputURL() throws Exception {
        String url = anvglProvenanceService.outputURL(preparedJob, fileInformation, serverURL);
        Assert.assertEquals(serverURL + "/secure/jobFile.do?jobId=1&key=cloudKey", url);
    }

    @Test
    public void testCreateEntitiesForInputs() throws Exception {
        Set<Entity> entities = anvglProvenanceService.createEntitiesForInputs(preparedJob, null, mockPortalUser);
        Assert.assertNotNull(entities);
        Assert.assertEquals(3, entities.size());
    }

    @Test
    public void testPost() throws Exception {
        Set<Entity> outputs = new HashSet<>();
        Set<Entity> usedEntities = new HashSet<>();
        InputStream activityStream = getClass().getResourceAsStream("/activity.ttl");
        Activity activity;
        Model model = ModelFactory.createDefaultModel();
        model = model.read(activityStream,
                serverURL,
                "TURTLE");
        URI activityURI = new URI(
                anvglProvenanceService.jobURL(turtleJob, serverURL));
        activity = new Activity().setActivityUri(activityURI).setTitle(activityURI.toString()).setFromModel(model);
        if (activity != null) {
            activity.setEndedAtTime(new Date());
            String outputURL = serverURL + "/secure/jobFile.do?jobId=21&key=job-macgo-bt-everbloom_gmail_com-0000000021/1000_yrRP_hazard_map.png";
            outputs.add(new Entity().setDataUri(new URI(outputURL)).setWasAttributedTo(mockProfileUrl).setTitle("1000_yrRP_hazard_map.png"));
            activity.setGeneratedEntities(outputs);
            outputURL = serverURL + "/secure/jobFile.do?jobId=21&key=job-macgo-bt-everbloom_gmail_com-0000000021/20_yrRP_hazard_map.png";
            usedEntities.add(new Entity().setDataUri(new URI(outputURL)).setWasAttributedTo(mockProfileUrl).setTitle("20_yrRP_hazard_map.png"));
            activity.setUsedEntities(usedEntities);
            final ExternalReport report = new ExternalReport()
                    .setActivity(activity)
                    .setTitle(jobName)
                    .setNativeId(Integer.toString(jobID))
                    .setReportingSystemUri(new URI(serverURL))
                    .setGeneratedAtTime(new Date());            
            final URI pURI = new URI(PROMSURI);
            //final ProvenanceReporter reporter = context.mock(ProvenanceReporter.class);
            HttpResponse resp = reporter.postReport(new URI(PROMSURI), report);
            Assert.assertTrue((resp.getStatusLine().getStatusCode() == 200 ||
                    resp.getStatusLine().getStatusCode() == 201));
        }

    }

    @Test
    public void testSetFromModel() throws Exception {
        Set<Entity> outputs = new HashSet<>();
        InputStream activityStream = getClass().getResourceAsStream("/activity.ttl");
        Activity activity;
        Model model = ModelFactory.createDefaultModel();
        model = model.read(activityStream,
                serverURL,
                "TURTLE");
        activity = new Activity().setActivityUri(new URI(
                anvglProvenanceService.jobURL(turtleJob, serverURL))).setFromModel(model);
        if (activity != null) {
            activity.setEndedAtTime(new Date());
            String outputURL = serverURL + "/secure/jobFile.do?jobId=21&key=job-macgo-bt-everbloom_gmail_com-0000000021/1000_yrRP_hazard_map.png";
            outputs.add(new Entity().setDataUri(new URI(outputURL)).setWasAttributedTo(mockProfileUrl));
            activity.setGeneratedEntities(outputs);
            StringWriter out = new StringWriter();
            activity.getGraph().write(out, "TURTLE", serverURL);
            String turtle = out.toString();
            Assert.assertTrue(turtle.contains(initialTurtle));
            Assert.assertTrue(turtle.contains(endedTurtle));
            //Assert.assertTrue(turtle.contains(file1Turtle));
            Assert.assertTrue(turtle.contains(outputURL));
        }
    }
}
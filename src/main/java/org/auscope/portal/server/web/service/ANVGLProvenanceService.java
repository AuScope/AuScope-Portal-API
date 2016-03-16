package org.auscope.portal.server.web.service;

import au.csiro.promsclient.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.cloud.CloudFileInformation;
import org.auscope.portal.core.server.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VglDownload;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.scm.Solution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by Catherine Wise (wis056) on 3/10/2014. Modified by Stuart Woodman
 * (woo392) for ANVGL.
 *
 * A Service for reporting provenance information for storage in a PROMS
 * instance and also included in downloads.
 */
@Service
public class ANVGLProvenanceService {
    /** Logger for this class. */
    private static final Log LOGGER = LogFactory.getLog(ANVGLProvenanceService.class);
    /** Default name for the half-baked provenance uploaded to the cloud. */
    private static final String ACTIVITY_FILE_NAME = "activity.ttl";
    /** Protocol for email URIs */
    private static final String MAIL = "mailto:";
    /** Document type for output. */
    private static final String TURTLE_FORMAT = "TTL";

    /* Can be changed in application context */
    private String promsUrl = "http://ec2-54-213-205-234.us-west-2.compute.amazonaws.com/id/report/";
    private final String reportingSystemUrl = "http://anvgl.org.au/rs";
    private URI PROMSService = null;

    /**
     * URL of the current webserver. Will need to be set by classes using this
     * service.
     */
    public void setServerURL(String serverURL) {
        ANVGLServerURL.INSTANCE.set(serverURL);
    }

    public String serverURL() {
        return ANVGLServerURL.INSTANCE.get();
    }

    /** The service to allow us to write temporary local files. */
    private ANVGLFileStagingService anvglFileStagingService;
    /** The service to allow us to write files to the cloud. */
    private CloudStorageService[] cloudStorageServices;

    /**
     * Autowired constructor for Spring -- don't use this directly, you should
     * be able to autowire this into your own class.
     * 
     * @param newAnvglFileStagingService
     *            set the local file store must not be null
     * @param newCloudStorageServices
     *            set the cloud file store must not be null
     */
    @Autowired
    public ANVGLProvenanceService(final ANVGLFileStagingService anvglFileStagingService,
            final CloudStorageService[] cloudStorageServices,
            final PortalPropertyPlaceholderConfigurer propertyConfigurer) {
        this.anvglFileStagingService = anvglFileStagingService;
        this.cloudStorageServices = cloudStorageServices;
        this.promsUrl = propertyConfigurer.resolvePlaceholder("proms.report.url");
        try {
            this.PROMSService = new URI(promsUrl);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Create the half-baked provenance information for this job just before it
     * starts. This will create the provenance information on the inputs, job
     * and script, but not on the outputs (as they don't exist yet).
     * 
     * @param job
     *            The Virtual Labs Job we want to report provenance on. It
     *            should be just about to execute, but not yet have started.
     * @return The TURTLE text.
     */
    public String createActivity(final VEGLJob job, final Solution solution, ANVGLUser user) {
        String jobURL = jobURL(job, serverURL());
        Activity anvglJob = null;
        Set<Entity> inputs = createEntitiesForInputs(job, solution, user);
        try {
            anvglJob = new Activity().setActivityUri(new URI(jobURL)).setTitle(job.getName())
                    .setDescription(job.getDescription()).setStartedAtTime(new Date())
                    .setWasAssociatedWith(new URI(user.getId())).setUsedEntities(inputs);
        } catch (URISyntaxException ex) {
            LOGGER.error(String.format("Error parsing server name %s into URI.", jobURL), ex);
        }
        StringWriter out = new StringWriter();
        Model graph = anvglJob.getGraph();
        if (graph != null) {
            uploadModel(graph, job);
            anvglJob.getGraph().write(out, TURTLE_FORMAT);
        }
        return out.toString();
    }

    /**
     * Upload a complete or partially complete model to the cloud for storage.
     * 
     * @param model
     *            The RDF model to serialize and upload to the cloud.
     * @param job
     *            The virtual lab job this model refers to.
     */
    protected final void uploadModel(final Model model, final VEGLJob job) {
        if (model != null) {
            try {
                File tmpActivity = anvglFileStagingService.createLocalFile(ACTIVITY_FILE_NAME, job);
                FileWriter fileWriter = new FileWriter(tmpActivity);
                model.write(fileWriter, TURTLE_FORMAT);
                fileWriter.close();
                File[] files = { tmpActivity };

                CloudStorageService cloudStorageService = getStorageService(job);
                cloudStorageService.uploadJobFiles(job, files);
            } catch (IOException | PortalServiceException e) {
                // JAVA RAGE
                LOGGER.error(e.getLocalizedMessage());
            }
        }
    }

    /**
     * Looks through the list of all cloud storage providers and finds one we
     * can use for this job.
     * 
     * @param job
     *            The virtual lab job we want to know the appropriate cloud
     *            providers for.
     * @return The first cloud provider selected for this job, or null if none
     *         has yet been assigned.
     */
    protected final CloudStorageService getStorageService(final VEGLJob job) {
        for (CloudStorageService s : cloudStorageServices) {
            if (s.getId().equals(job.getStorageServiceId())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Constructs a full URL which can be used to get information (JSON) about a
     * job.
     * 
     * @param job
     *            The virtual labs job we want a url for.
     * @param serverURL
     *            URL of the webserver.
     * @return The URL for this job.
     */
    protected static String jobURL(final VEGLJob job, final String serverURL) {
        return String.format("%s/secure/getJobObject.do?jobId=%s", serverURL, job.getId());
    }

    /**
     * Get a unique url for this output file.
     * 
     * @param job
     *            The virtual labs job this output belongs to.
     * @param outputInfo
     *            The metadata for the output file.
     * @param serverURL
     *            URL of the webserver.
     * @return A URL for the file. May or may not be public.
     */
    protected static String outputURL(final VEGLJob job, final CloudFileInformation outputInfo, final String serverURL)
            throws URIException {
        String url = String.format("%s/secure/jobFile.do?jobId=%s&key=%s", serverURL, job.getId(),
                outputInfo.getCloudKey());
        url = URIUtil.encodeQuery(url);
        return url;
    }

    /**
     * Looks through the input files listed for a job and create appropriate
     * PROV-O Entities for them.
     * 
     * @param job
     *            The virtual labs job we want to examine the inputs of.
     * @return An array of PROV-O entities. May be empty, but won't be null.
     */
    public Set<Entity> createEntitiesForInputs(final VEGLJob job, final Solution solution, ANVGLUser user) {
        Set<Entity> inputs = new HashSet<>();
        // Downloads first
        try {
            for (VglDownload dataset : job.getJobDownloads()) {
                URI dataURI = new URI(dataset.getUrl());
                URI baseURI = new URI(dataURI.getScheme() + "://" + dataURI.getAuthority() + dataURI.getPath());
                inputs.add((ServiceEntity) new ServiceEntity().setQuery(dataURI.getQuery()).setServiceBaseUri(baseURI)
                        .setDataUri(dataURI).setDescription(dataset.getDescription())
                        .setWasAttributedTo(new URI(user.getId())).setTitle(dataset.getName()));
                LOGGER.debug("New Input: " + dataset.getUrl());
            }
        } catch (URISyntaxException ex) {
            LOGGER.error(
                    String.format("Error parsing data source urls %s into URIs.", job.getJobDownloads().toString()),
                    ex);
        }
        // Then extra files
        try {
            CloudStorageService cloudStorageService = getStorageService(job);
            CloudFileInformation[] fileInformationSet;
            fileInformationSet = cloudStorageService.listJobFiles(job);

            for (CloudFileInformation information : fileInformationSet) {
                URI inputURI = new URI(outputURL(job, information, serverURL()));
                LOGGER.debug("New Input: " + inputURI.toString());
                inputs.add(new Entity().setDataUri(inputURI).setWasAttributedTo(new URI(user.getId())));
            }
        } catch (PortalServiceException e) {
            LOGGER.error(String.format("Unable to retrieve upload file information for job: %s", e));
        } catch (URISyntaxException | URIException ex) {
            LOGGER.error(
                    String.format("Error parsing data source urls %s into URIs.", job.getJobDownloads().toString()),
                    ex);
        }

        if (solution != null) {
            try {
                URI dataURI = new URI(solution.getUri());
                inputs.add(new Entity().setWasAttributedTo(new URI(user.getId())).setEntityUri(dataURI)
                        .setDescription(solution.getDescription()).setCreated(solution.getCreatedAt())
                        .setTitle(solution.getName()).setMetadataUri(dataURI));
            } catch (URISyntaxException ex) {
                LOGGER.error(String.format("Error parsing data source urls %s into URIs.", solution.getUri()), ex);
            }
        }
        return inputs;
    }

    public void generateAndSaveReport(Activity activity, URI PROMSURI, VEGLJob job) {
        String server = ANVGLServerURL.INSTANCE.get();
        try {
            Report report = new ExternalReport().setActivity(activity).setTitle(job.getName())
                    .setGeneratedAtTime(new Date()).setNativeId(Integer.toString(job.getId()))
                    .setReportingSystemUri(new URI(reportingSystemUrl));
            ProvenanceReporter reporter = new ProvenanceReporter();
            int resp = reporter.postReport(PROMSURI, report);
            this.uploadModel(report.getGraph(), job);

            StringWriter stringWriter = new StringWriter();
            report.getGraph().write(new PrintWriter(stringWriter), "TURTLE");
            String reportString = stringWriter.toString();
            LOGGER.info(reportString);
            LOGGER.info(resp);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Takes a completed job and finishes creating the provenance record, and
     * uploads it to the cloud. The job *must* have had
     * {@link #createActivity(ANVGLJob, Solution, ANVGLUser) createActivity}
     * called with it already. Otherwise it can't collect the relevant
     * information, and won't do anything.
     * 
     * @param job
     *            Completed virtual labs job, about which we will finish our
     *            provenance gathering.
     */
    public String createEntitiesForOutputs(final VEGLJob job) {
        Set<Entity> outputs = new HashSet<>();
        Set<Entity> potentialOutputs = new HashSet<>();
        CloudStorageService cloudStorageService = getStorageService(job);
        CloudFileInformation[] fileInformationSet;
        Activity activity = null;
        try {
            fileInformationSet = cloudStorageService.listJobFiles(job);
            for (CloudFileInformation information : fileInformationSet) {
                List<VglDownload> inputs = job.getJobDownloads();
                List<String> names = new ArrayList<>();
                for (VglDownload input : inputs) {
                    names.add(input.getName());
                }
                if (information.getName().equals(ACTIVITY_FILE_NAME)) {
                    // Here's our Turtle!
                    InputStream activityStream = cloudStorageService.getJobFile(job, ACTIVITY_FILE_NAME);
                    Model model = ModelFactory.createDefaultModel();
                    LOGGER.debug("Current server URL: " + serverURL());
                    model = model.read(activityStream, serverURL(), TURTLE_FORMAT);
                    activity = new Activity().setActivityUri(new URI(jobURL(job, serverURL()))).setFromModel(model);
                } else if (!names.contains(information.getName())) {
                    // Ah ha! This must be an output or input.
                    URI outputURI = new URI(outputURL(job, information, serverURL()));
                    LOGGER.debug("New input/output: " + outputURI.toString());
                    potentialOutputs
                            .add(new Entity().setDataUri(outputURI).setWasAttributedTo(new URI(MAIL + job.getUser())));
                }
            }
        } catch (PortalServiceException | URISyntaxException | URIException ex) {
            LOGGER.error(
                    String.format("Error parsing data results urls %s into URIs.", job.getJobDownloads().toString()),
                    ex);
        }

        if (activity != null) {
            activity.setEndedAtTime(job.getProcessDate());
            for (Entity potentialOutput : potentialOutputs) {
                if (activity.usedEntities != null && !activity.usedEntities.contains(potentialOutput)) {
                    outputs.add(potentialOutput);
                    LOGGER.debug("Added input from potentials list: " + potentialOutput);
                }
            }
            activity.setGeneratedEntities(outputs);
            generateAndSaveReport(activity, PROMSService, job);
            StringWriter out = new StringWriter();
            activity.getGraph().write(out, TURTLE_FORMAT, serverURL());
            return out.toString();
        } else {
            return "";
        }
    }
}

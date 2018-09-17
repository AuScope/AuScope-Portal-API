package org.auscope.portal.server.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VLScmSnapshot;
import org.auscope.portal.server.vegl.VLScmSnapshotDao;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.auscope.portal.server.web.service.csw.SearchFacet;
import org.auscope.portal.server.web.service.csw.SearchFacet.Comparison;
import org.auscope.portal.server.web.service.scm.Dependency;
import org.auscope.portal.server.web.service.scm.Dependency.Type;
import org.auscope.portal.server.web.service.scm.Entries;
import org.auscope.portal.server.web.service.scm.Entry;
import org.auscope.portal.server.web.service.scm.Problem;
import org.auscope.portal.server.web.service.scm.ScmLoader;
import org.auscope.portal.server.web.service.scm.ScmLoaderFactory;
import org.auscope.portal.server.web.service.scm.Solution;
import org.auscope.portal.server.web.service.scm.Toolbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.MapperFeature;

/**
 * A service for handling Scientific Code Marketplace templates.
 *
 * @author Geoff Squire
 *
 */
@Service
public class ScmEntryService implements ScmLoader {
    private final Log logger = LogFactory.getLog(getClass());

    /** Puppet module template resource */
    protected static final String PUPPET_TEMPLATE =
            "org/auscope/portal/server/web/service/template.pp";

    private VLScmSnapshotDao vlScmSnapshotDao;
    private VelocityEngine velocityEngine;
    private VEGLJobManager jobManager;
    private CloudComputeService[] cloudComputeServices;

    private List<HttpMessageConverter<?>> converters;

    static String solutionsUrl;

    /**
     * Create a new instance.
     */
    @Autowired
    public ScmEntryService(VLScmSnapshotDao vlScmSnapshotDao,
            VEGLJobManager jobManager,
            VelocityEngine velocityEngine,
            CloudComputeService[] cloudComputeServices) {
        super();
        this.vlScmSnapshotDao = vlScmSnapshotDao;
        this.jobManager = jobManager;
        this.setVelocityEngine(velocityEngine);
        this.cloudComputeServices = cloudComputeServices;

        // Configure Jackson converters for use with RestTemplate
        this.converters = new ArrayList<HttpMessageConverter<?>>();
        this.converters.add(
            new MappingJackson2HttpMessageConverter(
                new Jackson2ObjectMapperBuilder()
                .featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build()
            )
        );

        // Register this bean as the ScmLoader instance to use
        ScmLoaderFactory.registerLoader(this);
    }

    /**
     * Return id of the VM for entry at computeServiceId, or null if not found.
     *
     * @param entryId SCM template entry ID
     * @param computeServiceId ID of the CloudComputeService provider
     * @return Snapshot id if one exists, otherwise null
     */
    public String getScmEntrySnapshotId(String entryId,
            String computeServiceId) {
        String vmId = null;
        VLScmSnapshot snapshot = vlScmSnapshotDao
                .getSnapshotForEntryAndProvider(entryId, computeServiceId);
        if (snapshot != null) {
            vmId = snapshot.getComputeVmId();
        }
        return vmId;
    }

    /**
     * Update job (jobId) with vmId and computeServiceId for solution
     * if we have one.
     *
     * @param jobId String job ID
     * @param solutionId String solution URL
     * @param user Authenticated ANVGLUser
     * @throws PortalServiceException
     */
    public void updateJobForSolution(VEGLJob job, Set<String> solutions, ANVGLUser user)
            throws PortalServiceException {
        // Store the solutionId in the job
        job.setJobSolutions(solutions);

        // Save the job
        try {
            jobManager.saveJob(job);
        } catch (Exception ex) {
            logger.error("Error updating job " + job, ex);
            throw new PortalServiceException("Error updating job for solution: ", ex);
        }
    }

    /**
     * Return the puppet module for SCM solution.
     *
     * Generates a puppet module that will provision a VM suitable for
     * running a job using the SCM entry.
     *
     * Placeholder parameters:
     *
     * <table>
     * <tr><td>sc-name</td><td>Name of the scientific code</td></tr>
     * <tr><td>source</td><td>Map of source parameters</td></tr>
     * <tr><td>source.type</td><td>Source repository type ("git", "svn")</td></tr>
     * <tr><td>source.url</td><td>Source repository URL</td></tr>
     * <tr><td>source.checkout</td><td>Checkout target for source repository</td></tr>
     * <tr><td>source.exec</td><td>Shell command to execute after source checkout.</td></tr>
     * <tr><td>system_packages</td><td>List of system packages</td></tr>
     * <tr><td>python_packages</td><td>List of python packages</td></tr>
     * <tr><td>python_requirements</td><td>Path to a pip requirements.txt file in the source</td></tr>
     * </table>
     *
     * @param solutionUrl String URL of the SCM solution
     * @return String contents of the puppet module
     */
    public String createPuppetModule(String solutionUrl) throws PortalServiceException {
        // Fetch the solution entry from the SCM
        Solution solution = getScmSolution(solutionUrl);

        // Create a velocity template vars map from the entry
        Map<String, Object> vars = puppetTemplateVars(solution);

        // Create the puppet module
        return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                PUPPET_TEMPLATE,
                "UTF-8",
                vars);
    }

    /**
     * Retrieve and decode an SCM entry.
     *
     * @param entryUrl String URL of the catalogue entry
     * @return Map<String, Object> deserialization of the json response
     *
     */
    public Solution getScmSolution(String entryUrl) {
        Solution solution = null;
        RestTemplate rest = this.restTemplate();

        try {
            solution = rest.getForObject(entryUrl, Solution.class);
        }
        catch (RestClientException ex) {
            logger.error("Failed to get SSC solution (" + entryUrl + ")", ex);
        }

        return solution;
    }

    /**
     * Retieve and return listing of all solutions available.
     *
     */
    public SolutionResponse getSolutions() throws PortalServiceException {
        return getSolutions((List<SearchFacet<? extends Object>>) null);
    }

    /**
     * Return the Solutions for a specific Problem.
     *
     * @param problem Problem to find Solutions for, or all Solutions if null.
     * @return List<Solution> list of Solutions if any.
     *
     */
    public SolutionResponse getSolutions(Problem problem) throws PortalServiceException {
        return getSolutions(Arrays.asList(new SearchFacet<Problem>(problem, "problem", Comparison.Equal)));
    }

    /**
     * Return the Solutions filtered by the specified search facets.
     *
     * @param problem Problem to find Solutions for, or all Solutions if null.
     * @param providers The set of cloud compute services to consider, if null it will use the default provider set
     * @return List<Solution> list of Solutions if any.
     *
     */
    public SolutionResponse getSolutions(List<SearchFacet<? extends Object>> facets)
        throws PortalServiceException {
        return getSolutions(facets, null);
    }

    /**
     * Return the Solutions filtered by the specified search facets.
     *
     * @param problem Problem to find Solutions for, or all Solutions if null.
     * @param providers The set of cloud compute services to consider, if null it will use the default provider set
     * @return List<Solution> list of Solutions if any.
     *
     */
    public SolutionResponse getSolutions(List<SearchFacet<? extends Object>> facets, CloudComputeService[] providers)
        throws PortalServiceException {
        StringBuilder url = new StringBuilder();
        RestTemplate rest = this.restTemplate();
        Entries solutions;

        url.append(solutionsUrl).append("/solutions");

        //Apply our search facets to the query
        String problemIdFilter = null;
        String providerFilter = null;
        if (facets != null) {
            for (SearchFacet<? extends Object> facet : facets) {
                if (facet.getValue() instanceof Problem) {
                    problemIdFilter = ((Problem) facet.getValue()).getId();
                } else if (facet.getValue() instanceof String) {
                    if (facet.getField().equals("text")) {
                        logger.error("Any Text filtering currently unsupported");
                    } else if (facet.getField().equals("provider")) {
                        providerFilter = (String) facet.getValue();
                    }
                }
            }
        }
        if (problemIdFilter != null) {
            url.append("?problem={problem_id}");
            solutions = rest.getForObject(url.toString(),
                    Entries.class,
                    problemIdFilter);
        }
        else {
            solutions = rest.getForObject(url.toString(), Entries.class);
        }

        return usefulSolutions(solutions.getSolutions(), providerFilter, providers);
    }

    /**
     * Return list of Solutions in solutions that are usable in this portal.
     *
     * Finds Solutions in solutions that can be used in this
     * portal. Either they refer to at least one image we can use, or
     * supply the information we need to create an image at runtime.
     *
     * Where a Solution has image(s) already, filter the set to those
     * we can use. Currently this means at a cloud provider we can
     * use, and we assume the image already has the portal
     * infrastructure in place.
     *
     * @param solutions List<Solution> solutions from the SSC
     * @param providerFilter If non null, the available provider list will be limited to providers with this ID.
     * @return List<Solution> subset of solutions that are usable
     *
     */
    private SolutionResponse usefulSolutions(List<Solution> solutions,
                                             String providerFilter,
                                             CloudComputeService[] configuredComputeServices)
        throws PortalServiceException {

        SolutionResponse useful = new SolutionResponse();

        Set<String> allProviders = new HashSet<String>();
        Set<String> configuredProviders = new HashSet<String>();

        Arrays.stream(configuredComputeServices).forEach(ccs -> configuredProviders.add(ccs.getId()));
        Arrays.stream(cloudComputeServices).forEach(ccs -> allProviders.add(ccs.getId()));

        for (Solution solution: solutions) {
            // Solution with toolbox with at least one image at a
            // provider we can use is useful.
            boolean foundConfigured = false;
            boolean foundUnconfigured = false;
            for (Dependency dep: solution.getDependencies()) {
                if (dep.type == Dependency.Type.TOOLBOX) {
                    Toolbox toolbox = restTemplate().getForObject(dep.identifier, Toolbox.class);
                    for (Map<String, String> image: toolbox.getImages()) {
                        String provider = image.get("provider");

                        if (StringUtils.isNotEmpty(providerFilter) && !providerFilter.equals(provider)) {
                            continue;
                        }

                        if (configuredProviders.contains(provider)) {
                            foundConfigured = true;
                            break;
                        } else if (allProviders.contains(provider)) {
                            foundUnconfigured = true;
                        }
                    }
                }
            }

            if (foundConfigured) {
                useful.getConfiguredSolutions().add(solution);
            } else if (foundUnconfigured) {
                useful.getUnconfiguredSolutions().add(solution);
            } else {
                useful.getOtherSolutions().add(solution);
            }
        }

        return useful;
    }

    /**
     * Return the Solution object(s) for job (if known).
     *
     * @param job VEGLJob object
     * @returns Solution object if job has a solutionId
     */
    public Set<Solution> getJobSolutions(VEGLJob job) {
        Solution solution = null;
        HashSet<Solution> solutions = new HashSet<>();

        if (job != null) {
            for (String uri: job.getJobSolutions()) {
                solution = getScmSolution(uri);
                if (solution != null) {
                    solutions.add(solution);
                }
            }
        }

        return solutions;
    }

    /**
     * Return a Set of the Toolbox object(s) for job.
     *
     * @param job VEGLJob object
     * @returns Set of Solution Objects.
     */
    public Set<Toolbox> getJobToolboxes(VEGLJob job) throws PortalServiceException {
        HashSet<Toolbox> toolboxes = new HashSet<>();

        for (Solution solution: getJobSolutions(job)) {
            toolboxes.addAll(entryToolboxes(solution));
        }

        return toolboxes;
    }

    /**
     * Return image info for toolbox at the specified cloud provider, or null.
     *
     * Uses the toolbox name and description as metadata for the machine image.
     *
     * TODO: Extract image metadata from compute provider, as well as toolbox
     * info.
     *
     * @param toolbox Toolbox of interest
     * @param provider ID of cloud Provider
     * @returns MachineImage with id and metadata of cloud Image
     */
    public MachineImage getToolboxImage(Toolbox toolbox, String provider) {
        if (toolbox != null && provider != null) {
            // Toolbox model allows multiple images for a given provider, but we
            // assume only one in practice, so take the first one that matches
            // the requested provider.
            for (Map<String, String> img: toolbox.getImages()) {
                if (provider.equals(img.get("provider"))) {
                    // Allow the image to override the run command, fall back to
                    // the toolbox supplied command (if any). Test for isEmpty
                    // rather than isBlank since we want to allow an image to
                    // override a non-blank toolbox command with an empty
                    // string.
                    String runCommand = img.get("command");
                    if (StringUtils.isEmpty(runCommand)) {
                        runCommand = toolbox.getCommand();
                    }

                    MachineImage image = new MachineImage(img.get("image_id"));
                    image.setName(toolbox.getName());
                    image.setDescription(toolbox.getDescription());
                    image.setRunCommand(runCommand);
                    return image;
                }
            }
        }

        return null;
    }

    /**
     * Return a map of computeServiceId to imageIds valid for job.
     *
     * @return Map<String, Set<String>> with images for job, or null.
     * @throws PortalServiceException
     */
    public Map<String, Set<MachineImage>> getJobImages(Integer jobId, ANVGLUser user) throws PortalServiceException {
        if (jobId == null) {
            return null;
        }
        
        VEGLJob job = jobManager.getJobById(jobId, user);        

        return getJobImages(job, user);
    }
    
    public Map<String, Set<MachineImage>> getJobImages(VEGLJob job, ANVGLUser user) throws PortalServiceException {
        if (job == null) {
            return null;
        }               

        return getJobImages(getJobSolutions(job), user);
    }

    public Map<String, Set<MachineImage>> getJobImages(Collection<String> solutionIds, ANVGLUser user) throws PortalServiceException {
    	if (solutionIds == null) {
    		return null;
    	}
    	
    	Set<Solution> solutions = solutionIds.stream().map((String id) -> getScmSolution(id)).collect(Collectors.toSet());
    	
    	return getJobImages(solutions, user);
    }
    
    /**
     * Return a map from compute service ids to the set of images they can 
     * provide for the solutions specified for the job.
     *  
     * @param solutions Set<Solution> solutions for the job in question
     * @param user ANVGLUser currently logged in user
     * @return Map<String, Set<MachineImage>> mapping from compute service id to set of image(s) they can provide
     * @throws PortalServiceException
     */
    public Map<String, Set<MachineImage>> getJobImages(Set<Solution> solutions, 
    												   ANVGLUser user) 
    	throws PortalServiceException {
    	Map<String, Set<MachineImage>> images = new HashMap<>();
    	
    	for (Solution solution: solutions) {
            for (Toolbox toolbox: entryToolboxes(solution)) {
                for (Map<String, String> img: toolbox.getImages()) {
                    String providerId = img.get("provider");
                    Set<MachineImage> vms = images.get(providerId);
                    if (vms == null) {
                        vms = new HashSet<>();
                        images.put(providerId, vms);
                    }
                    MachineImage mi = new MachineImage(img.get("image_id"));
                    mi.setName(toolbox.getName());
                    mi.setDescription(toolbox.getDescription());
                    mi.setRunCommand(img.get("command"));
                    vms.add(mi);
                }
            }    		
    	}

        return images;
    }

    /**
     * Return a Set of compute service ids with images for job with jobId.
     *
     * @return Set<String> of compute service ids for job, or null if jobId == null.
     * @throws PortalServiceException
     */
    public Set<String> getJobProviders(Integer jobId, ANVGLUser user) throws PortalServiceException {
        Map<String, Set<MachineImage>> images = getJobImages(jobId, user);
        return (images != null) ? images.keySet() : null;
    }
    
    /**
     * Return a set of compute service ids that can provide a toolbox suitable 
     * for running a job comprising the specified solutions.
     * 
     * @param solutionIds Collection<String> of ids for the job's solutions 
     * @param user ANVGLUser with the current logged in user
     * @return Set<String> of compute service id strings
     * @throws PortalServiceException
     */
    public Set<String> getJobProviders(Collection<String> solutionIds, ANVGLUser user)
    	throws PortalServiceException {
        Map<String, Set<MachineImage>> images = getJobImages(solutionIds, user);
        return (images != null) ? images.keySet() : null;
    }
    
    /**
     * Return a set of compute servivce ids that can provide a toolbox 
     * suitable for running a job comprising the specified solutions.
     * 
     * @param solutions Set<Solution> of solutions for the job in question
     * @param user ANVGLUser currently logged in user
     * @return Set<String> of compute service ids
     * @throws PortalServiceException
     */
    public Set<String> getJobProviders(Set<Solution> solutions, ANVGLUser user) throws PortalServiceException {
        Map<String, Set<MachineImage>> images = getJobImages(solutions, user);
        return (images != null) ? images.keySet() : null;
    }

    /**
     * Return a list of the Toolbox dependencies for entry.
     *
     * @param entry Entry to check dependencies
     * @return List<Toolbox> Toolbox dependencies for Entry
     */
    public List<Toolbox> entryToolboxes(Entry entry) throws PortalServiceException {
        List<Toolbox> toolboxes = new ArrayList<Toolbox>();

        for (Dependency dep: entry.getDependencies()) {
            if (dep.type == Dependency.Type.TOOLBOX) {
                toolboxes.add(restTemplate().getForObject(dep.identifier, Toolbox.class));
            }
        }

        return toolboxes;
    }

    private Map<String, Object> puppetTemplateVars(Solution solution)
        throws PortalServiceException {
        Map<String, Object> vars = new HashMap<>();
        // Make sure we have full Toolbox details.
        List<Toolbox> toolboxes = entryToolboxes(solution);
        if (toolboxes.size() > 0) {
            Toolbox toolbox = toolboxes.get(0);

            vars.put("sc_name", safeScName(toolbox));
            vars.put("source", toolbox.getSource());

            ArrayList<String> puppetModules = new ArrayList<>();
            ArrayList<String> pythonPackages = new ArrayList<>();
            ArrayList<String> requirements = new ArrayList<>();
            // Merge dependencies from solution and toolbox
            dependencies(toolbox.getDependencies(),
                         puppetModules,
                         pythonPackages,
                         requirements);
            dependencies(solution.getDependencies(),
                         puppetModules,
                         pythonPackages,
                         requirements);
            vars.put("puppet_modules", puppetModules);
            vars.put("python_packages", pythonPackages);
            vars.put("python_requirements", requirements);
        }
        return vars;
    }

    private void dependencies(List<Dependency> deps,
            List<String> puppetModules,
            List<String> pythonPackages,
            List<String> requirements) {
        for (Dependency dep: deps) {
            switch (dep.type) {
            case PUPPET:
                puppetModules.add(dep.identifier);
                break;
            case REQUIREMENTS:
                requirements.add(dep.identifier);
                break;
            case PYTHON:
                pythonPackages.add(dep.identifier);
                break;
            default:
                logger.warn("Unknown dependency type (" + dep + ")");
            }
        }
    }

    /**
     * Return a safe name for the scientific code used by toolbox.
     *
     * The name will be used to generate puppet classes and the path
     * where the code will be installed on the VM.
     *
     * Simple solution: strip out all non-word characters as defined
     * by the java regex spec.
     *
     */
    private static String safeScName(Toolbox toolbox) {
        return toolbox.getName().replaceAll("\\W", "");
    }

    /**
     * @return the vlScmSnapshotDao
     */
    public VLScmSnapshotDao getVlScmSnapshotDao() {
        return vlScmSnapshotDao;
    }

    /**
     * @param vlScmSnapshotDao the vlScmSnapshotDao to set
     */
    public void setVlScmSnapshotDao(VLScmSnapshotDao vlScmSnapshotDao) {
        this.vlScmSnapshotDao = vlScmSnapshotDao;
    }

    /**
     * @return the velocityEngine
     */
    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    /**
     * @param velocityEngine the velocityEngine to set
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    /**
     * Static setter used to inject the configured solution center URL.
     */
    public static void setSolutionsUrl(String solutionsUrl) {
        ScmEntryService.solutionsUrl = solutionsUrl;
    }

    private RestTemplate restTemplate() {
        return new RestTemplate(this.converters);
	}

	@Override
	public <T> T loadEntry(String id, Class<T> cls) {
      logger.debug(String.format("Loading ref-only %s from %s", cls.getName(), id));
      T entry = restTemplate().getForObject(id, cls);
      return entry;
	}

	@Override
	public Problem loadProblem(String id) {
      return loadEntry(id, Problem.class);
	}

	@Override
	public Toolbox loadToolbox(String id) {
      return loadEntry(id, Toolbox.class);
	}

	@Override
	public Solution loadSolution(String id) {
      return loadEntry(id, Solution.class);
	}
}

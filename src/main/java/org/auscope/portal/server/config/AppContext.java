package org.auscope.portal.server.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.cloud.StagingInformation;
import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.auscope.portal.core.server.PortalPropertySourcesPlaceholderConfigurer;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.server.http.download.FileDownloadService;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.GoogleCloudMonitoringCachedService;
import org.auscope.portal.core.services.KnownLayerService;
import org.auscope.portal.core.services.OpendapService;
import org.auscope.portal.core.services.PortalServiceException;
//import org.auscope.portal.core.services.SISSVoc2Service;
import org.auscope.portal.core.services.VocabularyCacheService;
import org.auscope.portal.core.services.VocabularyFilterService;
import org.auscope.portal.core.services.WCSService;
import org.auscope.portal.core.services.WFSGml32Service;
import org.auscope.portal.core.services.WFSService;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudComputeServiceAws;
//import org.auscope.portal.core.services.cloud.CloudComputeServiceNectar;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.CloudStorageServiceJClouds;
import org.auscope.portal.core.services.cloud.STSRequirement;
import org.auscope.portal.core.services.cloud.monitor.JobStatusChangeListener;
import org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.csw.GriddedCSWRecordTransformerFactory;
import org.auscope.portal.core.services.csw.ViewGriddedCSWRecordFactory;
import org.auscope.portal.core.services.methodmakers.GoogleCloudMonitoringMethodMaker;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker;
import org.auscope.portal.core.services.methodmakers.WCSMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WMSMethodMaker;
import org.auscope.portal.core.services.methodmakers.WMSMethodMakerInterface;
import org.auscope.portal.core.services.methodmakers.WMS_1_3_0_MethodMaker;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc2MethodMaker;
import org.auscope.portal.core.services.namespaces.ErmlNamespaceContext;
import org.auscope.portal.core.services.responses.vocab.ConceptFactory;
import org.auscope.portal.core.services.vocabs.VocabularyServiceItem;
import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.view.ViewGetCapabilitiesFactory;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.xslt.GmlToHtml;
import org.auscope.portal.core.xslt.WfsToKmlTransformer;
import org.auscope.portal.mscl.MSCLWFSService;
import org.auscope.portal.server.vegl.VEGLJobManager;
import org.auscope.portal.server.vegl.VGLJobStatusAndLogReader;
import org.auscope.portal.server.vegl.VglMachineImage;
import org.auscope.portal.server.vegl.mail.JobCompletionMailSender;
import org.auscope.portal.server.web.SearchHttpServiceCaller;
import org.auscope.portal.server.web.service.ANVGLFileStagingService;
import org.auscope.portal.server.web.service.ANVGLProvenanceService;
import org.auscope.portal.server.web.service.ANVGLUserService;
import org.auscope.portal.server.web.service.NCIDetailsService;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.auscope.portal.server.web.service.VGLCryptoService;
import org.auscope.portal.server.web.service.cloud.CloudComputeServiceNci;
import org.auscope.portal.server.web.service.cloud.CloudStorageServiceNci;
import org.auscope.portal.server.web.service.monitor.KnownLayerStatusMonitor;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusMonitor;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;


/**
 * Bean definitions.
 * 
 * Most definitions originally migrated from Spring MVC applicationContext.xml.
 * 
 * Definitions defined in file:
 *     JobManager
 * 
 * @author woo392
 *
 */
@Configuration
public class AppContext {

   protected final Log logger = LogFactory.getLog(getClass());

        @Value("${aws.account:undefined}") 
        private String awsAcct;

        @Bean public String awsAccount() {
            return awsAcct;
        }

        @Value("${aws.accesskey:undefined}")
        private String awsAccessKey;

        @Value("${aws.secretkey:undefined}")
        private String awsSecretKey;

        @Value("${aws.sessionkey:undefined}")
        private String awsSessionKey;

        @Value("${aws.stsrequirement:Mandatory}")
        private String awsStsRequirement;

        /*
        @Value("${nectar.ec2.accesskey:undefined}")
        private String nectarEc2AccessKey;

        @Value("${nectar.ec2.secretkey:undefined}")
        private String nectarEc2SecretKey;

        @Value("${nectar.storage.accesskey:undefined}")
        private String nectarStorageAccessKey;

        @Value("${nectar.storage.secretkey:undefined}")
        private String nectarStorageSecretKey;
        */

        @Value("${portalAdminEmail}")
        private String adminEmail;

        @Value("${localStageInDir}")
        private String stageInDirectory;

        @Value("${proms.report.url}")
        private String promsUrl;

        @Value("${proms.reportingsystem.uri}")
        private String promsReportingSystemUri;

        @Value("${smtp.server}")
        private String smtpServer;

        @Value("${frontEndUrl}")
        private String frontEndUrl;

        @Value("${portalAdminEmail}")
        private String portalAdminEmail;

        @Value("${encryption.password}")
        private String encryptionPassword;

        @Value("${solutions.url}")
        private String solutionsUrl;

        @Autowired
        private VEGLJobManager jobManager;

        @Autowired
        private ANVGLUserService userService;
        
        @Autowired
        private NCIDetailsService nciDetailsService;


    @Autowired
    private ArrayList<CSWServiceItem> cswServiceList;

    @Autowired
    private ArrayList<KnownLayer> knownTypes;
    
    @Bean
    public MailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpServer);
        return mailSender;
    }
    
    @Bean
    public VelocityEngine velocityEngine() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("input.encoding", "UTF-8");
        properties.setProperty("output.encoding", "UTF-8");
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class",
                               "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        // Stop logging to velocity.log and use standard logging
        properties.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        properties.setProperty("runtime.log.logsystem.log4j.category", "velocity");
        properties.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
        VelocityEngine velocityEngine = new VelocityEngine(properties);
        return velocityEngine;
    }
    
    @Bean
    public JobCompletionMailSender jobCompletionMailSender() throws Exception {
        JobCompletionMailSender sender = new JobCompletionMailSender(jobManager, jobStatusLogReader(), mailSender(), velocityEngine());
        sender.setTemplate("org/auscope/portal/server/web/service/monitor/templates/job-completion.tpl");
        sender.setDateFormat("EEE, d MMM yyyy HH:mm:ss");
        sender.setMaxLengthForSeriesNameInSubject(15);
        sender.setMaxLengthForJobNameInSubject(15);
        sender.setMaxLinesForTail(5);
        sender.setEmailSender(portalAdminEmail);
        sender.setEmailSubject("VGL Job (%s");
        sender.setPortalUrl(frontEndUrl);
        return sender;
    }
    
    @Bean(name="pylintCommand")
    public List<String> pylintCommand() {
        List<String> command = new ArrayList<String>();
        command.add("pylint");
        command.add("-r");
        command.add("n");
        command.add("-f");
        command.add("json");
        command.add("--disable=R,C");
        return command;
    }

    @Bean
    public CloudStorageService[] cloudStorageServices() {
    	/*
        CloudStorageService[] storageServices = new CloudStorageService[3];
        storageServices[0] = cloudStorageServiceAwsSydney();
        storageServices[1] = cloudStorageServiceNectarMelb();
        storageServices[2] = cloudStorageServiceNci();
        */
    	CloudStorageService[] storageServices = new CloudStorageService[2];
        storageServices[0] = cloudStorageServiceAwsSydney();
        storageServices[1] = cloudStorageServiceNci();
        return storageServices;
    }

    @Bean
    public ANVGLProvenanceService anvglProvenanceService() {
        ANVGLProvenanceService provService = new ANVGLProvenanceService(anvglFileStagingService(),
                cloudStorageServices(), promsUrl, promsReportingSystemUri);
        return provService;
    }

    @Bean
    public CloudComputeService[] cloudComputeServices()  {
        ArrayList<CloudComputeService> computeServicesList = new ArrayList<CloudComputeService>();
        computeServicesList.add(cloudComputeServiceAws());
        computeServicesList.add(cloudComputeServiceNci());
        /*
        try {
            CloudComputeService nectarService = cloudComputeServiceNectar();
            if (nectarService != null) {
                computeServicesList.add(nectarService);
            }
        } catch(UnknownHostException e) {
            logger.warn("Unable to create Nectar cloud compute service");
        }
		*/
        CloudComputeService computeServices[] = computeServicesList.toArray(new CloudComputeService[computeServicesList.size()]);
        return computeServices;
    }

    @Bean
    public VGLJobStatusAndLogReader jobStatusLogReader() {
        return new VGLJobStatusAndLogReader(jobManager, cloudStorageServices(), cloudComputeServices());
    }

    @Bean
    public WFSGetFeatureMethodMaker methodMaker() {
        return new WFSGetFeatureMethodMaker();
    }

    @Bean
    public VGLJobStatusChangeHandler vglJobStatusChangeHandler() throws Exception {
        return new VGLJobStatusChangeHandler(jobManager, jobCompletionMailSender(), jobStatusLogReader(), anvglProvenanceService());
    }

    @Bean
    public MSCLWFSService msclWfsService() {
        return new MSCLWFSService(httpServiceCallerApp(), methodMaker());
    }
    
    @Bean
    public JobStatusMonitor jobStatusMonitor() throws Exception {
        JobStatusChangeListener[] changeListeners = new JobStatusChangeListener[1];
        changeListeners[0] = vglJobStatusChangeHandler();
        return new JobStatusMonitor(jobStatusLogReader(), changeListeners);
    }

    @Bean
    public WFSService wfsService() {
        return new WFSService(httpServiceCallerApp(), methodMaker(), new GmlToHtml());
    }

    @Bean
    public WFSGml32Service wfsGml32Service() {
        WFSGetFeatureMethodMaker methodMaker = new WFSGetFeatureMethodMaker();
        // give it a ERML 2.0 namespace context
        methodMaker.setNamespaces(new ErmlNamespaceContext("2.0"));
        return new WFSGml32Service(new HttpServiceCaller(900000),
                methodMaker,
                // can instantiate with a different XSLT for GML 32 mapping?
                new GmlToHtml()
                );
    }

    @Bean
    public JobDetailFactoryBean vglJobStatusMonitorDetail() throws Exception {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setJobClass(VGLJobStatusMonitor.class);
        Map<String, Object> jobData = new HashMap<String, Object>();
        jobData.put("jobManager", jobManager);
        jobData.put("jobStatusMonitor", jobStatusMonitor());
        jobData.put("jobUserService", userService);
        jobData.put("nciDetailsService", nciDetailsService);
        jobDetail.setJobDataAsMap(jobData);        
        return jobDetail;
    }
    
    /***
     * Returns a factory to create jobs that update the OpenStack service status
     * for known layer services 
     * @return a factory to create jobs that update the OpenStack service status
     * for known layer services
     * @throws Exception
     */
    @Bean
    public JobDetailFactoryBean knownLayerStatusMonitorDetail() throws Exception {
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setJobClass(KnownLayerStatusMonitor.class);
        Map<String, Object> jobData = new HashMap<String, Object>();
        jobData.put("cswKnownLayerService", this.cswKnownLayerService());
        jobDetail.setJobDataAsMap(jobData);        
        return jobDetail;
    }

    @Bean
    public static PortalPropertySourcesPlaceholderConfigurer propertyConfigurer() {
        PortalPropertySourcesPlaceholderConfigurer pPropConf = new PortalPropertySourcesPlaceholderConfigurer();
        pPropConf.setLocations(new ClassPathResource("config.properties"), new ClassPathResource("config.properties"));
        return new PortalPropertySourcesPlaceholderConfigurer();
    }
    
    @Bean
    public SimpleTriggerFactoryBean jobMonitorTriggerFactoryBean() throws Exception {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(vglJobStatusMonitorDetail().getObject());
        trigger.setRepeatInterval(300000);
        trigger.setStartDelay(10000);
        return trigger;
    }

    /***
     * Returns a factory bean that create trigger for the known layer service status update job. The trigger can 
     * be used in the Quartz scheduler.
     * @return a factory bean that create trigger for the known layer service status update job
     * @throws Exception
     */
    @Bean
    public SimpleTriggerFactoryBean knownLayerStatusTriggerFactoryBean() throws Exception {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(knownLayerStatusMonitorDetail().getObject());
        trigger.setRepeatInterval(15 * 60 * 1000);
        trigger.setStartDelay(1000);
        return trigger;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws Exception {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setTaskExecutor(taskExecutor());
        Trigger[] triggers = new Trigger[2];
        triggers[0] = jobMonitorTriggerFactoryBean().getObject();
        triggers[1] = knownLayerStatusTriggerFactoryBean().getObject();
        schedulerFactory.setTriggers(triggers);
        return schedulerFactory;
    }

    /* This is the core threadpool shared by object instances throughout the portal */
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExec = new ThreadPoolTaskExecutor();
        taskExec.setCorePoolSize(5);
        taskExec.setMaxPoolSize(5);
        taskExec.setQueueCapacity(25);
        return taskExec;
    }

    @Bean
    @Autowired
    @Primary
    public HttpServiceCaller httpServiceCallerApp() {
        return new HttpServiceCaller(900000);
    }
    
    // Second HttpServiceCaller to reduce CSW record search timeout
    @Bean
    public SearchHttpServiceCaller searchHttpServiceCaller() {
        return new SearchHttpServiceCaller(60000);
    }
    
    @Bean
    public ViewGriddedCSWRecordFactory viewGriddedResourceFactory() {
        return new ViewGriddedCSWRecordFactory();
    }

    @Bean
    public ViewCSWRecordFactory viewCswRecordFactory() {
        return new ViewCSWRecordFactory();
    }

    @Bean
    public ViewKnownLayerFactory viewKnownLayerFactory() {
        return new ViewKnownLayerFactory();
    }

    @Bean
    public ViewGetCapabilitiesFactory viewGetCapabilitiesFactory() {
        return new ViewGetCapabilitiesFactory();
    }

    @Bean
    public GriddedCSWRecordTransformerFactory griddedCswTransformerFactory() {
        return new GriddedCSWRecordTransformerFactory();
    }
    
    @Bean
    public CSWCacheService cswCacheService() {
        CSWCacheService cacheService = new CSWCacheService(
                taskExecutor(), httpServiceCallerApp(), cswServiceList, griddedCswTransformerFactory());
        cacheService.setForceGetMethods(true);
        return cacheService;
    }
    
    @Bean
    public CSWFilterService cswFilterService() {
        return new CSWFilterService(taskExecutor(), searchHttpServiceCaller(), cswServiceList, griddedCswTransformerFactory());
    }
    
    @Bean
    public WCSMethodMaker wcsMethodMaker() {
        return new WCSMethodMaker();
    }
    
    @Bean
    public WCSService wcsService() {
        return new WCSService(httpServiceCallerApp(), wcsMethodMaker());
    }

    @Bean
    public WfsToKmlTransformer wfsToKmlTransformer() {
        return new WfsToKmlTransformer();
    }
    
    @Bean
    public WFSGetFeatureMethodMaker wfsMethodMaker() {
        return new WFSGetFeatureMethodMaker();
    }

    @Autowired
    VocabularyServiceItem vocabularyGeologicTimescales;

    @Bean
    public SimpleWfsService simpleWfsService() {
        return new SimpleWfsService(httpServiceCallerApp(), wfsMethodMaker());
    }

    @Autowired
    VocabularyServiceItem vocabularyCommodities;

    @Bean
    public OPeNDAPGetDataMethodMaker getDataMethodMaker() {
        return new OPeNDAPGetDataMethodMaker();
    }

    @Autowired
    VocabularyServiceItem vocabularyMineStatuses;

    @Autowired
    VocabularyServiceItem vocabularyReserveCategories;

    @Bean
    public WMSMethodMaker wmsMethodMaker() {
        return new WMSMethodMaker(httpServiceCallerApp());
    }

    @Autowired
    VocabularyServiceItem vocabularyResourceCategories;

    @Autowired
    VocabularyServiceItem nvclScalarsCategories;

    @Bean
    public WMS_1_3_0_MethodMaker wms130methodMaker() {
        return new WMS_1_3_0_MethodMaker(httpServiceCallerApp());
    }

    @Bean
    public ArrayList<VocabularyServiceItem> vocabularyServiceList() {
        ArrayList<VocabularyServiceItem> servList = new ArrayList<VocabularyServiceItem>();
        servList.add(vocabularyGeologicTimescales);
        servList.add(vocabularyCommodities);
        servList.add(vocabularyMineStatuses);
        servList.add(vocabularyReserveCategories);
        servList.add(vocabularyResourceCategories);
        servList.add(nvclScalarsCategories);
        return servList;
    }
    
    @Bean
    public WMSService wmsService() {
        List<WMSMethodMakerInterface> methodMakers = new ArrayList<WMSMethodMakerInterface>();
        methodMakers.add(wmsMethodMaker());
        methodMakers.add(wms130methodMaker());
        return new WMSService(httpServiceCallerApp(), methodMakers);
    }

    @Bean
    public VocabularyCacheService vocabularyCacheService() {
        return new VocabularyCacheService(taskExecutor(), vocabularyServiceList());
    }

    @Bean
    public VGLCryptoService encryptionService() throws PortalServiceException {
        return new VGLCryptoService(encryptionPassword);
    }

    @Bean
    public VocabularyFilterService vocabularyFilterService() {
        return new VocabularyFilterService(vocabularyCacheService());
    }
    
    @Bean
    public CloudStorageServiceJClouds cloudStorageServiceAwsSydney() {
        CloudStorageServiceJClouds storageService = new CloudStorageServiceJClouds(null, "aws-s3", awsAccessKey, awsSecretKey, awsSessionKey, "ap-southeast-2", false, true);
        storageService.setName("Amazon Web Services - S3");
        storageService.setId("amazon-aws-storage-sydney");
        storageService.setBucket("vgl-csiro");
        storageService.setAdminEmail(adminEmail);
        STSRequirement req = STSRequirement.valueOf(awsStsRequirement);
        storageService.setStsRequirement(req);
        return storageService;
    }

    /*
    @Bean
    public CloudStorageServiceJClouds cloudStorageServiceNectarMelb() {
        CloudStorageServiceJClouds storageService = new CloudStorageServiceJClouds("https://keystone.rc.nectar.org.au:5000/v3",
                "openstack-swift", nectarStorageAccessKey, nectarStorageSecretKey, null, "Melbourne", false, true);
        storageService.setName("National eResearch Collaboration Tools and Resources");
        storageService.setId("nectar-openstack-storage-melb");
        storageService.setBucket("vgl-portal");
        storageService.setAuthVersion("3");
        STSRequirement req = STSRequirement.ForceNone;
        storageService.setStsRequirement(req);
        return storageService;
    }
    */

    @Bean
    public CloudStorageServiceNci cloudStorageServiceNci() {
        CloudStorageServiceNci cloudStorageService = new CloudStorageServiceNci("gadi.nci.org.au", "nci-gadi");
        cloudStorageService.setId("nci-gadi-storage");
        cloudStorageService.setName("National Computing Infrastructure - Gadi");
        return cloudStorageService;
    }

    @Autowired
    private ViewKnownLayerFactory viewFactory;

    @Autowired
    private ViewCSWRecordFactory viewCSWRecordFactory;

    @Autowired
    private ViewGetCapabilitiesFactory viewGetCapabilitiesFactory;

    @Bean
    public KnownLayerService cswKnownLayerService() {
        return new KnownLayerService(knownTypes, cswCacheService(), viewFactory, viewCSWRecordFactory, viewGetCapabilitiesFactory, wmsService());
    }

    @Bean
    public InetAddress inetAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    @Bean
    public VglMachineImage machineImageEscript() {
        VglMachineImage machineImage = new VglMachineImage("ap-southeast-2/ami-0487de67");
        machineImage.setName("escript");
        machineImage.setDescription("A Debian (Jessie) machine with escript already installed.");
        machineImage.setKeywords(new String[] {"escript", "debian"});
        return machineImage;
    }

    @Bean
    public OpendapService opendapService() {
        return new OpendapService(httpServiceCallerApp(), getDataMethodMaker());
    }

    @Bean
    public VglMachineImage machineImageAemInversion() {
        VglMachineImage machineImage = new VglMachineImage("ap-southeast-2/ami-736b3010");
        machineImage.setName("AEM-Inversion");
        machineImage.setDescription("A Debian (Jessie) machine with aem already installed.");
        machineImage.setKeywords(new String[] {"AEM-Inversion", "debian"});
        return machineImage;
    }

    @Bean
    public SISSVoc2MethodMaker sissVocMethodMaker() {
        return new SISSVoc2MethodMaker();
    }
    
    @Bean
    public MachineImage[] vglMachineImages() {
        MachineImage[] machineImages = new MachineImage[2];
        machineImages[0] = machineImageEscript();
        machineImages[1] = machineImageAemInversion();
        return machineImages;
    }

    @Bean
    public ConceptFactory conceptFactory() {
        return new ConceptFactory();
    }
    
    @Bean
    public CloudComputeServiceAws cloudComputeServiceAws() {
        CloudComputeServiceAws computeService = new CloudComputeServiceAws("ec2.ap-southeast-2.amazonaws.com",
                awsAccessKey, awsSecretKey, null, awsSessionKey);
        computeService.setId("aws-ec2-compute");
        computeService.setName("Amazon Web Services - EC2");
        STSRequirement req = STSRequirement.valueOf(awsStsRequirement);
        computeService.setStsRequirement(req);
        computeService.setAvailableImages(vglMachineImages());
        return computeService;
    }

    /*
    @Bean
    public CloudComputeServiceNectar cloudComputeServiceNectar() throws UnknownHostException {
        if (!nectarEc2AccessKey.equals("undefined") && !nectarEc2SecretKey.equals("undefined")) {
            CloudComputeServiceNectar computeService = new CloudComputeServiceNectar(
                "https://keystone.rc.nectar.org.au:5000/v3", nectarEc2AccessKey, nectarEc2SecretKey);
            computeService.setId("nectar-nova-compute");
            computeService.setName("National eResearch Collaboration Tools and Resources");
            //computeService.setGroupName("vl-#{inetAddress().hostName.toLowerCase()}");
            String groupName = "vl-";
            groupName += inetAddress().getHostName().toLowerCase();
            computeService.setGroupName(groupName);
            computeService.setKeypair("vgl-developers");
            computeService.setApiVersion("3");
            return computeService;
        }
        return null;
    }
    */

    @Bean
    public CloudComputeServiceNci cloudComputeServiceNci() {
        CloudComputeServiceNci computeService = new CloudComputeServiceNci(cloudStorageServiceNci(), "gadi.nci.org.au");
        computeService.setId("nci-gadi-compute");
        computeService.setName("National Computing Infrastructure - Gadi");
        return computeService;
    }

    @Bean
    public StagingInformation stagingInformation() {
        return new StagingInformation(stageInDirectory);
    }

    @Bean
    public ANVGLFileStagingService anvglFileStagingService() {
        return new ANVGLFileStagingService(stagingInformation());
    }

    // Inject the configured solutions centre URL
    @Bean
    public MethodInvokingBean injectSsscUrl() {
        MethodInvokingBean ssscUrlBean = new MethodInvokingBean();
        ssscUrlBean.setStaticMethod("org.auscope.portal.server.web.service.ScmEntryService.setSolutionsUrl");
        ssscUrlBean.setArguments(solutionsUrl);
        return ssscUrlBean;
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }

    @Bean
    public ErmlNamespaceContext ermlNamespaceContext() {
        return new ErmlNamespaceContext();
    }

    // Needed? wfsService() creates a new WFSGetFeatureMethodMaker, not sure if this is referenced anywhere
    @Bean
    public WFSGetFeatureMethodMaker wfsMethodMakerErmlNamespace() {
        WFSGetFeatureMethodMaker methodMaker = new WFSGetFeatureMethodMaker();
        methodMaker.setNamespaces(ermlNamespaceContext());
        return methodMaker;
    }

    @Bean
    public FileDownloadService fileDownloadService() {
        return new FileDownloadService(httpServiceCallerApp());
    }

    @Bean
    public ServiceConfiguration serviceConfiguration() {
        List<ServiceConfigurationItem> serviceItems = new ArrayList<ServiceConfigurationItem>();
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration(serviceItems);
        return serviceConfiguration;
    }


    @Value("${env.stackdriver.enable}") private boolean enableStackdriver;
    @Value("${env.stackdriver.private_key}") private String privateKey;
    @Value("${env.stackdriver.private_key_id}") private String privateKeyId;
    @Value("${env.stackdriver.client_id}") private String clientId;
    @Value("${env.stackdriver.client_email}") private String clientEmail;
    @Value("${env.stackdriver.token_uri}") private String tokenUri;
    @Value("${env.stackdriver.project_id}") private String projectId;
    @Bean
    public GoogleCloudMonitoringCachedService googleCloudMonitoringCachedService() {
        if (!enableStackdriver) {
                return null;
        }
        GoogleCloudMonitoringCachedService stackdriverService = new GoogleCloudMonitoringCachedService(
                        new GoogleCloudMonitoringMethodMaker());
        HashMap<String, List<String>> servicesMap = new HashMap<String, List<String>>();
        servicesMap.put("EarthResourcesLayers", Arrays.asList(
                        new String[] {"wfsgetfeatureminoccview", "wfsgetcaps", "getcachedtile"}));
        servicesMap.put("TenementsLayers", Arrays.asList(
                        new String[] {"wfsgetfeaturetenements", "wfsgetcaps", "getcachedtile"}));
        servicesMap.put("NVCLBoreholeViewLayer", Arrays.asList(
                        new String[] {"nvcldataservices", "nvcldownloadservices", "wfsgetfeatureboreholeview", "wfsgetcaps", "getcachedtile"}));
        servicesMap.put("BoreholeViewLayer", Arrays.asList(
                        new String[] {"wfsgetfeatureboreholeview", "wfsgetcaps", "getcachedtile"}));
        stackdriverService.setServicesMap(servicesMap);
        stackdriverService.setPrivateKey(privateKey);
        stackdriverService.setPrivateKeyId(privateKeyId);
        stackdriverService.setClientId(clientId);
        stackdriverService.setClientEmail(clientEmail);
        stackdriverService.setTokenUri(tokenUri);
        stackdriverService.setProjectId(projectId);

        return stackdriverService;
    }

    @Bean
    public ViewCSWRecordFactory viewCSWRecordFactory() {
        return new ViewCSWRecordFactory();
    }

}

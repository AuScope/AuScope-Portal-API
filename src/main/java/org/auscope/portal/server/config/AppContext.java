package org.auscope.portal.server.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.auscope.portal.core.server.PortalPropertySourcesPlaceholderConfigurer;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.server.http.download.FileDownloadService;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.ElasticsearchService;
import org.auscope.portal.core.services.GoogleCloudMonitoringCachedService;
import org.auscope.portal.core.services.KnownLayerService;
import org.auscope.portal.core.services.OpendapService;
import org.auscope.portal.core.services.VocabularyCacheService;
import org.auscope.portal.core.services.VocabularyFilterService;
import org.auscope.portal.core.services.WCSService;
import org.auscope.portal.core.services.WFSService;
import org.auscope.portal.core.services.WMSService;
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
import org.auscope.portal.server.web.CatalogServicesHealthIndicator;
import org.auscope.portal.server.web.SearchHttpServiceCaller;
import org.auscope.portal.server.web.service.SimpleWfsService;
import org.auscope.portal.server.web.service.monitor.KnownLayerStatusMonitor;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;


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

    @Value("${smtp.server}")
    private String smtpServer;

    @Value("${frontEndUrl}")
    private String frontEndUrl;

    @Value("${portalAdminEmail}")
    private String portalAdminEmail;
    
    @Value("${knownLayersStartupDelay:1}")
    private int knownLayersStartupDelay;
    
    @Value("${knownLayersCronExpression:0 0 3 * * ?}")
    private String knownLayersCronExpression;

    // Active profile i.e. 'test' or 'prod'
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @Value("${spring.data.elasticsearch.manualUpdateOnly:false}")
    private boolean manualUpdateOnly;

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
    public WFSGetFeatureMethodMaker methodMaker() {
        return new WFSGetFeatureMethodMaker();
    }

    @Bean
    public MSCLWFSService msclWfsService() {
        return new MSCLWFSService(httpServiceCallerApp(), methodMaker());
    }
    
    @Bean
    public WFSService wfsService() {
        return new WFSService(httpServiceCallerApp(), methodMaker(), new GmlToHtml());
    }

    /***
     * Returns a factory to create jobs that update the OpenStack service status
     * for known layer services 
     * @return a factory to create jobs that update the OpenStack service status
     * for known layer services
     * @throws Exception
     */
    @Bean
    public JobDetailFactoryBean knownLayerCronStatusMonitorDetail() throws Exception {
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
        pPropConf.setIgnoreResourceNotFound(true);
        return new PortalPropertySourcesPlaceholderConfigurer();
    }
    
    /***
     * Returns a factory bean that create trigger for the known layer service status update job. The trigger can 
     * be used in the Quartz scheduler.
     * @return a factory bean that create trigger for the known layer service status update job
     * @throws Exception
     */
    @Bean
    public CronTriggerFactoryBean knownLayerStatusCronTriggerFactoryBean() throws Exception {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(knownLayerCronStatusMonitorDetail().getObject());
        trigger.setCronExpression(knownLayersCronExpression);
        trigger.setTimeZone(TimeZone.getTimeZone("Australia/Melbourne"));
        return trigger;
    }
    
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws Exception {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        
        schedulerFactory.setTaskExecutor(taskExecutor());
        Trigger[] triggers = new Trigger[1];
        triggers[0] = knownLayerStatusCronTriggerFactoryBean().getObject();
        schedulerFactory.setTriggers(triggers);

        // One off scheduler to get known layers X minutes after startup
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
            	cswKnownLayerService().updateKnownLayersCache(!manualUpdateOnly);
            }
        }, knownLayersStartupDelay, TimeUnit.MINUTES);
        scheduler.shutdown();
        
        return schedulerFactory;
    }

    /* This is the core threadpool shared by object instances throughout the portal */
    @Bean
    @Primary
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExec = new ThreadPoolTaskExecutor();
        taskExec.setCorePoolSize(5);
        taskExec.setMaxPoolSize(5);
        taskExec.setQueueCapacity(25);
        return taskExec;
    }

    // Primary (default) HttpServiceCaller bean
    // Will ignore SSL errors if the test profile is active (locally signed SSL certs)
    @Bean
    @Autowired
    @Primary
    public HttpServiceCaller httpServiceCallerApp() {
        return new HttpServiceCaller(900000, activeProfile.contains("test"));
    }
    
    // Second HttpServiceCaller to reduce CSW record search timeout
    // Will ignore SSL errors if the test profile is active (locally signed SSL certs)
    @Bean
    public SearchHttpServiceCaller searchHttpServiceCaller() {
        return new SearchHttpServiceCaller(60000, activeProfile.contains("test"));
    }

    // Third HttpServiceCaller for CSW cache services 
    // Will ignore SSL errors if the test profile is active (locally signed SSL certs)
    @Bean
    public HttpServiceCaller cswCacheHttpServiceCaller() {
        return new HttpServiceCaller(900000, activeProfile.contains("test"));
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
                taskExecutor(), cswCacheHttpServiceCaller(), cswServiceList, griddedCswTransformerFactory(), elasticsearchService());
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
    public VocabularyFilterService vocabularyFilterService() {
        return new VocabularyFilterService(vocabularyCacheService());
    }
    
    @Lazy
    @Autowired
    private ViewKnownLayerFactory viewFactory;

    @Lazy
    @Autowired
    private ViewCSWRecordFactory viewCSWRecordFactory;

    @Lazy
    @Autowired
    private ViewGetCapabilitiesFactory viewGetCapabilitiesFactory;

    @Bean
    public KnownLayerService cswKnownLayerService() {
        return new KnownLayerService(knownTypes, viewFactory, viewCSWRecordFactory,
        							 viewGetCapabilitiesFactory, wmsService(), elasticsearchService());
    }

    @Bean
    public InetAddress inetAddress() throws UnknownHostException {
        return InetAddress.getLocalHost();
    }

    @Bean
    public OpendapService opendapService() {
        return new OpendapService(httpServiceCallerApp(), getDataMethodMaker());
    }

    @Bean
    public SISSVoc2MethodMaker sissVocMethodMaker() {
        return new SISSVoc2MethodMaker();
    }
    
    @Bean
    public ConceptFactory conceptFactory() {
        return new ConceptFactory();
    }
    
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
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

    @Bean
    public CatalogServicesHealthIndicator CatalogServicesHealthIndicator() {
        return new CatalogServicesHealthIndicator(cswCacheService(), cswKnownLayerService(), cswServiceList);
    }
    
    @Bean
    ElasticsearchService elasticsearchService() {
    	return new ElasticsearchService(searchHttpServiceCaller());
    }

}

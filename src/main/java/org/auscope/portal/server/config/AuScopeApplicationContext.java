package org.auscope.portal.server.config;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Arrays;

import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.auscope.portal.server.web.controllers.sessonobject.StringArrayToCustomRegistry;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.KnownLayerService;
import org.auscope.portal.core.services.OpendapService;
import org.auscope.portal.core.services.WCSService;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.WFSService;

import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.csw.custom.CustomRegistry;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker;
import org.auscope.portal.core.services.methodmakers.WCSMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WMSMethodMaker;
import org.auscope.portal.core.services.methodmakers.WMSMethodMakerInterface;
import org.auscope.portal.core.services.methodmakers.WMS_1_3_0_MethodMaker;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayer;

import org.auscope.portal.core.view.ViewCSWRecordFactory;
import org.auscope.portal.core.xslt.WfsToKmlTransformer;
import org.auscope.portal.core.services.VocabularyCacheService;
import org.auscope.portal.core.services.VocabularyFilterService;
import org.auscope.portal.core.services.responses.vocab.ConceptFactory;
import org.auscope.portal.core.services.methodmakers.sissvoc.SISSVoc2MethodMaker;
import org.auscope.portal.core.services.SISSVoc2Service;
import org.auscope.portal.server.web.service.ErmlVocabService;
import org.auscope.portal.server.web.service.NvclVocabService;
import org.auscope.portal.core.server.http.download.FileDownloadService;
import org.auscope.portal.core.services.vocabs.VocabularyServiceItem;
import org.auscope.portal.core.services.VocabularyCacheService;
import org.auscope.portal.nvcl.NvclVocabMethodMaker;
import org.auscope.portal.mineraloccurrence.CommodityVocabMethodMaker;
import org.auscope.portal.core.server.PortalPropertySourcesPlaceholderConfigurer;
import org.auscope.portal.server.web.service.NotificationService;
import org.auscope.portal.core.services.methodmakers.Nagios4MethodMaker;
import org.auscope.portal.core.services.Nagios4CachedService;
import org.auscope.portal.mscl.MSCLWFSService;
import org.auscope.portal.core.services.NamespaceService;
import org.auscope.portal.core.services.LocalCSWFilterService;


/**
 * Bean definitions.
 * 
 * Most definitions originally migrated from Spring MVC applicationContext.xml.
 * 
 */
@Configuration
public class AuScopeApplicationContext {

    @Autowired
    ArrayList<CSWServiceItem> cswServiceList;

    @Autowired
    ArrayList<KnownLayer> knownTypes;

    @Bean
    public LocalCSWFilterService localCSWFilterService() {
        return(new LocalCSWFilterService(cswFilterService(), taskExecutor()));
    }

    @Bean
    public NamespaceService namespaceService() {
        return(new NamespaceService(httpServiceCallerApp(), methodMaker()));
    }

    @Bean
    public ServiceConfiguration serviceConfiguration() {
        List<ServiceConfigurationItem> sciList = new ArrayList<ServiceConfigurationItem>();
        return new ServiceConfiguration(sciList);
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
        return new WFSService(httpServiceCallerApp(), methodMaker(), null);
    }

    @Bean
    public static PortalPropertySourcesPlaceholderConfigurer propertyConfigurer() {
        PortalPropertySourcesPlaceholderConfigurer pPropConf = new PortalPropertySourcesPlaceholderConfigurer();
        pPropConf.setLocations(new ClassPathResource("config.properties"), new ClassPathResource("config.properties"));
        return new PortalPropertySourcesPlaceholderConfigurer();
    }

    /* This lists all the implementation of converters used in Spring */
    @Bean
    public ConversionServiceFactoryBean conversionService() {
        StringArrayToCustomRegistry strReg = new StringArrayToCustomRegistry();
        HashSet<StringArrayToCustomRegistry> converters = new HashSet<StringArrayToCustomRegistry>();
        converters.add(strReg);
        ConversionServiceFactoryBean convServ = new ConversionServiceFactoryBean();
        convServ.setConverters(converters);
        return convServ;
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

    @Bean
    public ViewCSWRecordFactory viewCswRecordFactory() {
    	return new ViewCSWRecordFactory();
    }

    @Bean
    public ViewKnownLayerFactory viewKnownLayerFactory() {
    	return new ViewKnownLayerFactory();
    }

    @Bean
    public WfsToKmlTransformer wfsToKmlTransformer() {
        return new WfsToKmlTransformer();
    }
    
    @Autowired
    VocabularyServiceItem vocabularyGeologicTimescales;

    @Autowired
    VocabularyServiceItem vocabularyCommodities;

    @Autowired
    VocabularyServiceItem vocabularyMineStatuses;

    @Autowired
    VocabularyServiceItem vocabularyReserveCategories;

    @Autowired
    VocabularyServiceItem vocabularyResourceCategories;

    @Bean
    public ArrayList<VocabularyServiceItem> vocabularyServiceList() {
        ArrayList<VocabularyServiceItem> servList = new ArrayList<VocabularyServiceItem>();
        servList.add(vocabularyGeologicTimescales);
        servList.add(vocabularyCommodities);
        servList.add(vocabularyMineStatuses);
        servList.add(vocabularyReserveCategories);
        servList.add(vocabularyResourceCategories);
        return servList;
    }
    
    @Bean
    public VocabularyCacheService vocabularyCacheService() {
        return new VocabularyCacheService(taskExecutor(), vocabularyServiceList());
    }

    @Bean
    public VocabularyFilterService vocabularyFilterService() {
        return new VocabularyFilterService(vocabularyCacheService());
    }
    
    @Bean
    @Primary
    public CSWCacheService cswCacheService() {
    	CSWCacheService cacheService = new CSWCacheService(
    			taskExecutor(), httpServiceCallerApp(), cswServiceList);
    	cacheService.setForceGetMethods(true);
    	return cacheService;
    }

    @Bean
    public CSWFilterService cswFilterService() {
    	return new CSWFilterService(taskExecutor(), httpServiceCallerApp(), cswServiceList);
    }

    @Bean
    public KnownLayerService cswKnownLayerService() {
    	return new KnownLayerService(knownTypes, cswCacheService());
    }

    @Bean
    public OPeNDAPGetDataMethodMaker getDataMethodMaker() {
    	return new OPeNDAPGetDataMethodMaker();
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
    public SISSVoc2Service sissVocService() {
        return new SISSVoc2Service(httpServiceCallerApp(), conceptFactory(),  sissVocMethodMaker());
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
    public WMSMethodMaker wmsMethodMaker() {
    	return new WMSMethodMaker(httpServiceCallerApp());
    }

    @Bean
    public WMS_1_3_0_MethodMaker wms130methodMaker() {
    	return new WMS_1_3_0_MethodMaker(httpServiceCallerApp());
    }
    

    @Bean
    public WMSService wmsService() {
    	List<WMSMethodMakerInterface> methodMakers = new ArrayList<WMSMethodMakerInterface>();
    	methodMakers.add(wmsMethodMaker());
    	methodMakers.add(wms130methodMaker());
    	return new WMSService(httpServiceCallerApp(), methodMakers);
    }

    @Value("${env.vocabService.url}")
    private String ermlVocabServiceURL;
    
    @Bean
    public ErmlVocabService ermlVocabService() {
        return new ErmlVocabService(httpServiceCallerApp(), new CommodityVocabMethodMaker(), ermlVocabServiceURL);
    }

    @Value("${env.nvclVocabService.url}")
    private String nvclVocabServiceURL;

    @Bean
    public NvclVocabService nvclVocabService() {
        return new NvclVocabService(httpServiceCallerApp(), new NvclVocabMethodMaker(), nvclVocabServiceURL);
    }

    @Bean
    public FileDownloadService fileDownloadService() {
        return new FileDownloadService(httpServiceCallerApp());
    }

    @Value("${env.nagios.url}") private String nagiosURL;
    @Value("${env.nagios.user}") private String nagiosUser;
    @Value("${env.nagios.password}") private String nagiosPassword;
    @Bean
    public Nagios4CachedService nagios4CachedService() {
        return new Nagios4CachedService(nagiosURL, httpServiceCallerApp(), new Nagios4MethodMaker());
    }
    
    @Value("${env.twitter.enable}") private boolean twitterEnable;
    @Value("${env.twitter.user}") private String twitterUser;
    @Value("${env.twitter.consumerKey}") private String twitterConsumerKey;
    @Value("${env.twitter.consumerSecret}") private String twitterConsumerSecret;
    @Value("${env.twitter.accessToken}") private String twitterAccessToken;
    @Value("${env.twitter.accessTokenSecret}") private String twitterAccessTokenSecret;
    @Bean 
    public NotificationService notificationService() {
        return new NotificationService(twitterEnable, twitterUser, twitterConsumerKey, twitterConsumerSecret, twitterAccessToken, twitterAccessTokenSecret);
    }
}

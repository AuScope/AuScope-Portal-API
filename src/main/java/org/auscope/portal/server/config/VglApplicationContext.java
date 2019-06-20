package org.auscope.portal.server.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.auscope.portal.core.cloud.MachineImage;
import org.auscope.portal.core.cloud.StagingInformation;
import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.auscope.portal.core.server.http.HttpServiceCaller;
import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.CSWFilterService;
import org.auscope.portal.core.services.KnownLayerService;
import org.auscope.portal.core.services.OpendapService;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.core.services.WCSService;
import org.auscope.portal.core.services.WMSService;
import org.auscope.portal.core.services.cloud.CloudComputeService;
import org.auscope.portal.core.services.cloud.CloudComputeServiceAws;
import org.auscope.portal.core.services.cloud.CloudComputeServiceNci;
import org.auscope.portal.core.services.cloud.CloudComputeServiceNectar;
import org.auscope.portal.core.services.cloud.CloudStorageService;
import org.auscope.portal.core.services.cloud.CloudStorageServiceJClouds;
import org.auscope.portal.core.services.cloud.CloudStorageServiceNci;
import org.auscope.portal.core.services.cloud.STSRequirement;
import org.auscope.portal.core.services.cloud.monitor.JobStatusChangeListener;
import org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.services.csw.custom.CustomRegistry;
import org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker;
import org.auscope.portal.core.services.methodmakers.WCSMethodMaker;
import org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker;
import org.auscope.portal.core.services.methodmakers.WMSMethodMaker;
import org.auscope.portal.core.services.methodmakers.WMSMethodMakerInterface;
import org.auscope.portal.core.services.methodmakers.WMS_1_3_0_MethodMaker;
import org.auscope.portal.core.services.namespaces.ErmlNamespaceContext;
import org.auscope.portal.core.view.ViewKnownLayerFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
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
import org.auscope.portal.server.web.service.csw.GriddedCSWRecordTransformerFactory;
import org.auscope.portal.server.web.service.csw.ViewGriddedCSWRecordFactory;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler;
import org.auscope.portal.server.web.service.monitor.VGLJobStatusMonitor;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;


/**
 * VHL bean definitions.
 * 
 * Most definitions originally migrated from Spring MVC applicationContext.xml.
 * 
 * Definitions defined in file:
 * 	JobManager
 * 
 * @author woo392
 *
 */
@Configuration
public class VglApplicationContext {

	@Value("${env.aws.accesskey}")
	private String awsAccessKey;
	
	@Value("${env.aws.secretkey}")
	private String awsSecretKey;
	
	@Value("${env.nectar.ec2.accesskey}")
	private String nectarAccessKey;
	
	@Value("${env.nectar.ec2.secretkey}")
	private String nectarSecretKey;
	
	@Value("${portalAdminEmail}")
	private String adminEmail;
	
	@Value("${aws.stsrequirement}")
	private String awsStsRequirement;
	//private STSRequirement awsStsRequirement;
	
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
	
	@Value("${env.encryption.password}")
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
	
	/*
	<bean id="mailSender" class="org.springframework.mail.javamail.JavaMailSenderImpl">
        <property name="host" value="${smtp.server}" />
    </bean>
    */
	
	@Bean
	public VelocityEngine velocityEngine() throws Exception {
	    Properties properties = new Properties();
	    properties.setProperty("input.encoding", "UTF-8");
	    properties.setProperty("output.encoding", "UTF-8");
	    properties.setProperty("resource.loader", "class");
	    properties.setProperty("class.resource.loader.class",
	    					   "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
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
	
	/*
	<bean name="jobCompletionMailSender" class="org.auscope.portal.server.vegl.mail.JobCompletionMailSender" autowire="constructor">
        <property name="template" value="org/auscope/portal/server/web/service/monitor/templates/job-completion.tpl" />
        <property name="dateFormat" value="EEE, d MMM yyyy HH:mm:ss" />
        <property name="maxLengthForSeriesNameInSubject" value="15" />
        <property name="maxLengthForJobNameInSubject" value="15" />
        <property name="maxLinesForTail" value="5" />
        <property name="emailSender" value="${portalAdminEmail}"/>
        <property name="emailSubject" value="VGL Job (%s)" />
        <property name="portalUrl" value="${frontEndUrl}"/>
    </bean>
	*/
	
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
    
    /*
    <util:list id="pylintCommand">
      <value>pylint</value>
      <value>-r</value> <value>n</value>
      <value>-f</value> <value>json</value>
      <value>--disable=R,C</value>
    </util:list> 
    */
	
	@Bean
	public CloudStorageService[] cloudStorageServices() {
		CloudStorageService[] storageServices = new CloudStorageService[3];
		storageServices[0] = cloudStorageServiceAwsSydney();
		storageServices[1] = cloudStorageServiceNectarMelb();
		storageServices[2] = cloudStorageServiceNci();
		return storageServices;
	}
	
	@Bean
	public ANVGLProvenanceService anvglProvenanceService() {
		ANVGLProvenanceService provService = new ANVGLProvenanceService(anvglFileStagingService(),
				cloudStorageServices(), promsUrl, promsReportingSystemUri);
		return provService;
	}
	
	/*
	<bean name="ANVGLProvenanceService" class="org.auscope.portal.server.web.service.ANVGLProvenanceService" autowire="constructor">
        <constructor-arg name="anvglFileStagingService" ref="fileStagingService"/>
        <constructor-arg name="cloudStorageServices"> 
            <array>  
                <ref bean="cloudStorageService-aws-sydney" /> 
                <!--<ref bean="cloudStorageService-nci" />--> 
                <!--<ref bean="cloudStorageService-nectar-melb" />--> 
            </array> 
        </constructor-arg>
        <constructor-arg name="promsUrl" value="${proms.report.url}"/>
        <constructor-arg name="promsReportingSystemUri" value="${proms.reportingsystem.uri}"/>
    </bean>
	*/
	
	@Bean
	public CloudComputeService[] cloudComputeServices()  {
		ArrayList<CloudComputeService> computeServicesList = new ArrayList<CloudComputeService>();
		computeServicesList.add(cloudComputeServiceAws());
		computeServicesList.add(cloudComputeServiceNci());
		try {
			computeServicesList.add(cloudComputeServiceNectar());
		} catch(UnknownHostException e) {
			System.out.println("Unable to create Nectar cloud compute service");
		}
		CloudComputeService computeServices[] = computeServicesList.toArray(new CloudComputeService[computeServicesList.size()]);
		return computeServices;
	}
	
	@Bean
	public VGLJobStatusAndLogReader jobStatusLogReader() {
		return new VGLJobStatusAndLogReader(jobManager, cloudStorageServices(), cloudComputeServices());
	}
	
	/*
	<bean name="jobStatusLogReader" class="org.auscope.portal.server.vegl.VGLJobStatusAndLogReader" autowire="constructor"/>
	*/
	
	@Bean
	public VGLJobStatusChangeHandler vglJobStatusChangeHandler() throws Exception {
		return new VGLJobStatusChangeHandler(jobManager, jobCompletionMailSender(), jobStatusLogReader(), anvglProvenanceService());
	}
	
	/*
	<bean name="VGLJobStatusChangeHandler" class="org.auscope.portal.server.web.service.monitor.VGLJobStatusChangeHandler" autowire="constructor">
    <constructor-arg name="anvglProvenanceService" ref="ANVGLProvenanceService"/>
    </bean>
    */
	
	@Bean
	public JobStatusMonitor jobStatusMonitor() throws Exception {
		JobStatusChangeListener[] changeListeners = new JobStatusChangeListener[1];
		changeListeners[0] = vglJobStatusChangeHandler();
		return new JobStatusMonitor(jobStatusLogReader(), changeListeners);
	}
	/*
	<bean name="jobStatusMonitor" class="org.auscope.portal.core.services.cloud.monitor.JobStatusMonitor">
        <constructor-arg name="jobStatusReader" ref="jobStatusLogReader"/>
        <constructor-arg name="jobStatusChangeListeners">
            <array>
                <ref bean="VGLJobStatusChangeHandler"/>
            </array>
        </constructor-arg>
    </bean>
	*/
	
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
	
	/*
    <bean name="vglJobStatusMonitorDetail" class="org.springframework.scheduling.quartz.JobDetailFactoryBean">
        <property name="jobClass" value="org.auscope.portal.server.web.service.monitor.VGLJobStatusMonitor" />
        <property name="jobDataAsMap">
            <map>
                <entry key="jobManager" value-ref="veglJobManager"/>
                <entry key="jobStatusMonitor" value-ref="jobStatusMonitor"/>
                <entry key="jobUserDao" value-ref="anvglUserDao"/>
                <entry key="nciDetailsDao" value-ref="nciDetailsDao"/>
            </map>
        </property>
    </bean>
	*/
	
	
    
    @Bean
    public SimpleTriggerFactoryBean simpleTriggerFactoryBean() throws Exception {
    	SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
    	trigger.setJobDetail(vglJobStatusMonitorDetail().getObject());
    	trigger.setRepeatInterval(300000);
    	trigger.setStartDelay(10000);
    	return trigger;
    }
    
    /*
    <bean id="simpleTrigger" class="org.springframework.scheduling.quartz.SimpleTriggerFactoryBean">
    <property name="jobDetail" ref="vglJobStatusMonitorDetail" />
    <property name="repeatInterval" value="300000" />
    <property name="startDelay" value="10000" />
    </bean>
    */
    
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws Exception {
    	SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
    	schedulerFactory.setTaskExecutor(taskExecutor());
    	Trigger[] triggers = new Trigger[1];
    	triggers[0] = simpleTriggerFactoryBean().getObject();
    	schedulerFactory.setTriggers(triggers);
    	return schedulerFactory;
    }
    
    /*
    <bean class="org.springframework.scheduling.quartz.SchedulerFactoryBean">
    <property name="taskExecutor" ref="taskExecutor" />
    <property name="triggers">
        <list>
            <ref bean="simpleTrigger" />
        </list>
    </property>
    </bean>
    */
    
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
    	ThreadPoolTaskExecutor taskExec = new ThreadPoolTaskExecutor();
    	taskExec.setCorePoolSize(5);
    	taskExec.setMaxPoolSize(5);
    	taskExec.setQueueCapacity(25);
    	return taskExec;
    }
    
    /*
    <!-- This is the core threadpool shared by object instances throughout the portal -->
    <bean id="taskExecutor" class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
        <property name="corePoolSize" value="5" />
        <property name="maxPoolSize" value="5" />
        <property name="queueCapacity" value="25" />
    </bean>
    */
    
    @Bean
    @Primary
    public HttpServiceCaller httpServiceCaller() {
    	return new HttpServiceCaller(900000);
    }
    
    /*
    <bean id="httpServiceCaller" class="org.auscope.portal.core.server.http.HttpServiceCaller">
    <constructor-arg type="int" name="connectionTimeOut">
        <value>900000</value>
    </constructor-arg>
    </bean>
    */
    
    // Second HttpServiceCaller to reduce CSW record search timeout
    @Bean
    public SearchHttpServiceCaller searchHttpServiceCaller() {
    	return new SearchHttpServiceCaller(10000);
    }
    
    @Bean
    public ViewGriddedCSWRecordFactory viewGriddedResourceFactory() {
    	return new ViewGriddedCSWRecordFactory();
    }
    
    /*
    <bean id="viewCswRecordFactory" class="org.auscope.portal.server.web.service.csw.ViewGriddedCSWRecordFactory">
    </bean>
    */
    
    @Bean
    public ViewKnownLayerFactory viewKnownLayerFactory() {
    	return new ViewKnownLayerFactory();
    }
    
    /*
    <bean id="viewKnownLayerFactory" class="org.auscope.portal.core.view.ViewKnownLayerFactory">
    </bean>
    */
    
    @Bean
    public GriddedCSWRecordTransformerFactory griddedCswTransformerFactory() {
    	return new GriddedCSWRecordTransformerFactory();
    }
    
    /*
    <!-- From CSWCacheService definition -->
    <bean class="org.auscope.portal.server.web.service.csw.GriddedCSWRecordTransformerFactory"/>
    */
    
    @Bean
    public CSWCacheService cswCacheService() {
    	CSWCacheService cacheService = new CSWCacheService(
    			taskExecutor(), httpServiceCaller(), cswServiceList, griddedCswTransformerFactory());
    	cacheService.setForceGetMethods(true);
    	return cacheService;
    }
    
    /*
    <bean id="cswCacheService" class="org.auscope.portal.core.services.CSWCacheService">
        <constructor-arg name="executor" ref="taskExecutor"/>
        <constructor-arg name="serviceCaller" ref="httpServiceCaller"/>
        <constructor-arg name="cswServiceList" ref="cswServiceList"/> <!-- This is pulled from the profile xml -->
        <constructor-arg name="transformerFactory">
            <bean class="org.auscope.portal.server.web.service.csw.GriddedCSWRecordTransformerFactory"/>
        </constructor-arg>
        <property name="forceGetMethods" value="true"/>
    </bean>
    */
    
    /*
    <!-- from cswFilterService definition -->
    <bean class="org.auscope.portal.server.web.service.csw.GriddedCSWRecordTransformerFactory"/>
    */
    
    @Bean
    public CSWFilterService cswFilterService() {
    	return new CSWFilterService(taskExecutor(), searchHttpServiceCaller(), cswServiceList, griddedCswTransformerFactory());
    }
    
    /*
    <bean id="cswFilterService" class="org.auscope.portal.core.services.CSWFilterService">
        <constructor-arg name="executor" ref="taskExecutor"/>
        <constructor-arg name="serviceCaller" ref="httpServiceCaller"/>
        <constructor-arg name="cswServiceList" ref="cswServiceList"/> <!-- This is pulled from the profile xml -->
        <constructor-arg name="transformerFactory">
            <bean class="org.auscope.portal.server.web.service.csw.GriddedCSWRecordTransformerFactory"/>
        </constructor-arg>
    </bean>
    */
    
    @Bean
    public KnownLayerService cswKnownLayerService() {
    	return new KnownLayerService(knownTypes, cswCacheService());
    }
    
    /*
    <bean id="cswKnownLayerService" class="org.auscope.portal.core.services.KnownLayerService">
        <constructor-arg name="knownTypes" ref="knownTypes"/> <!-- This is pulled from the profile xml -->
        <constructor-arg name="cswCacheService" ref="cswCacheService"/>
    </bean> 
    */
    
    @Bean
    public WCSMethodMaker wcsMethodMaker() {
    	return new WCSMethodMaker();
    }
    
    /*
    <!-- from wcsService definition -->
    <bean class="org.auscope.portal.core.services.methodmakers.WCSMethodMaker">
    </bean>
    */
    
    @Bean
    public WCSService wcsService() {
    	return new WCSService(httpServiceCaller(), wcsMethodMaker());
    }
    
    /*
    <bean id="wcsService" class="org.auscope.portal.core.services.WCSService">
        <constructor-arg name="serviceCaller" ref="httpServiceCaller"/>
        <constructor-arg name="methodMaker">
            <bean class="org.auscope.portal.core.services.methodmakers.WCSMethodMaker">
            </bean>
        </constructor-arg>
    </bean>
    */
    
    @Bean
    public WFSGetFeatureMethodMaker wfsMethodMaker() {
    	return new WFSGetFeatureMethodMaker();
    }
    
    /*
    <!-- from wfsService definition -->
    <bean class="org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker">
    </bean> 
    */
    
    @Bean
    public SimpleWfsService wfsService() {
    	return new SimpleWfsService(httpServiceCaller(), wfsMethodMaker());
    }
    
    /*
    <bean id="wfsService" class="org.auscope.portal.server.web.service.SimpleWfsService">
        <constructor-arg name="httpServiceCaller" ref="httpServiceCaller"/>
        <constructor-arg name="wfsMethodMaker">
            <bean class="org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker">
            </bean>
        </constructor-arg>
    </bean>
    */
    
    @Bean
    public OPeNDAPGetDataMethodMaker getDataMethodMaker() {
    	return new OPeNDAPGetDataMethodMaker();
    }
    
    /*
    <!-- from opendapService definition -->
    <bean class="org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker">
	</bean> 
    */
    
    @Bean
    public OpendapService opendapService() {
    	return new OpendapService(httpServiceCaller(), getDataMethodMaker());
    }
    
    /*
    <bean id="opendapService" class="org.auscope.portal.core.services.OpendapService" >
        <constructor-arg name="serviceCaller" ref="httpServiceCaller"/>
        <constructor-arg name="getDataMethodMaker">
            <bean class="org.auscope.portal.core.services.methodmakers.OPeNDAPGetDataMethodMaker">
            </bean>
        </constructor-arg>
    </bean>
    */
    
    @Bean
    public WMSMethodMaker wmsMethodMaker() {
    	return new WMSMethodMaker(httpServiceCaller());
    }
    
    /*
    <bean id= "WMSMethodMaker" class="org.auscope.portal.core.services.methodmakers.WMSMethodMaker">
        <constructor-arg name="serviceCaller" ref="httpServiceCaller"/>
    </bean>
    */
    
    @Bean
    public WMS_1_3_0_MethodMaker wms130methodMaker() {
    	return new WMS_1_3_0_MethodMaker(httpServiceCaller());
    }
    
    /*
    <bean id= "WMS_1_3_0_MethodMaker" class="org.auscope.portal.core.services.methodmakers.WMS_1_3_0_MethodMaker">
        <constructor-arg name="serviceCaller" ref="httpServiceCaller"/>
    </bean>
    */

    @Bean
    public WMSService wmsService() {
    	List<WMSMethodMakerInterface> methodMakers = new ArrayList<WMSMethodMakerInterface>();
    	methodMakers.add(wmsMethodMaker());
    	methodMakers.add(wms130methodMaker());
    	return new WMSService(httpServiceCaller(), methodMakers);
    }
    
    /*
    <bean id="wmsService" class="org.auscope.portal.core.services.WMSService">
        <constructor-arg name="serviceCaller" ref="httpServiceCaller"/>
        <constructor-arg name="methodMaker">
            <list>
               <ref bean="WMSMethodMaker"/>
               <ref bean="WMS_1_3_0_MethodMaker"/>
            </list>
        </constructor-arg>
    </bean>
    */

    @Bean
    public VGLCryptoService encryptionService() throws PortalServiceException {
    	return new VGLCryptoService(encryptionPassword);
    }
    
    /*
    <bean id="encryption-service" class="org.auscope.portal.server.web.service.VGLCryptoService">
        <constructor-arg name="encryptionPassword" value="${env.encryption.password}"/>
    </bean>
    */
    
    @Bean
	public CloudStorageServiceJClouds cloudStorageServiceAwsSydney() {
		CloudStorageServiceJClouds storageService = new CloudStorageServiceJClouds(null, "aws-s3", awsAccessKey, awsSecretKey, null, "ap-southeast-2", false, true);
		storageService.setName("Amazon Web Services - S3");
		storageService.setId("amazon-aws-storage-sydney");
		storageService.setBucket("vgl-csiro-");
		storageService.setAdminEmail(adminEmail);
		STSRequirement req = STSRequirement.valueOf(awsStsRequirement);
		storageService.setStsRequirement(req);
		return storageService;
	}
	
	/*
	<bean id="cloudStorageService-aws-sydney" class="org.auscope.portal.core.services.cloud.CloudStorageServiceJClouds">
        <constructor-arg name="provider" value="aws-s3"/>
        <constructor-arg name="endpoint"><null/></constructor-arg>
        <constructor-arg name="accessKey" value="${env.aws.accesskey}"/>
        <constructor-arg name="secretKey" value="${env.aws.secretkey}"/>
        <constructor-arg name="regionName" value="ap-southeast-2"/>
        <constructor-arg name="relaxHostName" value="false"/>
        <constructor-arg name="stripExpectHeader" value="true"/>
        <property name="name" value="Amazon Web Services - S3"/>
        <property name="id" value="amazon-aws-storage-sydney"/>
        <property name="bucket" value="vgl-csiro-"/>
        <property name="adminEmail" value="${portalAdminEmail}"/>
        <property name="stsRequirement" value="${aws.stsrequirement}"/>
    </bean>
	*/
    
    @Bean
	public CloudStorageServiceJClouds cloudStorageServiceNectarMelb() {
    	CloudStorageServiceJClouds storageService = new CloudStorageServiceJClouds("https://keystone.rc.nectar.org.au:5000/v2.0",
    			"openstack-swift", nectarAccessKey, nectarSecretKey, null, "Melbourne", false, true);
    	storageService.setName("National eResearch Collaboration Tools and Resources");
    	storageService.setId("nectar-openstack-storage-melb");
    	storageService.setBucket("vgl-portal");
    	storageService.setAuthVersion("2.0");
    	STSRequirement req = STSRequirement.ForceNone;
		storageService.setStsRequirement(req);
		return storageService;
    }
	/*
	<bean id="cloudStorageService-nectar-melb" class="org.auscope.portal.core.services.cloud.CloudStorageServiceJClouds">
	    <constructor-arg name="endpoint" value="https://keystone.rc.nectar.org.au:5000/v2.0"/>
	    <constructor-arg name="provider" value="openstack-swift"/>
	    <constructor-arg name="accessKey" value="${env.nectar.storage.accesskey}"/>
	    <constructor-arg name="secretKey" value="${env.nectar.storage.secretkey}"/>
	    <constructor-arg name="regionName" value="Melbourne"/>
	    <constructor-arg name="relaxHostName" value="false"/>
	    <constructor-arg name="stripExpectHeader" value="true"/>
	    <property name="name" value="National eResearch Collaboration Tools and Resources"/>
	    <property name="id" value="nectar-openstack-storage-melb"/>
	    <property name="bucket" value="vgl-portal"/>
	    <property name="authVersion" value="2.0"/>
	    <property name="stsRequirement" value="ForceNone"/>
	</bean>
	*/
	
	@Bean
	public CloudStorageServiceNci cloudStorageServiceNci() {
		CloudStorageServiceNci cloudStorageService = new CloudStorageServiceNci("raijin.nci.org.au", "nci-raijin");
		cloudStorageService.setId("nci-raijin-storage");
		cloudStorageService.setName("National Computing Infrastructure - Raijin");
		return cloudStorageService;
	}
	
	/*
	<bean id="cloudStorageService-nci" class="org.auscope.portal.core.services.cloud.CloudStorageServiceNci">
        <constructor-arg name="endpoint" value="raijin.nci.org.au"/>
        <constructor-arg name="provider" value="nci-raijin"/>
        <property name="name" value="National Computing Infrastructure - Raijin"/>
        <property name="id" value="nci-raijin-storage"/>
    </bean>
	*/
	
	@Bean
	public InetAddress inetAddress() throws UnknownHostException {
		return InetAddress.getLocalHost();
	}
	
	/*
	<!-- Used in cloudComputeService-nectar -->
    <bean id="inetAddress" class="java.net.InetAddress" factory-method="getLocalHost"/> 
	*/
	
	@Bean
	public VglMachineImage machineImageEscript() {
		VglMachineImage machineImage = new VglMachineImage("ap-southeast-2/ami-0487de67");
		machineImage.setName("escript");
		machineImage.setDescription("A Debian (Jessie) machine with escript already installed.");
		machineImage.setKeywords(new String[] {"escript", "debian"});
		return machineImage;
	}
	
	/*
	<!-- from cloudComputeService-aws def -->
	<bean class="org.auscope.portal.server.vegl.VglMachineImage">
        <constructor-arg name="imageId" value="ap-southeast-2/ami-0487de67"/>
        <property name="name" value="escript"/>
        <property name="description"><value>A Debian (Jessie) machine with escript already installed.</value></property>
        <property name="keywords">
            <array>
                <value>escript</value>
                <value>debian</value>
            </array>
        </property>
    </bean>
	*/
	
	@Bean
	public VglMachineImage machineImageAemInversion() {
		VglMachineImage machineImage = new VglMachineImage("ap-southeast-2/ami-736b3010");
		machineImage.setName("AEM-Inversion");
		machineImage.setDescription("A Debian (Jessie) machine with aem already installed.");
		machineImage.setKeywords(new String[] {"AEM-Inversion", "debian"});
		return machineImage;
	}
	
	/*
	<!-- from cloudComputeService-aws def -->
	<bean class="org.auscope.portal.server.vegl.VglMachineImage">
        <constructor-arg name="imageId" value="ap-southeast-2/ami-736b3010"/>
        <property name="name" value="AEM-Inversion"/>
        <property name="description"><value>A Debian (Jessie) machine with aem already installed.</value></property>
        <property name="keywords">
            <array>
                <value>AEM-Inversion</value>
                <value>debian</value>
            </array>
        </property>
    </bean>
	*/
	
	@Bean
	public MachineImage[] vglMachineImages() {
		MachineImage[] machineImages = new MachineImage[2];
		machineImages[0] = machineImageEscript();
		machineImages[1] = machineImageAemInversion();
		return machineImages;
	}
	
	
	@Bean
	public CloudComputeServiceAws cloudComputeServiceAws() {
		CloudComputeServiceAws computeService = new CloudComputeServiceAws("ec2.ap-southeast-2.amazonaws.com",
				awsAccessKey, awsSecretKey, null, null);
		computeService.setId("aws-ec2-compute");
		computeService.setName("Amazon Web Services - EC2");
		STSRequirement req = STSRequirement.valueOf(awsStsRequirement);
		computeService.setStsRequirement(req);
		computeService.setAvailableImages(vglMachineImages());
		return computeService;
	}
	
	/*
	<bean id="cloudComputeService-aws" class="org.auscope.portal.core.services.cloud.CloudComputeServiceAws">
        <constructor-arg name="endpoint" value="ec2.ap-southeast-2.amazonaws.com"/>
        <constructor-arg name="apiVersion"><null/></constructor-arg>
        <constructor-arg name="accessKey" value="${env.aws.accesskey}"/>
        <constructor-arg name="secretKey" value="${env.aws.secretkey}"/>
        
        <property name="id" value="aws-ec2-compute"/>
        <property name="name" value="Amazon Web Services - EC2"/>
        <property name="stsRequirement" value="${aws.stsrequirement}"/>
        
        <property name="availableImages">
            <list>
                <bean class="org.auscope.portal.server.vegl.VglMachineImage">
                    <constructor-arg name="imageId" value="ap-southeast-2/ami-0487de67"/>
                    <property name="name" value="escript"/>
                    <property name="description"><value>A Debian (Jessie) machine with escript already installed.</value></property>
                    <property name="keywords">
                        <array>
                            <value>escript</value>
                            <value>debian</value>
                        </array>
                    </property>
                </bean>
                <bean class="org.auscope.portal.server.vegl.VglMachineImage">
                    <constructor-arg name="imageId" value="ap-southeast-2/ami-736b3010"/>
                    <property name="name" value="AEM-Inversion"/>
                    <property name="description"><value>A Debian (Jessie) machine with aem already installed.</value></property>
                    <property name="keywords">
                        <array>
                            <value>AEM-Inversion</value>
                            <value>debian</value>
                        </array>
                    </property>
                </bean>
            </list>
        </property>
    </bean> 
	*/
	
	@Bean
	public CloudComputeServiceNectar cloudComputeServiceNectar() throws UnknownHostException {
		CloudComputeServiceNectar computeService = new CloudComputeServiceNectar(
				"https://keystone.rc.nectar.org.au:5000/v2.0", nectarAccessKey, nectarSecretKey);
		computeService.setId("nectar-nova-compute");
		computeService.setName("National eResearch Collaboration Tools and Resources");
		//computeService.setGroupName("vl-#{inetAddress().hostName.toLowerCase()}");
		String groupName = "vl-";
		groupName += inetAddress().getHostName().toLowerCase();
		computeService.setGroupName(groupName);
		computeService.setKeypair("vgl-developers");
		return computeService;
		
	}
	
	/*
	<bean id="cloudComputeService-nectar" class="org.auscope.portal.core.services.cloud.CloudComputeServiceNectar">
        <constructor-arg name="endpoint" value="https://keystone.rc.nectar.org.au:5000/v2.0"/>
        <constructor-arg name="accessKey" value="${env.nectar.ec2.accesskey}"/>
        <constructor-arg name="secretKey" value="${env.nectar.ec2.secretkey}"/>
        <property name="id" value="nectar-nova-compute"/>
        <property name="name" value="National eResearch Collaboration Tools and Resources"/>
        <property name="groupName" value ="vl-#{inetAddress.hostName.toLowerCase()}"/>
        <property name="keypair" value="vgl-developers"/>
    </bean>
	*/
	
	
	@Bean
	public CloudComputeServiceNci cloudComputeServiceNci() {
		CloudComputeServiceNci computeService = new CloudComputeServiceNci(cloudStorageServiceNci(), "raijin.nci.org.au");
		computeService.setId("nci-raijin-compute");
		computeService.setName("National Computing Infrastructure - Raijin");
		return computeService;
	}
	
	/*
	<bean id="cloudComputeService-nci" class="org.auscope.portal.core.services.cloud.CloudComputeServiceNci">
        <constructor-arg name="storageService" ref="cloudStorageService-nci"/>
        <constructor-arg name="endpoint" value="raijin.nci.org.au"/>
        <property name="name" value="National Computing Infrastructure - Raijin"/>
        <property name="id" value="nci-raijin-compute"/>
    </bean> 
	*/
	
	@Bean
	public StagingInformation stagingInformation() {
		return new StagingInformation(stageInDirectory);
	}
	
	@Bean
	public ANVGLFileStagingService anvglFileStagingService() {
		return new ANVGLFileStagingService(stagingInformation());
	}
	
	/*
	<bean id="fileStagingService" class="org.auscope.portal.server.web.service.ANVGLFileStagingService">
        <constructor-arg name="stagingInformation">
            <bean class="org.auscope.portal.core.cloud.StagingInformation">
                <constructor-arg name="stageInDirectory" value="${localStageInDir}"/>
            </bean>
        </constructor-arg>
    </bean> 
	*/
	
	// Inject the configured solutions centre URL
	@Bean
	public MethodInvokingBean injectSsscUrl() {
		MethodInvokingBean ssscUrlBean = new MethodInvokingBean();
		ssscUrlBean.setStaticMethod("org.auscope.portal.server.web.service.ScmEntryService.setSolutionsUrl");
		ssscUrlBean.setArguments(solutionsUrl);
		return ssscUrlBean;
	}
	
	/*
	<!-- Inject the configured solutions centre URL -->
    <bean class="org.springframework.beans.factory.config.MethodInvokingBean">
      <property name="staticMethod"
                value="org.auscope.portal.server.web.service.ScmEntryService.setSolutionsUrl"/>
      <property name="arguments">
        <list>
          <value>${solutions.url}</value>
        </list>
      </property>
    </bean>
    */
	
	@Bean
	public CommonsMultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}
	
	/*
    <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
    </bean> 
	*/
    
    @Bean
    public CustomRegistry cswRegVeglProduction() {
    	return new CustomRegistry("cswRegVeglProduction", "SISS ANU Geonetwork",
    			"http://vgl-reg.auscope.org/geonetwork/srv/eng/csw",
    			"http://vgl-reg.auscope.org/geonetwork/srv/eng/main.home?uuid=%1$s");
    }
    
    /*
    <bean id="cswRegVeglProduction" class="org.auscope.portal.core.services.csw.custom.CustomRegistry">
        <constructor-arg name="id" value="cswRegVeglProduction" />
        <constructor-arg name="title" value="SISS ANU Geonetwork" />
        <constructor-arg name="serviceUrl" value="http://vgl-reg.auscope.org/geonetwork/srv/eng/csw" />
        <constructor-arg name="recordInformationUrl" value="http://vgl-reg.auscope.org/geonetwork/srv/eng/main.home?uuid=%1$s" />
    </bean>
    */

    @Bean
    public ServiceConfigurationItem nswerml2WFSTest() {
    	return new ServiceConfigurationItem("NSWERML2Test",
    			"http://aus-test-worker2.arrc.csiro.au:8080/gsnsw-earthresource/wfs", false, true);
    }
    
    /*
    <bean id="NSWERML2_WFSTest" class="org.auscope.portal.core.configuration.ServiceConfigurationItem">
        <constructor-arg name="id" value="NSWERML2Test"/>
        <constructor-arg name="url" value="http://aus-test-worker2.arrc.csiro.au:8080/gsnsw-earthresource/wfs"/>
        <constructor-arg name="paging" type="boolean" value="false"/>      
        <constructor-arg name="isGml32" type="boolean" value="true"/>        
    </bean>
    */

    @Bean
    public ServiceConfiguration serviceConfiguration2() {
    	List<ServiceConfigurationItem> serviceItems = new ArrayList<ServiceConfigurationItem>();
    	serviceItems.add(nswerml2WFSTest());
    	return new ServiceConfiguration(serviceItems);
    }
    
    /*
    <bean id="service-configuration2" class="org.auscope.portal.core.configuration.ServiceConfiguration">        
        <constructor-arg name="serviceConfigurationItems">
            <list>
               <ref bean="NSWERML2_WFSTest"/>         
            </list>
        </constructor-arg>
    </bean>
    */

    @Bean
    public ArrayList<CustomRegistry> serviceConfiguration() {
    	ArrayList<CustomRegistry> servConf = new ArrayList<CustomRegistry>();
    	servConf.add(cswRegVeglProduction());
    	return servConf;
    }
    
    /*
    <bean id="service-configuration" class="java.util.ArrayList">
        <constructor-arg>
            <list>
                <ref bean="cswRegVeglProduction" />
            </list>
        </constructor-arg>
    </bean>
    */
    
    @Bean 
    public ErmlNamespaceContext ermlNamespaceContext() {
    	return new ErmlNamespaceContext();
    }
    
    /*
    <!-- from wfsMethodMaker definition -->
    <bean class="org.auscope.portal.core.services.namespaces.ErmlNamespaceContext"/>
    */

    // Needed? wfsService() creates a new WFSGetFEatureMethodMaker, not sure if this is referenced anywhere
    @Bean
    public WFSGetFeatureMethodMaker wfsMethodMakerErmlNamespace() {
    	WFSGetFeatureMethodMaker methodMaker = new WFSGetFeatureMethodMaker();
    	methodMaker.setNamespaces(ermlNamespaceContext());
    	return methodMaker;
    }
    
    /*
    <bean id="wfsMethodMaker" class="org.auscope.portal.core.services.methodmakers.WFSGetFeatureMethodMaker">
        <property name="namespaces">
            <bean class="org.auscope.portal.core.services.namespaces.ErmlNamespaceContext"/>
        </property>
    </bean>
    */
}

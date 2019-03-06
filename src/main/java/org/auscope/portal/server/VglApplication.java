package org.auscope.portal.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/*
@ImportResource({"WEB-INF/profile-portal-production.xml",	// PortalProfileXmlWebApplicationContext (portal-core) 
				 "WEB-INF/vl-known-layers.xml", 			// VLWebAppContext
				 "WEB-INF/vl-registries.xml",				// VLWebAppContext 
				 "WEB-INF/applicationContext-security.xml", // VLWebAppContext
				 "WEB-INF/applicationContext.xml"})			// VglApplication
*/
//@Import(VLWebAppContext.class)
@SpringBootApplication//(exclude = { SecurityAutoConfiguration.class })
public class VglApplication extends SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		//return application.sources(VglApplication.class);
		return application;
	}

	public static void main(String[] args) {
		SpringApplication.run(VglApplication.class, args);
	}
}

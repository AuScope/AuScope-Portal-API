package org.auscope.portal.server;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(
	basePackages = {
			"org.auscope.portal.server",
			"au.gov.geoscience.portal.services.vocabularies",
			"org.auscope.portal.core"
    }
)
public class PortalApplication extends SpringBootServletInitializer {
	
	@Value("${portalUrl}")
	private String portalUrl;
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application;
	}
	
	@PostConstruct
    public void initProperties() {
        System.setProperty("portalUrl", portalUrl);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}

}

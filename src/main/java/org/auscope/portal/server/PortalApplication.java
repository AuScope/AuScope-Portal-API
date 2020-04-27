package org.auscope.portal.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.auscope.portal.server.config.ProfilePortalTest;
import org.auscope.portal.server.config.ProfilePortalProduction;

@SpringBootApplication
@ComponentScan(
       basePackages = {"org.auscope.portal.server",
                       "au.gov.geoscience.portal.services.vocabularies",
                       "org.auscope.portal.core.server.controllers"
                      },
//     To switch profiles between 'Test' and 'Production', you can
//     exclude either 'ProfilePortalTest' or 'ProfilePortalProduction'
       excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ProfilePortalTest.class)
       )
public class PortalApplication extends SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application;
	}

	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}

}

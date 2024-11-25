package org.auscope.portal.server;

import java.util.Map;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;


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
	
    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI().components(new Components()).info(new Info().title("AuScope API").version(appVersion)
                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .schema("ParameterMap", new Schema<Map<String, String>>().addProperty("sourceAccountId", 
                        new StringSchema().example("1")).addProperty("targetAccountId", new StringSchema().example("2")));
    }

}

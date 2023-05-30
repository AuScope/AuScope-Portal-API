package org.auscope.portal.server;

import javax.annotation.PostConstruct;

import org.auscope.portal.core.server.controllers.CSWFilterController;
import org.auscope.portal.server.web.controllers.BookMarksController;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

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


    private String moduleName = "AuScope"; // @value("${module-name}");
    private String apiVersion = "2.4.3"; // @value("${api-version}");

    /*
     * This bean groups the endpoints (which corresponds to the "definitions" in the swagger document
     * The groups are: Public and Internal
     * The grouping is based on the Spring Boot annotation @SecurityRequirement, which is specified in each
     * controller eg @SecurityRequirement(name = "internal")
     * 
     * note: I initially used the @Tag annotation as this allows a nice controller name in the swagger doc plus
     * a description - however it didn't work very well with groups, i.e. the controller ended up in both groups
     * but it didn't have endpoints for the group it was not a member of :-(
     * eg @Tag(name="WMS", description="Handles GetCapabilites (WFS)WMS queries")
     * 
     *  A better approach might be to remain the packages so that they have "internal" or "public" in the name
     *  then you can utilize the "packagesToScan" and "packagesToScan" methods
     *  
     *  note: Authorisation needs to be addressed
     */
    @Bean
    public GroupedOpenApi groupPublic() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomiser(openApiCustomiserPublic())
                .group("Public")
                .build();
    }
    
    OpenApiCustomiser openApiCustomiserPublic() {
        return openApi -> openApi.getPaths().entrySet().removeIf(path -> path.getValue().readOperations().stream().peek(p -> System.out.println("path(public):" + p))
                .anyMatch(operation -> operation.getSecurity() != null && operation.getSecurity().stream()
                .anyMatch(securityRequirement -> securityRequirement.containsKey("internal"))
                ));
    }
    
    @Bean
    public GroupedOpenApi groupInternal() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomiser(openApiCustomiserInternal())
                .group("Internal")
                .build();
    }

    private OpenApiCustomiser openApiCustomiserInternal() {
        return openApi -> openApi.getPaths().entrySet().removeIf(path -> path.getValue().readOperations().stream().peek(p -> System.out.println("path(internal):" + p))
            .anyMatch(operation -> operation.getSecurity() != null && operation.getSecurity().stream()
            .anyMatch(securityRequirement -> securityRequirement.containsKey("public"))
            ));
    }
    
    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI().components(new Components()).info(new Info().title("AuScope API").version(appVersion)
                .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
    

}

package org.auscope.portal.server.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import org.auscope.portal.core.services.csw.CSWServiceItem;

/* This class used to hold AuScope registries. These are defined in two YAML files:
 * (1) src/main/resources/application-prod.yaml (registries used in production builds)
 * (2) src/main/resources/application-test.yaml (registries used in test builds)
 *
 * Only one file is used at a time, depending on build profile, which is set in pom.xml
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "auscope") // Binds all elements under 'auscope' in the YAML
                                             // file to the members of this class
public class AuscopeRegistries {

    private List<CSWServiceItem> registries = new ArrayList<CSWServiceItem>();
 
    @Bean
    public List<CSWServiceItem> auscopeRegistryList() {
        return this.registries;
    }

    public List<CSWServiceItem> getRegistries() {
        return registries;
    }

    public void setRegistries(List<CSWServiceItem> registries) {
        this.registries = registries;
    }
}

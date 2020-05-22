package org.auscope.portal.server.config;


import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

import org.auscope.portal.core.services.csw.CSWServiceItem;

/**
 * Bind the registries defined in application-registries.yaml to a list of
 * CSWServiceItems
 * 
 * @author woo392
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix="csw")
public class Registries {

    // List of AuScope registries, which varies according to build profile
    // and is defined in separate YAML files labelled with profile name
    @Autowired
    AuscopeRegistries auscopeRegistryList;
 
    private List<CSWServiceItem> registries= new ArrayList<CSWServiceItem>();

    public List<CSWServiceItem> getRegistries() {
        return registries;
    }
 
    public void setRegistries(List<CSWServiceItem> registries) {
        this.registries = registries;
    }

    /* This bean contains the portal's list of registries */
    @Bean
    public ArrayList<CSWServiceItem> cswServiceList() {
        // Create a copy of 'registries'
        ArrayList<CSWServiceItem> retList = new ArrayList<CSWServiceItem>(registries);

        // Add in the AuScope registries list
        retList.addAll(auscopeRegistryList.getRegistries());
        return retList;
    }

}

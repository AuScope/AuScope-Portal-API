package org.auscope.portal.server.config;


import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.auscope.portal.core.services.csw.CSWServiceItem;

/**
 * Bind the registries defined in application-registries.yaml to a list of
 * CSWServiceItems
 * 
 * @author woo392
 *
 */
@Configuration
@Profile("vl-portal-prod")
@EnableConfigurationProperties
@ConfigurationProperties(prefix="csw")
public class VlRegistries {
	
	private List<CSWServiceItem> registries= new ArrayList<CSWServiceItem>();
	
	
	public List<CSWServiceItem> getRegistries() {
		return registries;
	}

	public void setRegistries(List<CSWServiceItem> registries) {
		this.registries = registries;
	}

	@Bean
	public ArrayList<CSWServiceItem> cswServiceList() {
		return new ArrayList<CSWServiceItem>(registries);
	}

}

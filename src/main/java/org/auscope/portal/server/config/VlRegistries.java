package org.auscope.portal.server.config;

import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class VlRegistries {

	@Bean
	CSWServiceItem cswVeglProduction() {
		return new CSWServiceItem("cswVeglProduction",
				"http://vgl-reg.auscope.org/geonetwork/srv/eng/csw",
				"http://vgl-reg.auscope.org/geonetwork/srv/eng/main.home?uuid=%1$s",
				"SISS ANU Geonetwork");
	}
	/*
	<bean id="cswVeglProduction" class="org.auscope.portal.core.services.csw.CSWServiceItem">
        <constructor-arg name="id" value="cswVeglProduction"/>
        <constructor-arg name="title" value="SISS ANU Geonetwork"/>
        <constructor-arg name="serviceUrl" value="http://vgl-reg.auscope.org/geonetwork/srv/eng/csw"/>
        <constructor-arg name="recordInformationUrl" value="http://vgl-reg.auscope.org/geonetwork/srv/eng/main.home?uuid=%1$s"/>
    </bean>
    */

	@Bean
	CSWServiceItem cswGAECat() {
		return new CSWServiceItem("cswGAECat",
				"https://ecat.ga.gov.au/geonetwork/srv/eng/csw",
				"https://ecat.ga.gov.au/geonetwork/srv/eng/main.home?uuid=%1$s",
				"Geoscience Australia eCat");
	}
	
	/*
    <bean id="cswGAECat" class="org.auscope.portal.core.services.csw.CSWServiceItem">
        <constructor-arg name="id" value="cswGAECat"/>
        <constructor-arg name="title" value="Geoscience Australia eCat"/>
        <constructor-arg name="serviceUrl" value="https://ecat.ga.gov.au/geonetwork/srv/eng/csw"/>
        <constructor-arg name="recordInformationUrl" value="https://ecat.ga.gov.au/geonetwork/srv/eng/main.home?uuid=%1$s"/>
    </bean>
    */
	
	@Bean
	CSWServiceItem cswNCI() {
		return new CSWServiceItem("cswNCI",
				"http://geonetworkrr2.nci.org.au/geonetwork/srv/eng/csw",
				"https://geonetworkrr2.nci.org.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s",
				"NCI Data Portal");
	}
	
	@Bean
	CSWServiceItem cswNigel() {
		return new CSWServiceItem("cswNigel",
				"http://130.56.244.85/geonetwork/srv/eng/csw",
				"http://130.56.244.85/geonetwork/srv/eng/catalog.search#/metadata/%1$s",
				"Nigel's test Data Portal");
	}
	/*
    <bean id="cswNCI" class="org.auscope.portal.core.services.csw.CSWServiceItem">
        <constructor-arg name="id" value="cswNCI"/>
        <constructor-arg name="title" value="NCI Data Portal"/>
        <constructor-arg name="serviceUrl" value="http://geonetworkrr2.nci.org.au/geonetwork/srv/eng/csw"/>
        <constructor-arg name="recordInformationUrl" value="https://geonetworkrr2.nci.org.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s"/>
    </bean>
	*/
}

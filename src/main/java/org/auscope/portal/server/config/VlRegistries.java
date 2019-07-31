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
	CSWServiceItem cswNciRR2() {
		return new CSWServiceItem("cswNciRR2",
				"http://geonetworkrr2.nci.org.au/geonetwork/srv/eng/csw",
				"https://geonetworkrr2.nci.org.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s",
				"NCI Geophysics Data Portal");
	}
	/*
	<bean id="cswNciRR2" class="org.auscope.portal.core.services.csw.CSWServiceItem">
	    <constructor-arg name="id" value="cswNciRR2"/>
	    <constructor-arg name="title" value="NCI Geophysics Data Portal"/>
	    <constructor-arg name="serviceUrl" value="http://geonetworkrr2.nci.org.au/geonetwork/srv/eng/csw"/>
	    <constructor-arg name="recordInformationUrl" value="https://geonetworkrr2.nci.org.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s"/>
    </bean>
    */
	
	@Bean
	CSWServiceItem cswNCI() {
		return new CSWServiceItem("cswNCI",
				"https://geonetwork.nci.org.au/geonetwork/srv/eng/csw",
				"https://geonetwork.nci.org.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s",
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
	
	@Bean
	CSWServiceItem cswNciGSWA() {
		return new CSWServiceItem("cswNciGswa",
				"http://geonetworkrl1.nci.org.au/geonetwork/srv/eng/csw",
				"https://geonetworkrl1.nci.org.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s",
				"NCI GSWA Data Portal");
	}
	
	/*
	<bean id="cswNciGSWA" class="org.auscope.portal.core.services.csw.CSWServiceItem">
	    <constructor-arg name="id" value="cswNciGswa"/>
	    <constructor-arg name="title" value="NCI GSWA Data Portal"/>
	    <constructor-arg name="serviceUrl" value="http://geonetworkrl1.nci.org.au/geonetwork/srv/eng/csw"/>
	    <constructor-arg name="recordInformationUrl" value="https://geonetworkrl1.nci.org.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s"/>
    </bean>
    */
	
	@Bean
	CSWServiceItem cswNciMT() {
		return new CSWServiceItem("cswNciMt",
				"https://geonetworktest.nci.org.au/geonetwork/srv/eng/csw",
				"https://geonetwork.nci.org.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s",
				"NCI MT Data Portal");
	}

	/*
	<bean id="cswNciMT" class="org.auscope.portal.core.services.csw.CSWServiceItem">
	    <constructor-arg name="id" value="cswNciMt"/>
	    <constructor-arg name="title" value="NCI MT Data Portal"/>
	    <constructor-arg name="serviceUrl" value="https://geonetworktest.nci.org.au/geonetwork/srv/eng/csw"/>
	    <constructor-arg name="recordInformationUrl" value="https://geonetwork.nci.org.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s"/>
    </bean>
    */
	
	@Bean
	CSWServiceItem cswUrbanMonitor() {
	    return new CSWServiceItem("cswUrbanMonitor",
	            "http://dcdpgeo.data61.csiro.au:8080/api",
	            "http://dcdpgeo.data61.csiro.au:8080/api/catalog.search#/metadata/%1$s",
	            "Urban Monitor");
	}
	
	/*
	<bean id="cswUrbanMonitor" class="org.auscope.portal.core.services.csw.CSWServiceItem">
        <constructor-arg name="id" value="cswUrbanMonitor"/>
        <constructor-arg name="title" value="Urban Monitor"/>
        <constructor-arg name="serviceUrl" value="http://dcdpgeo.data61.csiro.au:8080/api"/>
        <constructor-arg name="recordInformationUrl" value="http://dcdpgeo.data61.csiro.au:8080/api/catalog.search#/metadata/%1$s"/>
    </bean>
	 */

}

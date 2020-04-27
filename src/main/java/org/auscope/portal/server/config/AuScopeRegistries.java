package org.auscope.portal.server.config;

import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AuScopeRegistries {

    @Bean
    public CSWServiceItem cswAuscopeTest() {
        return(new CSWServiceItem("cswAuscopeTest",
                                  "http://au-portal-2.it.csiro.au/geonetwork/srv/eng/csw",
                                  "http://au-portal-2.it.csiro.au/geonetwork/srv/eng/catalog.search#/metadata/%1$s",
                                  "AuScope Test Geonetwork"));
    }


    @Bean
    public CSWServiceItem cswAuscopeProduction() {
        return(new CSWServiceItem("cswAuscopeProduction",
                                  "http://portal.auscope.org/geonetwork/srv/eng/csw",
                                  "http://portal.auscope.org/geonetwork/srv/eng/catalog.search#/metadata/%1$s",
                                  "AuScope Geonetwork"));
    }


    // @Bean
    // public CSWServiceItem cswMDUTest() {
    //     return(new CSWServiceItem("cswMDUTest",
    //                               "http://mdu-data-2.arrc.csiro.au/geonetwork/srv/en/csw",
    //                               "http://mdu-data-2.arrc.csiro.au/geonetwork/srv/en/metadata.show?uuid=%1$s",
    //                               "MDU Test Geonetwork"));
    // }


    // @Bean
    // public CSWServiceItem cswMDUProduction() {
    //     return(new CSWServiceItem("cswMDUProduction",
    //                               "http://mdu-data.arrc.csiro.au/geonetwork/srv/en/csw",
    //                               "http://mdu-data.arrc.csiro.au/geonetwork/srv/en/main.home?uuid=%1$s",
    //                               "MDU-Geonetwork"));
    // }


    @Bean
    public CSWServiceItem cswGAPMDCRC() {
        return(new CSWServiceItem("cswGAPMDCRC",
                                  "http://www.ga.gov.au/geonetwork/srv/en/csw",
                                  "http://www.ga.gov.au/geonetwork/srv/en/csw?request=GetRecordById&amp;service=CSW&amp;version=2.0.2&amp;OUTPUTSCHEMA=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id=%1$s",
                                  "Geoscience Australia PMD*CRC Publications",
                                  "csw:AnyText Like '%PMD*CRC Publication%' AND BBOX(ows:BoundingBox, 110,-9,160,-55)"));
    }
}

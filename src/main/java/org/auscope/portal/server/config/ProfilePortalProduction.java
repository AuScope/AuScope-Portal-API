package org.auscope.portal.server.config;

import java.util.ArrayList;

import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Definitions for all known layers
 */
 
@Configuration
public class ProfilePortalProduction {

/* When running a local development server, uncomment this to disable CORS checking */
//@Bean
//public WebMvcConfigurer configurer() {
//    return new WebMvcConfigurer(){
//        @Override
//        public void addCorsMappings(CorsRegistry registry) {
//            registry.addMapping("/*").allowedOrigins("*");
//        }
//    };
//}

    @Autowired
    KnownLayer knownTypeMine;
    @Autowired
    KnownLayer knownTypeErlMineView;
    @Autowired
    KnownLayer knownTypeMineralOccurrence;
    @Autowired
    KnownLayer knownTypeErlMineralOccurrenceView;
    @Autowired
    KnownLayer knownTypeErlCommodityResourceView;                
    @Autowired
    KnownLayer knownTypeMiningActivity;
    @Autowired
    KnownLayer knownTypeMineralTenements;             
    @Autowired
    KnownLayer knownTypeRemanentAnomalies;
    @Autowired
    KnownLayer knownTypeRemanentAnomaliesTMI;
    @Autowired
    KnownLayer knownTypeRemanentAnomaliesAutoSearch;
    @Autowired
    KnownLayer knownTypeEMAGRemanentAnomalies;
    @Autowired
    KnownLayer knownTypeEMAGRemanentAnomaliesTMI;
    @Autowired
    KnownLayer knownTypeMineralOccurrenceView;
    @Autowired
    KnownLayer knownTypeBoreholeNvclV2;                
    @Autowired
    KnownLayer knownTypeReports;
    @Autowired
    KnownLayer knownTypePMDCRCReports;
    @Autowired
    KnownLayer knownTypeGeotransects;
    @Autowired
    KnownLayer knownTypeTimaGeoSample;
    @Autowired
    KnownLayer knownTypeSHRIMPGeoSample;
    @Autowired
    KnownLayer knownTypeYilgarnGeochemistry;
    @Autowired
    KnownLayer knownTypeSamplingPoint;
    @Autowired
    KnownLayer knownTypeFeatureCollection;
    @Autowired
    KnownLayer knownTypeLateriteYilgarnGeoChem;
    @Autowired
    KnownLayer knownTypeHighPSiteFeatureType;
    @Autowired
    KnownLayer knownTypeHighPFeatureType;
    @Autowired
    KnownLayer knownTypeHighPREIronLayer;
    @Autowired
    KnownLayer knownTypeHighPREPhosLayer;
    @Autowired
    KnownLayer knownTypeHighPSiteIronLayer;
    @Autowired
    KnownLayer knownTypeHighPSitePhosLayer;
    @Autowired
    KnownLayer knownTypePortals;
    @Autowired
    KnownLayer knownTypeGeoNetworks;
    @Autowired
    KnownLayer knownTypeAster;
    @Autowired
    KnownLayer knownTypeAsterAloh;
    @Autowired
    KnownLayer knownTypeAsterFerrous;
    @Autowired
    KnownLayer knownTypeAsterOpaque;
    @Autowired
    KnownLayer knownTypeAsterFerricOxideContent;
    @Autowired
    KnownLayer knownTypeAsterFeoh;
    @Autowired
    KnownLayer knownTypeFerricOxideComp;
    @Autowired
    KnownLayer knownTypeGroupIndex;
    @Autowired
    KnownLayer knownTypeQuartzIndex;
    @Autowired
    KnownLayer knownTypeMgohContent;
    @Autowired
    KnownLayer knownTypeGreenVeg;
    @Autowired
    KnownLayer knownTypeFerrCarb;
    @Autowired
    KnownLayer knownTypeMgohGroupComp;
    @Autowired
    KnownLayer knownTypeAlohGroupContent;
    @Autowired
    KnownLayer knownTypeGypsumContent;
    @Autowired
    KnownLayer knownTypeSilicaContent;
    @Autowired
    KnownLayer knownTypeBoreholeMSCL;
    @Autowired
    KnownLayer knownTypeSeismologyInSchool;
    @Autowired
    KnownLayer knownTypeSF0BoreholeNVCL;

    /* Generated from former prod "Registered" tab START */
    @Autowired
    KnownLayer knownTypeHighpSiteIronlayer;
    @Autowired
    KnownLayer knownTypeHighpfeaturetype;
    @Autowired
    KnownLayer knownTypeLateriteWesternYilgarnGeochem;
    @Autowired
    KnownLayer knownTypeHighpSitePhoslayer;
    @Autowired
    KnownLayer knownTypeHighpRePhoslayer;
    @Autowired
    KnownLayer knownTypeHighpSiteFeatureType;
    @Autowired
    KnownLayer knownTypeHighpReIronlayer;
    @Autowired
    KnownLayer knownTypeStrategicProspectivityZones;
    @Autowired
    KnownLayer knownTypeCate5ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeRsSample;
    @Autowired
    KnownLayer knownTypeMineOccuPoinOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGravBaseStatOfTasmMinResoTasm;

    
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit250KLithology;
    @Autowired
    KnownLayer knownTypeCate2ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeCate6ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeCate4ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeAirbGeopSurvOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeBoreTracOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeLandDamaPolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeLandLineOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeLandPoinOfTasmMinResoTasm;
                
    @Autowired
    KnownLayer knownTypeLandPolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeLandDamaPoinOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit50K;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit50KLithology;
    @Autowired
    KnownLayer knownTypeGravMeasOfTasmMinResoTasm;
                
    @Autowired
    KnownLayer knownTypeMiniLeasPolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeBorePoinOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeCate3ExplLicePolyOfTasmMinResoTasm;
                
    @Autowired
    KnownLayer knownTypeGsvShearDisplacementStructure50K;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnitContact250K;
    @Autowired
    KnownLayer knownTypeGsvShearDisplacementStructure250K;
    @Autowired
    KnownLayer knownTypeCate1ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit250K;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnitContact50K;
    @Autowired
    KnownLayer knownTypeProcLandAreaOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit250KAge;
    /* Generated from former prod "Registered" tab END */

    @Autowired
    KnownLayer knownTypeGeoModels;
    @Autowired
    KnownLayer knownTypeGeologicalProvinces;
    
    /* UOW OCTOPUS Open Cosmogenic Isotope and Luminescence Database */
    @Autowired
    KnownLayer knownTypeUOWCrnAusBasins;
    @Autowired
    KnownLayer knownTypeUOWCrnAusOutlets;
    @Autowired
    KnownLayer knownTypeUOWCrnInprepBasins;
    @Autowired
    KnownLayer knownTypeUOWCrnInprepOutlets;
    @Autowired
    KnownLayer knownTypeUOWCrnIntBasins;
    @Autowired
    KnownLayer knownTypeUOWCrnIntOutlets;
    @Autowired
    KnownLayer knownTypeUOWCrnXXLBasins;
    @Autowired
    KnownLayer knownTypeUOWCrnXXLOutlets;
    @Autowired
    KnownLayer knownTypeUOWOSLTLBasins;
    @Autowired
    KnownLayer knownTypeUOWOSLTLOutlets;
    /* END UOW OCTOPUS */


    @Bean
	public ArrayList<KnownLayer> knownTypes() {
		ArrayList<KnownLayer> knownLayers = new ArrayList<KnownLayer>();
    
        knownLayers.add(knownTypeMine);
        knownLayers.add(knownTypeErlMineView);
        knownLayers.add(knownTypeMineralOccurrence);
        knownLayers.add(knownTypeErlMineralOccurrenceView);
        knownLayers.add(knownTypeErlCommodityResourceView);                
        knownLayers.add(knownTypeMiningActivity);
        knownLayers.add(knownTypeMineralTenements);               
        knownLayers.add(knownTypeRemanentAnomalies);
        knownLayers.add(knownTypeRemanentAnomaliesTMI);
        knownLayers.add(knownTypeRemanentAnomaliesAutoSearch);
        knownLayers.add(knownTypeEMAGRemanentAnomalies);
        knownLayers.add(knownTypeEMAGRemanentAnomaliesTMI);
        knownLayers.add(knownTypeMineralOccurrenceView);
        knownLayers.add(knownTypeBoreholeNvclV2                );
        knownLayers.add(knownTypeReports);
        knownLayers.add(knownTypePMDCRCReports);
        knownLayers.add(knownTypeGeotransects);
        knownLayers.add(knownTypeTimaGeoSample);
        knownLayers.add(knownTypeSHRIMPGeoSample);
        knownLayers.add(knownTypeYilgarnGeochemistry);
        knownLayers.add(knownTypeSamplingPoint);
        knownLayers.add(knownTypeFeatureCollection);
        knownLayers.add(knownTypeLateriteYilgarnGeoChem);
        knownLayers.add(knownTypeHighPSiteFeatureType);
        knownLayers.add(knownTypeHighPFeatureType);
        knownLayers.add(knownTypeHighPREIronLayer);
        knownLayers.add(knownTypeHighPREPhosLayer);
        knownLayers.add(knownTypeHighPSiteIronLayer);
        knownLayers.add(knownTypeHighPSitePhosLayer);
        knownLayers.add(knownTypePortals);
        knownLayers.add(knownTypeGeoNetworks);
        knownLayers.add(knownTypeAster);
        knownLayers.add(knownTypeAsterAloh);
        knownLayers.add(knownTypeAsterFerrous);
        knownLayers.add(knownTypeAsterOpaque);
        knownLayers.add(knownTypeAsterFerricOxideContent);
        knownLayers.add(knownTypeAsterFeoh);
        knownLayers.add(knownTypeFerricOxideComp);
        knownLayers.add(knownTypeGroupIndex);
        knownLayers.add(knownTypeQuartzIndex);
        knownLayers.add(knownTypeMgohContent);
        knownLayers.add(knownTypeGreenVeg);
        knownLayers.add(knownTypeFerrCarb);
        knownLayers.add(knownTypeMgohGroupComp);
        knownLayers.add(knownTypeAlohGroupContent);
        knownLayers.add(knownTypeGypsumContent);
        knownLayers.add(knownTypeSilicaContent);
        knownLayers.add(knownTypeBoreholeMSCL);
        knownLayers.add(knownTypeSeismologyInSchool);
        knownLayers.add(knownTypeSF0BoreholeNVCL);
        /* Generated from former prod "Registered" tab START */
        
        knownLayers.add(knownTypeHighpSiteIronlayer);
        knownLayers.add(knownTypeHighpfeaturetype);
        knownLayers.add(knownTypeLateriteWesternYilgarnGeochem);
        knownLayers.add(knownTypeHighpSitePhoslayer);
        knownLayers.add(knownTypeHighpRePhoslayer);
        knownLayers.add(knownTypeHighpSiteFeatureType);
        knownLayers.add(knownTypeHighpReIronlayer);
        knownLayers.add(knownTypeStrategicProspectivityZones);
        knownLayers.add(knownTypeCate5ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeRsSample);
        knownLayers.add(knownTypeMineOccuPoinOfTasmMinResoTasm);
        knownLayers.add(knownTypeGravBaseStatOfTasmMinResoTasm);
        
        knownLayers.add(knownTypeGsvGeologicalUnit250KLithology);
        knownLayers.add(knownTypeCate2ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeCate6ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeCate4ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeAirbGeopSurvOfTasmMinResoTasm);
        knownLayers.add(knownTypeBoreTracOfTasmMinResoTasm);
        knownLayers.add(knownTypeLandDamaPolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeLandLineOfTasmMinResoTasm);
        knownLayers.add(knownTypeLandPoinOfTasmMinResoTasm);

        knownLayers.add(knownTypeLandPolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeLandDamaPoinOfTasmMinResoTasm);
        knownLayers.add(knownTypeGsvGeologicalUnit50K);
        knownLayers.add(knownTypeGsvGeologicalUnit50KLithology);
        knownLayers.add(knownTypeGravMeasOfTasmMinResoTasm);

        knownLayers.add(knownTypeMiniLeasPolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeBorePoinOfTasmMinResoTasm);
        knownLayers.add(knownTypeCate3ExplLicePolyOfTasmMinResoTasm);

        knownLayers.add(knownTypeGsvShearDisplacementStructure50K);
        knownLayers.add(knownTypeGsvGeologicalUnitContact250K);
        knownLayers.add(knownTypeGsvShearDisplacementStructure250K);
        knownLayers.add(knownTypeCate1ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeGsvGeologicalUnit250K);
        knownLayers.add(knownTypeGsvGeologicalUnitContact50K);
        knownLayers.add(knownTypeProcLandAreaOfTasmMinResoTasm);
        knownLayers.add(knownTypeGsvGeologicalUnit250KAge);
        /* Generated from former prod "Registered" tab*/

        knownLayers.add(knownTypeGeoModels);
        knownLayers.add(knownTypeGeologicalProvinces);

        /* UOW OCTOPUS Open Cosmogenic Isotope and Luminescence Database */
        knownLayers.add(knownTypeUOWCrnAusBasins);
        knownLayers.add(knownTypeUOWCrnAusOutlets);
        knownLayers.add(knownTypeUOWCrnInprepBasins);
        knownLayers.add(knownTypeUOWCrnInprepOutlets);
        knownLayers.add(knownTypeUOWCrnIntBasins);
        knownLayers.add(knownTypeUOWCrnIntOutlets);
        knownLayers.add(knownTypeUOWCrnXXLBasins);
        knownLayers.add(knownTypeUOWCrnXXLOutlets);
        knownLayers.add(knownTypeUOWOSLTLBasins);
        knownLayers.add(knownTypeUOWOSLTLOutlets);
        /* END UOW OCTOPUS */

		return knownLayers;
	}
    
    
    
    
    @Autowired
    @Qualifier("cswAuscopeProduction")
    CSWServiceItem cswAuscopeProduction;
    
    // @Autowired
    // @Qualifier("cswMDUProduction")
    // CSWServiceItem cswMDUProduction;
    
    @Autowired
    @Qualifier("cswGAPMDCRC")
    CSWServiceItem cswGAPMDCRC;
    
    @Bean
    @Primary
    @Autowired
    public ArrayList<CSWServiceItem> cswServiceList() {
		ArrayList<CSWServiceItem> serviceList = new ArrayList<CSWServiceItem>();
		serviceList.add(cswAuscopeProduction);
		// serviceList.add(cswMDUProduction);
		serviceList.add(cswGAPMDCRC);
		return serviceList;
    }

}
    
 

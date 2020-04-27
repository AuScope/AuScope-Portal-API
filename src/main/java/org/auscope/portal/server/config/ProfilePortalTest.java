package org.auscope.portal.server.config;

import java.util.ArrayList;

import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;


/**
 * Definitions for all known layers
 */
 
@Configuration
public class ProfilePortalTest {

/* Allow CORS for development purposes */
@Bean
public WebMvcConfigurer configurer() {
    return new WebMvcConfigurer(){
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/*").allowedOrigins("*");
        }
    };
}
    
    @Autowired
    KnownLayer knownTypeMine;
    @Autowired
    KnownLayer knownTypeErlMineView;
    @Autowired
    KnownLayer knownTypeMineralOccurrence;
    @Autowired
    KnownLayer knownTypeErlMineralOccurrenceView;
    @Autowired
    KnownLayer knownTypeErlCommodityResourceView  ;
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

    /* Generated from former test "Registered" tab START */
    @Autowired
    KnownLayer knownTypeHighpSiteIronlayer;
    @Autowired
    KnownLayer knownTypeHighpfeaturetype;
    @Autowired
    KnownLayer knownTypeLateriteWesternYilgarnGeochem;
    @Autowired
    KnownLayer knownTypeHighpReIronlayer;
    @Autowired
    KnownLayer knownTypeHighpSiteFeatureType;
    @Autowired
    KnownLayer knownTypeHighpSitePhoslayer;
    @Autowired
    KnownLayer knownTypeHighpRePhoslayer;
    @Autowired
    KnownLayer knownTypeLandDamaPolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeNswDrillhole;
    @Autowired
    KnownLayer knownTypeStrategicProspectivityZones;
    @Autowired
    KnownLayer knownTypeMineralFields;
    @Autowired
    KnownLayer knownTypeNswAssaySurface;
    @Autowired
    KnownLayer knownTypeNswFieldObservations;
    @Autowired
    KnownLayer knownTypeLandDamaPoinOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGravMeasOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeNswDownholeAssaySamples;
    @Autowired
    KnownLayer knownTypeCate6ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit50K;
    @Autowired
    KnownLayer knownTypeNswDrillholeAll;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit50KAge;
    @Autowired
    KnownLayer knownTypeNswDrillholesCoal;
    @Autowired
    KnownLayer knownTypeNswGeologicalFieldObservations;
    @Autowired
    KnownLayer knownTypeNswDrillholesMinerals;
    @Autowired
    KnownLayer knownTypeNswGeologySimplified;
    @Autowired
    KnownLayer knownTypeHistoricalExplorationActivityPoints;
    @Autowired
    KnownLayer knownTypeNswDrillholesPetroleum;
    @Autowired
    KnownLayer knownTypeCate1ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGravBaseStatOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeRadnlOperR25PcprrL3Knmi;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit250KAge;
    @Autowired
    KnownLayer knownTypeReleases;
    @Autowired
    KnownLayer knownTypeMineOccuPoinOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGsvShearDisplacementStructure50K;
    @Autowired
    KnownLayer knownTypeMineralOccurenceIndustryFull;
    @Autowired
    KnownLayer knownTypeNswHistoricExplorationTitles;
    @Autowired
    KnownLayer knownTypeCate2ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeNswMapBlockGraticule;
    @Autowired
    KnownLayer knownTypeSection574;
    @Autowired
    KnownLayer knownTypeBlLocalaboriginallandcouncil;
    @Autowired
    KnownLayer knownTypeLandLineOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeMiniLeasPolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeNswMineralOccurrenceIndustry;
    @Autowired
    KnownLayer knownTypeLandPoinOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeProcLandAreaOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit50KLithology;
    @Autowired
    KnownLayer knownTypeSection19;
    @Autowired
    KnownLayer knownTypeNsw100KMapSheetExtents;
    @Autowired
    KnownLayer knownTypeCate5ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeMinesAndMineralDepositsMinedex;
    @Autowired
    KnownLayer knownTypeNswFossickingDistricts;
    @Autowired
    KnownLayer knownTypeNswGeologicalFieldObservationsPhoto;
    @Autowired
    KnownLayer knownTypeGeopTotaMagnInteRtpTmiRtpTiltFilt;
    @Autowired
    KnownLayer knownTypeNswSeismic;
    
    @Autowired
    KnownLayer knownTypeNswCurrMiniAndExplTitl;
    @Autowired
    KnownLayer knownTypeBorePoinOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGeopTotaMagnInte1StDeriReduToPole;
    @Autowired
    KnownLayer knownTypeNswGeologicalSpectralSamples;
    @Autowired
    KnownLayer knownTypeGeopTernRadiPota;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit250K;
    @Autowired
    KnownLayer knownTypeCate4ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeMineralisationZonesNonS572Aa;
    @Autowired
    KnownLayer knownTypeNswDrillholesCsg;
    @Autowired
    KnownLayer knownTypeNswCurrentMiningApplications;
    @Autowired
    KnownLayer knownTypeAmalgamations;
    @Autowired
    KnownLayer knownTypeNswOperatingMineralMines;
    
    @Autowired
    KnownLayer knownTypeTenementsCurrent;
    @Autowired
    KnownLayer knownTypeBoreTracOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnitContact250K;
    @Autowired
    KnownLayer knownTypeGeophysicsElevation;
    @Autowired
    KnownLayer knownTypeNswGeolSimpRockUnitBoun;
    @Autowired
    KnownLayer knownTypeBlocksAndUnitsGraticule;
    @Autowired
    KnownLayer knownTypeAirbGeopSurvOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeNswMapUnitGraticule;
    @Autowired
    KnownLayer knownTypeCate3ExplLicePolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeNswGeochronology;
    
    @Autowired
    KnownLayer knownTypeGeophysicsIsostaticBougerGravity;
    @Autowired
    KnownLayer knownTypeGeopTotaMagnInteReduToPole;
    @Autowired
    KnownLayer knownTypeNswCoreLibrarySamples;
    @Autowired
    KnownLayer knownTypeNswGeochemistrySamples;
    @Autowired
    KnownLayer knownTypeNswTitles;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnit250KLithology;
    
    @Autowired
    KnownLayer knownTypeHistoricalExplorationActivityLines;
    @Autowired
    KnownLayer knownTypeRestorations;
    @Autowired
    KnownLayer knownTypeBorehole;
    @Autowired
    KnownLayer knownTypeNswGeology;
    @Autowired
    KnownLayer knownTypeGeopIsosGravOverTmiRtpTilt;
    @Autowired
    KnownLayer knownTypeGsvShearDisplacementStructure250K;
    @Autowired
    KnownLayer knownTypeNswLithologySamples;
    @Autowired
    KnownLayer knownTypeRadnlOperR25PcprrL3Color;
    @Autowired
    KnownLayer knownTypeNswBase;
    @Autowired
    KnownLayer knownTypeHist;
    @Autowired
    KnownLayer knownTypeNswMineralOccurrenceCommodity;
    @Autowired
    KnownLayer knownTypeLandPolyOfTasmMinResoTasm;
    @Autowired
    KnownLayer knownTypeGsvGeologicalUnitContact50K;
    @Autowired
    KnownLayer knownTypeL180MtIsaDeepCrusSeisSurvQld2006StacAndMigrDataAndImagForLine06GaTo06Ga;
    @Autowired
    KnownLayer knownTypeAreTherAnySandUranSystInTheEromBasi;
    @Autowired
    KnownLayer knownTypeL164CurnSeisSurvSa20032004StacAndMigrSeisDataAndImagForLine03Ga;
    @Autowired
    KnownLayer knownTypeLawnHillPlatAndLeicRiveFaulTrouMeasStraSectOnliGis;
    @Autowired
    KnownLayer knownTypePredMineDiscInTheEastYilgCratAnExamOfDistTargOfAnOrogGoldMineSyst;
    @Autowired
    KnownLayer knownTypeFinaRepo3DGeolModeOfTheEastYilgCratProjPmdY2Sept2001Dece2004;
    
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
        knownLayers.add(knownTypeErlCommodityResourceView  );
        knownLayers.add(knownTypeMiningActivity);
        knownLayers.add(knownTypeMineralTenements);
        knownLayers.add(knownTypeRemanentAnomalies);
        knownLayers.add(knownTypeRemanentAnomaliesTMI);
        knownLayers.add(knownTypeRemanentAnomaliesAutoSearch);
        knownLayers.add(knownTypeEMAGRemanentAnomalies);
        knownLayers.add(knownTypeEMAGRemanentAnomaliesTMI);
        knownLayers.add(knownTypeMineralOccurrenceView);
        knownLayers.add(knownTypeBoreholeNvclV2);
        knownLayers.add(knownTypeReports);
        knownLayers.add(knownTypePMDCRCReports);
        knownLayers.add(knownTypeGeotransects);
        knownLayers.add(knownTypeTimaGeoSample);
        knownLayers.add(knownTypeSHRIMPGeoSample);
        knownLayers.add(knownTypeYilgarnGeochemistry);
        knownLayers.add(knownTypeSamplingPoint);
        knownLayers.add(knownTypeFeatureCollection);
        knownLayers.add(knownTypeLateriteYilgarnGeoChem);
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

        /* Generated from former test "Registered" tab START */
        knownLayers.add(knownTypeHighpSiteIronlayer);
        knownLayers.add(knownTypeHighpfeaturetype);
        knownLayers.add(knownTypeLateriteWesternYilgarnGeochem);
        knownLayers.add(knownTypeHighpReIronlayer);
        knownLayers.add(knownTypeHighpSiteFeatureType);
        knownLayers.add(knownTypeHighpSitePhoslayer);
        knownLayers.add(knownTypeHighpRePhoslayer);
        knownLayers.add(knownTypeLandDamaPolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeNswDrillhole);
        knownLayers.add(knownTypeStrategicProspectivityZones);
        knownLayers.add(knownTypeMineralFields);
        knownLayers.add(knownTypeNswAssaySurface);
        knownLayers.add(knownTypeNswFieldObservations);
        knownLayers.add(knownTypeLandDamaPoinOfTasmMinResoTasm);
        knownLayers.add(knownTypeGravMeasOfTasmMinResoTasm);
        knownLayers.add(knownTypeNswDownholeAssaySamples);
        knownLayers.add(knownTypeCate6ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeGsvGeologicalUnit50K);
        knownLayers.add(knownTypeNswDrillholeAll);
        knownLayers.add(knownTypeGsvGeologicalUnit50KAge);
        knownLayers.add(knownTypeNswDrillholesCoal);
        knownLayers.add(knownTypeNswGeologicalFieldObservations);
        knownLayers.add(knownTypeNswDrillholesMinerals);
        knownLayers.add(knownTypeNswGeologySimplified);
        knownLayers.add(knownTypeHistoricalExplorationActivityPoints);
        knownLayers.add(knownTypeNswDrillholesPetroleum);
        knownLayers.add(knownTypeCate1ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeGravBaseStatOfTasmMinResoTasm);
        knownLayers.add(knownTypeRadnlOperR25PcprrL3Knmi);
        knownLayers.add(knownTypeGsvGeologicalUnit250KAge);
        knownLayers.add(knownTypeReleases);
        knownLayers.add(knownTypeMineOccuPoinOfTasmMinResoTasm);
        knownLayers.add(knownTypeGsvShearDisplacementStructure50K);
        knownLayers.add(knownTypeMineralOccurenceIndustryFull);
        knownLayers.add(knownTypeNswHistoricExplorationTitles);
        knownLayers.add(knownTypeCate2ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeNswMapBlockGraticule);
        knownLayers.add(knownTypeSection574);
        knownLayers.add(knownTypeBlLocalaboriginallandcouncil);
        knownLayers.add(knownTypeLandLineOfTasmMinResoTasm);
        knownLayers.add(knownTypeMiniLeasPolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeNswMineralOccurrenceIndustry);
        knownLayers.add(knownTypeLandPoinOfTasmMinResoTasm);
        knownLayers.add(knownTypeProcLandAreaOfTasmMinResoTasm);
        knownLayers.add(knownTypeGsvGeologicalUnit50KLithology);
        knownLayers.add(knownTypeSection19);
        knownLayers.add(knownTypeNsw100KMapSheetExtents);
        knownLayers.add(knownTypeCate5ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeMinesAndMineralDepositsMinedex);
        knownLayers.add(knownTypeNswFossickingDistricts);
        knownLayers.add(knownTypeNswGeologicalFieldObservationsPhoto);
        knownLayers.add(knownTypeGeopTotaMagnInteRtpTmiRtpTiltFilt);
        knownLayers.add(knownTypeNswSeismic);

        knownLayers.add(knownTypeNswCurrMiniAndExplTitl);
        knownLayers.add(knownTypeBorePoinOfTasmMinResoTasm);
        knownLayers.add(knownTypeGeopTotaMagnInte1StDeriReduToPole);
        knownLayers.add(knownTypeNswGeologicalSpectralSamples);
        knownLayers.add(knownTypeGeopTernRadiPota);
        knownLayers.add(knownTypeGsvGeologicalUnit250K);
        knownLayers.add(knownTypeCate4ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeMineralisationZonesNonS572Aa);
        knownLayers.add(knownTypeNswDrillholesCsg);
        knownLayers.add(knownTypeNswCurrentMiningApplications);
        knownLayers.add(knownTypeAmalgamations);
        knownLayers.add(knownTypeNswOperatingMineralMines);

        knownLayers.add(knownTypeTenementsCurrent);
        knownLayers.add(knownTypeBoreTracOfTasmMinResoTasm);
        knownLayers.add(knownTypeGsvGeologicalUnitContact250K);
        knownLayers.add(knownTypeGeophysicsElevation);
        knownLayers.add(knownTypeNswGeolSimpRockUnitBoun);
        knownLayers.add(knownTypeBlocksAndUnitsGraticule);
        knownLayers.add(knownTypeAirbGeopSurvOfTasmMinResoTasm);
        knownLayers.add(knownTypeNswMapUnitGraticule);
        knownLayers.add(knownTypeCate3ExplLicePolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeNswGeochronology);

        knownLayers.add(knownTypeGeophysicsIsostaticBougerGravity);
        knownLayers.add(knownTypeGeopTotaMagnInteReduToPole);
        knownLayers.add(knownTypeNswCoreLibrarySamples);
        knownLayers.add(knownTypeNswGeochemistrySamples);
        knownLayers.add(knownTypeNswTitles);
        knownLayers.add(knownTypeGsvGeologicalUnit250KLithology);

        knownLayers.add(knownTypeHistoricalExplorationActivityLines);
        knownLayers.add(knownTypeRestorations);
        knownLayers.add(knownTypeBorehole);
        knownLayers.add(knownTypeNswGeology);
        knownLayers.add(knownTypeGeopIsosGravOverTmiRtpTilt);
        knownLayers.add(knownTypeGsvShearDisplacementStructure250K);
        knownLayers.add(knownTypeNswLithologySamples);
        knownLayers.add(knownTypeRadnlOperR25PcprrL3Color);
        knownLayers.add(knownTypeNswBase);
        knownLayers.add(knownTypeHist);
        knownLayers.add(knownTypeNswMineralOccurrenceCommodity);
        knownLayers.add(knownTypeLandPolyOfTasmMinResoTasm);
        knownLayers.add(knownTypeGsvGeologicalUnitContact50K);
        knownLayers.add(knownTypeL180MtIsaDeepCrusSeisSurvQld2006StacAndMigrDataAndImagForLine06GaTo06Ga);
        knownLayers.add(knownTypeAreTherAnySandUranSystInTheEromBasi);
        knownLayers.add(knownTypeL164CurnSeisSurvSa20032004StacAndMigrSeisDataAndImagForLine03Ga);
        knownLayers.add(knownTypeLawnHillPlatAndLeicRiveFaulTrouMeasStraSectOnliGis);
        knownLayers.add(knownTypePredMineDiscInTheEastYilgCratAnExamOfDistTargOfAnOrogGoldMineSyst);
        knownLayers.add(knownTypeFinaRepo3DGeolModeOfTheEastYilgCratProjPmdY2Sept2001Dece2004);
                
                
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
	@Qualifier("cswAuscopeTest")
	CSWServiceItem cswAuscopeTest;
    
    //@Autowired
    //	@Qualifier("cswMDUTest")
    //	CSWServiceItem cswMDUTest;
    
    @Autowired
	@Qualifier("cswGAPMDCRC")
	CSWServiceItem cswGAPMDCRC;
    
    @Bean
	public ArrayList<CSWServiceItem> cswServiceList() {
		ArrayList<CSWServiceItem> serviceList = new ArrayList<CSWServiceItem>();
		serviceList.add(cswAuscopeTest);
	        // serviceList.add(cswMDUTest);
		serviceList.add(cswGAPMDCRC);
		return serviceList;
	}

}

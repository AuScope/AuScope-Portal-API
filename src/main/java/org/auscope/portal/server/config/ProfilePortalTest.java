package org.auscope.portal.server.config;

import java.util.ArrayList;

import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * Definitions for all known layers
 */
 
@Configuration
@Profile("test")
public class ProfilePortalTest {

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
    KnownLayer knownTypeMagnetotellurics;
    @Autowired
    KnownLayer knownTypeTimaGeoSample;
    @Autowired
    KnownLayer knownTypeSHRIMPGeoSample;
    @Autowired
    KnownLayer knownTypeSamplingPoint;
    @Autowired
    KnownLayer knownTypeFeatureCollection;
    @Autowired
    KnownLayer knownTypePortals;
    @Autowired
    KnownLayer knownTypeGeoNetworks;
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
    // NB: Temporarily disabled until colour representation issues resolved
    //@Autowired
    //KnownLayer knownTypeGypsumContent;
    @Autowired
    KnownLayer knownTypeSilicaContent;
    @Autowired
    KnownLayer knownTypeBoreholeMSCL;
    @Autowired
    KnownLayer knownTypeSeismologyInSchool;
    @Autowired
    KnownLayer knownTypePassiveSeismic;
    @Autowired
    KnownLayer knownTypeSF0BoreholeNVCL;

    /* Generated from former test "Registered" tab START */
    @Autowired
    KnownLayer knownTypeHighpSiteIronlayer;
    @Autowired
    KnownLayer knownTypeHighpfeaturetype;
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
    /* IGSN sample layer */
    @Autowired
    KnownLayer knownTypeIGSNSample;
    @Autowired
    KnownLayer knownTypeIGSNGASample;
    @Autowired
    KnownLayer knownTypeIGSNANDSSample;
    @Autowired
    KnownLayer knownTypeIGSNWdcSample;

    /* GA Geophysical Survey Datasets */
    @Autowired
    KnownLayer knownTypeGeophysSurveys;
    @Autowired
    KnownLayer knownTypeGeophysGravSurveys;
    @Autowired
    KnownLayer knownTypeGeophysRadioSurveys;
    @Autowired
    KnownLayer knownTypeGeophysMagSurveys;
    @Autowired
    KnownLayer knownTypeGeophysElevSurveys;
    
     /* GA Geophysical Layers */
     @Autowired
     KnownLayer knownTypeGANatGeoPhys1;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys2;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys3;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys4;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys5;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys6;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys7;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys8;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys9;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys10;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys11;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys12;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys13;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys14;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys15;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys16;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys17;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys18;
     @Autowired
     KnownLayer knownTypeGANatGeoPhys19;


    
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
        knownLayers.add(knownTypeMagnetotellurics);
        knownLayers.add(knownTypeTimaGeoSample);
        knownLayers.add(knownTypeSHRIMPGeoSample);
        knownLayers.add(knownTypeSamplingPoint);
        knownLayers.add(knownTypeFeatureCollection);
        knownLayers.add(knownTypePortals);
        knownLayers.add(knownTypeGeoNetworks);
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
        // knownLayers.add(knownTypeGypsumContent);
        knownLayers.add(knownTypeSilicaContent);
        knownLayers.add(knownTypeBoreholeMSCL);
        knownLayers.add(knownTypeSeismologyInSchool);
        knownLayers.add(knownTypePassiveSeismic);
        knownLayers.add(knownTypeSF0BoreholeNVCL);

        /* Generated from former test "Registered" tab START */
        knownLayers.add(knownTypeHighpSiteIronlayer);
        knownLayers.add(knownTypeHighpfeaturetype);
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
        knownLayers.add(knownTypeIGSNSample);        
        knownLayers.add(knownTypeIGSNGASample);        
        knownLayers.add(knownTypeIGSNANDSSample);        
        knownLayers.add(knownTypeIGSNWdcSample);

        /* GA Geophysical Survey Datasets */
        knownLayers.add(knownTypeGeophysSurveys);
        knownLayers.add(knownTypeGeophysGravSurveys);
        knownLayers.add(knownTypeGeophysRadioSurveys);
        knownLayers.add(knownTypeGeophysMagSurveys);
        knownLayers.add(knownTypeGeophysElevSurveys);

        /* GA National Geophysical Layers */
        knownLayers.add(knownTypeGANatGeoPhys1);
        knownLayers.add(knownTypeGANatGeoPhys2);
        knownLayers.add(knownTypeGANatGeoPhys3);
        knownLayers.add(knownTypeGANatGeoPhys4);
        knownLayers.add(knownTypeGANatGeoPhys5);
        knownLayers.add(knownTypeGANatGeoPhys6);
        knownLayers.add(knownTypeGANatGeoPhys7);
        knownLayers.add(knownTypeGANatGeoPhys8);
        knownLayers.add(knownTypeGANatGeoPhys9);
        knownLayers.add(knownTypeGANatGeoPhys10);
        knownLayers.add(knownTypeGANatGeoPhys11);
        knownLayers.add(knownTypeGANatGeoPhys12);
        knownLayers.add(knownTypeGANatGeoPhys13);
        knownLayers.add(knownTypeGANatGeoPhys14);
        knownLayers.add(knownTypeGANatGeoPhys15);
        knownLayers.add(knownTypeGANatGeoPhys16);
        knownLayers.add(knownTypeGANatGeoPhys17);
        knownLayers.add(knownTypeGANatGeoPhys18);
        knownLayers.add(knownTypeGANatGeoPhys19);

        return knownLayers;
	}
                

}

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
@Profile("prod")
public class ProfilePortalProduction {

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
    // @Autowired
    //KnownLayer knownTypeGypsumContent;
    @Autowired
    KnownLayer knownTypeSilicaContent;
    @Autowired
    KnownLayer knownTypeBoreholeMSCL;
    @Autowired
    KnownLayer knownTypeSeismologyInSchool;
    @Autowired
    KnownLayer knownTypeSKIPPY;
    @Autowired
    KnownLayer knownTypeKIMBA97;
    @Autowired
    KnownLayer knownTypeKIMBA98;
    @Autowired
    KnownLayer knownTypeWACRATON;
    @Autowired
    KnownLayer knownTypeSEAL;
    @Autowired
    KnownLayer knownTypeSEAL2;
    @Autowired
    KnownLayer knownTypeSEAL3;
    @Autowired
    KnownLayer knownTypeCAPRAL;
    @Autowired
    KnownLayer knownTypeSOC;
    @Autowired
    KnownLayer knownTypeGAWLER;
    @Autowired
    KnownLayer knownTypeBILBY;
    @Autowired
    KnownLayer knownTypeCURNAMONA;
    @Autowired
    KnownLayer knownTypeMINQ;
    @Autowired
    KnownLayer knownTypeEAL1;
    @Autowired
    KnownLayer knownTypeEAL2;
    @Autowired
    KnownLayer knownTypeEAL3;
    @Autowired
    KnownLayer knownTypeBASS;
    @Autowired
    KnownLayer knownTypeSQEAL;
    @Autowired
    KnownLayer knownTypeAQ3;
    @Autowired
    KnownLayer knownTypeAQT;
    @Autowired
    KnownLayer knownTypeBANDA;
    @Autowired
    KnownLayer knownTypeASR;
    @Autowired
    KnownLayer knownTypeMARLALINE;
    @Autowired
    KnownLayer knownTypePassiveSeismic;
    @Autowired
    KnownLayer knownTypeSF0BoreholeNVCL;

    /* Generated from former prod "Registered" tab START */
    @Autowired
    KnownLayer knownTypeStrategicProspectivityZones;
    @Autowired
    KnownLayer knownTypeCate5ExplLicePolyOfTasmMinResoTasm;
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
        knownLayers.add(knownTypeErlCommodityResourceView);                
        knownLayers.add(knownTypeMiningActivity);
        knownLayers.add(knownTypeMineralTenements);               
        knownLayers.add(knownTypeRemanentAnomalies);
        knownLayers.add(knownTypeRemanentAnomaliesTMI);
        knownLayers.add(knownTypeRemanentAnomaliesAutoSearch);
        knownLayers.add(knownTypeEMAGRemanentAnomalies);
        knownLayers.add(knownTypeEMAGRemanentAnomaliesTMI);
        knownLayers.add(knownTypeMineralOccurrenceView);
        knownLayers.add(knownTypeBoreholeNvclV2);
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
        knownLayers.add(knownTypeSKIPPY);
        knownLayers.add(knownTypeKIMBA97);
        knownLayers.add(knownTypeKIMBA98);
        knownLayers.add(knownTypeWACRATON);
        knownLayers.add(knownTypeSEAL);
        knownLayers.add(knownTypeSEAL2);
        knownLayers.add(knownTypeSEAL3);
        knownLayers.add(knownTypeCAPRAL);
        knownLayers.add(knownTypeSOC);
        knownLayers.add(knownTypeGAWLER);
        knownLayers.add(knownTypeBILBY);
        knownLayers.add(knownTypeCURNAMONA);
        knownLayers.add(knownTypeMINQ);
        knownLayers.add(knownTypeEAL1);
        knownLayers.add(knownTypeEAL2);
        knownLayers.add(knownTypeEAL3);
        knownLayers.add(knownTypeBASS);
        knownLayers.add(knownTypeSQEAL);
        knownLayers.add(knownTypeAQ3);
        knownLayers.add(knownTypeAQT);
        knownLayers.add(knownTypeBANDA);
        knownLayers.add(knownTypeASR);
        knownLayers.add(knownTypeMARLALINE );
        knownLayers.add(knownTypePassiveSeismic);
        knownLayers.add(knownTypeSF0BoreholeNVCL);
        /* Generated from former prod "Registered" tab START */
        
        knownLayers.add(knownTypeStrategicProspectivityZones);
        knownLayers.add(knownTypeCate5ExplLicePolyOfTasmMinResoTasm);
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

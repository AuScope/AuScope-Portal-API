package org.auscope.portal.server.config;

import java.util.ArrayList;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Definitions for all known layers
 * 
 * @author woo392
 *
 */
@Configuration
public class ProfilePortalProduction {
	
	@Autowired
	KnownLayer knownTypeBouguerGeodetic;
	@Autowired
	KnownLayer knownTypeGravAnomalyGeodetic;
	@Autowired
	KnownLayer knownTypeRadMapTotaldose;
	@Autowired
	KnownLayer knownTypeMagMap;
	@Autowired
	KnownLayer knownTypeGocadModels;
	@Autowired
	KnownLayer knownTypeGAAemSurvey;
	@Autowired
	KnownLayer knownTypeGARumJungleAEM;
	@Autowired
	KnownLayer knownTypeGAWoolnerAEM;
	@Autowired
	KnownLayer knownTypeGAGravitySurvey;
	@Autowired
	KnownLayer knownTypeMusgraveAem;
	@Autowired
	KnownLayer knownTypeWesternAreaAem;
	@Autowired
	KnownLayer knownTypeAshburtonAem;
	@Autowired
	KnownLayer knownTypeGswaGravMerge;
	@Autowired
	KnownLayer knownTypeGswa20mMagMerge;
	@Autowired
	KnownLayer knownTypeGswa40mMagMerge1VD;
	@Autowired
	KnownLayer knownTypeGswa40mMagMerge;
	@Autowired
	KnownLayer knownTypeGswa80mMagMerge1VD;
	@Autowired
	KnownLayer knownTypeGswa80mMagMergertp;
	@Autowired
	KnownLayer knownTypeGswa80mMagMerge;
	@Autowired
	KnownLayer knownTypeGswa80mKmerge;
	@Autowired
	KnownLayer knownTypeGswa80mTCmerge;
	@Autowired
	KnownLayer knownTypeGswa80mThmerge;
	@Autowired
	KnownLayer knownTypeGswa80mUmerge;
	//@Autowired
	//KnownLayer knownTypeCunyuAem;

	
	
	@Bean
	public ArrayList<KnownLayer> knownTypes() {
		ArrayList<KnownLayer> knownLayers = new ArrayList<KnownLayer>();
		knownLayers.add(knownTypeBouguerGeodetic);
		knownLayers.add(knownTypeGravAnomalyGeodetic);
		knownLayers.add(knownTypeRadMapTotaldose);
		knownLayers.add(knownTypeMagMap);
		knownLayers.add(knownTypeGocadModels);
		knownLayers.add(knownTypeGAAemSurvey);
		knownLayers.add(knownTypeGARumJungleAEM);
		knownLayers.add(knownTypeGAWoolnerAEM);
		knownLayers.add(knownTypeGAGravitySurvey);
		knownLayers.add(knownTypeMusgraveAem);
		knownLayers.add(knownTypeWesternAreaAem);
		knownLayers.add(knownTypeAshburtonAem);
		knownLayers.add(knownTypeGswaGravMerge);
		knownLayers.add(knownTypeGswa20mMagMerge);
		knownLayers.add(knownTypeGswa40mMagMerge1VD);
		knownLayers.add(knownTypeGswa40mMagMerge);
		knownLayers.add(knownTypeGswa80mMagMerge1VD);
		knownLayers.add(knownTypeGswa80mMagMergertp);
		knownLayers.add(knownTypeGswa80mMagMerge);
		knownLayers.add(knownTypeGswa80mKmerge);
		knownLayers.add(knownTypeGswa80mTCmerge);
		knownLayers.add(knownTypeGswa80mThmerge);
		knownLayers.add(knownTypeGswa80mUmerge);
		//knownLayers.add(knownTypeCunyuAem);
		return knownLayers;
	}
	
	/*
	<!-- All elements must be of type org.auscope.portal.server.web.KnownLayer -->
    <bean id="knownTypes" class="java.util.ArrayList">
        <constructor-arg>
            <list>
                <ref bean="knownTypeBouguerGeodetic" />
                <ref bean="knownTypeGravAnomalyGeodetic" />
                <ref bean="knownTypeRadMapTotaldose" />
                <ref bean="knownTypeMagMap" />
                <ref bean="knownTypeGocadModels" />
                <ref bean="knownTypeGAAemSurvey" />
                <ref bean="knownTypeGARumJungleAEM" />
                <ref bean="knownTypeGAWoolnerAEM" />
                <ref bean="knownTypeGAGravitySurvey" />
                <ref bean="knownTypeMusgraveAem" />
                <ref bean="knownTypeWesternAreaAem" />
                <ref bean="knownTypeAshburtonAem" />
                <ref bean="knownTypeGswaGravMerge" />
                <ref bean="knownTypeGswa20mMagMerge"/>
                <ref bean="knownTypeGswa40mMagMerge1VD"/>
                <ref bean="knownTypeGswa40mMagMerge"/>
                <ref bean="knownTypeGswa80mMagMerge1VD"/>
                <ref bean="knownTypeGswa80mMagMergertp"/>
                <ref bean="knownTypeGswa80mMagMerge"/>
                <ref bean="knownTypeGswa80mKmerge"/>
                <ref bean="knownTypeGswa80mTCmerge"/>
                <ref bean="knownTypeGswa80mThmerge"/>
                <ref bean="knownTypeGswa80mUmerge"/>
                <!-- <ref bean="knownTypeCunyuAem" />  -->
            </list>
        </constructor-arg>
    </bean>
    */


}

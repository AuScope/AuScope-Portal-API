package org.auscope.portal.server;

import java.awt.Dimension;
import java.awt.Point;

import org.auscope.portal.core.view.knownlayer.CSWRecordSelector;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.WFSSelector;
import org.auscope.portal.core.view.knownlayer.WMSSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Known layer bean definitions (originally migrated from Spring MVC vl-known-layers.xml)
 * @author woo392
 *
 */
@Configuration
public class VlKnownLayers {
	
	@Bean
	public WMSSelector gravMergeWmsSelector() {
		return new WMSSelector("gravity_merged");
	}
	
	@Bean
	public KnownLayer knownTypeGswaGravMerge() {
		KnownLayer layer = new KnownLayer("gswa-gravmerge", gravMergeWmsSelector());
		layer.setName("400m Gravity Merged Grid (2016 v1)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
	
	/*
	<bean id="knownTypeGswaGravMerge" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-gravmerge"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="gravity_merged"/>
            </bean>
        </constructor-arg>
        <property name="name" value="400m Gravity Merged Grid (2016 v1)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
    
	@Bean
	public WMSSelector totalMagIntensity20mSelector() {
		String[] serviceEndpoints = new String[1];
		serviceEndpoints[0] = "http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_20m_Mag_Merge_v1_2014.nc";
		return new WMSSelector("total_magnetic_intensity", serviceEndpoints, true);
	}
	
	@Bean
	public KnownLayer knownTypeGswa20mMagMerge() {
		KnownLayer layer = new KnownLayer("gswa-20mmagmerge", totalMagIntensity20mSelector());
		layer.setName("20m Magnetic Merged Grid (2014 v1)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
	
    /*
    <bean id="knownTypeGswa20mMagMerge" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-20mmagmerge"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="total_magnetic_intensity"/>
                <constructor-arg name="serviceEndpoints">
                    <array>
                        <value>http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_20m_Mag_Merge_v1_2014.nc</value>
                    </array>
                </constructor-arg>
                <constructor-arg name="includeEndpoints" value="true"/>
            </bean>
        </constructor-arg>
        <property name="name" value="20m Magnetic Merged Grid (2014 v1)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
    
	@Bean
	public WMSSelector firstVerticalDerivTmi40mSelector() {
		String[] serviceEndpoints = new String[1];
		serviceEndpoints[0] = "http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_40m_Mag_Merge_1VD_v1_2014.nc";
		return new WMSSelector("first_vertical_deriv_tmi", serviceEndpoints, true);
	}

	@Bean
	public KnownLayer knownTypeGswa40mMagMerge1VD() {
		KnownLayer layer = new KnownLayer("gswa-40mmagmerge1vd", firstVerticalDerivTmi40mSelector());
		layer.setName("40m Magnetic Merged Grid - 1st Vertical Derivative (2014 v1)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
	
    /*
    <bean id="knownTypeGswa40mMagMerge1VD" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-40mmagmerge1vd"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="first_vertical_deriv_tmi"/>
                <constructor-arg name="serviceEndpoints">
                    <array>
                        <value>http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_40m_Mag_Merge_1VD_v1_2014.nc</value>
                    </array>
                </constructor-arg>
                <constructor-arg name="includeEndpoints" value="true"/>
            </bean>
        </constructor-arg>
        <property name="name" value="40m Magnetic Merged Grid - 1st Vertical Derivative (2014 v1)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
	
	@Bean
	public WMSSelector totalMagneticIntensity40mSelector() {
		String[] serviceEndpoints = new String[1];
		serviceEndpoints[0] = "http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_40m_Mag_Merge_v1_2014.nc";
		return new WMSSelector("total_magnetic_intensity", serviceEndpoints, true);
	}

	@Bean
	public KnownLayer knownTypeGswa40mMagMerge() {
		KnownLayer layer = new KnownLayer("gswa-40mmagmerge", totalMagneticIntensity40mSelector());
		layer.setName("40m Magnetic Merged Grid (2014 v1)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}

	/*
    <bean id="knownTypeGswa40mMagMerge" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-40mmagmerge"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="total_magnetic_intensity"/>
                <constructor-arg name="serviceEndpoints">
                    <array>
                        <value>http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_40m_Mag_Merge_v1_2014.nc</value>
                    </array>
                </constructor-arg>
                <constructor-arg name="includeEndpoints" value="true"/>
            </bean>
        </constructor-arg>
        <property name="name" value="40m Magnetic Merged Grid (2014 v1)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
    
	@Bean
	public WMSSelector firstVerticalDerivTmi80mSelector() {
		String[] serviceEndpoints = new String[1];
		serviceEndpoints[0] = "http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_80m_Mag_Merge_1VD_v1_2014.nc";
		return new WMSSelector("first_vertical_deriv_tmi", serviceEndpoints, true);
	}

	@Bean
	public KnownLayer knownTypeGswa80mMagMerge1VD() {
		KnownLayer layer = new KnownLayer("gswa-80mmagmerge1vd", firstVerticalDerivTmi80mSelector());
		layer.setName("80m Magnetic Merged Grid - 1st Vertical Derivative (2014 v1)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
    
    /*
    <bean id="knownTypeGswa80mMagMerge1VD" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-80mmagmerge1vd"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="first_vertical_deriv_tmi"/>
                <constructor-arg name="serviceEndpoints">
                    <array>
                        <value>http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_80m_Mag_Merge_1VD_v1_2014.nc</value>
                    </array>
                </constructor-arg>
                <constructor-arg name="includeEndpoints" value="true"/>
            </bean>
        </constructor-arg>
        <property name="name" value="80m Magnetic Merged Grid - 1st Vertical Derivative (2014 v1)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
    
	@Bean
	public WMSSelector reducedToPoleTmi80mSelector() {
		String[] serviceEndpoints = new String[1];
		serviceEndpoints[0] = "http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_80m_Mag_Merge_RTP_v1_2014.nc";
		return new WMSSelector("reduced_to_pole_tmi", serviceEndpoints, true);
	}

	@Bean
	public KnownLayer knownTypeGswa80mMagMergertp() {
		KnownLayer layer = new KnownLayer("gswa-80mmagmergertp", reducedToPoleTmi80mSelector());
		layer.setName("80m Magnetic Merged Grid - Reduced to Pole (2014 v1)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
	
	/*
    <bean id="knownTypeGswa80mMagMergertp" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-80mmagmergertp"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="reduced_to_pole_tmi"/>
                <constructor-arg name="serviceEndpoints">
                    <array>
                        <value>http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_80m_Mag_Merge_RTP_v1_2014.nc</value>
                    </array>
                </constructor-arg>
                <constructor-arg name="includeEndpoints" value="true"/>
            </bean>
        </constructor-arg>
        <property name="name" value="80m Magnetic Merged Grid - Reduced to Pole (2014 v1)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
    
	@Bean
	public WMSSelector totalMagneticIntensity80mSelector() {
		String[] serviceEndpoints = new String[1];
		serviceEndpoints[0] = "http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_80m_Mag_Merge_v1_2014.nc";
		return new WMSSelector("total_magnetic_intensity", serviceEndpoints, true);
	}

	@Bean
	public KnownLayer knownTypeGswa80mMagMerge() {
		KnownLayer layer = new KnownLayer("gswa-80mmagmerge", totalMagneticIntensity80mSelector());
		layer.setName("80m Magnetic Merged Grid (2014 v1)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
	
	/*
    <bean id="knownTypeGswa80mMagMerge" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-80mmagmerge"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="total_magnetic_intensity"/>
                <constructor-arg name="serviceEndpoints">
                    <array>
                        <value>http://dapds00.nci.org.au/thredds/wms/rl1/GSWA_Geophysics/WA_Magnetic_Grids/WA_80m_Mag_Merge_v1_2014.nc</value>
                    </array>
                </constructor-arg>
                <constructor-arg name="includeEndpoints" value="true"/>
            </bean>
        </constructor-arg>
        <property name="name" value="80m Magnetic Merged Grid (2014 v1)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
	
	@Bean WMSSelector percentagePotassiumSelector() {
		return new WMSSelector("percentage_potassium");
	}

	@Bean
	public KnownLayer knownTypeGswa80mKmerge() {
		KnownLayer layer = new KnownLayer("gswa-80mkmerge", percentagePotassiumSelector());
		layer.setName("80m Radiometric Merge (K)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
    
	/*
    <bean id="knownTypeGswa80mKmerge" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-80mkmerge"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="percentage_potassium"/>
            </bean>
        </constructor-arg>
        <property name="name" value="80m Radiometric Merge (K)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
	
	@Bean
	public WMSSelector doseRateSelector() {
		return new WMSSelector("dose_rate");
	}

	@Bean
	public KnownLayer knownTypeGswa80mTCmerge() {
		KnownLayer layer = new KnownLayer("gswa-80mtcmerge", doseRateSelector());
		layer.setName("80m Radiometric Merge (Dose Rate)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
	
    /*
    <bean id="knownTypeGswa80mTCmerge" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-80mtcmerge"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="dose_rate"/>
            </bean>
        </constructor-arg>
        <property name="name" value="80m Radiometric Merge (Dose Rate)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
	
	@Bean
	public WMSSelector thoriumPpmSelector() {
		return new WMSSelector("thorium_ppm");
	}

	@Bean
	public KnownLayer knownTypeGswa80mThmerge() {
		KnownLayer layer = new KnownLayer("gswa-80mthmerge", thoriumPpmSelector());
		layer.setName("80m Radiometric Merge (Th)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
    
	/*
    <bean id="knownTypeGswa80mThmerge" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-80mthmerge"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="thorium_ppm"/>
            </bean>
        </constructor-arg>
        <property name="name" value="80m Radiometric Merge (Th)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */
    
	@Bean
	public WMSSelector uraniumPpmSelector() {
		return new WMSSelector("uranium_ppm");
	}

	@Bean
	public KnownLayer knownTypeGswa80mUmerge() {
		KnownLayer layer = new KnownLayer("gswa-80mumerge", uraniumPpmSelector());
		layer.setName("80m Radiometric Merge (U)");
		layer.setDescription("");
		layer.setGroup("Geological Survey of Western Australia");
		layer.setOrder("1_Geological Survey of Western Australia_1");
		return layer;
	}
	
	/*
    <bean id="knownTypeGswa80mUmerge" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gswa-80mumerge"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="uranium_ppm"/>
            </bean>
        </constructor-arg>
        <property name="name" value="80m Radiometric Merge (U)"/>
        <property name="description" value=""/>
        <property name="group" value="Geological Survey of Western Australia"/>
         <property name="order" value="1_Geological Survey of Western Australia_1"/>
    </bean>
    */

	@Bean
	public WMSSelector magmapV5_2010Selector() {
		return new WMSSelector("magmap_V5_2010");
	}

	@Bean
	public KnownLayer knownTypeMagMap() {
		KnownLayer layer = new KnownLayer("ga-magmap-v5-2010", magmapV5_2010Selector());
		layer.setName("MagMap V5 2010");
		layer.setDescription("");
		layer.setGroup("Geoscience Australia Coverages");
		layer.setOrder("2_Geoscience Australia Coverages_1");
		return layer;
	}
	
	/*
    <bean id="knownTypeMagMap" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="ga-magmap-v5-2010"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="magmap_V5_2010"/>
            </bean>
        </constructor-arg>
        <property name="name" value="MagMap V5 2010"/>
        <property name="description" value=""/>
        <property name="group" value="Geoscience Australia Coverages"/>
         <property name="order" value="2_Geoscience Australia Coverages_1"/>
    </bean>
    */
    
	@Bean
	public WMSSelector onshoreAndOffshoreGravityAnomalyGeodeticSelector() {
		return new WMSSelector("onshore_and_offshore_gravity_anomaly_geodetic");
	}

	@Bean
	public KnownLayer knownTypeGravAnomalyGeodetic() {
		KnownLayer layer = new KnownLayer("ga-grav-anom-geo", onshoreAndOffshoreGravityAnomalyGeodeticSelector());
		layer.setName("Onshore and Offshore Gravity Anomaly Geodetic");
		layer.setDescription("");
		layer.setGroup("Geoscience Australia Coverages");
		layer.setOrder("2_Geoscience Australia Coverages_2");
		return layer;
	}
	
	/*
    <bean id="knownTypeGravAnomalyGeodetic" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="ga-grav-anom-geo"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="onshore_and_offshore_gravity_anomaly_geodetic"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Onshore and Offshore Gravity Anomaly Geodetic"/>
        <property name="description" value=""/>
        <property name="group" value="Geoscience Australia Coverages"/>
        <property name="order" value="2_Geoscience Australia Coverages_2"/>
    </bean>
    */
    
	@Bean
	public WMSSelector onshoreOnlyBouguerGeodeticSelector() {
		return new WMSSelector("onshore_only_Bouguer_geodetic");
	}

	@Bean
	public KnownLayer knownTypeBouguerGeodetic() {
		KnownLayer layer = new KnownLayer("ga-onshore-bouguer-geodetic", onshoreOnlyBouguerGeodeticSelector());
		layer.setName("Onshore Only Bouguer Geodetic");
		layer.setDescription("");
		layer.setGroup("Geoscience Australia Coverages");
		layer.setOrder("2_Geoscience Australia Coverages_3");
		return layer;
	}
	
	/*
   <bean id="knownTypeBouguerGeodetic" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="ga-onshore-bouguer-geodetic"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="onshore_only_Bouguer_geodetic"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Onshore Only Bouguer Geodetic"/>
        <property name="description" value=""/>
        <property name="group" value="Geoscience Australia Coverages"/>
        <property name="order" value="2_Geoscience Australia Coverages_3"/>
    </bean>
    */
	
	@Bean
	public WMSSelector radmap10FilteredTotaldoseSelector() {
		return new WMSSelector("radmap10_filtered_totaldose");
	}

	@Bean
	public KnownLayer knownTypeRadMapTotaldose() {
		KnownLayer layer = new KnownLayer("ga-radmap-totaldose", radmap10FilteredTotaldoseSelector());
		layer.setName("RadMap Totaldose");
		layer.setDescription("");
		layer.setGroup("Geoscience Australia Coverages");
		layer.setOrder("2_Geoscience Australia Coverages_4");
		return layer;
	}

	/*
    <bean id="knownTypeRadMapTotaldose" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="ga-radmap-totaldose"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WMSSelector">
                <constructor-arg name="layerName" value="radmap10_filtered_totaldose"/>
            </bean>
        </constructor-arg>
        <property name="name" value="RadMap Totaldose"/>
        <property name="description" value=""/>
        <property name="group" value="Geoscience Australia Coverages"/>
        <property name="order" value="2_Geoscience Australia Coverages_4"/>
    </bean>
    */

	@Bean
	public WFSSelector gaGravitypointsSelector() {
		return new WFSSelector("ga:gravitypoints");
	}

	@Bean
	public KnownLayer knownTypeGAGravitySurvey() {
		KnownLayer layer = new KnownLayer("ga-gravity", gaGravitypointsSelector());
		layer.setName("Australian Point Gravity");
		layer.setDescription("");
		layer.setGroup("Geoscience Australia");
		layer.setOrder("3_Geoscience Australia_1");
		return layer;
	}
	
	/*
    <bean id="knownTypeGAGravitySurvey" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="ga-gravity"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WFSSelector">
                <constructor-arg name="featureTypeName" value="ga:gravitypoints"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Australian Point Gravity"/>
        <property name="description" value=""/>
        <property name="group" value="Geoscience Australia"/>
        <property name="order" value="3_Geoscience Australia_1"/>
    </bean>
    */
	
	@Bean
	public CSWRecordSelector gocadSelector() {
		CSWRecordSelector selector = new CSWRecordSelector();
		selector.setDescriptiveKeyword("http://vgl.auscope.org/model/gocad");
		return selector;
	}

	@Bean
	public KnownLayer knownTypeGocadModels() {
		KnownLayer layer = new KnownLayer("gocad-models", gocadSelector());
		layer.setName("GOCAD Models");
		layer.setDescription("A collection of spatially located 3D models that have been generated using GOCAD");
		layer.setGroup("Geoscience Australia");
		layer.setOrder("3_Geoscience Australia_3");
		layer.setIconUrl("http://maps.google.com/mapfiles/kml/paddle/blu-square.png");
		layer.setIconAnchor(new Point(16, 32));
		layer.setIconSize(new Dimension(32, 32));
		return layer;
	}
	
	/*
    <bean id="knownTypeGocadModels" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="gocad-models"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.CSWRecordSelector">
                <property name="descriptiveKeyword" value="http://vgl.auscope.org/model/gocad"/>
            </bean>
        </constructor-arg>
        <property name="name" value="GOCAD Models"/>
        <property name="description" value="A collection of spatially located 3D models that have been generated using GOCAD"/>
        <property name="group" value="Geoscience Australia"/>
         <property name="order" value="3_Geoscience Australia_3"/>
        <property name="iconUrl" value="http://maps.google.com/mapfiles/kml/paddle/blu-square.png"/>
        <property name="iconAnchor">
            <bean class="java.awt.Point">
                <constructor-arg index="0" value="16"/>
                <constructor-arg index="1" value="32"/>
            </bean>
        </property>
        <property name="iconSize">
            <bean class="java.awt.Dimension">
                <constructor-arg index="0" value="32"/>
                <constructor-arg index="1" value="32"/>
            </bean>
        </property>
    </bean>
    */

	@Bean
	public WFSSelector gaAemSurveysSelector() {
		return new WFSSelector("ga:aemsurveys");
	}

	@Bean
	public KnownLayer knownTypeGAAemSurvey() {
		KnownLayer layer = new KnownLayer("ga-aem", gaAemSurveysSelector());
		layer.setName("Paterson Airborne Electromagnetic Survey");
		layer.setDescription("");
		layer.setGroup("Geoscience Australia");
		layer.setOrder("3_Geoscience Australia_4");
		return layer;
	}
	
	/*
     <bean id="knownTypeGAAemSurvey" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="ga-aem"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WFSSelector">
                <constructor-arg name="featureTypeName" value="ga:aemsurveys"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Paterson Airborne Electromagnetic Survey"/>
        <property name="description" value=""/>
        <property name="group" value="Geoscience Australia"/>
        <property name="order" value="3_Geoscience Australia_4"/>
    </bean>
    */
	
	@Bean
	public WFSSelector gaRumJungleAemSelector() {
		return new WFSSelector("ga:rum_jungle_aem");
	}

	@Bean
	public KnownLayer knownTypeGARumJungleAEM() {
		KnownLayer layer = new KnownLayer("ga-rum-jungle-aem", gaRumJungleAemSelector());
		layer.setName("Rum Jungle Airborne Electromagnetic Survey");
		layer.setDescription("");
		layer.setGroup("Geoscience Australia");
		layer.setOrder("3_Geoscience Australia_5");
		return layer;
	}

	/*
    <bean id="knownTypeGARumJungleAEM" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="ga-rum-jungle-aem"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WFSSelector">
                <constructor-arg name="featureTypeName" value="ga:rum_jungle_aem"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Rum Jungle Airborne Electromagnetic Survey"/>
        <property name="description" value=""/>
        <property name="group" value="Geoscience Australia"/>
        <property name="order" value="3_Geoscience Australia_5"/>
    </bean>
    */

	@Bean
	public WFSSelector gaWoolnerAemSelector() {
		return new WFSSelector("ga:woolner_aem");
	}

	@Bean
	public KnownLayer knownTypeGAWoolnerAEM() {
		KnownLayer layer = new KnownLayer("ga-woolner-aem", gaWoolnerAemSelector());
		layer.setName("Woolner Airborne Electromagnetic Survey");
		layer.setDescription("");
		layer.setGroup("Geoscience Australia");
		layer.setOrder("3_Geoscience Australia_6");
		return layer;
	}
	
	/*
    <bean id="knownTypeGAWoolnerAEM" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="ga-woolner-aem"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WFSSelector">
                <constructor-arg name="featureTypeName" value="ga:woolner_aem"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Woolner Airborne Electromagnetic Survey"/>
        <property name="description" value=""/>
        <property name="group" value="Geoscience Australia"/>
        <property name="order" value="3_Geoscience Australia_6"/>
    </bean>
    */
	
	@Bean
	public WFSSelector gaAshburtonAemSelector() {
		return new WFSSelector("ga:ashburton_aem");
	}

	@Bean
	public KnownLayer knownTypeAshburtonAem() {
		KnownLayer layer = new KnownLayer("csiro-ashburton-aem", gaAshburtonAemSelector());
		layer.setName("Ashburton Airborne Electromagnetic Survey");
		layer.setDescription("");
		layer.setGroup("CSIRO");
		layer.setOrder("4_CSIRO_1");
		return layer;
	}
	
	/*
    <bean id="knownTypeAshburtonAem" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="csiro-ashburton-aem"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WFSSelector">
                <constructor-arg name="featureTypeName" value="ga:ashburton_aem"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Ashburton Airborne Electromagnetic Survey"/>
        <property name="description" value=""/>
        <property name="group" value="CSIRO"/>
        <property name="order" value="4_CSIRO_1"/>
    </bean>
    */
    
	@Bean
	public WFSSelector gaMusgraveAemSelector() {
		return new WFSSelector("ga:musgrave_aem");
	}

	@Bean
	public KnownLayer knownTypeMusgraveAem() {
		KnownLayer layer = new KnownLayer("csiro-musgrave-aem", gaMusgraveAemSelector());
		layer.setName("Musgrave Airborne Electromagnetic Survey");
		layer.setDescription("");
		layer.setGroup("CSIRO");
		layer.setOrder("4_CSIRO_2");
		return layer;
	}
	
	/*
    <bean id="knownTypeMusgraveAem" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="csiro-musgrave-aem"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WFSSelector">
                <constructor-arg name="featureTypeName" value="ga:musgrave_aem"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Musgrave Airborne Electromagnetic Survey"/>
        <property name="description" value=""/>
        <property name="group" value="CSIRO"/>
         <property name="order" value="4_CSIRO_2"/>
    </bean>
    */
	
	@Bean
	public WFSSelector gaWesternAreaAemSelector() {
		return new WFSSelector("ga:western_area_aem");
	}

	@Bean
	public KnownLayer knownTypeWesternAreaAem() {
		KnownLayer layer = new KnownLayer("csiro-western-area-aem", gaWesternAreaAemSelector());
		layer.setName("Western Area Airborne Electromagnetic Survey");
		layer.setDescription("Western Area 70001");
		layer.setGroup("CSIRO");
		layer.setOrder("4_CSIRO_3");
		return layer;
	}
    
	/*
    <bean id="knownTypeWesternAreaAem" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="csiro-western-area-aem"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WFSSelector">
                <constructor-arg name="featureTypeName" value="ga:western_area_aem"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Western Area Airborne Electromagnetic Survey"/>
        <property name="description" value="Western Area 70001"/>
        <property name="group" value="CSIRO"/>
        <property name="order" value="4_CSIRO_3"/>
    </bean>
    
    <!-- * cunyu_aem layer doesn't seems to run well with the existing AEM Inversion program and it doesn't have 
         * columns to map to TXRX_DX and TXRX_DZ - which all other working AEM layers have.  
    <bean id="knownTypeCunyuAem" class="org.auscope.portal.core.view.knownlayer.KnownLayer">
        <constructor-arg name="id" value="csiro-cunyu-aem"/>
        <constructor-arg name="knownLayerSelector">
            <bean class="org.auscope.portal.core.view.knownlayer.WFSSelector">
                <constructor-arg name="featureTypeName" value="ga:cunyu_aem"/>
            </bean>
        </constructor-arg>
        <property name="name" value="Cunyu Airborne Electromagnetic Survey"/>
        <property name="description" value=""/>
        <property name="group" value="CSIRO"/>
    </bean>  -->
	*/

}

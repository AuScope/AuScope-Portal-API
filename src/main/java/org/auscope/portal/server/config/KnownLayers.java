package org.auscope.portal.server.config;

import java.awt.Dimension;
import java.awt.Point;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.auscope.portal.core.uifilter.AbstractBaseFilter;
import org.auscope.portal.core.uifilter.FilterCollection;
import org.auscope.portal.core.uifilter.Predicate;
import org.auscope.portal.core.uifilter.mandatory.AbstractMandatoryParamBinding;
import org.auscope.portal.core.uifilter.optional.UICheckBoxGroupProvider;
import org.auscope.portal.core.uifilter.optional.xpath.UIDate;
import org.auscope.portal.core.uifilter.optional.xpath.UIDropDownRemote;
import org.auscope.portal.core.uifilter.optional.xpath.UIDropDownSelectList;
import org.auscope.portal.core.uifilter.optional.xpath.UIPolygonBBox;
import org.auscope.portal.core.uifilter.optional.xpath.UITextBox;
import org.auscope.portal.core.view.knownlayer.CSWRecordSelector;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;
import org.auscope.portal.core.view.knownlayer.WFSSelector;
import org.auscope.portal.core.view.knownlayer.WMSSelector;
import org.auscope.portal.core.view.knownlayer.WMSWFSSelector;
import org.auscope.portal.view.knownlayer.IRISSelector;

/**
 * Known layer bean definitions (originally migrated from Spring MVC
 * auscope-known-layers.xml)
 *
 */
@Configuration
public class KnownLayers {

    private void setupIcon(KnownLayer layer) {
        Point iconAnchor = new Point(16, 32);
        layer.setIconAnchor(iconAnchor);
        Dimension iconSize = new Dimension(32, 32);
        layer.setIconSize(iconSize);
    }

    private FilterCollection createProviderFilterCollection() {
        UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider("Provider", null);
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        optionalFilters.add(uiCheckBoxGroupProvider);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        return filterCollection;
    }

    private FilterCollection createTextBoxFilterCollection(String label, String value) {
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox(label, value, null, Predicate.ISLIKE);
        optionalFilters.add(nameTextBox);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        return filterCollection;
    }

    private FilterCollection createIgsnFilterCollection() {
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("Name", "gsmlp:name", null, Predicate.ISLIKE);
        UITextBox igsnTextBox = new UITextBox("IGSN", "igsn", null, Predicate.ISLIKE);
        optionalFilters.add(nameTextBox);
        optionalFilters.add(igsnTextBox);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        return filterCollection;
    }

    private void addDrillDateFilters(List<AbstractBaseFilter> optionalFilters) {
        UIDate startFromDate = new UIDate("Drilling Start From", "gsmlp:drillStartDate", null, Predicate.BIGGER_THAN);
        UIDate startToDate = new UIDate("Drilling Start To", "gsmlp:drillStartDate", null, Predicate.SMALLER_THAN);
        UIDate endFromDate = new UIDate("Drilling End From", "gsmlp:drillEndDate", null, Predicate.BIGGER_THAN);
        UIDate endToDate = new UIDate("Drilling End To", "gsmlp:drillEndDate", null, Predicate.SMALLER_THAN);
        optionalFilters.add(startFromDate);
        optionalFilters.add(startToDate);
        optionalFilters.add(endFromDate);
        optionalFilters.add(endToDate);
    }

    @Bean
    public WFSSelector knownTypeMineSelector() {
        WFSSelector wfsSelector = new WFSSelector("er:MiningFeatureOccurrence");
        String[] relNameList = new String[2];
        relNameList[0] = "er:Mine";
        relNameList[1] = "gsml:MappedFeature";
        wfsSelector.setRelatedFeatureTypeNames(relNameList);
        return wfsSelector;
    }

    @Bean
    public KnownLayer knownTypeMine() {
        KnownLayer layer = new KnownLayer("erml-mine", knownTypeMineSelector());
        layer.setName("Earth Resource Mine");
        layer.setDescription(
                "A collection of services that implement the AuScope EarthResourceML v1 Profile for er:Mine");
        layer.setGroup("Earth Resources(old)");
        layer.setProxyUrl("doMineFilter.do");
        layer.setProxyCountUrl("doMineFilterCount.do");
        layer.setProxyStyleUrl("doMineFilterStyle.do");
        layer.setProxyDownloadUrl("doMineFilterDownload.do");
        layer.setIconUrl("http://maps.google.com/mapfiles/kml/paddle/pink-blank.png");
        setupIcon(layer);
        layer.setOrder("10");
        layer.setStackdriverServiceGroup("EarthResourcesLayers");

        UITextBox uiTextBox = new UITextBox("Mine Name", "er:specification/er:Mine/er:mineName/er:MineName/er:mineName",
                null, Predicate.ISLIKE);
        UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider("Provider", null);
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        optionalFilters.add(uiTextBox);
        optionalFilters.add(uiCheckBoxGroupProvider);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        layer.setFilterCollection(filterCollection);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeMineralOccurrenceSelector() {
        WFSSelector wfsSelector = new WFSSelector("gsml:MappedFeature");
        String[] relFeatList = new String[2];
        relFeatList[0] = "er:Commodity";
        relFeatList[1] = "er:MineralOccurrence";
        wfsSelector.setRelatedFeatureTypeNames(relFeatList);
        return wfsSelector;
    }

    @Bean
    public KnownLayer knownTypeMineralOccurrence() {
        KnownLayer layer = new KnownLayer("erml-mineraloccurrence", knownTypeMineralOccurrenceSelector());
        layer.setName("Earth Resource Mineral Occurrence");
        layer.setDescription(
                "A collection of services that implement the AuScope EarthResourceML v1 Profile for er:MineralOccurence");
        layer.setGroup("Earth Resources(old)");
        layer.setProxyUrl("doMineralOccurrenceFilter.do");
        layer.setProxyCountUrl("doMineralOccurrenceFilterCount.do");
        layer.setProxyStyleUrl("doMineralOccurrenceFilterStyle.do");
        layer.setProxyDownloadUrl("doMineralOccurrenceFilterDownload.do");
        layer.setIconUrl("http://maps.google.com/mapfiles/kml/paddle/purple-blank.png");
        setupIcon(layer);
        layer.setOrder("11");
        layer.setStackdriverServiceGroup("EarthResourcesLayers");
// RA: this is not working due to lack of vocabs, but it's covered in ERlite anyway
//        UIDropDownRemote uiDropDownRemote = new UIDropDownRemote("Commodity",
//                "gsml:specification/er:MineralOccurrence/er:commodityDescription/er:Commodity/er:commodityName", null,
//                Predicate.ISEQUAL, "getAllCommodities.do");
        UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider("Provider", null);
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
//        optionalFilters.add(uiDropDownRemote);
        optionalFilters.add(uiCheckBoxGroupProvider);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        layer.setFilterCollection(filterCollection);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeMiningActivitySelector() {
        WFSSelector wfsSelector = new WFSSelector("er:MiningFeatureOccurrence");
        String[] relNameList = new String[2];
        relNameList[0] = "er:MiningActivity";
        relNameList[1] = "gsml:MappedFeature";
        wfsSelector.setRelatedFeatureTypeNames(relNameList);
        return wfsSelector;
    }

    @Bean
    public KnownLayer knownTypeMiningActivity() {
        KnownLayer layer = new KnownLayer("erml-miningactivity", knownTypeMiningActivitySelector());
        layer.setName("Earth Resource Mining Activity");
        layer.setDescription(
                "A collection of services that implement the AuScope EarthResourceML v1 Profile for er:MiningActivity");
        layer.setGroup("Earth Resources(old)");
        layer.setProxyUrl("doMiningActivityFilter.do");
        layer.setProxyCountUrl("doMiningActivityFilterCount.do");
        layer.setProxyStyleUrl("doMiningActivityFilterStyle.do");
        layer.setProxyDownloadUrl("doMiningActivityFilterDownload.do");
        layer.setIconUrl("http://maps.google.com/mapfiles/kml/paddle/orange-blank.png");
        setupIcon(layer);
        layer.setOrder("12");
        layer.setStackdriverServiceGroup("EarthResourcesLayers");
        layer.setFilterCollection(this.createProviderFilterCollection());
        return layer;
    }

    @Bean
    public WMSWFSSelector knownTypeMineralTenementsSelector() {
        return new WMSWFSSelector("mt:MineralTenement", "MineralTenement");
    }

    @Bean
    public KnownLayer knownTypeMineralTenements() {
        KnownLayer layer = new KnownLayer("mineral-tenements", knownTypeMineralTenementsSelector());
        layer.setName("Mineral Tenements");
        layer.setDescription(
                "A collection of services that implement the AuScope EarthResourceML v1 Profile for mt:Mineral Tenement");
        layer.setGroup("Tenements");
        layer.setProxyUrl("getAllMineralTenementFeatures.do");
        layer.setProxyGetFeatureInfoUrl("getMineralTenementFeatureInfo.do");
        layer.setProxyCountUrl("getMineralTenementCount.do");
        layer.setProxyStyleUrl("getMineralTenementStyle.do");
        layer.setProxyDownloadUrl("doMineralTenementCSVDownload.do");
        layer.setOrder("150");
        layer.setStackdriverServiceGroup("TenementsLayers");

        UITextBox nameTextBox = new UITextBox("Name", "mt:name", null, Predicate.ISLIKE);
        List<ImmutablePair<String, String>> options = new ArrayList<ImmutablePair<String, String>>();
        options.add(new ImmutablePair<String, String>("Exploration", "exploration"));
        options.add(new ImmutablePair<String, String>("Prospecting", "prospecting"));
        options.add(new ImmutablePair<String, String>("Miscellaneous", "miscellaneous"));
        options.add(new ImmutablePair<String, String>("Mining Lease", "mining"));
        options.add(new ImmutablePair<String, String>("Licence", "licence"));
        UIDropDownSelectList uiDropDownSelectList = new UIDropDownSelectList("Tenement Type", "mt:tenementType", null,
                Predicate.ISLIKE, options);
        UITextBox ownerTextBox = new UITextBox("Owner", "mt:owner", null, Predicate.ISLIKE);
        UIDate expiryFromDate = new UIDate("Expiry From", "mt:expireDate", null, Predicate.BIGGER_THAN);
        UIDate expiryToDate = new UIDate("Expiry To", "mt:expireDate", null, Predicate.SMALLER_THAN);
        UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider("Provider", null);

        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        optionalFilters.add(nameTextBox);
        optionalFilters.add(uiDropDownSelectList);
        optionalFilters.add(ownerTextBox);
        optionalFilters.add(expiryFromDate);
        optionalFilters.add(expiryToDate);
        optionalFilters.add(uiCheckBoxGroupProvider);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);

        // Mandatory filters - note use of full path names for declaring vars to prevent
        // clashes with imported optional params
        List<ImmutablePair<String, String>> mandatoryOptions = new ArrayList<ImmutablePair<String, String>>();
        mandatoryOptions.add(new ImmutablePair<String, String>("Tenement Type", "TenementType"));
        mandatoryOptions.add(new ImmutablePair<String, String>("Tenement Status", "TenementStatus"));
        mandatoryOptions.add(new ImmutablePair<String, String>("UnStyled", null));
        org.auscope.portal.core.uifilter.mandatory.UIDropDownSelectList mandDropDownSelectList;
        mandDropDownSelectList = new org.auscope.portal.core.uifilter.mandatory.UIDropDownSelectList("Color Code",
                "ccProperty", "TenementType", mandatoryOptions);
        List<AbstractMandatoryParamBinding> mandParamList = new ArrayList<AbstractMandatoryParamBinding>();
        mandParamList.add(mandDropDownSelectList);
        filterCollection.setMandatoryFilters(mandParamList);
        layer.setFilterCollection(filterCollection);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeErlMineViewSelector() {
        return new WFSSelector("erl:MineView");
    }

    @Bean
    public KnownLayer knownTypeErlMineView() {
        KnownLayer layer = new KnownLayer("erl-mineview", knownTypeErlMineViewSelector());
        layer.setName("Mine View");
        layer.setDescription("Earth ResourceML Lite Mine");
        layer.setGroup("Earth Resources Lite(new)");
        layer.setProxyUrl("");
        layer.setProxyCountUrl("");
        layer.setProxyStyleUrl("getErlMineViewStyle.do");
        layer.setProxyDownloadUrl("");
        layer.setOrder("160");
        layer.setStackdriverServiceGroup("EarthResourcesLayers");

        // Optional filters
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("Name", "erl:name", null, Predicate.ISLIKE);
        UIDropDownRemote uiDropDownRemote = new UIDropDownRemote("Status", "erl:status_uri", null, Predicate.ISEQUAL,
                "getAllMineStatuses.do");
        UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider("Provider", null);
        UIPolygonBBox uiPolygonBBox = new UIPolygonBBox("Polygon BBox", "erl:shape", null, Predicate.ISEQUAL);
        optionalFilters.add(nameTextBox);
        optionalFilters.add(uiDropDownRemote);
        optionalFilters.add(uiCheckBoxGroupProvider);
        optionalFilters.add(uiPolygonBBox);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        layer.setFilterCollection(filterCollection);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeErlMineralOccurrenceViewSelector() {
        return new WFSSelector("erl:MineralOccurrenceView");
    }

    @Bean
    public KnownLayer knownTypeErlMineralOccurrenceView() {
        KnownLayer layer = new KnownLayer("erl-mineraloccurrenceview", knownTypeErlMineralOccurrenceViewSelector());
        layer.setName("Mineral Occurrence");
        layer.setDescription("Earth ResourceML Lite Mineral Occurrence View");
        layer.setGroup("Earth Resources Lite(new)");
        layer.setProxyUrl("");
        layer.setProxyCountUrl("");
        layer.setProxyStyleUrl("getErlMineralOccurrenceViewStyle.do");
        layer.setProxyDownloadUrl("");
        layer.setOrder("160");
        layer.setStackdriverServiceGroup("EarthResourcesLayers");

        // Optional filters
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("Name", "erl:name", null, Predicate.ISLIKE);
        UIDropDownRemote commodityDropDownRemote = new UIDropDownRemote("Commodity", "erl:representativeCommodity_uri",
                null, Predicate.ISEQUAL, "getAllCommodities.do");
        UIDropDownRemote timescaleDropDownRemote = new UIDropDownRemote("Geologic Timescale",
                "erl:representativeAge_uri", null, Predicate.ISEQUAL, "getAllTimescales.do");
        UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider("Provider", null);
        UIPolygonBBox uiPolygonBBox = new UIPolygonBBox("Polygon BBox", "erl:shape", null, Predicate.ISEQUAL);
        optionalFilters.add(nameTextBox);
        optionalFilters.add(commodityDropDownRemote);
        optionalFilters.add(timescaleDropDownRemote);
        optionalFilters.add(uiCheckBoxGroupProvider);
        optionalFilters.add(uiPolygonBBox);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        layer.setFilterCollection(filterCollection);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeErlCommodityResourceViewSelector() {
        return new WFSSelector("erl:CommodityResourceView");
    }

    @Bean
    public KnownLayer knownTypeErlCommodityResourceView() {
        KnownLayer layer = new KnownLayer("erl-commodityresourceview", knownTypeErlCommodityResourceViewSelector());
        layer.setName("Commodity Resource");
        layer.setDescription("Earth ResourceML Lite Commodity Resource View");
        layer.setGroup("Earth Resources Lite(new)");
        layer.setProxyUrl("");
        layer.setProxyCountUrl("");
        layer.setProxyStyleUrl("getErlCommodityResourceViewStyle.do");
        layer.setProxyDownloadUrl("");
        layer.setOrder("160");
        layer.setStackdriverServiceGroup("EarthResourcesLayers");

        // Optional filters
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("Mine Name", "erl:mineName", null, Predicate.ISLIKE);
        UIDropDownRemote commodityDropDownRemote = new UIDropDownRemote("Commodity", "erl:commodityClassifier_uri",
                null, Predicate.ISEQUAL, "getAllCommodities.do");
        UIDropDownRemote resourcesDropDownRemote = new UIDropDownRemote("JORC Category", "erl:resourcesCategory_uri",
                null, Predicate.ISEQUAL, "getAllJorcCategories.do");
        UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider("Provider", null);
        UIPolygonBBox uiPolygonBBox = new UIPolygonBBox("Polygon BBox", "erl:shape", null, Predicate.ISEQUAL);
        optionalFilters.add(nameTextBox);
        optionalFilters.add(commodityDropDownRemote);
        optionalFilters.add(resourcesDropDownRemote);
        optionalFilters.add(uiCheckBoxGroupProvider);
        optionalFilters.add(uiPolygonBBox);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        layer.setFilterCollection(filterCollection);
        return layer;
    }

    @Bean
    public WMSSelector knownTypeMineralOccurrenceViewSelector() {
        return new WMSSelector("mo:MinOccView");
    }

    @Bean
    public KnownLayer knownTypeMineralOccurrenceView() {
        KnownLayer layer = new KnownLayer("mineral-occ-view", knownTypeMineralOccurrenceViewSelector());
        layer.setName("Mineral Occurrence View");
        layer.setDescription(
                "A collection of services that implement the AuScope EarthResourceML v1 Profile for mo:MinOccView");
        layer.setGroup("Earth Resources(old)");
        layer.setProxyUrl("");
        layer.setProxyCountUrl("");
        layer.setProxyStyleUrl("doMinOccurViewFilterStyle.do");
        layer.setProxyDownloadUrl("downloadMinOccurView.do");
        layer.setOrder("13");
        layer.setStackdriverServiceGroup("EarthResourcesLayers");
        layer.setFilterCollection(this.createProviderFilterCollection());
        return layer;
    }

    @Bean
    public WMSSelector knownTypeMagnetotelluricsSelector() {
        return new WMSSelector("magnetotelluric");
    }

    @Bean KnownLayer knownTypeMagnetotellurics() {
        KnownLayer layer = new KnownLayer("magnetotellurics", knownTypeMagnetotelluricsSelector());
        layer.setId("magnetotellurics");
        layer.setName("Magnetotelluric Survey Sites");
        layer.setGroup("Magnetotellurics");
        layer.setDescription(
                "The magnetotelluric surveys were conducted in Australia with public funding (government and university) from the 1960s to present.");
        layer.setOrder("401");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypePMDCRCReportsSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setDescriptiveKeyword("PMD*CRC Publication");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypePMDCRCReports() {
        KnownLayer layer = new KnownLayer("portal-pmd-crc-reports", knownTypePMDCRCReportsSelector());
        layer.setName("GA PMD*CRC Reports");
        layer.setDescription("A collection of PMD*CRC reports from Geoscience Australia's Catalogue");
        layer.setIconUrl("http://maps.google.com/mapfiles/kml/paddle/blu-square.png");
        setupIcon(layer);
        layer.setOrder("405");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeGeoModelsSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setDescriptiveKeyword("3D Geological Models");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeGeoModels() {
        KnownLayer layer = new KnownLayer("portal-geo-models", knownTypeGeoModelsSelector());
        layer.setName("3D Geological Models");
        layer.setGroup("Models");
        layer.setDescription("Various Geological Models");
        layer.setIconUrl("http://maps.google.com/mapfiles/kml/paddle/blu-square.png");
        setupIcon(layer);
        layer.setOrder("407");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeReportsSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setDescriptiveKeyword("Report");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeReports() {
        KnownLayer layer = new KnownLayer("portal-reports", knownTypeReportsSelector());
        layer.setName("Reports");
        layer.setDescription("A collection of scientific reports that have been spatially located");
        layer.setIconUrl("http://maps.google.com/mapfiles/kml/paddle/blu-square.png");
        setupIcon(layer);
        layer.setOrder("410");
        return layer;
    }

    @Bean
    public WFSSelector knownTypeBoreholeNvclV2Selector() {
        String[] featNameList = { "sa:SamplingFeatureCollection", "om:GETPUBLISHEDSYSTEMTSA", "nvcl:scannedBorehole",
                "nvcl:ScannedBoreholeCollection" };
        String[] serviceEndPoints = { "http://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "https://nvclwebservices.vm.csiro.au:80/geoserverBH/wfs",
                "http://www.mrt.tas.gov.au:80/web-services/wfs", "https://www.mrt.tas.gov.au:80/web-services/wfs",
                "http://geossdi.dmp.wa.gov.au:80/services/wfs", "http://geossdi.dmp.wa.gov.au/services/wfs",
                "https://geossdi.dmp.wa.gov.au/services/wfs", "http://geology.data.nt.gov.au:80/geoserver/wfs",
                "https://geology.data.nt.gov.au/geoserver/wfs", "https://gs.geoscience.nsw.gov.au/geoserver/wfs",
                "https://sarigdata.pir.sa.gov.au/geoserver/wfs",
                "http://geology-uat.information.qld.gov.au/geoserver/wfs",
                "https://geology-uat.information.qld.gov.au/geoserver/wfs",
                "http://geology.information.qld.gov.au/geoserver/wfs",
                "https://geology.information.qld.gov.au/geoserver/wfs", "http://geology.data.vic.gov.au/nvcl/wfs",
                "https://geology.data.vic.gov.au/nvcl/wfs" };

        WFSSelector wfsSelector = new WFSSelector("gsmlp:BoreholeView", serviceEndPoints, true);
        wfsSelector.setRelatedFeatureTypeNames(featNameList);
        return wfsSelector;
    }

    @Bean
    public KnownLayer knownTypeBoreholeNvclV2() {
        KnownLayer layer = new KnownLayer("nvcl-v2-borehole", knownTypeBoreholeNvclV2Selector());
        layer.setName("National Virtual Core Library V-2.0");
        layer.setGroup("Boreholes");
        layer.setDescription(
                "A collection of services implementing the National Virtual Core Library Profile v1 for gsml:Borehole and a collection of observations");
        layer.setProxyUrl("doBoreholeViewFilter.do");
        layer.setProxyCountUrl("");
        layer.setProxyDownloadUrl("doNVCLBoreholeViewCSVDownload.do");
        layer.setProxyStyleUrl("doNvclV2FilterStyle.do");
        setupIcon(layer);
        layer.setOrder("51");
        layer.setStackdriverServiceGroup("NVCLBoreholeViewLayer");

        // Mandatory filters
        org.auscope.portal.core.uifilter.mandatory.UITextBox jobidTextBox;
        jobidTextBox = new org.auscope.portal.core.uifilter.mandatory.UITextBox("Analytics Job Id", "analyticsJobId",
                "");
        List<AbstractMandatoryParamBinding> mandParamList = new ArrayList<AbstractMandatoryParamBinding>();
        mandParamList.add(jobidTextBox);

        // Optional filters
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("Name", "gsmlp:name", null, Predicate.ISLIKE);
        UITextBox identifierTextBox = new UITextBox("Identifier", "gsmlp:identifier", null, Predicate.ISLIKE);
        this.addDrillDateFilters(optionalFilters);
        UIPolygonBBox uiPolygonBBox = new UIPolygonBBox("Polygon BBox - Clipboard", "gsmlp:shape", null,
                Predicate.ISEQUAL);
        UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider("Provider", null);
        optionalFilters.add(nameTextBox);
        optionalFilters.add(identifierTextBox);
        optionalFilters.add(uiPolygonBBox);
        optionalFilters.add(uiCheckBoxGroupProvider);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        filterCollection.setMandatoryFilters(mandParamList);
        layer.setFilterCollection(filterCollection);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeTimaGeoSampleSelector() {
        return new WFSSelector("tima:geosample_and_mineralogy");
    }

    @Bean
    public KnownLayer knownTypeTimaGeoSample() {
        KnownLayer layer = new KnownLayer("tima-geosample", knownTypeTimaGeoSampleSelector());
        layer.setName("TESCAN TIMA Heavy Mineral Analyses");
        layer.setGroup("Curtin University");
        layer.setDescription("A collection of the results published from TIMA");
        layer.setProxyUrl("doTIMAGeoSample.do");
        layer.setProxyCountUrl("");
        layer.setProxyDownloadUrl("doTIMAGeoSampleCSVDownload.do");
        layer.setIconUrl("http://maps.google.com/mapfiles/kml/paddle/grn-circle.png");
        setupIcon(layer);
        layer.setOrder("180");

        // Optional filters
        layer.setFilterCollection(this.createIgsnFilterCollection());
        return layer;
    }

    @Bean
    public WFSSelector knownTypeSHRIMPGeoSampleSelector() {
        return new WFSSelector("tima:view_shrimp_geochronology_result");
    }

    @Bean
    public KnownLayer knownTypeSHRIMPGeoSample() {
        KnownLayer layer = new KnownLayer("tima-shrimp-geosample", knownTypeSHRIMPGeoSampleSelector());
        layer.setName("SHRIMP Geochronology");
        layer.setGroup("Curtin University");
        layer.setDescription("A collection of the results published from SHRIMP U-Pb mass spectrometer");
        layer.setProxyUrl("doSHRIMPGeoSample.do");
        layer.setProxyCountUrl("");
        layer.setProxyDownloadUrl("doSHRIMPGeoSampleCSVDownload.do");
        layer.setIconUrl("http://maps.google.com/mapfiles/kml/paddle/ylw-circle.png");
        setupIcon(layer);
        layer.setOrder("180");

        // Optional filters
        layer.setFilterCollection(this.createIgsnFilterCollection());
        return layer;
    }

    @Bean
    public WFSSelector knownTypeSF0BoreholeNVCLSelector() {
        WFSSelector wfsSelector = new WFSSelector("gsmlp:BoreholeView");
        String[] featNameList = { "sa:SamplingFeatureCollection", "om:GETPUBLISHEDSYSTEMTSA", "nvcl:scannedBorehole",
                "nvcl:ScannedBoreholeCollection" };
        wfsSelector.setRelatedFeatureTypeNames(featNameList);
        return wfsSelector;
    }

    @Bean
    public KnownLayer knownTypeSF0BoreholeNVCL() {
        KnownLayer layer = new KnownLayer("sf0-borehole-nvcl", knownTypeSF0BoreholeNVCLSelector());
        layer.setName("All Boreholes");
        layer.setGroup("Boreholes");
        layer.setDescription(
                "A collection of services implementing the GeoSciML Portrayal Borehole View (gsmlp:BoreholeView)");
        layer.setProxyUrl("doBoreholeViewFilter.do");
        layer.setProxyCountUrl("");
        layer.setProxyStyleUrl("doBoreholeViewFilterStyle.do");
        setupIcon(layer);
        layer.setOrder("52");
        layer.setStackdriverServiceGroup("BoreholeViewLayer");

        // Mandatory filters
        org.auscope.portal.core.uifilter.mandatory.UITextBox jobidTextBox;
        jobidTextBox = new org.auscope.portal.core.uifilter.mandatory.UITextBox("Analytics Job Id", "analyticsJobId",
                "");
        List<AbstractMandatoryParamBinding> mandParamList = new ArrayList<AbstractMandatoryParamBinding>();
        mandParamList.add(jobidTextBox);

        // Optional filters
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("Name", "gsmlp:name", null, Predicate.ISLIKE);
        this.addDrillDateFilters(optionalFilters);
        UIPolygonBBox uiPolygonBBox = new UIPolygonBBox("Polygon BBox - Clipboard", "gsmlp:shape", null,
                Predicate.ISEQUAL);
        UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider("Provider", null);
        optionalFilters.add(nameTextBox);
        optionalFilters.add(uiPolygonBBox);
        optionalFilters.add(uiCheckBoxGroupProvider);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        filterCollection.setMandatoryFilters(mandParamList);
        layer.setFilterCollection(filterCollection);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeBoreholeMSCLSelector() {
        String[] serviceEndPoints = { "http://meiproc.earthsci.unimelb.edu.au:80/geoserver/wfs" };
        WFSSelector wfsSelector = new WFSSelector("gsmlp:BoreholeView", serviceEndPoints, true);
        String[] featNameList = { "mscl:scanned_data", "sa:SamplingFeatureCollection" };
        wfsSelector.setRelatedFeatureTypeNames(featNameList);
        return wfsSelector;
    }

    @Bean
    public KnownLayer knownTypeBoreholeMSCL() {
        KnownLayer layer = new KnownLayer("mscl-borehole", knownTypeBoreholeMSCLSelector());
        layer.setName("MSCL Data");
        layer.setDescription("Borehole observations made with a multi-sensor core logger.");
        layer.setGroup("Boreholes");
        layer.setProxyUrl("doBoreholeViewFilter.do");
        layer.setProxyStyleUrl("");
        layer.setProxyCountUrl("");
        setupIcon(layer);
        layer.setOrder("200");
        return layer;
    }
    
    @Bean
    public IRISSelector knownTypeSeismologyInSchoolSelector() {
        try {
            return new IRISSelector("S1","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }

    @Bean
    public KnownLayer knownTypeSeismologyInSchool() {
        KnownLayer layer = new KnownLayer("seismology-in-schools-site", knownTypeSeismologyInSchoolSelector());
        layer.setName("Seismographs in Schools Network");
        layer.setDescription(
                "Seismographs in Schools data feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("250");
        return layer;
    }

    @Bean
    public IRISSelector knownTypeSKIPPYSelector() {
        try {
            return new IRISSelector("7B","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
     
    @Bean
    public KnownLayer knownTypeSKIPPY(){
        KnownLayer layer = new KnownLayer("seismology-skippy", knownTypeSKIPPYSelector());
        layer.setName("SKIPPY");
        layer.setDescription(
                "The SKIPPY seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("251");
        return layer;
     }
    
    @Bean
    public IRISSelector knownTypeKIMBA97Selector() {
        try {
            return new IRISSelector("7D","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
     
    @Bean
    public KnownLayer knownTypeKIMBA97(){
        KnownLayer layer = new KnownLayer("seismology-kimba97", knownTypeKIMBA97Selector());
        layer.setName("KIMBA97: Kimberley Region in 1997");
        layer.setDescription(
                "The KIMBA97 seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("252");
        return layer;
     }
    
    @Bean
    public IRISSelector knownTypeKIMBA98Selector() {
        try {
            return new IRISSelector("7E","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
     
    @Bean
    public KnownLayer knownTypeKIMBA98(){
        KnownLayer layer = new KnownLayer("seismology-kimba98", knownTypeKIMBA98Selector());
        layer.setName("KIMBA98: Kimberley Region in 1998");
        layer.setDescription(
                "The KIMBA98 seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("253");
        return layer;
     }
    
    @Bean
    public IRISSelector knownTypeWACRATONSelector() {
        try {
            return new IRISSelector("7G","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
     
    @Bean
    public KnownLayer knownTypeWACRATON(){
        KnownLayer layer = new KnownLayer("seismology-wacraton", knownTypeWACRATONSelector());
        layer.setName("WACRATON: Western Australia");
        layer.setDescription(
                "The West Australian Cratons seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("254");
        return layer;
     }
    
    @Bean
    public IRISSelector knownTypeSEALSelector() {
        try {
            return new IRISSelector("7Q","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
     
    @Bean
    public KnownLayer knownTypeSEAL(){
        KnownLayer layer = new KnownLayer("seismology-seal", knownTypeSEALSelector());
        layer.setName("SEAL: South East Australia Linkage");
        layer.setDescription(
                "The SEAL seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("255");
        return layer;
     }    
    @Bean
    public IRISSelector knownTypeSEAL2Selector() {
        try {
            return new IRISSelector("7T","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
     
    @Bean
    public KnownLayer knownTypeSEAL2(){
        KnownLayer layer = new KnownLayer("seismology-seal2", knownTypeSEAL2Selector());
        layer.setName("SEAL2: South East Australia Linkage");
        layer.setDescription(
                "The SEAL2 seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("256");
        return layer;
     }  
    
    @Bean
    public IRISSelector knownTypeSEAL3Selector() {
        try {
            return new IRISSelector("7U","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
     
    @Bean
    public KnownLayer knownTypeSEAL3(){
        KnownLayer layer = new KnownLayer("seismology-seal3", knownTypeSEAL3Selector());
        layer.setName("SEAL3: South East Australia Linkage");
        layer.setDescription(
                "The SEAL3 seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("257");
        return layer;
     }  
    
    @Bean
    public IRISSelector knownTypeCAPRALSelector() {
        try {
            return new IRISSelector("7J","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
     
    @Bean
    public KnownLayer knownTypeCAPRAL(){
        KnownLayer layer = new KnownLayer("seismology-capral", knownTypeCAPRALSelector());
        layer.setName("CAPRAL: Northwest Australia");
        layer.setDescription(
                "The CAPRAL seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("258");
        return layer;
     }  
    
    @Bean
    public IRISSelector knownTypeSOCSelector() {
        try {
            return new IRISSelector("7K","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
    
     
    @Bean
    public KnownLayer knownTypeSOC(){
        KnownLayer layer = new KnownLayer("seismology-soc", knownTypeSOCSelector());
        layer.setName("SOC: Southern Craton");
        layer.setDescription(
                "The SOC seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("259");
        return layer;
     }  
    
    @Bean
    public IRISSelector knownTypeGAWLERSelector() {
        try {
            return new IRISSelector("1G","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
    
     
    @Bean
    public KnownLayer knownTypeGAWLER(){
        KnownLayer layer = new KnownLayer("seismology-gawler", knownTypeGAWLERSelector());
        layer.setName("GAWLER");
        layer.setDescription(
                "The GAWLER seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("260");
        return layer;
     } 
    
    @Bean
    public IRISSelector knownTypeBILBYSelector() {
        try {
            return new IRISSelector("6F","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
    
    @Bean
    public KnownLayer knownTypeBILBY(){
        KnownLayer layer = new KnownLayer("seismology-bilby", knownTypeBILBYSelector());
        layer.setName("BILBY: Australian Cratonic Lithosphere");
        layer.setDescription(
                "The BILBY seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("261");
        return layer;
     } 
    
    @Bean
    public IRISSelector knownTypeCURNAMONASelector() {
        try {
            return new IRISSelector("1F","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeCURNAMONA(){
        KnownLayer layer = new KnownLayer("seismology-curnamona", knownTypeCURNAMONASelector());
        layer.setName("CURNAMONA: Seismic structure of South Australia");
        layer.setDescription(
                "The CURNAMONA seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("262");
        return layer;
     }  
    
    @Bean
    public IRISSelector knownTypeMINQSelector() {
        try {
            return new IRISSelector("ZR","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeMINQ(){
        KnownLayer layer = new KnownLayer("seismology-minq", knownTypeMINQSelector());
        layer.setName("MINQ: Mount Isa Northern Queensland");
        layer.setDescription(
                "The MINQ seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("263");
        return layer;
     }  
    
    @Bean
    public IRISSelector knownTypeEAL1Selector() {
        try {
            return new IRISSelector("YJ","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeEAL1(){
        KnownLayer layer = new KnownLayer("seismology-eal1", knownTypeEAL1Selector());
        layer.setName("EAL1: Eastern Australia Linkage");
        layer.setDescription(
                "The EAL1 seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("264");
        return layer;
     } 
    
    @Bean
    public IRISSelector knownTypeEAL2Selector() {
        try {
            return new IRISSelector("1H","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeEAL2(){
        KnownLayer layer = new KnownLayer("seismology-eal2", knownTypeEAL2Selector());
        layer.setName("EAL2: Eastern Australia Linkage");
        layer.setDescription(
                "The EAL2 seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("265");
        return layer;
     } 
    
    @Bean
    public IRISSelector knownTypeEAL3Selector() {
        try {
            return new IRISSelector("7L","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeEAL3(){
        KnownLayer layer = new KnownLayer("seismology-eal3", knownTypeEAL3Selector());
        layer.setName("EAL3: Eastern Australia Linkage");
        layer.setDescription(
                "The EAL3 seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("266");
        return layer;
     } 
    
    @Bean
    public IRISSelector knownTypeBASSSelector() {
        try {
            return new IRISSelector("1P","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeBASS(){
        KnownLayer layer = new KnownLayer("seismology-bass", knownTypeBASSSelector());
        layer.setName("BASS: Bass strait from 2011 to 2013");
        layer.setDescription(
                "The BASS seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("267");
        return layer;
     } 
    
    @Bean
    public IRISSelector knownTypeSQEALSelector() {
        try {
            return new IRISSelector("8J","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeSQEAL(){
        KnownLayer layer = new KnownLayer("seismology-sqeal", knownTypeSQEALSelector());
        layer.setName("SQEAL: South Queensland Eastern Australia Linkage");
        layer.setDescription(
                "The SQEAL seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("268");
        return layer;
     }    
    
    @Bean
    public IRISSelector knownTypeAQ3Selector() {
        try {
            return new IRISSelector("4J","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeAQ3(){
        KnownLayer layer = new KnownLayer("seismology-aq3", knownTypeAQ3Selector());
        layer.setName("AQ3 SW Queensland and NW New South Wales");
        layer.setDescription(
                "The AQ3 seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("269");
        return layer;
     }  
    
    @Bean
    public IRISSelector knownTypeAQTSelector() {
        try {
            return new IRISSelector("1Q","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeAQT(){
        KnownLayer layer = new KnownLayer("seismology-aqt", knownTypeAQTSelector());
        layer.setName("AQT: Western Queensland");
        layer.setDescription(
                "The AQT seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("270");
        return layer;
     }    
    
    @Bean
    public IRISSelector knownTypeBANDASelector() {
        try {
            return new IRISSelector("YS","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeBANDA(){
        KnownLayer layer = new KnownLayer("seismology-banda", knownTypeBANDASelector());
        layer.setName("Banda Arc: Transitions in the Banda Arc-Australia continental collision");
        layer.setDescription(
                "The BANDA seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("271");
        return layer;
     }    
    
    @Bean
    public IRISSelector knownTypeASRSelector() {
        try {
            return new IRISSelector("5J","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeASR(){
        KnownLayer layer = new KnownLayer("seismology-asr", knownTypeASRSelector());
        layer.setName("ASR");
        layer.setDescription(
                "The ASR seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("272");
        return layer;
     }    
    @Bean
    public IRISSelector knownTypeMARLALINESelector() {
        try {
            return new IRISSelector("3G","http://auspass.edu.au:80");
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }    
     
    @Bean
    public KnownLayer knownTypeMARLALINE(){
        KnownLayer layer = new KnownLayer("seismology-marla-line", knownTypeBANDASelector());
        layer.setName("Marla Line");
        layer.setDescription(
                "The MARLA LINE seismic array feed from AusPass: the Australian Passive Seismic Server.");
        layer.setGroup("Passive Seismic");
        layer.setProxyUrl("getIRISStations.do");
        setupIcon(layer);
        layer.setOrder("273");
        return layer;
     }    
    @Bean
    public WMSSelector knownTypePassiveSeismicSelector() {
        return new WMSSelector("passive-seismic");
    }

    @Bean KnownLayer knownTypePassiveSeismic() {
        KnownLayer layer = new KnownLayer("passive-seismic", knownTypePassiveSeismicSelector());
        layer.setId("passive-seismic");
        layer.setName("Passive Seismic Sites");
        layer.setGroup("Passive Seismic");
        layer.setDescription("Passive Seismic Sites");
        layer.setOrder("274");
        return layer;
    }


    @Bean
    public WMSSelector knownTypeGeotransectsSelector() {
        return new WMSSelector("Onshore_Seismic_Surveys");
    }

    @Bean
    public KnownLayer knownTypeGeotransects() {
        KnownLayer layer = new KnownLayer("ga-geotransects", knownTypeGeotransectsSelector());
        layer.setId("ga-geotransects");
        layer.setName("GA Onshore Seismic Surveys");
        layer.setGroup("Passive Seismic");
        layer.setDescription(
                "The Onshore Seismic Data of Australia is a collection of all land seismic traverses cross the Australian continent and its margins. The data includes raw and processed data in SEGY format. The metadata includes acquisition reports, processing reports, processed images, logs, and so on. The data acquisition was carried out in Australia from 1949-2012 by Geoscience Australia and various partners. The set of reflection and refraction data comprises over 12,000 km of coverage, and provides an insight into the variations in crustal architecture in the varied geological domains. The complete processed dataset was first available for public access in Oct 2013 (http://www.ga.gov.au/minerals/projects/current-projects/seismic-acquisition-processing.html ). The location of seismic traverses is shown by the Gallery link on the webpage. The new survey data will be updated on the webpage by the official data release date. The attribute structure of the dataset has also been revised to be more compatible with the GeoSciML data standard, published by the IUGS Commission for Geoscience Information. The onshore seismic data were collected with partner organizations: Australian Geodynamics Cooperative Research Centre, National Research Facility for Earth Sounding, Australian Nuclear Science and Technology Organisation, Cooperative Research Centre for Greenhouse Gas Technologies, Curtin University of Technology, Geological Survey of New South Wales, NSW Department of Mineral Resources, NSW Department of Primary Industries Mineral Resources, An organisation for a National Earth Science Infrastructure Program, Geological Survey Western Australia, Northern Territory Geological Survey, Primary Industries and Resources South Australia, Predictive Mineral Discovery Cooperative Research Centre, Queensland Geological Survey, GeoScience Victoria Department of Primary Industries, Tasmania Development and Resources, University of Western Australia.");
        layer.setOrder("275");
        return layer;
    }

    // GA Geophysical Survey Datasets (GADDS v2.0)

    // All datasets
    @Bean
    public WMSSelector knownTypeGeophysSurveysSelector() {
        return new WMSSelector("gadds:geophysical_survey_datasets");
    }

    @Bean
    public KnownLayer knownTypeGeophysSurveys() {
        KnownLayer layer = new KnownLayer("ga-geophysicalsurveys-all", knownTypeGeophysSurveysSelector());
        layer.setId("ga-geophysicalsurveys-all");
        layer.setName("Geophysical Surveys - All");
        layer.setGroup("GA Geophysical Survey Datasets");
        layer.setDescription("Descriptions of geophysical datasets, their associated surveys, and the methods used for delivery of those datasets. Includes datasets collected from airborne, land, and marine surveys conducted or managed by Geoscience Australia and its predecessor agencies, as well as data and surveys from State and Territory geological survey agencies.");
        layer.setOrder("310");
        return layer;
    }

    // Gravity datasets
    @Bean
    public WMSSelector knownTypeGeophysSurveysGravSelector() {
        return new WMSSelector("gadds:geophysical_datasets_gravity");
    }

    @Bean
    public KnownLayer knownTypeGeophysGravSurveys() {
        KnownLayer layer = new KnownLayer("ga-geophysicalsurveys-grav", knownTypeGeophysSurveysGravSelector());
        layer.setId("ga-geophysicalsurveys-grav");
        layer.setName("Geophysical Surveys - Gravity");
        layer.setGroup("GA Geophysical Survey Datasets");
        layer.setDescription("Gravity data measures small changes in gravity due to changes in the density of rocks beneath the Earth's surface.");
        layer.setOrder("311");
        return layer;
    }

    // Radiometric datasets
    @Bean
    public WMSSelector knownTypeGeophysRadioSurveysSelector() {
        return new WMSSelector("gadds:geophysical_datasets_radiometric");
    }

    @Bean
    public KnownLayer knownTypeGeophysRadioSurveys() {
        KnownLayer layer = new KnownLayer("ga-geophysicalsurveys-radio", knownTypeGeophysRadioSurveysSelector());
        layer.setId("ga-geophysicalsurveys-radio");
        layer.setName("Geophysical Surveys - Radiometric");
        layer.setGroup("GA Geophysical Survey Datasets");
        layer.setDescription("The radiometric, or gamma-ray spectrometric method, measures the natural variations in the gamma-rays detected near the Earth's surface as the result of the natural radioactive decay of potassium (K), uranium (U) and thorium (Th). Radiometrics can tell us about the distribution of certain soils and rocks.");
        layer.setOrder("312");
        return layer;
    }

    // Magnetic datasets
    @Bean
    public WMSSelector knownTypeGeophysMagSurveysSelector() {
        return new WMSSelector("gadds:geophysical_datasets_magnetic");
    }

    @Bean
    public KnownLayer knownTypeGeophysMagSurveys() {
        KnownLayer layer = new KnownLayer("ga-geophysicalsurveys-mag", knownTypeGeophysMagSurveysSelector());
        layer.setId("ga-geophysicalsurveys-mag");
        layer.setName("Geophysical Surveys - Magnetic");
        layer.setGroup("GA Geophysical Survey Datasets");
        layer.setDescription("Total magnetic intensity (TMI) data measures variations in the intensity of the Earth's magnetic field caused by the contrasting content of rock-forming minerals in the Earth crust. Magnetic anomalies can be either positive (field stronger than normal) or negative (field weaker) depending on the susceptibility of the rock. ");
        layer.setOrder("313");
        return layer;
    }

    // Elevation datasets
    @Bean
    public WMSSelector knownTypeGeophysElevSurveysSelector() {
        return new WMSSelector("gadds:geophysical_datasets_elevation");
    }

    @Bean
    public KnownLayer knownTypeGeophysElevSurveys() {
        KnownLayer layer = new KnownLayer("ga-geophysicalsurveys-elev", knownTypeGeophysElevSurveysSelector());
        layer.setId("ga-geophysicalsurveys-elev");
        layer.setName("Geophysical Surveys - Elevation");
        layer.setGroup("GA Geophysical Survey Datasets");
        layer.setDescription("Digital Elevation data record the terrain height variations from the processed point-located data recorded during a geophysical survey.");
        layer.setOrder("314");
        return layer;
    }

    
    // GA National Geophysics Compilation

    // Bouguer Gravity Anomaly Grid of Onshore Australia 2016
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector1() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        // NB: Using recordId because there are multiple records with the same title/service name
        cswSelector.setRecordId("f0886_1142_1614_4044");
	return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys1() {
        KnownLayer layer = new KnownLayer("ga-geophys-1", knownTypeGANatGeoPhysSelector1());
        layer.setName("Bouguer Gravity Anomaly Grid of Onshore Australia 2016");
        layer.setDescription("This gravity anomaly grid is derived from observations stored in the Australian National Gravity Database (ANGD) as at February 2016 as well as data from the 2013 New South Wales Riverina gravity survey.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("351");
        return layer;
    }


    // Complete Bouguer Gravity Anomaly Grid of Onshore Australia 2016
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector2() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Complete Bouguer Gravity Anomaly Grid of Onshore Australia 2016");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys2() {
        KnownLayer layer = new KnownLayer("ga-geophys-2", knownTypeGANatGeoPhysSelector2());
        layer.setName("Complete Bouguer Gravity Anomaly Grid of Onshore Australia 2016");
        layer.setDescription("This gravity anomaly grid is derived from observations stored in the Australian National Gravity Database (ANGD) as at February 2016 as well as data from the 2013 New South Wales Riverina gravity survey.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("352");
        return layer;
    }


    // Isostatic Residual Gravity Anomaly Grid of Onshore Australia 2016
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector3() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        // NB: Using recordId because there are multiple records with the same title/service name
        cswSelector.setRecordId("f4523_7439_0612_0991");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys3() {
        KnownLayer layer = new KnownLayer("ga-geophys-3", knownTypeGANatGeoPhysSelector3());
        layer.setName("Isostatic Residual Gravity Anomaly Grid of Onshore Australia 2016");
        layer.setDescription("This grid is derived from gravity observations stored in the Australian National Gravity Database (ANGD) as at February 2016 as well as data from the 2013 New South Wales Riverina gravity survey.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("353");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 filtered pct potassium grid
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector4() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 filtered pct potassium grid");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys4() {
        KnownLayer layer = new KnownLayer("ga-geophys-4", knownTypeGANatGeoPhysSelector4());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 filtered pct potassium grid");
        layer.setDescription("The filtered potassium grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("354");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 filtered ppm thorium
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector5() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 filtered ppm thorium");
	    return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys5() {
        KnownLayer layer = new KnownLayer("ga-geophys-5", knownTypeGANatGeoPhysSelector5());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 filtered ppm thorium");
        layer.setDescription("The filtered thorium grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("355");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 filtered ppm uranium
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector6() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 filtered ppm uranium");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys6() {
        KnownLayer layer = new KnownLayer("ga-geophys-6", knownTypeGANatGeoPhysSelector6());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 filtered ppm uranium");
        layer.setDescription("The filtered uranium grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("356");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 filtered terrestrial dose rate
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector7() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 filtered terrestrial dose rate");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys7() {
        KnownLayer layer = new KnownLayer("ga-geophys-7", knownTypeGANatGeoPhysSelector7());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 filtered terrestrial dose rate");
        layer.setDescription("The filtered terrestrial dose rate grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia, made of a combination of over 600 individual survey grids.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("357");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 ratio thorium over potassium
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector8() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 ratio thorium over potassium");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys8() {
        KnownLayer layer = new KnownLayer("ga-geophys-8", knownTypeGANatGeoPhysSelector8());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 ratio thorium over potassium");
        layer.setDescription("The thorium over potassium grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("358");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 ratio uranium over potassium
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector9() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 ratio uranium over potassium");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys9() {
        KnownLayer layer = new KnownLayer("ga-geophys-9", knownTypeGANatGeoPhysSelector9());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 ratio uranium over potassium");
        layer.setDescription("The uranium over potassium grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia comprising over 600 airborne gamma-ray spectrometric surveys.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("359");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 ratio uranium over thorium
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector10() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 ratio uranium over thorium");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys10() {
        KnownLayer layer = new KnownLayer("ga-geophys-10", knownTypeGANatGeoPhysSelector10());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 ratio uranium over thorium");
        layer.setDescription("The uranium over thorium grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia which is a merge of over 600 individual gamma-ray spectrometric surveys.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("360");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 ratio uranium squared over thorium 
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector11() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 ratio uranium squared over thorium");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys11() {
        KnownLayer layer = new KnownLayer("ga-geophys-11", knownTypeGANatGeoPhysSelector11());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 ratio uranium squared over thorium");
        layer.setDescription("The uranium squared over thorium grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("361");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 unfiltered pct potassium
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector12() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 unfiltered pct potassium");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys12() {
        KnownLayer layer = new KnownLayer("ga-geophys-12", knownTypeGANatGeoPhysSelector12());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 unfiltered pct potassium");
        layer.setDescription("The unfiltered potassium grid is a derivative of the 2019 radiometric grid of Australia.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("362");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 unfiltered ppm thorium
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector13() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 unfiltered ppm thorium");;
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys13() {
        KnownLayer layer = new KnownLayer("ga-geophys-13", knownTypeGANatGeoPhysSelector13());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 unfiltered ppm thorium");
        layer.setDescription("The unfiltered thorium grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia which is a merge of over 600 individual gamma-ray spectrometric surveys.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("363");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 unfiltered ppm uranium
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector14() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 unfiltered ppm uranium");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys14() {
        KnownLayer layer = new KnownLayer("ga-geophys-14", knownTypeGANatGeoPhysSelector14());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 unfiltered ppm uranium");
        layer.setDescription("The unfiltered uranium grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia which is a merge of over 600 individual gamma-ray spectrometric surveys.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("364");
        return layer;
    }


    // Radiometric Grid of Australia (Radmap) v4 2019 unfiltered terrestrial dose rate
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector15() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Radiometric Grid of Australia (Radmap) v4 2019 unfiltered terrestrial dose rate");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys15() {
        KnownLayer layer = new KnownLayer("ga-geophys-15", knownTypeGANatGeoPhysSelector15());
        layer.setName("Radiometric Grid of Australia (Radmap) v4 2019 unfiltered terrestrial dose rate");
        layer.setDescription("The unfiltered terrestrial dose rate grid is a derivative of the 2019 radiometric or gamma-ray grid of Australia, which is a merge of over 600 individual gamma-ray spectrometric surveys.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("365");
        return layer;
    }


    // Total Magnetic Intensity (TMI) Grid of Australia 2019 - seventh edition - 40 m cell size
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector16() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Total Magnetic Intensity (TMI) Grid of Australia 2019 - seventh edition - 40 m cell size");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys16() {
        KnownLayer layer = new KnownLayer("ga-geophys-16", knownTypeGANatGeoPhysSelector16());
        layer.setName("Total Magnetic Intensity (TMI) Grid of Australia 2019 - seventh edition - 40 m cell size");
        layer.setDescription("Total magnetic intensity (TMI) data measures variations in the intensity of the Earth magnetic field caused by the contrasting content of rock-forming minerals in the Earth crust.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("366");
        return layer;
    }


    // Total Magnetic Intensity (TMI) Grid of Australia 2019 - seventh edition - 80 m cell size
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector17() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Total Magnetic Intensity (TMI) Grid of Australia 2019 - seventh edition - 80 m cell size");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys17() {
        KnownLayer layer = new KnownLayer("ga-geophys-17", knownTypeGANatGeoPhysSelector17());
        layer.setName("Total Magnetic Intensity (TMI) Grid of Australia 2019 - seventh edition - 80 m cell size");
        layer.setDescription("Total magnetic intensity (TMI) data measures variations in the intensity of the Earth magnetic field caused by the contrasting content of rock-forming minerals in the Earth crust.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("367");
        return layer;
    }


    // Total Magnetic Intensity (TMI) Grid of Australia with Variable Reduction to Pole (VRTP) 2019 - seventh edition
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector18() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Total Magnetic Intensity (TMI) Grid of Australia with Variable Reduction to Pole (VRTP) 2019 - seventh edition");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys18() {
        KnownLayer layer = new KnownLayer("ga-geophys-18", knownTypeGANatGeoPhysSelector18());
        layer.setName("Total Magnetic Intensity (TMI) Grid of Australia with Variable Reduction to Pole (VRTP) 2019 - seventh edition");
        layer.setDescription("The 2019 Total magnetic Intensity (TMI) grid of Australia with variable reduction to pole (VRTP) has a grid cell size of ~3 seconds of arc (approximately 80 m). This grid only includes airborne-derived TMI data for onshore and near-offshore continental areas.");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("368");
        return layer;
    }


    // Total Magnetic Intensity Grid of Australia 2019 - First Vertical Derivative (1VD)
    @Bean CSWRecordSelector knownTypeGANatGeoPhysSelector19() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("Total Magnetic Intensity Grid of Australia 2019 - First Vertical Derivative (1VD)");
        return cswSelector;
    }

    @Bean KnownLayer knownTypeGANatGeoPhys19() {
        KnownLayer layer = new KnownLayer("ga-geophys-19", knownTypeGANatGeoPhysSelector19());
        layer.setName("Total Magnetic Intensity Grid of Australia 2019 - First Vertical Derivative (1VD)");
        layer.setDescription(" The first vertical derivative (1VD) grid is derived from the 2019 Total magnetic Intensity (TMI) grid of Australia which has a grid cell size of ~3 seconds of arc (approximately 80 m).");
        layer.setGroup("GA/NCI National Geophysical Compilations");
        layer.setOrder("369");
        return layer;
    }
    // End of GA National Geophysics Compilation

    @Bean
    public CSWRecordSelector knownTypeAsterAlohSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map AlOH group composition");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeAsterAloh() {
        KnownLayer layer = new KnownLayer("aster-aloh", knownTypeAsterAlohSelector());
        layer.setName("AlOH group composition");
        layer.setDescription(
                "1. Band ratio: B5/B7Blue is well ordered kaolinite, Al-rich muscovite/illite, paragonite, pyrophyllite Red is Al-poor (Si-rich) muscovite (phengite)");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_020");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeAsterFerrousSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map Ferrous iron index");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeAsterFerrous() {
        KnownLayer layer = new KnownLayer("aster-ferrous", knownTypeAsterFerrousSelector());
        layer.setName("Ferrous iron index");
        layer.setDescription("1. Band ratio: B5/B4Blue is low abundance, Red is high abundance");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_030");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeAsterOpaqueSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map Opaque index");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeAsterOpaque() {
        KnownLayer layer = new KnownLayer("aster-opaque", knownTypeAsterOpaqueSelector());
        layer.setName("Opaque index");
        layer.setDescription(
                "1. Band ratio: B1/B4Blue is low abundance, Red is high abundance(potentially includes  carbon black (e.g. ash), magnetite, Mn oxides, and sulphides in unoxidised envornments");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_040");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeAsterFerricOxideContentSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map Ferric oxide content");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeAsterFerricOxideContent() {
        KnownLayer layer = new KnownLayer("aster-ferric-oxide-content", knownTypeAsterFerricOxideContentSelector());
        layer.setName("Ferric oxide content");
        layer.setDescription("1. Band ratio: B4/B3Blue is low abundance, Red is high abundance");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_050");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeAsterFeohSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map FeOH group content");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeAsterFeoh() {
        KnownLayer layer = new KnownLayer("aster-feoh", knownTypeAsterFeohSelector());
        layer.setName("FeOH group content");
        layer.setDescription(
                "1. Band ratio: (B6+B8)/B7Blue is low content, Red is high content(potentially includes: chlorite, epidote, jarosite, nontronite, gibbsite, gypsum, opal-chalcedony)");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_060");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeFerricOxideCompSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map Ferric oxide composition");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeFerricOxideComp() {
        KnownLayer layer = new KnownLayer("aster-ferric-oxide-comp", knownTypeFerricOxideCompSelector());
        layer.setName("Ferric oxide composition");
        layer.setDescription(
                "1. Band ratio: B2/B1Blue-cyan is goethite rich, Green is hematite-goethite, Red-yellow is hematite-rich");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_070");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeGroupIndexSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map Kaolin group index");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeGroupIndex() {
        KnownLayer layer = new KnownLayer("aster-group-index", knownTypeGroupIndexSelector());
        layer.setName("Kaolin group index");
        layer.setDescription(
                "B6/B5(potential includes: pyrophyllite, alunite, well-ordered kaolinite)Blue is low content, Red is high content");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_080");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeQuartzIndexSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map TIR Quartz index");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeQuartzIndex() {
        KnownLayer layer = new KnownLayer("aster-quartz-index", knownTypeQuartzIndexSelector());
        layer.setName("TIR Quartz index");
        layer.setDescription("1. Band ratio: B11/(B10+B12)Blue is low quartz contentRed is high quartz content");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_090");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeMgohContentSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map MgOH group content");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeMgohContent() {
        KnownLayer layer = new KnownLayer("aster-mgoh-content", knownTypeMgohContentSelector());
        layer.setName("MgOH group content");
        layer.setDescription(
                "1. Band ratio: (B6+B9/(B7+B8)Blue is low content, Red is high content(potentially includes: calcite, dolomite, magnesite, chlorite, epidote, amphibole, talc, serpentine)");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_100");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeGreenVegSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map green vegetation content");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeGreenVeg() {
        KnownLayer layer = new KnownLayer("aster-green-veg", knownTypeGreenVegSelector());
        layer.setName("Green vegetation content");
        layer.setDescription("Band ratio: B3/B2 Blue is low contentRed is high content");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_110");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeFerrCarbSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map Ferrous iron content in MgOH/carbonate");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeFerrCarb() {
        KnownLayer layer = new KnownLayer("aster-ferr-carb", knownTypeFerrCarbSelector());
        layer.setName("Ferrous iron content in MgOH/carbonate");
        layer.setDescription(
                "1. Band ratio: B5/B4Blue is low ferrous iron content in carbonate and MgOH minerals like talc and tremolite.Red is high ferrous iron content in carbonate and MgOH minerals like chlorite and actinolite.");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_120");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeMgohGroupCompSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map MgOH group composition");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeMgohGroupComp() {
        KnownLayer layer = new KnownLayer("aster-mgoh-group-comp", knownTypeMgohGroupCompSelector());
        layer.setName("MgOH group composition");
        layer.setDescription(
                "1. Band ratio: B7/B8Blue-cyan is magnesite-dolomite, amphibole, chlorite\tRed is calcite, epidote, amphibole");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_130");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeAlohGroupContentSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map AlOH group content");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeAlohGroupContent() {
        KnownLayer layer = new KnownLayer("aster-aloh-group-content", knownTypeAlohGroupContentSelector());
        layer.setName("AlOH group content");
        layer.setDescription(
                "1. Band ratio: (B5+B7)/B6Blue is low abundance, Red is high abundance potentially includes: phengite, muscovite, paragonite, lepidolite, illite, brammalite, montmorillonite, beidellite, kaolinite, dickite");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_160");
        return layer;
    }

    // NB: Disabled this layer until colour representation issues are sorted out
    //
    // @Bean
    // public CSWRecordSelector knownTypeGypsumContentSelector() {
    //     CSWRecordSelector cswSelector = new CSWRecordSelector();
    //     cswSelector.setServiceName("National ASTER Map TIR Gypsum index");
    //     return cswSelector;
    // }

    // @Bean
    // public KnownLayer knownTypeGypsumContent() {
    //     KnownLayer layer = new KnownLayer("aster-gypsum-content", knownTypeGypsumContentSelector());
    //     layer.setName("TIR Gypsum index");
    //     layer.setDescription("1. Band ratio: (B10+B12)/B11Blue is low gypsum contentRed is high gypsum content");
    //     layer.setGroup("ASTER Maps");
    //     layer.setOrder("30_ASTER Maps_170");
    //     return layer;
    // }

    @Bean
    public CSWRecordSelector knownTypeSilicaContentSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setServiceName("National ASTER Map TIR Silica index");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeSilicaContent() {
        KnownLayer layer = new KnownLayer("aster-silica-content", knownTypeSilicaContentSelector());
        layer.setName("TIR Silica index");
        layer.setDescription(
                "1. Band ratio: B13/B10Blue is low silica contentRed is high silica content(potentially includes Si-rich minerals, such as quartz, feldspars, Al-clays)");
        layer.setGroup("ASTER Maps");
        layer.setOrder("30_ASTER Maps_180");
        return layer;
    }

    @Bean
    public WFSSelector knownTypeRemanentAnomaliesSelector() {
        String[] serviceEndPoints = { "http://remanentanomalies.csiro.au/geoserver/wfs" };
        WFSSelector wfsSelector = new WFSSelector("RemAnom:Anomaly", serviceEndPoints, true);
        return wfsSelector;
    }

    @Bean
    public KnownLayer knownTypeRemanentAnomalies() {
        KnownLayer layer = new KnownLayer("remanent-anomalies", knownTypeRemanentAnomaliesSelector());
        layer.setName("AUS5 - Remanent Anomalies");
        layer.setDescription("A collection of services publishing magnetic anomalies");
        layer.setGroup("Magnetics");
        layer.setProxyUrl("");
        layer.setProxyCountUrl("");
        layer.setProxyStyleUrl("getRemanentAnomaliesStyle.do");
        layer.setProxyDownloadUrl("doRemanentAnomaliesDownload.do");
        layer.setOrder("80");

        // Optional filters
        layer.setFilterCollection(this.createTextBoxFilterCollection("Remanent Anomaly Name", "RenAnom:AnomalyName"));

        return layer;
    }

    @Bean
    public WFSSelector knownTypeRemanentAnomaliesAutoSearchSelector() {
        return new WFSSelector("RemAnomAutoSearch:AutoSearchAnomalies");
    }

    @Bean
    public KnownLayer knownTypeRemanentAnomaliesAutoSearch() {
        KnownLayer layer = new KnownLayer("remanent-anomalies-AutoSearch",
                knownTypeRemanentAnomaliesAutoSearchSelector());
        layer.setName("AUS5 - AutoSearch Anomalies");
        layer.setDescription("A collection of services publishing magnetic anomalies");
        layer.setGroup("Magnetics");
        layer.setProxyUrl("");
        layer.setProxyCountUrl("");
        layer.setProxyStyleUrl("getRemanentAnomaliesAutoSearchStyle.do");
        layer.setProxyDownloadUrl("doRemanentAnomaliesAutoSearchDownload.do");
        layer.setOrder("81");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeRemanentAnomaliesTMISelector() {
        return new WMSSelector("mag:australia_tmihires2a");
    }

    @Bean
    public KnownLayer knownTypeRemanentAnomaliesTMI() {
        KnownLayer layer = new KnownLayer("remanent-anomaliesTMI", knownTypeRemanentAnomaliesTMISelector());
        layer.setName("AUS5 - Total Magnetic Intensity");
        layer.setDescription(
                "Total Magnetic Intensity from Magnetic Anomaly Map of Australia (fifth edition), Geoscience Australia and other sources");
        layer.setGroup("Magnetics");
        layer.setOrder("82");
        return layer;
    }

    @Bean
    public WFSSelector knownTypeEMAGRemanentAnomaliesSelector() {
        String[] serviceEndPoints = { "http://remanentanomalies.csiro.au/geoserverEMAG/wfs" };
        return new WFSSelector("RemAnom:Anomaly", serviceEndPoints, true);
    }

    @Bean
    public KnownLayer knownTypeEMAGRemanentAnomalies() {
        KnownLayer layer = new KnownLayer("remanent-anomalies-EMAG", knownTypeEMAGRemanentAnomaliesSelector());
        layer.setName("EMAG2 - Remanent Anomalies");
        layer.setDescription("A collection of services publishing magnetic anomalies from the EMAG2 layer");
        layer.setGroup("Magnetics");
        layer.setProxyUrl("");
        layer.setProxyCountUrl("");
        layer.setProxyStyleUrl("getRemanentAnomaliesStyle.do");
        layer.setProxyDownloadUrl("doRemanentAnomaliesDownload.do");
        layer.setOrder("83");

        // Optional filters
        layer.setFilterCollection(this.createTextBoxFilterCollection("Remanent Anomaly Name", "RemAnom:AnomalyName"));
        return layer;
    }

    @Bean
    public WMSSelector knownTypeEMAGRemanentAnomaliesTMISelector() {
        String[] serviceEndPoints = { "http://remanentanomalies.csiro.au/thredds/wms/Emag2/EMAG2.nc" };
        return new WMSSelector("z", serviceEndPoints, true);
    }

    @Bean
    public KnownLayer knownTypeEMAGRemanentAnomaliesTMI() {
        KnownLayer layer = new KnownLayer("remanent-anomalies-EMAGTMI", knownTypeEMAGRemanentAnomaliesTMISelector());
        layer.setName("EMAG2 - Total Magnetic Intensity");
        layer.setDescription(
                "Total Magnetic Intensity from the EMAG2 datasource.http://www.geomag.org/models/emag2.html");
        layer.setGroup("Magnetics");
        layer.setOrder("84");
        return layer;
    }

    @Bean
    public WFSSelector knownTypeSamplingPointSelector() {
        return new WFSSelector("sa:SamplingPoint");
    }

    @Bean
    public KnownLayer knownTypeSamplingPoint() {
        KnownLayer layer = new KnownLayer("notused-samplingpoint", knownTypeSamplingPointSelector());
        layer.setHidden(true);
        layer.setOrder("450");
        return layer;
    }

    @Bean
    public WFSSelector knownTypeFeatureCollectionSelector() {
        return new WFSSelector("gml:FeatureCollection");
    }

    @Bean
    public KnownLayer knownTypeFeatureCollection() {
        KnownLayer layer = new KnownLayer("notused-featurecollection", knownTypeFeatureCollectionSelector());
        layer.setHidden(true);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeHighPSiteFeatureTypeSelector() {
        return new WFSSelector("highp:HighPSiteFeatureType");
    }

    @Bean
    public KnownLayer knownTypeHighPSiteFeatureType() {
        KnownLayer layer = new KnownLayer("notused-highpsitefeaturetype", knownTypeHighPSiteFeatureTypeSelector());
        layer.setHidden(true);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeHighPFeatureTypeSelector() {
        return new WFSSelector("highp:HighPFeatureType");
    }

    @Bean
    public KnownLayer knownTypeHighPFeatureType() {
        KnownLayer layer = new KnownLayer("notused-highpfeaturetype", knownTypeHighPFeatureTypeSelector());
        layer.setHidden(true);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeHighPREIronLayerSelector() {
        return new WFSSelector("HighP-RE-IronLayer");
    }

    @Bean
    public KnownLayer knownTypeHighPREIronLayer() {
        KnownLayer layer = new KnownLayer("notused-highpreironlayer", knownTypeHighPREIronLayerSelector());
        layer.setHidden(true);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeHighPREPhosLayerSelector() {
        return new WFSSelector("HighP-RE-PhosLayer");
    }

    @Bean
    public KnownLayer knownTypeHighPREPhosLayer() {
        KnownLayer layer = new KnownLayer("notused-highprephoslayer", knownTypeHighPREPhosLayerSelector());
        layer.setHidden(true);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeHighPSiteIronLayerSelector() {
        return new WFSSelector("HighP-Site-IronLayer");
    }

    @Bean
    public KnownLayer knownTypeHighPSiteIronLayer() {
        KnownLayer layer = new KnownLayer("notused-highpsiteironlayer", knownTypeHighPSiteIronLayerSelector());
        layer.setHidden(true);
        return layer;
    }

    @Bean
    public WFSSelector knownTypeHighPSitePhosLayerSelector() {
        return new WFSSelector("HighP-Site-PhosLayer");
    }

    @Bean
    public KnownLayer knownTypeHighPSitePhosLayer() {
        KnownLayer layer = new KnownLayer("notused-highpsitephoslayer", knownTypeHighPSitePhosLayerSelector());
        layer.setHidden(true);
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypePortalsSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setDescriptiveKeyword("Portal");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypePortals() {
        KnownLayer layer = new KnownLayer("notused-portals", knownTypePortalsSelector());
        layer.setHidden(true);
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeGeoNetworksSelector() {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        cswSelector.setDescriptiveKeyword("GeoNetwork");
        return cswSelector;
    }

    @Bean
    public KnownLayer knownTypeGeoNetworks() {
        KnownLayer layer = new KnownLayer("notused-geonetwork", knownTypeGeoNetworksSelector());
        layer.setHidden(true);
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvGeologicalUnit250KSelector() {
        return new WMSSelector("erdd:GSV_SG_250K_GEOLUNIT");
    }

    @Bean
    public KnownLayer knownTypeGsvGeologicalUnit250K() {
        KnownLayer layer = new KnownLayer("gsv-geological-unit-250k-", knownTypeGsvGeologicalUnit250KSelector());
        layer.setId("gsv-geological-unit-250k-");
        layer.setName("Gsv Geological Unit 250K ");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Geological units represented as two dimensional polygons, designed for portrayal");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvGeologicalUnitContact250KSelector() {
        return new WMSSelector("erdd:GSV_SG_250K_GEOLUNITCONTACT");
    }

    @Bean
    public KnownLayer knownTypeGsvGeologicalUnitContact250K() {
        KnownLayer layer = new KnownLayer("gsv-geological-unit-contact-250k-",
                knownTypeGsvGeologicalUnitContact250KSelector());
        layer.setId("gsv-geological-unit-contact-250k-");
        layer.setName("Gsv Geological Unit Contact 250K ");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Geological boundaries represented as two dimensional lines, designed for portray");
        layer.setOrder("Registered_2");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvGeologicalUnitContact50KSelector() {
        return new WMSSelector("erdd:GSV_SG_50K_GEOLUNITCONTACT");
    }

    @Bean
    public KnownLayer knownTypeGsvGeologicalUnitContact50K() {
        KnownLayer layer = new KnownLayer("gsv-geological-unit-contact-50k-",
                knownTypeGsvGeologicalUnitContact50KSelector());
        layer.setId("gsv-geological-unit-contact-50k-");
        layer.setName("Gsv Geological Unit Contact 50K ");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Geological boundaries represented as two dimensional lines, designed for portray");
        layer.setOrder("Registered_3");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvGeologicalUnit250KAgeSelector() {
        return new WMSSelector("erdd:GSV_SG_250K_GEOLUNIT_AGE");
    }

    @Bean
    public KnownLayer knownTypeGsvGeologicalUnit250KAge() {
        KnownLayer layer = new KnownLayer("gsv-geological-unit-250k-age", knownTypeGsvGeologicalUnit250KAgeSelector());
        layer.setId("gsv-geological-unit-250k-age");
        layer.setName("Gsv Geological Unit 250K Age");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Geological units represented as two dimensional polygons, designed for portrayal");
        layer.setOrder("Registered_4");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvShearDisplacementStructure250KSelector() {
        return new WMSSelector("erdd:GSV_SG_250K_SHEARDISPSTRUCTURE");
    }

    @Bean
    public KnownLayer knownTypeGsvShearDisplacementStructure250K() {
        KnownLayer layer = new KnownLayer("gsv-shear-displacement-structure-250k-",
                knownTypeGsvShearDisplacementStructure250KSelector());
        layer.setId("gsv-shear-displacement-structure-250k-");
        layer.setName("Gsv Shear Displacement Structure 250K ");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Shear displacement structures (faults) represented as two dimensional lines, des");
        layer.setOrder("Registered_5");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvGeologicalUnit250KLithologySelector() {
        return new WMSSelector("erdd:GSV_SG_250K_GEOLUNIT_LITHOLOGY");
    }

    @Bean
    public KnownLayer knownTypeGsvGeologicalUnit250KLithology() {
        KnownLayer layer = new KnownLayer("gsv-geological-unit-250k-lithology",
                knownTypeGsvGeologicalUnit250KLithologySelector());
        layer.setId("gsv-geological-unit-250k-lithology");
        layer.setName("Gsv Geological Unit 250K Lithology");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Geological units represented as two dimensional polygons, designed for portrayal");
        layer.setOrder("Registered_6");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvGeologicalUnit50KLithologySelector() {
        return new WMSSelector("erdd:GSV_SG_50K_GEOLUNIT_LITHOLOGY");
    }

    @Bean
    public KnownLayer knownTypeGsvGeologicalUnit50KLithology() {
        KnownLayer layer = new KnownLayer("gsv-geological-unit-50k-lithology",
                knownTypeGsvGeologicalUnit50KLithologySelector());
        layer.setId("gsv-geological-unit-50k-lithology");
        layer.setName("Gsv Geological Unit 50K Lithology");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Geological units represented as two dimensional polygons, designed for portrayal");
        layer.setOrder("Registered_7");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvGeologicalUnit50KSelector() {
        return new WMSSelector("erdd:GSV_SG_50K_GEOLUNIT");
    }

    @Bean
    public KnownLayer knownTypeGsvGeologicalUnit50K() {
        KnownLayer layer = new KnownLayer("gsv-geological-unit-50k-", knownTypeGsvGeologicalUnit50KSelector());
        layer.setId("gsv-geological-unit-50k-");
        layer.setName("Gsv Geological Unit 50K ");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Geological units represented as two dimensional polygons, designed for portrayal");
        layer.setOrder("Registered_8");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvShearDisplacementStructure50KSelector() {
        return new WMSSelector("erdd:GSV_SG_50K_SHEARDISPSTRUCTURE");
    }

    @Bean
    public KnownLayer knownTypeGsvShearDisplacementStructure50K() {
        KnownLayer layer = new KnownLayer("gsv-shear-displacement-structure-50k-",
                knownTypeGsvShearDisplacementStructure50KSelector());
        layer.setId("gsv-shear-displacement-structure-50k-");
        layer.setName("Gsv Shear Displacement Structure 50K ");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Shear displacement structures (faults) represented as two dimensional lines, des");
        layer.setOrder("Registered_9");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGsvGeologicalUnit50KAgeSelector() {
        return new WMSSelector("erdd:GSV_SG_50K_GEOLUNIT_AGE");
    }

    @Bean
    public KnownLayer knownTypeGsvGeologicalUnit50KAge() {
        KnownLayer layer = new KnownLayer("gsv-geological-unit-50k-age", knownTypeGsvGeologicalUnit50KAgeSelector());
        layer.setId("gsv-geological-unit-50k-age");
        layer.setName("Gsv Geological Unit 50K Age");
        layer.setGroup("Geological Survey of Victoria");
        layer.setDescription("Geological units represented as two dimensional polygons, designed for portrayal");
        layer.setOrder("Registered_10");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeL180MtIsaDeepCrusSeisSurvQld2006StacAndMigrDataAndImagForLine06GaTo06GaSelector() {
        CSWRecordSelector cswRecord = new CSWRecordSelector();
        cswRecord.setRecordId("a05f7892-ee53-7506-e044-00144fdd4fa6");
        return cswRecord;
    }

    @Bean
    public KnownLayer knownTypeL180MtIsaDeepCrusSeisSurvQld2006StacAndMigrDataAndImagForLine06GaTo06Ga() {
        KnownLayer layer = new KnownLayer(
                "l180-mt-isa-deep-crus-seis-surv-qld-2006-stac-and-migr-data-and-imag-for-line-06ga-to-06ga",
                knownTypeL180MtIsaDeepCrusSeisSurvQld2006StacAndMigrDataAndImagForLine06GaTo06GaSelector());
        layer.setId("l180-mt-isa-deep-crus-seis-surv-qld-2006-stac-and-migr-data-and-imag-for-line-06ga-to-06ga");
        layer.setName(
                "L180 Mt Isa Deep Crustal Seismic Survey Qld 2006 Stacked And Migrated Data And Images For Lines 06Ga M1 To 06Ga M6");
        layer.setGroup("Geoscience Australia");
        layer.setDescription("Processed seismic data (SEG-Y format) and TIFF images for the 2006 Mt Isa Deep C");
        layer.setOrder("Registered_14");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeAreTherAnySandUranSystInTheEromBasiSelector() {
        CSWRecordSelector cswRecord = new CSWRecordSelector();
        cswRecord.setRecordId("a05f7892-f9c4-7506-e044-00144fdd4fa6");
        return cswRecord;
    }

    @Bean
    public KnownLayer knownTypeAreTherAnySandUranSystInTheEromBasi() {
        KnownLayer layer = new KnownLayer("are-ther-any-sand-uran-syst-in-the-erom-basi",
                knownTypeAreTherAnySandUranSystInTheEromBasiSelector());
        layer.setId("are-ther-any-sand-uran-syst-in-the-erom-basi");
        layer.setName("Are There Any Sandstone Hosted Uranium Systems In The Eromanga Basin ");
        layer.setGroup("Geoscience Australia");
        layer.setDescription("As part of Geoscience Australia's Onshore Energy Security Program the authors ha");
        layer.setOrder("Registered_15");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeL164CurnSeisSurvSa20032004StacAndMigrSeisDataAndImagForLine03GaSelector() {
        CSWRecordSelector cswRecord = new CSWRecordSelector();
        cswRecord.setRecordId("cd697530-5b75-3811-e044-00144fdd4fa6");
        return cswRecord;
    }

    @Bean
    public KnownLayer knownTypeL164CurnSeisSurvSa20032004StacAndMigrSeisDataAndImagForLine03Ga() {
        KnownLayer layer = new KnownLayer(
                "l164-curn-seis-surv-sa-2003-2004-stac-and-migr-seis-data-and-imag-for-line-03ga",
                knownTypeL164CurnSeisSurvSa20032004StacAndMigrSeisDataAndImagForLine03GaSelector());
        layer.setId("l164-curn-seis-surv-sa-2003-2004-stac-and-migr-seis-data-and-imag-for-line-03ga");
        layer.setName(
                "L164 Curnamona Seismic Survey Sa 2003 2004 Stacked And Migrated Seismic Data And Images For Lines 03Ga Cu1");
        layer.setGroup("Geoscience Australia");
        layer.setDescription("A seismic survey using the Australian National Seismic Imaging Resource (ANSIR) ");
        layer.setOrder("Registered_16");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeLawnHillPlatAndLeicRiveFaulTrouMeasStraSectOnliGisSelector() {
        CSWRecordSelector cswRecord = new CSWRecordSelector();
        cswRecord.setRecordId("a05f7892-b7a0-7506-e044-00144fdd4fa6");
        return cswRecord;
    }

    @Bean
    public KnownLayer knownTypeLawnHillPlatAndLeicRiveFaulTrouMeasStraSectOnliGis() {
        KnownLayer layer = new KnownLayer("lawn-hill-plat-and-leic-rive-faul-trou-meas-stra-sect-onli-gis",
                knownTypeLawnHillPlatAndLeicRiveFaulTrouMeasStraSectOnliGisSelector());
        layer.setId("lawn-hill-plat-and-leic-rive-faul-trou-meas-stra-sect-onli-gis");
        layer.setName("Lawn Hill Platform And Leichhardt River Fault Trough Measured Stratigraphic Section Online Gis");
        layer.setGroup("Geoscience Australia");
        layer.setDescription("This GIS web browser contains stratigraphic information from the southern flank ");
        layer.setOrder("Registered_17");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypePredMineDiscInTheEastYilgCratAnExamOfDistTargOfAnOrogGoldMineSystSelector() {
        CSWRecordSelector cswRecord = new CSWRecordSelector();
        cswRecord.setRecordId("a05f7892-eafd-7506-e044-00144fdd4fa6");
        return cswRecord;
    }

    @Bean
    public KnownLayer knownTypePredMineDiscInTheEastYilgCratAnExamOfDistTargOfAnOrogGoldMineSyst() {
        KnownLayer layer = new KnownLayer(
                "pred-mine-disc-in-the-east-yilg-crat-an-exam-of-dist-targ-of-an-orog-gold-mine-syst",
                knownTypePredMineDiscInTheEastYilgCratAnExamOfDistTargOfAnOrogGoldMineSystSelector());
        layer.setId("pred-mine-disc-in-the-east-yilg-crat-an-exam-of-dist-targ-of-an-orog-gold-mine-syst");
        layer.setName(
                "Predictive Mineral Discovery In The Eastern Yilgarn Craton An Example Of District Scale Targeting Of An Orogenic Gold Mineral System");
        layer.setGroup("Geoscience Australia");
        layer.setDescription("Predictive mineral discovery is concerned with the application of a whole of sys");
        layer.setOrder("Registered_18");
        return layer;
    }

    @Bean
    public CSWRecordSelector knownTypeFinaRepo3DGeolModeOfTheEastYilgCratProjPmdY2Sept2001Dece2004Selector() {
        CSWRecordSelector cswRecord = new CSWRecordSelector();
        cswRecord.setRecordId("a05f7892-ccc9-7506-e044-00144fdd4fa6");
        return cswRecord;
    }

    @Bean
    public KnownLayer knownTypeFinaRepo3DGeolModeOfTheEastYilgCratProjPmdY2Sept2001Dece2004() {
        KnownLayer layer = new KnownLayer(
                "fina-repo-3d-geol-mode-of-the-east-yilg-crat-proj-pmd-y2-sept-2001-dece-2004",
                knownTypeFinaRepo3DGeolModeOfTheEastYilgCratProjPmdY2Sept2001Dece2004Selector());
        layer.setId("fina-repo-3d-geol-mode-of-the-east-yilg-crat-proj-pmd-y2-sept-2001-dece-2004");
        layer.setName(
                "Final Report 3D Geological Models Of The Eastern Yilgarn Craton Project Pmd Crc Y2 September 2001 December 2004");
        layer.setGroup("Geoscience Australia");
        layer.setDescription("The pmd*CRC Y2 project operated for a little over three years, and in this time ");
        layer.setOrder("Registered_19");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeMineralFieldsSelector() {
        return new WMSSelector("Mineral_Fields");
    }

    @Bean
    public KnownLayer knownTypeMineralFields() {
        KnownLayer layer = new KnownLayer("mineral-fields", knownTypeMineralFieldsSelector());
        layer.setId("mineral-fields");
        layer.setName("Mineral Fields");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_22");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeHistoricalExplorationActivityPointsSelector() {
        return new WMSSelector("Historical_Exploration_Activity_-_points");
    }

    @Bean
    public KnownLayer knownTypeHistoricalExplorationActivityPoints() {
        KnownLayer layer = new KnownLayer("historical-exploration-activity-points",
                knownTypeHistoricalExplorationActivityPointsSelector());
        layer.setId("historical-exploration-activity-points");
        layer.setName("Historical Exploration Activity Points");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_23");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeReleasesSelector() {
        return new WMSSelector("Releases");
    }

    @Bean
    public KnownLayer knownTypeReleases() {
        KnownLayer layer = new KnownLayer("releases", knownTypeReleasesSelector());
        layer.setId("releases");
        layer.setName("Releases");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_24");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeSection574Selector() {
        return new WMSSelector("Section_57-4");
    }

    @Bean
    public KnownLayer knownTypeSection574() {
        KnownLayer layer = new KnownLayer("section-57-4", knownTypeSection574Selector());
        layer.setId("section-57-4");
        layer.setName("Section 57 4");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_25");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeSection19Selector() {
        return new WMSSelector("Section_19");
    }

    @Bean
    public KnownLayer knownTypeSection19() {
        KnownLayer layer = new KnownLayer("section-19", knownTypeSection19Selector());
        layer.setId("section-19");
        layer.setName("Section 19");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_26");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeMinesAndMineralDepositsMinedexSelector() {
        return new WMSSelector("Mines_and_Mineral_Deposits_-_MINEDEX");
    }

    @Bean
    public KnownLayer knownTypeMinesAndMineralDepositsMinedex() {
        KnownLayer layer = new KnownLayer("mines-and-mineral-deposits-minedex",
                knownTypeMinesAndMineralDepositsMinedexSelector());
        layer.setId("mines-and-mineral-deposits-minedex");
        layer.setName("Mines And Mineral Deposits Minedex");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("Mines and Mineral Deposits of Western Australia (MINEDEX)");
        layer.setOrder("Registered_27");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeMineralisationZonesNonS572AaSelector() {
        return new WMSSelector("Mineralisation_Zones_Non_S57-2AA");
    }

    @Bean
    public KnownLayer knownTypeMineralisationZonesNonS572Aa() {
        KnownLayer layer = new KnownLayer("mineralisation-zones-non-s57-2aa",
                knownTypeMineralisationZonesNonS572AaSelector());
        layer.setId("mineralisation-zones-non-s57-2aa");
        layer.setName("Mineralisation Zones Non S57 2Aa");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_28");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeAmalgamationsSelector() {
        return new WMSSelector("Amalgamations");
    }

    @Bean
    public KnownLayer knownTypeAmalgamations() {
        KnownLayer layer = new KnownLayer("amalgamations", knownTypeAmalgamationsSelector());
        layer.setId("amalgamations");
        layer.setName("Amalgamations");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_29");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeTenementsCurrentSelector() {
        return new WMSSelector("Tenements_Current");
    }

    @Bean
    public KnownLayer knownTypeTenementsCurrent() {
        KnownLayer layer = new KnownLayer("tenements-current", knownTypeTenementsCurrentSelector());
        layer.setId("tenements-current");
        layer.setName("Tenements Current");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("Current Mining Tenements of Western Australia");
        layer.setOrder("Registered_30");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeHistoricalExplorationActivityLinesSelector() {
        return new WMSSelector("Historical_Exploration_Activity_-_lines");
    }

    @Bean
    public KnownLayer knownTypeHistoricalExplorationActivityLines() {
        KnownLayer layer = new KnownLayer("historical-exploration-activity-lines",
                knownTypeHistoricalExplorationActivityLinesSelector());
        layer.setId("historical-exploration-activity-lines");
        layer.setName("Historical Exploration Activity Lines");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_31");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeRestorationsSelector() {
        return new WMSSelector("Restorations");
    }

    @Bean
    public KnownLayer knownTypeRestorations() {
        KnownLayer layer = new KnownLayer("restorations", knownTypeRestorationsSelector());
        layer.setId("restorations");
        layer.setName("Restorations");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_32");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeHistSelector() {
        return new WMSSelector("Historical_Exploration_Activity_-_polygons");
    }

    @Bean
    public KnownLayer knownTypeHist() {
        KnownLayer layer = new KnownLayer("hist", knownTypeHistSelector());
        layer.setId("hist");
        layer.setName("Historical Exploration Activity Polygons");
        layer.setGroup("WA Department of Mines and Petroleum");
        layer.setDescription("");
        layer.setOrder("Registered_33");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeRsSampleSelector() {
        return new WMSSelector("test:rs_sample");
    }

    @Bean
    public KnownLayer knownTypeRsSample() {
        KnownLayer layer = new KnownLayer("rs-sample", knownTypeRsSampleSelector());
        layer.setId("rs-sample");
        layer.setName("Rs Sample");
        layer.setGroup("Geological Survey of New South Wales");
        layer.setDescription("");
        layer.setOrder("Registered_34");
        return layer;
    }

    @Bean
    public WFSSelector knownTypeBoreholeSelector() {
        return new WFSSelector("gsmlbh:Borehole");
    }

    @Bean
    public KnownLayer knownTypeBorehole() {
        KnownLayer layer = new KnownLayer("borehole", knownTypeBoreholeSelector());
        layer.setId("borehole");
        layer.setName("Borehole");
        layer.setGroup("CSIRO");
        layer.setDescription("Boreholes submitted to CSIRO by industry and government organisations for analys");
        layer.setOrder("Registered_35");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeHighpSiteIronlayerSelector() {
        return new WMSSelector("HighP-Site-IronLayer");
    }

    @Bean
    public KnownLayer knownTypeHighpSiteIronlayer() {
        KnownLayer layer = new KnownLayer("highp-site-ironlayer", knownTypeHighpSiteIronlayerSelector());
        layer.setId("highp-site-ironlayer");
        layer.setName("Highp Site Ironlayer");
        layer.setGroup("CSIRO");
        layer.setDescription("Layer-Group type layer: HighP-Site-IronLayer");
        layer.setOrder("Registered_36");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeHighpfeaturetypeSelector() {
        return new WMSSelector("highp:HighPFeatureType");
    }

    @Bean
    public KnownLayer knownTypeHighpfeaturetype() {
        KnownLayer layer = new KnownLayer("highpfeaturetype", knownTypeHighpfeaturetypeSelector());
        layer.setId("highpfeaturetype");
        layer.setName("Highpfeaturetype");
        layer.setGroup("CSIRO");
        layer.setDescription("");
        layer.setOrder("Registered_37");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeHighpSiteFeatureTypeSelector() {
        return new WMSSelector("highp:HighPSiteFeatureType");
    }

    @Bean
    public KnownLayer knownTypeHighpSiteFeatureType() {
        KnownLayer layer = new KnownLayer("highp-site-feature-type", knownTypeHighpSiteFeatureTypeSelector());
        layer.setId("highp-site-feature-type");
        layer.setName("Highp Site Feature Type");
        layer.setGroup("CSIRO");
        layer.setDescription("Generated from highp");
        layer.setOrder("Registered_38");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeHighpSitePhoslayerSelector() {
        return new WMSSelector("HighP-Site-PhosLayer");
    }

    @Bean
    public KnownLayer knownTypeHighpSitePhoslayer() {
        KnownLayer layer = new KnownLayer("highp-site-phoslayer", knownTypeHighpSitePhoslayerSelector());
        layer.setId("highp-site-phoslayer");
        layer.setName("Highp Site Phoslayer");
        layer.setGroup("CSIRO");
        layer.setDescription("Layer-Group type layer: HighP-Site-PhosLayer");
        layer.setOrder("Registered_39");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeHighpRePhoslayerSelector() {
        return new WMSSelector("HighP-RE-PhosLayer");
    }

    @Bean
    public KnownLayer knownTypeHighpRePhoslayer() {
        KnownLayer layer = new KnownLayer("highp-re-phoslayer", knownTypeHighpRePhoslayerSelector());
        layer.setId("highp-re-phoslayer");
        layer.setName("Highp Re Phoslayer");
        layer.setGroup("CSIRO");
        layer.setDescription("Layer-Group type layer: HighP-RE-PhosLayer");
        layer.setOrder("Registered_41");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeHighpReIronlayerSelector() {
        return new WMSSelector("HighP-RE-IronLayer");
    }

    @Bean
    public KnownLayer knownTypeHighpReIronlayer() {
        KnownLayer layer = new KnownLayer("highp-re-ironlayer", knownTypeHighpReIronlayerSelector());
        layer.setId("highp-re-ironlayer");
        layer.setName("Highp Re Ironlayer");
        layer.setGroup("CSIRO");
        layer.setDescription("Layer-Group type layer: HighP-RE-IronLayer");
        layer.setOrder("Registered_42");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeCate3ExplLicePolyOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:LicenceCategory3");
    }

    @Bean
    public KnownLayer knownTypeCate3ExplLicePolyOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("cate-3-expl-lice-poly-of-tasm-min-reso-tasm",
                knownTypeCate3ExplLicePolyOfTasmMinResoTasmSelector());
        layer.setId("cate-3-expl-lice-poly-of-tasm-min-reso-tasm");
        layer.setName("Category 3 Exploration Licence Polygons Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Category 3 Exploration Licence polygons and Exploration Release Areas across Tas");
        layer.setOrder("Registered_43");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeCate1ExplLicePolyOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:LicenceCategory1");
    }

    @Bean
    public KnownLayer knownTypeCate1ExplLicePolyOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("cate-1-expl-lice-poly-of-tasm-min-reso-tasm",
                knownTypeCate1ExplLicePolyOfTasmMinResoTasmSelector());
        layer.setId("cate-1-expl-lice-poly-of-tasm-min-reso-tasm");
        layer.setName("Category 1 Exploration Licence Polygons Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Category 1 Exploration Licence polygons and Exploration Release Areas across Tas");
        layer.setOrder("Registered_44");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeProcLandAreaOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:ProclaimedAreasPoly");
    }

    @Bean
    public KnownLayer knownTypeProcLandAreaOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("proc-land-area-of-tasm-min-reso-tasm",
                knownTypeProcLandAreaOfTasmMinResoTasmSelector());
        layer.setId("proc-land-area-of-tasm-min-reso-tasm");
        layer.setName("Proclaimed Landslip Areas Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Proclaimed Landslip Areas (A and B) of Tasmania, which are defined under the Min");
        layer.setOrder("Registered_45");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeAirbGeopSurvOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:AirborneSurveys");
    }

    @Bean
    public KnownLayer knownTypeAirbGeopSurvOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("airb-geop-surv-of-tasm-min-reso-tasm",
                knownTypeAirbGeopSurvOfTasmMinResoTasmSelector());
        layer.setId("airb-geop-surv-of-tasm-min-reso-tasm");
        layer.setName("Airborne Geophysical Surveys Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Spatial index of open file airborne geophysical surveys for which digital data i");
        layer.setOrder("Registered_46");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeLandPoinOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:LandSlidePoint");
    }

    @Bean
    public KnownLayer knownTypeLandPoinOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("land-poin-of-tasm-min-reso-tasm",
                knownTypeLandPoinOfTasmMinResoTasmSelector());
        layer.setId("land-poin-of-tasm-min-reso-tasm");
        layer.setName("Landslide Points Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Landslide features across Tasmania as representative points, with summary landsl");
        layer.setOrder("Registered_47");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeMineOccuPoinOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:MineralOccurences");
    }

    @Bean
    public KnownLayer knownTypeMineOccuPoinOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("mine-occu-poin-of-tasm-min-reso-tasm",
                knownTypeMineOccuPoinOfTasmMinResoTasmSelector());
        layer.setId("mine-occu-poin-of-tasm-min-reso-tasm");
        layer.setName("Mineral Occurrence Points Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Mineral occurrences, including operating and abandoned mines, located in Tasmani");
        layer.setOrder("Registered_48");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeLandLineOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:LandSlideLine");
    }

    @Bean
    public KnownLayer knownTypeLandLineOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("land-line-of-tasm-min-reso-tasm",
                knownTypeLandLineOfTasmMinResoTasmSelector());
        layer.setId("land-line-of-tasm-min-reso-tasm");
        layer.setName("Landslide Lines Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Linear landslide components of landslide features mapped across Tasmania, with s");
        layer.setOrder("Registered_49");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeLandDamaPolyOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:DamagePoly");
    }

    @Bean
    public KnownLayer knownTypeLandDamaPolyOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("land-dama-poly-of-tasm-min-reso-tasm",
                knownTypeLandDamaPolyOfTasmMinResoTasmSelector());
        layer.setId("land-dama-poly-of-tasm-min-reso-tasm");
        layer.setName("Landslide Damage Polygons Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Polygons of structures or property known to be damaged by a landslide in Tasmani");
        layer.setOrder("Registered_50");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeCate4ExplLicePolyOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:LicenceCategory4");
    }

    @Bean
    public KnownLayer knownTypeCate4ExplLicePolyOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("cate-4-expl-lice-poly-of-tasm-min-reso-tasm",
                knownTypeCate4ExplLicePolyOfTasmMinResoTasmSelector());
        layer.setId("cate-4-expl-lice-poly-of-tasm-min-reso-tasm");
        layer.setName("Category 4 Exploration Licence Polygons Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Category 4 Exploration Licence polygons and Exploration Release Areas across Tas");
        layer.setOrder("Registered_51");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeCate6ExplLicePolyOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:LicenceCategory6");
    }

    @Bean
    public KnownLayer knownTypeCate6ExplLicePolyOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("cate-6-expl-lice-poly-of-tasm-min-reso-tasm",
                knownTypeCate6ExplLicePolyOfTasmMinResoTasmSelector());
        layer.setId("cate-6-expl-lice-poly-of-tasm-min-reso-tasm");
        layer.setName("Category 6 Exploration Licence Polygons Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Category 6 Exploration Licence polygons and Exploration Release Areas across Tas");
        layer.setOrder("Registered_52");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeMiniLeasPolyOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:Leases");
    }

    @Bean
    public KnownLayer knownTypeMiniLeasPolyOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("mini-leas-poly-of-tasm-min-reso-tasm",
                knownTypeMiniLeasPolyOfTasmMinResoTasmSelector());
        layer.setId("mini-leas-poly-of-tasm-min-reso-tasm");
        layer.setName("Mining Lease Polygons Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Mining Lease polygons and production licence polygons for all mineral categories");
        layer.setOrder("Registered_53");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeCate2ExplLicePolyOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:LicenceCategory2");
    }

    @Bean
    public KnownLayer knownTypeCate2ExplLicePolyOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("cate-2-expl-lice-poly-of-tasm-min-reso-tasm",
                knownTypeCate2ExplLicePolyOfTasmMinResoTasmSelector());
        layer.setId("cate-2-expl-lice-poly-of-tasm-min-reso-tasm");
        layer.setName("Category 2 Exploration Licence Polygons Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Category 2 Exploration Licence polygons and Exploration Release Areas across Tas");
        layer.setOrder("Registered_54");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGravMeasOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:GravityMeasurements");
    }

    @Bean
    public KnownLayer knownTypeGravMeasOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("grav-meas-of-tasm-min-reso-tasm",
                knownTypeGravMeasOfTasmMinResoTasmSelector());
        layer.setId("grav-meas-of-tasm-min-reso-tasm");
        layer.setName("Gravity Measurements Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Primary and derived (i.e. Bouguer anomaly) gravity observation points (stations)");
        layer.setOrder("Registered_55");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeCate5ExplLicePolyOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:LicenceCategory5");
    }

    @Bean
    public KnownLayer knownTypeCate5ExplLicePolyOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("cate-5-expl-lice-poly-of-tasm-min-reso-tasm",
                knownTypeCate5ExplLicePolyOfTasmMinResoTasmSelector());
        layer.setId("cate-5-expl-lice-poly-of-tasm-min-reso-tasm");
        layer.setName("Category 5 Exploration Licence Polygons Of Tasmania Mineral Resources Tasmania");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Category 5 Exploration Licence polygons and Exploration Release Areas across Tas");
        layer.setOrder("Registered_56");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeLandDamaPoinOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:DamagePoint");
    }

    @Bean
    public KnownLayer knownTypeLandDamaPoinOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("land-dama-poin-of-tasm-min-reso-tasm",
                knownTypeLandDamaPoinOfTasmMinResoTasmSelector());
        layer.setId("land-dama-poin-of-tasm-min-reso-tasm");
        layer.setName("Landslide Damage Points Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Point locations of known damage to structures or property caused by a landslide ");
        layer.setOrder("Registered_57");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGravBaseStatOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:GravityBaseStations");
    }

    @Bean
    public KnownLayer knownTypeGravBaseStatOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("grav-base-stat-of-tasm-min-reso-tasm",
                knownTypeGravBaseStatOfTasmMinResoTasmSelector());
        layer.setId("grav-base-stat-of-tasm-min-reso-tasm");
        layer.setName("Gravity Base Stations Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Precise locations where the absolute value of gravity is known.");
        layer.setOrder("Registered_58");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeBorePoinOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:Boreholes");
    }

    @Bean
    public KnownLayer knownTypeBorePoinOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("bore-poin-of-tasm-min-reso-tasm",
                knownTypeBorePoinOfTasmMinResoTasmSelector());
        layer.setId("bore-poin-of-tasm-min-reso-tasm");
        layer.setName("Borehole Points Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Borehole features across Tasmania derived from the Borehole Database, administer");
        layer.setOrder("Registered_59");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeStrategicProspectivityZonesSelector() {
        return new WMSSelector("mrtwfs:StrategicProspectivityZones");
    }

    @Bean
    public KnownLayer knownTypeStrategicProspectivityZones() {
        KnownLayer layer = new KnownLayer("strategic-prospectivity-zones",
                knownTypeStrategicProspectivityZonesSelector());
        layer.setId("strategic-prospectivity-zones");
        layer.setName("Strategic Prospectivity Zones");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("");
        layer.setOrder("Registered_60");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeLandPolyOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:LandSlidePoly");
    }

    @Bean
    public KnownLayer knownTypeLandPolyOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("land-poly-of-tasm-min-reso-tasm",
                knownTypeLandPolyOfTasmMinResoTasmSelector());
        layer.setId("land-poly-of-tasm-min-reso-tasm");
        layer.setName("Landslide Polygons Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("Landslide component polygons of landslide features mapped across Tasmania, with ");
        layer.setOrder("Registered_61");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeBoreTracOfTasmMinResoTasmSelector() {
        return new WMSSelector("mrtwfs:BoreholeTrace");
    }

    @Bean
    public KnownLayer knownTypeBoreTracOfTasmMinResoTasm() {
        KnownLayer layer = new KnownLayer("bore-trac-of-tasm-min-reso-tasm",
                knownTypeBoreTracOfTasmMinResoTasmSelector());
        layer.setId("bore-trac-of-tasm-min-reso-tasm");
        layer.setName("Borehole Traces Of Tasmania Mineral Resources Tasmania ");
        layer.setGroup("Mineral Resources Tasmania");
        layer.setDescription("");
        layer.setOrder("Registered_62");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeRadnlOperR25PcprrL3KnmiSelector() {
        return new WMSSelector("RADNL_OPER_R___25PCPRR_L3_KNMI");
    }

    @Bean
    public KnownLayer knownTypeRadnlOperR25PcprrL3Knmi() {
        KnownLayer layer = new KnownLayer("radnl-oper-r-25pcprr-l3-knmi", knownTypeRadnlOperR25PcprrL3KnmiSelector());
        layer.setId("radnl-oper-r-25pcprr-l3-knmi");
        layer.setName("Radnl Oper R 25Pcprr L3 Knmi");
        layer.setGroup("Unknown");
        layer.setDescription("");
        layer.setOrder("Registered_66");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeRadnlOperR25PcprrL3ColorSelector() {
        return new WMSSelector("RADNL_OPER_R___25PCPRR_L3_COLOR");
    }

    @Bean
    public KnownLayer knownTypeRadnlOperR25PcprrL3Color() {
        KnownLayer layer = new KnownLayer("radnl-oper-r-25pcprr-l3-color", knownTypeRadnlOperR25PcprrL3ColorSelector());
        layer.setId("radnl-oper-r-25pcprr-l3-color");
        layer.setName("Radnl Oper R 25Pcprr L3 Color");
        layer.setGroup("Unknown");
        layer.setDescription("");
        layer.setOrder("Registered_67");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswDrillholeSelector() {
        return new WMSSelector("gsnsw:dw_drillhole");
    }

    @Bean
    public KnownLayer knownTypeNswDrillhole() {
        KnownLayer layer = new KnownLayer("nsw-drillhole", knownTypeNswDrillholeSelector());
        layer.setId("nsw-drillhole");
        layer.setName("Nsw Drillhole");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This is the full NSW drilling dataset available from Geoscientific Data Warehous");
        layer.setOrder("Registered_68");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswAssaySurfaceSelector() {
        return new WMSSelector("gsnsw:dw_surfassay");
    }

    @Bean
    public KnownLayer knownTypeNswAssaySurface() {
        KnownLayer layer = new KnownLayer("nsw-assay-surface", knownTypeNswAssaySurfaceSelector());
        layer.setId("nsw-assay-surface");
        layer.setName("Nsw Assay Surface");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This dataset contains geochemical assay data collected by companies exploring in");
        layer.setOrder("Registered_69");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswFieldObservationsSelector() {
        return new WMSSelector("gsnsw:dw_fieldobs_full");
    }

    @Bean
    public KnownLayer knownTypeNswFieldObservations() {
        KnownLayer layer = new KnownLayer("nsw-field-observations", knownTypeNswFieldObservationsSelector());
        layer.setId("nsw-field-observations");
        layer.setName("Nsw Field Observations");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_70");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswDownholeAssaySamplesSelector() {
        return new WMSSelector("gsnsw:dw_assayhole");
    }

    @Bean
    public KnownLayer knownTypeNswDownholeAssaySamples() {
        KnownLayer layer = new KnownLayer("nsw-downhole-assay-samples", knownTypeNswDownholeAssaySamplesSelector());
        layer.setId("nsw-downhole-assay-samples");
        layer.setName("Nsw Downhole Assay Samples");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_71");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswDrillholeAllSelector() {
        return new WMSSelector("gsnsw:dw_drillhole_full");
    }

    @Bean
    public KnownLayer knownTypeNswDrillholeAll() {
        KnownLayer layer = new KnownLayer("nsw-drillhole-all", knownTypeNswDrillholeAllSelector());
        layer.setId("nsw-drillhole-all");
        layer.setName("Nsw Drillhole All");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_72");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswDrillholesCoalSelector() {
        return new WMSSelector("gsnsw:dw_drillhole_full_coal");
    }

    @Bean
    public KnownLayer knownTypeNswDrillholesCoal() {
        KnownLayer layer = new KnownLayer("nsw-drillholes-coal", knownTypeNswDrillholesCoalSelector());
        layer.setId("nsw-drillholes-coal");
        layer.setName("Nsw Drillholes Coal");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This is the coal subset of the NSW drilling dataset available from Geoscientific");
        layer.setOrder("Registered_73");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswGeologicalFieldObservationsSelector() {
        return new WMSSelector("gsnsw:dw_fieldobs");
    }

    @Bean
    public KnownLayer knownTypeNswGeologicalFieldObservations() {
        KnownLayer layer = new KnownLayer("nsw-geological-field-observations",
                knownTypeNswGeologicalFieldObservationsSelector());
        layer.setId("nsw-geological-field-observations");
        layer.setName("Nsw Geological Field Observations");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("The Field Observations (FieldObs) database stores observations and measurements ");
        layer.setOrder("Registered_74");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswDrillholesMineralsSelector() {
        return new WMSSelector("gsnsw:dw_drillhole_full_min");
    }

    @Bean
    public KnownLayer knownTypeNswDrillholesMinerals() {
        KnownLayer layer = new KnownLayer("nsw-drillholes-minerals", knownTypeNswDrillholesMineralsSelector());
        layer.setId("nsw-drillholes-minerals");
        layer.setName("Nsw Drillholes Minerals");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This is the mineral subset of the NSW drilling dataset available from Geoscienti");
        layer.setOrder("Registered_75");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswGeologySimplifiedSelector() {
        return new WMSSelector("gsnsw:ge_geology15m");
    }

    @Bean
    public KnownLayer knownTypeNswGeologySimplified() {
        KnownLayer layer = new KnownLayer("nsw-geology-simplified", knownTypeNswGeologySimplifiedSelector());
        layer.setId("nsw-geology-simplified");
        layer.setName("Nsw Geology Simplified");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_76");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswDrillholesPetroleumSelector() {
        return new WMSSelector("gsnsw:dw_drillhole_full_pet");
    }

    @Bean
    public KnownLayer knownTypeNswDrillholesPetroleum() {
        KnownLayer layer = new KnownLayer("nsw-drillholes-petroleum", knownTypeNswDrillholesPetroleumSelector());
        layer.setId("nsw-drillholes-petroleum");
        layer.setName("Nsw Drillholes Petroleum");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("The petroleum drillholes dataset stores information about conventional petroleum");
        layer.setOrder("Registered_77");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeMineralOccurenceIndustryFullSelector() {
        return new WMSSelector("gsnsw:dw_mineraloccurrence_full");
    }

    @Bean
    public KnownLayer knownTypeMineralOccurenceIndustryFull() {
        KnownLayer layer = new KnownLayer("mineral-occurence-industry-full",
                knownTypeMineralOccurenceIndustryFullSelector());
        layer.setId("mineral-occurence-industry-full");
        layer.setName("Mineral Occurence Industry Full");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_78");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswHistoricExplorationTitlesSelector() {
        return new WMSSelector("gsnsw:bl_histels");
    }

    @Bean
    public KnownLayer knownTypeNswHistoricExplorationTitles() {
        KnownLayer layer = new KnownLayer("nsw-historic-exploration-titles",
                knownTypeNswHistoricExplorationTitlesSelector());
        layer.setId("nsw-historic-exploration-titles");
        layer.setName("Nsw Historic Exploration Titles");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_79");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswMapBlockGraticuleSelector() {
        return new WMSSelector("gsnsw:bl_mapblock");
    }

    @Bean
    public KnownLayer knownTypeNswMapBlockGraticule() {
        KnownLayer layer = new KnownLayer("nsw-map-block-graticule", knownTypeNswMapBlockGraticuleSelector());
        layer.setId("nsw-map-block-graticule");
        layer.setName("Nsw Map Block Graticule");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_80");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeBlLocalaboriginallandcouncilSelector() {
        return new WMSSelector("gsnsw:bl_localaboriginallandcouncil");
    }

    @Bean
    public KnownLayer knownTypeBlLocalaboriginallandcouncil() {
        KnownLayer layer = new KnownLayer("bl-localaboriginallandcouncil",
                knownTypeBlLocalaboriginallandcouncilSelector());
        layer.setId("bl-localaboriginallandcouncil");
        layer.setName("Bl Localaboriginallandcouncil");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_81");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswMineralOccurrenceIndustrySelector() {
        return new WMSSelector("gsnsw:dw_metindustry");
    }

    @Bean
    public KnownLayer knownTypeNswMineralOccurrenceIndustry() {
        KnownLayer layer = new KnownLayer("nsw-mineral-occurrence-industry",
                knownTypeNswMineralOccurrenceIndustrySelector());
        layer.setId("nsw-mineral-occurrence-industry");
        layer.setName("Nsw Mineral Occurrence Industry");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_82");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNsw100KMapSheetExtentsSelector() {
        return new WMSSelector("gsnsw:bl_mapsheet100k");
    }

    @Bean
    public KnownLayer knownTypeNsw100KMapSheetExtents() {
        KnownLayer layer = new KnownLayer("nsw-100k-map-sheet-extents", knownTypeNsw100KMapSheetExtentsSelector());
        layer.setId("nsw-100k-map-sheet-extents");
        layer.setName("Nsw 100K Map Sheet Extents");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_83");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswFossickingDistrictsSelector() {
        return new WMSSelector("gsnsw:bl_fossicking");
    }

    @Bean
    public KnownLayer knownTypeNswFossickingDistricts() {
        KnownLayer layer = new KnownLayer("nsw-fossicking-districts", knownTypeNswFossickingDistrictsSelector());
        layer.setId("nsw-fossicking-districts");
        layer.setName("Nsw Fossicking Districts");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("Fossicking is the small scale search for and collection of minerals, gemstones o");
        layer.setOrder("Registered_84");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswGeologicalFieldObservationsPhotoSelector() {
        return new WMSSelector("gsnsw:dw_photo");
    }

    @Bean
    public KnownLayer knownTypeNswGeologicalFieldObservationsPhoto() {
        KnownLayer layer = new KnownLayer("nsw-geological-field-observations-photo",
                knownTypeNswGeologicalFieldObservationsPhotoSelector());
        layer.setId("nsw-geological-field-observations-photo");
        layer.setName("Nsw Geological Field Observations Photo");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_85");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGeopTotaMagnInteRtpTmiRtpTiltFiltSelector() {
        return new WMSSelector("geophys:MagRTPtilt");
    }

    @Bean
    public KnownLayer knownTypeGeopTotaMagnInteRtpTmiRtpTiltFilt() {
        KnownLayer layer = new KnownLayer("geop-tota-magn-inte-rtp-tmi-rtp-tilt-filt",
                knownTypeGeopTotaMagnInteRtpTmiRtpTiltFiltSelector());
        layer.setId("geop-tota-magn-inte-rtp-tmi-rtp-tilt-filt");
        layer.setName("Geophysics Total Magnetic Intensity Rtp Tmi Rtp Tilt Filter");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This dataset is part of the Geological Survey NSW Geophysics dataset series.  To");
        layer.setOrder("Registered_86");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswSeismicSelector() {
        return new WMSSelector("gsnsw:dw_seismic");
    }

    @Bean
    public KnownLayer knownTypeNswSeismic() {
        KnownLayer layer = new KnownLayer("nsw-seismic", knownTypeNswSeismicSelector());
        layer.setId("nsw-seismic");
        layer.setName("Nsw Seismic");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_87");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswCurrMiniAndExplTitlSelector() {
        return new WMSSelector("gsnsw:bl_title");
    }

    @Bean
    public KnownLayer knownTypeNswCurrMiniAndExplTitl() {
        KnownLayer layer = new KnownLayer("nsw-curr-mini-and-expl-titl", knownTypeNswCurrMiniAndExplTitlSelector());
        layer.setId("nsw-curr-mini-and-expl-titl");
        layer.setName("Nsw Current Mining And Exploration Titles");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_88");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGeopTotaMagnInte1StDeriReduToPoleSelector() {
        return new WMSSelector("geophys:TMI_RTP_1st");
    }

    @Bean
    public KnownLayer knownTypeGeopTotaMagnInte1StDeriReduToPole() {
        KnownLayer layer = new KnownLayer("geop-tota-magn-inte-1st-deri-redu-to-pole",
                knownTypeGeopTotaMagnInte1StDeriReduToPoleSelector());
        layer.setId("geop-tota-magn-inte-1st-deri-redu-to-pole");
        layer.setName("Geophysics Total Magnetic Intensity 1St Derivative Reduced To Pole");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_89");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswGeologicalSpectralSamplesSelector() {
        return new WMSSelector("gsnsw:dw_spectral");
    }

    @Bean
    public KnownLayer knownTypeNswGeologicalSpectralSamples() {
        KnownLayer layer = new KnownLayer("nsw-geological-spectral-samples",
                knownTypeNswGeologicalSpectralSamplesSelector());
        layer.setId("nsw-geological-spectral-samples");
        layer.setName("Nsw Geological Spectral Samples");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_90");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGeopTernRadiPotaSelector() {
        return new WMSSelector("geophys:Radio");
    }

    @Bean
    public KnownLayer knownTypeGeopTernRadiPota() {
        KnownLayer layer = new KnownLayer("geop-tern-radi-pota", knownTypeGeopTernRadiPotaSelector());
        layer.setId("geop-tern-radi-pota");
        layer.setName("Geophysics Ternary Radioelement Potassium K Thorium Th Uranium U ");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This dataset is part of the Geological Survey NSW Geophysics dataset series.  Te");
        layer.setOrder("Registered_91");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswDrillholesCsgSelector() {
        return new WMSSelector("gsnsw:dw_drillhole_full_csg");
    }

    @Bean
    public KnownLayer knownTypeNswDrillholesCsg() {
        KnownLayer layer = new KnownLayer("nsw-drillholes-csg", knownTypeNswDrillholesCsgSelector());
        layer.setId("nsw-drillholes-csg");
        layer.setName("Nsw Drillholes Csg");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("The coal seam gas drillholes dataset stores information about CSG sites within N");
        layer.setOrder("Registered_92");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswCurrentMiningApplicationsSelector() {
        return new WMSSelector("gsnsw:bl_titleappl");
    }

    @Bean
    public KnownLayer knownTypeNswCurrentMiningApplications() {
        KnownLayer layer = new KnownLayer("nsw-current-mining-applications",
                knownTypeNswCurrentMiningApplicationsSelector());
        layer.setId("nsw-current-mining-applications");
        layer.setName("Nsw Current Mining Applications");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_93");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswOperatingMineralMinesSelector() {
        return new WMSSelector("gsnsw:dw_opmines");
    }

    @Bean
    public KnownLayer knownTypeNswOperatingMineralMines() {
        KnownLayer layer = new KnownLayer("nsw-operating-mineral-mines", knownTypeNswOperatingMineralMinesSelector());
        layer.setId("nsw-operating-mineral-mines");
        layer.setName("Nsw Operating Mineral Mines");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_94");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGeophysicsElevationSelector() {
        return new WMSSelector("geophys:DEM");
    }

    @Bean
    public KnownLayer knownTypeGeophysicsElevation() {
        KnownLayer layer = new KnownLayer("geophysics-elevation", knownTypeGeophysicsElevationSelector());
        layer.setId("geophysics-elevation");
        layer.setName("Geophysics Elevation");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("Elevation is a pseudocolour layer with a histogram-equalised colour-stretch. Coo");
        layer.setOrder("Registered_95");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswGeolSimpRockUnitBounSelector() {
        return new WMSSelector("gsnsw:ge_geology15m_bdy");
    }

    @Bean
    public KnownLayer knownTypeNswGeolSimpRockUnitBoun() {
        KnownLayer layer = new KnownLayer("nsw-geol-simp-rock-unit-boun", knownTypeNswGeolSimpRockUnitBounSelector());
        layer.setId("nsw-geol-simp-rock-unit-boun");
        layer.setName("Nsw Geology Simplified Rock Unit Boundary");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_96");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeBlocksAndUnitsGraticuleSelector() {
        return new WMSSelector("gsnsw:Map Blocks and Units");
    }

    @Bean
    public KnownLayer knownTypeBlocksAndUnitsGraticule() {
        KnownLayer layer = new KnownLayer("blocks-and-units-graticule", knownTypeBlocksAndUnitsGraticuleSelector());
        layer.setId("blocks-and-units-graticule");
        layer.setName("Blocks And Units Graticule");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("Layer-Group type layer: gsnsw:Map Blocks and Units");
        layer.setOrder("Registered_97");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswMapUnitGraticuleSelector() {
        return new WMSSelector("gsnsw:bl_mapunit");
    }

    @Bean
    public KnownLayer knownTypeNswMapUnitGraticule() {
        KnownLayer layer = new KnownLayer("nsw-map-unit-graticule", knownTypeNswMapUnitGraticuleSelector());
        layer.setId("nsw-map-unit-graticule");
        layer.setName("Nsw Map Unit Graticule");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_98");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswGeochronologySelector() {
        return new WMSSelector("gsnsw:dw_geochron");
    }

    @Bean
    public KnownLayer knownTypeNswGeochronology() {
        KnownLayer layer = new KnownLayer("nsw-geochronology", knownTypeNswGeochronologySelector());
        layer.setId("nsw-geochronology");
        layer.setName("Nsw Geochronology");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("The radiogenic isotope database contains geochronological data managed by the Ge");
        layer.setOrder("Registered_99");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGeophysicsIsostaticBougerGravitySelector() {
        return new WMSSelector("geophys:IsoGrav");
    }

    @Bean
    public KnownLayer knownTypeGeophysicsIsostaticBougerGravity() {
        KnownLayer layer = new KnownLayer("geophysics-isostatic-bouger-gravity",
                knownTypeGeophysicsIsostaticBougerGravitySelector());
        layer.setId("geophysics-isostatic-bouger-gravity");
        layer.setName("Geophysics Isostatic Bouger Gravity");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This dataset is part of the Geological Survey NSW Geophysics dataset series.  Is");
        layer.setOrder("Registered_100");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGeopTotaMagnInteReduToPoleSelector() {
        return new WMSSelector("geophys:MagRTP");
    }

    @Bean
    public KnownLayer knownTypeGeopTotaMagnInteReduToPole() {
        KnownLayer layer = new KnownLayer("geop-tota-magn-inte-redu-to-pole",
                knownTypeGeopTotaMagnInteReduToPoleSelector());
        layer.setId("geop-tota-magn-inte-redu-to-pole");
        layer.setName("Geophysics Total Magnetic Intensity Reduced To Pole");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This dataset is part of the Geological Survey NSW Geophysics dataset series.  To");
        layer.setOrder("Registered_101");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswCoreLibrarySamplesSelector() {
        return new WMSSelector("gsnsw:dw_corelibhole");
    }

    @Bean
    public KnownLayer knownTypeNswCoreLibrarySamples() {
        KnownLayer layer = new KnownLayer("nsw-core-library-samples", knownTypeNswCoreLibrarySamplesSelector());
        layer.setId("nsw-core-library-samples");
        layer.setName("Nsw Core Library Samples");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_102");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswGeochemistrySamplesSelector() {
        return new WMSSelector("gsnsw:dw_geochemistry");
    }

    @Bean
    public KnownLayer knownTypeNswGeochemistrySamples() {
        KnownLayer layer = new KnownLayer("nsw-geochemistry-samples", knownTypeNswGeochemistrySamplesSelector());
        layer.setId("nsw-geochemistry-samples");
        layer.setName("Nsw Geochemistry Samples");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("The Geochemistry (Whole Rock) dataset contains information about the chemistry o");
        layer.setOrder("Registered_103");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswTitlesSelector() {
        return new WMSSelector("gsnsw:Titles");
    }

    @Bean
    public KnownLayer knownTypeNswTitles() {
        KnownLayer layer = new KnownLayer("nsw-titles", knownTypeNswTitlesSelector());
        layer.setId("nsw-titles");
        layer.setName("Nsw Titles");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("Layer-Group type layer: gsnsw:Titles");
        layer.setOrder("Registered_104");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswGeologySelector() {
        return new WMSSelector("gsnsw:NSW_Geology");
    }

    @Bean
    public KnownLayer knownTypeNswGeology() {
        KnownLayer layer = new KnownLayer("nsw-geology", knownTypeNswGeologySelector());
        layer.setId("nsw-geology");
        layer.setName("Nsw Geology");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("Layer-Group type layer: gsnsw:NSW_Geology");
        layer.setOrder("Registered_105");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeGeopIsosGravOverTmiRtpTiltSelector() {
        return new WMSSelector("geophys:IsoGravTilt");
    }

    @Bean
    public KnownLayer knownTypeGeopIsosGravOverTmiRtpTilt() {
        KnownLayer layer = new KnownLayer("geop-isos-grav-over-tmi-rtp-tilt",
                knownTypeGeopIsosGravOverTmiRtpTiltSelector());
        layer.setId("geop-isos-grav-over-tmi-rtp-tilt");
        layer.setName("Geophysics Isostatic Gravity Over Tmi Rtp Tilt");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This dataset is part of the Geological Survey NSW Geophysics dataset series.  Bo");
        layer.setOrder("Registered_106");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswLithologySamplesSelector() {
        return new WMSSelector("gsnsw:dw_lithhole");
    }

    @Bean
    public KnownLayer knownTypeNswLithologySamples() {
        KnownLayer layer = new KnownLayer("nsw-lithology-samples", knownTypeNswLithologySamplesSelector());
        layer.setId("nsw-lithology-samples");
        layer.setName("Nsw Lithology Samples");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("");
        layer.setOrder("Registered_107");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswBaseSelector() {
        return new WMSSelector("gsnsw:NSW_Base");
    }

    @Bean
    public KnownLayer knownTypeNswBase() {
        KnownLayer layer = new KnownLayer("nsw-base", knownTypeNswBaseSelector());
        layer.setId("nsw-base");
        layer.setName("Nsw Base");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("Layer-Group type layer: gsnsw:NSW_Base");
        layer.setOrder("Registered_108");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeNswMineralOccurrenceCommoditySelector() {
        return new WMSSelector("gsnsw:dw_metelement");
    }

    @Bean
    public KnownLayer knownTypeNswMineralOccurrenceCommodity() {
        KnownLayer layer = new KnownLayer("nsw-mineral-occurrence-commodity",
                knownTypeNswMineralOccurrenceCommoditySelector());
        layer.setId("nsw-mineral-occurrence-commodity");
        layer.setName("Nsw Mineral Occurrence Commodity");
        layer.setGroup("Geological Survey NSW");
        layer.setDescription("This spatial dataset is a derivative product of the New South Wales Mineral Occu");
        layer.setOrder("Registered_109");
        return layer;
    }

    @Bean
    public WMSWFSSelector knownTypeGeologicalProvincesSelector() {
        return new WMSWFSSelector("gml:ProvinceFullExtent", "GeologicalProvinces");
    }

    @Bean
    public KnownLayer knownTypeGeologicalProvinces() {
        KnownLayer layer = new KnownLayer("geological-provinces", knownTypeGeologicalProvincesSelector());
        layer.setName("Geological Provinces");
        layer.setDescription("Geological Provinces provided by GA");
        layer.setGroup("Geological Provinces");
        layer.setProxyStyleUrl("getGeologicalProvincestyle.do");
        layer.setOrder("provinces_01");

        // Optional filters
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("Name", "NAME", null, Predicate.ISLIKE);
        UIPolygonBBox uiPolygonBBox = new UIPolygonBBox("Polygon BBox", "the_geom", null, Predicate.ISEQUAL);
        optionalFilters.add(nameTextBox);
        optionalFilters.add(uiPolygonBBox);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        layer.setFilterCollection(filterCollection);
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWCrnAusBasinsSelector() {
        return new WMSSelector("be10-denude:crn_aus_basins");
    }

    @Bean
    public KnownLayer knownTypeUOWCrnAusBasins() {
        KnownLayer layer = new KnownLayer("UOW-Crn-Aus-Basins", knownTypeUOWCrnAusBasinsSelector());
        layer.setId("UOW-Crn-Aus-Basins");
        layer.setName("CRN Australia: river Basins");
        layer.setGroup("University of Wollongong");
        layer.setDescription("CRN Australia: river Basins");
        layer.setProxyStyleUrl("getDefaultPolygonStyle.do?colour=0x0000EE");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWCrnAusOutletsSelector() {
        return new WMSSelector("be10-denude:crn_aus_outlets");
    }

    @Bean
    public KnownLayer knownTypeUOWCrnAusOutlets() {
        KnownLayer layer = new KnownLayer("UOW-Crn-Aus-Outlets", knownTypeUOWCrnAusOutletsSelector());
        layer.setId("UOW-Crn-Aus-Outlets");
        layer.setName("CRN Australia: sample sites");
        layer.setGroup("University of Wollongong");
        layer.setDescription("CRN Australia: sample sites");
        layer.setProxyStyleUrl("getDefaultStyle.do?colour=0x00AAFF&layerName=be10-denude:crn_aus_outlets");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWCrnInprepBasinsSelector() {
        return new WMSSelector("be10-denude:crn_inprep_basins");
    }

    @Bean
    public KnownLayer knownTypeUOWCrnInprepBasins() {
        KnownLayer layer = new KnownLayer("UOW-Crn-Inprep-Basins", knownTypeUOWCrnInprepBasinsSelector());
        layer.setId("UOW-Crn-Inprep-Basins");
        layer.setName("CRN InPrep: river Basins");
        layer.setGroup("University of Wollongong");
        layer.setDescription("CRN InPrep: river Basins");
        layer.setProxyStyleUrl("getDefaultPolygonStyle.do?colour=0x00FFFF");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWCrnInprepOutletsSelector() {
        return new WMSSelector("be10-denude:crn_inprep_outlets");
    }

    @Bean
    public KnownLayer knownTypeUOWCrnInprepOutlets() {
        KnownLayer layer = new KnownLayer("UOW-Crn-Inprep-Outlets", knownTypeUOWCrnInprepOutletsSelector());
        layer.setId("UOW-Crn-Inprep-Outlets");
        layer.setName("CRN InPrep: sample sites");
        layer.setGroup("University of Wollongong");
        layer.setDescription("CRN InPrep: sample sites");
        layer.setProxyStyleUrl("getDefaultStyle.do?colour=0x00FFBB&layerName=be10-denude:crn_inprep_outlets");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWCrnIntBasinsSelector() {
        return new WMSSelector("be10-denude:crn_int_basins");
    }

    @Bean
    public KnownLayer knownTypeUOWCrnIntBasins() {
        KnownLayer layer = new KnownLayer("UOW-Crn-Int-Basins", knownTypeUOWCrnIntBasinsSelector());
        layer.setId("UOW-Crn-Int-Basins");
        layer.setName("CRN International: river Basins");
        layer.setGroup("University of Wollongong");
        layer.setDescription("CRN International: river Basins");
        layer.setProxyStyleUrl("getDefaultPolygonStyle.do?colour=0xBBFF00");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWCrnIntOutletsSelector() {
        return new WMSSelector("be10-denude:crn_int_outlets");
    }

    @Bean
    public KnownLayer knownTypeUOWCrnIntOutlets() {
        KnownLayer layer = new KnownLayer("UOW-Crn-Int-Outlets", knownTypeUOWCrnIntOutletsSelector());
        layer.setId("UOW-Crn-Int-Outlets");
        layer.setName("CRN International: sample sites");
        layer.setGroup("University of Wollongong");
        layer.setDescription("CRN International: sample sites");
        layer.setProxyStyleUrl("getDefaultStyle.do?colour=0xBBFFAA&layerName=be10-denude:crn_int_outlets");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWCrnXXLBasinsSelector() {
        return new WMSSelector("be10-denude:crn_xxl_basins");
    }

    @Bean
    public KnownLayer knownTypeUOWCrnXXLBasins() {
        KnownLayer layer = new KnownLayer("UOW-Crn-XXL-Basins", knownTypeUOWCrnXXLBasinsSelector());
        layer.setId("UOW-Crn-XXL-Basins");
        layer.setName("CRN XXL: river Basins");
        layer.setGroup("University of Wollongong");
        layer.setDescription("CRN XXL: river Basins");
        layer.setProxyStyleUrl("getDefaultPolygonStyle.do?colour=0xDDFF00");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWCrnXXLOutletsSelector() {
        return new WMSSelector("be10-denude:crn_xxl_outlets");
    }

    @Bean
    public KnownLayer knownTypeUOWCrnXXLOutlets() {
        KnownLayer layer = new KnownLayer("UOW-Crn-XXL-Outlets", knownTypeUOWCrnXXLOutletsSelector());
        layer.setId("UOW-Crn-XXL-Outlets");
        layer.setName("CRN XXL: sample sites");
        layer.setGroup("University of Wollongong");
        layer.setDescription("CRN XXL: sample sites");
        layer.setProxyStyleUrl("getDefaultStyle.do?colour=0xDDFFAA&layerName=be10-denude:crn_xxl_outlets");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWOSLTLBasinsSelector() {
        return new WMSSelector("be10-denude:osltl_basins");
    }

    @Bean
    public KnownLayer knownTypeUOWOSLTLBasins() {
        KnownLayer layer = new KnownLayer("UOW-OSLTL-Basins", knownTypeUOWOSLTLBasinsSelector());
        layer.setId("UOW-OSLTL-Basins");
        layer.setName("OSL & TL: river Basins");
        layer.setGroup("University of Wollongong");
        layer.setDescription("OSL & TL: river Basins");
        layer.setProxyStyleUrl("getDefaultPolygonStyle.do?colour=0xFFDD00");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public WMSSelector knownTypeUOWOSLTLOutletsSelector() {
        return new WMSSelector("be10-denude:osltl_samples");
    }

    @Bean
    public KnownLayer knownTypeUOWOSLTLOutlets() {
        KnownLayer layer = new KnownLayer("UOW-OSLTL-Outlets", knownTypeUOWOSLTLOutletsSelector());
        layer.setId("UOW-OSLTL-Outlets");
        layer.setName("OSL & TL: sample sites");
        layer.setGroup("University of Wollongong");
        layer.setDescription("OSL & TL: sample sites");
        layer.setProxyStyleUrl("getDefaultStyle.do?colour=0xFFDDAA&layerName=be10-denude:osltl_samples");
        layer.setOrder("Registered_1");
        return layer;
    }

    @Bean
    public KnownLayerSelector knownTypeIGSNSampleSelector() {
        return new WMSSelector("igsn:sample");
    }

    @Bean
    public KnownLayer knownTypeIGSNSample() {
        KnownLayer layer = new KnownLayer("igsn-sample", knownTypeIGSNSampleSelector());
        layer.setId("IGSN-CSIRO-SAMPLE");
        layer.setName("igsn-csiro-sample");
        layer.setGroup("IGSN");
        layer.setDescription("A collection of igsn-csiro-sample");
        layer.setProxyStyleUrl("doGenericFilterStyle.do?styleType=POINT&color=0xFF0000&layerName=igsn:sample");

        layer.setOrder("50");
        // Optional filters
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("IGSN Identifier", "igsn", null, Predicate.ISLIKE);
        optionalFilters.add(nameTextBox);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        layer.setFilterCollection(filterCollection);        
        return layer;
    }

    @Bean
    public KnownLayerSelector knownTypeIGSNGASampleSelector() {
        return new WMSSelector("igsn:igsn_ga_sample");
    }

    @Bean
    public KnownLayer knownTypeIGSNGASample() {
        KnownLayer layer = new KnownLayer("igsn-ga-sample", knownTypeIGSNGASampleSelector());
        layer.setId("IGSN-GA-SAMPLE");
        layer.setName("igsn-ga-sample");
        layer.setGroup("IGSN");
        layer.setDescription("A collection of igsn-ga-sample");
        layer.setProxyStyleUrl("doGenericFilterStyle.do?styleType=POINT&color=0x00FF00&layerName=igsn:igsn_ga_sample");
        layer.setOrder("51");
        // Optional filters
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("IGSN Identifier", "identifier", null, Predicate.ISLIKE);
        optionalFilters.add(nameTextBox);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        layer.setFilterCollection(filterCollection);        
        return layer;   
    }
    @Bean
    public KnownLayerSelector knownTypeIGSNANDSSampleSelector() {
        return new WMSSelector("igsn:igsn_ands_sample");
    }

    @Bean
    public KnownLayer knownTypeIGSNANDSSample() {
        KnownLayer layer = new KnownLayer("igsn-ardc-sample", knownTypeIGSNANDSSampleSelector());
        layer.setId("IGSN-ARDC-SAMPLE");
        layer.setName("igsn-ardc-sample");
        layer.setGroup("IGSN");
        layer.setDescription("A collection of igsn-ardc-sample");
        layer.setProxyStyleUrl("doGenericFilterStyle.do?styleType=POINT&color=0x0000FF&layerName=igsn:igsn_ands_sample");
        layer.setOrder("52");
        // Optional filters
        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
        UITextBox nameTextBox = new UITextBox("IGSN Identifier", "identifier", null, Predicate.ISLIKE);
        optionalFilters.add(nameTextBox);
        FilterCollection filterCollection = new FilterCollection();
        filterCollection.setOptionalFilters(optionalFilters);
        layer.setFilterCollection(filterCollection);
        return layer;
    }
    @Bean
    public KnownLayerSelector knownTypeIGSNWdcSampleSelector() {
        return new WMSSelector("igsn:igsn_wdc_sample");
    }

    @Bean
    public KnownLayer knownTypeIGSNWdcSample() {
        KnownLayer layer = new KnownLayer("igsn-sample", knownTypeIGSNWdcSampleSelector());
        layer.setId("IGSN-WDC-SAMPLE");
        layer.setName("igsn-wdc-sample");
        layer.setGroup("IGSN");
        layer.setDescription("A collection of igsn-wdc-sample");
        layer.setProxyStyleUrl("doGenericFilterStyle.do?styleType=POINT&color=0xFF00FF&layerName=igsn:igsn_wdc_sample");
        layer.setOrder("53");     
        return layer;
    }       
}

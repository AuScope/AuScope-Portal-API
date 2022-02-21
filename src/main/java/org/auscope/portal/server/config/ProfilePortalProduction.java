package org.auscope.portal.server.config;

import java.awt.Dimension;
import java.awt.Point;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.auscope.portal.core.view.knownlayer.WFSSelector;
import org.auscope.portal.core.view.knownlayer.WMSSelector;
import org.auscope.portal.core.view.knownlayer.WMSWFSSelector;
import org.auscope.portal.view.knownlayer.IRISSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.yaml.snakeyaml.Yaml;

/**
 * Definitions for all known layers
 */

@Configuration
@Profile("prod")
public class ProfilePortalProduction {

    private boolean layersLoaded = false;

    Map<String, Object> yamlLayers;

    class wfsRec {
        String selector;
        ArrayList<String> feature = new ArrayList<String>();
        ArrayList<String> endPoints = new ArrayList<String>();
    }

    wfsRec wfs = new wfsRec();

    class  selRec {
        ArrayList<Object> params  = new ArrayList<Object>();
        ArrayList<Object> options  = new ArrayList<Object>();
    }
    
    class mandRec {
        ArrayList<Object> params  = new ArrayList<Object>();
        ArrayList<Object> options  = new ArrayList<Object>();        
    }
    
    class filterCtl {
        ArrayList<Object> textbox; // = new ArrayList<Object>();
        selRec selectlist; // = new selRec();
        ArrayList<Object> checkbox; // = new ArrayList<Object>();
        ArrayList<Object> date; // = new ArrayList<Object>();
        mandRec mandatorylist;
        ArrayList<Object> mandatorytextbox;
        ArrayList<Object>  dropdownremote;
        ArrayList<Object>  polygonbox;
    }

    filterCtl filter = new filterCtl();

    private void setupIcon(KnownLayer layer) {
        Point iconAnchor = new Point(16, 32);
        layer.setIconAnchor(iconAnchor);
        Dimension iconSize = new Dimension(32, 32);
        layer.setIconSize(iconSize);
    }

    public WFSSelector knownTypeWFSSelector(wfsRec wfs) {
        WFSSelector wfsSelector = null;

        if (wfs != null) {

            if (wfs.endPoints.size() > 0) {

                String[] endPoints = new String[wfs.endPoints.size()];
                for (int i = 0; i < wfs.endPoints.size(); i++) {
                    endPoints[i] = wfs.endPoints.get(i);
                }
                wfsSelector = new WFSSelector(wfs.selector, endPoints, true);
            } else {
                wfsSelector = new WFSSelector((String) wfs.selector);
            }
            if (wfs.feature.size() > 0) {
                String[] featNameList = new String[wfs.feature.size()];
                for (int i = 0; i < wfs.feature.size(); i++) {
                    featNameList[i] = wfs.feature.get(i);
                }
                wfsSelector.setRelatedFeatureTypeNames(featNameList);                
            }
        }
        return wfsSelector;
    }

    
    public WMSWFSSelector knownTypeWMSWFSSelector(String FeatureName, String layerName) {
        return new WMSWFSSelector(FeatureName, layerName);
    }
    
    public CSWRecordSelector knownTypeCSWSelector(String keyword, String id, String serviceName) {
        CSWRecordSelector cswSelector = new CSWRecordSelector();
        if (keyword != null) cswSelector.setDescriptiveKeyword(keyword);
        if (id != null) cswSelector.setRecordId(id);
        if (serviceName != null) cswSelector.setServiceName(serviceName);
        return cswSelector;
    }
    
    public IRISSelector knownTypeIRISSelector(String networkCode, String serviceEndPoint) {
        try {
            return new IRISSelector(networkCode, serviceEndPoint);
        } catch (MalformedURLException e) {
            // TODO: Log exception??
        }
        return null;
    }
    
    public WMSSelector knownTypeWMSSelector(String LayerName, ArrayList<String> endPointsList) {
        WMSSelector f = null;
        if (endPointsList.size() > 0) {
            String[] endPoints = new String[endPointsList.size()];
            for (int i = 0; i < endPointsList.size(); i++) endPoints[i] = endPointsList.get(i);
            f = new WMSSelector(LayerName, endPoints, true);
        } else  f = new WMSSelector(LayerName);        
        
        return f;
    }
    
    private KnownLayer annotateLayer(String id) {
        KnownLayer layer = new KnownLayer(id, null);

        if (layersLoaded) {

        yamlLayers.forEach((k, v) -> {
            if (k.toString().equalsIgnoreCase(id)) {
                ((Map<String, Object>) yamlLayers.get(k)).forEach((k1, v1) -> {
                    String value = v1.toString();
                    switch (k1) {
                    case "group":
                        layer.setGroup(value);
                        break;
                    case "name":
                        layer.setName(value);
                        break;
                    case "description":
                        layer.setDescription(value);
                        break;
                    case "proxyUrl":
                        layer.setProxyUrl(value);
                        break;
                    case "proxyCountUrl":
                        layer.setProxyCountUrl(value);
                        break;
                    case "proxyStyleUrl":
                        layer.setProxyStyleUrl(value);
                        break;
                    case "proxyDownloadUrl":
                        layer.setProxyDownloadUrl(value);
                        break;
                    case "icon": {
                        String url = null;
                        Map<String, Object> x = (Map<String, Object>) v1;
                        String[] arr = {null};
                        x.forEach((sk1, sv1) -> {
                            if (sk1.startsWith("url")) {
                                arr[0] = (String) sv1;
                            }
                        });
                        url = arr[0];
                        if (url != null) { layer.setIconUrl(url); }
                        setupIcon(layer);
                        break;
                        }
                    case "order":
                        layer.setOrder(value);
                        break;
                    case "stackdriverServiceGroup":
                        layer.setStackdriverServiceGroup(value);
                        break;
                    case "wfs": {
                        wfs.feature.clear();
                        wfs.endPoints.clear();
                        wfs.selector = null;
                        Map<String, Object> x = (Map<String, Object>) v1;
                        x.forEach((sk1, sv1) -> {
                            if (sk1.startsWith("selector")) {
                                wfs.selector = (String) sv1;
                            }
                            if (sk1.startsWith("feature")) {
                                ArrayList x2 = (ArrayList) sv1;
                                x2.forEach((item) -> {                      
                                     wfs.feature.add(item.toString());
                                });
                            }
                            if (sk1.startsWith("endPoints")) {
                                ArrayList x2 = (ArrayList) sv1;
                                x2.forEach((item) -> {                      
                                     wfs.endPoints.add((String) item);
                                });
                            }
                        });

                        System.out.println("\twfs: "+wfs.selector+", "+wfs.feature+", "+wfs.endPoints);
                        layer.setKnownLayerSelector(knownTypeWFSSelector(wfs));
                        break;
                        }
                    case "wmswfs": {
                        String featureName = null;
                        String layerName = null;
                        Map<String, Object> x = (Map<String, Object>) v1;
                        String[] arr = {"",""};
                        x.forEach((sk1, sv1) -> {
                            if (sk1.startsWith("featureName")) {
                                arr[0] = (String) sv1;
                            } 
                            if (sk1.startsWith("layerName")) {
                                arr[1] = (String) sv1;
                            }
                        });
                        featureName = arr[0];
                        layerName = arr[1];
                        System.out.println("\twmswfs: "+featureName+", "+layerName);
                        layer.setKnownLayerSelector(knownTypeWMSWFSSelector(featureName,layerName));
                        break;
                        }
                    case "csw": {
                        String keyword = null;
                        String recordId = null;
                        String serviceName = null;

                        Map<String, Object> x = (Map<String, Object>) v1;
                        String[] arr = {null,null,null};
                        x.forEach((sk1, sv1) -> {
                            if (sk1.startsWith("keyword")) {
                                arr[0] = (String) sv1;
                            } 
                            if (sk1.startsWith("id")) {;
                                arr[1] = (String) sv1;
                            }
                            if (sk1.startsWith("serviceName")) {;
                                arr[2] = (String) sv1;
                            }
                        });
                        keyword = arr[0];
                        recordId = arr[1];
                        serviceName = arr[2];
                        
                        System.out.println("\tcsw: "+keyword+", "+recordId+", "+serviceName);
                        layer.setKnownLayerSelector(knownTypeCSWSelector(keyword, recordId, serviceName));
                        break;
                        }
                    case "iris": {
                        String networkCode = null;
                        String serviceEndPoint = null;

                        Map<String, Object> x = (Map<String, Object>) v1;
                        //System.out.println("\tx=" + x);
                        String[] attr = new String[2];
                        x.forEach((sk1, sv1) -> {
                            if (sk1.startsWith("selector")) {
                                ArrayList x2 = (ArrayList) sv1;
                                attr[0] = (String) x2.get(0);
                                attr[1] = (String) x2.get(1);
                            }
                        });
                        networkCode = attr[0];
                        serviceEndPoint = attr[1];
                        System.out.println("\tiris: "+networkCode+", "+serviceEndPoint);
                        layer.setKnownLayerSelector(knownTypeIRISSelector(networkCode, serviceEndPoint));
                        break;
                        }
                    case "wms": {
                        String layerName = null;
                        ArrayList<String> endPointList = new ArrayList<String>();  
                        String[] attr = new String[1];
                        Map<String, Object> x = (Map<String, Object>) v1;
                        x.forEach((sk1, sv1) -> {
                            if (sk1.startsWith("selector")) {
                                attr[0] = (String) sv1;
                            }
                            if (sk1.startsWith("endPoints")) {
                                ArrayList x2 = (ArrayList) sv1;
                                x2.forEach((item) -> {                       
                                     endPointList.add((String) item);
                                });
                            }
                        });
                        layerName = attr[0];
                        System.out.println("\twms: "+layerName+", "+endPointList);
                        layer.setKnownLayerSelector(knownTypeWMSSelector(layerName,endPointList));
                        break;

                    }
                    case "filters": {
                        filter = new filterCtl();
                        
                        List<AbstractMandatoryParamBinding> mandParamList = null;
                        Map<String, Object> x = (Map<String, Object>) v1;
                        x.forEach((sk1, sv1) -> {
                            if (sk1.startsWith("textbox")) {
                                filter.textbox = new ArrayList<Object>(); 
                                ArrayList x2 = (ArrayList) sv1;
                                x2.forEach((item) -> {                     
                                    filter.textbox.add(item);
                                });                                                               
                            }
                            if (sk1.startsWith("polygonbox")) {
                                filter.polygonbox = new ArrayList<Object>();
                                ArrayList x2 = (ArrayList) sv1;
                                x2.forEach((item) -> {                        
                                    filter.polygonbox.add(item);
                                });                                                      
                            }
                            if (sk1.startsWith("dropdownremote")) {
                                filter.dropdownremote = new ArrayList<Object>();
                                ArrayList x2 = (ArrayList) sv1;
                                x2.forEach((item) -> {                        
                                    filter.dropdownremote.add(item);
                                });                                                               
                            }
                            if (sk1.startsWith("date")) {
                                if (filter.date == null) {
                                    filter.date = new ArrayList<Object>();
                                }                                
                                ArrayList x2 = (ArrayList) sv1;
                                x2.forEach((item) -> {                                  
                                    ArrayList<String> dateAttr = new ArrayList<String>();
                                    ArrayList x3 = (ArrayList) item;
                                    x3.forEach((attr) -> {
                                        dateAttr.add((String)attr);
                                    });
                                    filter.date.add(dateAttr);
                                });
                            }
                            if (sk1.startsWith("selectlist")) {
                                filter.selectlist = new selRec();

                                ((Map<String,Object>) sv1).forEach((sk2, sv2) -> {
                                    if (sk2.startsWith("params")) {
                                        ArrayList x2 = (ArrayList) sv2;
                                        x2.forEach((item) -> {
                                            filter.selectlist.params.add(item);
                                        });
                                    }
                                    if (sk2.startsWith("options")) {
                                        ArrayList x2 = (ArrayList) sv2;
                                        x2.forEach((item) -> {
                                            ArrayList x3 = (ArrayList) item;
                                            String i1 = (String) x3.get(0);
                                            String i2 = (String) x3.get(1);
                                            filter.selectlist.options.add(item);
                                        });
                                    }
                                });
                            }
                            if (sk1.startsWith("mandatorylist")) {
                                filter.mandatorylist = new mandRec();

                                ((Map<String,Object>) sv1).forEach((sk2, sv2) -> {
                                    if (sk2.startsWith("params")) {
                                        ArrayList x2 = (ArrayList) sv2;
                                        x2.forEach((item) -> {
                                            filter.mandatorylist.params.add(item);
                                        });
                                    }
                                    if (sk2.startsWith("options")) {
                                        ArrayList x2 = (ArrayList) sv2;
                                        x2.forEach((item) -> {
                                            ArrayList x3 = (ArrayList) item;
                                            String i1 = (String) x3.get(0);
                                            String i2 = (String) x3.get(1);
                                            filter.mandatorylist.options.add(item);
                                        });
                                    }
                                });
                            }
                            if (sk1.startsWith("mandatorytextbox")) {
                                filter.mandatorytextbox = new ArrayList<Object>();
                                ArrayList x2 = (ArrayList) sv1;
                                x2.forEach((item) -> {                           
                                    filter.mandatorytextbox.add(item);
                                });                                                               
                            }
                            if (sk1.startsWith("checkbox")) {
                                filter.checkbox = new ArrayList<Object>();
                                ArrayList x2 = (ArrayList) sv1;
                                x2.forEach((item) -> {
                                    filter.checkbox.add(item);
                                });
                            }
                        });

                        List<AbstractBaseFilter> optionalFilters = new ArrayList<AbstractBaseFilter>();
                        if (filter != null) {
                            if (filter.textbox != null) {
                                for (int i = 0; i < filter.textbox.size(); i++) {
                                    ArrayList<Object> tb = (ArrayList<Object>) filter.textbox.get(i);
                                    UITextBox ctrlTextBox = new UITextBox((String)tb.get(0), (String)tb.get(1), (String)tb.get(2), Predicate.valueOf((String) tb.get(3)));
                                    optionalFilters.add(ctrlTextBox);
                                }
                            }
                            if (filter.polygonbox != null) {
                                for (int i = 0; i < filter.polygonbox.size(); i++) {
                                    ArrayList<Object> tb = (ArrayList<Object>) filter.polygonbox.get(i);
                                    UIPolygonBBox ctrlPolygonBox = new UIPolygonBBox((String)tb.get(0), (String)tb.get(1), (String)tb.get(2), Predicate.valueOf((String) tb.get(3)));
                                    optionalFilters.add(ctrlPolygonBox);
                                }
                            }
                            if (filter.dropdownremote != null) {
                                for (int i = 0; i < filter.dropdownremote.size(); i++) {
                                    ArrayList<Object> tb = (ArrayList<Object>) filter.dropdownremote.get(i);
                                    UIDropDownRemote ctrlDropDownRemote = new UIDropDownRemote((String)tb.get(0), (String)tb.get(1), (String)tb.get(2), Predicate.valueOf((String) tb.get(3)), (String)tb.get(4));                                    
                                    optionalFilters.add(ctrlDropDownRemote);
                                }
                            }
                            if (filter.selectlist != null) {
                                List<ImmutablePair<String, String>> options = new ArrayList<ImmutablePair<String, String>>();
                                filter.selectlist.options.forEach((opt) -> {
                                    ArrayList o = (ArrayList) opt;
                                    options.add(new ImmutablePair<String, String>((String)o.get(0), (String)o.get(1)));
                                });
                                UIDropDownSelectList uiDropDownSelectList = new UIDropDownSelectList((String)filter.selectlist.params.get(0), (String)filter.selectlist.params.get(1), (String)filter.selectlist.params.get(2), Predicate.valueOf((String) filter.selectlist.params.get(3)), options);
                                optionalFilters.add(uiDropDownSelectList);
                            }
                            if (filter.mandatorylist != null) {
                                List<ImmutablePair<String, String>> mandatoryOptions = new ArrayList<ImmutablePair<String, String>>();
                                filter.mandatorylist.options.forEach((opt) -> {
                                    ArrayList o = (ArrayList) opt;
                                    mandatoryOptions.add(new ImmutablePair<String, String>((String)o.get(0), (String)o.get(1)));
                                });
                                org.auscope.portal.core.uifilter.mandatory.UIDropDownSelectList mandDropDownSelectList;
                                mandDropDownSelectList = new org.auscope.portal.core.uifilter.mandatory.UIDropDownSelectList((String)filter.selectlist.params.get(0),
                                        (String)filter.selectlist.params.get(1), (String)filter.selectlist.params.get(2), mandatoryOptions);

                                mandDropDownSelectList = new org.auscope.portal.core.uifilter.mandatory.UIDropDownSelectList("Color Code",
                                        "ccProperty", "TenementType", mandatoryOptions);
                                mandParamList = new ArrayList<AbstractMandatoryParamBinding>();
                                mandParamList.add(mandDropDownSelectList);
                            }
                            if (filter.mandatorytextbox != null) {                                
                                mandParamList = new ArrayList<AbstractMandatoryParamBinding>();                                
                                for (int i = 0; i < filter.mandatorytextbox.size(); i++) {
                                    ArrayList<String> tb = (ArrayList<String>) filter.mandatorytextbox.get(i);
                                    org.auscope.portal.core.uifilter.mandatory.UITextBox ctrlMandatoryTextBox = new org.auscope.portal.core.uifilter.mandatory.UITextBox(tb.get(0), tb.get(1),"");
                                    mandParamList.add(ctrlMandatoryTextBox);
                                }     
                            }
                            if (filter.date != null) {
                                for (int i = 0; i < filter.date.size(); i++) {
                                    UIDate ctrlDate = new UIDate((String) ((ArrayList<String>) filter.date.get(i)).get(0), (String) ((ArrayList<String>) filter.date.get(i)).get(1), (String) ((ArrayList<String>) filter.date.get(i)).get(2), Predicate.valueOf((String) ((ArrayList<String>) filter.date.get(i)).get(3)));
                                    optionalFilters.add(ctrlDate);
                                }
                            }
                            if (filter.checkbox != null) {
                                for (int i = 0; i < filter.checkbox.size(); i++) {
                                    ArrayList<Object> tb = (ArrayList<Object>) filter.checkbox.get(i);
                                    UICheckBoxGroupProvider uiCheckBoxGroupProvider = new UICheckBoxGroupProvider((String) tb.get(0), (String) tb.get(1));
                                    optionalFilters.add(uiCheckBoxGroupProvider);
                                }
                            }
                        }
                        FilterCollection filterCollection = new FilterCollection();
                        filterCollection.setOptionalFilters(optionalFilters);
                        if (mandParamList != null) {
                            filterCollection.setMandatoryFilters(mandParamList);
                        }
                        layer.setFilterCollection(filterCollection);

                        break;
                    }
                    }
                });
            }
        });

        }
        return layer;
    }

    public KnownLayer knownType(String id) {
        KnownLayer layer = annotateLayer(id);

        return layer;
    }

    @Bean
    public ArrayList<KnownLayer> knownTypes() {
        ArrayList<KnownLayer> knownLayers = new ArrayList<KnownLayer>();

        layersLoaded = true;
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("layers.yaml");
        yamlLayers = yaml.load(inputStream);

        int[] counter = new int[1];
        yamlLayers.forEach((k, v) -> {
            counter[0]++;
            String id = k.toString();
            //if (counter[0] <= 181) { // 180
                System.out.println(counter[0] + ", Key = " + id + ", Value = " + v);
                knownLayers.add(knownType(id));
            //}
        });

        return knownLayers;
    }
}

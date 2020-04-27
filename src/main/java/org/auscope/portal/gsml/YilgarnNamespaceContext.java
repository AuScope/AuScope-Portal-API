package org.auscope.portal.gsml;

import org.auscope.portal.core.services.namespaces.IterableNamespace;

/**
 * The Class YilgarnNamespaceContext.
 */
public class YilgarnNamespaceContext extends IterableNamespace {

    /**
     * Instantiates a new yilgarn namespace context.
     */
    public YilgarnNamespaceContext() {
        map.put("ogc", "http://www.opengis.net/ogc");
        map.put("omx", "http://www.opengis.net/omx/1.0");
        map.put("sa", "http://www.opengis.net/sampling/1.0");
        map.put("om", "http://www.opengis.net/om/1.0");
        map.put("swe", "http://www.opengis.net/swe/1.0.1");
        map.put("ansir", "http://mdu-data.arrc.csiro.au/schema/urn:cgi:xmlns:DI4SMB:ansir:01");
        map.put("wfs", "http://www.opengis.net/wfs");
        map.put("highp", "urn:cgi:xmlns:DI4SMB:HighP:0.1");
        map.put("topp", "http://www.openplans.org/topp");
        map.put("geonetwork", "urn:cgi:xmlns:DI4SMB:geonetwork");
        map.put("sml", "http://www.opengis.net/sensorML/1.0.1");
        map.put("ragingspot", "http://mdu-data-2.arrc.csiro.au/schema/urn:cgi:xmlns:auscope:ragingspot");
        map.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        map.put("gsml", "urn:cgi:xmlns:CGI:GeoSciML:2.0");
        map.put("ows", "http://www.opengis.net/ows");
        map.put("gml", "http://www.opengis.net/gml");
        map.put("xlink", "http://www.w3.org/1999/xlink");
    }
}

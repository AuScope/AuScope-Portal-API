package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.csw.CSWGeographicBoundingBox;
import org.springframework.stereotype.Repository;

@Repository
public class ERDDAPMethodMakerGET implements
        IERDDAPMethodMaker {

    private final Log logger = LogFactory.getLog(getClass());
    
    @Override
    public HttpMethodBase makeMethod(String serviceUrl, String layerName, 
    		CSWGeographicBoundingBox bbox, String format) throws Exception {
        
        // check for the minimum ERDDAP request requirements 
        if (serviceUrl == null || serviceUrl.isEmpty()) 
            throw new IllegalArgumentException("You must specify a serviceUrl");
        if (format == null || format.isEmpty()) 
            throw new IllegalArgumentException("You must specify a format");
        
        // convert bbox co-ordinates to ERDDAP an ERDDAP dimension string
        String erddapDimensions = "%5B("+ bbox.getSouthBoundLatitude() +"):1:("+ bbox.getNorthBoundLatitude() +
		")%5D%5B("+ bbox.getWestBoundLongitude() +"):1:("+ bbox.getEastBoundLongitude() +")%5D";
		
        String uri = serviceUrl + layerName + "." + format + "?" + layerName + erddapDimensions;
        
        GetMethod httpMethod = new GetMethod(uri);
        logger.debug(String.format("uri='%1$s'", uri));

        return httpMethod;
    }
}

package org.auscope.portal.server.web;

import org.apache.commons.httpclient.HttpMethodBase;
import org.auscope.portal.core.services.responses.csw.CSWGeographicBoundingBox;

public interface IERDDAPMethodMaker {
    /**
     * Returns a method that makes an ERDDAP request for a given bouding box
     *
     * @param serviceUrl The url to query
     * @param layerName The layer to query
     * @param bbox The bounding box co-ordinates
     * @param format the desired output format
     * @return
     * @throws Exception
     */
    public HttpMethodBase makeMethod(String serviceUrl, String layerName, CSWGeographicBoundingBox bbox, String format) throws Exception;
}

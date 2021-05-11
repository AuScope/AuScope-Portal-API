package org.auscope.portal.view.knownlayer;

import java.net.MalformedURLException;
import java.net.URL;

import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource;
import org.auscope.portal.core.services.responses.csw.AbstractCSWOnlineResource.OnlineResourceType;
import org.auscope.portal.core.services.responses.csw.CSWRecord;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;

/**
 * The IRISSelector class provides functionality to determine the type of relationship, if any, that exists between a service endpoint and a CSWRecord.
 * 
 * @author Adam
 *
 */
public class IRISSelector implements KnownLayerSelector {

    /** The Network Code. */
    private String networkCode;
    
    /**
     * The service endpoint that the instance of the selector is concerned with.
     */
    private URL serviceEndpoint;

    /**
     * Initialises a new instance of the IRISSelector class.
     * 
     * @param networkCode
     *            The networkCode that identifies which IRIS this KnownLayer is identifying
     * @param serviceEndpoint
     *            The service endpoint that the instance of the selector is concerned with.
     * @throws MalformedURLException
     */
    public IRISSelector(String networkCode, String serviceEndpoint) throws MalformedURLException {
        this.networkCode = networkCode;
        this.serviceEndpoint = new URL(serviceEndpoint);
    }

    /**
     * Gets the networkCode that identifies which IRIS this KnownLayer is identifying.
     *
     * @return the networkCode
     */
    public String getnetworkCode() {
        return networkCode;
    }

    /**
     * Sets the networkCode that identifies which IRIS this KnownLayer is identifying.
     *
     * @param networkCode
     *            the networkCode to set
     */
    public void setnetworkCode(String networkCode) {
        this.networkCode = networkCode;
    }

   
    /**
     * Returns a RelationType enum that indicates the relationship between the record provided and the service endpoint that this IRISSelector was instantiated
     * with.
     */
    @Override
    public RelationType isRelatedRecord(CSWRecord record) {
        AbstractCSWOnlineResource[] onlineResources = record.getOnlineResourcesByType(OnlineResourceType.IRIS);

        if (onlineResources.length > 0) {
            for (AbstractCSWOnlineResource onlineResource : onlineResources) {
                if (networkCode.equals(onlineResource.getName())) {
                    if (serviceEndpoint.sameFile(onlineResource.getLinkage())) {
                        return RelationType.Belongs;
                    }
                }
            }
        }

        return RelationType.NotRelated;
    }
}
    
package org.auscope.portal.server.web.service;

import java.util.ArrayList;


import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.vegl.VglMachineImage;
import org.jclouds.trmk.vcloud_0_8.endpoints.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * A service class for accessing the configured set of VGL machine images
 * @author Josh Vote
 *
 */
@Service
public class VglMachineImageService {
    /** Cached set of image objects*/
    private VglMachineImage[] allImages;

    /**
     * Creates a new set of VglMachineImage objects from an untyped list
     * @param allImages All items must be of type VglMachineImage
     */
    @Autowired
    public VglMachineImageService(@SuppressWarnings("rawtypes") @Qualifier("vglImageList") ArrayList allImages) {
        this.allImages = new VglMachineImage[allImages.size()];
        int i = 0;
        for (Object o : allImages) {
            this.allImages[i++] = (VglMachineImage) o;
        }
    }

    /**
     * Gets every VglMachine image that is accessible by this service.
     * @return
     */
    public VglMachineImage[] getAllImages() throws PortalServiceException {
        return allImages;
    }
}

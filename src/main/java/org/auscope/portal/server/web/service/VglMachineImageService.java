package org.auscope.portal.server.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.vegl.VglMachineImage;
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

    /**
     * Gets a list of VglMachine images that are accessible by a list of given roles.
     * @param roles User roles
     * @return 
     * @throws PortalServiceException
     */
    public VglMachineImage[] getImagesByRoles(String[] roles) throws PortalServiceException {
        List<VglMachineImage> selectedImages = new ArrayList<VglMachineImage>();
        final Set<String> userRoles = new HashSet<String>(Arrays.asList(roles));

        for (VglMachineImage vmi : this.allImages) {
           Set<String> permissions = new HashSet<String>(Arrays.asList(vmi.getPermissions()));
           permissions.retainAll(userRoles);
           if (permissions.size() > 0) {
               selectedImages.add(vmi);
           }
        }

        return selectedImages.toArray(new VglMachineImage[selectedImages.size()]);
    }
}
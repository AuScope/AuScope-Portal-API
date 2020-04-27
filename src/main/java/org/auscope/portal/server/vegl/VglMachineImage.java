package org.auscope.portal.server.vegl;

import org.auscope.portal.core.cloud.MachineImage;

/**
 * Represents a single virtual machine image that can be used for spawning worker instances.
 *
 * Extends the original concept to include security permissions
 * @author Josh Vote
 *
 */
public class VglMachineImage extends MachineImage {
    /** List of roles that have been given the permission to use this image */
    private String[] permissions;

    /**
     * Creates a new VglMachineImage object
     * @param imageId
     */
    public VglMachineImage(String imageId) {
        super(imageId);
    }

    /**
     * List of roles that have been given the permission to use this image
     * @return
     */
    public String[] getPermissions() {
        return permissions;
    }

    /**
     * List of roles that have been given the permission to use this image
     * @param permissions
     */
    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }
}

package org.auscope.portal.server.config;

import org.springframework.stereotype.Service;
/**
 * 
 * Class for tracking for changes in the layers.yaml file in the asucope-assets bucket.
 */
@Service
public class LayerChecksumService {

    private Long checksum;
    
    public Long getChecksum() {
        return this.checksum;
    }
    
    public void setChecksum(Long checksum) {
        this.checksum = checksum;
    }
}

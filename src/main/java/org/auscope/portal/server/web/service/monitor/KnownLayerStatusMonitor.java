/**
 * 
 */
package org.auscope.portal.server.web.service.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.services.KnownLayerService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * This class implements a Quartz jobs to regularly update the service status of the known layers.
 * The update is done through the class KnownLayerService 
 * 
 * @author Carsten Friedrich, CSIRO
 *
 */
public class KnownLayerStatusMonitor extends QuartzJobBean{
    private final Log LOG = LogFactory.getLog(getClass());

    private KnownLayerService cswKnownLayerService;
    
    public KnownLayerService getCswKnownLayerService() {
        return cswKnownLayerService;
    }

    public void setCswKnownLayerService(KnownLayerService cswKnownLayerService) {
        this.cswKnownLayerService = cswKnownLayerService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            cswKnownLayerService.updateKnownLayersCache();
        } catch (Exception ex) {
            LOG.info(String.format("Error updating status of known layers: %1$s", ex.getMessage()));
            LOG.debug("Exception:", ex);
            throw new JobExecutionException(ex);
        }        
    }

}

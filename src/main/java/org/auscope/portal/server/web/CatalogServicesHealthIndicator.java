package org.auscope.portal.server.web;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;

import org.auscope.portal.core.services.CSWCacheService;
import org.auscope.portal.core.services.KnownLayerService;
import org.auscope.portal.core.services.csw.CSWServiceItem;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("CatalogServices")
public class CatalogServicesHealthIndicator implements HealthIndicator {

    private KnownLayerService cswKnownLayerService;
    private CSWCacheService cswCacheService;
    private ArrayList<CSWServiceItem> cswServiceList;
    private RestTemplate restTemplate = new RestTemplateBuilder().setConnectTimeout(Duration.ofMillis(5000))
        .setReadTimeout(Duration.ofMillis(5000))
        .build();

    public CatalogServicesHealthIndicator(CSWCacheService cswCacheService, KnownLayerService cswKnownLayerService,
            ArrayList<CSWServiceItem> cswServiceList) {
        this.cswKnownLayerService = cswKnownLayerService;
        this.cswCacheService = cswCacheService;
        this.cswServiceList = cswServiceList;
    }

    @Override
    public Health health() {
        int knownlayercount = this.cswKnownLayerService.getKnownLayersCache().size();
        int recordcount = this.cswCacheService.getRecordCache().size();
        int keywordcount = this.cswCacheService.getKeywordCache().size();
        HashMap<String, String> cswrecordstatus = new HashMap<String, String>();
        for (CSWServiceItem cswservice : cswServiceList) {
            try {
                ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                        cswservice.getServiceUrl() + "?SERVICE=CSW&REQUEST=GetCapabilities", String.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    String status = responseEntity.getBody();
                    if (status.contains("ServiceIdentification")) {
                        cswrecordstatus.put(cswservice.getServiceUrl(), "UP");
                    } else {
                        cswrecordstatus.put(cswservice.getServiceUrl(), "DOWN");
                    }
                } else {
                    cswrecordstatus.put(cswservice.getServiceUrl(), "DOWN");
                }
            }
            catch (Exception e) {
                cswrecordstatus.put(cswservice.getServiceUrl(), "DOWN");
            }
        }

        Builder result;
        if (recordcount > 0) {
            result = Health.up()
                    .withDetail("cachedCSWRecords", recordcount)
                    .withDetail("knownlayers", knownlayercount)
                    .withDetail("keywords", keywordcount)
                    .withDetail("updateRuningNow", this.cswCacheService.getUpdateRunning());
        } else {
            result = Health.down()
                    .withDetail("cachedCSWRecords", recordcount)
                    .withDetail("knownlayers", knownlayercount)
                    .withDetail("keywords", keywordcount)
                    .withDetail("updateRuningNow", this.cswCacheService.getUpdateRunning());
        }
        if (cswrecordstatus.size() > 0) {
            result.withDetails(cswrecordstatus);
        }
        return result.build();
    }
}

package org.auscope.portal.server.web.controllers;

import java.time.Instant;
import java.util.List;

import org.auscope.portal.core.services.responses.grace.GeoJsonPolygonRequest;
import org.auscope.portal.core.services.responses.grace.GraceGraphResponse;
import org.auscope.portal.server.web.service.GraceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/grace")
public class GraceController {

    private final GraceService graceService;
    
    public GraceController(GraceService graceService) {
        this.graceService = graceService;
    }
    
    @GetMapping("/dates")
    public List<Instant> getDates() {
        return graceService.getDates();
    }
    
    @GetMapping("/point")
    public GraceGraphResponse getPoint(@RequestParam double lon, @RequestParam double lat) {
        return graceService.getPoint(lon, lat);
    }
    
    @PostMapping("/polygon")
    public GraceGraphResponse getPolygon(@RequestBody GeoJsonPolygonRequest polygon) {
        return graceService.getPolygon(polygon);
    }
}

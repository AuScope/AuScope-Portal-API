package org.auscope.portal.server.web.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.auscope.portal.core.services.responses.grace.GeoJsonPolygonRequest;
import org.auscope.portal.core.services.responses.grace.GraceGraphResponse;
import org.auscope.portal.core.services.responses.grace.PointResponse;
import org.auscope.portal.core.services.responses.grace.PolygonSummary;
import org.auscope.portal.core.services.responses.grace.TimeSeriesPoint;
import org.auscope.portal.server.web.repositories.GraceRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.stereotype.Service;

@Service
public class GraceService {

    private final GraceRepository repository;
    
    public GraceService(GraceRepository repository) {
        this.repository = repository;
    }

    public List<Instant> getDates() {
        return repository.getDates();
    }

    public GraceGraphResponse getPoint(double lon, double lat) {
        PointResponse point = repository.getPoint(lon, lat);
        List<Instant> graphX =point.data().stream().map(TimeSeriesPoint::date).toList();
        List<BigDecimal> graphY = point.data().stream().map(TimeSeriesPoint::value).toList();
        List<BigDecimal> errorY = point.data().stream().map(TimeSeriesPoint::error).toList();
        return new GraceGraphResponse(graphX, graphY, errorY, point.primaryMascon(), null, point.totalArea(), null);
    }

    public GraceGraphResponse getPolygon(GeoJsonPolygonRequest request) {
        String wkt = polygonToWkt(request);
        PolygonSummary summary = repository.getPolygonSummary(wkt);
        List<TimeSeriesPoint> data = repository.getPolygonSeries(wkt);
        List<Instant> graphX = data.stream().map(TimeSeriesPoint::date).toList();
        List<BigDecimal> graphY = data.stream().map(TimeSeriesPoint::value).toList();
        return new GraceGraphResponse(graphX, graphY, List.of(), null, summary.primaryMascons(), summary.totalArea(), null);
    }

    private String polygonToWkt(GeoJsonPolygonRequest request) {
        Coordinate[] coordinates = request.coordinates().get(0).stream().map(c ->
            new Coordinate(c.get(0),c.get(1))).toArray(Coordinate[]::new);
        GeometryFactory factory = new GeometryFactory();
        Polygon polygon = factory.createPolygon(coordinates);
        return new WKTWriter().write(polygon);
    }

}

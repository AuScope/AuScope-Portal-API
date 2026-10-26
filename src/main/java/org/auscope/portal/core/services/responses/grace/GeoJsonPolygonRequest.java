package org.auscope.portal.core.services.responses.grace;

import java.util.List;

public record GeoJsonPolygonRequest(String type, List<List<List<Double>>> coordinates) { }

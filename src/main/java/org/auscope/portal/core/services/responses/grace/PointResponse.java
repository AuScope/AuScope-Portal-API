package org.auscope.portal.core.services.responses.grace;

import java.math.BigDecimal;
import java.util.List;

public record PointResponse(Integer primaryMascon, BigDecimal totalArea, List<TimeSeriesPoint> data) { }

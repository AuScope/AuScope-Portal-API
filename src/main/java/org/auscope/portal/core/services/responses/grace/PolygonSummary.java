package org.auscope.portal.core.services.responses.grace;

import java.math.BigDecimal;

public record PolygonSummary(Integer primaryMascons, Integer intersectedMascons, BigDecimal totalArea) { }

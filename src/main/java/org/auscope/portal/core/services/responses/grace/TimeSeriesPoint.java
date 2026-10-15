package org.auscope.portal.core.services.responses.grace;

import java.math.BigDecimal;
import java.time.Instant;

public record TimeSeriesPoint(Instant date, BigDecimal value, BigDecimal error) { }

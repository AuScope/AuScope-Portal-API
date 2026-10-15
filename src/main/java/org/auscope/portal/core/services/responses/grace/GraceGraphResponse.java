package org.auscope.portal.core.services.responses.grace;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Pre-formatted graph response to GRACE data queries
 */
public record GraceGraphResponse(
    List<Instant> graph_x,
    List<BigDecimal> graph_y,
    List<BigDecimal> error_y,
    Integer primary_mascon_id,
    Integer primary_mascons,
    BigDecimal total_area,
    String centroid
) { }
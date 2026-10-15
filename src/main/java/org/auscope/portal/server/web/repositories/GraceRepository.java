package org.auscope.portal.server.web.repositories;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.auscope.portal.core.services.responses.grace.PointResponse;
import org.auscope.portal.core.services.responses.grace.PolygonSummary;
import org.auscope.portal.core.services.responses.grace.TimeSeriesPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GraceRepository {

    private final NamedParameterJdbcTemplate jdbc;
    
    public GraceRepository(@Qualifier("graceJdbcTemplate") NamedParameterJdbcTemplate jdbc) {
            this.jdbc = jdbc;
    }

    public List<Instant> getDates() {    
        String sql = """
            SELECT DISTINCT solution_date
            FROM grace_data_stage_5
            ORDER BY solution_date DESC
        """;
        return jdbc.query(
        sql,
        new MapSqlParameterSource(), (rs, rowNum) -> rs.getTimestamp("solution_date").toInstant());
    }

    public PointResponse getPoint(double lon, double lat) {
        String sql = """
            WITH nearest AS (
                SELECT
                    id,
                    primary_mascon,
                    area
                FROM mascons_stage_5
                ORDER BY geog <-> ST_GeogFromText(:point)
                LIMIT 1
            )
            
            SELECT
                n.primary_mascon,
                n.area,
                d.solution_date,
                d.ewh,
                d.sigma
            FROM nearest n
            JOIN grace_data_stage_5 d
            ON d.mascon_id = n.id
            ORDER BY d.solution_date
        """;
        String pointWkt = "SRID=4326;POINT(" + lon + " " + lat + ")";
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("point", pointWkt);
        List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        if (rows.isEmpty()) {
            return null;
        }
        Integer primaryMascon = (Integer)rows.get(0).get("primary_mascon");
        BigDecimal totalArea = (BigDecimal) rows.get(0).get("area");
        List<TimeSeriesPoint> series = new ArrayList<TimeSeriesPoint>();
        for (Map<String,Object> row : rows) {
            series.add(new TimeSeriesPoint(((Timestamp) row.get("solution_date")).toInstant(),
                (BigDecimal) row.get("ewh"),
                (BigDecimal) row.get("sigma")));
        }
        return new PointResponse(primaryMascon, totalArea, series);
    }

    public PolygonSummary getPolygonSummary(String polygonWkt) {
        String sql = """
            SELECT
                COUNT(DISTINCT primary_mascon) AS primary_count,
                COUNT(*) AS mascon_count,
                SUM(area) AS total_area
            FROM mascons_stage_5
            WHERE ST_Intersects(geog, ST_GeogFromText('SRID=4326;' || :polygon))
        """;
        Map<String,Object> result = jdbc.queryForMap(sql, new MapSqlParameterSource().addValue("polygon", polygonWkt));
        return new PolygonSummary(
            ((Number) result.get("primary_count")).intValue(),
            ((Number) result.get("mascon_count")).intValue(),
            (BigDecimal) result.get("total_area")
        );
    }

    public List<TimeSeriesPoint> getPolygonSeries(
        String polygonWkt) {
        String sql = """
            SELECT
                d.solution_date,
                SUM(d.ewh) AS total_ewh,
                SUM(m.area) AS total_area,
                SUM(d.ewh) / SUM(m.area) AS area_weighted_ewh
            FROM grace_data_stage_5 d
            JOIN mascons_stage_5 m
                ON d.mascon_id = m.id
            WHERE ST_Intersects(m.geog, ST_GeogFromText('SRID=4326;' || :polygon))
            GROUP BY d.solution_date
            ORDER BY d.solution_date
        """;
        return jdbc.query(
            sql,
            new MapSqlParameterSource().addValue("polygon", polygonWkt),
            (rs,rowNum) ->
                new TimeSeriesPoint(
                    rs.getTimestamp("solution_date").toInstant(),
                    rs.getBigDecimal("area_weighted_ewh"), null)
        );
    }

}

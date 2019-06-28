package org.auscope.portal.server.web.repositories;

import org.auscope.portal.server.vegl.VEGLSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VEGLSeriesRepository extends JpaRepository<VEGLSeries, Integer> {

	
}

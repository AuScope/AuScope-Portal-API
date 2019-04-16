package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.vegl.VEGLJob;
import org.auscope.portal.server.web.controllers.JobBuilderController;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VEGLJobRepository extends JpaRepository<VEGLJob, Integer> {
	
	
	@Query("SELECT j FROM VEGLJob j WHERE j.seriesId= ?1 AND j.emailAddress = ?2 AND LOWER(j.status) <> 'deleted'")
	List<VEGLJob> findBySeriesIdAndEmail(Integer seriesId, String email);
	
	@Query("SELECT j FROM VEGLJob j WHERE j.emailAddress = ?1 AND LOWER(j.status) <> 'deleted'")
	List<VEGLJob> findByEmail(String email);
	
	@Query("SELECT j FROM VEGLJob j WHERE LOWER(j.status)='" + JobBuilderController.STATUS_PENDING +
			"' OR LOWER(j.status)='" + JobBuilderController.STATUS_ACTIVE + "'")
	List<VEGLJob> findPendingOrActiveJobs();
	
	@Query("SELECT j FROM VEGLJob j WHERE LOWER(j.status)='" + JobBuilderController.STATUS_INQUEUE + "'")
	List<VEGLJob> findInqueueJobs();
	
}

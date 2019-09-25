package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.vegl.VGLJobAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VGLJobAuditLogRepository extends JpaRepository<VGLJobAuditLog, Integer> {

	List<VGLJobAuditLog> findByJobId(Integer id);
}

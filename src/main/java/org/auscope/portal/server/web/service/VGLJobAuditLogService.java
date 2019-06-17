package org.auscope.portal.server.web.service;

import java.util.List;

import org.auscope.portal.server.vegl.VGLJobAuditLog;
import org.auscope.portal.server.web.repositories.VGLJobAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VGLJobAuditLogService {

	@Autowired
	private VGLJobAuditLogRepository jobAuditLogRepository;

	
	public List<VGLJobAuditLog> getAuditLogsOfJob(final int jobId) {
		return jobAuditLogRepository.findByJobId(jobId);
    }

	
    /**
     * Retrieves the series with given ID.
     */
    public VGLJobAuditLog get(final int id) {
    	return jobAuditLogRepository.findById(id).orElse(null);
    }

    
    /**
     * Saves or updates the given series.
     */
    public void save(final VGLJobAuditLog jobAuditLog) {
    	jobAuditLogRepository.save(jobAuditLog);
    }
}

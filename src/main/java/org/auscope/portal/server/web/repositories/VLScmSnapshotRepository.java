package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.vegl.VLScmSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VLScmSnapshotRepository extends JpaRepository<VLScmSnapshot, Integer> {

	List<VLScmSnapshot> findByScmEntryId(String scmEntryId);
	VLScmSnapshot findByScmEntryIdAndComputeServiceId(String scmEntryId, String computeServiceId);
}

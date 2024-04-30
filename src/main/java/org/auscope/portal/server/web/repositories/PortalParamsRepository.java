package org.auscope.portal.server.web.repositories;

import org.auscope.portal.server.params.HashmapParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalParamsRepository extends JpaRepository<HashmapParams, Integer> {
	HashmapParams findByKey(String key);
}

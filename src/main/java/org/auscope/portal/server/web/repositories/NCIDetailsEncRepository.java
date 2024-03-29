package org.auscope.portal.server.web.repositories;

import org.auscope.portal.server.web.security.PortalUser;
import org.auscope.portal.server.web.security.NCIDetailsEnc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NCIDetailsEncRepository extends JpaRepository<NCIDetailsEnc, Integer> {

	NCIDetailsEnc findByUser(PortalUser user);
}

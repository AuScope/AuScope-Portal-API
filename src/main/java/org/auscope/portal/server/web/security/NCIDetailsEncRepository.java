package org.auscope.portal.server.web.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NCIDetailsEncRepository extends JpaRepository<NCIDetailsEnc, Integer> {

	NCIDetailsEnc findByUser(ANVGLUser user);
}

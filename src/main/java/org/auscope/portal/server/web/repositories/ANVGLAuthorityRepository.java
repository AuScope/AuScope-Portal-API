package org.auscope.portal.server.web.repositories;

import org.auscope.portal.server.web.security.ANVGLAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ANVGLAuthorityRepository extends JpaRepository<ANVGLAuthority, Integer> {

}

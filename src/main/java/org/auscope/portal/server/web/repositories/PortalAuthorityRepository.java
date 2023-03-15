package org.auscope.portal.server.web.repositories;

import org.auscope.portal.server.web.security.PortalAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalAuthorityRepository extends JpaRepository<PortalAuthority, Integer> {

}

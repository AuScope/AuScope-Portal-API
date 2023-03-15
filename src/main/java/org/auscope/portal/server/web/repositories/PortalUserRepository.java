package org.auscope.portal.server.web.repositories;

import org.auscope.portal.server.web.security.PortalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PortalUserRepository extends JpaRepository<PortalUser, String> {

    PortalUser findByEmail(String email);
    boolean existsByEmail(String email);
    PortalUser findByFullName(String fullName);
}

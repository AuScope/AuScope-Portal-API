package org.auscope.portal.server.web.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ANVGLUserRepository extends JpaRepository<ANVGLUser, String> {

    ANVGLUser findByEmail(String email);   
    ANVGLUser findByFullName(String fullName);
}

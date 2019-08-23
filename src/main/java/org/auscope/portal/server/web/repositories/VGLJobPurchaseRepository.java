package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.vegl.VGLJobPurchase;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VGLJobPurchaseRepository extends JpaRepository<VGLJobPurchase, Integer> {

	List<VGLJobPurchase> findByParent(ANVGLUser user);
}

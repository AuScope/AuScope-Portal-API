package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.vegl.VGLDataPurchase;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VGLDataPurchaseRepository extends JpaRepository<VGLDataPurchase, Integer> {

	List<VGLDataPurchase> findByParentOrderByDateDesc(ANVGLUser user);
}

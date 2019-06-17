package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.vegl.VGLBookMark;
import org.auscope.portal.server.web.security.ANVGLUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VGLBookMarkRepository extends JpaRepository<VGLBookMark, Integer> {

	List<VGLBookMark> findByParent(ANVGLUser user);
}

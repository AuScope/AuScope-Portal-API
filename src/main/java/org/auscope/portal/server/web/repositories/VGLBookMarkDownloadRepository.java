package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.vegl.VGLBookMark;
import org.auscope.portal.server.vegl.VGLBookMarkDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VGLBookMarkDownloadRepository extends JpaRepository<VGLBookMarkDownload, Integer> {
	
	List<VGLBookMarkDownload> findByParent(VGLBookMark bookmark);
}

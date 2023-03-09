package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.bookmark.BookMark;
import org.auscope.portal.server.bookmark.BookMarkDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookMarkDownloadRepository extends JpaRepository<BookMarkDownload, Integer> {
	
	List<BookMarkDownload> findByParent(BookMark bookmark);
}

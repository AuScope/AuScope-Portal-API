package org.auscope.portal.server.web.repositories;

import java.util.List;

import org.auscope.portal.server.bookmark.BookMark;
import org.auscope.portal.server.web.security.PortalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Integer> {

	List<BookMark> findByParent(PortalUser user);
}

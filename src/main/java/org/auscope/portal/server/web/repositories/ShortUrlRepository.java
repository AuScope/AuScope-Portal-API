package org.auscope.portal.server.web.repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.auscope.portal.server.shorturl.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Integer> {
    
    List<ShortUrl> findAll();

    ShortUrl findByName(String name);

    @Query("SELECT u.id FROM ShortUrl u WHERE TO_TIMESTAMP(u.timestamp, 'YYYY-MM-DD HH24:MI:SS') <= :cutoffDate AND persist = false")
    List<Integer> findExpired(@Param("cutoffDate") LocalDateTime cutoffDate);
    

    @Transactional
    @Modifying
    @Query("DELETE FROM ShortUrl u WHERE TO_TIMESTAMP(u.timestamp, 'YYYY-MM-DD HH24:MI:SS') <= :cutoffDate AND persist = false")
    Integer deleteExpired(@Param("cutoffDate") LocalDateTime cutoffDate);

    void deleteById(Integer id);

}

package com.crms.domain.operative.repository;

import com.crms.domain.operative.entity.SiteSignOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteSignOnRepository extends JpaRepository<SiteSignOn, Long> {

    List<SiteSignOn> findBySiteId(Long siteId);

    List<SiteSignOn> findByOperativeId(Long operativeId);

    @Query("SELECT s FROM SiteSignOn s WHERE s.site.id = :siteId AND s.operative.id = :operativeId AND s.signOnTime >= :startOfDay AND s.signOffTime IS NULL")
    Optional<SiteSignOn> findActiveSignOn(@Param("siteId") Long siteId, @Param("operativeId") Long operativeId, @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT s FROM SiteSignOn s WHERE s.site.id = :siteId AND s.signOnTime >= :startTime AND s.signOnTime <= :endTime")
    List<SiteSignOn> findBySiteAndDateRange(@Param("siteId") Long siteId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM SiteSignOn s WHERE s.operative.id = :operativeId AND s.weekEnding >= :startDate AND s.weekEnding <= :endDate")
    List<SiteSignOn> findByOperativeAndDateRange(@Param("operativeId") Long operativeId, @Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);
}

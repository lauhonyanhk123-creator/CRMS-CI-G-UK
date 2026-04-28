package com.crms.domain.healthsafety.repository;

import com.crms.domain.healthsafety.entity.RAMSSignOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RAMSSignOnRepository extends JpaRepository<RAMSSignOn, Long> {

    List<RAMSSignOn> findByRamsId(Long ramsId);

    List<RAMSSignOn> findByOperativeId(Long operativeId);

    List<RAMSSignOn> findBySiteId(Long siteId);

    @Query("SELECT r FROM RAMSSignOn r WHERE r.operative.id = :operativeId AND r.site.id = :siteId AND r.validUntil > :now")
    Optional<RAMSSignOn> findValidSignOn(@Param("operativeId") Long operativeId, @Param("siteId") Long siteId, @Param("now") LocalDateTime now);

    @Query("SELECT r FROM RAMSSignOn r WHERE r.validUntil <= :date AND r.validUntil > :now")
    List<RAMSSignOn> findExpiringSignOns(@Param("date") LocalDateTime date, @Param("now") LocalDateTime now);
}

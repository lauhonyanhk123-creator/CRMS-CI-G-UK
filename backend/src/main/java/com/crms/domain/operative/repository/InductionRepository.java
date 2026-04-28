package com.crms.domain.operative.repository;

import com.crms.domain.operative.entity.Induction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InductionRepository extends JpaRepository<Induction, Long> {

    List<Induction> findByOperativeId(Long operativeId);

    List<Induction> findBySiteId(Long siteId);

    @Query("SELECT i FROM Induction i WHERE i.operative.id = :operativeId AND i.site.id = :siteId AND i.validUntil > :now")
    Optional<Induction> findValidInduction(@Param("operativeId") Long operativeId, @Param("siteId") Long siteId, @Param("now") LocalDateTime now);

    @Query("SELECT i FROM Induction i WHERE i.validUntil <= :date AND i.validUntil > :now")
    List<Induction> findExpiringInductions(@Param("date") LocalDateTime date, @Param("now") LocalDateTime now);
}

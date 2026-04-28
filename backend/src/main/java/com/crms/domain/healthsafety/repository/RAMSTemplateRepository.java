package com.crms.domain.healthsafety.repository;

import com.crms.domain.healthsafety.entity.RAMSTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RAMSTemplateRepository extends JpaRepository<RAMSTemplate, Long> {

    List<RAMSTemplate> findByIsActiveTrue();

    @Query("SELECT r FROM RAMSTemplate r WHERE r.trade = :trade AND r.isActive = true")
    List<RAMSTemplate> findActiveByTrade(@Param("trade") String trade);

    List<RAMSTemplate> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title);

    @Query("SELECT DISTINCT r.trade FROM RAMSTemplate r WHERE r.trade IS NOT NULL AND r.isActive = true")
    List<String> findDistinctTrades();
}

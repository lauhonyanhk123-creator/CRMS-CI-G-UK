package com.crms.domain.quality.repository;

import com.crms.domain.quality.entity.ITPTemplate;
import com.crms.domain.quality.enums.TemplateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITPTemplateRepository extends JpaRepository<ITPTemplate, Long> {

    Page<ITPTemplate> findByStatus(TemplateStatus status, Pageable pageable);

    Page<ITPTemplate> findByCategory(String category, Pageable pageable);

    Optional<ITPTemplate> findByName(String name);

    @Query("SELECT t FROM ITPTemplate t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:tradeCategory IS NULL OR t.tradeCategory = :tradeCategory)")
    Page<ITPTemplate> findByFilters(
        @Param("status") TemplateStatus status,
        @Param("category") String category,
        @Param("tradeCategory") String tradeCategory,
        Pageable pageable
    );

    List<ITPTemplate> findByTradeCategory(String tradeCategory);

    @Query("SELECT DISTINCT t.category FROM ITPTemplate t ORDER BY t.category")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT t.tradeCategory FROM ITPTemplate t WHERE t.tradeCategory IS NOT NULL ORDER BY t.tradeCategory")
    List<String> findDistinctTradeCategories();
}

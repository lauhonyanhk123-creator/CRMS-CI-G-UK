package com.crms.domain.tender.repository;

import com.crms.domain.tender.entity.BoQItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BoQItemRepository extends JpaRepository<BoQItem, Long> {

    List<BoQItem> findByTenderId(Long tenderId);

    @Query("SELECT SUM(b.totalValue) FROM BoQItem b WHERE b.tender.id = :tenderId")
    Optional<BigDecimal> sumTotalValueByTenderId(@Param("tenderId") Long tenderId);

    @Query("SELECT b FROM BoQItem b WHERE b.tender.id = :tenderId AND b.trade = :trade")
    List<BoQItem> findByTenderIdAndTrade(@Param("tenderId") Long tenderId, @Param("trade") String trade);
}

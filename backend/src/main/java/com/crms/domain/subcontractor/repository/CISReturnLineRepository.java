package com.crms.domain.subcontractor.repository;

import com.crms.domain.subcontractor.entity.CISReturnLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CISReturnLineRepository extends JpaRepository<CISReturnLine, Long> {

    List<CISReturnLine> findByCisReturnId(Long cisReturnId);

    @Query("SELECT SUM(l.deduction) FROM CISReturnLine l WHERE l.cisReturn.id = :cisReturnId")
    Optional<BigDecimal> sumDeductionsByCisReturnId(@Param("cisReturnId") Long cisReturnId);

    @Query("SELECT SUM(l.grossPaid) FROM CISReturnLine l WHERE l.subcontractor.id = :subcontractorId")
    Optional<BigDecimal> sumGrossPaidBySubcontractorId(@Param("subcontractorId") Long subcontractorId);
}

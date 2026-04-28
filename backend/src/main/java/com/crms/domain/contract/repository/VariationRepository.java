package com.crms.domain.contract.repository;

import com.crms.domain.contract.entity.Variation;
import com.crms.domain.contract.enums.VariationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VariationRepository extends JpaRepository<Variation, Long> {

    Optional<Variation> findByVariationRef(String variationRef);

    List<Variation> findByContractId(Long contractId);

    List<Variation> findByContractIdOrderByNotifiedDateDesc(Long contractId);

    @Query("SELECT v FROM Variation v WHERE v.contract.id = :contractId AND v.status = :status")
    List<Variation> findByContractIdAndStatus(@Param("contractId") Long contractId, @Param("status") VariationStatus status);

    @Query("SELECT SUM(v.agreedValue) FROM Variation v WHERE v.contract.id = :contractId AND v.status = 'AGREED'")
    Optional<BigDecimal> sumAgreedValueByContractId(@Param("contractId") Long contractId);
}

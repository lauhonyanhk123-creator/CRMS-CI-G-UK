package com.crms.domain.subcontractor.repository;

import com.crms.domain.subcontractor.entity.CISReturn;
import com.crms.domain.subcontractor.enums.CisReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CISReturnRepository extends JpaRepository<CISReturn, Long> {

    Optional<CISReturn> findByTaxMonth(String taxMonth);

    List<CISReturn> findByStatus(CisReturnStatus status);

    @Query("SELECT c FROM CISReturn c LEFT JOIN FETCH c.cisReturnLines WHERE c.id = :id")
    Optional<CISReturn> findByIdWithLines(@Param("id") Long id);

    @Query("SELECT c FROM CISReturn c WHERE c.taxMonth = :taxMonth AND c.status != 'DRAFT'")
    Optional<CISReturn> findSubmittedByTaxMonth(@Param("taxMonth") String taxMonth);
}

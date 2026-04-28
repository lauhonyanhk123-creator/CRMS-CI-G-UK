package com.crms.domain.quality.repository;

import com.crms.domain.quality.entity.SignOff;
import com.crms.domain.quality.enums.BuildingControlType;
import com.crms.domain.quality.enums.SignOffResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SignOffRepository extends JpaRepository<SignOff, Long> {

    Page<SignOff> findByContractId(Long contractId, Pageable pageable);

    Page<SignOff> findByBuildingControlType(BuildingControlType type, Pageable pageable);

    Page<SignOff> findByResult(SignOffResult result, Pageable pageable);

    @Query("SELECT s FROM SignOff s WHERE " +
           "(:contractId IS NULL OR s.contract.id = :contractId) AND " +
           "(:buildingControlType IS NULL OR s.buildingControlType = :buildingControlType) AND " +
           "(:result IS NULL OR s.result = :result)")
    Page<SignOff> findByFilters(
        @Param("contractId") Long contractId,
        @Param("buildingControlType") BuildingControlType buildingControlType,
        @Param("result") SignOffResult result,
        Pageable pageable
    );

    List<SignOff> findByInspectionDateBetween(LocalDate start, LocalDate end);

    List<SignOff> findByNextInspectionDate(LocalDate date);

    Optional<SignOff> findByReferenceNumber(String referenceNumber);

    @Query("SELECT s FROM SignOff s WHERE s.contract.id = :contractId AND s.buildingControlType = :type ORDER BY s.inspectionDate DESC")
    List<SignOff> findByContractAndType(@Param("contractId") Long contractId, @Param("type") BuildingControlType type);
}

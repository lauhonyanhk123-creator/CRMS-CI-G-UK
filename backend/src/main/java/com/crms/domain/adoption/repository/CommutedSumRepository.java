package com.crms.domain.adoption.repository;

import com.crms.domain.adoption.entity.CommutedSum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommutedSumRepository extends JpaRepository<CommutedSum, Long> {

    Optional<CommutedSum> findByAdoptionCaseId(Long adoptionCaseId);

    @Query("SELECT cs FROM CommutedSum cs WHERE cs.adoptionCase.contract.id = :contractId")
    List<CommutedSum> findByContractId(@Param("contractId") Long contractId);

    @Query("SELECT cs FROM CommutedSum cs WHERE cs.commutedSumType = :type")
    List<CommutedSum> findByCommutedSumType(@Param("type") String type);

    @Query("SELECT cs FROM CommutedSum cs WHERE cs.paidAmount < cs.totalAmount")
    List<CommutedSum> findUnpaidCommutedSums();

    @Query("SELECT cs FROM CommutedSum cs WHERE cs.releasedAmount < cs.paidAmount")
    List<CommutedSum> findPartiallyReleasedCommutedSums();

    @Query("SELECT COALESCE(SUM(cs.totalAmount), 0) FROM CommutedSum cs")
    java.math.BigDecimal sumTotalAmount();
}

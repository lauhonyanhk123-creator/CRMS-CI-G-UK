package com.crms.domain.contract.repository;

import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationForPaymentRepository extends JpaRepository<ApplicationForPayment, Long> {

    Optional<ApplicationForPayment> findByApplicationRef(String applicationRef);

    List<ApplicationForPayment> findByContractId(Long contractId);

    @Query("SELECT a FROM ApplicationForPayment a WHERE a.contract.id = :contractId ORDER BY a.applicationNumber DESC")
    List<ApplicationForPayment> findByContractIdOrderByNumberDesc(@Param("contractId") Long contractId);

    @Query("SELECT MAX(a.applicationNumber) FROM ApplicationForPayment a WHERE a.contract.id = :contractId")
    Optional<Integer> findMaxApplicationNumberByContractId(@Param("contractId") Long contractId);

    @Query("SELECT SUM(a.grossValue) FROM ApplicationForPayment a WHERE a.contract.id = :contractId AND a.status IN ('PAID', 'CERTIFIED')")
    Optional<BigDecimal> sumPaidValueByContractId(@Param("contractId") Long contractId);

    @Query("SELECT a FROM ApplicationForPayment a WHERE a.dueDate <= :date AND a.status = 'SUBMITTED'")
    List<ApplicationForPayment> findOverdueApplications(@Param("date") LocalDate date);

    @Query("SELECT a FROM ApplicationForPayment a WHERE a.contract.id = :contractId AND a.status IN ('PAID', 'CERTIFIED') AND a.dueDate <= :upToDate")
    List<ApplicationForPayment> findApprovedApplicationsUpToDate(@Param("contractId") Long contractId, @Param("upToDate") LocalDate upToDate);
}

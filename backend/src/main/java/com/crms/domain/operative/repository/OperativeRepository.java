package com.crms.domain.operative.repository;

import com.crms.domain.company.entity.Company;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.enums.OperativeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperativeRepository extends JpaRepository<Operative, Long> {

    Optional<Operative> findByEmployeeRef(String employeeRef);

    Optional<Operative> findByNiNumber(String niNumber);

    List<Operative> findByEmployer(Company employer);

    List<Operative> findByStatus(OperativeStatus status);

    long countByStatus(OperativeStatus status);

    Page<Operative> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);

    @Query("SELECT o FROM Operative o WHERE o.rightToWorkExpiry <= :date AND o.status = 'ACTIVE'")
    List<Operative> findWithExpiringRightToWork(@Param("date") LocalDate date);

    @Query("SELECT o FROM Operative o LEFT JOIN FETCH o.cards LEFT JOIN FETCH o.qualifications WHERE o.id = :id")
    Optional<Operative> findByIdWithCardsAndQualifications(@Param("id") Long id);

    @Query("SELECT o FROM Operative o WHERE o.employer.id = :companyId AND o.status = 'ACTIVE'")
    List<Operative> findActiveByCompanyId(@Param("companyId") Long companyId);

    boolean existsByEmployeeRef(String employeeRef);

    boolean existsByNiNumber(String niNumber);

    // Fix N+1: Use @EntityGraph to eagerly fetch employer for findAll queries
    @EntityGraph(attributePaths = {"employer"})
    Page<Operative> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"employer"})
    Page<Operative> findByStatus(OperativeStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"employer"})
    @Override
    Optional<Operative> findById(Long id);

    @EntityGraph(attributePaths = {"employer"})
    @Query("SELECT o FROM Operative o WHERE LOWER(o.firstName) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(o.lastName) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(o.employeeRef) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<Operative> searchByNameOrRef(@Param("q") String query, Pageable pageable);
}

package com.crms.domain.company.repository;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.enums.CompanyStatus;
import com.crms.domain.company.enums.CompanyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByCompanyType(CompanyType companyType);

    List<Company> findByStatus(CompanyStatus status);

    List<Company> findByCompanyTypeAndStatus(CompanyType companyType, CompanyStatus status);

    Optional<Company> findByRegistrationNumber(String registrationNumber);

    Optional<Company> findByCompaniesHouseId(String companiesHouseId);

    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.companyType = :type AND c.status = :status AND c.cisStatus = 'VERIFIED'")
    List<Company> findVerifiedSubcontractors(@Param("type") CompanyType type, @Param("status") CompanyStatus status);

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.contacts WHERE c.id = :id")
    Optional<Company> findByIdWithContacts(@Param("id") Long id);

    boolean existsByName(String name);

    boolean existsByRegistrationNumber(String registrationNumber);
}

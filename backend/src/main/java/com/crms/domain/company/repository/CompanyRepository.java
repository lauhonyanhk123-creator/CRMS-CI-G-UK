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

    @Query("SELECT DISTINCT c FROM Company c WHERE c.companyType = :type")
    List<Company> findByCompanyType(@Param("type") CompanyType companyType);

    @Query("SELECT DISTINCT c FROM Company c WHERE c.status = :status")
    List<Company> findByStatus(@Param("status") CompanyStatus status);

    @Query("SELECT DISTINCT c FROM Company c WHERE c.companyType = :type AND c.status = :status")
    List<Company> findByCompanyTypeAndStatus(@Param("type") CompanyType companyType, @Param("status") CompanyStatus status);

    Optional<Company> findByRegistrationNumber(String registrationNumber);

    Optional<Company> findByCompaniesHouseId(String companiesHouseId);

    @Query("SELECT DISTINCT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Company> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.companyType = :type AND c.status = :status AND c.cisStatus = 'VERIFIED'")
    List<Company> findVerifiedSubcontractors(@Param("type") CompanyType type, @Param("status") CompanyStatus status);

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.contacts WHERE c.id = :id")
    Optional<Company> findByIdWithContacts(@Param("id") Long id);
    
    @Query("SELECT DISTINCT c FROM Company c")
    Page<Company> findAll(Pageable pageable);
    
    @Query("SELECT DISTINCT c FROM Company c WHERE c.companyType = :type")
    Page<Company> findByCompanyType(@Param("type") CompanyType companyType, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByRegistrationNumber(String registrationNumber);
}

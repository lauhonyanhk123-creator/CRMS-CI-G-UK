package com.crms.domain.company.repository;

import com.crms.domain.company.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("SELECT DISTINCT c FROM Contact c LEFT JOIN FETCH c.company WHERE c.company.id = :companyId")
    List<Contact> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT DISTINCT c FROM Contact c LEFT JOIN FETCH c.company WHERE c.isPrimary = true")
    List<Contact> findByIsPrimaryTrue();

    @Query("SELECT DISTINCT c FROM Contact c LEFT JOIN FETCH c.company WHERE c.email = :email")
    Optional<Contact> findByEmail(@Param("email") String email);

    @Query("SELECT c FROM Contact c WHERE c.company.id = :companyId AND c.isPrimary = true")
    Optional<Contact> findPrimaryContactByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT DISTINCT c FROM Contact c LEFT JOIN FETCH c.company")
    Page<Contact> findAll(Pageable pageable);
    
    @Query("SELECT DISTINCT c FROM Contact c LEFT JOIN FETCH c.company WHERE c.company.id = :companyId")
    Page<Contact> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    boolean existsByEmail(String email);
}

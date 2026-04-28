package com.crms.domain.company.repository;

import com.crms.domain.company.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByCompanyId(Long companyId);

    List<Contact> findByIsPrimaryTrue();

    Optional<Contact> findByEmail(String email);

    @Query("SELECT c FROM Contact c WHERE c.company.id = :companyId AND c.isPrimary = true")
    Optional<Contact> findPrimaryContactByCompanyId(@Param("companyId") Long companyId);

    boolean existsByEmail(String email);
}

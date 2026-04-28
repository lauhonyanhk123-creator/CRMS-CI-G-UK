package com.crms.domain.subcontractor.repository;

import com.crms.domain.subcontractor.entity.Subcontractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubcontractorRepository extends JpaRepository<Subcontractor, Long> {

    @Query("SELECT s FROM Subcontractor s WHERE s.name LIKE %:name%")
    List<Subcontractor> findByNameContaining(@Param("name") String name);

    @Query("SELECT s FROM Subcontractor s LEFT JOIN FETCH s.cisVerifications WHERE s.id = :id")
    Optional<Subcontractor> findByIdWithVerifications(@Param("id") Long id);

    boolean existsByRegistrationNumber(String registrationNumber);
}

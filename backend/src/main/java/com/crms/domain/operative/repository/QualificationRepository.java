package com.crms.domain.operative.repository;

import com.crms.domain.operative.entity.Qualification;
import com.crms.domain.operative.enums.QualificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QualificationRepository extends JpaRepository<Qualification, Long> {

    List<Qualification> findByOperativeId(Long operativeId);

    List<Qualification> findByQualificationType(QualificationType type);

    @Query("SELECT q FROM Qualification q WHERE q.operative.id = :operativeId AND q.qualificationType = :type AND q.expiryDate > :date")
    List<Qualification> findValidByType(@Param("operativeId") Long operativeId, @Param("type") QualificationType type, @Param("date") LocalDate date);

    @Query("SELECT q FROM Qualification q WHERE q.expiryDate <= :date AND q.expiryDate > :today")
    List<Qualification> findExpiringQualifications(@Param("date") LocalDate date, @Param("today") LocalDate today);

    void deleteByOperativeId(Long operativeId);
}

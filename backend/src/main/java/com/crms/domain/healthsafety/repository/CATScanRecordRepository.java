package com.crms.domain.healthsafety.repository;

import com.crms.domain.healthsafety.entity.CATScanRecord;
import com.crms.domain.healthsafety.enums.UtilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CATScanRecordRepository extends JpaRepository<CATScanRecord, Long> {

    List<CATScanRecord> findByPermitId(Long permitId);

    List<CATScanRecord> findByUtility(UtilityType utility);
}

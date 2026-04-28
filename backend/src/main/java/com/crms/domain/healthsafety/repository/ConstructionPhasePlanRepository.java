package com.crms.domain.healthsafety.repository;

import com.crms.domain.healthsafety.entity.ConstructionPhasePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConstructionPhasePlanRepository extends JpaRepository<ConstructionPhasePlan, Long> {

    List<ConstructionPhasePlan> findByContractId(Long contractId);

    Optional<ConstructionPhasePlan> findTopByContractIdOrderByVersionDesc(Long contractId);
}

package com.crms.domain.contract.repository;

import com.crms.domain.contract.entity.PaymentNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentNoticeRepository extends JpaRepository<PaymentNotice, Long> {

    List<PaymentNotice> findByApplicationId(Long applicationId);
}

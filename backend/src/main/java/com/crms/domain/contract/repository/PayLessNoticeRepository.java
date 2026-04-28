package com.crms.domain.contract.repository;

import com.crms.domain.contract.entity.PayLessNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayLessNoticeRepository extends JpaRepository<PayLessNotice, Long> {
}

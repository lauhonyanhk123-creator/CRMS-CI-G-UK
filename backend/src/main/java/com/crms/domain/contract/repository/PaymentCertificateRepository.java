package com.crms.domain.contract.repository;

import com.crms.domain.contract.entity.PaymentCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentCertificateRepository extends JpaRepository<PaymentCertificate, Long> {

    Optional<PaymentCertificate> findByCertificateNumber(String certificateNumber);
}

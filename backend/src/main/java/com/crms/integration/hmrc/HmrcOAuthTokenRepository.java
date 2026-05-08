package com.crms.integration.hmrc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HmrcOAuthTokenRepository extends JpaRepository<HmrcOAuthToken, Long> {

    Optional<HmrcOAuthToken> findByContractorUtr(String contractorUtr);

    void deleteByContractorUtr(String contractorUtr);
}

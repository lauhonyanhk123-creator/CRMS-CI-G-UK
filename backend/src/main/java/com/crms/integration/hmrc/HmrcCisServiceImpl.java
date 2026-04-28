package com.crms.integration.hmrc;

import com.crms.integration.config.IntegrationProperties;
import com.crms.integration.dto.HmrcCisDeductionRateResponse;
import com.crms.integration.dto.HmrcCisSubmitResponse;
import com.crms.integration.dto.HmrcCisSubmitResponse.SubmissionStatus;
import com.crms.integration.dto.HmrcCisVerificationResponse;
import com.crms.integration.dto.HmrcCisVerificationResponse.CisVerificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * HMRC CIS (Construction Industry Scheme) Service implementation.
 * Handles OAuth2 authentication and provides mock/demo mode.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HmrcCisServiceImpl implements HmrcCisService {

    private final RestTemplate hmrcRestTemplate;
    private final IntegrationProperties properties;

    private HmrcOAuth2Token cachedToken;

    // Demo mode data - simulates HMRC responses
    private static final Map<String, DemoVerificationData> DEMO_VERIFICATIONS = new HashMap<>();
    private static final List<String> DEMO_VALID_UTRS = Arrays.asList(
            "1234567890", "0987654321", "1112223339"
    );
    private static final BigDecimal DEMO_GRS_RATE = new BigDecimal("0"); // Gross rate (no deduction)
    private static final BigDecimal DEMO_NET_RATE = new BigDecimal("20"); // 20% deduction

    static {
        // Pre-populate demo verifications
        DEMO_VERIFICATIONS.put("1234567890", DemoVerificationData.builder()
                .utr("1234567890")
                .companyName("DEMO CONSTRUCTION LTD")
                .result(CisVerificationResult.VERIFIED)
                .deductionRate(DEMO_GRS_RATE)
                .verifiedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build());

        DEMO_VERIFICATIONS.put("0987654321", DemoVerificationData.builder()
                .utr("0987654321")
                .companyName("DEMO SUBCONTRACTOR LTD")
                .result(CisVerificationResult.VERIFIED)
                .deductionRate(DEMO_NET_RATE)
                .verifiedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build());

        DEMO_VERIFICATIONS.put("1112223339", DemoVerificationData.builder()
                .utr("1112223339")
                .companyName("DEMO UNVERIFIED LTD")
                .result(CisVerificationResult.NOT_VERIFIED)
                .deductionRate(new BigDecimal("30"))
                .verifiedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build());
    }

    @Override
    public HmrcCisVerificationResponse verifySubcontractor(String utr) {
        log.info("Verifying subcontractor with UTR: {}", utr);

        if (isDemoMode()) {
            return mockVerifySubcontractor(utr);
        }

        try {
            String accessToken = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HMRC CIS verification endpoint
            String url = properties.getHmrc().getBaseUrl() +
                    "/organisations/constructive-industry-scheme/subcontractors/" + utr + "/verification";

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = hmrcRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapToVerificationResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("HMRC API error during verification: {}", e.getMessage());
            return createErrorVerification(utr, "HMRC_API_ERROR: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to verify subcontractor: {}", e.getMessage());
            return createErrorVerification(utr, "VERIFICATION_FAILED: " + e.getMessage());
        }
    }

    @Override
    public HmrcCisDeductionRateResponse getDeductionPercentage(String supplierUtr, String contractorUtr) {
        log.info("Getting deduction percentage for supplier {} / contractor {}", supplierUtr, contractorUtr);

        if (isDemoMode()) {
            return mockGetDeductionPercentage(supplierUtr, contractorUtr);
        }

        try {
            String accessToken = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = properties.getHmrc().getBaseUrl() +
                    "/organisations/constructive-industry-scheme/contractors/" + contractorUtr +
                    "/subcontractors/" + supplierUtr + "/deduction-rate";

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = hmrcRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapToDeductionResponse(response.getBody(), supplierUtr, contractorUtr);
        } catch (Exception e) {
            log.error("Failed to get deduction percentage: {}", e.getMessage());
            return createErrorDeductionResponse(supplierUtr, contractorUtr, e.getMessage());
        }
    }

    @Override
    public HmrcCisSubmitResponse submitMonthlyReturn(CisReturnDto cisReturn) {
        log.info("Submitting CIS return for tax month: {}", cisReturn.getTaxMonth());

        if (isDemoMode()) {
            return mockSubmitMonthlyReturn(cisReturn);
        }

        try {
            String accessToken = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = properties.getHmrc().getBaseUrl() +
                    "/organisations/constructive-industry-scheme/contractors/" + cisReturn.getContractorUtr() +
                    "/monthly-returns";

            HttpEntity<CisReturnDto> entity = new HttpEntity<>(cisReturn, headers);
            ResponseEntity<Map> response = hmrcRestTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            return mapToSubmitResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("HMRC API error during submission: {}", e.getMessage());
            return createErrorSubmitResponse(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to submit CIS return: {}", e.getMessage());
            return createErrorSubmitResponse(e.getMessage());
        }
    }

    @Override
    public BigDecimal calculateDeduction(BigDecimal grossAmount, BigDecimal cisRate) {
        if (grossAmount == null || cisRate == null) {
            return BigDecimal.ZERO;
        }
        
        // CIS deduction = gross amount * (rate / 100)
        BigDecimal rate = cisRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal deduction = grossAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        
        log.debug("Calculated CIS deduction: {} * {}% = {}", grossAmount, cisRate, deduction);
        return deduction;
    }

    @Override
    public boolean isDemoMode() {
        return properties.isDemoMode() || 
               properties.getHmrc().getClientId() == null || 
               properties.getHmrc().getClientId().isEmpty();
    }

    // ==================== OAuth2 Authentication ====================

    private synchronized String getAccessToken() {
        if (cachedToken != null && !cachedToken.isExpired()) {
            return cachedToken.getAccessToken();
        }

        log.info("Obtaining new OAuth2 access token from HMRC");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials" +
                "&client_id=" + properties.getHmrc().getClientId() +
                "&client_secret=" + properties.getHmrc().getClientSecret() +
                "&scope=" + "read:constructive-industry-scheme write:constructive-industry-scheme";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = hmrcRestTemplate.postForEntity(
                "https://api.service.hmrc.gov.uk/oauth/token",
                entity,
                Map.class
        );

        Map<String, Object> tokenData = response.getBody();
        cachedToken = HmrcOAuth2Token.builder()
                .accessToken((String) tokenData.get("access_token"))
                .tokenType((String) tokenData.getOrDefault("token_type", "Bearer"))
                .expiresIn(((Number) tokenData.get("expires_in")).longValue())
                .issuedAt(Instant.now())
                .build();

        return cachedToken.getAccessToken();
    }

    // ==================== Mock/Demo Methods ====================

    private HmrcCisVerificationResponse mockVerifySubcontractor(String utr) {
        log.info("[DEMO MODE] Verifying subcontractor: {}", utr);

        if (!isValidUtr(utr)) {
            return HmrcCisVerificationResponse.builder()
                    .verificationRef("DEMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .utr(utr)
                    .result(CisVerificationResult.UTR_NOT_FOUND)
                    .verifiedAt(LocalDate.now())
                    .expiresAt(LocalDate.now().plusMonths(1))
                    .build();
        }

        DemoVerificationData demo = DEMO_VERIFICATIONS.get(utr);
        if (demo == null) {
            demo = DEMO_VERIFICATIONS.get("1112223339"); // Default to unverified
        }

        return HmrcCisVerificationResponse.builder()
                .verificationRef("DEMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .utr(utr)
                .companyName(demo.getCompanyName())
                .result(demo.getResult())
                .deductionRate(demo.getDeductionRate())
                .verifiedAt(demo.getVerifiedAt())
                .expiresAt(demo.getExpiresAt())
                .build();
    }

    private HmrcCisDeductionRateResponse mockGetDeductionPercentage(String supplierUtr, String contractorUtr) {
        log.info("[DEMO MODE] Getting deduction rate for {} / {}", supplierUtr, contractorUtr);

        CisVerificationResult result = CisVerificationResult.VERIFIED;
        BigDecimal rate = DEMO_GRS_RATE;
        String rateType = "GRS";
        String basis = "Contractor is verified CIS member";

        if (!DEMO_VALID_UTRS.contains(supplierUtr)) {
            result = CisVerificationResult.UTR_NOT_FOUND;
            rate = new BigDecimal("30");
            rateType = "STANDARD";
            basis = "Subcontractor UTR not found - standard 30% rate applied";
        }

        return HmrcCisDeductionRateResponse.builder()
                .supplierUtr(supplierUtr)
                .contractorUtr(contractorUtr)
                .deductionRate(rate)
                .rateType(rateType)
                .applicable(true)
                .calculationBasis(basis)
                .build();
    }

    private HmrcCisSubmitResponse mockSubmitMonthlyReturn(CisReturnDto cisReturn) {
        log.info("[DEMO MODE] Submitting CIS return for month: {}", cisReturn.getTaxMonth());

        String receiptRef = "DEMO-RCP-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        return HmrcCisSubmitResponse.builder()
                .receiptRef(receiptRef)
                .acknowledgementRef("DEMO-ACK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .submittedAt(java.time.LocalDateTime.now())
                .status(SubmissionStatus.ACCEPTED)
                .submissionMessages("Demo submission accepted. In live mode, HMRC would process this return.")
                .build();
    }

    // ==================== Helper Methods ====================

    private boolean isValidUtr(String utr) {
        if (utr == null || utr.length() != 10) {
            return false;
        }
        return utr.matches("\\d{10}");
    }

    private HmrcCisVerificationResponse createErrorVerification(String utr, String error) {
        return HmrcCisVerificationResponse.builder()
                .verificationRef("ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .utr(utr)
                .result(CisVerificationResult.NOT_VERIFIED)
                .verifiedAt(LocalDate.now())
                .expiresAt(LocalDate.now())
                .build();
    }

    private HmrcCisDeductionRateResponse createErrorDeductionResponse(String supplierUtr, String contractorUtr, String error) {
        return HmrcCisDeductionRateResponse.builder()
                .supplierUtr(supplierUtr)
                .contractorUtr(contractorUtr)
                .deductionRate(new BigDecimal("30"))
                .rateType("STANDARD")
                .applicable(true)
                .calculationBasis("Error occurred - default rate applied: " + error)
                .build();
    }

    private HmrcCisSubmitResponse createErrorSubmitResponse(String error) {
        return HmrcCisSubmitResponse.builder()
                .receiptRef("ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .submittedAt(java.time.LocalDateTime.now())
                .status(SubmissionStatus.FAILED)
                .submissionMessages("Submission failed: " + error)
                .build();
    }

    private HmrcCisVerificationResponse mapToVerificationResponse(Map<String, Object> body) {
        // Map real HMRC response to our DTO
        return HmrcCisVerificationResponse.builder()
                .verificationRef((String) body.get("verificationReference"))
                .utr((String) body.get("subcontractorUtr"))
                .companyName((String) body.getOrDefault("subcontractorName", body.get("name")))
                .deductionRate(new BigDecimal(body.getOrDefault("deductionRate", "20").toString()))
                .result(CisVerificationResult.VERIFIED)
                .verifiedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build();
    }

    private HmrcCisDeductionRateResponse mapToDeductionResponse(Map<String, Object> body, String supplierUtr, String contractorUtr) {
        String rateType = (String) body.getOrDefault("rateType", "STANDARD");
        BigDecimal rate;
        
        if ("GRS".equals(rateType)) {
            rate = BigDecimal.ZERO;
        } else if ("NET".equals(rateType)) {
            rate = new BigDecimal("20");
        } else {
            rate = new BigDecimal(body.getOrDefault("deductionRate", "20").toString());
        }

        return HmrcCisDeductionRateResponse.builder()
                .supplierUtr(supplierUtr)
                .contractorUtr(contractorUtr)
                .deductionRate(rate)
                .rateType(rateType)
                .applicable(true)
                .calculationBasis((String) body.getOrDefault("calculationBasis", "Standard CIS deduction"))
                .build();
    }

    private HmrcCisSubmitResponse mapToSubmitResponse(Map<String, Object> body) {
        return HmrcCisSubmitResponse.builder()
                .receiptRef((String) body.get("receiptReference"))
                .acknowledgementRef((String) body.get("acknowledgementReference"))
                .submittedAt(java.time.LocalDateTime.now())
                .status(SubmissionStatus.ACCEPTED)
                .submissionMessages((String) body.getOrDefault("messages", "Return accepted"))
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class DemoVerificationData {
        private String utr;
        private String companyName;
        private CisVerificationResult result;
        private BigDecimal deductionRate;
        private LocalDate verifiedAt;
        private LocalDate expiresAt;
    }
}

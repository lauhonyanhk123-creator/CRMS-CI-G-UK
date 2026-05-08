package com.crms.integration.hmrc;

import com.crms.integration.cache.IntegrationCacheService;
import com.crms.integration.config.IntegrationProperties;
import com.crms.integration.dto.HmrcCisDeductionRateResponse;
import com.crms.integration.dto.HmrcCisSubmitResponse;
import com.crms.integration.dto.HmrcCisSubmitResponse.SubmissionStatus;
import com.crms.integration.dto.HmrcCisVerificationResponse;
import com.crms.integration.dto.HmrcCisVerificationResponse.CisVerificationResult;
import com.crms.integration.monitoring.NetworkMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final NetworkMonitor networkMonitor;
    private final IntegrationCacheService cacheService;
    private final HmrcOAuthService hmrcOAuthService;

    private final AtomicInteger tokenRefreshAttempts = new AtomicInteger(0);
    private static final int MAX_TOKEN_REFRESH_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 1000;

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

        // Check if HMRC is reachable
        if (!networkMonitor.isHmrcReachable()) {
            log.warn("HMRC service is not reachable. Attempting to use cached data for UTR: {}", utr);
            HmrcCisVerificationResponse cached = cacheService.getCachedCisVerification(utr);
            if (cached != null) {
                log.info("Returning cached CIS verification for UTR: {}. Note: Data may be stale.", utr);
                return HmrcCisVerificationResponse.builder()
                        .verificationRef(cached.getVerificationRef())
                        .utr(cached.getUtr())
                        .companyName(cached.getCompanyName())
                        .result(cached.getResult())
                        .deductionRate(cached.getDeductionRate())
                        .verifiedAt(cached.getVerifiedAt())
                        .expiresAt(cached.getExpiresAt())
                        .offlineData(true)
                        .build();
            }
            throw new HmrcOfflineException("HMRC service is unavailable and no cached data exists for UTR: " + utr);
        }

        try {
            return executeWithTokenRefresh(() -> {
                String accessToken = getValidAccessToken();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                // HMRC CIS verification endpoint
                String url = properties.getHmrc().getBaseUrl() +
                        "/organisations/construction-industry-scheme/subcontractors/" + utr + "/verification";

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = hmrcRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

                HmrcCisVerificationResponse verificationResponse = mapToVerificationResponse(response.getBody());
                // Cache successful response
                cacheService.cacheCisVerification(utr, verificationResponse);
                return verificationResponse;
            }, utr, "verify");
        } catch (ResourceAccessException e) {
            log.warn("Network error connecting to HMRC: {}. Attempting cached data.", e.getMessage());
            return handleNetworkErrorWithCache(utr, e);
        } catch (HmrcAuthenticationException | HmrcApiException e) {
            log.error("HMRC API error for verification: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Handle network error by attempting to return cached data.
     */
    private HmrcCisVerificationResponse handleNetworkErrorWithCache(String utr, ResourceAccessException e) {
        HmrcCisVerificationResponse cached = cacheService.getCachedCisVerification(utr);
        if (cached != null) {
            log.warn("Returning stale cached CIS verification due to network error. UTR: {}", utr);
            return HmrcCisVerificationResponse.builder()
                    .verificationRef(cached.getVerificationRef())
                    .utr(cached.getUtr())
                    .companyName(cached.getCompanyName())
                    .result(cached.getResult())
                    .deductionRate(cached.getDeductionRate())
                    .verifiedAt(cached.getVerifiedAt())
                    .expiresAt(cached.getExpiresAt())
                    .offlineData(true)
                    .build();
        }
        throw new HmrcOfflineException("Network error and no cached data available for UTR: " + utr, e);
    }

    @Override
    public HmrcCisDeductionRateResponse getDeductionPercentage(String supplierUtr, String contractorUtr) {
        log.info("Getting deduction percentage for supplier {} / contractor {}", supplierUtr, contractorUtr);

        if (isDemoMode()) {
            return mockGetDeductionPercentage(supplierUtr, contractorUtr);
        }

        // Check if HMRC is reachable
        if (!networkMonitor.isHmrcReachable()) {
            log.warn("HMRC service is not reachable. Attempting to use cached data for {}/{}", supplierUtr, contractorUtr);
            HmrcCisDeductionRateResponse cached = cacheService.getCachedCisDeductionRate(supplierUtr, contractorUtr);
            if (cached != null) {
                log.info("Returning cached CIS deduction rate. Note: Data may be stale.");
                return HmrcCisDeductionRateResponse.builder()
                        .supplierUtr(cached.getSupplierUtr())
                        .contractorUtr(cached.getContractorUtr())
                        .deductionRate(cached.getDeductionRate())
                        .rateType(cached.getRateType())
                        .applicable(cached.isApplicable())
                        .calculationBasis(cached.getCalculationBasis() + " [OFFLINE DATA]")
                        .build();
            }
            throw new HmrcOfflineException("HMRC service is unavailable and no cached deduction rate for " + supplierUtr + "/" + contractorUtr);
        }

        try {
            return executeWithTokenRefresh(() -> {
                String accessToken = getValidAccessToken();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                String url = properties.getHmrc().getBaseUrl() +
                        "/organisations/construction-industry-scheme/contractors/" + contractorUtr +
                        "/subcontractors/" + supplierUtr + "/deduction-rate";

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = hmrcRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

                HmrcCisDeductionRateResponse deductionResponse = mapToDeductionResponse(response.getBody(), supplierUtr, contractorUtr);
                // Cache successful response
                cacheService.cacheCisDeductionRate(supplierUtr, contractorUtr, deductionResponse);
                return deductionResponse;
            }, supplierUtr + "/" + contractorUtr, "deduction");
        } catch (ResourceAccessException e) {
            log.warn("Network error connecting to HMRC: {}. Attempting cached data.", e.getMessage());
            return handleNetworkErrorWithCache(supplierUtr, contractorUtr, e);
        } catch (HmrcAuthenticationException | HmrcApiException e) {
            log.error("HMRC API error for deduction rate: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Handle network error for deduction rate by attempting to return cached data.
     */
    private HmrcCisDeductionRateResponse handleNetworkErrorWithCache(String supplierUtr, String contractorUtr, ResourceAccessException e) {
        HmrcCisDeductionRateResponse cached = cacheService.getCachedCisDeductionRate(supplierUtr, contractorUtr);
        if (cached != null) {
            log.warn("Returning stale cached CIS deduction rate due to network error. {}/{}", supplierUtr, contractorUtr);
            return HmrcCisDeductionRateResponse.builder()
                    .supplierUtr(cached.getSupplierUtr())
                    .contractorUtr(cached.getContractorUtr())
                    .deductionRate(cached.getDeductionRate())
                    .rateType(cached.getRateType())
                    .applicable(cached.isApplicable())
                    .calculationBasis(cached.getCalculationBasis() + " [OFFLINE - STALE DATA]")
                    .build();
        }
        throw new HmrcOfflineException("Network error and no cached deduction rate for " + supplierUtr + "/" + contractorUtr, e);
    }

    @Override
    public HmrcCisSubmitResponse submitMonthlyReturn(CisReturnDto cisReturn) {
        log.info("Submitting CIS return for tax month: {}", cisReturn.getTaxMonth());

        if (isDemoMode()) {
            return mockSubmitMonthlyReturn(cisReturn);
        }

        // Check if HMRC is reachable
        if (!networkMonitor.isHmrcReachable()) {
            log.error("Cannot submit CIS return - HMRC service is unavailable");
            throw new HmrcOfflineException("HMRC service is unavailable. Cannot submit CIS return for month: " + cisReturn.getTaxMonth());
        }

        try {
            return executeWithTokenRefresh(() -> {
                String accessToken = getValidAccessToken();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                String url = properties.getHmrc().getBaseUrl() +
                        "/organisations/construction-industry-scheme/contractors/" + cisReturn.getContractorUtr() +
                        "/monthly-returns";

                HttpEntity<CisReturnDto> entity = new HttpEntity<>(cisReturn, headers);
                ResponseEntity<Map> response = hmrcRestTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

                return mapToSubmitResponse(response.getBody());
            }, cisReturn.getContractorUtr(), "submit");
        } catch (ResourceAccessException e) {
            log.error("Network error submitting CIS return: {}. Submissions cannot be completed offline.", e.getMessage());
            throw new HmrcOfflineException("Network error submitting CIS return. Please try again when connectivity is restored.", e);
        } catch (HmrcAuthenticationException | HmrcApiException e) {
            log.error("HMRC API error submitting CIS return: {}", e.getMessage());
            throw e;
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

    /**
     * Get a valid access token via the persistent authorization code flow.
     * Delegates to HmrcOAuthService which handles refresh automatically.
     */
    private String getValidAccessToken() {
        String contractorUtr = properties.getHmrc().getContractorUtr();
        if (contractorUtr == null || contractorUtr.isBlank()) {
            throw new HmrcAuthenticationException(
                    "HMRC_CONTRACTOR_UTR is not configured. Set the environment variable and re-authorise via /api/v1/hmrc/oauth/begin");
        }
        return hmrcOAuthService.getValidAccessToken(contractorUtr);
    }

    /**
     * Execute an HMRC API call with automatic token refresh on 401 errors.
     * Implements retry logic with exponential backoff.
     * 
     * @param operation the API operation to execute
     * @param identifier identifier for logging (e.g., UTR)
     * @param operationType type of operation for logging
     * @return the result of the operation
     */
    private <T> T executeWithTokenRefresh(TokenOperation<T> operation, String identifier, String operationType) {
        int attempts = 0;
        int maxAttempts = MAX_TOKEN_REFRESH_ATTEMPTS;
        
        while (attempts < maxAttempts) {
            attempts++;
            try {
                return operation.execute();
                
            } catch (HttpClientErrorException e) {
                // Handle 401 Unauthorized - token may have expired
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    log.warn("Received 401 Unauthorized for {} operation ({}). Attempt {}/{}", 
                            operationType, identifier, attempts, maxAttempts);
                    
                    if (attempts < maxAttempts) {
                        log.info("Token may be stale — retrying, HmrcOAuthService will refresh");
                        
                        // Brief delay before retry
                        try {
                            Thread.sleep(RETRY_DELAY_MS * attempts);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                    
                    log.error("Token refresh exhausted after {} attempts for {} operation", maxAttempts, operationType);
                    throw new HmrcAuthenticationException(
                            "Token refresh exhausted after " + maxAttempts + " attempts: " + e.getMessage(), e);
                }
                
                // Other client errors (4xx) - don't retry
                log.error("HMRC API client error for {} operation: {} - {}", 
                        operationType, e.getStatusCode(), e.getMessage());
                throw e;
                
            } catch (HmrcAuthenticationException e) {
                // Authentication errors should bubble up
                log.error("HMRC authentication error for {} operation: {}", operationType, e.getMessage());
                throw e;
                
            } catch (HttpServerErrorException e) {
                // 5xx errors - may be temporary, retry with backoff
                log.warn("HMRC API server error for {} operation: {} - {}. Attempt {}/{}", 
                        operationType, e.getStatusCode(), e.getMessage(), attempts, maxAttempts);
                
                if (attempts < maxAttempts) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts * 2);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                
                log.error("Server error retry exhausted after {} attempts for {} operation", maxAttempts, operationType);
                throw new HmrcApiException("HMRC API server error after " + maxAttempts + " attempts: " + e.getMessage(), e);
                
            } catch (ResourceAccessException e) {
                // Network errors - retry with backoff
                log.warn("Network error for {} operation: {}. Attempt {}/{}", 
                        operationType, e.getMessage(), attempts, maxAttempts);
                
                if (attempts < maxAttempts) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }
                
                log.error("Network error retry exhausted after {} attempts for {} operation", maxAttempts, operationType);
                throw new HmrcApiException("Network error after " + maxAttempts + " attempts: " + e.getMessage(), e);
            }
        }
        
        // Should not reach here, but return error response if we do
        throw new HmrcApiException("Operation failed after " + maxAttempts + " attempts");
    }

    @FunctionalInterface
    private interface TokenOperation<T> {
        T execute();
    }

    // ==================== Custom Exceptions ====================

    /**
     * Exception for HMRC authentication failures.
     */
    public static class HmrcAuthenticationException extends RuntimeException {
        public HmrcAuthenticationException(String message) {
            super(message);
        }
        public HmrcAuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception for HMRC API errors.
     */
    public static class HmrcApiException extends RuntimeException {
        public HmrcApiException(String message) {
            super(message);
        }
        public HmrcApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception for HMRC service being offline/unavailable.
     * Used for graceful degradation - thrown when service is unavailable
     * and no cached data exists.
     */
    public static class HmrcOfflineException extends RuntimeException {
        private final boolean hasCachedData;
        
        public HmrcOfflineException(String message) {
            super(message);
            this.hasCachedData = false;
        }
        
        public HmrcOfflineException(String message, Throwable cause) {
            super(message, cause);
            this.hasCachedData = false;
        }
        
        public boolean hasCachedData() {
            return hasCachedData;
        }
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

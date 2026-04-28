package com.crms.integration.cscs;

import com.crms.integration.config.IntegrationProperties;
import com.crms.integration.dto.CscsCardVerificationResponse;
import com.crms.integration.dto.CscsCardVerificationResponse.CardStatus;
import com.crms.integration.dto.CscsCardVerificationResponse.QualificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * CSCS Smart Check API Service implementation.
 * Handles card verification and qualification checking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CscsSmartCheckServiceImpl implements CscsSmartCheckService {

    private final RestTemplate cscsRestTemplate;
    private final IntegrationProperties properties;

    // Demo mode data - simulates CSCS Smart Check responses
    private static final Map<String, DemoCardData> DEMO_CARDS = new HashMap<>();

    static {
        DEMO_CARDS.put("CSCS-001-2024-ABC1234567", DemoCardData.builder()
                .cardNumber("CSCS-001-2024-ABC1234567")
                .cardHolderName("JOHN SMITH")
                .schemeName("CSCS")
                .cardType("SKILLED WORKER")
                .occupation("Carpenter")
                .competencyRef("CSC-CARP-2024-001")
                .expiryDate(LocalDate.of(2027, 6, 30))
                .qualifications(Arrays.asList(
                        QualificationDto.builder()
                                .name("NVQ Level 2 in Carpentry and Joinery")
                                .level("Level 2")
                                .expiryDate(LocalDate.of(2028, 12, 31))
                                .valid(true)
                                .build(),
                        QualificationDto.builder()
                                .name("CSCS Health & Safety Test")
                                .level("CITB HS&E")
                                .expiryDate(LocalDate.of(2026, 6, 30))
                                .valid(true)
                                .build()
                ))
                .build());

        DEMO_CARDS.put("CSCS-002-2023-XYZ9876543", DemoCardData.builder()
                .cardNumber("CSCS-002-2023-XYZ9876543")
                .cardHolderName("JANE DOE")
                .schemeName("CSCS")
                .cardType("APPRENTICE")
                .occupation("Bricklayer")
                .competencyRef("CSC-BRICK-2023-002")
                .expiryDate(LocalDate.of(2026, 3, 31))
                .qualifications(Arrays.asList(
                        QualificationDto.builder()
                                .name("NVQ Level 2 in Trowel Occupations")
                                .level("Level 2")
                                .expiryDate(LocalDate.of(2027, 12, 31))
                                .valid(true)
                                .build(),
                        QualificationDto.builder()
                                .name("CSCS Health & Safety Test")
                                .level("CITB HS&E")
                                .expiryDate(LocalDate.of(2025, 3, 31))
                                .valid(false)
                                .build()
                ))
                .build());

        DEMO_CARDS.put("CSCS-003-2022-EXPD123456", DemoCardData.builder()
                .cardNumber("CSCS-003-2022-EXPD123456")
                .cardHolderName("EXPIRED WORKER")
                .schemeName("CSCS")
                .cardType("SKILLED WORKER")
                .occupation("Plumber")
                .competencyRef("CSC-PLUM-2022-003")
                .expiryDate(LocalDate.of(2024, 12, 31))
                .qualifications(Arrays.asList(
                        QualificationDto.builder()
                                .name("NVQ Level 3 in Plumbing")
                                .level("Level 3")
                                .expiryDate(LocalDate.of(2025, 12, 31))
                                .valid(false)
                                .build()
                ))
                .build());
    }

    @Override
    public CscsCardVerificationResponse verifyCard(String cardNumber) {
        log.info("Verifying CSCS card: {}", cardNumber);

        if (isDemoMode()) {
            return mockVerifyCard(cardNumber);
        }

        try {
            String accessToken = getAccessToken();
            HttpHeaders headers = createCscsHeaders(accessToken);

            String url = properties.getCscs().getBaseUrl() + "/smartcheck/v1/cards/" + cardNumber + "/verify";
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = cscsRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapToVerificationResponse(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Card not found: {}", cardNumber);
            return createNotFoundResponse(cardNumber);
        } catch (Exception e) {
            log.error("Failed to verify card: {}", e.getMessage());
            return createErrorResponse(cardNumber, e.getMessage());
        }
    }

    @Override
    public boolean checkCardExpiry(String cardNumber) {
        log.info("Checking card expiry: {}", cardNumber);

        CscsCardVerificationResponse response = getCardDetails(cardNumber);
        return response.isExpired();
    }

    @Override
    public CscsCardVerificationResponse getCardDetails(String cardNumber) {
        log.info("Getting card details: {}", cardNumber);

        if (isDemoMode()) {
            return mockVerifyCard(cardNumber);
        }

        try {
            String accessToken = getAccessToken();
            HttpHeaders headers = createCscsHeaders(accessToken);

            String url = properties.getCscs().getBaseUrl() + "/smartcheck/v1/cards/" + cardNumber;
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = cscsRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapToVerificationResponse(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get card details: {}", e.getMessage());
            return createErrorResponse(cardNumber, e.getMessage());
        }
    }

    @Override
    public List<QualificationDto> checkOperativeQualifications(String operativeId) {
        log.info("Checking operative qualifications: {}", operativeId);

        if (isDemoMode()) {
            return mockCheckOperativeQualifications(operativeId);
        }

        try {
            String accessToken = getAccessToken();
            HttpHeaders headers = createCscsHeaders(accessToken);

            String url = properties.getCscs().getBaseUrl() + "/smartcheck/v1/operatives/" + operativeId + "/qualifications";
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = cscsRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapQualifications(response.getBody());
        } catch (Exception e) {
            log.error("Failed to check qualifications: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isDemoMode() {
        return properties.isDemoMode() ||
               properties.getCscs().getApiKey() == null ||
               properties.getCscs().getApiKey().isEmpty();
    }

    // ==================== Authentication ====================

    private String accessToken;
    private Instant tokenExpiry;

    private synchronized String getAccessToken() {
        if (accessToken != null && tokenExpiry != null && tokenExpiry.isAfter(Instant.now())) {
            return accessToken;
        }

        log.info("Obtaining CSCS API access token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials" +
                "&client_id=" + properties.getCscs().getClientId() +
                "&client_secret=" + properties.getCscs().getClientSecret() +
                "&scope=" + "smartcheck:read";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = cscsRestTemplate.postForEntity(
                properties.getCscs().getBaseUrl() + "/oauth/token",
                entity,
                Map.class
        );

        Map<String, Object> tokenData = response.getBody();
        this.accessToken = (String) tokenData.get("access_token");
        this.tokenExpiry = Instant.now().plusSeconds(((Number) tokenData.get("expires_in")).longValue());

        return this.accessToken;
    }

    private HttpHeaders createCscsHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    // ==================== Mock Methods ====================

    private CscsCardVerificationResponse mockVerifyCard(String cardNumber) {
        log.info("[DEMO MODE] Verifying CSCS card: {}", cardNumber);

        DemoCardData demo = DEMO_CARDS.get(cardNumber);
        if (demo == null) {
            // Generate a generic valid card for testing
            return CscsCardVerificationResponse.builder()
                    .cardNumber(cardNumber)
                    .verified(true)
                    .expired(false)
                    .status(CardStatus.VALID)
                    .schemeName("CSCS")
                    .cardHolderName("DEMO OPERATIVE")
                    .cardExpiryDate(LocalDate.now().plusYears(2))
                    .occupation("Construction Operative")
                    .competencyRef("DEMO-REF-" + cardNumber.substring(0, 8))
                    .qualifications(Arrays.asList(
                            QualificationDto.builder()
                                    .name("CSCS Health & Safety Test")
                                    .level("CITB HS&E")
                                    .expiryDate(LocalDate.now().plusYears(1))
                                    .valid(true)
                                    .build()
                    ))
                    .build();
        }

        LocalDate now = LocalDate.now();
        boolean isExpired = demo.getExpiryDate().isBefore(now);
        boolean hasValidQuals = demo.getQualifications().stream()
                .anyMatch(q -> q.isValid() && q.getExpiryDate() != null && q.getExpiryDate().isAfter(now));

        CardStatus status = isExpired ? CardStatus.EXPIRED :
                !hasValidQuals ? CardStatus.PENDING_RENEWAL : CardStatus.VALID;

        return CscsCardVerificationResponse.builder()
                .cardNumber(demo.getCardNumber())
                .verified(!isExpired)
                .expired(isExpired)
                .status(status)
                .schemeName(demo.getSchemeName())
                .cardHolderName(demo.getCardHolderName())
                .cardExpiryDate(demo.getExpiryDate())
                .occupation(demo.getOccupation())
                .competencyRef(demo.getCompetencyRef())
                .qualifications(demo.getQualifications())
                .build();
    }

    private List<QualificationDto> mockCheckOperativeQualifications(String operativeId) {
        log.info("[DEMO MODE] Checking qualifications for operative: {}", operativeId);

        // Return default qualifications for any operative ID
        return Arrays.asList(
                QualificationDto.builder()
                        .name("CSCS Health & Safety Test")
                        .level("CITB HS&E")
                        .expiryDate(LocalDate.now().plusYears(1))
                        .valid(true)
                        .build(),
                QualificationDto.builder()
                        .name("NVQ Level 2 in Construction Operations")
                        .level("Level 2")
                        .expiryDate(LocalDate.now().plusYears(2))
                        .valid(true)
                        .build()
        );
    }

    // ==================== Mapping Methods ====================

    private CscsCardVerificationResponse mapToVerificationResponse(Map<String, Object> body) {
        if (body == null) {
            return createErrorResponse("UNKNOWN", "Empty response from CSCS API");
        }

        LocalDate expiryDate = parseDate((String) body.get("expiry_date"));
        LocalDate now = LocalDate.now();
        boolean isExpired = expiryDate != null && expiryDate.isBefore(now);

        String statusStr = (String) body.getOrDefault("card_status", "VALID");
        CardStatus status = parseCardStatus(statusStr, isExpired);

        List<QualificationDto> qualifications = mapQualifications(body);

        return CscsCardVerificationResponse.builder()
                .cardNumber((String) body.get("card_number"))
                .verified(!isExpired && status != CardStatus.REVOKED)
                .expired(isExpired)
                .status(status)
                .schemeName((String) body.getOrDefault("scheme_name", "CSCS"))
                .cardHolderName((String) body.get("card_holder_name"))
                .cardExpiryDate(expiryDate)
                .occupation((String) body.get("occupation"))
                .competencyRef((String) body.get("competency_ref"))
                .qualifications(qualifications)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<QualificationDto> mapQualifications(Map<String, Object> body) {
        List<QualificationDto> qualifications = new ArrayList<>();

        if (body == null || !body.containsKey("qualifications")) {
            return qualifications;
        }

        try {
            List<Map<String, Object>> quals = (List<Map<String, Object>>) body.get("qualifications");
            for (Map<String, Object> q : quals) {
                LocalDate expiryDate = parseDate((String) q.get("expiry_date"));
                boolean isValid = expiryDate != null && expiryDate.isAfter(LocalDate.now());

                qualifications.add(QualificationDto.builder()
                        .name((String) q.get("name"))
                        .level((String) q.get("level"))
                        .expiryDate(expiryDate)
                        .valid(isValid)
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to map qualifications: {}", e.getMessage());
        }

        return qualifications;
    }

    private CardStatus parseCardStatus(String status, boolean isExpired) {
        if (isExpired) return CardStatus.EXPIRED;
        
        switch (status.toUpperCase()) {
            case "VALID": return CardStatus.VALID;
            case "EXPIRED": return CardStatus.EXPIRED;
            case "REVOKED": return CardStatus.REVOKED;
            case "PENDING_RENEWAL": return CardStatus.PENDING_RENEWAL;
            default: return CardStatus.VALID;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ISO_DATE);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private CscsCardVerificationResponse createNotFoundResponse(String cardNumber) {
        return CscsCardVerificationResponse.builder()
                .cardNumber(cardNumber)
                .verified(false)
                .expired(false)
                .status(CardStatus.NOT_FOUND)
                .schemeName("CSCS")
                .qualifications(Collections.emptyList())
                .build();
    }

    private CscsCardVerificationResponse createErrorResponse(String cardNumber, String error) {
        return CscsCardVerificationResponse.builder()
                .cardNumber(cardNumber)
                .verified(false)
                .expired(false)
                .status(CardStatus.NOT_FOUND)
                .schemeName("CSCS")
                .qualifications(Collections.emptyList())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class DemoCardData {
        private String cardNumber;
        private String cardHolderName;
        private String schemeName;
        private String cardType;
        private String occupation;
        private String competencyRef;
        private LocalDate expiryDate;
        private List<QualificationDto> qualifications;
    }
}

package com.crms.integration.hmrc;

import com.crms.integration.config.IntegrationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HmrcOAuthServiceImpl implements HmrcOAuthService {

    private final HmrcOAuthTokenRepository tokenRepository;
    private final RestTemplate hmrcRestTemplate;
    private final IntegrationProperties properties;

    private static final String HMRC_AUTH_URL = "https://api.service.hmrc.gov.uk/oauth/authorize";
    private static final String HMRC_TOKEN_URL = "https://api.service.hmrc.gov.uk/oauth/token";

    @Override
    public String buildAuthorizationUrl(String state) {
        IntegrationProperties.HmrcProperties hmrc = properties.getHmrc();
        if (state == null || state.isBlank()) {
            state = UUID.randomUUID().toString();
        }
        return HMRC_AUTH_URL
                + "?response_type=code"
                + "&client_id=" + hmrc.getClientId()
                + "&scope=" + encodeScope(hmrc.getScope())
                + "&redirect_uri=" + hmrc.getRedirectUri()
                + "&state=" + state;
    }

    @Override
    @Transactional
    public void exchangeCodeAndStore(String code, String contractorUtr) {
        IntegrationProperties.HmrcProperties hmrc = properties.getHmrc();
        log.info("Exchanging HMRC authorisation code for contractor UTR {}", contractorUtr);

        String body = "grant_type=authorization_code"
                + "&code=" + code
                + "&client_id=" + hmrc.getClientId()
                + "&client_secret=" + hmrc.getClientSecret()
                + "&redirect_uri=" + hmrc.getRedirectUri();

        Map<String, Object> tokenData = postToTokenEndpoint(body);
        persistToken(contractorUtr, tokenData);
        log.info("HMRC OAuth tokens stored for contractor UTR {}", contractorUtr);
    }

    @Override
    @Transactional
    public String getValidAccessToken(String contractorUtr) {
        HmrcOAuthToken token = tokenRepository.findByContractorUtr(contractorUtr)
                .orElseThrow(() -> new IllegalStateException(
                        "No HMRC authorisation found for contractor UTR " + contractorUtr
                        + ". Please complete OAuth2 authorisation via /api/v1/hmrc/oauth/begin"));

        if (!token.isAccessTokenExpired(properties.getHmrc().getTokenRefreshBufferSeconds())) {
            return token.getAccessToken();
        }

        if (token.getRefreshToken() == null) {
            throw new IllegalStateException(
                    "HMRC access token expired and no refresh token available. Re-authorise via /api/v1/hmrc/oauth/begin");
        }

        log.info("HMRC access token expired — refreshing for contractor UTR {}", contractorUtr);
        return refreshAndPersist(token);
    }

    @Override
    public Map<String, Object> getStatus(String contractorUtr) {
        Map<String, Object> status = new LinkedHashMap<>();
        tokenRepository.findByContractorUtr(contractorUtr).ifPresentOrElse(t -> {
            boolean expired = t.isAccessTokenExpired(0);
            status.put("authorised", true);
            status.put("contractorUtr", contractorUtr);
            status.put("accessTokenExpired", expired);
            status.put("hasRefreshToken", t.getRefreshToken() != null);
            status.put("issuedAt", t.getIssuedAt());
            status.put("expiresAt", t.getIssuedAt().plusSeconds(t.getExpiresIn()));
            status.put("scope", t.getScope());
        }, () -> {
            status.put("authorised", false);
            status.put("contractorUtr", contractorUtr);
        });
        return status;
    }

    @Override
    @Transactional
    public void disconnect(String contractorUtr) {
        tokenRepository.deleteByContractorUtr(contractorUtr);
        log.info("HMRC OAuth tokens removed for contractor UTR {}", contractorUtr);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private String refreshAndPersist(HmrcOAuthToken existing) {
        IntegrationProperties.HmrcProperties hmrc = properties.getHmrc();

        String body = "grant_type=refresh_token"
                + "&refresh_token=" + existing.getRefreshToken()
                + "&client_id=" + hmrc.getClientId()
                + "&client_secret=" + hmrc.getClientSecret();

        Map<String, Object> tokenData = postToTokenEndpoint(body);
        return persistToken(existing.getContractorUtr(), tokenData).getAccessToken();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postToTokenEndpoint(String formBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(formBody, headers);

        try {
            ResponseEntity<Map> response = hmrcRestTemplate.postForEntity(HMRC_TOKEN_URL, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("access_token")) {
                throw new IllegalStateException("Invalid token response from HMRC: missing access_token");
            }
            return body;
        } catch (HttpClientErrorException e) {
            log.error("HMRC token endpoint error: {} — {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new HmrcCisServiceImpl.HmrcAuthenticationException(
                    "HMRC token exchange failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    HmrcOAuthToken persistToken(String contractorUtr, Map<String, Object> tokenData) {
        HmrcOAuthToken token = tokenRepository.findByContractorUtr(contractorUtr)
                .orElse(HmrcOAuthToken.builder().contractorUtr(contractorUtr).build());

        token.setAccessToken((String) tokenData.get("access_token"));
        if (tokenData.containsKey("refresh_token")) {
            token.setRefreshToken((String) tokenData.get("refresh_token"));
        }
        token.setTokenType((String) tokenData.getOrDefault("token_type", "Bearer"));
        token.setExpiresIn(((Number) tokenData.get("expires_in")).longValue());
        token.setIssuedAt(Instant.now());
        token.setScope((String) tokenData.get("scope"));
        token.setUpdatedAt(Instant.now());

        return tokenRepository.save(token);
    }

    private String encodeScope(String scope) {
        if (scope == null) return "";
        return scope.replace(" ", "%20");
    }
}

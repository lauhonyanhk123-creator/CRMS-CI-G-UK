package com.crms.web;

import com.crms.dto.response.ApiResponse;
import com.crms.integration.config.IntegrationProperties;
import com.crms.integration.hmrc.HmrcOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/hmrc/oauth")
@RequiredArgsConstructor
@Tag(name = "HMRC OAuth", description = "HMRC MTD CIS OAuth2 authorisation management")
@SecurityRequirement(name = "bearerAuth")
public class HmrcOAuthController {

    private final HmrcOAuthService oAuthService;
    private final IntegrationProperties properties;
    private final StringRedisTemplate redisTemplate;

    private static final String OAUTH_STATE_PREFIX = "hmrc:oauth:state:";

    @GetMapping("/begin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Begin HMRC authorisation",
               description = "Returns the HMRC OAuth2 authorisation URL. Redirect the user's browser to this URL.")
    public ResponseEntity<ApiResponse<Map<String, String>>> begin() {
        String contractorUtr = properties.getHmrc().getContractorUtr();
        if (contractorUtr == null || contractorUtr.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("HMRC_CONTRACTOR_UTR is not configured. Set the HMRC_CONTRACTOR_UTR environment variable."));
        }

        String state = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(OAUTH_STATE_PREFIX + state, "1", 10, TimeUnit.MINUTES);
        String authUrl = oAuthService.buildAuthorizationUrl(state);

        log.info("HMRC OAuth2 authorisation initiated for contractor UTR {}", contractorUtr);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "authorizationUrl", authUrl,
                "state", state,
                "contractorUtr", contractorUtr
        )));
    }

    @GetMapping("/callback")
    @Operation(summary = "HMRC OAuth2 callback",
               description = "Handles the redirect from HMRC after the user grants authorisation. " +
                             "Register this URL as your redirect URI in the HMRC Developer Hub.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> callback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription) {

        if (error != null) {
            log.warn("HMRC OAuth2 callback returned error: {} — {}", error, errorDescription);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("HMRC authorisation denied: " + errorDescription));
        }

        // Validate CSRF state parameter
        if (state == null || !Boolean.TRUE.equals(redisTemplate.hasKey(OAUTH_STATE_PREFIX + state))) {
            log.warn("HMRC OAuth2 callback received invalid or missing state parameter");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired OAuth2 state — please restart the authorisation flow"));
        }
        redisTemplate.delete(OAUTH_STATE_PREFIX + state);

        String contractorUtr = properties.getHmrc().getContractorUtr();
        if (contractorUtr == null || contractorUtr.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("HMRC_CONTRACTOR_UTR is not configured."));
        }

        oAuthService.exchangeCodeAndStore(code, contractorUtr);
        Map<String, Object> status = oAuthService.getStatus(contractorUtr);

        log.info("HMRC OAuth2 authorisation completed for contractor UTR {}", contractorUtr);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "HMRC authorisation status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        String contractorUtr = properties.getHmrc().getContractorUtr();
        Map<String, Object> statusMap = oAuthService.getStatus(
                contractorUtr != null ? contractorUtr : "");
        return ResponseEntity.ok(ApiResponse.success(statusMap));
    }

    @DeleteMapping("/disconnect")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disconnect HMRC authorisation",
               description = "Removes stored HMRC OAuth2 tokens. A new authorisation flow will be required.")
    public ResponseEntity<ApiResponse<String>> disconnect() {
        String contractorUtr = properties.getHmrc().getContractorUtr();
        if (contractorUtr == null || contractorUtr.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("HMRC_CONTRACTOR_UTR is not configured."));
        }
        oAuthService.disconnect(contractorUtr);
        return ResponseEntity.ok(ApiResponse.success("HMRC authorisation disconnected for UTR " + contractorUtr));
    }
}

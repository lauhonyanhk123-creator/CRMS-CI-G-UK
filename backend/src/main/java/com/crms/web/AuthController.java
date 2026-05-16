package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.request.LoginRequest;
import com.crms.dto.request.RegisterRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.AuthResponse;
import com.crms.dto.response.UserResponse;
import com.crms.security.totp.TotpService;
import com.crms.security.totp.TotpSetupResponse;
import com.crms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

import com.crms.dto.request.ChangePasswordRequest;
import com.crms.security.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {
    
    private final AuthService authService;
    private final TotpService totpService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("refreshToken is required"));
        }
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/profile")
    @Operation(summary = "Get profile", description = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        UserResponse response = authService.getProfile();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register user", description = "Register a new user account (admin only)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the authenticated user's password; clears mustChangePassword flag")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    // ── TOTP 2FA endpoints ────────────────────────────────────────────────────

    @PostMapping("/totp/challenge")
    @Operation(summary = "Complete TOTP challenge", description = "Exchange a TOTP challenge token + OTP code for a full JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> completeTotpChallenge(
            @RequestBody Map<String, String> body) {
        String challengeToken = body.get("challengeToken");
        String code = body.get("code");
        if (challengeToken == null || challengeToken.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("challengeToken is required"));
        }
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("code is required"));
        }
        AuthResponse response = authService.completeTotpChallenge(challengeToken, code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/totp/setup")
    @Operation(summary = "Initiate TOTP setup", description = "Generate a TOTP secret and return a QR code data URI")
    public ResponseEntity<ApiResponse<TotpSetupResponse>> setupTotp() {
        TotpSetupResponse response = totpService.setupTotp();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/totp/enable")
    @Operation(summary = "Enable TOTP", description = "Confirm a valid TOTP code to activate 2FA on the account")
    public ResponseEntity<ApiResponse<Void>> enableTotp(@RequestBody Map<String, String> body) {
        totpService.enableTotp(body.get("code"));
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication enabled", null));
    }

    @DeleteMapping("/totp/disable")
    @Operation(summary = "Disable TOTP", description = "Disable 2FA for the authenticated user")
    public ResponseEntity<ApiResponse<Void>> disableTotp() {
        totpService.disableTotp();
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication disabled", null));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate access and refresh tokens by blacklisting them")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, String> body) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            blacklistTokenFromString(authHeader.substring(7));
        }
        if (body != null) {
            String refreshToken = body.get("refreshToken");
            if (refreshToken != null && !refreshToken.isBlank()) {
                blacklistTokenFromString(refreshToken);
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    private void blacklistTokenFromString(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);
                String tokenId = claims.containsKey("jti") ? (String) claims.get("jti") : "unknown";
                Object expVal = claims.get("exp");
                long expiresAt = expVal instanceof Number ? ((Number) expVal).longValue() : 0;
                tokenBlacklistService.blacklist(tokenId, expiresAt);
            }
        } catch (Exception e) {
            log.warn("Could not blacklist token on logout: {}", e.getMessage());
        }
    }
}

package com.crms.web;

import com.crms.dto.request.LoginRequest;
import com.crms.dto.request.RegisterRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.AuthResponse;
import com.crms.dto.response.UserResponse;
import com.crms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

import com.crms.security.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {
    
    private final AuthService authService;
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
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken.replace("\"", ""));
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/profile")
    @Operation(summary = "Get profile", description = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        UserResponse response = authService.getProfile();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate JWT token by blacklisting it")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
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
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
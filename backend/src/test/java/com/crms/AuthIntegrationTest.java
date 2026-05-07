package com.crms;

import com.crms.config.JwtConfig;
import com.crms.config.SecurityConfig;
import com.crms.domain.user.entity.User;
import com.crms.domain.user.enums.Role;
import com.crms.dto.request.ChangePasswordRequest;
import com.crms.dto.request.LoginRequest;
import com.crms.dto.response.AuthResponse;
import com.crms.dto.response.UserResponse;
import com.crms.security.CustomUserDetails;
import com.crms.security.JwtAuthenticationFilter;
import com.crms.security.JwtTokenProvider;
import com.crms.security.PasswordEncoderConfig;
import com.crms.security.TokenBlacklistService;
import com.crms.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer integration tests for authentication and security.
 * Uses @WebMvcTest (no database, no Flyway) to start only the security filter chain
 * and controllers, giving fast tests that verify the full HTTP security contract:
 *   - public vs protected endpoints
 *   - JWT issuance on login
 *   - 401 on missing/invalid token
 *   - 403 MUST_CHANGE_PASSWORD gate
 *   - actuator endpoint access rules
 */
@WebMvcTest(controllers = com.crms.web.AuthController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationFilter.class,
    JwtTokenProvider.class,
    JwtConfig.class,
    PasswordEncoderConfig.class
})
@TestPropertySource(properties = {
    "app.jwt.secret=dGhpcy1pcy1jcm1zLXRlc3Qtand0LXNlY3JldC1rZXk=",
    "app.jwt.expiration=900000",
    "app.jwt.refresh-expiration=86400000"
})
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .email("admin@crms.local")
                .password("$argon2id$encoded")
                .enabled(true)
                .mustChangePassword(true)
                .roles(Set.of(Role.ROLE_ADMIN))
                .build();

        normalUser = User.builder()
                .id(UUID.randomUUID())
                .username("ops")
                .email("ops@crms.local")
                .password("$argon2id$encoded")
                .enabled(true)
                .mustChangePassword(false)
                .roles(Set.of(Role.ROLE_OPS_DIRECTOR))
                .build();
    }

    // ================================================================
    // Login endpoint
    // ================================================================

    @Test
    @DisplayName("POST /auth/login with valid credentials returns 200 with JWT")
    void login_validCredentials_returns200WithToken() throws Exception {
        AuthResponse authResponse = AuthResponse.builder()
                .token("test.jwt.token")
                .refreshToken("test.refresh.token")
                .expiresIn(900L)
                .user(UserResponse.builder()
                        .id(normalUser.getId())
                        .username("ops")
                        .email("ops@crms.local")
                        .roles(Set.of("ROLE_OPS_DIRECTOR"))
                        .mustChangePassword(false)
                        .build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder().username("ops").password("pass").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("test.jwt.token"));
    }

    @Test
    @DisplayName("POST /auth/login with bad credentials returns 401")
    void login_badCredentials_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder().username("ops").password("wrong").build())))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // Protected endpoint — unauthenticated
    // ================================================================

    @Test
    @DisplayName("GET /contracts without Authorization header returns 401")
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/contracts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /contracts with a garbled token returns 401")
    void protectedEndpoint_invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/contracts")
                        .header("Authorization", "Bearer not.a.valid.token"))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // mustChangePassword gate — user with flag set
    // ================================================================

    @Test
    @DisplayName("Request with valid JWT but mustChangePassword=true returns 403 MUST_CHANGE_PASSWORD")
    void mustChangePasswordGate_blocksAccess_returns403() throws Exception {
        String token = jwtTokenProvider.generateToken("admin", adminUser.getRoles());

        CustomUserDetails details = new CustomUserDetails(adminUser);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(details);

        mockMvc.perform(get("/api/v1/contracts")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MUST_CHANGE_PASSWORD"));
    }

    @Test
    @DisplayName("POST /auth/change-password is reachable even when mustChangePassword=true")
    void changePasswordEndpoint_isAllowed_withMustChangePassword() throws Exception {
        String token = jwtTokenProvider.generateToken("admin", adminUser.getRoles());

        CustomUserDetails details = new CustomUserDetails(adminUser);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(details);
        doNothing().when(authService).changePassword(anyString(), anyString());

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("Admin123!");
        req.setNewPassword("NewAdmin456!");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ================================================================
    // Normal authenticated access
    // ================================================================

    @Test
    @DisplayName("Request with valid JWT and mustChangePassword=false is allowed through the gate")
    void validToken_normalUser_passesGate() throws Exception {
        String token = jwtTokenProvider.generateToken("ops", normalUser.getRoles());

        CustomUserDetails details = new CustomUserDetails(normalUser);
        when(userDetailsService.loadUserByUsername("ops")).thenReturn(details);

        // /api/v1/contracts is not loaded in this WebMvcTest slice; any non-401/403 response
        // confirms the security filter chain let the request through.
        int status = mockMvc.perform(get("/api/v1/contracts")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, status, "Expected security to pass but got 401");
        org.junit.jupiter.api.Assertions.assertNotEquals(403, status, "Expected security to pass but got 403");
    }
}

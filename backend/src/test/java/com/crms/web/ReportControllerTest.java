package com.crms.web;

import com.crms.config.JwtConfig;
import com.crms.config.SecurityConfig;
import com.crms.domain.user.entity.User;
import com.crms.domain.user.enums.Role;
import com.crms.dto.response.CVRItem;
import com.crms.security.CustomUserDetails;
import com.crms.security.JwtAuthenticationFilter;
import com.crms.security.JwtTokenProvider;
import com.crms.security.PasswordEncoderConfig;
import com.crms.security.TokenBlacklistService;
import com.crms.security.totp.TotpService;
import com.crms.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
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
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private TotpService totpService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = User.builder()
                .id(UUID.randomUUID())
                .username("ops")
                .email("ops@crms.local")
                .password("encoded")
                .enabled(true)
                .mustChangePassword(false)
                .roles(Set.of(Role.ROLE_OPS_DIRECTOR))
                .build();
    }

    private String tokenFor(User user) {
        when(userDetailsService.loadUserByUsername(user.getUsername()))
                .thenReturn(new CustomUserDetails(user));
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }

    // ================================================================
    // GET /api/v1/reports/cvr
    // ================================================================

    @Test
    @DisplayName("GET /reports/cvr — authenticated gets 200")
    void getCVR_authenticated_returns200() throws Exception {
        String token = tokenFor(authenticatedUser);
        when(reportService.getCVR(anyLong(), anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reports/cvr")
                        .param("contract", "1")
                        .param("period", "2024-01")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /reports/cvr — unauthenticated gets 401")
    void getCVR_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/cvr")
                        .param("contract", "1"))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // GET /api/v1/reports/cashflow
    // ================================================================

    @Test
    @DisplayName("GET /reports/cashflow — authenticated gets 200")
    void getCashflow_authenticated_returns200() throws Exception {
        String token = tokenFor(authenticatedUser);
        when(reportService.getCashflow(anyString(), anyString())).thenReturn(Map.of("total", 0));

        mockMvc.perform(get("/api/v1/reports/cashflow")
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /reports/cashflow — unauthenticated gets 401")
    void getCashflow_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/cashflow")
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31"))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // GET /api/v1/reports/retention-schedule
    // ================================================================

    @Test
    @DisplayName("GET /reports/retention-schedule — authenticated gets 200")
    void getRetentionSchedule_authenticated_returns200() throws Exception {
        String token = tokenFor(authenticatedUser);
        when(reportService.getRetention()).thenReturn(Map.of("items", List.of()));

        mockMvc.perform(get("/api/v1/reports/retention-schedule")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /reports/retention-schedule — unauthenticated gets 401")
    void getRetentionSchedule_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/reports/retention-schedule"))
                .andExpect(status().isUnauthorized());
    }
}

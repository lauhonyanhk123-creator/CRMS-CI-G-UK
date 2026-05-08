package com.crms.web;

import com.crms.config.JwtConfig;
import com.crms.config.SecurityConfig;
import com.crms.domain.contract.enums.VariationType;
import com.crms.exception.GlobalExceptionHandler;
import com.crms.domain.user.entity.User;
import com.crms.domain.user.enums.Role;
import com.crms.dto.request.VariationRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.VariationResponse;
import com.crms.security.CustomUserDetails;
import com.crms.security.JwtAuthenticationFilter;
import com.crms.security.JwtTokenProvider;
import com.crms.security.PasswordEncoderConfig;
import com.crms.security.TokenBlacklistService;
import com.crms.security.totp.TotpService;
import com.crms.service.VariationService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VariationController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationFilter.class,
    JwtTokenProvider.class,
    JwtConfig.class,
    PasswordEncoderConfig.class,
    GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
    "app.jwt.secret=dGhpcy1pcy1jcm1zLXRlc3Qtand0LXNlY3JldC1rZXk=",
    "app.jwt.expiration=900000",
    "app.jwt.refresh-expiration=86400000"
})
class VariationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VariationService variationService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private TotpService totpService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private User adminUser;
    private User viewerUser;

    private VariationResponse sampleVariation;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .email("admin@crms.local")
                .password("encoded")
                .enabled(true)
                .mustChangePassword(false)
                .roles(Set.of(Role.ROLE_ADMIN))
                .build();

        viewerUser = User.builder()
                .id(UUID.randomUUID())
                .username("viewer")
                .email("viewer@crms.local")
                .password("encoded")
                .enabled(true)
                .mustChangePassword(false)
                .roles(Set.of(Role.ROLE_USER))
                .build();

        sampleVariation = VariationResponse.builder()
                .id(1L)
                .variationRef("VAR-001")
                .contractId(1L)
                .type("ADDITION")
                .description("Extra groundworks")
                .originalValue(BigDecimal.valueOf(5000))
                .status("PENDING")
                .build();
    }

    private String tokenFor(User user) {
        when(userDetailsService.loadUserByUsername(user.getUsername()))
                .thenReturn(new CustomUserDetails(user));
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }

    // ================================================================
    // GET /api/v1/variations?contractId=1
    // ================================================================

    @Test
    @DisplayName("GET /variations?contractId=1 — authenticated gets 200")
    void findVariations_withContractId_authenticated_returns200() throws Exception {
        String token = tokenFor(viewerUser);
        PageResponse<VariationResponse> page = PageResponse.<VariationResponse>builder()
                .content(List.of(sampleVariation))
                .page(0).size(20).totalElements(1L).totalPages(1)
                .build();
        when(variationService.findByContract(1L)).thenReturn(page);

        mockMvc.perform(get("/api/v1/variations")
                        .param("contractId", "1")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /variations?contractId=1 — unauthenticated gets 401")
    void findVariations_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/variations")
                        .param("contractId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /variations (no contractId) — gets 400 because contractId is required")
    void findVariations_missingContractId_returns400() throws Exception {
        String token = tokenFor(viewerUser);

        mockMvc.perform(get("/api/v1/variations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ================================================================
    // POST /api/v1/variations
    // ================================================================

    @Test
    @DisplayName("POST /variations — ADMIN gets 200")
    void create_admin_returns200() throws Exception {
        String token = tokenFor(adminUser);
        when(variationService.create(anyLong(), any(VariationRequest.class))).thenReturn(sampleVariation);

        VariationRequest req = VariationRequest.builder()
                .type(VariationType.ADDITION)
                .description("Extra groundworks")
                .originalValue(BigDecimal.valueOf(5000))
                .build();

        mockMvc.perform(post("/api/v1/variations")
                        .param("contractId", "1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /variations — VIEWER gets 403")
    void create_viewer_returns403() throws Exception {
        String token = tokenFor(viewerUser);

        VariationRequest req = VariationRequest.builder()
                .type(VariationType.ADDITION)
                .description("Extra groundworks")
                .originalValue(BigDecimal.valueOf(5000))
                .build();

        mockMvc.perform(post("/api/v1/variations")
                        .param("contractId", "1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /variations — unauthenticated gets 401")
    void create_unauthenticated_returns401() throws Exception {
        VariationRequest req = VariationRequest.builder()
                .type(VariationType.ADDITION)
                .description("Extra groundworks")
                .originalValue(BigDecimal.valueOf(5000))
                .build();

        mockMvc.perform(post("/api/v1/variations")
                        .param("contractId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}

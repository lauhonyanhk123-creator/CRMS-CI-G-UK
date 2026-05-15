package com.crms.web;

import com.crms.config.JwtConfig;
import com.crms.config.SecurityConfig;
import com.crms.domain.user.entity.User;
import com.crms.domain.user.enums.Role;
import com.crms.dto.request.CDMRegisterRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.CDMRegisterResponse;
import com.crms.dto.response.PageResponse;
import com.crms.security.CustomUserDetails;
import com.crms.security.JwtAuthenticationFilter;
import com.crms.security.JwtTokenProvider;
import com.crms.security.PasswordEncoderConfig;
import com.crms.security.TokenBlacklistService;
import com.crms.security.totp.TotpService;
import com.crms.service.CDMRegisterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.crms.config.TestMetricsConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CDMRegisterController.class)
@Import({
    SecurityConfig.class,
    JwtAuthenticationFilter.class,
    JwtTokenProvider.class,
    JwtConfig.class,
    PasswordEncoderConfig.class,
    TestMetricsConfig.class
})
@TestPropertySource(properties = {
    "app.jwt.secret=dGhpcy1pcy1jcm1zLXRlc3Qtand0LXNlY3JldC1rZXk=",
    "app.jwt.expiration=900000",
    "app.jwt.refresh-expiration=86400000"
})
class CDMRegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CDMRegisterService cdmRegisterService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private TotpService totpService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private User adminUser;
    private User managerUser;
    private User viewerUser;

    private CDMRegisterResponse sampleCDM;

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

        managerUser = User.builder()
                .id(UUID.randomUUID())
                .username("manager")
                .email("manager@crms.local")
                .password("encoded")
                .enabled(true)
                .mustChangePassword(false)
                .roles(Set.of(Role.ROLE_CONTRACTS_MANAGER))
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

        sampleCDM = CDMRegisterResponse.builder()
                .id(1L)
                .projectName("Test Project")
                .notificationNumber("CDM-001")
                .isNotifiable(true)
                .isActive(true)
                .build();
    }

    private String tokenFor(User user) {
        when(userDetailsService.loadUserByUsername(user.getUsername()))
                .thenReturn(new CustomUserDetails(user));
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }

    // ================================================================
    // GET /api/v1/cdm-register
    // ================================================================

    @Test
    @DisplayName("GET /cdm-register — authenticated gets 200")
    void findAll_authenticated_returns200() throws Exception {
        String token = tokenFor(viewerUser);
        PageResponse<CDMRegisterResponse> page = PageResponse.<CDMRegisterResponse>builder()
                .content(List.of(sampleCDM))
                .page(0).size(20).totalElements(1L).totalPages(1)
                .build();
        when(cdmRegisterService.findAll(anyMap())).thenReturn(page);

        mockMvc.perform(get("/api/v1/cdm-register")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /cdm-register — unauthenticated gets 401")
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/cdm-register"))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // POST /api/v1/cdm-register
    // ================================================================

    @Test
    @DisplayName("POST /cdm-register — ADMIN gets 200")
    void create_admin_returns200() throws Exception {
        String token = tokenFor(adminUser);
        when(cdmRegisterService.create(any(CDMRegisterRequest.class))).thenReturn(sampleCDM);

        CDMRegisterRequest req = CDMRegisterRequest.builder()
                .projectName("New CDM Project")
                .clientId(1L)
                .isNotifiable(true)
                .build();

        mockMvc.perform(post("/api/v1/cdm-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /cdm-register — MANAGER gets 200")
    void create_manager_returns200() throws Exception {
        String token = tokenFor(managerUser);
        when(cdmRegisterService.create(any(CDMRegisterRequest.class))).thenReturn(sampleCDM);

        CDMRegisterRequest req = CDMRegisterRequest.builder()
                .projectName("New CDM Project")
                .clientId(1L)
                .isNotifiable(false)
                .build();

        mockMvc.perform(post("/api/v1/cdm-register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /cdm-register — unauthenticated gets 401")
    void create_unauthenticated_returns401() throws Exception {
        CDMRegisterRequest req = CDMRegisterRequest.builder()
                .projectName("New CDM Project")
                .clientId(1L)
                .build();

        mockMvc.perform(post("/api/v1/cdm-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // DELETE — CDMRegisterController has no DELETE endpoint
    // Testing GET /{id} instead (authenticated access)
    // ================================================================

    @Test
    @DisplayName("GET /cdm-register/{id} — authenticated gets 200")
    void findById_authenticated_returns200() throws Exception {
        String token = tokenFor(viewerUser);
        when(cdmRegisterService.findById(1L)).thenReturn(sampleCDM);

        mockMvc.perform(get("/api/v1/cdm-register/1")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectName").value("Test Project"));
    }

    @Test
    @DisplayName("GET /cdm-register/{id} — unauthenticated gets 401")
    void findById_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/cdm-register/1"))
                .andExpect(status().isUnauthorized());
    }
}

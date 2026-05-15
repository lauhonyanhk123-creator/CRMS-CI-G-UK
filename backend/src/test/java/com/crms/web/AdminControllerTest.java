package com.crms.web;

import com.crms.config.JwtConfig;
import com.crms.config.SecurityConfig;
import com.crms.domain.user.entity.User;
import com.crms.domain.user.enums.Role;
import com.crms.dto.request.RegisterRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.UserResponse;
import com.crms.security.CustomUserDetails;
import com.crms.security.JwtAuthenticationFilter;
import com.crms.security.JwtTokenProvider;
import com.crms.security.PasswordEncoderConfig;
import com.crms.security.TokenBlacklistService;
import com.crms.security.totp.TotpService;
import com.crms.service.UserService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class)
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
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private TotpService totpService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private User adminUser;
    private User nonAdminUser;

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

        nonAdminUser = User.builder()
                .id(UUID.randomUUID())
                .username("manager")
                .email("manager@crms.local")
                .password("encoded")
                .enabled(true)
                .mustChangePassword(false)
                .roles(Set.of(Role.ROLE_CONTRACTS_MANAGER))
                .build();
    }

    private String tokenFor(User user) {
        when(userDetailsService.loadUserByUsername(user.getUsername()))
                .thenReturn(new CustomUserDetails(user));
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }

    // ================================================================
    // GET /api/v1/admin/users
    // ================================================================

    @Test
    @DisplayName("GET /admin/users — ADMIN gets 200")
    void listUsers_admin_returns200() throws Exception {
        String token = tokenFor(adminUser);
        PageResponse<UserResponse> page = PageResponse.<UserResponse>builder()
                .content(List.of(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .username("someuser")
                        .email("someuser@crms.local")
                        .roles(Set.of("ROLE_VIEWER"))
                        .build()))
                .page(0).size(20).totalElements(1L).totalPages(1)
                .build();
        when(userService.listUsers(anyInt(), anyInt(), anyString())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /admin/users — non-admin gets 403")
    void listUsers_nonAdmin_returns403() throws Exception {
        String token = tokenFor(nonAdminUser);

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin/users — unauthenticated gets 401")
    void listUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // POST /api/v1/admin/users
    // ================================================================

    @Test
    @DisplayName("POST /admin/users — ADMIN gets 201")
    void createUser_admin_returns201() throws Exception {
        String token = tokenFor(adminUser);
        UserResponse created = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .email("newuser@crms.local")
                .roles(Set.of("ROLE_VIEWER"))
                .build();
        when(userService.createUser(any(RegisterRequest.class))).thenReturn(created);

        RegisterRequest req = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@crms.local")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /admin/users — non-admin gets 403")
    void createUser_nonAdmin_returns403() throws Exception {
        String token = tokenFor(nonAdminUser);

        RegisterRequest req = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@crms.local")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /admin/users — unauthenticated gets 401")
    void createUser_unauthenticated_returns401() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@crms.local")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // GET /api/v1/admin/settings
    // ================================================================

    @Test
    @DisplayName("GET /admin/settings — ADMIN gets 200")
    void getSettings_admin_returns200() throws Exception {
        String token = tokenFor(adminUser);

        mockMvc.perform(get("/api/v1/admin/settings")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /admin/settings — non-admin gets 403")
    void getSettings_nonAdmin_returns403() throws Exception {
        String token = tokenFor(nonAdminUser);

        mockMvc.perform(get("/api/v1/admin/settings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin/settings — unauthenticated gets 401")
    void getSettings_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/settings"))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // GET /api/v1/admin/integrations/status
    // ================================================================

    @Test
    @DisplayName("GET /admin/integrations/status — ADMIN gets 200")
    void getIntegrationsStatus_admin_returns200() throws Exception {
        String token = tokenFor(adminUser);

        mockMvc.perform(get("/api/v1/admin/integrations/status")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /admin/integrations/status — unauthenticated gets 401")
    void getIntegrationsStatus_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/integrations/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /admin/integrations/status — non-admin gets 403")
    void getIntegrationsStatus_nonAdmin_returns403() throws Exception {
        String token = tokenFor(nonAdminUser);

        mockMvc.perform(get("/api/v1/admin/integrations/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}

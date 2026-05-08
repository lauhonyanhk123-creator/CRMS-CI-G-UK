package com.crms.web;

import com.crms.config.JwtConfig;
import com.crms.config.SecurityConfig;
import com.crms.domain.plant.enums.PlantCategory;
import com.crms.domain.user.entity.User;
import com.crms.domain.user.enums.Role;
import com.crms.dto.request.PlantItemRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.PlantItemResponse;
import com.crms.security.CustomUserDetails;
import com.crms.security.JwtAuthenticationFilter;
import com.crms.security.JwtTokenProvider;
import com.crms.security.PasswordEncoderConfig;
import com.crms.security.TokenBlacklistService;
import com.crms.security.totp.TotpService;
import com.crms.service.PlantService;
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
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PlantController.class)
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
class PlantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlantService plantService;

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

    private PlantItemResponse samplePlantItem;

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

        samplePlantItem = PlantItemResponse.builder()
                .id(1L)
                .plantRef("PLT-001")
                .description("360 Excavator")
                .category("EXCAVATOR_360")
                .status("AVAILABLE")
                .build();
    }

    private String tokenFor(User user) {
        when(userDetailsService.loadUserByUsername(user.getUsername()))
                .thenReturn(new CustomUserDetails(user));
        return jwtTokenProvider.generateToken(user.getUsername(), user.getRoles());
    }

    // ================================================================
    // GET /api/v1/plant-items
    // ================================================================

    @Test
    @DisplayName("GET /plant-items — authenticated user gets 200")
    void findAll_authenticated_returns200() throws Exception {
        String token = tokenFor(managerUser);
        PageResponse<PlantItemResponse> page = PageResponse.<PlantItemResponse>builder()
                .content(List.of(samplePlantItem))
                .page(0).size(20).totalElements(1L).totalPages(1)
                .build();
        when(plantService.findAll(anyMap())).thenReturn(page);

        mockMvc.perform(get("/api/v1/plant-items")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /plant-items — unauthenticated gets 401")
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/plant-items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // POST /api/v1/plant-items
    // ================================================================

    @Test
    @DisplayName("POST /plant-items — ADMIN gets 200")
    void create_admin_returns200() throws Exception {
        String token = tokenFor(adminUser);
        when(plantService.create(any(PlantItemRequest.class))).thenReturn(samplePlantItem);

        PlantItemRequest req = PlantItemRequest.builder()
                .plantRef("PLT-002")
                .description("Telehandler")
                .category(PlantCategory.TELEHANDLER)
                .build();

        mockMvc.perform(post("/api/v1/plant-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /plant-items — MANAGER gets 403")
    void create_manager_returns403() throws Exception {
        String token = tokenFor(managerUser);

        PlantItemRequest req = PlantItemRequest.builder()
                .plantRef("PLT-003")
                .description("Dumper")
                .category(PlantCategory.DUMPER)
                .build();

        mockMvc.perform(post("/api/v1/plant-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /plant-items — unauthenticated gets 401")
    void create_unauthenticated_returns401() throws Exception {
        PlantItemRequest req = PlantItemRequest.builder()
                .plantRef("PLT-004")
                .description("Roller")
                .category(PlantCategory.ROLLER)
                .build();

        mockMvc.perform(post("/api/v1/plant-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // GET /api/v1/plant-items/{id}
    // ================================================================

    @Test
    @DisplayName("GET /plant-items/{id} — authenticated gets 200 with data")
    void findById_authenticated_returns200() throws Exception {
        String token = tokenFor(viewerUser);
        when(plantService.findById(1L)).thenReturn(samplePlantItem);

        mockMvc.perform(get("/api/v1/plant-items/1")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.plantRef").value("PLT-001"));
    }

    @Test
    @DisplayName("GET /plant-items/{id} — unauthenticated gets 401")
    void findById_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/plant-items/1"))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // DELETE /api/v1/plant-items/{id}
    // ================================================================

    @Test
    @DisplayName("DELETE /plant-items/{id} — ADMIN gets 200")
    void delete_admin_returns200() throws Exception {
        String token = tokenFor(adminUser);
        doNothing().when(plantService).delete(anyLong());

        mockMvc.perform(delete("/api/v1/plant-items/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /plant-items/{id} — VIEWER gets 403")
    void delete_viewer_returns403() throws Exception {
        String token = tokenFor(viewerUser);

        mockMvc.perform(delete("/api/v1/plant-items/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}

package com.crms.web;

import com.crms.domain.user.enums.Role;
import com.crms.dto.request.RegisterRequest;
import com.crms.dto.request.UpdateUserRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.UserResponse;
import com.crms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Administrative endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;

    // In-memory settings store (survives restarts only until a proper settings entity is added)
    private static final Map<String, Object> SYSTEM_SETTINGS = new ConcurrentHashMap<>(Map.of(
            "companyName", "CRMS Construction Ltd",
            "defaultRetentionPercent", 5,
            "cisSchemeActive", true,
            "maxFileUploadMb", 50,
            "sessionTimeoutMinutes", 60,
            "emailNotificationsEnabled", true
    ));

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users", description = "Get paginated list of users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> findAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "username") String sort) {
        return ResponseEntity.ok(ApiResponse.success(userService.listUsers(page, size, sort)));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "Create a new user — user must change password on first login")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created", created));
    }

    @PatchMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Update email, name, roles, enabled flag, or reset password")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated", userService.updateUser(id, request)));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable user", description = "Disables the user account (soft delete — preserves audit trail)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User disabled", null));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List roles", description = "Get all available system roles")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getRoles() {
        List<Map<String, String>> roles = Arrays.stream(Role.values())
                .map(r -> {
                    Map<String, String> entry = new LinkedHashMap<>();
                    entry.put("name", r.name());
                    entry.put("label", r.name().replace("ROLE_", "").replace("_", " "));
                    return entry;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(new LinkedHashMap<>(SYSTEM_SETTINGS)));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update system settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSettings(@RequestBody Map<String, Object> updates) {
        SYSTEM_SETTINGS.putAll(updates);
        return ResponseEntity.ok(ApiResponse.success("Settings saved", new LinkedHashMap<>(SYSTEM_SETTINGS)));
    }

    @GetMapping("/backup/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Backup status")
    public ResponseEntity<ApiResponse<Object>> getBackupStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("mode", "docker-volume");
        status.put("note", "Backups are managed via the 'backup' Docker Compose service profile. Run: docker compose --profile backup up backup");
        status.put("retentionDays", System.getenv().getOrDefault("BACKUP_RETENTION_DAYS", "30"));
        status.put("lastBackup", null);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping("/backup/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger backup")
    public ResponseEntity<ApiResponse<Object>> triggerBackup() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("triggered", false);
        result.put("message", "Manual backup trigger is not available in self-hosted mode. Run: docker compose --profile backup up backup");
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/integrations/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Integration status")
    public ResponseEntity<ApiResponse<Object>> getIntegrationsStatus() {
        Map<String, Object> result = new LinkedHashMap<>();

        String mailHost = System.getenv().getOrDefault("MAIL_HOST", "disabled");
        boolean smtpConfigured = !mailHost.isBlank() && !"disabled".equalsIgnoreCase(mailHost);
        result.put("smtpConfigured", smtpConfigured);
        result.put("smtpHost", smtpConfigured ? mailHost : null);
        result.put("adminEmail", System.getenv().getOrDefault("MAIL_ADMIN", "admin@crms.local"));

        String hmrcDemoMode = System.getenv().getOrDefault("HMRC_DEMO_MODE", "true");
        result.put("hmrcDemoMode", "true".equalsIgnoreCase(hmrcDemoMode));
        result.put("hmrcContractorUtr", System.getenv().get("HMRC_CONTRACTOR_UTR"));
        result.put("hmrcBaseUrl", System.getenv().getOrDefault("HMRC_BASE_URL", "https://test-api.service.hmrc.gov.uk"));

        String chApiKey = System.getenv().getOrDefault("COMPANIES_HOUSE_API_KEY", "");
        result.put("companiesHouseConfigured", !chApiKey.isBlank());

        String cscsApiKey = System.getenv().getOrDefault("CSCS_API_KEY", "");
        result.put("cscsConfigured", !cscsApiKey.isBlank());

        String minioEndpoint = System.getenv().getOrDefault("MINIO_ENDPOINT", "http://localhost:9000");
        result.put("minioConfigured", System.getenv().containsKey("MINIO_ENDPOINT"));
        result.put("minioEndpoint", minioEndpoint);
        result.put("minioBucket", System.getenv().getOrDefault("MINIO_BUCKET", "crms-documents"));

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

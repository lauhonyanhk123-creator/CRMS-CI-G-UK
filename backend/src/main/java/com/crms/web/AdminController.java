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
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error("Backup management not yet implemented"));
    }

    @PostMapping("/backup/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger backup")
    public ResponseEntity<ApiResponse<Object>> triggerBackup() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error("Backup management not yet implemented"));
    }

    @GetMapping("/integrations/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Integration status")
    public ResponseEntity<ApiResponse<Object>> getIntegrationsStatus() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error("Integration status not yet implemented"));
    }
}

package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.request.RegisterRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Administrative endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users", description = "Get paginated list of users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> findAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        // Would call user management service
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody RegisterRequest request) {
        // Would call user management service
        return ResponseEntity.ok(ApiResponse.success("User created", null));
    }
    
    @PatchMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Update user details")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody Object request) {
        return ResponseEntity.ok(ApiResponse.success("User updated", null));
    }
    
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete a user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }
    
    @GetMapping("/backup/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Backup status", description = "Get backup system status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBackupStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("lastBackup", "2026-04-27T10:00:00Z");
        status.put("status", "HEALTHY");
        status.put("nextScheduled", "2026-04-28T02:00:00Z");
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    
    @PostMapping("/backup/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger backup", description = "Manually trigger backup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerBackup() {
        Map<String, Object> result = new HashMap<>();
        result.put("backupId", "BACKUP-" + System.currentTimeMillis());
        result.put("status", "STARTED");
        return ResponseEntity.ok(ApiResponse.success("Backup triggered", result));
    }
    
    @GetMapping("/integrations/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Integration status", description = "Get external integrations status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getIntegrationsStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("companiesHouse", "CONNECTED");
        status.put("hmrc", "CONNECTED");
        status.put("cscs", "CONNECTED");
        status.put("tenderApi", "CONNECTED");
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}

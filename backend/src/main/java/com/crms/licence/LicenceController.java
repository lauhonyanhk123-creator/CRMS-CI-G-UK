package com.crms.licence;

import com.crms.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/licence")
@RequiredArgsConstructor
@Tag(name = "Licence", description = "Installation licence and tier information")
@SecurityRequirement(name = "bearerAuth")
public class LicenceController {

    private final LicenceService licenceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get licence status",
        description = "Returns the current tier, user counts, and maintenance expiry. Accessible to all authenticated users; detailed installation ID restricted to admins."
    )
    public ResponseEntity<ApiResponse<LicenceStatus>> getLicenceStatus() {
        LicenceStatus status = licenceService.currentStatus();
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/installation-id")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get installation ID",
        description = "Returns the raw installation ID. Admin only."
    )
    public ResponseEntity<ApiResponse<String>> getInstallationId() {
        LicenceStatus status = licenceService.currentStatus();
        return ResponseEntity.ok(ApiResponse.success(status.getInstallationId()));
    }
}

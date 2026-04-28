package com.crms.web;

import com.crms.dto.request.CompanyRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.CISVerificationResponse;
import com.crms.dto.response.CompanyResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SubbieGateStatus;
import com.crms.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {
    
    private final CompanyService companyService;
    
    @GetMapping
    @Operation(summary = "List companies", description = "Get paginated list of companies")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponse>>> findAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("filter", filter);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<CompanyResponse> response = companyService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @Operation(summary = "Create company", description = "Create a new company")
    public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Company created successfully", response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get company", description = "Get company by ID")
    public ResponseEntity<ApiResponse<CompanyResponse>> findById(@PathVariable Long id) {
        CompanyResponse response = companyService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update company", description = "Update company details")
    public ResponseEntity<ApiResponse<CompanyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Company updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete company", description = "Delete a company")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Company deleted successfully", null));
    }
    
    @PostMapping("/{id}/companies-house-refresh")
    @Operation(summary = "Refresh from Companies House", description = "Refresh company data from Companies House API")
    public ResponseEntity<ApiResponse<CompanyResponse>> refreshCompaniesHouse(@PathVariable Long id) {
        CompanyResponse response = companyService.refreshCompaniesHouse(id);
        return ResponseEntity.ok(ApiResponse.success("Companies House data refreshed", response));
    }
    
    @PostMapping("/{id}/cis-verify")
    @Operation(summary = "Verify CIS", description = "Verify company CIS status with HMRC")
    public ResponseEntity<ApiResponse<CISVerificationResponse>> verifyCIS(@PathVariable Long id) {
        CISVerificationResponse response = companyService.verifyCIS(id);
        return ResponseEntity.ok(ApiResponse.success("CIS verification completed", response));
    }
    
    @GetMapping("/{id}/subbie-gate-status")
    @Operation(summary = "Get subbie gate status", description = "Get gate entry status for subcontractor")
    public ResponseEntity<ApiResponse<SubbieGateStatus>> getSubbieGateStatus(@PathVariable Long id) {
        SubbieGateStatus response = companyService.getSubbieGateStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
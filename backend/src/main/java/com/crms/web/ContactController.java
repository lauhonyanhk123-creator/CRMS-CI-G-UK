package com.crms.web;

import com.crms.dto.request.ContactRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.ContactResponse;
import com.crms.dto.response.PageResponse;
import com.crms.service.ContactService;
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
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts", description = "Contact management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ContactController {
    
    private final ContactService contactService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List contacts", description = "Get paginated list of contacts")
    public ResponseEntity<ApiResponse<PageResponse<ContactResponse>>> findAll(
            @RequestParam(required = false) Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);
        
        PageResponse<ContactResponse> response = contactService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create contact", description = "Create a new contact")
    public ResponseEntity<ApiResponse<ContactResponse>> create(@Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Contact created successfully", response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get contact", description = "Get contact by ID")
    public ResponseEntity<ApiResponse<ContactResponse>> findById(@PathVariable Long id) {
        ContactResponse response = contactService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update contact", description = "Update contact details")
    public ResponseEntity<ApiResponse<ContactResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Contact updated successfully", response));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete contact", description = "Delete a contact")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Contact deleted successfully", null));
    }
}
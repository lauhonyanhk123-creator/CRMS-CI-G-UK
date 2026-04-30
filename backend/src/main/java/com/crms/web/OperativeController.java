package com.crms.web;

import com.crms.dto.request.CardRequest;
import com.crms.dto.request.OperativeRequest;
import com.crms.dto.request.QualificationRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.CardResponse;
import com.crms.dto.response.OperativeResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.QualificationResponse;
import com.crms.dto.response.SubbieGateStatus;
import com.crms.service.OperativeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/operatives")
@RequiredArgsConstructor
@Tag(name = "Operatives", description = "Operative, card and qualification management")
@SecurityRequirement(name = "bearerAuth")
public class OperativeController {

    private final OperativeService operativeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List operatives", description = "Get paginated list of operatives")
    public ResponseEntity<ApiResponse<PageResponse<OperativeResponse>>> findAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {

        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("search", search);
        params.put("page", page);
        params.put("size", size);
        params.put("sort", sort);

        PageResponse<OperativeResponse> response = operativeService.findAll(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create operative", description = "Create a new operative record")
    public ResponseEntity<ApiResponse<OperativeResponse>> create(@Valid @RequestBody OperativeRequest request) {
        OperativeResponse response = operativeService.create(request);
        return ResponseEntity.ok(ApiResponse.success("Operative created", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get operative", description = "Get operative by ID")
    public ResponseEntity<ApiResponse<OperativeResponse>> findById(@PathVariable Long id) {
        OperativeResponse response = operativeService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update operative", description = "Update operative details")
    public ResponseEntity<ApiEntity.ApiResponse<OperativeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody OperativeRequest request) {
        OperativeResponse response = operativeService.update(id, request);
return ResponseEntity.ok(ApiResponse.success("Operative updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete operative", description = "Delete an operative")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        operativeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Operative deleted", null));
    }

    // Cards
    @GetMapping("/{id}/cards")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List cards", description = "Get all cards for an operative")
    public ResponseEntity<ApiResponse<List<CardResponse>>> getCards(@PathVariable Long id) {
        List<CardResponse> cards = operativeService.getCards(id);
        return ResponseEntity.ok(ApiResponse.success(cards));
    }

    @PostMapping("/{id}/cards")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add card", description = "Add a card (CSCS/CPCS/SIAS/CIP) to an operative")
    public ResponseEntity<ApiResponse<CardResponse>> addCard(
            @PathVariable Long id,
            @Valid @RequestBody CardRequest request) {
        CardResponse card = operativeService.addCard(id, request);
        return ResponseEntity.ok(ApiResponse.success("Card added", card));
    }

    @DeleteMapping("/{id}/cards/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete card", description = "Remove a card from an operative")
    public ResponseEntity<ApiResponse<CardResponse>> deleteCard(
            @PathVariable Long id,
            @PathVariable Long cardId) {
        CardResponse card = operativeService.deleteCard(id, cardId);
        return ResponseEntity.ok(ApiResponse.success("Card deleted", card));
    }

    // CSCS Smart Check
    @PostMapping("/{id}/cards/{cardId}/cscs-smart-check")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "CSCS Smart Check", description = "Run CSCS Smart Check on a card")
    public ResponseEntity<ApiResponse<SubbieGateStatus>> smartCheckCard(
            @PathVariable Long id,
            @PathVariable Long cardId) {
        SubbieGateStatus status = operativeService.smartCheckCard(id, cardId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    // Qualifications
    @GetMapping("/{id}/qualifications")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List qualifications", description = "Get all qualifications for an operative")
    public ResponseEntity<ApiResponse<List<QualificationResponse>>> getQualifications(@PathVariable Long id) {
        List<QualificationResponse> quals = operativeService.getQualifications(id);
        return ResponseEntity.ok(ApiResponse.success(quals));
    }

    @PostMapping("/{id}/qualifications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add qualification", description = "Add a qualification to an operative")
    public ResponseEntity<ApiResponse<QualificationResponse>> addQualification(
            @PathVariable Long id,
            @Valid @RequestBody QualificationRequest request) {
        QualificationResponse qual = operativeService.addQualification(id, request);
        return ResponseEntity.ok(ApiResponse.success("Qualification added", qual));
    }

    @DeleteMapping("/{id}/qualifications/{qualId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete qualification", description = "Remove a qualification from an operative")
    public ResponseEntity<ApiResponse<QualificationResponse>> deleteQualification(
            @PathVariable Long id,
            @PathVariable Long qualId) {
        QualificationResponse qual = operativeService.deleteQualification(id, qualId);
        return ResponseEntity.ok(ApiResponse.success("Qualification deleted", qual));
    }

    // Gate Status
    @GetMapping("/{id}/subbie-gate-status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Subbie gate status", description = "Get the gate status for an operative")
    public ResponseEntity<ApiResponse<SubbieGateStatus>> getSubbieGateStatus(@PathVariable Long id) {
        SubbieGateStatus status = operativeService.getSubbieGateStatus(id);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
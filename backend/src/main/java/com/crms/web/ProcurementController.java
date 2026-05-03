package com.crms.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.service.ProcurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/procurement")
@RequiredArgsConstructor
@Tag(name = "Procurement", description = "Procurement management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProcurementController {

    private final ProcurementService procurementService;

    // --- Purchase Requisitions ---

    @GetMapping("/purchase-requisitions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List requisitions", description = "Get paginated list of purchase requisitions")
    public ResponseEntity<ApiResponse<Object>> findRequisitions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long siteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("siteId", siteId);
        params.put("page", page);
        params.put("size", size);
        PageResponse<?> response = procurementService.findRequisitions(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/purchase-requisitions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create requisition", description = "Create a new purchase requisition")
    public ResponseEntity<ApiResponse<Object>> createRequisition(@RequestBody Object request) {
        Object response = procurementService.createRequisition(request);
        return ResponseEntity.ok(ApiResponse.success("Requisition created", response));
    }

    @PostMapping("/purchase-requisitions/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Approve requisition")
    public ResponseEntity<ApiResponse<Object>> approveRequisition(@PathVariable Long id) {
        Object response = procurementService.approveRequisition(id);
        return ResponseEntity.ok(ApiResponse.success("Requisition approved", response));
    }

    @PostMapping("/purchase-requisitions/{id}/create-po")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create purchase order from requisition")
    public ResponseEntity<ApiResponse<Object>> createPO(@PathVariable Long id) {
        Object response = procurementService.createPO(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order created", response));
    }

    // --- Purchase Orders ---

    @GetMapping("/purchase-orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List purchase orders", description = "Get paginated list of purchase orders")
    public ResponseEntity<ApiResponse<PageResponse<?>>> findPurchaseOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        params.put("page", page);
        params.put("size", size);
        PageResponse<?> response = procurementService.findPurchaseOrders(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // --- Delivery Notes ---

    @GetMapping("/delivery-notes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List delivery notes")
    public ResponseEntity<ApiResponse<Object>> findDeliveryNotes(
            @RequestParam(required = false) Long orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        Object response = procurementService.getDeliveryNotes(params);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

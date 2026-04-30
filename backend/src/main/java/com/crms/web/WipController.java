package com.crms.web;

import com.crms.dto.request.WipReportRequest;
import com.crms.dto.response.ApiResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.WipReportResponse;
import com.crms.service.WipJournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/wip")
@RequiredArgsConstructor
@Tag(name = "WIP Management", description = "WIP journal generation and reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WipController {

    private final WipJournalService wipJournalService;

    @GetMapping("/contract/{contractId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get latest WIP for contract", description = "Get the latest WIP report for a contract")
    public ResponseEntity<ApiResponse<WipReportResponse>> getLatestWipForContract(@PathVariable Long contractId) {
        List<WipReportResponse> reports = wipJournalService.getWipReportHistory(contractId, 0, 1).getContent();
        if (reports.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No WIP reports found for this contract", null));
        }
        return ResponseEntity.ok(ApiResponse.success(reports.get(0)));
    }

    @GetMapping("/contract/{contractId}/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get WIP history", description = "Get WIP report history for a contract")
    public ResponseEntity<ApiResponse<PageResponse<WipReportResponse>>> getWipHistory(
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<WipReportResponse> response = wipJournalService.getWipReportHistory(contractId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/generate/{contractId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate WIP report", description = "Generate a WIP report for a specific contract")
    public ResponseEntity<ApiResponse<WipReportResponse>> generateWipReport(
            @PathVariable Long contractId,
            @Valid @RequestBody WipReportRequest request) {
        WipReportResponse response = wipJournalService.generateWipReport(contractId, request.getReportDate());
        return ResponseEntity.ok(ApiResponse.success("WIP report generated successfully", response));
    }

    @PostMapping("/generate-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate all WIP reports", description = "Generate WIP reports for all active contracts")
    public ResponseEntity<ApiResponse<List<WipReportResponse>>> generateAllWipReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        List<WipReportResponse> responses = wipJournalService.generateAllWipReports(reportDate);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Generated %d WIP reports", responses.size()), responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get WIP report", description = "Get a specific WIP report by ID")
    public ResponseEntity<ApiResponse<WipReportResponse>> getWipReport(@PathVariable Long id) {
        WipReportResponse response = wipJournalService.getWipReportById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Post to journal", description = "Post WIP report to journal")
    public ResponseEntity<ApiResponse<WipReportResponse>> postToJournal(@PathVariable Long id) {
        WipReportResponse response = wipJournalService.postToJournal(id);
        return ResponseEntity.ok(ApiResponse.success("WIP report posted to journal", response));
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reverse journal entries", description = "Reverse journal entries for a WIP report")
    public ResponseEntity<ApiResponse<WipReportResponse>> reverseJournal(@PathVariable Long id) {
        WipReportResponse response = wipJournalService.reverseJournal(id);
        return ResponseEntity.ok(ApiResponse.success("Journal entries reversed", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete draft report", description = "Delete a draft WIP report")
    public ResponseEntity<ApiResponse<Void>> deleteDraftReport(@PathVariable Long id) {
        wipJournalService.deleteDraftReport(id);
        return ResponseEntity.ok(ApiResponse.success("Draft WIP report deleted", null));
    }

    @GetMapping("/by-date")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get drafts by date", description = "Get all draft WIP reports for a specific date")
    public ResponseEntity<ApiResponse<List<WipReportResponse>>> getDraftsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        List<WipReportResponse> responses = wipJournalService.getWipReportsByDate(reportDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}

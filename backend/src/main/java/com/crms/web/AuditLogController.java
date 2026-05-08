package com.crms.web;

import com.crms.domain.user.entity.AuditLog;
import com.crms.domain.user.repository.AuditLogRepository;
import com.crms.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit log endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or isAuthenticated()")
    @Operation(summary = "List audit logs", description = "Query audit logs with optional filters")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {

        List<AuditLog> results;

        if (entityType != null && entityId != null) {
            results = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
        } else {
            Pageable pageable = PageRequest.of(page, limit, Sort.by("timestamp").descending());
            Page<AuditLog> paged;
            if (entityType != null) {
                paged = auditLogRepository.findByEntityType(entityType, pageable);
            } else if (action != null) {
                paged = auditLogRepository.findByAction(action, pageable);
            } else if (startDate != null && endDate != null) {
                LocalDateTime from = startDate.atStartOfDay();
                LocalDateTime to = endDate.plusDays(1).atStartOfDay();
                paged = auditLogRepository.findByTimestampBetween(from, to, pageable);
            } else {
                paged = auditLogRepository.findAll(pageable);
            }
            results = paged.getContent();
        }

        List<Map<String, Object>> data = results.stream()
                .map(log -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("id", log.getId());
                    entry.put("action", log.getAction());
                    entry.put("entityType", log.getEntityType());
                    entry.put("entityId", log.getEntityId());
                    entry.put("username", log.getUserName());
                    entry.put("timestamp", log.getTimestamp());
                    entry.put("details", log.getAfterState());
                    entry.put("ipAddress", log.getIpAddress());
                    return entry;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", data);
        response.put("total", data.size());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

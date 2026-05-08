package com.crms.web;

import com.crms.domain.adoption.repository.BondRepository;
import com.crms.domain.operative.repository.CardRepository;
import com.crms.domain.operative.repository.QualificationRepository;
import com.crms.domain.plant.repository.LOLERExaminationRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Compliance notification alerts")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final CardRepository cardRepository;
    private final QualificationRepository qualificationRepository;
    private final LOLERExaminationRepository lolerExaminationRepository;
    private final BondRepository bondRepository;

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get notification count and alerts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCount() {

        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        LocalDate in14Days = today.plusDays(14);

        List<Map<String, Object>> items = new ArrayList<>();

        // Cards expiring within 30 days
        cardRepository.findExpiringCards(in30Days, today).forEach(card -> {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "card");
            item.put("severity", card.getExpiryDate().isBefore(today.plusDays(7)) ? "danger" : "warning");
            item.put("message", "CSCS card expiring " + card.getExpiryDate()
                    + (card.getOperative() != null
                    ? " for " + card.getOperative().getFirstName() + " " + card.getOperative().getLastName()
                    : ""));
            item.put("link", card.getOperative() != null ? "/operatives" : "/operatives");
            items.add(item);
        });

        // Qualifications expiring within 30 days
        qualificationRepository.findExpiringQualifications(in30Days, today).forEach(qual -> {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "qualification");
            item.put("severity", qual.getExpiryDate().isBefore(today.plusDays(7)) ? "danger" : "warning");
            item.put("message", "Qualification '" + qual.getQualificationType() + "' expiring " + qual.getExpiryDate()
                    + (qual.getOperative() != null
                    ? " for " + qual.getOperative().getFirstName() + " " + qual.getOperative().getLastName()
                    : ""));
            item.put("link", "/operatives");
            items.add(item);
        });

        // LOLER due within 14 days
        lolerExaminationRepository.findDueExaminations(in14Days).forEach(loler -> {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "loler");
            item.put("severity", loler.getNextDueDate().isBefore(today) ? "danger" : "warning");
            item.put("message", "LOLER examination due " + loler.getNextDueDate()
                    + (loler.getPlant() != null ? " for " + loler.getPlant().getDescription() : ""));
            item.put("link", "/plant");
            items.add(item);
        });

        // Bonds with status CALLED or PARTIALLY_RELEASED
        bondRepository.findAll().stream()
                .filter(b -> b.getStatus() != null
                        && (b.getStatus().name().equals("CALLED")
                        || b.getStatus().name().equals("PARTIALLY_RELEASED")))
                .forEach(bond -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "bond");
                    item.put("severity", "danger");
                    item.put("message", "Bond " + bond.getBondNumber() + " status: " + bond.getStatus().name());
                    item.put("link", "/adoption");
                    items.add(item);
                });

        Map<String, Object> result = new HashMap<>();
        result.put("count", items.size());
        result.put("items", items);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/alerts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get notification alerts list (alias for count)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlerts() {
        return getCount();
    }
}

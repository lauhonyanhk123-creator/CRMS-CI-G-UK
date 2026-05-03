package com.crms.web;

import com.crms.domain.adoption.entity.Bond;
import com.crms.domain.adoption.entity.CommutedSumMovement;
import com.crms.domain.adoption.enums.AdoptionStatus;
import com.crms.domain.adoption.repository.AdoptionCaseRepository;
import com.crms.domain.adoption.repository.BondRepository;
import com.crms.domain.adoption.repository.CommutedSumMovementRepository;
import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.healthsafety.entity.F10Notification;
import com.crms.domain.healthsafety.entity.IncidentReport;
import com.crms.domain.healthsafety.entity.RAMSDocument;
import com.crms.domain.healthsafety.enums.IncidentStatus;
import com.crms.domain.healthsafety.repository.F10NotificationRepository;
import com.crms.domain.healthsafety.repository.IncidentReportRepository;
import com.crms.domain.healthsafety.repository.RAMSDocumentRepository;
import com.crms.domain.material.entity.ConcreteTicket;
import com.crms.domain.material.entity.DeliveryNote;
import com.crms.domain.material.entity.MuckawayTicket;
import com.crms.domain.material.repository.ConcreteTicketRepository;
import com.crms.domain.material.repository.DeliveryNoteRepository;
import com.crms.domain.material.repository.MuckawayTicketRepository;
import com.crms.domain.operative.entity.Card;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.entity.Qualification;
import com.crms.domain.operative.enums.OperativeStatus;
import com.crms.domain.operative.repository.CardRepository;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.operative.repository.QualificationRepository;
import com.crms.domain.plant.entity.LOLERExamination;
import com.crms.domain.plant.entity.PlantAllocation;
import com.crms.domain.plant.entity.PlantItem;
import com.crms.domain.plant.enums.PlantStatus;
import com.crms.domain.plant.repository.LOLERExaminationRepository;
import com.crms.domain.plant.repository.PlantAllocationRepository;
import com.crms.domain.plant.repository.PlantItemRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.enums.SiteStatus;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.domain.subcontractor.entity.CISReturn;
import com.crms.domain.subcontractor.entity.CISVerification;
import com.crms.domain.material.repository.PurchaseOrderRepository;
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.domain.subcontractor.repository.CISVerificationRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.enums.TenderStatus;
import com.crms.domain.tender.repository.TenderRepository;
import com.crms.domain.user.entity.AuditLog;
import com.crms.domain.user.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "KPI Dashboard & Analytics")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final ContractRepository contractRepository;
    private final TenderRepository tenderRepository;
    private final SiteRepository siteRepository;
    private final PlantItemRepository plantItemRepository;
    private final PlantAllocationRepository plantAllocationRepository;
    private final LOLERExaminationRepository lolerRepository;
    private final ApplicationForPaymentRepository applicationRepository;
    private final OperativeRepository operativeRepository;
    private final CardRepository cardRepository;
    private final QualificationRepository qualificationRepository;
    private final IncidentReportRepository incidentRepository;
    private final RAMSDocumentRepository ramsRepository;
    private final F10NotificationRepository f10Repository;
    private final AdoptionCaseRepository adoptionCaseRepository;
    private final BondRepository bondRepository;
    private final CommutedSumMovementRepository commutedSumRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final ConcreteTicketRepository concreteTicketRepository;
    private final MuckawayTicketRepository muckawayTicketRepository;
    private final CISReturnRepository cisReturnRepository;
    private final CISVerificationRepository cisVerificationRepository;
    private final AuditLogRepository auditLogRepository;

    // ========================================================================
    // Main KPI Summary (stats endpoint)
    // ========================================================================

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Dashboard stats", description = "Get key metrics for dashboard KPI cards")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        // Active contracts count
        long activeContracts = contractRepository.findAll().stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                .count();
        
        // Operatives on site (active operatives)
        long operativesOnSite = operativeRepository.findByStatus(OperativeStatus.ACTIVE).size();
        
        // Pending applications (submitted status)
        long pendingApplications = applicationRepository.findAll().stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SUBMITTED)
                .count();
        
        // Plant allocated (on hire)
        long plantAllocated = plantItemRepository.findAll().stream()
                .filter(p -> p.getStatus() == PlantStatus.ON_HIRE)
                .count();
        
        // Revenue MTD (from approved/paid applications this month)
        YearMonth currentMonth = YearMonth.now();
        BigDecimal revenueMTD = applicationRepository.findAll().stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PAID || a.getStatus() == ApplicationStatus.APPROVED)
                .filter(a -> {
                    LocalDate dueDate = a.getDueDate();
                    return dueDate != null && YearMonth.from(dueDate).equals(currentMonth);
                })
                .map(a -> a.getGrossValue() != null ? a.getGrossValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // CIS Deductions MTD
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();
        BigDecimal cisDeductionsMTD = cisReturnRepository.findAll().stream()
                .filter(r -> r.getSubmissionDate() != null)
                .filter(r -> {
                    LocalDate subDate = r.getSubmissionDate();
                    return !subDate.isBefore(monthStart) && !subDate.isAfter(monthEnd);
                })
                .map(r -> r.getTotalDeduction() != null ? r.getTotalDeduction() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.put("activeContracts", activeContracts);
        stats.put("operativesOnSite", operativesOnSite);
        stats.put("pendingApplications", pendingApplications);
        stats.put("plantAllocated", plantAllocated);
        stats.put("revenueMTD", revenueMTD.setScale(2, RoundingMode.HALF_UP));
        stats.put("cisDeductionsMTD", cisDeductionsMTD.setScale(2, RoundingMode.HALF_UP));
        
        return ResponseEntity.ok(stats);
    }

    // ========================================================================
    // Main KPIs
    // ========================================================================

    @GetMapping("/kpis")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Top-level KPI summary")
    public ResponseEntity<Map<String, Object>> getKpis() {
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("_timestamp", LocalDateTime.now().toString());
        kpis.put("_reportingPeriod", "last_12_months");
        kpis.put("tenders", getTenderKpis());
        kpis.put("contracts", getContractKpis());
        kpis.put("applicationsForPayment", getAfpKpis());
        kpis.put("plant", getPlantKpis());
        kpis.put("operatives", getOperativeKpis());
        kpis.put("healthSafety", getHsKpis());
        kpis.put("adoption", getAdoptionKpis());
        kpis.put("procurement", getProcurementKpis());
        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/activity-feed")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Recent activity feed", description = "Last N audit log entries for the activity stream")
    public ResponseEntity<List<Map<String, Object>>> getActivityFeed(
            @RequestParam(defaultValue = "50") int limit) {
        List<AuditLog> logs = auditLogRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"))
        ).getContent();

        List<Map<String, Object>> feed = logs.stream().map(log -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", log.getId());
            item.put("user", log.getUserName() != null ? log.getUserName() : "System");
            item.put("action", log.getAction());
            item.put("entityType", log.getEntityType());
            item.put("entityId", log.getEntityId());
            item.put("details", log.getAfterState());
            item.put("timestamp", log.getTimestamp().toString());
            item.put("ipAddress", log.getIpAddress());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(feed);
    }

    @GetMapping("/expiring-items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Expiring items requiring action", description = "Items expiring within 90 days across all domains")
    public ResponseEntity<Map<String, Object>> getExpiringItems(
            @RequestParam(defaultValue = "90") int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        LocalDate now = LocalDate.now();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("thresholdDays", days);
        result.put("thresholdDate", threshold.toString());

        // Operative cards expiring within threshold
        List<Card> expiringCards = cardRepository.findExpiringCards(threshold, now);
        List<Map<String, Object>> cardsData = expiringCards.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("cardNumber", c.getCardNumber());
            m.put("cardType", c.getCardType().name());
            m.put("expiryDate", c.getExpiryDate().toString());
            m.put("operative", c.getOperative() != null ? c.getOperative().getFullName() : "Unknown");
            return m;
        }).collect(Collectors.toList());
        result.put("operativeCards", cardsData);

        // Qualifications expiring
        List<Qualification> expiringQuals = qualificationRepository.findExpiringQualifications(threshold, now);
        List<Map<String, Object>> qualsData = expiringQuals.stream().map(q -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", q.getId());
            m.put("qualificationType", q.getQualificationType().name());
            m.put("expiryDate", q.getExpiryDate().toString());
            m.put("operative", q.getOperative() != null ? q.getOperative().getFullName() : "Unknown");
            return m;
        }).collect(Collectors.toList());
        result.put("qualifications", qualsData);

        // LOLER examinations due
        List<LOLERExamination> lolerDue = lolerRepository.findDueExaminations(threshold);
        List<Map<String, Object>> lolerData = lolerDue.stream().map(l -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", l.getId());
            m.put("nextDueDate", l.getNextDueDate().toString());
            m.put("plantRef", l.getPlant() != null ? l.getPlant().getPlantRef() : "Unknown");
            m.put("plantDescription", l.getPlant() != null ? l.getPlant().getDescription() : "");
            return m;
        }).collect(Collectors.toList());
        result.put("plantCertifications", lolerData);

        // RAMS documents expiring
        List<RAMSDocument> ramsExpiring = ramsRepository.findExpiringDocuments(threshold);
        List<Map<String, Object>> ramsData = ramsExpiring.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("title", r.getTitle());
            m.put("validUntil", r.getValidUntil() != null ? r.getValidUntil().toString() : "");
            m.put("contract", r.getContract() != null ? r.getContract().getContractRef() : "");
            return m;
        }).collect(Collectors.toList());
        result.put("ramsDocuments", ramsData);

        // CIS verifications expiring
        List<CISVerification> cisExpiring = cisVerificationRepository.findExpiringVerifications(threshold);
        List<Map<String, Object>> cisData = cisExpiring.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("verificationRef", c.getVerificationRef());
            m.put("expiresAt", c.getExpiresAt() != null ? c.getExpiresAt().toString() : "");
            m.put("company", c.getCompany() != null ? c.getCompany().getName() : "");
            return m;
        }).collect(Collectors.toList());
        result.put("cisVerifications", cisData);

        // Bonds expiring
        List<Bond> bondsExpiring = bondRepository.findExpiringBonds(threshold);
        List<Map<String, Object>> bondsData = bondsExpiring.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("bondNumber", b.getBondNumber());
            m.put("expiryDate", b.getExpiryDate() != null ? b.getExpiryDate().toString() : "");
            m.put("value", b.getBondValue());
            return m;
        }).collect(Collectors.toList());
        result.put("bonds", bondsData);

        // F10 notifications expiring (within threshold AND active)
        List<F10Notification> f10Expiring = f10Repository.findExpiringNotifications(threshold);
        List<Map<String, Object>> f10Data = f10Expiring.stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("notificationNumber", f.getNotificationNumber());
            m.put("constructionEndDate", f.getConstructionEndDate() != null ? f.getConstructionEndDate().toString() : "");
            m.put("contract", f.getContract() != null ? f.getContract().getContractRef() : "");
            return m;
        }).collect(Collectors.toList());
        result.put("f10Notifications", f10Data);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/pipeline-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tender pipeline funnel", description = "Win/loss funnel across tender statuses")
    public ResponseEntity<Map<String, Object>> getPipelineSummary() {
        List<Tender> tenders = tenderRepository.findAll();
        Map<TenderStatus, List<Tender>> byStatus = tenders.stream()
                .collect(Collectors.groupingBy(Tender::getStatus));

        Map<String, Object> funnel = new LinkedHashMap<>();
        for (TenderStatus status : TenderStatus.values()) {
            List<Tender> group = byStatus.getOrDefault(status, Collections.emptyList());
            BigDecimal totalValue = group.stream()
                    .map(t -> t.getTargetValue() != null ? t.getTargetValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> statusData = new LinkedHashMap<>();
            statusData.put("count", group.size());
            statusData.put("totalValue", totalValue);
            funnel.put(status.name(), statusData);
        }

        // Summary totals
        long awarded = byStatus.getOrDefault(TenderStatus.AWARDED, Collections.emptyList()).size();
        long lost = byStatus.getOrDefault(TenderStatus.LOST, Collections.emptyList()).size();
        long totalDecided = awarded + lost;
        double winRate = totalDecided > 0 ? (double) awarded / totalDecided * 100 : 0.0;
        funnel.put("_summary", Map.of(
                "awarded", awarded,
                "lost", lost,
                "winRate", Math.round(winRate * 10) / 10.0
        ));

        return ResponseEntity.ok(funnel);
    }

    @GetMapping("/contract-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Contract status summary")
    public ResponseEntity<Map<String, Object>> getContractSummary() {
        List<Contract> contracts = contractRepository.findAll();
        Map<ContractStatus, List<Contract>> byStatus = contracts.stream()
                .collect(Collectors.groupingBy(Contract::getStatus));

        Map<String, Object> summary = new LinkedHashMap<>();
        for (ContractStatus status : ContractStatus.values()) {
            List<Contract> group = byStatus.getOrDefault(status, Collections.emptyList());
            BigDecimal totalValue = group.stream()
                    .map(c -> c.getContractValue() != null ? c.getContractValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> statusData = new LinkedHashMap<>();
            statusData.put("count", group.size());
            statusData.put("value", totalValue);
            summary.put(status.name(), statusData);
        }

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/cashflow-forecast")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cash flow forecast", description = "Forecast of incoming payments from approved applications for payment")
    public ResponseEntity<Map<String, Object>> getCashflowForecast(
            @RequestParam(defaultValue = "12") int monthsAhead) {
        List<ApplicationForPayment> approved = applicationRepository.findAll().stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PAID || a.getStatus() == ApplicationStatus.SUBMITTED)
                .collect(Collectors.toList());

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(monthsAhead);

        // Group by month
        Map<YearMonth, BigDecimal> byMonth = new TreeMap<>();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal retentionHeld = BigDecimal.ZERO;
        BigDecimal retentionReleasable = BigDecimal.ZERO;

        for (ApplicationForPayment app : approved) {
            LocalDate dueDate = app.getDueDate();
            if (dueDate == null) continue;
            if (dueDate.isBefore(start) || dueDate.isAfter(end)) continue;

            BigDecimal amount = app.getGrossValue() != null ? app.getGrossValue() : BigDecimal.ZERO;
            BigDecimal ret = app.getRetention() != null ? app.getRetention() : BigDecimal.ZERO;

            YearMonth ym = YearMonth.from(dueDate);
            byMonth.merge(ym, amount, BigDecimal::add);
            total = total.add(amount);

            if (app.getStatus() == ApplicationStatus.PAID) {
                retentionHeld = retentionHeld.add(ret);
            } else {
                // Check if retention is releasable (defects period ended)
                LocalDate defectsEnd = app.getContract() != null ?
                        app.getContract().getDefectsEndDate() : null;
                if (defectsEnd != null && dueDate.isBefore(defectsEnd)) {
                    retentionReleasable = retentionReleasable.add(ret);
                }
            }
        }

        List<Map<String, Object>> monthlyData = byMonth.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("month", e.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM")));
                    m.put("amount", e.getValue().setScale(2, RoundingMode.HALF_UP));
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> forecast = new LinkedHashMap<>();
        forecast.put("periodMonths", monthsAhead);
        forecast.put("totalForecast", total.setScale(2, RoundingMode.HALF_UP));
        forecast.put("byMonth", monthlyData);
        forecast.put("retentionHeld", retentionHeld.setScale(2, RoundingMode.HALF_UP));
        forecast.put("retentionReleasable", retentionReleasable.setScale(2, RoundingMode.HALF_UP));

        return ResponseEntity.ok(forecast);
    }

    @GetMapping("/retention-schedule")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Retention schedule", description = "Retention held and upcoming release dates")
    public ResponseEntity<List<Map<String, Object>>> getRetentionSchedule() {
        List<Contract> contracts = contractRepository.findAll().stream()
                .filter(c -> c.getRetentionLedger() != null)
                .collect(Collectors.toList());

        List<Map<String, Object>> schedule = contracts.stream().map(c -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("contractRef", c.getContractRef());
            item.put("title", c.getTitle());
            item.put("client", c.getClient() != null ? c.getClient().getName() : "");
            item.put("contractValue", c.getContractValue());
            item.put("defectsEndDate", c.getDefectsEndDate() != null ? c.getDefectsEndDate().toString() : "");
            item.put("retentionPercent", c.getRetentionPercent());
            // Calculate total retention held from applications
            BigDecimal retentionHeld = applicationRepository.findAll().stream()
                    .filter(a -> a.getContract() != null && a.getContract().getId().equals(c.getId()))
                    .map(a -> a.getRetention() != null ? a.getRetention() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.put("retentionHeld", retentionHeld.setScale(2, RoundingMode.HALF_UP));
            item.put("status", c.getStatus().name());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/health-safety-stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "H&S statistics", description = "RIDDOR stats, AFR, near-miss ratio over N months")
    public ResponseEntity<Map<String, Object>> getHealthSafetyStats(
            @RequestParam(defaultValue = "12") int months) {
        LocalDateTime start = LocalDateTime.now().minusMonths(months);
        LocalDateTime end = LocalDateTime.now();

        List<IncidentReport> incidents = incidentRepository.findByDateRange(start, end);

        int total = incidents.size();
        int nearMisses = (int) incidents.stream()
                .filter(i -> i.getIncidentType() != null && i.getIncidentType().name().contains("NEAR_MISS"))
                .count();
        int ridDOR = (int) incidents.stream()
                .filter(IncidentReport::getRidDORNotifiable)
                .count();
        int openIncidents = (int) incidents.stream()
                .filter(i -> i.getStatus() != IncidentStatus.CLOSED)
                .count();

        // AFR = (incidents x 100,000) / hours worked
        // Assuming 220 working days * 8 hours * active operatives as proxy
        long activeOperatives = operativeRepository.findByStatus(OperativeStatus.ACTIVE).size();
        long hoursWorked = activeOperatives * 220L * 8L;
        double afr = hoursWorked > 0 ? (double) total * 100000.0 / hoursWorked : 0.0;

        // Near-miss ratio
        int actualIncidents = total - nearMisses;
        double nearMissRatio = actualIncidents > 0 ? (double) nearMisses / actualIncidents : 0.0;

        // RAMS expiring in 30 days
        int ramsExpiring30 = ramsRepository.findExpiringDocuments(LocalDate.now().plusDays(30)).size();

        // Active F10s
        List<F10Notification> activeF10 = f10Repository.findAll().stream()
                .filter(F10Notification::getIsActive)
                .collect(Collectors.toList());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("periodMonths", months);
        stats.put("afr", Math.round(afr * 100.0) / 100.0);
        stats.put("totalIncidents", total);
        stats.put("nearMisses", nearMisses);
        stats.put("nearMissRatio", Math.round(nearMissRatio * 10.0) / 10.0);
        stats.put("ridDORNotifiable", ridDOR);
        stats.put("openIncidents", openIncidents);
        stats.put("ramsExpiring30Days", ramsExpiring30);
        stats.put("f10Active", activeF10.size());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/plant-utilisation")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Plant utilisation", description = "Plant allocation vs available over N days")
    public ResponseEntity<Map<String, Object>> getPlantUtilisation(
            @RequestParam(defaultValue = "30") int days) {
        List<PlantItem> allPlant = plantItemRepository.findAll();
        LocalDate today = LocalDate.now();

        int total = allPlant.size();
        int allocated = (int) allPlant.stream()
                .filter(p -> p.getStatus() == PlantStatus.ON_HIRE)
                .count();
        int idle = (int) allPlant.stream()
                .filter(p -> p.getStatus() == PlantStatus.AVAILABLE || p.getStatus() == PlantStatus.IDLE)
                .count();

        double utilisationPct = total > 0 ? (double) allocated / total * 100 : 0;

        // LOLER due in 30 days
        LocalDate thirtyDays = today.plusDays(30);
        List<LOLERExamination> lolerDue30 = lolerRepository.findDueExaminations(thirtyDays);
        int lolerDueCount = (int) lolerDue30.stream()
                .filter(l -> l.getNextDueDate() != null && !l.getNextDueDate().isBefore(today))
                .count();

        // LOLER overdue
        List<LOLERExamination> lolerOverdue = lolerRepository.findDueExaminations(today);
        int lolerOverdueCount = lolerOverdue.size();

        Map<String, Object> utilisation = new LinkedHashMap<>();
        utilisation.put("periodDays", days);
        utilisation.put("totalPlant", total);
        utilisation.put("allocated", allocated);
        utilisation.put("idle", idle);
        utilisation.put("utilisationPercent", Math.round(utilisationPct * 10.0) / 10.0);
        utilisation.put("lolErDue30Days", lolerDueCount);
        utilisation.put("lolErOverdue", lolerOverdueCount);

        return ResponseEntity.ok(utilisation);
    }

    @GetMapping("/cis-deductions-summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "CIS deductions summary", description = "Monthly CIS deductions for the current tax year")
    public ResponseEntity<Map<String, Object>> getCisSummary() {
        int year = LocalDate.now().getYear();
        String taxYear = year + "/" + (year + 1);

        List<CISReturn> allReturns = cisReturnRepository.findAll();
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        int submitted = 0;
        int pending = 0;
        int overdue = 0;

        LocalDate now = LocalDate.now();
        LocalDate taxYearEnd = LocalDate.of(year, 4, 5); // 6 April
        if (now.isAfter(taxYearEnd)) {
            taxYearEnd = LocalDate.of(year + 1, 4, 5);
        }

        for (CISReturn ret : allReturns) {
            if (ret.getTaxMonth() != null && ret.getTaxMonth().contains(String.valueOf(year))) {
                submitted++;
                BigDecimal gross = ret.getTotalGrossValue() != null ? ret.getTotalGrossValue() : BigDecimal.ZERO;
                BigDecimal deduct = ret.getTotalDeductions() != null ? ret.getTotalDeductions() : BigDecimal.ZERO;
                totalGross = totalGross.add(gross);
                totalDeductions = totalDeductions.add(deduct);
            } else if (ret.getStatus() != null && ret.getStatus().name().equals("DRAFT")) {
                pending++;
            }

            // Check overdue (submission deadline passed)
            if (ret.getTaxMonth() != null && now.isAfter(taxYearEnd.minusMonths(1))) {
                overdue++;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("taxYear", taxYear);
        summary.put("totalGrossPaid", totalGross.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalDeductions", totalDeductions.setScale(2, RoundingMode.HALF_UP));
        summary.put("returnsSubmitted", submitted);
        summary.put("returnsPending", pending);
        summary.put("returnsOverdue", Math.max(0, overdue - submitted));
        summary.put("netPaidToSubcontractors", totalGross.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP));

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/adoption-status")
    @Operation(summary = "Adoption cases status")
    public ResponseEntity<Map<String, Object>> getAdoptionStatus() {
        List<com.crms.domain.adoption.entity.AdoptionCase> allCases = adoptionCaseRepository.findAll();
        Map<AdoptionStatus, Long> statusCounts = allCases.stream()
                .collect(Collectors.groupingBy(com.crms.domain.adoption.entity.AdoptionCase::getStatus, Collectors.counting()));

        BigDecimal totalBondValue = bondRepository.findAll().stream()
                .map(b -> b.getBondValue() != null ? b.getBondValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommutedSums = commutedSumRepository.findAll().stream()
                .map(m -> m.getAmount() != null ? m.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int bondsExpiring30 = bondRepository.findExpiringBonds(LocalDate.now().plusDays(30)).size();

        Map<String, Object> status = new LinkedHashMap<>();
        for (AdoptionStatus as : AdoptionStatus.values()) {
            status.put(as.name(), statusCounts.getOrDefault(as, 0L).intValue());
        }
        status.put("totalBondsValue", totalBondValue.setScale(2, RoundingMode.HALF_UP));
        status.put("totalCommutedSums", totalCommutedSums.setScale(2, RoundingMode.HALF_UP));
        status.put("bondsExpiring30Days", bondsExpiring30);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/procurement-summary")
    @Operation(summary = "Procurement overview")
    public ResponseEntity<Map<String, Object>> getProcurementSummary() {
        LocalDate today = LocalDate.now();

        List<com.crms.domain.material.entity.PurchaseOrder> allPO = purchaseOrderRepository.findAll();
        int draftPO = (int) allPO.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equals("DRAFT"))
                .count();
        int issuedPO = (int) allPO.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equals("ISSUED"))
                .count();

        List<DeliveryNote> deliveriesExpected = deliveryNoteRepository.findExpectedDeliveries(today);
        int deliveriesToday = deliveriesExpected.size();

        List<ConcreteTicket> concreteToday = concreteTicketRepository.findAll().stream()
                .filter(c -> {
                    if (c.getDeliveryNote() == null || c.getDeliveryNote().getDeliveryDate() == null) return false;
                    return c.getDeliveryNote().getDeliveryDate().isEqual(today);
                })
                .toList();
        int concreteCount = concreteToday.size();

        List<MuckawayTicket> muckawayToday = muckawayTicketRepository.findAll().stream()
                .filter(m -> {
                    if (m.getDeliveryNote() == null || m.getDeliveryNote().getDeliveryDate() == null) return false;
                    return m.getDeliveryNote().getDeliveryDate().isEqual(today);
                })
                .toList();
        int muckawayCount = muckawayToday.size();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("purchaseOrdersDraft", draftPO);
        summary.put("purchaseOrdersIssued", issuedPO);
        summary.put("deliveriesExpectedToday", deliveriesToday);
        summary.put("concreteTicketsToday", concreteCount);
        summary.put("muckawayTicketsToday", muckawayCount);

        return ResponseEntity.ok(summary);
    }

    // ========================================================================
    // Private KPI helpers
    // ========================================================================

    private Map<String, Object> getTenderKpis() {
        List<Tender> tenders = tenderRepository.findAll();
        long total = tenders.size();
        long awarded = tenders.stream().filter(t -> t.getStatus() == TenderStatus.AWARDED).count();
        long lost = tenders.stream().filter(t -> t.getStatus() == TenderStatus.LOST).count();
        long decided = awarded + lost;
        double winRate = decided > 0 ? (double) awarded / decided * 100 : 0.0;

        BigDecimal totalValue = tenders.stream()
                .map(t -> t.getTargetValue() != null ? t.getTargetValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal awardedValue = tenders.stream()
                .filter(t -> t.getStatus() == TenderStatus.AWARDED)
                .map(t -> t.getTargetValue() != null ? t.getTargetValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("total", total);
        kpis.put("awarded", awarded);
        kpis.put("lost", lost);
        kpis.put("winRate", Math.round(winRate * 10.0) / 10.0);
        kpis.put("totalValue", totalValue.setScale(2, RoundingMode.HALF_UP));
        kpis.put("awardedValue", awardedValue.setScale(2, RoundingMode.HALF_UP));
        return kpis;
    }

    private Map<String, Object> getContractKpis() {
        List<Contract> contracts = contractRepository.findAll();
        long total = contracts.size();
        long active = contracts.stream().filter(c -> c.getStatus() == ContractStatus.ACTIVE).count();

        BigDecimal totalValue = contracts.stream()
                .map(c -> c.getContractValue() != null ? c.getContractValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate retention held from all applications
        BigDecimal retentionHeld = applicationRepository.findAll().stream()
                .map(a -> a.getRetention() != null ? a.getRetention() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Variations total
        BigDecimal variationsTotal = contracts.stream()
                .flatMap(c -> c.getVariations() != null ? c.getVariations().stream() : java.util.stream.Stream.empty())
                .map(v -> v.getValue() != null ? v.getValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overdueVariations = contracts.stream()
                .flatMap(c -> c.getVariations() != null ? c.getVariations().stream() : java.util.stream.Stream.empty())
                .filter(v -> v.getStatus() != null && v.getStatus().name().equals("OVERDUE"))
                .count();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("total", total);
        kpis.put("active", active);
        kpis.put("totalValue", totalValue.setScale(2, RoundingMode.HALF_UP));
        kpis.put("retentionHeld", retentionHeld.setScale(2, RoundingMode.HALF_UP));
        kpis.put("variations", variationsTotal.setScale(2, RoundingMode.HALF_UP));
        kpis.put("overdueVariations", overdueVariations);
        return kpis;
    }

    private Map<String, Object> getAfpKpis() {
        List<ApplicationForPayment> apps = applicationRepository.findAll();
        int draft = (int) apps.stream().filter(a -> a.getStatus() == ApplicationStatus.DRAFT).count();
        int submitted = (int) apps.stream().filter(a -> a.getStatus() == ApplicationStatus.SUBMITTED).count();
        int paid = (int) apps.stream().filter(a -> a.getStatus() == ApplicationStatus.PAID).count();

        LocalDate now = LocalDate.now();
        int overdue = (int) apps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SUBMITTED)
                .filter(a -> a.getDueDate() != null && a.getDueDate().isBefore(now))
                .count();

        BigDecimal totalCertifiedYtd = apps.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PAID)
                .map(a -> a.getGrossValue() != null ? a.getGrossValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("draft", draft);
        kpis.put("submitted", submitted);
        kpis.put("paid", paid);
        kpis.put("overdue", overdue);
        kpis.put("totalCertifiedYtd", totalCertifiedYtd.setScale(2, RoundingMode.HALF_UP));
        return kpis;
    }

    private Map<String, Object> getPlantKpis() {
        List<PlantItem> allPlant = plantItemRepository.findAll();
        long total = allPlant.size();
        long available = allPlant.stream().filter(p -> p.getStatus() == PlantStatus.AVAILABLE).count();
        long onHire = allPlant.stream().filter(p -> p.getStatus() == PlantStatus.ON_HIRE).count();
        long offHire = allPlant.stream().filter(p -> p.getStatus() == PlantStatus.OFF_HIRE).count();

        LocalDate thirtyDays = LocalDate.now().plusDays(30);
        List<LOLERExamination> lolerDue = lolerRepository.findDueExaminations(thirtyDays);
        long lolerDue30 = lolerDue.stream()
                .filter(l -> l.getNextDueDate() != null && !l.getNextDueDate().isBefore(LocalDate.now()))
                .count();
        long lolerOverdue = lolerRepository.findDueExaminations(LocalDate.now()).size();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("total", total);
        kpis.put("available", available);
        kpis.put("onHire", onHire);
        kpis.put("offHire", offHire);
        kpis.put("lolerDue30Days", lolerDue30);
        kpis.put("lolerOverdue", lolerOverdue);
        return kpis;
    }

    private Map<String, Object> getOperativeKpis() {
        List<Operative> operatives = operativeRepository.findAll();
        long total = operatives.size();
        long active = operatives.stream().filter(o -> o.getStatus() == OperativeStatus.ACTIVE).count();

        LocalDate now = LocalDate.now();
        LocalDate thirtyDays = now.plusDays(30);

        List<Card> expiringCards = cardRepository.findExpiringCards(thirtyDays, now);
        List<Qualification> expiringQuals = qualificationRepository.findExpiringQualifications(thirtyDays, now);
        List<Operative> expiringRTW = operativeRepository.findWithExpiringRightToWork(thirtyDays);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("total", total);
        kpis.put("active", active);
        kpis.put("cardsExpiring30Days", expiringCards.size());
        kpis.put("qualificationsExpiring30Days", expiringQuals.size());
        kpis.put("rightToWorkExpiring30Days", expiringRTW.size());
        return kpis;
    }

    private Map<String, Object> getHsKpis() {
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        List<IncidentReport> incidents = incidentRepository.findByDateRange(start, LocalDateTime.now());

        long activeOps = operativeRepository.findByStatus(OperativeStatus.ACTIVE).size();
        long hoursWorked = activeOps * 220L * 8L;
        double afr = hoursWorked > 0 ? (double) incidents.size() * 100000.0 / hoursWorked : 0.0;

        int open = (int) incidents.stream()
                .filter(i -> i.getStatus() != IncidentStatus.CLOSED)
                .count();

        LocalDate now = LocalDate.now();
        int ramsExp30 = ramsRepository.findExpiringDocuments(now.plusDays(30)).size();
        int f10Active = (int) f10Repository.findAll().stream().filter(F10Notification::getIsActive).count();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("afr", Math.round(afr * 100.0) / 100.0);
        kpis.put("openIncidents", open);
        kpis.put("ramsExpiring30Days", ramsExp30);
        kpis.put("f10Active", f10Active);
        return kpis;
    }

    private Map<String, Object> getAdoptionKpis() {
        long totalCases = adoptionCaseRepository.count();
        BigDecimal bondValue = bondRepository.findAll().stream()
                .map(b -> b.getBondValue() != null ? b.getBondValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commutedSums = commutedSumRepository.findAll().stream()
                .map(m -> m.getAmount() != null ? m.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int bondsExpiring30 = bondRepository.findExpiringBonds(LocalDate.now().plusDays(30)).size();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalCases", totalCases);
        kpis.put("bondsExpiring30Days", bondsExpiring30);
        kpis.put("totalBondValue", bondValue.setScale(2, RoundingMode.HALF_UP));
        kpis.put("totalCommutedSums", commutedSums.setScale(2, RoundingMode.HALF_UP));
        return kpis;
    }

    private Map<String, Object> getProcurementKpis() {
        List<com.crms.domain.material.entity.PurchaseOrder> allPO = purchaseOrderRepository.findAll();
        int draftPO = (int) allPO.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equals("DRAFT"))
                .count();
        int issuedPO = (int) allPO.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equals("ISSUED"))
                .count();

        LocalDate today = LocalDate.now();
        int deliveriesToday = deliveryNoteRepository.findExpectedDeliveries(today).size();
        int concreteToday = (int) concreteTicketRepository.findAll().stream()
                .filter(c -> c.getDeliveryNote() != null && c.getDeliveryNote().getDeliveryDate() != null
                        && c.getDeliveryNote().getDeliveryDate().isEqual(today))
                .count();
        int muckawayToday = (int) muckawayTicketRepository.findAll().stream()
                .filter(m -> m.getDeliveryNote() != null && m.getDeliveryNote().getDeliveryDate() != null
                        && m.getDeliveryNote().getDeliveryDate().isEqual(today))
                .count();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("purchaseOrdersDraft", draftPO);
        kpis.put("purchaseOrdersIssued", issuedPO);
        kpis.put("deliveriesExpectedToday", deliveriesToday);
        kpis.put("concreteTicketsToday", concreteToday);
        kpis.put("muckawayTicketsToday", muckawayToday);
        return kpis;
    }
}
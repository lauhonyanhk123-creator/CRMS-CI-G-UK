package com.crms.service.impl;

import com.crms.domain.adoption.entity.AdoptionCase;
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
import com.crms.domain.healthsafety.enums.IncidentType;
import com.crms.domain.healthsafety.repository.F10NotificationRepository;
import com.crms.domain.healthsafety.repository.IncidentReportRepository;
import com.crms.domain.healthsafety.repository.RAMSDocumentRepository;
import com.crms.domain.material.entity.ConcreteTicket;
import com.crms.domain.material.entity.DeliveryNote;
import com.crms.domain.material.entity.MuckawayTicket;
import com.crms.domain.material.repository.ConcreteTicketRepository;
import com.crms.domain.material.repository.DeliveryNoteRepository;
import com.crms.domain.material.repository.MuckawayTicketRepository;
import com.crms.domain.material.repository.PurchaseOrderRepository;
import com.crms.domain.operative.entity.Card;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.entity.Qualification;
import com.crms.domain.operative.enums.OperativeStatus;
import com.crms.domain.operative.repository.CardRepository;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.operative.repository.QualificationRepository;
import com.crms.domain.operative.repository.TimesheetRepository;
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
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.domain.subcontractor.repository.CISVerificationRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.enums.TenderStatus;
import com.crms.domain.tender.repository.TenderRepository;
import com.crms.domain.user.entity.AuditLog;
import com.crms.domain.user.repository.AuditLogRepository;
import com.crms.dto.response.DashboardStats;
import com.crms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ContractRepository contractRepository;
    private final SiteRepository siteRepository;
    private final PlantItemRepository plantRepository;
    private final ApplicationForPaymentRepository applicationRepository;
    private final TenderRepository tenderRepository;
    private final IncidentReportRepository incidentRepository;
    private final LOLERExaminationRepository lolerRepository;
    private final OperativeRepository operativeRepository;
    private final CardRepository cardRepository;
    private final QualificationRepository qualificationRepository;
    private final RAMSDocumentRepository ramsRepository;
    private final F10NotificationRepository f10Repository;
    private final AdoptionCaseRepository adoptionCaseRepository;
    private final BondRepository bondRepository;
    private final CommutedSumMovementRepository commutedSumRepository;
    private final PlantAllocationRepository plantAllocationRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final TimesheetRepository timesheetRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final ConcreteTicketRepository concreteTicketRepository;
    private final MuckawayTicketRepository muckawayTicketRepository;
    private final CISReturnRepository cisReturnRepository;
    private final CISVerificationRepository cisVerificationRepository;
    private final AuditLogRepository auditLogRepository;

    // =========================================================================
    // Typed DTO summary (used by DashboardStats widget)
    // =========================================================================

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public DashboardStats getStats() {
        log.info("Generating dashboard statistics");

        long totalContracts = contractRepository.countByStatus(ContractStatus.ACTIVE);
        long activeSites = siteRepository.countByStatus(SiteStatus.ACTIVE);
        long plantOnSite = plantRepository.countByStatus(PlantStatus.ON_HIRE);
        long pendingApplications = applicationRepository.countByStatus(ApplicationStatus.SUBMITTED);

        List<DashboardStats.CVRItem> cvrSummary = calculateCVRSummary();
        List<DashboardStats.CashflowItem> cashflowForecast = calculateCashflowForecast();
        DashboardStats.HAndSStats hsStats = calculateHSStats();
        List<DashboardStats.LOLERItem> lolerCalendar = calculateLOLERCalendar();
        List<DashboardStats.TenderPipelineItem> tenderPipeline = calculateTenderPipeline();

        return DashboardStats.builder()
                .totalContracts(totalContracts)
                .activeSites(activeSites)
                .plantOnSite(plantOnSite)
                .pendingApplications(pendingApplications)
                .cvrSummary(cvrSummary)
                .cashflowForecast(cashflowForecast)
                .hsStats(hsStats)
                .lolerCalendar(lolerCalendar)
                .tenderPipeline(tenderPipeline)
                .build();
    }

    // =========================================================================
    // Controller endpoint implementations
    // =========================================================================

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long activeContracts = contractRepository.countByStatus(ContractStatus.ACTIVE);

        long operativesOnSite = operativeRepository.countByStatus(OperativeStatus.ACTIVE);

        long pendingApplications = applicationRepository.countByStatus(ApplicationStatus.SUBMITTED);

        long plantAllocated = plantRepository.countByStatus(PlantStatus.ON_HIRE);

        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        BigDecimal revenueMTD = applicationRepository.sumGrossValueByStatusInAndDueDateBetween(
                List.of(ApplicationStatus.PAID, ApplicationStatus.APPROVED),
                monthStart, monthEnd);
        if (revenueMTD == null) revenueMTD = BigDecimal.ZERO;

        BigDecimal cisDeductionsMTD = cisReturnRepository
                .sumTotalDeductionBySubmissionDateBetween(monthStart, monthEnd);
        if (cisDeductionsMTD == null) cisDeductionsMTD = BigDecimal.ZERO;

        stats.put("activeContracts", activeContracts);
        stats.put("operativesOnSite", operativesOnSite);
        stats.put("pendingApplications", pendingApplications);
        stats.put("plantAllocated", plantAllocated);
        stats.put("revenueMTD", revenueMTD.setScale(2, RoundingMode.HALF_UP));
        stats.put("cisDeductionsMTD", cisDeductionsMTD.setScale(2, RoundingMode.HALF_UP));
        return stats;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getKpis() {
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
        return kpis;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Map<String, Object>> getActivityFeed(int limit) {
        List<AuditLog> logs = auditLogRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"))
        ).getContent();

        return logs.stream().map(auditLog -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", auditLog.getId());
            item.put("user", auditLog.getUserName() != null ? auditLog.getUserName() : "System");
            item.put("action", auditLog.getAction());
            item.put("entityType", auditLog.getEntityType());
            item.put("entityId", auditLog.getEntityId());
            item.put("details", auditLog.getAfterState());
            item.put("timestamp", auditLog.getTimestamp().toString());
            item.put("ipAddress", auditLog.getIpAddress());
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getExpiringItems(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        LocalDate now = LocalDate.now();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("thresholdDays", days);
        result.put("thresholdDate", threshold.toString());

        List<Card> expiringCards = cardRepository.findExpiringCards(threshold, now);
        result.put("operativeCards", expiringCards.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("cardNumber", c.getCardNumber());
            m.put("cardType", c.getCardType().name());
            m.put("expiryDate", c.getExpiryDate().toString());
            m.put("operative", c.getOperative() != null ? c.getOperative().getFullName() : "Unknown");
            return m;
        }).collect(Collectors.toList()));

        List<Qualification> expiringQuals = qualificationRepository.findExpiringQualifications(threshold, now);
        result.put("qualifications", expiringQuals.stream().map(q -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", q.getId());
            m.put("qualificationType", q.getQualificationType().name());
            m.put("expiryDate", q.getExpiryDate().toString());
            m.put("operative", q.getOperative() != null ? q.getOperative().getFullName() : "Unknown");
            return m;
        }).collect(Collectors.toList()));

        List<LOLERExamination> lolerDue = lolerRepository.findDueExaminations(threshold);
        result.put("plantCertifications", lolerDue.stream().map(l -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", l.getId());
            m.put("nextDueDate", l.getNextDueDate().toString());
            m.put("plantRef", l.getPlant() != null ? l.getPlant().getPlantRef() : "Unknown");
            m.put("plantDescription", l.getPlant() != null ? l.getPlant().getDescription() : "");
            return m;
        }).collect(Collectors.toList()));

        List<RAMSDocument> ramsExpiring = ramsRepository.findExpiringDocuments(threshold);
        result.put("ramsDocuments", ramsExpiring.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("title", r.getTitle());
            m.put("validUntil", r.getValidUntil() != null ? r.getValidUntil().toString() : "");
            m.put("contract", r.getContract() != null ? r.getContract().getContractRef() : "");
            return m;
        }).collect(Collectors.toList()));

        List<CISVerification> cisExpiring = cisVerificationRepository.findExpiringVerifications(threshold);
        result.put("cisVerifications", cisExpiring.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("verificationRef", c.getVerificationRef());
            m.put("expiresAt", c.getExpiresAt() != null ? c.getExpiresAt().toString() : "");
            m.put("company", c.getCompany() != null ? c.getCompany().getName() : "");
            return m;
        }).collect(Collectors.toList()));

        List<Bond> bondsExpiring = bondRepository.findExpiringBonds(threshold);
        result.put("bonds", bondsExpiring.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("bondNumber", b.getBondNumber());
            m.put("expiryDate", b.getExpiryDate() != null ? b.getExpiryDate().toString() : "");
            m.put("value", b.getBondValue());
            return m;
        }).collect(Collectors.toList()));

        List<F10Notification> f10Expiring = f10Repository.findExpiringNotifications(threshold);
        result.put("f10Notifications", f10Expiring.stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("notificationNumber", f.getNotificationNumber());
            m.put("constructionEndDate", f.getConstructionEndDate() != null ? f.getConstructionEndDate().toString() : "");
            m.put("contract", f.getContract() != null ? f.getContract().getContractRef() : "");
            return m;
        }).collect(Collectors.toList()));

        return result;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getPipelineSummary() {
        Map<String, Object> funnel = new LinkedHashMap<>();
        long awarded = 0;
        long lost = 0;
        for (TenderStatus status : TenderStatus.values()) {
            long count = tenderRepository.countByStatus(status);
            BigDecimal totalValue = tenderRepository.sumTargetValueByStatus(status);
            Map<String, Object> statusData = new LinkedHashMap<>();
            statusData.put("count", count);
            statusData.put("totalValue", totalValue != null ? totalValue : BigDecimal.ZERO);
            funnel.put(status.name(), statusData);
            if (status == TenderStatus.AWARDED) awarded = count;
            if (status == TenderStatus.LOST) lost = count;
        }
        long totalDecided = awarded + lost;
        double winRate = totalDecided > 0 ? (double) awarded / totalDecided * 100 : 0.0;
        funnel.put("_summary", Map.of(
                "awarded", awarded,
                "lost", lost,
                "winRate", Math.round(winRate * 10) / 10.0
        ));
        return funnel;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getContractSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        for (ContractStatus status : ContractStatus.values()) {
            long count = contractRepository.countByStatus(status);
            BigDecimal totalValue = contractRepository.sumContractValueByStatus(status);
            Map<String, Object> statusData = new LinkedHashMap<>();
            statusData.put("count", count);
            statusData.put("value", totalValue != null ? totalValue : BigDecimal.ZERO);
            summary.put(status.name(), statusData);
        }
        return summary;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getCashflowForecast(int monthsAhead) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(monthsAhead);
        List<ApplicationForPayment> approved = applicationRepository.findCashflowRelevantByDateRange(start, end);

        Map<YearMonth, BigDecimal> byMonth = new TreeMap<>();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal retentionHeld = BigDecimal.ZERO;
        BigDecimal retentionReleasable = BigDecimal.ZERO;

        for (ApplicationForPayment app : approved) {
            LocalDate dueDate = app.getDueDate() != null ? app.getDueDate()
                    : (app.getPaidDate() != null ? app.getPaidDate() : null);
            if (dueDate == null) continue;

            BigDecimal amount = app.getGrossValue() != null ? app.getGrossValue() : BigDecimal.ZERO;
            BigDecimal ret = app.getRetention() != null ? app.getRetention() : BigDecimal.ZERO;

            byMonth.merge(YearMonth.from(dueDate), amount, BigDecimal::add);
            total = total.add(amount);

            if (app.getStatus() == ApplicationStatus.PAID) {
                retentionHeld = retentionHeld.add(ret);
            } else {
                LocalDate defectsEnd = app.getContract() != null ? app.getContract().getDefectsEndDate() : null;
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
        return forecast;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Map<String, Object>> getRetentionSchedule() {
        return contractRepository.findByRetentionLedgerIsNotNull().stream()
                .map(c -> {
                    BigDecimal retentionHeld = applicationRepository.sumRetentionByContractId(c.getId());
                    if (retentionHeld == null) retentionHeld = BigDecimal.ZERO;
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("contractRef", c.getContractRef());
                    item.put("title", c.getTitle());
                    item.put("client", c.getClient() != null ? c.getClient().getName() : "");
                    item.put("contractValue", c.getContractValue());
                    item.put("defectsEndDate", c.getDefectsEndDate() != null ? c.getDefectsEndDate().toString() : "");
                    item.put("retentionPercent", c.getRetentionPercent());
                    item.put("retentionHeld", retentionHeld.setScale(2, RoundingMode.HALF_UP));
                    item.put("status", c.getStatus().name());
                    return item;
                }).collect(Collectors.toList());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getHealthSafetyStats(int months) {
        LocalDateTime start = LocalDateTime.now().minusMonths(months);
        List<IncidentReport> incidents = incidentRepository.findByDateRange(start, LocalDateTime.now());

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

        long activeOperatives = operativeRepository.findByStatus(OperativeStatus.ACTIVE).size();
        long hoursWorked = activeOperatives * 220L * 8L;
        double afr = hoursWorked > 0 ? (double) total * 100000.0 / hoursWorked : 0.0;

        int actualIncidents = total - nearMisses;
        double nearMissRatio = actualIncidents > 0 ? (double) nearMisses / actualIncidents : 0.0;

        int ramsExpiring30 = ramsRepository.findExpiringDocuments(LocalDate.now().plusDays(30)).size();
        long f10Active = f10Repository.countActive();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("periodMonths", months);
        stats.put("afr", Math.round(afr * 100.0) / 100.0);
        stats.put("totalIncidents", total);
        stats.put("nearMisses", nearMisses);
        stats.put("nearMissRatio", Math.round(nearMissRatio * 10.0) / 10.0);
        stats.put("ridDORNotifiable", ridDOR);
        stats.put("openIncidents", openIncidents);
        stats.put("ramsExpiring30Days", ramsExpiring30);
        stats.put("f10Active", f10Active);
        return stats;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getPlantUtilisation(int days) {
        LocalDate today = LocalDate.now();

        long total = plantRepository.count();
        long allocated = plantRepository.countByStatus(PlantStatus.ON_HIRE);
        long idle = plantRepository.countByStatus(PlantStatus.AVAILABLE)
                  + plantRepository.countByStatus(PlantStatus.IDLE);
        double utilisationPct = total > 0 ? (double) allocated / total * 100 : 0;

        LocalDate thirtyDays = today.plusDays(30);
        int lolerDueCount = (int) lolerRepository.findDueExaminations(thirtyDays).stream()
                .filter(l -> l.getNextDueDate() != null && !l.getNextDueDate().isBefore(today))
                .count();
        int lolerOverdueCount = lolerRepository.findDueExaminations(today).size();

        Map<String, Object> utilisation = new LinkedHashMap<>();
        utilisation.put("periodDays", days);
        utilisation.put("totalPlant", (int) total);
        utilisation.put("allocated", (int) allocated);
        utilisation.put("idle", (int) idle);
        utilisation.put("utilisationPercent", Math.round(utilisationPct * 10.0) / 10.0);
        utilisation.put("lolErDue30Days", lolerDueCount);
        utilisation.put("lolErOverdue", lolerOverdueCount);
        return utilisation;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getCisSummary() {
        int year = LocalDate.now().getYear();
        String taxYear = year + "/" + (year + 1);
        String yearStr = String.valueOf(year);

        BigDecimal totalGross = cisReturnRepository.sumGrossValueByYear(yearStr);
        if (totalGross == null) totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = cisReturnRepository.sumDeductionsByYear(yearStr);
        if (totalDeductions == null) totalDeductions = BigDecimal.ZERO;
        long submitted = cisReturnRepository.countByYear(yearStr);
        long pending = cisReturnRepository.countByStatus(com.crms.domain.subcontractor.enums.CisReturnStatus.DRAFT);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("taxYear", taxYear);
        summary.put("totalGrossPaid", totalGross.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalDeductions", totalDeductions.setScale(2, RoundingMode.HALF_UP));
        summary.put("returnsSubmitted", submitted);
        summary.put("returnsPending", pending);
        summary.put("returnsOverdue", 0);
        summary.put("netPaidToSubcontractors", totalGross.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP));
        return summary;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getAdoptionStatus() {
        BigDecimal totalBondValue = bondRepository.sumBondValue();
        if (totalBondValue == null) totalBondValue = BigDecimal.ZERO;
        BigDecimal totalCommutedSums = commutedSumRepository.sumTotalAmount();
        if (totalCommutedSums == null) totalCommutedSums = BigDecimal.ZERO;
        int bondsExpiring30 = bondRepository.findExpiringBonds(LocalDate.now().plusDays(30)).size();

        Map<String, Object> status = new LinkedHashMap<>();
        for (AdoptionStatus as : AdoptionStatus.values()) {
            status.put(as.name(), (int) adoptionCaseRepository.countByStatus(as));
        }
        status.put("totalBondsValue", totalBondValue.setScale(2, RoundingMode.HALF_UP));
        status.put("totalCommutedSums", totalCommutedSums.setScale(2, RoundingMode.HALF_UP));
        status.put("bondsExpiring30Days", bondsExpiring30);
        return status;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<String, Object> getProcurementSummary() {
        LocalDate today = LocalDate.now();

        int draftPO = purchaseOrderRepository.findByStatus(com.crms.domain.material.enums.PurchaseOrderStatus.DRAFT).size();
        int issuedPO = purchaseOrderRepository.findByStatus(com.crms.domain.material.enums.PurchaseOrderStatus.ISSUED).size();
        int deliveriesToday = deliveryNoteRepository.findExpectedDeliveries(today).size();
        int concreteCount = (int) concreteTicketRepository.countByDeliveryDate(today);
        int muckawayCount = (int) muckawayTicketRepository.countByDeliveryDate(today);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("purchaseOrdersDraft", draftPO);
        summary.put("purchaseOrdersIssued", issuedPO);
        summary.put("deliveriesExpectedToday", deliveriesToday);
        summary.put("concreteTicketsToday", concreteCount);
        summary.put("muckawayTicketsToday", muckawayCount);
        return summary;
    }

    // =========================================================================
    // Private helpers — typed DashboardStats DTO
    // =========================================================================

    private List<DashboardStats.CVRItem> calculateCVRSummary() {
        List<Contract> contracts = contractRepository.findByStatus(ContractStatus.ACTIVE);
        return contracts.stream()
                .map(contract -> {
                    BigDecimal valueToDate = applicationRepository.sumGrossValueByContractId(contract.getId());
                    if (valueToDate == null) valueToDate = BigDecimal.ZERO;
                    BigDecimal costToDate = calculateContractCostToDate(contract);
                    BigDecimal grossMargin = valueToDate.subtract(costToDate);
                    BigDecimal marginPercent = BigDecimal.ZERO;
                    if (valueToDate.compareTo(BigDecimal.ZERO) > 0) {
                        marginPercent = grossMargin.multiply(new BigDecimal("100"))
                                .divide(valueToDate, 2, RoundingMode.HALF_UP);
                    }
                    return DashboardStats.CVRItem.builder()
                            .contractId(contract.getId())
                            .contractName(contract.getTitle())
                            .contractRef(contract.getContractRef())
                            .valueToDate(valueToDate)
                            .costToDate(costToDate)
                            .grossMargin(grossMargin)
                            .marginPercent(marginPercent)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateContractCostToDate(com.crms.domain.contract.entity.Contract contract) {
        if (contract.getSite() == null) return BigDecimal.ZERO;
        Long siteId = contract.getSite().getId();
        LocalDate start = contract.getStartDate() != null ? contract.getStartDate() : LocalDate.of(2000, 1, 1);
        LocalDate today = LocalDate.now();
        BigDecimal labour = BigDecimal.ZERO;
        BigDecimal materials = BigDecimal.ZERO;
        BigDecimal plant = BigDecimal.ZERO;
        try {
            BigDecimal w = timesheetRepository.calculateTotalWagesBySiteAndPeriod(siteId, start, today);
            if (w != null) labour = w;
        } catch (Exception ignored) {}
        try {
            BigDecimal m = purchaseOrderRepository.sumReceivedNetValueBySiteAndDateRange(siteId, start, today);
            if (m != null) materials = m;
        } catch (Exception ignored) {}
        try {
            for (com.crms.domain.plant.entity.PlantAllocation alloc :
                    plantAllocationRepository.findBySiteAndDateRangeWithPlant(siteId, start, today)) {
                if (alloc.getPlant() == null || alloc.getPlant().getDailyHireRate() == null) continue;
                LocalDate allocStart = alloc.getStartDate() != null && alloc.getStartDate().isAfter(start)
                        ? alloc.getStartDate() : start;
                LocalDate allocEnd = alloc.getEndDate() == null || alloc.getEndDate().isAfter(today)
                        ? today : alloc.getEndDate();
                if (!allocStart.isAfter(allocEnd)) {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(allocStart, allocEnd) + 1;
                    plant = plant.add(alloc.getPlant().getDailyHireRate().multiply(BigDecimal.valueOf(days)));
                }
            }
        } catch (Exception ignored) {}
        return labour.add(materials).add(plant);
    }

    private List<DashboardStats.CashflowItem> calculateCashflowForecast() {
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusMonths(12);

        // Group submitted/approved AFP values by due month
        Map<YearMonth, BigDecimal[]> byMonth = new TreeMap<>();
        applicationRepository.findAll().stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SUBMITTED
                        || a.getStatus() == ApplicationStatus.APPROVED
                        || a.getStatus() == ApplicationStatus.PAID)
                .forEach(a -> {
                    LocalDate due = a.getDueDate();
                    if (due == null || due.isBefore(now) || due.isAfter(end)) return;
                    YearMonth ym = YearMonth.from(due);
                    byMonth.computeIfAbsent(ym, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    BigDecimal val = a.getGrossValue() != null ? a.getGrossValue() : BigDecimal.ZERO;
                    byMonth.get(ym)[0] = byMonth.get(ym)[0].add(val); // forecast
                    if (a.getStatus() == ApplicationStatus.PAID) {
                        byMonth.get(ym)[1] = byMonth.get(ym)[1].add(val); // confirmed
                    }
                });

        List<DashboardStats.CashflowItem> forecast = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            YearMonth ym = YearMonth.from(now.plusMonths(i));
            String monthLabel = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.UK) + " " + ym.getYear();
            BigDecimal[] vals = byMonth.getOrDefault(ym, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            forecast.add(DashboardStats.CashflowItem.builder()
                    .month(monthLabel)
                    .forecast(vals[0])
                    .confirmed(vals[1])
                    .build());
        }
        return forecast;
    }

    private DashboardStats.HAndSStats calculateHSStats() {
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);
        List<IncidentReport> incidents = incidentRepository.findByDateRange(twelveMonthsAgo, LocalDateTime.now());

        long nearMisses = incidents.stream()
                .filter(i -> i.getType() == IncidentType.NEAR_MISS)
                .count();
        long minorInjuries = incidents.stream()
                .filter(i -> i.getType() == IncidentType.MINOR_INJURY)
                .count();
        long majorInjuries = incidents.stream()
                .filter(i -> i.getType() == IncidentType.MAJOR_INJURY || i.getType() == IncidentType.FATALITY)
                .count();

        long activeOperatives = operativeRepository.findByStatus(OperativeStatus.ACTIVE).size();
        long hoursWorked = activeOperatives * 220L * 8L;
        BigDecimal afr = hoursWorked > 0
                ? new BigDecimal((minorInjuries + majorInjuries) * 100000.0 / hoursWorked)
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return DashboardStats.HAndSStats.builder()
                .nearMisses(nearMisses)
                .minorInjuries(minorInjuries)
                .majorInjuries(majorInjuries)
                .observations(0L)
                .afr(afr)
                .build();
    }

    private List<DashboardStats.LOLERItem> calculateLOLERCalendar() {
        LocalDate now = LocalDate.now();
        LocalDate sixtyDaysFromNow = now.plusDays(60);

        List<DashboardStats.LOLERItem> calendar = new ArrayList<>();
        for (PlantItem plant : plantRepository.findAllWithLolerExaminations()) {
            if (plant.getLolerExaminations() == null || plant.getLolerExaminations().isEmpty()) continue;
            plant.getLolerExaminations().stream()
                    .filter(e -> e.getNextInspectionDate() != null)
                    .filter(e -> e.getNextInspectionDate().isAfter(now)
                            && e.getNextInspectionDate().isBefore(sixtyDaysFromNow))
                    .findFirst()
                    .ifPresent(exam -> calendar.add(DashboardStats.LOLERItem.builder()
                            .plantId(plant.getId())
                            .plantRef(plant.getPlantRef())
                            .description(plant.getDescription())
                            .lastExamDate(exam.getExaminationDate())
                            .nextExamDate(exam.getNextInspectionDate())
                            .overdue(exam.getNextInspectionDate().isBefore(now))
                            .build()));
        }
        return calendar;
    }

    private List<DashboardStats.TenderPipelineItem> calculateTenderPipeline() {
        return tenderRepository.findByStatusIn(
                List.of(TenderStatus.LEAD, TenderStatus.PROSPECT, TenderStatus.BID))
                .stream()
                .map(tender -> DashboardStats.TenderPipelineItem.builder()
                        .tenderId(tender.getId())
                        .tenderRef(tender.getTenderRef())
                        .title(tender.getTitle())
                        .value(tender.getValueRange())
                        .probability(tender.getWinProbability())
                        .status(tender.getStatus() != null ? tender.getStatus().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Private KPI helpers
    // =========================================================================

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
        List<Contract> contracts = contractRepository.findAllWithVariations();
        long total = contracts.size();
        long active = contracts.stream().filter(c -> c.getStatus() == ContractStatus.ACTIVE).count();

        BigDecimal totalValue = contracts.stream()
                .map(c -> c.getContractValue() != null ? c.getContractValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retentionHeld = applicationRepository.findAll().stream()
                .map(a -> a.getRetention() != null ? a.getRetention() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
        List<PlantItem> allPlant = plantRepository.findAll();
        long total = allPlant.size();
        long available = allPlant.stream().filter(p -> p.getStatus() == PlantStatus.AVAILABLE).count();
        long onHire = allPlant.stream().filter(p -> p.getStatus() == PlantStatus.ON_HIRE).count();
        long offHire = allPlant.stream().filter(p -> p.getStatus() == PlantStatus.OFF_HIRE).count();

        LocalDate now = LocalDate.now();
        LocalDate thirtyDays = now.plusDays(30);
        long lolerDue30 = lolerRepository.findDueExaminations(thirtyDays).stream()
                .filter(l -> l.getNextDueDate() != null && !l.getNextDueDate().isBefore(now))
                .count();
        long lolerOverdue = lolerRepository.findDueExaminations(now).size();

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
        List<Operative> expiringRTW = operativeRepository.findWithExpiringRightToWork(thirtyDays);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("total", total);
        kpis.put("active", active);
        kpis.put("cardsExpiring30Days", cardRepository.findExpiringCards(thirtyDays, now).size());
        kpis.put("qualificationsExpiring30Days", qualificationRepository.findExpiringQualifications(thirtyDays, now).size());
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
        int f10Active = (int) f10Repository.findAll().stream()
                .filter(F10Notification::getIsActive)
                .count();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("afr", Math.round(afr * 100.0) / 100.0);
        kpis.put("openIncidents", open);
        kpis.put("ramsExpiring30Days", ramsExp30);
        kpis.put("f10Active", f10Active);
        return kpis;
    }

    private Map<String, Object> getAdoptionKpis() {
        long totalCases = adoptionCaseRepository.count();
        BigDecimal bondValue = bondRepository.sumBondValue();
        if (bondValue == null) bondValue = BigDecimal.ZERO;
        BigDecimal commutedSums = commutedSumRepository.sumTotalAmount();
        if (commutedSums == null) commutedSums = BigDecimal.ZERO;
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

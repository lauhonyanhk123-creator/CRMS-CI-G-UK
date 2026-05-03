package com.crms.service.impl;

import com.crms.dto.response.DashboardStats;
import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.healthsafety.entity.IncidentReport;
import com.crms.domain.healthsafety.enums.IncidentType;
import com.crms.domain.healthsafety.repository.IncidentReportRepository;
import com.crms.domain.plant.entity.LOLERExamination;
import com.crms.domain.plant.entity.PlantItem;
import com.crms.domain.plant.enums.PlantStatus;
import com.crms.domain.plant.repository.LOLERExaminationRepository;
import com.crms.domain.plant.repository.PlantItemRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.enums.SiteStatus;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.enums.TenderStatus;
import com.crms.domain.tender.repository.TenderRepository;
import com.crms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
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
    
    @Override
    public DashboardStats getStats() {
        log.info("Generating dashboard statistics");
        
        // Contract stats
        long totalContracts = contractRepository.countByStatus(ContractStatus.ACTIVE);
        long activeSites = siteRepository.countByStatus(SiteStatus.ACTIVE);
        long plantOnSite = plantRepository.countByStatus(PlantStatus.ON_HIRE);
        long pendingApplications = applicationRepository.countByStatus(ApplicationStatus.SUBMITTED);
        
        // CVR Summary - calculate from contracts with applications
        List<DashboardStats.CVRItem> cvrSummary = calculateCVRSummary();
        
        // Cashflow forecast - next 12 months
        List<DashboardStats.CashflowItem> cashflowForecast = calculateCashflowForecast();
        
        // H&S Stats - last 12 months
        DashboardStats.HAndSStats hsStats = calculateHSStats();
        
        // LOLER calendar - upcoming examinations
        List<DashboardStats.LOLERItem> lolerCalendar = calculateLOLERCalendar();
        
        // Tender pipeline
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
    
    private List<DashboardStats.CVRItem> calculateCVRSummary() {
        List<Contract> contracts = contractRepository.findByStatus(ContractStatus.ACTIVE);
        
        return contracts.stream()
                .map(contract -> {
                    BigDecimal valueToDate = applicationRepository.sumGrossValueByContractId(contract.getId());
                    if (valueToDate == null) valueToDate = BigDecimal.ZERO;
                    
                    // Cost would come from cost tracking system - stubbed
                    BigDecimal costToDate = BigDecimal.ZERO;
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
    
    private List<DashboardStats.CashflowItem> calculateCashflowForecast() {
        List<DashboardStats.CashflowItem> forecast = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 0; i < 12; i++) {
            YearMonth ym = YearMonth.from(now.plusMonths(i));
            String monthLabel = ym.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.UK) + " " + ym.getYear();
            
            // Stub - would calculate from applications and projections
            BigDecimal forecastAmount = new BigDecimal((i + 1) * 50000);
            BigDecimal confirmedAmount = i < 2 ? forecastAmount.multiply(new BigDecimal("0.7")) : BigDecimal.ZERO;
            
            forecast.add(DashboardStats.CashflowItem.builder()
                    .month(monthLabel)
                    .forecast(forecastAmount)
                    .confirmed(confirmedAmount)
                    .build());
        }
        
        return forecast;
    }
    
    private DashboardStats.HAndSStats calculateHSStats() {
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);
        List<IncidentReport> incidents = incidentRepository.findByDateRange(twelveMonthsAgo, LocalDateTime.now());
        
        long nearMisses = incidents.stream()
                .filter(incident -> incident.getType() == IncidentType.NEAR_MISS)
                .count();
        long minorInjuries = incidents.stream()
                .filter(incident -> incident.getType() == IncidentType.MINOR_INJURY)
                .count();
        long majorInjuries = incidents.stream()
                .filter(incident -> incident.getType() == IncidentType.MAJOR_INJURY
                        || incident.getType() == IncidentType.FATALITY)
                .count();
        long observations = 0L; // Would come from observations system
        
        // AFR = (Major injuries + minor injuries) / total hours worked * 100000
        // Stub calculation
        BigDecimal afr = new BigDecimal("0.34"); // Stubbed average AFR
        
        return DashboardStats.HAndSStats.builder()
                .nearMisses(nearMisses)
                .minorInjuries(minorInjuries)
                .majorInjuries(majorInjuries)
                .observations(observations)
                .afr(afr)
                .build();
    }
    
    private List<DashboardStats.LOLERItem> calculateLOLERCalendar() {
        LocalDate now = LocalDate.now();
        LocalDate sixtyDaysFromNow = now.plusDays(60);
        
        List<PlantItem> plantWithLoler = plantRepository.findAll().stream()
                .filter(p -> p.getLolerExaminations() != null && !p.getLolerExaminations().isEmpty())
                .collect(Collectors.toList());
        
        List<DashboardStats.LOLERItem> calendar = new ArrayList<>();
        
        for (PlantItem plant : plantWithLoler) {
            LOLERExamination lastExam = plant.getLolerExaminations().stream()
                    .filter(e -> e.getNextInspectionDate() != null)
                    .filter(e -> e.getNextInspectionDate().isAfter(now) && 
                                e.getNextInspectionDate().isBefore(sixtyDaysFromNow))
                    .findFirst()
                    .orElse(null);
            
            if (lastExam != null) {
                calendar.add(DashboardStats.LOLERItem.builder()
                        .plantId(plant.getId())
                        .plantRef(plant.getPlantRef())
                        .description(plant.getDescription())
                        .lastExamDate(lastExam.getExaminationDate())
                        .nextExamDate(lastExam.getNextInspectionDate())
                        .overdue(lastExam.getNextInspectionDate().isBefore(now))
                        .build());
            }
        }
        
        return calendar;
    }
    
    private List<DashboardStats.TenderPipelineItem> calculateTenderPipeline() {
        List<Tender> activeTenders = tenderRepository.findByStatusIn(
                List.of(TenderStatus.LEAD, TenderStatus.PROSPECT, TenderStatus.BID));
        
        return activeTenders.stream()
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
}
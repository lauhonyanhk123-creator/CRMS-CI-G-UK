package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    
    private Long totalContracts;
    
    private Long activeSites;
    
    private Long plantOnSite;
    
    private Long pendingApplications;
    
    private List<CVRItem> cvrSummary;
    
    private List<CashflowItem> cashflowForecast;
    
    private HAndSStats hsStats;
    
    private List<LOLERItem> lolerCalendar;
    
    private List<TenderPipelineItem> tenderPipeline;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CVRItem {
        private Long contractId;
        private String contractName;
        private String contractRef;
        private BigDecimal valueToDate;
        private BigDecimal costToDate;
        private BigDecimal grossMargin;
        private BigDecimal marginPercent;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashflowItem {
        private String month;
        private BigDecimal forecast;
        private BigDecimal confirmed;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HAndSStats {
        private Long nearMisses;
        private Long minorInjuries;
        private Long majorInjuries;
        private Long observations;
        private BigDecimal afr;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LOLERItem {
        private Long plantId;
        private String plantRef;
        private String description;
        private LocalDate lastExamDate;
        private LocalDate nextExamDate;
        private Boolean overdue;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenderPipelineItem {
        private Long tenderId;
        private String tenderRef;
        private String title;
        private BigDecimal value;
        private Integer probability;
        private String status;
    }
}
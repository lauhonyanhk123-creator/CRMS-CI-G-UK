package com.crms.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WipReportResponse {
    private Long id;
    private Long contractId;
    private String contractRef;
    private LocalDate reportDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal certifiedValue;
    private BigDecimal costIncurred;
    private BigDecimal wipValue;
    private BigDecimal underRecovery;
    private BigDecimal overRecovery;
    private String status;
    private String journalReference;
    private String notes;
    private LocalDateTime createdAt;
}

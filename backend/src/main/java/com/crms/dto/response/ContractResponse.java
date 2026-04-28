package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {
    
    private Long id;
    private String contractRef;
    private String title;
    private String description;
    private Long clientId;
    private String clientName;
    private Long siteId;
    private String siteName;
    private String status;
    private String contractType;
    private String jctType;
    private String formOfContract;
    private BigDecimal contractValue;
    private BigDecimal fluctuations;
    private BigDecimal retentionPercentage;
    private Integer retentionLimit;
    private Integer paymentTermsDays;
    private Integer finalDateForPaymentOffsetDays;
    private Integer payLessNoticePrescribedPeriodDays;
    private String insuranceCompany;
    private String policyNumber;
    private String programmeRef;
    private String architectRef;
    private String projectManagerRef;
    private String contractAdministratorRef;
    private String subcontractorRef;
    private String commencementDate;
    private String defectsLiabilityPeriod;
    private String practicalCompletionDate;
}
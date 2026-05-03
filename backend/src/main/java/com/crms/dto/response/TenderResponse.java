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
public class TenderResponse {
    
    private Long id;
    private Long siteId;
    private String siteName;
    private String tenderRef;
    private String clientName;
    private String title;
    private String description;
    private String client;
    private Long clientId;
    private String clientContact;
    private BigDecimal valueRange;
    private String status;
    private String contractForm;
    private String measurementStandard;
    private String probability;
    private Integer winProbability;
    private String tenderOwner;
    private java.time.LocalDate tenderIssuedDate;
    private java.time.LocalDate tenderReturnDate;
    private BigDecimal tenderValueSubmitted;
    private String lossReason;
    private Long contractId;
    private String closingDate;
    private String returnedDate;
    private String decisionDate;
    private String tenderValue;
    private String markupRate;
    private String overheadRate;
    private String preliminaries;
    private String programme;
    private String riskRegister;
    private String notes;
}
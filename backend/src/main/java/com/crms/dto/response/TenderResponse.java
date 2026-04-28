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
    private String tenderRef;
    private String title;
    private String description;
    private String client;
    private Long clientId;
    private String clientContact;
    private BigDecimal valueRange;
    private String status;
    private String probability;
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
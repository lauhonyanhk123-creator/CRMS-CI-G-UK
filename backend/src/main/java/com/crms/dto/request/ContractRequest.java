package com.crms.dto.request;

import com.crms.domain.tender.enums.ContractForm;
import com.crms.domain.tender.enums.MeasurementStandard;
import com.crms.domain.contract.enums.ContractStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractRequest {
    
    @NotBlank(message = "Contract reference is required")
    private String contractRef;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    @NotNull(message = "Site ID is required")
    private Long siteId;
    
    private Long tenderId;
    
    private ContractForm contractForm;
    
    private MeasurementStandard measurementStandard;
    
    private BigDecimal contractValue;
    
    private BigDecimal retentionPercent;
    
    private BigDecimal retentionReductionPercent;
    
    private Integer practicalCompletionDefectsPeriodMonths;
    
    private Integer paymentTermsDays;
    
    private Integer finalDateForPaymentOffsetDays;
    
    private Integer payLessNoticePrescribedPeriodDays;
    
    private BigDecimal bondPercent;
    
    private BigDecimal bondValue;
    
    private String bondRef;
    
    private String contractDocuments;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private LocalDate defectsEndDate;
    
    private String nec4Options;
    
    private String nec4PricingMechanism;
    
    private ContractStatus status;
}
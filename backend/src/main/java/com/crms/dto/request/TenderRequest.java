package com.crms.dto.request;

import com.crms.domain.tender.enums.ContractForm;
import com.crms.domain.tender.enums.MeasurementStandard;
import com.crms.domain.tender.enums.TenderStatus;
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
public class TenderRequest {
    
    @NotBlank(message = "Tender reference is required")
    private String tenderRef;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    private Long siteId;
    
    private TenderStatus status;
    
    private ContractForm contractForm;
    
    private MeasurementStandard measurementStandard;
    
    private BigDecimal valueRange;
    
    private Integer winProbability;
    
    private String tenderOwner;
    
    private LocalDate tenderIssuedDate;
    
    private LocalDate tenderReturnDate;
    
    private BigDecimal tenderValueSubmitted;
    
    private String notes;
}
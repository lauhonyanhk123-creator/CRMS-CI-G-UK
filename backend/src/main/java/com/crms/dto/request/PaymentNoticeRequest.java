package com.crms.dto.request;

import com.crms.domain.contract.enums.NoticeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNoticeRequest {
    
    @NotNull(message = "Notice type is required")
    private NoticeType noticeType;
    
    @NotNull(message = "Issue date is required")
    private LocalDateTime issuedOn;
    
    @NotNull(message = "Sum considered due is required")
    private BigDecimal sumConsideredDue;
    
    @Builder.Default
    private String currency = "GBP";
    
    private String basisOfCalculation;
}
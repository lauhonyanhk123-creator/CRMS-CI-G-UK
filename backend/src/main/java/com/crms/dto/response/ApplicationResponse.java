package com.crms.dto.response;

import com.crms.domain.contract.enums.DeadlineStatus;
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
public class ApplicationResponse {
    
    private Long id;
    private String applicationRef;
    private Integer applicationNumber;
    private Long contractId;
    private String contractRef;
    private LocalDate applicationPeriodStart;
    private LocalDate applicationPeriodEnd;
    private LocalDate dueDate;
    private BigDecimal valueOfWorks;
    private BigDecimal retention;
    private BigDecimal grossValue;
    private String status;
    private LocalDate submittedDate;
    private LocalDate paidDate;
    private String payerRef;
    private Boolean reverseCharge;
    
    // Pay-less notice s.111 deadline fields
    private LocalDate payLessNoticeDeadline;
    private DeadlineStatus deadlineStatus;
}
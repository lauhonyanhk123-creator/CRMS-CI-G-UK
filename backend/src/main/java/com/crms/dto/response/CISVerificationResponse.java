package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CISVerificationResponse {
    
    private Long id;
    private Long companyId;
    private String companyName;
    private String cisStatus;
    private String verificationStatus;
    private Boolean verified;
    private String verificationDate;
    private String verifiedAt;
    private String verificationRef;
    private String reference;
    private String message;
    private java.math.BigDecimal hmrcDeductionRate;
}
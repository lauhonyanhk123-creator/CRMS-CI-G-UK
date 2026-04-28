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
    private Boolean verified;
    private String verificationDate;
    private String verificationRef;
    private String message;
}
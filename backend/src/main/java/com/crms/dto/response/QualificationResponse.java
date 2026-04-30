package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualificationResponse {

    private Long id;
    private Long operativeId;
    private String qualificationType;
    private String level;
    private String awardingBody;
    private String certificateNumber;
    private String achievedDate;
    private String expiryDate;
    private String notes;
    private Boolean isValid;
    private Boolean isExpiringSoon;
}
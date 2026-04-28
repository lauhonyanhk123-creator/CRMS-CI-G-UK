package com.crms.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * HMRC CIS Submit Monthly Return Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HmrcCisSubmitResponse {
    
    private String receiptRef;
    private String acknowledgementRef;
    private LocalDateTime submittedAt;
    private SubmissionStatus status;
    private String submissionMessages;
    
    public enum SubmissionStatus {
        ACCEPTED,
        REJECTED,
        PENDING,
        FAILED
    }
}

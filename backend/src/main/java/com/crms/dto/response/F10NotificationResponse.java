package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class F10NotificationResponse {

    private Long id;
    private Long contractId;
    private String contractRef;
    private String notificationNumber;
    private LocalDate submittedDate;
    private String confirmationNumber;
    private Boolean moreThan30Days;
    private Boolean moreThan500PersonDays;
    private LocalDate constructionStartDate;
    private LocalDate constructionEndDate;
    private Boolean isActive;

    // HDF tracking fields
    private String hdfReference;
    private LocalDate hdfSubmittedDate;
    private Boolean hdfAcknowledged;
    private String hdfAcknowledgedBy;
    private LocalDateTime hdfAcknowledgedDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

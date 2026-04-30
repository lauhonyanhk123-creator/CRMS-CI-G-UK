package com.crms.dto.response;

import com.crms.domain.healthsafety.entity.F10Notification;
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

    public static F10NotificationResponse fromEntity(F10Notification entity) {
        return F10NotificationResponse.builder()
                .id(entity.getId())
                .contractId(entity.getContract() != null ? entity.getContract().getId() : null)
                .contractRef(entity.getContract() != null ? entity.getContract().getContractRef() : null)
                .notificationNumber(entity.getNotificationNumber())
                .submittedDate(entity.getSubmittedDate())
                .confirmationNumber(entity.getConfirmationNumber())
                .moreThan30Days(entity.getMoreThan30Days())
                .moreThan500PersonDays(entity.getMoreThan500PersonDays())
                .constructionStartDate(entity.getConstructionStartDate())
                .constructionEndDate(entity.getConstructionEndDate())
                .isActive(entity.getIsActive())
                .hdfReference(entity.getHdfReference())
                .hdfSubmittedDate(entity.getHdfSubmittedDate())
                .hdfAcknowledged(entity.getHdfAcknowledged())
                .hdfAcknowledgedBy(entity.getHdfAcknowledgedBy())
                .hdfAcknowledgedDate(entity.getHdfAcknowledgedDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

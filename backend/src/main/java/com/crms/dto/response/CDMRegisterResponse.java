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
public class CDMRegisterResponse {

    private Long id;
    private String notificationNumber;
    private String projectName;
    private String projectAddress;
    private String projectDescription;
    private Long clientId;
    private String clientName;
    private Long siteId;
    private String siteName;
    private String principalDesignerName;
    private String principalDesignerEmail;
    private String principalDesignerPhone;
    private String principalContractorName;
    private String principalContractorEmail;
    private String principalContractorPhone;
    private LocalDate notificationDate;
    private LocalDate constructionStartDate;
    private LocalDate constructionEndDate;
    private Boolean isNotifiable;
    private Boolean moreThan30Days;
    private Boolean moreThan500PersonDays;
    private String hseNotificationRef;
    private LocalDate datePreconstructionInfoShared;
    private LocalDate constructionPhasePlanDate;
    private String healthSafetyFileRef;
    private LocalDateTime healthSafetyFileCreatedDate;
    private LocalDateTime healthSafetyFileCompletedDate;
    private Boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

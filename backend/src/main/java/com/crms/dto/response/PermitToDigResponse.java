package com.crms.dto.response;

import com.crms.domain.healthsafety.enums.PermitStatus;
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
public class PermitToDigResponse {

    private Long id;
    private Long siteId;
    private String siteName;
    private String permitNumber;
    private String worksDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private PermitStatus status;
    private String lsbudReference;
    private Integer trialHoleCount;
    private String trialHolePhotoRef;
    private String catScanRef;
    private LocalDate catScanDate;
    private String catScanDeviceSerial;
    private LocalDate catScanLastCalibrationDate;
    private String supervisorApprovalRef;
    private LocalDate supervisorApprovalDate;
    private String documentRef;

    // Workflow fields
    private String requestedBy;
    private LocalDate requestedDate;
    private String precheckedBy;
    private LocalDate precheckedDate;
    private String issuedBy;
    private LocalDate issuedDate;
    private String completedBy;
    private LocalDate completedDate;
    private String cancellationReason;
    private String cancelledBy;
    private LocalDate cancelledDate;
    private Integer extensionCount;
    private LocalDate lastExtensionDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

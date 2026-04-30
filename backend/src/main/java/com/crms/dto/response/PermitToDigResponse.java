package com.crms.dto.response;

import com.crms.domain.healthsafety.entity.PermitToDig;
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

    public static PermitToDigResponse fromEntity(PermitToDig entity) {
        return PermitToDigResponse.builder()
                .id(entity.getId())
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .permitNumber(entity.getPermitNumber())
                .worksDescription(entity.getWorksDescription())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .lsbudReference(entity.getLsbudReference())
                .trialHoleCount(entity.getTrialHoleCount())
                .trialHolePhotoRef(entity.getTrialHolePhotoRef())
                .catScanRef(entity.getCatScanRef())
                .catScanDate(entity.getCatScanDate())
                .catScanDeviceSerial(entity.getCatScanDeviceSerial())
                .catScanLastCalibrationDate(entity.getCatScanLastCalibrationDate())
                .supervisorApprovalRef(entity.getSupervisorApprovalRef())
                .supervisorApprovalDate(entity.getSupervisorApprovalDate())
                .documentRef(entity.getDocumentRef())
                .requestedBy(entity.getRequestedBy())
                .requestedDate(entity.getRequestedDate())
                .precheckedBy(entity.getPrecheckedBy())
                .precheckedDate(entity.getPrecheckedDate())
                .issuedBy(entity.getIssuedBy())
                .issuedDate(entity.getIssuedDate())
                .completedBy(entity.getCompletedBy())
                .completedDate(entity.getCompletedDate())
                .cancellationReason(entity.getCancellationReason())
                .cancelledBy(entity.getCancelledBy())
                .cancelledDate(entity.getCancelledDate())
                .extensionCount(entity.getExtensionCount())
                .lastExtensionDate(entity.getLastExtensionDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

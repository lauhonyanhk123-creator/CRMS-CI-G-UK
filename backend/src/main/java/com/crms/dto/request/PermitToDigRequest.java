package com.crms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermitToDigRequest {

    @NotNull(message = "Site ID is required")
    private Long siteId;

    private String worksDescription;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

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
}

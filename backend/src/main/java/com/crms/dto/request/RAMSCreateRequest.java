package com.crms.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class RAMSCreateRequest {

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    @NotBlank(message = "Document title is required")
    private String title;

    private String documentRef;
    private String version;
    private String revision;

    @NotBlank(message = "Scope of work is required")
    private String scopeOfWork;

    private String riskAssessments;
    private String methodStatements;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @NotBlank(message = "Prepared by is required")
    private String preparedBy;

    private String reviewedBy;
    private String approvedBy;

    private Boolean isActive = true;
}

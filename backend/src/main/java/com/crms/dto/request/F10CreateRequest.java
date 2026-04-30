package com.crms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class F10CreateRequest {

    @NotBlank(message = "Project name is required")
    private String projectName;

    @NotBlank(message = "Description of work is required")
    private String descriptionOfWork;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Expected completion date is required")
    private LocalDate expectedCompletionDate;

    @NotBlank(message = "Principal contractor is required")
    private String principalContractor;

    @NotBlank(message = "Principal designer is required")
    private String principalDesigner;

    private String clientName;
    private String clientAddress;
    private String clientPhone;
    private String clientEmail;

    private String siteAddress;
    private String siteManagerName;
    private String siteManagerPhone;

    @NotNull(message = "Number of contractors is required")
    @Positive
    private Integer numberOfContractors;

    @NotNull(message = "Number of workers on site is required")
    @Positive
    private Integer maxWorkersOnSite;

    private String healthSafetyFileRef;
    private Boolean moreThan30Days = false;
    private Boolean moreThan500PersonDays = false;
    private Boolean isActive = true;
}

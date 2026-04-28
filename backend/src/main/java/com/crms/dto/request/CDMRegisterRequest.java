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
public class CDMRegisterRequest {

    @NotBlank(message = "Project name is required")
    private String projectName;

    private String projectAddress;

    private String projectDescription;

    @NotNull(message = "Client ID is required")
    private Long clientId;

    private Long siteId;

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
}

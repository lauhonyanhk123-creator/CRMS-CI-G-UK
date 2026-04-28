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
public class F10NotificationRequest {

    @NotNull(message = "Contract ID is required")
    private Long contractId;

    private LocalDate submittedDate;

    private String confirmationNumber;

    private Boolean moreThan30Days;

    private Boolean moreThan500PersonDays;

    private LocalDate constructionStartDate;

    private LocalDate constructionEndDate;

    private String hdfReference;

    private LocalDate hdfSubmittedDate;

    private Boolean hdfAcknowledged;

    private String hdfAcknowledgedBy;

    private LocalDate hdfAcknowledgedDate;
}

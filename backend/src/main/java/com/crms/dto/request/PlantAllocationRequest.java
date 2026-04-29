package com.crms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantAllocationRequest {

    @NotNull(message = "Operative ID is required")
    private Long operativeId;

    @NotNull(message = "Site ID is required")
    private Long siteId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private String notes;
}

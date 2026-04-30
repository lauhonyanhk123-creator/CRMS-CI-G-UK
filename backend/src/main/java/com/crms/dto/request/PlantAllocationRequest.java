package com.crms.dto.request;

import com.crms.domain.plant.enums.AllocationStatus;
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
public class PlantAllocationRequest {

    @NotNull(message = "Operative ID is required")
    private Long operativeId;

    @NotNull(message = "Site ID is required")
    private Long siteId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private AllocationStatus status;
}
package com.crms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WipReportRequest {
    @NotNull(message = "Report date is required")
    private LocalDate reportDate;
    
    private String notes;
}

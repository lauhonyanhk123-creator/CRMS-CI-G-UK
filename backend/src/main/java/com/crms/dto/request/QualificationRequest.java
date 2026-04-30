package com.crms.dto.request;

import com.crms.domain.operative.enums.QualificationType;
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
public class QualificationRequest {

    @NotNull(message = "Qualification type is required")
    private QualificationType qualificationType;

    private String level;

    private String awardingBody;

    private String certificateNumber;

    private LocalDate achievedDate;

    private LocalDate expiryDate;

    private String notes;
}
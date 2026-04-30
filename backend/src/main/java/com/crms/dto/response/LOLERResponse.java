package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LOLERResponse {

    private Long id;
    private Long plantId;
    private String examinationDate;
    private String nextDueDate;
    private String examiner;
    private String examinerCompany;
    private String result;
    private String reportRef;
    private String notes;
    private String documentRef;
    private Boolean isDue;
    private Boolean isDueSoon;
}
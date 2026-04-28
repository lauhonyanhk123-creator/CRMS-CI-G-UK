package com.crms.dto.response;

import com.crms.domain.common.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteResponse {
    
    private Long id;
    private String name;
    private String siteCode;
    private Address address;
    private String gridReference;
    private String postcode;
    private Long clientId;
    private String clientName;
    private String status;
    private LocalDate startDate;
    private LocalDate completionDate;
    private LocalDate estimatedCompletionDate;
    private String notes;
}
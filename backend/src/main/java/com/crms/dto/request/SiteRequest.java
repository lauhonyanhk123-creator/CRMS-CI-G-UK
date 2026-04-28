package com.crms.dto.request;

import com.crms.domain.common.entity.Address;
import com.crms.domain.site.enums.SiteStatus;
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
public class SiteRequest {
    
    @NotBlank(message = "Site name is required")
    private String name;
    
    @NotBlank(message = "Site code is required")
    private String siteCode;
    
    private Address address;
    
    private String gridReference;
    
    private String postcode;
    
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    private SiteStatus status;
    
    private LocalDate startDate;
    
    private LocalDate completionDate;
    
    private LocalDate estimatedCompletionDate;
    
    private String notes;
}
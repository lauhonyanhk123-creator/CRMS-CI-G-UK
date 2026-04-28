package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationPeriod {
    
    private Long operativeId;
    
    private Long siteId;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String status;
}
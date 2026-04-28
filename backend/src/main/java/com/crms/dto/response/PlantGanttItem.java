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
public class PlantGanttItem {
    
    private Long plantId;
    
    private String plantRef;
    
    private String description;
    
    private String category;
    
    private String status;
    
    private List<AllocationPeriod> allocations;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationPeriod {
        private Long operativeId;
        private Long siteId;
        private String siteName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String status;
    }
}
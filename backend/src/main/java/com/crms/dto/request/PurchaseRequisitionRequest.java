package com.crms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequisitionRequest {
    
    @NotNull(message = "Site ID is required")
    private Long siteId;
    
    private Long contractId;
    
    private LocalDate requiredDate;
    
    private String notes;
    
    private List<RequisitionLineRequest> lines;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequisitionLineRequest {
        
        private String description;
        
        private BigDecimal quantity;
        
        private String unit;
        
        private BigDecimal unitPrice;
        
        private BigDecimal totalPrice;
        
        private Long materialId;
    }
}
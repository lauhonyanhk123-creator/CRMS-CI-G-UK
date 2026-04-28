package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantItemResponse {
    
    private Long id;
    private String plantRef;
    private String serialNumber;
    private String description;
    private String make;
    private String model;
    private Integer year;
    private String category;
    private BigDecimal weight;
    private String hireStatus;
    private Long supplierId;
    private String supplierName;
    private String telematicsId;
    private String quickHitchType;
    private String status;
    private BigDecimal dailyHireRate;
    private String notes;
}
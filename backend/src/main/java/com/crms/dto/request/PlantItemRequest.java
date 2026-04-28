package com.crms.dto.request;

import com.crms.domain.plant.enums.HireStatus;
import com.crms.domain.plant.enums.PlantCategory;
import com.crms.domain.plant.enums.PlantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantItemRequest {
    
    @NotBlank(message = "Plant reference is required")
    private String plantRef;
    
    private String serialNumber;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String make;
    
    private String model;
    
    private Integer year;
    
    @NotNull(message = "Category is required")
    private PlantCategory category;
    
    private BigDecimal weight;
    
    private HireStatus hireStatus;
    
    private Long supplierId;
    
    private String telematicsId;
    
    private String quickHitchType;
    
    private PlantStatus status;
    
    private BigDecimal dailyHireRate;
    
    private String notes;
}
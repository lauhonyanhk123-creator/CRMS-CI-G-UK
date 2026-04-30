package com.crms.dto.request;

import com.crms.domain.operative.enums.EmploymentStatus;
import com.crms.domain.operative.enums.OperativeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperativeRequest {
    
    @NotBlank(message = "Employee reference is required")
    private String employeeRef;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private LocalDate dateOfBirth;
    
    private String gender;
    
    private String nationality;
    
    private String niNumber;
    
    private BigDecimal utr;
    
    private LocalDate rightToWorkExpiry;
    
    private String rightToWorkDocType;
    
    private String passportNumber;
    
    private String bankSortCode;
    
    private String bankAccountNumber;
    
    private EmploymentStatus employmentStatus;
    
    private OperativeStatus status;
    
    private Long employerId;

    private Boolean hmrcVerified;
}
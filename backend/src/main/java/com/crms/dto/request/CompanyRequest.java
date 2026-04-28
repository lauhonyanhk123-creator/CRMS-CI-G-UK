package com.crms.dto.request;

import com.crms.domain.common.entity.Address;
import com.crms.domain.company.enums.CompanyType;
import com.crms.domain.subcontractor.enums.CisStatus;
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
public class CompanyRequest {
    
    @NotBlank(message = "Company name is required")
    private String name;
    
    @NotNull(message = "Company type is required")
    private CompanyType companyType;
    
    private String registrationNumber;
    
    private String vatNumber;
    
    private Address address;
    
    private String phone;
    
    private String email;
    
    private String website;
    
    private String sicCode;
    
    private CisStatus cisStatus;
    
    private String companiesHouseId;
    
    private String hmrcVerificationRef;
    
    private BigDecimal hmrcDeductionRate;
    
    private Boolean copVerified;
    
    private String bankName;
    
    private String bankSortCode;
    
    private String bankAccountNumber;
    
    private String bankAccountName;
    
    private String taxAddress;
}
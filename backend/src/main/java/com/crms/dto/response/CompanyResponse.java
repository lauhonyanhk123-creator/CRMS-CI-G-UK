package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    
    private Long id;
    
    private String name;
    
    private String companyType;
    
    private String registrationNumber;
    
    private String vatNumber;
    
    private Map<String, Object> address;
    
    private String phone;
    
    private String email;
    
    private String website;
    
    private String sicCode;
    
    private String cisStatus;
    
    private String companiesHouseId;
    
    private Map<String, Object> companiesHouseData;
    
    private String hmrcVerificationRef;
    
    private LocalDate hmrcVerificationDate;
    
    private String hmrcDeductionRate;
    
    private Boolean copVerified;
    
    private String bankName;
    
    private String bankSortCode;
    
    private String bankAccountNumber;
    
    private String bankAccountName;
    
    private String taxAddress;
    
    private String status;
}
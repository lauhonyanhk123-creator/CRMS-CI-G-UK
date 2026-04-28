package com.crms.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Companies House Company Profile Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySearchResult {
    
    private String companyNumber;
    private String title;
    private String companyType;
    private String companyStatus;
    private String jurisdiction;
    private String dateOfCreation;
    private String dateOfCessation;
    private AddressDto registeredOfficeAddress;
    private List<OfficerDto> officers;
    private List<ChargeDto> charges;
    private Map<String, Object> insolvencyCases;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private String addressLine1;
        private String addressLine2;
        private String careOf;
        private String country;
        private String locality;
        private String poBox;
        private String postalCode;
        private String premises;
        private String region;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfficerDto {
        private String name;
        private String officerRole;
        private String nationality;
        private String occupation;
        private LocalDate dateOfBirth;
        private AddressDto address;
        private LocalDate appointedDate;
        private LocalDate resignedDate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargeDto {
        private String chargeId;
        private String status;
        private String natureOfCharge;
        private String personsEntitled;
        private LocalDate createdDate;
        private LocalDate acquiredDate;
        private BigDecimal amountSecured;
    }
}

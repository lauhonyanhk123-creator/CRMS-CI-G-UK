package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperativeResponse {
    
    private Long id;
    private String employeeRef;
    private String firstName;
    private String lastName;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String nationality;
    private String niNumber;
    private String utr;
    private String rightToWorkExpiry;
    private String rightToWorkDocType;
    private String employmentStatus;
    private String status;
    private Long employerId;
    private String employerName;
}
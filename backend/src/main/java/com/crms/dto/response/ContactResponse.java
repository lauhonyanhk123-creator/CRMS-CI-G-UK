package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String title;
    private String position;
    private String email;
    private String phone;
    private String mobile;
    private String fax;
    private Long companyId;
    private String companyName;
    private String notes;
}
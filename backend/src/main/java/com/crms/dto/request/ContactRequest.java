package com.crms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {
    
    @NotNull(message = "Company ID is required")
    private Long companyId;
    
    @NotNull(message = "First name is required")
    private String firstName;
    
    @NotNull(message = "Last name is required")
    private String lastName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    
    private String mobile;
    
    private String jobTitle;
    
    private Boolean isPrimary;
    
    private String notes;
}
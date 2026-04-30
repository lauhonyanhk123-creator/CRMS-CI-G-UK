package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private Long id;
    private Long operativeId;
    private String cardType;
    private String scheme;
    private String cardNumber;
    private String expiryDate;
    private String photoUrl;
    private Boolean isVerified;
    private String lastCheckedAt;
    private String competencyRef;
    private Boolean isValid;
    private Boolean isExpiringSoon;
}
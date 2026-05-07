package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;

    private String refreshToken;

    private Long expiresIn;

    private UserResponse user;

    /** Present (and true) when the user has TOTP enabled; full JWT is withheld until the code is verified. */
    private Boolean requiresTotp;

    /** Short-lived token used to identify the pending TOTP challenge (no access claims). */
    private String totpChallengeToken;
}
package com.crms.security.totp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TotpSetupResponse {
    private String secret;
    private String qrDataUri;
    private String issuer;
}

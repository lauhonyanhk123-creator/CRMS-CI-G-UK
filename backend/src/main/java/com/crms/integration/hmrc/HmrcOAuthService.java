package com.crms.integration.hmrc;

import java.util.Map;

public interface HmrcOAuthService {

    /** Build the HMRC authorisation URL to redirect the user to. */
    String buildAuthorizationUrl(String state);

    /** Exchange the authorisation code for access + refresh tokens and persist them. */
    void exchangeCodeAndStore(String code, String contractorUtr);

    /**
     * Return a valid access token for the given contractor UTR.
     * Automatically uses the stored refresh token to obtain a new access token if needed.
     *
     * @throws IllegalStateException if no token exists (not yet authorised)
     */
    String getValidAccessToken(String contractorUtr);

    /** Return a status map describing the current authorisation state. */
    Map<String, Object> getStatus(String contractorUtr);

    /** Revoke and delete all stored tokens for the contractor. */
    void disconnect(String contractorUtr);
}

package com.crms.security.totp;

import com.crms.domain.user.entity.User;
import com.crms.domain.user.repository.UserRepository;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotpService {

    private static final String ISSUER = "CRMS CI G UK";

    private final UserRepository userRepository;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator(32);
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    /**
     * Generate a new TOTP secret and return a data-URI PNG QR code for the authenticator app.
     * The secret is NOT yet saved — the user must confirm a valid code first via enableTotp().
     */
    @Transactional
    public TotpSetupResponse setupTotp() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        if (user.isTotpEnabled()) {
            throw new ValidationException("TOTP 2FA is already enabled for this account");
        }

        String secret = secretGenerator.generate();
        // Persist the pending secret (not yet enabled — flag remains false)
        user.setTotpSecret(secret);
        userRepository.save(user);

        String qrDataUri = generateQrDataUri(username, secret);

        return TotpSetupResponse.builder()
                .secret(secret)
                .qrDataUri(qrDataUri)
                .issuer(ISSUER)
                .build();
    }

    /**
     * Confirm a TOTP code and flip totpEnabled to true, completing enrolment.
     */
    @Transactional
    public void enableTotp(String code) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        if (user.getTotpSecret() == null) {
            throw new ValidationException("Call /auth/totp/setup first");
        }
        if (user.isTotpEnabled()) {
            throw new ValidationException("TOTP 2FA is already enabled");
        }
        if (!codeVerifier.isValidCode(user.getTotpSecret(), code)) {
            throw new ValidationException("Invalid TOTP code");
        }

        user.setTotpEnabled(true);
        userRepository.save(user);
        log.info("TOTP 2FA enabled for user '{}'", username);
    }

    /**
     * Disable TOTP for the authenticated user (requires current password confirmed at controller level).
     */
    @Transactional
    public void disableTotp() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
        log.info("TOTP 2FA disabled for user '{}'", username);
    }

    /**
     * Verify a TOTP code against the user's stored secret.
     */
    public boolean verifyCode(User user, String code) {
        if (!user.isTotpEnabled() || user.getTotpSecret() == null) {
            return false;
        }
        return codeVerifier.isValidCode(user.getTotpSecret(), code);
    }

    private String generateQrDataUri(String username, String secret) {
        try {
            QrData data = new QrData.Builder()
                    .label(username)
                    .secret(secret)
                    .issuer(ISSUER)
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

            QrGenerator generator = new ZxingPngQrGenerator();
            byte[] imageData = generator.generate(data);
            String mimeType = generator.getImageMimeType();
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageData);
        } catch (Exception e) {
            log.error("Failed to generate TOTP QR code: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}

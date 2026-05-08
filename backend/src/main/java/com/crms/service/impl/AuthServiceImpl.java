package com.crms.service.impl;

import com.crms.domain.user.entity.User;
import com.crms.domain.user.repository.UserRepository;
import com.crms.dto.request.LoginRequest;
import com.crms.dto.request.RegisterRequest;
import com.crms.dto.response.AuthResponse;
import com.crms.dto.response.UserResponse;
import com.crms.exception.ValidationException;
import com.crms.security.JwtTokenProvider;
import com.crms.security.totp.TotpService;
import com.crms.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final TotpService totpService;
    
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Load user first so we can enforce lockout before attempting authentication
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ValidationException("Invalid username or password"));

        if (user.isLocked()) {
            throw new ValidationException("Account is temporarily locked due to too many failed login attempts. Please try again later.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception ex) {
            user.incrementFailedLoginAttempts();
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockoutEnd(java.time.LocalDateTime.now().plusMinutes(15));
                log.warn("Account '{}' locked after {} failed login attempts", request.getUsername(), user.getFailedLoginAttempts());
            }
            userRepository.save(user);
            throw new ValidationException("Invalid username or password");
        }

        // Successful login — reset failed attempts
        user.resetFailedLoginAttempts();
        user.setLockoutEnd(null);
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // If TOTP is enabled, return a challenge token instead of the full JWT
        if (user.isTotpEnabled()) {
            String challengeToken = tokenProvider.generateTotpChallengeToken(user.getUsername());
            return AuthResponse.builder()
                    .requiresTotp(true)
                    .totpChallengeToken(challengeToken)
                    .user(mapToUserResponse(user))
                    .build();
        }

        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(tokenProvider.getExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public AuthResponse completeTotpChallenge(String challengeToken, String totpCode) {
        if (!tokenProvider.validateToken(challengeToken)) {
            throw new ValidationException("Invalid or expired TOTP challenge token");
        }
        if (!tokenProvider.isTotpChallengeToken(challengeToken)) {
            throw new ValidationException("Invalid challenge token type");
        }

        String username = tokenProvider.getUsernameFromToken(challengeToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

        if (!totpService.verifyCode(user, totpCode)) {
            throw new ValidationException("Invalid TOTP code");
        }

        String token = tokenProvider.generateToken(user.getUsername(), user.getRoles());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(tokenProvider.getExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }
    
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .roles(Set.of(com.crms.domain.user.enums.Role.valueOf((request.getRole() != null ? request.getRole() : "ROLE_USER"))))
                .build();
        
        user = userRepository.save(user);
        
        String token = tokenProvider.generateToken(user.getUsername(), user.getRoles());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());
        
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(tokenProvider.getExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }
    
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new ValidationException("Invalid refresh token");
        }
        
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));
        
        String token = tokenProvider.generateToken(user.getUsername(), user.getRoles());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getUsername());
        
        return AuthResponse.builder()
                .token(token)
                .refreshToken(newRefreshToken)
                .expiresIn(tokenProvider.getExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }
    
    @Override
    public UserResponse getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        log.info("User '{}' changed their password", username);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles() == null ? java.util.Collections.emptySet() : user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))
                .enabled(user.getEnabled())
                .mustChangePassword(user.getMustChangePassword())
                .build();
    }
}
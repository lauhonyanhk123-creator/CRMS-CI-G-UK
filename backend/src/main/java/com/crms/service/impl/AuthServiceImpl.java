package com.crms.service.impl;

import com.crms.domain.user.entity.User;
import com.crms.domain.user.repository.UserRepository;
import com.crms.dto.request.LoginRequest;
import com.crms.dto.request.RegisterRequest;
import com.crms.dto.response.AuthResponse;
import com.crms.dto.response.UserResponse;
import com.crms.exception.ValidationException;
import com.crms.security.JwtTokenProvider;
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
    
    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ValidationException("User not found"));
        
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
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles() == null ? java.util.Collections.emptySet() : user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))
                .enabled(user.getEnabled())
                .build();
    }
}
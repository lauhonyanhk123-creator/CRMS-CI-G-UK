package com.crms.service;

import com.crms.dto.request.LoginRequest;
import com.crms.dto.request.RegisterRequest;
import com.crms.dto.response.AuthResponse;
import com.crms.dto.response.UserResponse;

public interface AuthService {
    
    AuthResponse login(LoginRequest request);
    
    AuthResponse register(RegisterRequest request);
    
    AuthResponse refreshToken(String refreshToken);
    
    UserResponse getProfile();

    void changePassword(String currentPassword, String newPassword);
}
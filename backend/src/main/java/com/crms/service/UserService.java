package com.crms.service;

import com.crms.dto.request.RegisterRequest;
import com.crms.dto.request.UpdateUserRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    PageResponse<UserResponse> listUsers(int page, int size, String sort);

    UserResponse createUser(RegisterRequest request);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void deleteUser(UUID id);
}

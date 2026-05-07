package com.crms.service.impl;

import com.crms.domain.user.entity.User;
import com.crms.domain.user.enums.Role;
import com.crms.domain.user.repository.UserRepository;
import com.crms.dto.request.RegisterRequest;
import com.crms.dto.request.UpdateUserRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.UserResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(int page, int size, String sort) {
        String safeSort = resolveSort(sort);
        Page<User> users = userRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, safeSort)));
        return PageResponse.<UserResponse>builder()
                .content(users.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public UserResponse createUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        Role role = parseRole(request.getRole(), Role.ROLE_USER);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .mustChangePassword(true)
                .roles(Set.of(role))
                .build();

        user = userRepository.save(user);
        log.info("Admin created user '{}' with role {}", user.getUsername(), role);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ValidationException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)  user.setLastName(request.getLastName());
        if (request.getEnabled() != null)   user.setEnabled(request.getEnabled());

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = request.getRoles().stream()
                    .map(r -> parseRole(r, null))
                    .collect(Collectors.toSet());
            user.getRoles().clear();
            user.getRoles().addAll(roles);
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setMustChangePassword(false);
            log.info("Admin reset password for user '{}'", user.getUsername());
        }

        user = userRepository.save(user);
        log.info("Admin updated user '{}'", user.getUsername());
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
        // Soft-delete: disable rather than hard-delete to preserve the audit trail
        user.setEnabled(false);
        userRepository.save(user);
        log.info("Admin disabled user '{}'", user.getUsername());
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles() == null
                        ? java.util.Collections.emptySet()
                        : user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .enabled(user.getEnabled())
                .build();
    }

    private Role parseRole(String roleName, Role fallback) {
        if (roleName == null || roleName.isBlank()) return fallback;
        try {
            return Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown role: " + roleName);
        }
    }

    private String resolveSort(String sort) {
        return switch (sort) {
            case "username", "email", "firstName", "lastName", "createdAt", "enabled" -> sort;
            default -> "username";
        };
    }
}

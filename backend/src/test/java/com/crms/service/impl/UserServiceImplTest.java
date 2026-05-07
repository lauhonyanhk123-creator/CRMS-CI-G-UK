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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        existingUser = User.builder()
                .id(userId)
                .username("jdoe")
                .email("jdoe@example.com")
                .password("encodedPass")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .mustChangePassword(false)
                .roles(new HashSet<>(Set.of(Role.ROLE_USER)))
                .build();
    }

    // ================================================================
    // LIST USERS
    // ================================================================

    @Nested
    @DisplayName("listUsers")
    class ListUsersTests {

        @Test
        @DisplayName("returns page of users")
        void listUsers_returnsPage() {
            Page<User> page = new PageImpl<>(List.of(existingUser), PageRequest.of(0, 20), 1);
            when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

            PageResponse<UserResponse> result = userService.listUsers(0, 20, "username");

            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
            assertEquals("jdoe", result.getContent().get(0).getUsername());
        }

        @Test
        @DisplayName("falls back to username sort for unknown sort field")
        void listUsers_unknownSort_usesUsernameFallback() {
            Page<User> page = new PageImpl<>(List.of());
            when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

            PageResponse<UserResponse> result = userService.listUsers(0, 10, "nonexistent");

            assertNotNull(result);
            verify(userRepository).findAll(any(org.springframework.data.domain.Pageable.class));
        }
    }

    // ================================================================
    // CREATE USER
    // ================================================================

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {

        @Test
        @DisplayName("creates user and sets mustChangePassword=true")
        void createUser_success() {
            RegisterRequest req = RegisterRequest.builder()
                    .username("newuser")
                    .email("new@example.com")
                    .password("secret1234")
                    .firstName("New")
                    .lastName("User")
                    .role("ROLE_ADMIN")
                    .build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("secret1234")).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });

            UserResponse resp = userService.createUser(req);

            assertNotNull(resp);
            assertEquals("newuser", resp.getUsername());
            assertTrue(resp.getMustChangePassword());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws on duplicate username")
        void createUser_duplicateUsername_throws() {
            RegisterRequest req = RegisterRequest.builder()
                    .username("jdoe").email("other@x.com").password("pass1234").build();
            when(userRepository.existsByUsername("jdoe")).thenReturn(true);

            assertThrows(ValidationException.class, () -> userService.createUser(req));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws on duplicate email")
        void createUser_duplicateEmail_throws() {
            RegisterRequest req = RegisterRequest.builder()
                    .username("unique").email("jdoe@example.com").password("pass1234").build();
            when(userRepository.existsByUsername("unique")).thenReturn(false);
            when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(true);

            assertThrows(ValidationException.class, () -> userService.createUser(req));
        }

        @Test
        @DisplayName("defaults to ROLE_USER when role is null")
        void createUser_nullRole_defaultsToRoleUser() {
            RegisterRequest req = RegisterRequest.builder()
                    .username("u2").email("u2@x.com").password("pass1234").role(null).build();
            when(userRepository.existsByUsername("u2")).thenReturn(false);
            when(userRepository.existsByEmail("u2@x.com")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("enc");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });

            UserResponse resp = userService.createUser(req);

            assertTrue(resp.getRoles().contains("ROLE_USER"));
        }
    }

    // ================================================================
    // UPDATE USER
    // ================================================================

    @Nested
    @DisplayName("updateUser")
    class UpdateUserTests {

        @Test
        @DisplayName("updates email, name, and enabled flag")
        void updateUser_basicFields() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail("new@x.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserRequest req = UpdateUserRequest.builder()
                    .email("new@x.com").firstName("Jane").enabled(false).build();

            UserResponse resp = userService.updateUser(userId, req);

            assertEquals("new@x.com", resp.getEmail());
            assertEquals("Jane", resp.getFirstName());
            assertFalse(resp.getEnabled());
        }

        @Test
        @DisplayName("resets password when newPassword supplied and clears mustChangePassword")
        void updateUser_passwordReset() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.encode("newPass1234")).thenReturn("encodedNew");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateUserRequest req = UpdateUserRequest.builder().newPassword("newPass1234").build();
            userService.updateUser(userId, req);

            assertFalse(existingUser.getMustChangePassword());
            assertEquals("encodedNew", existingUser.getPassword());
        }

        @Test
        @DisplayName("throws when user not found")
        void updateUser_notFound_throws() {
            UUID missing = UUID.randomUUID();
            when(userRepository.findById(missing)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.updateUser(missing, UpdateUserRequest.builder().build()));
        }

        @Test
        @DisplayName("throws when new email is already taken")
        void updateUser_duplicateEmail_throws() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail("taken@x.com")).thenReturn(true);

            UpdateUserRequest req = UpdateUserRequest.builder().email("taken@x.com").build();

            assertThrows(ValidationException.class, () -> userService.updateUser(userId, req));
        }
    }

    // ================================================================
    // DELETE USER (soft-delete)
    // ================================================================

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("disables user instead of hard-deleting")
        void deleteUser_disablesUser() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            userService.deleteUser(userId);

            assertFalse(existingUser.getEnabled());
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("throws when user not found")
        void deleteUser_notFound_throws() {
            UUID missing = UUID.randomUUID();
            when(userRepository.findById(missing)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(missing));
        }
    }
}

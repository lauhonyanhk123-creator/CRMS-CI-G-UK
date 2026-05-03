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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl.
 * Tests cover login, registration, token refresh, and profile retrieval.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(com.crms.domain.user.enums.Role.ROLE_USER))
                .enabled(true)
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .role("ROLE_USER")
                .build();
    }

    // ================================================================
    // LOGIN TESTS
    // ================================================================

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("login authenticates user and returns tokens")
        void login_returnsAuthResponse_onSuccess() {
            // Given
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "testuser", "password123");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(java.util.Optional.of(testUser));
            when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
            when(tokenProvider.generateRefreshToken(testUser.getUsername())).thenReturn("refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600L);

            // When
            AuthResponse response = authService.login(loginRequest);

            // Then
            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
            assertEquals("refresh-token", response.getRefreshToken());
            assertEquals(3600L, response.getExpiresIn());
            assertNotNull(response.getUser());
            assertEquals("testuser", response.getUser().getUsername());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("login throws exception when user not found")
        void login_throwsException_whenUserNotFound() {
            // Given
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "unknown", "password");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsername("unknown")).thenReturn(java.util.Optional.empty());

            // When/Then
            assertThrows(ValidationException.class, () -> authService.login(
                    LoginRequest.builder().username("unknown").password("password").build()));
        }
    }

    // ================================================================
    // REGISTRATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register creates new user and returns tokens")
        void register_createsUser_onSuccess() {
            // Given
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(UUID.randomUUID());
                return user;
            });
            when(tokenProvider.generateToken(anyString(), any())).thenReturn("jwt-token");
            when(tokenProvider.generateRefreshToken("newuser")).thenReturn("refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600L);

            // When
            AuthResponse response = authService.register(registerRequest);

            // Then
            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
            assertEquals("refresh-token", response.getRefreshToken());
            assertNotNull(response.getUser());
            assertEquals("newuser", response.getUser().getUsername());
            assertEquals("new@example.com", response.getUser().getEmail());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("register throws exception when username exists")
        void register_throwsException_whenUsernameExists() {
            // Given
            when(userRepository.existsByUsername("newuser")).thenReturn(true);

            // When/Then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> authService.register(registerRequest));
            assertEquals("Username already exists", exception.getMessage());
        }

        @Test
        @DisplayName("register throws exception when email exists")
        void register_throwsException_whenEmailExists() {
            // Given
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

            // When/Then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> authService.register(registerRequest));
            assertEquals("Email already exists", exception.getMessage());
        }

        @Test
        @DisplayName("register assigns default role when role is null")
        void register_assignsDefaultRole_whenRoleNull() {
            // Given
            RegisterRequest requestNoRole = RegisterRequest.builder()
                    .username("newuser")
                    .email("new@example.com")
                    .password("password123")
                    .role(null)
                    .build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(UUID.randomUUID());
                return user;
            });
            when(tokenProvider.generateToken(anyString(), any())).thenReturn("jwt-token");
            when(tokenProvider.generateRefreshToken("newuser")).thenReturn("refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600L);

            // When
            AuthResponse response = authService.register(requestNoRole);

            // Then
            assertNotNull(response);
            assertTrue(response.getUser().getRoles().contains("ROLE_USER"));
        }
    }

    // ================================================================
    // TOKEN REFRESH TESTS
    // ================================================================

    @Nested
    @DisplayName("Token Refresh Tests")
    class TokenRefreshTests {

        @Test
        @DisplayName("refreshToken returns new tokens on valid refresh token")
        void refreshToken_returnsNewTokens_onValidRefreshToken() {
            // Given
            String refreshToken = "valid-refresh-token";
            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getUsernameFromToken(refreshToken)).thenReturn("testuser");
            when(userRepository.findByUsername("testuser"))
                    .thenReturn(java.util.Optional.of(testUser));
            when(tokenProvider.generateToken("testuser", testUser.getRoles())).thenReturn("new-jwt-token");
            when(tokenProvider.generateRefreshToken("testuser")).thenReturn("new-refresh-token");
            when(tokenProvider.getExpirationTime()).thenReturn(3600L);

            // When
            AuthResponse response = authService.refreshToken(refreshToken);

            // Then
            assertNotNull(response);
            assertEquals("new-jwt-token", response.getToken());
            assertEquals("new-refresh-token", response.getRefreshToken());
        }

        @Test
        @DisplayName("refreshToken throws exception on invalid refresh token")
        void refreshToken_throwsException_onInvalidToken() {
            // Given
            String invalidToken = "invalid-refresh-token";
            when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

            // When/Then
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> authService.refreshToken(invalidToken));
            assertEquals("Invalid refresh token", exception.getMessage());
        }

        @Test
        @DisplayName("refreshToken throws exception when user not found")
        void refreshToken_throwsException_whenUserNotFound() {
            // Given
            String refreshToken = "valid-token";
            when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(tokenProvider.getUsernameFromToken(refreshToken)).thenReturn("unknownuser");
            when(userRepository.findByUsername("unknownuser")).thenReturn(java.util.Optional.empty());

            // When/Then
            assertThrows(ValidationException.class, () -> authService.refreshToken(refreshToken));
        }
    }

    // ================================================================
    // PROFILE TESTS
    // ================================================================

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {

        @Test
        @DisplayName("getProfile returns current user profile")
        void getProfile_returnsUserProfile() {
            // Given
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("testuser");
            SecurityContextHolder.setContext(securityContext);

            when(userRepository.findByUsername("testuser"))
                    .thenReturn(java.util.Optional.of(testUser));

            // When
            UserResponse response = authService.getProfile();

            // Then
            assertNotNull(response);
            assertEquals("testuser", response.getUsername());
            assertEquals("test@example.com", response.getEmail());
            assertEquals("Test", response.getFirstName());
            assertEquals("User", response.getLastName());
        }

        @Test
        @DisplayName("getProfile throws exception when user not found")
        void getProfile_throwsException_whenUserNotFound() {
            // Given
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("unknownuser");
            SecurityContextHolder.setContext(securityContext);

            when(userRepository.findByUsername("unknownuser")).thenReturn(java.util.Optional.empty());

            // When/Then
            assertThrows(ValidationException.class, () -> authService.getProfile());
        }
    }
}

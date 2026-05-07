package com.crms.service.impl;

import com.crms.domain.user.entity.User;
import com.crms.domain.user.enums.Role;
import com.crms.domain.user.repository.UserRepository;
import com.crms.exception.ValidationException;
import com.crms.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .email("alice@example.com")
                .password("encodedOldPass")
                .enabled(true)
                .mustChangePassword(true)
                .roles(Set.of(Role.ROLE_USER))
                .build();

        // Wire SecurityContext so changePassword() can read the current username
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", null);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    @DisplayName("changePassword succeeds with correct current password")
    void changePassword_success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass1234")).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> authService.changePassword("oldPass", "newPass1234"));

        assertEquals("encodedNewPass", user.getPassword());
        assertFalse(user.getMustChangePassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("changePassword throws when current password is wrong")
    void changePassword_wrongCurrentPassword_throws() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedOldPass")).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> authService.changePassword("wrongPass", "newPass1234"));

        assertEquals("Current password is incorrect", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword throws when user not found in repository")
    void changePassword_userNotFound_throws() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class,
                () -> authService.changePassword("any", "newPass1234"));
    }
}

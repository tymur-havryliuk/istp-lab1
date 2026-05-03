package com.istp.lab1.auth.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.istp.lab1.auth.controller.response.InternalUserResponse;
import com.istp.lab1.exception.UnauthorizedException;
import com.istp.lab1.user.dao.entity.UserEntity;
import com.istp.lab1.user.dao.entity.UserRole;
import com.istp.lab1.user.dao.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class InternalAuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InternalAuthServiceImpl internalAuthService;

    @Test
    void verifyCredentialsReturnsInternalUser() {
        UserEntity user = org.mockito.Mockito.mock(UserEntity.class);
        when(user.getId()).thenReturn(1001L);
        when(user.getFullName()).thenReturn("Gateway Teacher");
        when(user.getEmail()).thenReturn("teacher@example.com");
        when(user.getRole()).thenReturn(UserRole.TEACHER);
        when(user.getPasswordHash()).thenReturn("hash");
        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

        InternalUserResponse result = internalAuthService.verifyCredentials("teacher@example.com", "password123");

        assertThat(result).isEqualTo(new InternalUserResponse(1001L, "Gateway Teacher", "teacher@example.com", "TEACHER"));
    }

    @Test
    void verifyCredentialsRejectsMissingUser() {
        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalAuthService.verifyCredentials("teacher@example.com", "password123"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void verifyCredentialsRejectsWrongPassword() {
        UserEntity user = org.mockito.Mockito.mock(UserEntity.class);
        when(user.getPasswordHash()).thenReturn("hash");
        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> internalAuthService.verifyCredentials("teacher@example.com", "wrong"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }
}

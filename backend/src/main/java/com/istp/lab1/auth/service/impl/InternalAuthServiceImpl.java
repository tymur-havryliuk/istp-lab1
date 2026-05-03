package com.istp.lab1.auth.service.impl;

import com.istp.lab1.auth.controller.response.InternalUserResponse;
import com.istp.lab1.auth.service.api.InternalAuthService;
import com.istp.lab1.exception.UnauthorizedException;
import com.istp.lab1.user.dao.entity.UserEntity;
import com.istp.lab1.user.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalAuthServiceImpl implements InternalAuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public InternalUserResponse verifyCredentials(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
        }

        return new InternalUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}

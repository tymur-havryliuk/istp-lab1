package com.istp.lab1.auth.service.impl;

import com.istp.lab1.auth.controller.request.InternalRegisterRequest;
import com.istp.lab1.auth.controller.response.InternalUserResponse;
import com.istp.lab1.auth.service.api.InternalAuthService;
import com.istp.lab1.exception.BadRequestException;
import com.istp.lab1.exception.UnauthorizedException;
import com.istp.lab1.user.dao.entity.StudentEntity;
import com.istp.lab1.user.dao.entity.TeacherEntity;
import com.istp.lab1.user.dao.entity.UserEntity;
import com.istp.lab1.user.dao.entity.UserRole;
import com.istp.lab1.user.dao.repository.StudentRepository;
import com.istp.lab1.user.dao.repository.TeacherRepository;
import com.istp.lab1.user.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalAuthServiceImpl implements InternalAuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";
    private static final String EMAIL_EXISTS_MESSAGE = "Email is already registered";

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
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

    @Override
    @Transactional
    public InternalUserResponse register(InternalRegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException(EMAIL_EXISTS_MESSAGE);
        }

        UserRole role = request.role();
        UserEntity user = userRepository.save(new UserEntity(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                role
        ));

        if (role == UserRole.TEACHER) {
            String department = requireValue(request.department(), "department");
            teacherRepository.save(new TeacherEntity(user, department));
        } else {
            String groupName = requireValue(request.groupName(), "groupName");
            studentRepository.save(new StudentEntity(user, groupName));
        }

        return new InternalUserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }

    private String requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required");
        }
        return value.trim();
    }
}

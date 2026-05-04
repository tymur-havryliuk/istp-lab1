package com.istp.lab1.auth.controller.request;

import com.istp.lab1.user.dao.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InternalRegisterRequest(
        @NotBlank
        @Size(max = 100)
        String fullName,
        @NotBlank
        @Email
        @Size(max = 100)
        String email,
        @NotBlank
        @Size(min = 8, max = 255)
        String password,
        @NotNull
        UserRole role,
        String department,
        String groupName
) {
}

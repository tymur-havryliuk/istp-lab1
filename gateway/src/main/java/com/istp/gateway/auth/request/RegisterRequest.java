package com.istp.gateway.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
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
        String role,
        String department,
        String groupName
) {
}

package com.istp.lab1.auth.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InternalLoginRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) {
}

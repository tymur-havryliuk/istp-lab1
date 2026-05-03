package com.istp.lab1.auth.controller.response;

public record InternalUserResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}

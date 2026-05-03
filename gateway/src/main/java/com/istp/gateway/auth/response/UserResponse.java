package com.istp.gateway.auth.response;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}

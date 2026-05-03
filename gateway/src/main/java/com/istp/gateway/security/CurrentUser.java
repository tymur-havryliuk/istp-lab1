package com.istp.gateway.security;

public record CurrentUser(
        Long id,
        String email,
        UserRole role
) {
}

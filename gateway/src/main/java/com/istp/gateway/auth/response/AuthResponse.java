package com.istp.gateway.auth.response;

public record AuthResponse(
        String token,
        UserResponse user
) {
}

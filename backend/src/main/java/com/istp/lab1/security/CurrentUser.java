package com.istp.lab1.security;

import com.istp.lab1.user.dao.entity.UserRole;

public record CurrentUser(
        Long id,
        String email,
        UserRole role
) {
}
